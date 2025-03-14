import org.json.JSONObject;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates random lift ride data for skiers.
 * Uses a thread pool to speed up data generation.
 * The generated data is stored in a BlockingQueue for later processing.
 */
public class LiftRideDataGenerator {
  private static final int THREAD_COUNT = 4; // number of threads used for data generation
  private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
  private static final AtomicInteger printCounter = new AtomicInteger(0); // Track printed messages
  /**
   * Generates random skier lift ride data and adds it to the provided queue.
   */
  public static void generateData(BlockingQueue<String> queue, int totalRequests) {
    int requestsPerThread = totalRequests / THREAD_COUNT;  // distributes tasks among threads

    for (int i = 0; i < THREAD_COUNT; i++) {
      executor.execute(() -> {
        try {
          for (int j = 0; j < requestsPerThread; j++) {
            JSONObject json = new JSONObject();
            json.put("skierID", (int) (Math.random() * 100000) + 1);
            json.put("liftID", (int) (Math.random() * 40) + 1);
            json.put("resortID", (int) (Math.random() * 10) + 1);
            json.put("seasonID", ClientConfig.SEASON_ID);
            json.put("dayID", 1);
            json.put("time", (int) (Math.random() * 360) + 1);

            queue.put(json.toString()); // adds data to BlockingQueue
          }
          // System.out.println(Thread.currentThread().getName() + " Data generation completed: " + requestsPerThread);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          e.printStackTrace();
        }
      });
    }

    executor.shutdown(); // Shut down the thread pool after all tasks are submitted
  }
}