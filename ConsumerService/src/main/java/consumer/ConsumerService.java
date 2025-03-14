package consumer;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ConsumerService: Listens to RabbitMQ, retrieves messages, and processes them using MessageProcessor.
 */
public class ConsumerService {
  private static final int NUM_THREADS = 5; // Number of consumer threads (adjust based on needs)

  public static void main(String[] args) {
    ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(ConsumerConfig.RABBITMQ_HOST);
      factory.setUsername(ConsumerConfig.USERNAME);
      factory.setPassword(ConsumerConfig.PASSWORD);

      Connection connection = factory.newConnection();

      for (int i = 0; i < NUM_THREADS; i++) {
        executor.execute(() -> {
          try {
            Channel channel = connection.createChannel();
            channel.queueDeclare(ConsumerConfig.QUEUE_NAME, true, false, false, null);
            System.out.println("Consumer thread started, listening to RabbitMQ...");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
              String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
              System.out.println("Received message: " + message);

              try {
                MessageProcessor.processMessage(message); // Process message
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
              } catch (Exception e) {
                e.printStackTrace();
              }
            };

            channel.basicConsume(ConsumerConfig.QUEUE_NAME, false, deliverCallback, consumerTag -> {});

          } catch (IOException e) {
            e.printStackTrace();
          }
        });
      }

    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
    }
  }
}