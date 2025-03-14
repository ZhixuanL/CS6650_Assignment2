package neu.cs6650;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedReader;
import org.json.JSONObject;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;


/**
 * Remember to change the IP address every time when connect to EC2!!!
 * Rebuild project and artifacts
 */
public class SkierServlet extends HttpServlet {
  private static final String QUEUE_NAME = "ski_queue"; // queue name
  private static final String RABBITMQ_HOST = "34.214.120.223"; // public IP address of ec2
  private Connection connection;
  private Channel channel;

  @Override
  public void init() throws ServletException {
    try {
      // Create factory to connect to RabbitMQ
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(RABBITMQ_HOST);
      factory.setPort(5672);
      factory.setUsername("myuser"); // RabbitMQ user name
      factory.setPassword("mypassword"); // RabbitMQ password

      // Create connection and channel
      connection = factory.newConnection();
      channel = connection.createChannel();

      // Declare a queue, and create one if it doesn't exist
      channel.queueDeclare(QUEUE_NAME, true, false, false, null);
    } catch (Exception e) {
      throw new ServletException("Failed to connect to RabbitMQ", e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    res.setStatus(HttpServletResponse.SC_OK);
    res.getWriter().write("{\"message\": \"This is the skiers API. Use POST to send data.\"}");
  }


  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");

    // read JSON request body
    StringBuilder sb = new StringBuilder();
    String line;
    try (BufferedReader reader = req.getReader()) {
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    }

    // parse JSON
    try {
      JSONObject json = new JSONObject(sb.toString());

      int skierID = json.getInt("skierID");
      int resortID = json.getInt("resortID");
      int liftID = json.getInt("liftID");
      int seasonID = json.getInt("seasonID");
      int dayID = json.getInt("dayID");
      int time = json.getInt("time");

      if (!isValid(skierID, resortID, liftID, seasonID, dayID, time)) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("{\"message\": \"Invalid parameters\"}");
        return;
      }

      // Send JSON data to RabbitMQ
      channel.basicPublish("", QUEUE_NAME, null, json.toString().getBytes());
      System.out.println("Sent to RabbitMQ: " + json.toString());

      // even Servlet sends data to RabbitMQ, it still has to tell Client "We've got your request"
      // Client doesn't care when Servet deals with data; it only cares whether Servlet has received their request
      // as long as Rabbit receives Servlet's message, doPost() replies HTTP 201
      res.setStatus(HttpServletResponse.SC_CREATED);
      res.getWriter().write("{\"message\": \"Lift ride recorded successfully\"}");

    } catch (Exception e) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("{\"message\": \"Invalid JSON format\"}");
    }
  }

  // will be automatically called by the servlet when Tomcat is shut down,
  // or EC2 is restarted
  @Override
  public void destroy() {
    try {
      if (channel != null) channel.close();
      if (connection != null) connection.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean isValid(int skierID, int resortID, int liftID, int seasonID, int dayID, int time) {
    return skierID > 0 && skierID <= 100000 &&
        resortID > 0 && resortID <= 10 &&
        liftID > 0 && liftID <= 40 &&
        seasonID == 2025 &&
        dayID == 1 &&
        time > 0 && time <= 360;
  }
}
