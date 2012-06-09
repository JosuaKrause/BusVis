package infovis.data.test;

import static org.junit.Assert.*;
import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusTime;

import java.util.Iterator;

import org.junit.Test;

/**
 * Tests for the algorithms in {@link BusStation}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class BusStationTests {

  static {
    final BusStation a = BusStation.createStation("a", 0);
    final BusStation b = BusStation.createStation("b", 1);
    final BusStation c = BusStation.createStation("c", 2);
    final BusStation d = BusStation.createStation("d", 3);
    final BusStation e = BusStation.createStation("e", 4);
    a.addEdge(c, new BusTime(3, 10), new BusTime(3, 13));
    a.addEdge(b, new BusTime(3, 10), new BusTime(3, 12));
    a.addEdge(d, new BusTime(3, 10), new BusTime(3, 11));
    b.addEdge(a, new BusTime(3, 10), new BusTime(3, 20));
    b.addEdge(c, new BusTime(3, 9), new BusTime(3, 5));
    c.addEdge(a, new BusTime(2, 0), new BusTime(2, 1));
    d.addEdge(a, new BusTime(0, 1), new BusTime(0, 2));
    d.addEdge(b, new BusTime(0, 2), new BusTime(0, 3));
    d.addEdge(c, new BusTime(0, 3), new BusTime(0, 4));
    d.addEdge(e, new BusTime(0, 4), new BusTime(0, 5));
  }

  /**
   * Tests simultan edges appearing in the right order.
   */
  @Test
  public void simultan() {
    final BusStation a = BusStation.getForId(0);
    final int[] es = { 3, 1, 2};
    int i = 0;
    for(final BusEdge e : a.getEdges(new BusTime(3, 10))) {
      if(e.getTo().getId() != es[i++]) {
        fail("expected edge " + es[i - 1] + " got " + e.getTo().getId() + " at "
            + (i - 1));
      }
    }
  }

  /**
   * Tests edges before the current edge appearing.
   */
  @Test
  public void fullTime() {
    final BusStation b = BusStation.getForId(1);
    final int[] es = { 0, 2, 2, 0, 2, 0};
    int i = 0;
    for(final BusEdge e : b.getEdges(new BusTime(3, 10))) {
      if(e.getTo().getId() != es[i++]) {
        fail("expected edge " + es[i - 1] + " got " + e.getTo().getId() + " at "
            + (i - 1));
      }
    }
    for(final BusEdge e : b.getEdges(new BusTime(0, 0))) {
      if(e.getTo().getId() != es[i++]) {
        fail("expected edge " + es[i - 1] + " got " + e.getTo().getId() + " at "
            + (i - 1));
      }
    }
    for(final BusEdge e : b.getEdges(new BusTime(3, 15))) {
      if(e.getTo().getId() != es[i++]) {
        fail("expected edge " + es[i - 1] + " got " + e.getTo().getId() + " at "
            + (i - 1));
      }
    }
  }

  /**
   * Tests bus stations with single edges.
   */
  @SuppressWarnings("unused")
  @Test
  public void singleEdge() {
    final BusStation c = BusStation.getForId(2);
    int i = 0;
    for(final BusEdge _ : c.getEdges(new BusTime(0, 0))) {
      ++i;
    }
    if(i != 1) {
      fail("must have exactly one edge");
    }
    for(final BusEdge _ : c.getEdges(new BusTime(3, 0))) {
      ++i;
    }
    if(i != 2) {
      fail("must have exactly one edge");
    }
    for(final BusEdge _ : c.getEdges(new BusTime(2, 1))) {
      ++i;
    }
    if(i != 3) {
      fail("must have exactly one edge");
    }
    for(final BusEdge _ : c.getEdges(new BusTime(2, 0))) {
      ++i;
    }
    if(i != 4) {
      fail("must have exactly one edge");
    }
  }

  /**
   * Tests whether iterators of empty edge sets are empty.
   */
  @Test
  public void emptyEdges() {
    assertFalse(
        "must be empty",
        BusStation.createStation("r", -1).getEdges(new BusTime(12, 15)).iterator().hasNext());
  }

  /**
   * Test whether duplicate bus stations are detected.
   */
  @Test
  public void duplicateStation() {
    try {
      BusStation.createStation("fail", 0);
      fail("bus stations must have unique ids");
    } catch(final IllegalArgumentException e) {
      // success
    }
  }

  /**
   * Tests special cases in the iteration of edges.
   */
  @Test
  public void iteration() {
    final BusStation d = BusStation.getForId(3);
    final Iterator<BusEdge> it = d.getEdges(new BusTime(0, 0)).iterator();
    assertEquals(0, it.next().getTo().getId());
    assertTrue(it.hasNext());
    assertTrue("succesive calls to hasNext", it.hasNext());
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
