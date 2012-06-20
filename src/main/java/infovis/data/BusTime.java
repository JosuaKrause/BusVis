package infovis.data;

import java.util.Calendar;
import java.util.Comparator;


/**
 * A bus time consists of an hour and a minute.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusTime implements Comparable<BusTime> {

  /**
   * Midnight.
   */
  public static final BusTime MIDNIGHT = new BusTime(0, 0);

  /**
   * The number of hours per day.
   */
  public static final int HOURS_PER_DAY = 24;

  /**
   * The number of minutes in an hour.
   */
  public static final int MINUTES_PER_HOUR = 60;

  /**
   * The number of milliseconds in one minute.
   */
  public static final long MILLISECONDS_PER_MINUTE = 60 * 1000;

  /**
   * The hour of the time.
   */
  private final int hour;

  /**
   * The minute of the time.
   */
  private final int minute;

  /**
   * Creates a new bus time.
   * 
   * @param hour The hour ranging from 0 to 23.
   * @param minute The minute ranging from 0 to 59.
   */
  public BusTime(final int hour, final int minute) {
    if(hour < 0 || hour >= HOURS_PER_DAY) throw new IllegalArgumentException(
        "hour out of range: " + hour);
    if(minute < 0 || minute >= MINUTES_PER_HOUR) throw new IllegalArgumentException(
        "minute out of range: " + minute);
    this.hour = hour;
    this.minute = minute;
  }

  /**
   * Getter.
   * 
   * @return The hour.
   */
  public int getHour() {
    return hour;
  }

  /**
   * Getter.
   * 
   * @return The minute.
   */
  public int getMinute() {
    return minute;
  }

  /**
   * Calculates the time until the given end. This method always returns
   * positive values even when the end time is before this time. The values are
   * wrapped around 24 hours.
   * 
   * @param end The end time.
   * @return The time until the end in minutes.
   */
  public int minutesTo(final BusTime end) {
    if(hour < end.hour) return (end.hour - hour) * MINUTES_PER_HOUR + end.minute - minute;
    if(end.hour < hour) return (HOURS_PER_DAY + end.hour - hour) * MINUTES_PER_HOUR
        + end.minute - minute;
    if(minute <= end.minute) return end.minute - minute;
    return HOURS_PER_DAY * MINUTES_PER_HOUR + end.minute - minute;
  }

  /**
   * Getter.
   * 
   * @param min The difference in minutes.
   * @return The time that is the given amount of minutes later.
   */
  public BusTime later(final int min) {
    final int newMin = minute + min;
    if(min < 0) {
      if(newMin >= 0) return new BusTime(hour, newMin);
      int nh = hour;
      int nm = newMin;
      // slow but secure :)
      while(nm < 0) {
        --nh;
        nm += MINUTES_PER_HOUR;
      }
      while(nh < 0) {
        nh += HOURS_PER_DAY;
      }
      return new BusTime(nh, nm);
    }
    final int newHour = hour + newMin / MINUTES_PER_HOUR;
    return new BusTime(newHour % HOURS_PER_DAY, newMin % MINUTES_PER_HOUR);
  }

  /**
   * Creates a comparator that assumes this {@link BusTime} as lowest possible
   * value. Meaning the {@link BusTime} one minute before this is considered the
   * largest value. The times wrap around 24 hours.
   * 
   * @return The comparator.
   */
  public Comparator<BusTime> createRelativeComparator() {
    final int curHour = hour;
    final int curMin = minute;
    return new Comparator<BusTime>() {

      @Override
      public int compare(final BusTime o1, final BusTime o2) {
        final int h1 = (o1.getHour() < curHour) ? HOURS_PER_DAY + o1.getHour()
            : ((o1.getHour() == curHour && o1.getMinute() < curMin) ? HOURS_PER_DAY
                + o1.getHour() : o1.getHour());
        final int h2 = (o2.getHour() < curHour) ? HOURS_PER_DAY + o2.getHour()
            : ((o2.getHour() == curHour && o2.getMinute() < curMin) ? HOURS_PER_DAY
                + o2.getHour() : o2.getHour());
        if(h1 != h2) return ((Integer) h1).compareTo(h2);
        // minutes can easily be compared
        return ((Integer) o1.getMinute()).compareTo(o2.getMinute());
      }

    };
  }

  @Override
  public int compareTo(final BusTime o) {
    if(hour != o.hour) return ((Integer) hour).compareTo(o.hour);
    return ((Integer) minute).compareTo(o.minute);
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == null) return false;
    final BusTime time = (BusTime) obj;
    return hour == time.hour && minute == time.minute;
  }

  @Override
  public int hashCode() {
    return hour * MINUTES_PER_HOUR + minute;
  }

  @Override
  public String toString() {
    return String.format("%s[%dh, %dmin]", getClass().getSimpleName(), hour, minute);
  }

  /**
   * Getter.
   * 
   * @return A pretty representation.
   */
  public String pretty() {
    final String min = "0" + minute;
    return hour + ":" + min.substring(min.length() - 2) + "h";
  }

  /**
   * Converts minutes into a readable string.
   * 
   * @param minutes The minutes.
   * @return The string.
   */
  public static String minutesToString(final int minutes) {
    final int min = Math.abs(minutes);
    final int h = min / MINUTES_PER_HOUR;
    final int m = min % MINUTES_PER_HOUR;
    return (minutes < 0 ? "-" : "") + (h > 0 ? h + " h" : "")
        + (m > 0 ? (h > 0 ? " " : "") + m + " min" : (h > 0 ? "" : "0 min"));
  }

  /**
   * Getter.
   * 
   * @return The current time.
   */
  public static BusTime now() {
    final Calendar calendar = Calendar.getInstance();
    return new BusTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
  }

}
