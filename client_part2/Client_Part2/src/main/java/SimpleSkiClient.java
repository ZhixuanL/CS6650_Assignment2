import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;

public class SimpleSkiClient {
  private static final BlockingQueue<String> requestQueue = new LinkedBlockingQueue<>();
  private static final AtomicInteger successCount = new AtomicInteger(0);
  private static final AtomicInteger failureCount = new AtomicInteger(0);
  private static final List<Long> latencyList = Collections.synchronizedList(new ArrayList<>()); // 存储所有的延迟数据

  public static void main(String[] args) {
    System.out.println("Starting Client...");
    System.out.println("Threads used: " + ClientConfig.NUM_THREADS);

    // 生成数据
    LiftRideDataGenerator.generateData(requestQueue, ClientConfig.TOTAL_REQUESTS);

    // 创建线程池
    ExecutorService executor = Executors.newFixedThreadPool(ClientConfig.NUM_THREADS);
    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(60))
        .build();

    long startTime = System.currentTimeMillis();

    // 启动多个线程
    for (int i = 0; i < ClientConfig.NUM_THREADS; i++) {
      executor.execute(new RequestSender(client, requestQueue, successCount, failureCount, latencyList));
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

    // 打印统计信息
    printStatistics(latencyList, successCount.get(), failureCount.get(), totalTimeSeconds);
  }

  /**
   * 计算统计数据并打印
   */
  private static void printStatistics(List<Long> latencies, int success, int failure, double totalTime) {
    if (latencies.isEmpty()) {
      System.out.println("No latency data recorded.");
      return;
    }

    Collections.sort(latencies);

    double mean = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);
    long median = latencies.get(latencies.size() / 2);
    long p99 = latencies.get((int) (latencies.size() * 0.99));
    long min = latencies.get(0);
    long max = latencies.get(latencies.size() - 1);
    double throughput = success / totalTime;

    System.out.println("====== Results ======");
    System.out.println("Total Requests Sent: " + (success + failure));
    System.out.println("Successful Requests: " + success);
    System.out.println("Failed Requests: " + failure);
    System.out.println("Total Time Taken: " + totalTime + " seconds");
    System.out.println("Throughput: " + throughput + " requests/sec");
    System.out.println("Mean Latency: " + mean + " ms");
    System.out.println("Median Latency: " + median + " ms");
    System.out.println("99th Percentile Latency (p99): " + p99 + " ms");
    System.out.println("Min Latency: " + min + " ms");
    System.out.println("Max Latency: " + max + " ms");
  }
}