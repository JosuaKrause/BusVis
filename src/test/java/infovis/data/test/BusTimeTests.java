package infovis.data.test;

import static org.junit.Assert.*;
import infovis.data.BusTime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for the time-based algorithms in {@link infovis.data.BusTime}.
 * 
 * @author Leo Woerteler
 */
public class BusTimeTests {

  /**
   * Trivial tests.
   */
  @Test
  public void trivial() {
    if(new BusTime(1, 2).equals(new BusTime(2, 1))) {
      fail("times are different!");
    }
    if(new BusTime(1, 2).equals(new BusTime(1, 1))) {
      fail("times are different!");
    }
    if(new BusTime(0, 0).equals(null)) {
      fail("null is different");
    }
    try {
      new BusTime(24, 0);
      fail("should throw an exception");
    } catch(final IllegalArgumentException e) {
      // ignore
    }
    try {
      new BusTime(-1, 0);
      fail("should throw an exception");
    } catch(final IllegalArgumentException e) {
      // ignore
    }
    try {
      new BusTime(12, 60);
      fail("should throw an exception");
    } catch(final IllegalArgumentException e) {
      // ignore
    }
    try {
      new BusTime(11, -1);
      fail("should throw an exception");
    } catch(final IllegalArgumentException e) {
      // ignore
    }
  }

  /**
   * Tests the natural ordering of the times.
   */
  @Test
  public void ordering() {
    final BusTime[] times = new BusTime[] {
        new BusTime(23, 59),
        new BusTime(12, 0),
        new BusTime(17, 23),
        new BusTime(0, 0),
        new BusTime(5, 42),
        new BusTime(17, 24),
    };
    final int[] perm = {
        3, 4, 1, 2, 5, 0,
    };
    final BusTime[] copy = Arrays.copyOf(times, times.length);
    Arrays.sort(copy);
    for(int i = 0; i < times.length; ++i) {
      if(!copy[i].equals(times[perm[i]])) {
        System.out.println(Arrays.toString(copy));
        fail("expected " + times[perm[i]] + " got " + copy[i] + " at " + i);
      }
    }
  }

  /**
   * Tests the dependent ordering of the times.
   */
  @Test
  public void dependentOrdering() {
    final BusTime[] times = new BusTime[] {
        new BusTime(23, 59),
        new BusTime(12, 0),
        new BusTime(17, 23),
        new BusTime(0, 0),
        new BusTime(5, 42),
        new BusTime(17, 22),
        new BusTime(17, 24),
        new BusTime(5, 43),
        new BusTime(17, 21),
        new BusTime(17, 25),
        new BusTime(5, 41),
        new BusTime(18, 15),
        new BusTime(18, 9),
        new BusTime(18, 10),
        new BusTime(17, 10),
        new BusTime(17, 0),
        new BusTime(17, 15),
        new BusTime(17, 14),
        new BusTime(17, 9),
    };
    final int[] perm = {
        2, 6, 9, 12, 13, 11, 0, 3, 10, 4, 7, 1, 15, 18, 14, 17, 16, 8, 5,
    };
    final BusTime[] copy = Arrays.copyOf(times, times.length);
    Arrays.sort(copy, new BusTime(17, 23).createRelativeComparator());
    for(int i = 0; i < times.length; ++i) {
      if(!copy[i].equals(times[perm[i]])) {
        System.out.println(Arrays.toString(copy));
        fail("expected " + times[perm[i]] + " got " + copy[i] + " at " + i);
      }
    }
  }

  /**
   * Tests difference calculation.
   */
  @Test
  public void diffCalc() {
    final BusTime[] times = new BusTime[] {
        new BusTime(17, 23),
        new BusTime(17, 24),
        new BusTime(23, 59),
        new BusTime(0, 0),
        new BusTime(5, 42),
        new BusTime(12, 0),
        new BusTime(17, 22),
    };
    final int[] diff = {
        0, 1, 37 + 59 + 5 * 60,
        37 + 6 * 60, 37 + 11 * 60 + 42,
        37 + 18 * 60, 24 * 60 - 1,
    };
    final BusTime ref = new BusTime(17, 23);
    for(int i = 0; i < times.length; ++i) {
      if(ref.minutesTo(times[i]) != diff[i]) {
        fail("expected " + diff[i] + " got " + ref.minutesTo(times[i]) + " minutes at "
            + i);
      }
    }
  }

  /**
   * Tests the hash value and equals.
   */
  @Test
  public void hashes() {
    final Set<BusTime> times = new HashSet<BusTime>();
    times.add(new BusTime(13, 37));
    if(!times.contains(new BusTime(13, 37))) {
      fail("should be contained");
    }
  }

}
