package consumer;

/**
 * Represents a single lift ride record for a skier.
 * Stores the skier's resort, season, and day information along with lift ride details.
 */
public class LiftRide {
  private final int resortID;
  private final int seasonID;
  private final int dayID;
  private final int liftID;
  private final int time;

  public LiftRide(int resortID, int seasonID, int dayID, int liftID, int time) {
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.liftID = liftID;
    this.time = time;
  }

  public int getResortID() {
    return resortID;
  }

  public int getSeasonID() {
    return seasonID;
  }

  public int getDayID() {
    return dayID;
  }

  public int getLiftID() {
    return liftID;
  }

  public int getTime() {
    return time;
  }

  @Override
  public String toString() {
    return "LiftRide{" +
        "resortID=" + resortID +
        ", seasonID=" + seasonID +
        ", dayID=" + dayID +
        ", liftID=" + liftID +
        ", time=" + time +
        '}';
  }
}