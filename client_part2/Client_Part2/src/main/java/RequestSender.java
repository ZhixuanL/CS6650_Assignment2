import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestSender implements Runnable {
  private final HttpClient client;
  private final BlockingQueue<String> requestQueue;
  private final AtomicInteger successCount;
  private final AtomicInteger failureCount;
  private final List<Long> latencyList;

  public RequestSender(HttpClient client, BlockingQueue<String> requestQueue,
      AtomicInteger successCount, AtomicInteger failureCount, List<Long> latencyList) {
    this.client = client;
    this.requestQueue = requestQueue;
    this.successCount = successCount;
    this.failureCount = failureCount;
    this.latencyList = latencyList;
  }

  @Override
  public void run() {
    boolean writeHeader = false;

    // **检查文件是否已存在，如果文件为空则写入表头**
    try {
      writeHeader = new java.io.File(ClientConfig.CSV_FILE_PATH).length() == 0;
    } catch (Exception ignored) {}

    try (FileWriter fw = new FileWriter(ClientConfig.CSV_FILE_PATH, true);
        PrintWriter writer = new PrintWriter(fw)) {

      // **如果文件为空，先写入表头**
      if (writeHeader) {
        writer.println("Start Time,Request Type,Latency(ms),Response Code");
        writer.flush();
      }

      while (true) {
        try {
          long startTime = System.currentTimeMillis();
          String jsonBody = requestQueue.poll(ClientConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS);
          if (jsonBody == null) break;

          int statusCode = sendPostRequest(jsonBody);
          long endTime = System.currentTimeMillis();

          long latency = endTime - startTime;
          latencyList.add(latency);

          // **确保数据严格按照 CSV 四列写入**
          writer.printf("%d,POST,%d,%d%n", startTime, latency, statusCode);
          writer.flush(); // 立即写入磁盘，防止数据丢失
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private int sendPostRequest(String jsonBody) {
    int retryCount = 0;
    while (retryCount < ClientConfig.MAX_RETRIES) {
      try {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ClientConfig.SERVER_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
          successCount.incrementAndGet();
          return 201;
        } else {
          failureCount.incrementAndGet();
        }
      } catch (Exception e) {
        failureCount.incrementAndGet();
      }

      retryCount++;
      if (retryCount < ClientConfig.MAX_RETRIES) {
        try {
          Thread.sleep(500 + new Random().nextInt(1000));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    return 500;
  }
}