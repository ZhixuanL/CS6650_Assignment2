package consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe data store for storing skier lift ride data.
 * Integer -- skierID
 * List<LiftRide> -- skier's lift ride data
 */
public class SkierDataStore {
  private static final ConcurrentHashMap<Integer, List<LiftRide>> skierLiftRides = new ConcurrentHashMap<>();


  // Stores a new lift ride record for a given skier.
  // if skierID doesn't exist, create a new ArrayList
  public static void addLiftRide(int skierID, LiftRide liftRide) {
    skierLiftRides.computeIfAbsent(skierID, k -> new ArrayList<>()).add(liftRide);
  }

  // Retrieves all lift rides for a given skier.
  // return an empty ArrayList if skierID doesn't exit
  public static List<LiftRide> getLiftRidesForSkier(int skierID) {
    return skierLiftRides.getOrDefault(skierID, new ArrayList<>());
  }
}