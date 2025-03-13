package consumer;

import org.json.JSONObject;
/**
 * MessageProcessor: Parses JSON messages and stores skier lift ride data in SkierDataStore.
 */
public class MessageProcessor {
  // Processes a JSON message and stores it in the skier data store.
  // message: JSON message containing skier lift ride data
  public static void processMessage(String message) {
    try {
      JSONObject json = new JSONObject(message);
      int skierID = json.getInt("skierID");
      int liftID = json.getInt("liftID");
      int resortID = json.getInt("resortID");
      int dayID = json.getInt("dayID");
      int seasonID = json.getInt("seasonID");
      int time = json.getInt("time");

      LiftRide liftRide = new LiftRide(liftID, time, resortID, dayID, seasonID);
      SkierDataStore.addLiftRide(skierID, liftRide);

      System.out.println("Processed & stored lift ride for skierID: " + skierID);
    } catch (Exception e) {
      System.err.println("Error processing message: " + e.getMessage());
    }
  }
}