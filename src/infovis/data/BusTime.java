package infovis.data;

import java.util.Comparator;


/**
 * A bus time consists of an hour and a minute.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusTime implements Comparable<BusTime> {

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
   * @param minute The minute ranging from 0 to 60.
   */
  public BusTime(final int hour, final int minute) {
    if(hour < 0 || hour >= 24) throw new IllegalArgumentException("hour out of range: "
        + hour);
    if(minute < 0 || minute >= 60) throw new IllegalArgumentException(
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
        final int h1 = (o1.getHour() < curHour) ? 24 + o1.getHour() : o1.getHour();
        final int h2 = (o2.getHour() < curHour) ? 24 + o2.getHour() : o1.getHour();
        if(h1 != h2) return ((Integer) h1).compareTo(h2);
        if(h1 == curHour) { // look closely at the minutes
          final int m1 = (o1.getMinute() < curMin) ? 60 + o1.getMinute() : o1.getMinute();
          final int m2 = (o2.getMinute() < curMin) ? 60 + o2.getMinute() : o2.getMinute();
          return ((Integer) m1).compareTo(m2);
        }
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
    return hour + 31 * minute;
  }

}
