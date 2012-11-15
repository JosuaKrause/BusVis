/**
 * 
 */
package jkit.io.csv;

import static jkit.io.csv.CSVTest.EventType.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Tests for the csv reader.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class CSVTest {

  /**
   * The event type.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  public static enum EventType {
    /** Start. */
    START,
    /** End. */
    END,
    /** New row. */
    ROW,
    /** New column. */
    COL,
    /** Next cell. */
    CELL,
    /* end of declaration */;
  }

  private static final class Event {

    private final EventType type;

    private final String content;

    private final String row;

    private final String col;

    private final int r;

    private final int c;

    public Event(final EventType type, final int r, final int c) {
      this(type, r, c, null);
    }

    public Event(final EventType type, final int r, final int c,
        final String content) {
      this(type, r, c, content, null, null);
    }

    public Event(final EventType type, final int r, final int c,
        final String content, final String row, final String col) {
      this.type = type;
      this.content = content;
      this.row = row;
      this.col = col;
      this.r = r;
      this.c = c;
    }

    @Override
    public boolean equals(final Object obj) {
      if(obj == this) return true;
      if(obj == null) return false;
      if(!(obj instanceof Event)) return false;
      final Event e = (Event) obj;
      if(e.type != type) return false;
      if(e.r != r) return false;
      if(e.c != c) return false;
      if(row != null && e.row != null && !row.equals(e.row)) return false;
      if(col != null && e.col != null && !col.equals(e.col)) return false;
      if(content == null) return e.content == null;
      return content.equals(e.content);
    }

    @Override
    public int hashCode() {
      return toString().hashCode();
    }

    @Override
    public String toString() {
      return type + "[" + r + "," + c + "]"
          + (content != null ? "(\"" + content + "\")" : "")
          + (row != null ? "{r:" + row + "}" : "")
          + (col != null ? "{c:" + col + "}" : "");
    }

  }

  private final class TestHandler implements CSVHandler {

    private final List<Event> events;

    public TestHandler() {
      events = new ArrayList<Event>();
    }

    public void test(final Event[] es) {
      for(int i = 0; i < es.length; ++i) {
        if(events.size() <= i) {
          final StringBuilder res = new StringBuilder(
              "missing events: ");
          for(int j = i; j < es.length; ++j) {
            if(j != i) {
              res.append(", ");
            }
            res.append(es[j]);
          }
          throw new IllegalStateException(res.toString());
        }
        if(!es[i].equals(events.get(i))) throw new IllegalStateException(
            "expected " + es[i]
                + " got " + events.get(i));
      }
    }

    @Override
    public void cell(final CSVContext ctx, final String content) {
      events.add(new Event(CELL, ctx.row(), ctx.col(), content, ctx.rowName(),
          ctx.colName()));
    }

    @Override
    public void colTitle(final CSVContext ctx, final String title) {
      events.add(new Event(COL, ctx.row(), ctx.col(), title, ctx.rowName(),
          ctx.colName()));
    }

    @Override
    public void row(final CSVContext ctx) {
      events.add(new Event(ROW, ctx.row(), ctx.col(), null, ctx.rowName(),
          ctx.colName()));
    }

    @Override
    public void rowTitle(final CSVContext ctx, final String title) {
      events.add(new Event(ROW, ctx.row(), ctx.col(), title, ctx.rowName(),
          ctx.colName()));
    }

    @Override
    public void start(final CSVContext ctx) {
      events.add(new Event(START, ctx.row(), ctx.col(), null, null, null));
    }

    @Override
    public void end(final CSVContext ctx) {
      events.add(new Event(END, -2, -2, null, ctx.rowName(), ctx.colName()));
    }

  }

  private static final String NL = System.getProperty("line.separator");

  private static final String STR_TEST0 = "hallo;\"abc\"; buh ;\r\nbello;;"
      + "\"ab\"\"cd\";\"wu\r\nff\"\r\ngrr"
      + "rh;\"te;st\"\rblubb\nblubb;;";

  private static final Event[] EV_TEST0 = new Event[] {
      new Event(START, 0, 0), new Event(ROW, 0, 0),
      new Event(CELL, 0, 0, "hallo"), new Event(CELL, 0, 1, "abc"),
      new Event(CELL, 0, 2, " buh "), new Event(CELL, 0, 3, ""),
      new Event(ROW, 1, 0), new Event(CELL, 1, 0, "bello"),
      new Event(CELL, 1, 1, ""), new Event(CELL, 1, 2, "ab\"cd"),
      new Event(CELL, 1, 3, "wu" + NL + "ff"), new Event(ROW, 2, 0),
      new Event(CELL, 2, 0, "grrrh"), new Event(CELL, 2, 1, "te;st"),
      new Event(ROW, 3, 0), new Event(CELL, 3, 0, "blubbblubb"),
      new Event(CELL, 3, 1, ""), new Event(END, -2, -2)};

  private static final String STR_TEST1R =
      "abc;def;ghi\rjkl;mno;pqr\rstu;vwx;yz_";

  private static final String STR_TEST1RN =
      "abc;def;ghi\r\njkl;mno;pqr\r\nstu;vwx;yz_\r\n";

  private static final String STR_TEST1N =
      "abc;def;ghi\njkl;mno;pqr\nstu;vwx;yz_\n";

  private static final Event[] EV_TEST1 = new Event[] {
      new Event(START, 0, 0), new Event(ROW, 0, 0),
      new Event(CELL, 0, 0, "abc"), new Event(CELL, 0, 1, "def"),
      new Event(CELL, 0, 2, "ghi"), new Event(ROW, 1, 0),
      new Event(CELL, 1, 0, "jkl"), new Event(CELL, 1, 1, "mno"),
      new Event(CELL, 1, 2, "pqr"), new Event(ROW, 2, 0),
      new Event(CELL, 2, 0, "stu"), new Event(CELL, 2, 1, "vwx"),
      new Event(CELL, 2, 2, "yz_"), new Event(END, -2, -2)};

  private static final String STR_TEST2 = "a\"b;c\"d;e\"f;\"gh\"";

  private static final Event[] EV_TEST2 = new Event[] {
      new Event(START, 0, 0), new Event(ROW, 0, 0),
      new Event(CELL, 0, 0, "a\"b"), new Event(CELL, 0, 1, "c\"d"),
      new Event(CELL, 0, 2, "e\"f"), new Event(CELL, 0, 3, "gh"),
      new Event(END, -2, -2)};

  private static final String STR_TEST3 =
      "-;c1;\"c2\";c3\nr1;1;2;3\n\"r2\";4;5;\"6\"\n\"r3\";7;8;9\n";

  private static final Event[] EV_TEST3R = new Event[] {
      new Event(START, 0, -1), new Event(ROW, 0, -1, "-"),
      new Event(ROW, 0, 0), new Event(CELL, 0, 0, "c1"),
      new Event(CELL, 0, 1, "c2"), new Event(CELL, 0, 2, "c3"),
      new Event(ROW, 1, -1, "r1"), new Event(ROW, 1, 0),
      new Event(CELL, 1, 0, "1", "r1", null),
      new Event(CELL, 1, 1, "2", "r1", null),
      new Event(CELL, 1, 2, "3", "r1", null),
      new Event(ROW, 2, -1, "r2"), new Event(ROW, 2, 0),
      new Event(CELL, 2, 0, "4", "r2", null),
      new Event(CELL, 2, 1, "5", "r2", null),
      new Event(CELL, 2, 2, "6", "r2", null),
      new Event(ROW, 3, -1, "r3"), new Event(ROW, 3, 0),
      new Event(CELL, 3, 0, "7", "r3", null),
      new Event(CELL, 3, 1, "8", "r3", null),
      new Event(CELL, 3, 2, "9", "r3", null), new Event(END, -2, -2)};

  private static final Event[] EV_TEST3C = new Event[] {
      new Event(START, -1, 0), new Event(COL, -1, 0, "-"),
      new Event(COL, -1, 1, "c1"), new Event(COL, -1, 2, "c2"),
      new Event(COL, -1, 3, "c3"), new Event(ROW, 0, 0),
      new Event(CELL, 0, 0, "r1", null, "-"),
      new Event(CELL, 0, 1, "1", null, "c1"),
      new Event(CELL, 0, 2, "2", null, "c2"),
      new Event(CELL, 0, 3, "3", null, "c3"), new Event(ROW, 1, 0),
      new Event(CELL, 1, 0, "r2", null, "-"),
      new Event(CELL, 1, 1, "4", null, "c1"),
      new Event(CELL, 1, 2, "5", null, "c2"),
      new Event(CELL, 1, 3, "6", null, "c3"), new Event(ROW, 2, 0),
      new Event(CELL, 2, 0, "r3", null, "-"),
      new Event(CELL, 2, 1, "7", null, "c1"),
      new Event(CELL, 2, 2, "8", null, "c2"),
      new Event(CELL, 2, 3, "9", null, "c3"), new Event(END, -2, -2)};

  private static final Event[] EV_TEST3RC = new Event[] {
      new Event(START, -1, -1), new Event(COL, -1, 0, "c1"),
      new Event(COL, -1, 1, "c2"), new Event(COL, -1, 2, "c3"),
      new Event(ROW, 0, -1, "r1"),
      new Event(ROW, 0, 0, null, "r1", null),
      new Event(CELL, 0, 0, "1", "r1", "c1"),
      new Event(CELL, 0, 1, "2", "r1", "c2"),
      new Event(CELL, 0, 2, "3", "r1", "c3"),
      new Event(ROW, 1, -1, "r2"),
      new Event(ROW, 1, 0, null, "r2", null),
      new Event(CELL, 1, 0, "4", "r2", "c1"),
      new Event(CELL, 1, 1, "5", "r2", "c2"),
      new Event(CELL, 1, 2, "6", "r2", "c3"),
      new Event(ROW, 2, -1, "r3"),
      new Event(ROW, 2, 0, null, "r3", null),
      new Event(CELL, 2, 0, "7", "r3", "c1"),
      new Event(CELL, 2, 1, "8", "r3", "c2"),
      new Event(CELL, 2, 2, "9", "r3", "c3"), new Event(END, -2, -2)};

  private void doTest(final CSVReader reader, final String in,
      final Event[] valid) throws Exception {
    final TestHandler th = new TestHandler();
    reader.setHandler(th);
    reader.read(new StringReader(in));
    th.test(valid);
  }

  /**
   * Tests types of cells.
   * 
   * @throws Exception Exception.
   */
  @Test
  public void test0() throws Exception {
    doTest(new CSVReader(), STR_TEST0, EV_TEST0);
  }

  /**
   * Tests different line endings.
   * 
   * @throws Exception Exception.
   */
  @Test
  public void test1() throws Exception {
    doTest(new CSVReader(), STR_TEST1R, EV_TEST1);
    doTest(new CSVReader(), STR_TEST1RN, EV_TEST1);
    doTest(new CSVReader(), STR_TEST1N, EV_TEST1);
  }

  /**
   * Tests delimiters in cells.
   * 
   * @throws Exception Exception.
   */
  @Test
  public void test2() throws Exception {
    doTest(new CSVReader(), STR_TEST2, EV_TEST2);
  }

  /**
   * Tests different title modes.
   * 
   * @throws Exception Exception.
   */
  @Test
  public void test3() throws Exception {
    final CSVReader csv = new CSVReader();
    csv.setReadRowTitles(true);
    doTest(csv, STR_TEST3, EV_TEST3R);
    csv.setReadColTitles(true);
    doTest(csv, STR_TEST3, EV_TEST3RC);
    csv.setReadRowTitles(false);
    doTest(csv, STR_TEST3, EV_TEST3C);
  }

  /**
   * Tests csv writing.
   * 
   * @throws Exception Exception.
   */
  @Test
  public void test4() throws Exception {
    final CSVReader csv = new CSVReader();
    final String test = "abc;\";\";\"\"\"\"" + NL + "def;ghu;\"" + NL
        + "\";" + NL;
    final StringWriter out = new StringWriter();
    final CSVWriter cw = new CSVWriter(new PrintWriter(out));
    csv.setHandler(new CSVAdapter() {

      @Override
      public void cell(final CSVContext ctx, final String content) {
        cw.writeCell(content);
      }

      @Override
      public void row(final CSVContext ctx) {
        if(ctx.row() > 0) {
          cw.writeRow();
        }
      }

      @Override
      public void end(final CSVContext ctx) {
        try {
          cw.close();
        } catch(final IOException e) {
          throw new RuntimeException(e);
        }
      }

    });
    csv.read(new StringReader(test));
    if(!out.toString().equals(test)) throw new IllegalStateException(
        "expected \"" + test + "\" got \""
            + out.toString() + "\"");
  }
}
