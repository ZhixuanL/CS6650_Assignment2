import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleSkiClient {
  private static final BlockingQueue<String> requestQueue = new LinkedBlockingQueue<>();
  private static final AtomicInteger successCount = new AtomicInteger(0);
  private static final AtomicInteger failureCount = new AtomicInteger(0);

  public static void main(String[] args) {
    System.out.println("Starting Client...");
    System.out.println("Threads used: " + ClientConfig.NUM_THREADS);

    // generate data
    LiftRideDataGenerator.generateData(requestQueue, ClientConfig.TOTAL_REQUESTS);

    // creates a thread pool to send request concurrently, increasing throughput
    ExecutorService executor = Executors.newFixedThreadPool(ClientConfig.NUM_THREADS);
    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(60))
        .build();

    long startTime = System.currentTimeMillis();

    // start multiple threads
    for (int i = 0; i < ClientConfig.NUM_THREADS; i++) {
      executor.execute(new RequestSender(client, requestQueue, successCount, failureCount));
    }
    executor.shutdown();

    try {
      executor.awaitTermination(10, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    long endTime = System.currentTimeMillis();
    double totalTimeSeconds = (endTime - startTime) / 1000.0;

    System.out.println("Queue Remaining Size: " + requestQueue.size());

    // print results
    System.out.println("====== Results ======");
    System.out.println("Total Requests Sent: " + (successCount.get() + failureCount.get()));
    System.out.println("Successful Requests: " + successCount.get());
    System.out.println("Failed Requests: " + failureCount.get());
    System.out.println("Total Time Taken: " + totalTimeSeconds + " seconds");
    System.out.println("Throughput: " + (successCount.get() / totalTimeSeconds) + " requests/sec");
  }
}