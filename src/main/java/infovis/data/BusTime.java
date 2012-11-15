package infovis.data;

import java.util.Calendar;
import java.util.Comparator;

/**
 * A bus time consists of an hour and a minute.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusTime implements Comparable<BusTime> {

  /** Midnight. */
  public static final BusTime MIDNIGHT = new BusTime(0);

  /** Bus time for 12:00 AM. */
  public static final BusTime NOON = new BusTime(12, 0);

  /** The number of hours per day. */
  public static final int HOURS_PER_DAY = 24;

  /** The number of minutes in an hour. */
  public static final int MINUTES_PER_HOUR = 60;

  /** The number of seconds in one minute. */
  public static final int SECONDS_PER_MINUTE = 60;

  /** The number of milliseconds in one second. */
  public static final int MILLISECONDS_PER_SECOND = 1000;

  /** The number of seconds in one day. */
  private static final int SECONDS_PER_DAY =
      HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE;

  /** Number of seconds since midnight. */
  private final int seconds;

  /**
   * Creates a new bus time.
   * 
   * @param hour The hour ranging from 0 to 23.
   * @param minute The minute ranging from 0 to 59.
   */
  public BusTime(final int hour, final int minute) {
    this(hour, minute, 0);
  }

  /**
   * Creates a new bus time.
   * 
   * @param hour The hour ranging from 0 to 23.
   * @param minute The minute ranging from 0 to 59.
   * @param second The second ranging from 0 to 59.
   */
  public BusTime(final int hour, final int minute, final int second) {
    this((checkRange("hour", hour, HOURS_PER_DAY) * MINUTES_PER_HOUR
        + checkRange("minute", minute, MINUTES_PER_HOUR)) * SECONDS_PER_MINUTE
        + checkRange("second", second, SECONDS_PER_MINUTE));
  }

  /**
   * Private constructor taking the number of seconds since midnight.
   * 
   * @param secondsSinceMidnight seconds since midnight
   */
  private BusTime(final int secondsSinceMidnight) {
    seconds = checkRange("seconds since midnight", secondsSinceMidnight, SECONDS_PER_DAY);
  }

  /**
   * Checks if the given value lies within the range {@code [0,max)}.
   * 
   * @param desc description of the value
   * @param val the value
   * @param max exclusive upper bound
   * @return the value for convenience
   * @throws IllegalArgumentException if the value lies outside the range
   */
  private static int checkRange(final String desc, final int val, final int max) {
    if(0 <= val && val < max) return val;
    throw new IllegalArgumentException(desc + " out of range: " + val);
  }

  /**
   * Getter.
   * 
   * @return The hour.
   */
  public int getHour() {
    return seconds / (SECONDS_PER_MINUTE * MINUTES_PER_HOUR);
  }

  /**
   * Getter.
   * 
   * @return The minute.
   */
  public int getMinute() {
    return (seconds / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR;
  }

  /**
   * Getter.
   * 
   * @return The second.
   */
  public int getSecond() {
    return seconds % SECONDS_PER_MINUTE;
  }

  /**
   * Calculates the time until the given end. This method always returns
   * positive values even when the end time is before this time. The values are
   * wrapped around 24 hours.
   * 
   * @param end The end time.
   * @return The time until the end in seconds.
   */
  public int secondsTo(final BusTime end) {
    return (end.seconds - seconds + SECONDS_PER_DAY) % SECONDS_PER_DAY;
  }

  /**
   * Getter.
   * 
   * @return The seconds from midnight.
   */
  public int secondsFromMidnight() {
    return seconds;
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
    return (secondsTo(end) + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE;
  }

  /**
   * Getter.
   * 
   * @param min minutes
   * @param seconds seconds
   * @return The time that is the given amount of time later.
   */
  public BusTime later(final int min, final int seconds) {
    final int newSecs = this.seconds + min * SECONDS_PER_MINUTE + seconds;
    return new BusTime(((newSecs % SECONDS_PER_DAY) + SECONDS_PER_DAY) % SECONDS_PER_DAY);
  }

  /**
   * Creates a comparator that assumes this {@link BusTime} as lowest possible
   * value. Meaning the {@link BusTime} one minute before this is considered the
   * largest value. The times wrap around 24 hours.
   * 
   * @return The comparator.
   */
  public Comparator<BusTime> createRelativeComparator() {
    return new Comparator<BusTime>() {

      @Override
      public int compare(final BusTime o1, final BusTime o2) {
        return BusTime.this.secondsTo(o1) - BusTime.this.secondsTo(o2);
      }

    };
  }

  @Override
  public int compareTo(final BusTime o) {
    return seconds - o.seconds;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof BusTime && seconds == ((BusTime) obj).seconds;
  }

  @Override
  public int hashCode() {
    return seconds;
  }

  /**
   * Whether the given time represents a blink second, ie a second when the
   * colon is not printed.
   * 
   * @return Whether it is a blink second.
   */
  public boolean isBlinkSecond() {
    return seconds % 2 != 0;
  }

  @Override
  public String toString() {
    return String.format("%s[%dh, %dmin, %dsec]", getClass().getSimpleName(),
        getHour(), getMinute(), getSecond());
  }

  /**
   * Getter.
   * 
   * @return A pretty representation.
   */
  public String pretty() {
    return pretty(false);
  }

  /**
   * Getter.
   * 
   * @param blink Whether the colon should not be printed
   * @return A pretty representation.
   */
  public String pretty(final boolean blink) {
    final String min = "0" + getMinute();
    return getHour() + (blink ? " " : ":") + min.substring(min.length() - 2) + "h";
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
   * Calculates the bus time for the given calendar object.
   * 
   * @param calendar The time to convert.
   * @return The converted time.
   */
  public static BusTime fromCalendar(final Calendar calendar) {
    return new BusTime(calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
  }

  /**
   * Getter.
   * 
   * @return The current time.
   */
  public static BusTime now() {
    return fromCalendar(Calendar.getInstance());
  }

}
