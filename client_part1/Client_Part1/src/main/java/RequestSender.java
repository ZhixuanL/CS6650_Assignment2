import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestSender implements Runnable {
  private final HttpClient client;
  private final BlockingQueue<String> requestQueue;
  private final AtomicInteger successCount;
  private final AtomicInteger failureCount;

  public RequestSender(HttpClient client, BlockingQueue<String> requestQueue,
      AtomicInteger successCount, AtomicInteger failureCount) {
    this.client = client;
    this.requestQueue = requestQueue;
    this.successCount = successCount;
    this.failureCount = failureCount;
  }

  @Override
  public void run() {
    int processedRequests = 0;

    while (true) {
      try {
        // read data from the requestQueue
        String jsonBody = requestQueue.poll(ClientConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (jsonBody == null) break;  // end the thread if empty queue
        // System.out.println("Sending request: " + jsonBody);
        sendPostRequest(jsonBody);  // send the request if otherwise
        processedRequests++;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // System.out.println(Thread.currentThread().getName() + " processed " + processedRequests + " requests.");
  }

  private void sendPostRequest(String jsonBody) {
    int retryCount = 0;
    while (retryCount < ClientConfig.MAX_RETRIES) {   // the maximum times of retry
      try {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ClientConfig.SERVER_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // System.out.println("Response Code: " + response.statusCode());
        // System.out.println("Response Body: " + response.body());

        if (response.statusCode() == 201) {  // request successfully
          successCount.incrementAndGet();
          return;
        } else {                             // not successfully
          failureCount.incrementAndGet();
          System.out.println("Failed request with status code: " + response.statusCode());
          System.out.println("Response Body: " + response.body());
        }
      } catch (Exception e) {
        failureCount.incrementAndGet();
        System.out.println("Exception in sendPostRequest: " + e.getMessage());
        e.printStackTrace();
      }

      retryCount++;
      if (retryCount < ClientConfig.MAX_RETRIES) {           // retry after failure
        try {
          Thread.sleep(500 + new Random().nextInt(1000)); // sleep to avoid overload
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    System.out.println("Request failed after " + ClientConfig.MAX_RETRIES + "retries.");
  }
}