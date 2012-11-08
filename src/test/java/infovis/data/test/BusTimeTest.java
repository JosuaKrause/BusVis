package infovis.data.test;

import static org.hamcrest.CoreMatchers.*;
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
public class BusTimeTest {

  /**
   * Trivial tests.
   */
  @Test
  public void trivial() {
    assertThat("times are different!", new BusTime(1, 2), not(new BusTime(2, 1)));
    assertThat("times are different!", new BusTime(1, 2), not(new BusTime(1, 1)));
    assertFalse("times are different!", new BusTime(0, 0).equals(null));

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
    final BusTime[] copy = times.clone();
    Arrays.sort(copy);
    for(int i = 0; i < times.length; ++i) {
      assertSame("Position " + i, times[perm[i]], copy[i]);
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
    final BusTime[] copy = times.clone();
    Arrays.sort(copy, new BusTime(17, 23).createRelativeComparator());
    for(int i = 0; i < times.length; ++i) {
      assertSame("Position " + i, times[perm[i]], copy[i]);
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
      assertEquals("Position " + i, diff[i], ref.minutesTo(times[i]));
    }
  }

  /**
   * Tests the hash value and equals.
   */
  @Test
  public void hashes() {
    final Set<BusTime> times = new HashSet<BusTime>();
    times.add(new BusTime(13, 37));
    assertTrue("should be contained", times.contains(new BusTime(13, 37)));
  }

  /**
   * Later and earlier.
   */
  @Test
  public void later() {
    final BusTime time = new BusTime(12, 34);
    assertEquals(new BusTime(23, 0), time.later(-34 - 13 * 60, 0));
    assertEquals(new BusTime(23, 5), time.later(-34 - 13 * 60 + 5, 0));
    assertEquals(new BusTime(9, 59), time.later(-35 - 2 * 60, 0));
    assertEquals(new BusTime(10, 0), time.later(-34 - 2 * 60, 0));
    assertEquals(new BusTime(11, 59), time.later(-35, 0));
    assertEquals(new BusTime(12, 0), time.later(-34, 0));
    assertEquals(new BusTime(12, 33), time.later(-1, 0));
    assertEquals(new BusTime(12, 34), time.later(0, 0));
    assertEquals(new BusTime(12, 35), time.later(1, 0));
    assertEquals(new BusTime(12, 59), time.later(25, 0));
    assertEquals(new BusTime(13, 0), time.later(26, 0));
    assertEquals(new BusTime(23, 0), time.later(26 + 10 * 60, 0));
    assertEquals(new BusTime(9, 0), time.later(26 + 20 * 60, 0));
    assertEquals(new BusTime(9, 5), time.later(26 + 20 * 60 + 5, 0));
  }

}
