package infovis.data.test;

import static org.junit.Assert.*;
import infovis.data.BusDataBuilder;
import infovis.data.BusEdge;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;

import java.awt.Color;
import java.util.Iterator;

import org.junit.Test;

/**
 * Tests for the algorithms in {@link BusStation}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class BusStationTest {

  /** The example bus stations. */
  private static final BusStationManager MANAGER;

  static {
    final BusDataBuilder builder = new BusDataBuilder(null);
    final BusLine line = BusDataBuilder.createLine("1", Color.RED);
    final BusLine other = BusDataBuilder.createLine("2", Color.BLUE);

    final BusStation a = builder.createStation("a", 0, 0, 0, 0, 0);
    final BusStation b = builder.createStation("b", 1, 0, 0, 0, 0);
    final BusStation c = builder.createStation("c", 2, 0, 0, 0, 0);
    final BusStation d = builder.createStation("d", 3, 0, 0, 0, 0);
    final BusStation e = builder.createStation("e", 4, 0, 0, 0, 0);
    builder.addEdge(a, line, 0, c, new BusTime(3, 10), new BusTime(3, 13));
    builder.addEdge(a, line, 1, b, new BusTime(3, 10), new BusTime(3, 12));
    builder.addEdge(a, line, 2, d, new BusTime(3, 10), new BusTime(3, 11));
    builder.addEdge(b, line, 0, a, new BusTime(3, 10), new BusTime(3, 20));
    builder.addEdge(b, line, 2, c, new BusTime(3, 9), new BusTime(3, 10));
    builder.addEdge(c, line, 0, a, new BusTime(2, 0), new BusTime(2, 1));
    builder.addEdge(d, line, 5, a, new BusTime(0, 1), new BusTime(0, 2));
    builder.addEdge(d, line, 6, b, new BusTime(0, 2), new BusTime(0, 3));
    builder.addEdge(d, line, 7, c, new BusTime(0, 3), new BusTime(0, 4));
    builder.addEdge(d, line, 8, e, new BusTime(0, 4), new BusTime(0, 5));
    final BusStation f = builder.createStation("f", 5, 0, 0, 0, 0);
    final BusStation g = builder.createStation("g", 6, 0, 0, 0, 0);
    final BusStation h = builder.createStation("h", 7, 0, 0, 0, 0);
    builder.addEdge(e, line, 0, h, new BusTime(23, 59), new BusTime(0, 1));
    builder.addEdge(e, line, 1, h, new BusTime(0, 7), new BusTime(0, 0));
    builder.addEdge(e, line, 2, h, new BusTime(0, 0), new BusTime(0, 6));
    builder.addEdge(e, line, 3, h, new BusTime(0, 6), new BusTime(0, 8));
    builder.addEdge(e, line, 4, h, new BusTime(0, 50), new BusTime(1, 0));
    builder.addEdge(e, line, 5, f, new BusTime(0, 0), new BusTime(0, 2));
    builder.addEdge(e, other, 0, f, new BusTime(0, 0), new BusTime(0, 1));
    builder.addEdge(e, line, 6, g, new BusTime(0, 1), new BusTime(0, 3));
    builder.addEdge(f, line, 7, h, new BusTime(1, 2), new BusTime(1, 3));
    builder.addEdge(f, line, 5, h, new BusTime(0, 2), new BusTime(0, 5));
    builder.addEdge(g, other, 0, h, new BusTime(0, 3), new BusTime(0, 4));
    builder.addEdge(g, line, 6, h, new BusTime(0, 4), new BusTime(0, 7));
    builder.addEdge(g, line, 8, h, new BusTime(0, 1), new BusTime(0, 2));

    MANAGER = builder.finish();
  }

  /**
   * Tests simultaneous edges appearing in the right order.
   */
  @Test
  public void simultan() {
    final BusStation a = MANAGER.getForId(0);
    final int[] es = { 3, 1, 2};
    int i = 0;
    for(final BusEdge e : a.getEdges(new BusTime(3, 10))) {
      assertEquals(es[i++], e.getTo().getId());
    }
  }

  /**
   * Tests edges before the current edge appearing.
   */
  @Test
  public void fullTime() {
    final BusStation b = MANAGER.getForId(1);
    final int[] es = { 0, 2, 2, 0, 2, 0};
    int i = 0;
    for(final BusEdge e : b.getEdges(new BusTime(3, 10))) {
      assertEquals(es[i++], e.getTo().getId());
    }
    for(final BusEdge e : b.getEdges(new BusTime(0, 0))) {
      assertEquals(es[i++], e.getTo().getId());
    }
    for(final BusEdge e : b.getEdges(new BusTime(3, 15))) {
      assertEquals(es[i++], e.getTo().getId());
    }
  }

  /**
   * Tests bus stations with single edges.
   */
  @SuppressWarnings("unused")
  @Test
  public void singleEdge() {
    final BusStation c = MANAGER.getForId(2);
    int i = 0;
    for(final BusEdge e : c.getEdges(new BusTime(0, 0))) {
      ++i;
    }
    assertEquals("must have exactly one edge", 1, i);

    for(final BusEdge e : c.getEdges(new BusTime(3, 0))) {
      ++i;
    }
    assertEquals("must have exactly one edge", 2, i);

    for(final BusEdge e : c.getEdges(new BusTime(2, 1))) {
      ++i;
    }
    assertEquals("must have exactly one edge", 3, i);

    for(final BusEdge e : c.getEdges(new BusTime(2, 0))) {
      ++i;
    }

    assertEquals("must have exactly one edge", 4, i);
  }

  /**
   * Tests whether iterators of empty edge sets are empty.
   */
  @Test
  public void emptyEdges() {
    assertFalse(
        "must be empty",
        new BusDataBuilder(null).createStation("r", 12345, 0, 0, 0, 0).
        getEdges(new BusTime(12, 15)).iterator().hasNext());
  }

  /**
   * Test whether duplicate bus stations are detected.
   */
  @Test
  public void duplicateStation() {
    final BusDataBuilder builder = new BusDataBuilder(null);
    try {
      builder.createStation("ok", 0, 0, 0, 0, 0);
      builder.createStation("fail", 0, 0, 0, 0, 0);
      fail("bus stations must have unique ids");
    } catch(final IllegalArgumentException e) {
      // success
    }
  }

  /**
   * Trivial tests.
   */
  @Test
  public void trivial() {
    assertFalse(MANAGER.getForId(0).equals(null));
    try {
      MANAGER.setMaxTimeHours(-1);
      fail("must throw an exception");
    } catch(final IllegalArgumentException e) {
      // success
    }
    try {
      MANAGER.setMaxTimeHours(25);
      fail("must throw an exception");
    } catch(final IllegalArgumentException e) {
      // success
    }
  }

  /**
   * Tests special cases in the iteration of edges.
   */
  @Test
  public void iteration() {
    final BusStation d = MANAGER.getForId(3);
    final Iterator<BusEdge> it = d.getEdges(new BusTime(0, 0)).iterator();
    assertEquals(0, it.next().getTo().getId());
    assertTrue(it.hasNext());
    assertTrue("successive calls to hasNext", it.hasNext());
    assertEquals(1, it.next().getTo().getId());
    assertEquals(2, it.next().getTo().getId());
    assertEquals(4, it.next().getTo().getId());
    assertFalse(it.hasNext());
    assertEquals(null, it.next());
    assertEquals(null, it.next());
    assertFalse(it.hasNext());
    assertEquals(null, it.next());
    assertFalse(it.hasNext());
    final BusTime[] times = new BusTime[] { new BusTime(0, 0), new BusTime(0, 1),
        new BusTime(0, 2), new BusTime(0, 3), new BusTime(0, 4), new BusTime(0, 5)};
    final int[] ids = { 0, 1, 2, 4, 0, 1, 2, 4, 1, 2, 4, 0, 2, 4, 0, 1, 4, 0, 1, 2, 0, 1,
        2, 4};
    int i = 0;
    for(final BusTime start : times) {
      for(final BusEdge e : d.getEdges(start)) {
        assertEquals(ids[i++], e.getTo().getId());
      }
    }
  }
}
