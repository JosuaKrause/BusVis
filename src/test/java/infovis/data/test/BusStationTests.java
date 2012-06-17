package infovis.data.test;

import static org.junit.Assert.*;
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
public class BusStationTests {

  /**
   * The example bus stations.
   */
  private static BusStationManager manager = new BusStationManager(null);

  static {
    final BusLine line = new BusLine("1", Color.RED);
    final BusLine other = new BusLine("2", Color.BLUE);
    final BusStation a = manager.createStation("a", 0, 0, 0, 0, 0);
    final BusStation b = manager.createStation("b", 1, 0, 0, 0, 0);
    final BusStation c = manager.createStation("c", 2, 0, 0, 0, 0);
    final BusStation d = manager.createStation("d", 3, 0, 0, 0, 0);
    final BusStation e = manager.createStation("e", 4, 0, 0, 0, 0);
    a.addEdge(line, 0, c, new BusTime(3, 10), new BusTime(3, 13));
    a.addEdge(line, 1, b, new BusTime(3, 10), new BusTime(3, 12));
    a.addEdge(line, 2, d, new BusTime(3, 10), new BusTime(3, 11));
    b.addEdge(line, 0, a, new BusTime(3, 10), new BusTime(3, 20));
    b.addEdge(line, 2, c, new BusTime(3, 9), new BusTime(3, 10));
    c.addEdge(line, 0, a, new BusTime(2, 0), new BusTime(2, 1));
    d.addEdge(line, 5, a, new BusTime(0, 1), new BusTime(0, 2));
    d.addEdge(line, 6, b, new BusTime(0, 2), new BusTime(0, 3));
    d.addEdge(line, 7, c, new BusTime(0, 3), new BusTime(0, 4));
    d.addEdge(line, 8, e, new BusTime(0, 4), new BusTime(0, 5));
    final BusStation f = manager.createStation("f", 5, 0, 0, 0, 0);
    final BusStation g = manager.createStation("g", 6, 0, 0, 0, 0);
    final BusStation h = manager.createStation("h", 7, 0, 0, 0, 0);
    e.addEdge(line, 0, h, new BusTime(23, 59), new BusTime(0, 1));
    e.addEdge(line, 1, h, new BusTime(0, 7), new BusTime(0, 0));
    e.addEdge(line, 2, h, new BusTime(0, 0), new BusTime(0, 6));
    e.addEdge(line, 3, h, new BusTime(0, 6), new BusTime(0, 8));
    e.addEdge(line, 4, h, new BusTime(0, 50), new BusTime(1, 0));
    e.addEdge(line, 5, f, new BusTime(0, 0), new BusTime(0, 2));
    e.addEdge(other, 0, f, new BusTime(0, 0), new BusTime(0, 1));
    e.addEdge(line, 6, g, new BusTime(0, 1), new BusTime(0, 3));
    f.addEdge(line, 7, h, new BusTime(1, 2), new BusTime(1, 3));
    f.addEdge(line, 5, h, new BusTime(0, 2), new BusTime(0, 5));
    g.addEdge(other, 0, h, new BusTime(0, 3), new BusTime(0, 4));
    g.addEdge(line, 6, h, new BusTime(0, 4), new BusTime(0, 7));
    g.addEdge(line, 8, h, new BusTime(0, 1), new BusTime(0, 2));
  }

  /**
   * Tests simultaneous edges appearing in the right order.
   */
  @Test
  public void simultan() {
    final BusStation a = manager.getForId(0);
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
    final BusStation b = manager.getForId(1);
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
    final BusStation c = manager.getForId(2);
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
        manager.createStation("r", 12345, 0, 0, 0, 0).
        getEdges(new BusTime(12, 15)).iterator().hasNext());
  }

  /**
   * Test whether duplicate bus stations are detected.
   */
  @Test
  public void duplicateStation() {
    try {
      manager.createStation("fail", 0, 0, 0, 0, 0);
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
    assertFalse(manager.getForId(0).equals(null));
    try {
      manager.setMaxTimeHours(-1);
      fail("must throw an exception");
    } catch(final IllegalArgumentException e) {
      // success
    }
    try {
      manager.setMaxTimeHours(25);
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
    final BusStation d = manager.getForId(3);
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
