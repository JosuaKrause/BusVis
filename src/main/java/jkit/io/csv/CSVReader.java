/**
 * 
 */
package jkit.io.csv;

import infovis.util.IOUtil;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An easy to use csv reader. During the reading events are generated and passed
 * to the {@link CSVHandler} set by {@link #setHandler(CSVHandler)}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class CSVReader {

  private static final String NL = System.getProperty("line.separator");

  private final class Context implements CSVContext {

    private final List<String> colNames;

    private final boolean hasRowNames;

    private int col;

    private int row;

    private String rowName;

    public Context(final boolean hasColNames, final boolean hasRowNames) {
      this.hasRowNames = hasRowNames;
      colNames = hasColNames ? new LinkedList<String>() : null;
      rowName = null;
      row = hasColNames ? -1 : 0;
      col = hasRowNames ? -1 : 0;
    }

    public void addColName(final String name) {
      colNames.add(name);
    }

    public void setRowName(final String rowName) {
      this.rowName = rowName;
    }

    public void nextCell() {
      ++col;
    }

    public void nextRow() {
      ++row;
      col = hasRowNames ? -1 : 0;
    }

    @Override
    public int col() {
      return col;
    }

    @Override
    public String colName() {
      if(col < 0) return null;
      return colNames != null ? colNames.get(col) : "" + col;
    }

    @Override
    public int row() {
      return row;
    }

    @Override
    public String rowName() {
      return rowName != null ? rowName : "" + row;
    }

    @Override
    public CSVReader reader() {
      return CSVReader.this;
    }

    @Override
    public String toString() {
      return "ctx[" + row + ":" + col + "](" + rowName() + ":"
          + colName() + ")";
    }

  }

  private final char delimiter;

  private final char string;

  private boolean colTitle;

  private boolean rowTitle;

  private CSVHandler handler;

  /**
   * Creates a csv reader with default delimiters. ';' for cells and '"' for
   * strings.
   */
  public CSVReader() {
    this(';', '"');
  }

  /**
   * Creates a csv reader.
   * 
   * @param delimiter The cell delimiter.
   * @param string The string delimiter.
   */
  public CSVReader(final char delimiter, final char string) {
    this(delimiter, string, false, false);
  }

  /**
   * Creates a csv reader.
   * 
   * @param delimiter The cell delimiter.
   * @param string The string delimiter.
   * @param columnTitles Whether to interpret the first row as column titles.
   * @param rowTitles Wheter to interpret the first column of each row as row
   *          title.
   */
  public CSVReader(final char delimiter, final char string,
      final boolean columnTitles, final boolean rowTitles) {
    this.delimiter = delimiter;
    this.string = string;
    rowTitle = rowTitles;
    colTitle = columnTitles;
    handler = null;
  }

  public static final Iterable<CSVRow> readRows(final String local, final String path,
      final String file, final Charset cs, final CSVReader reader) throws IOException {
    if(!IOUtil.hasContent(IOUtil.getURL(local, path, file))) return null;
    return new Iterable<CSVRow>() {

      @Override
      public Iterator<CSVRow> iterator() {
        try {
          return readRows(IOUtil.reader(local, path, file, cs), reader);
        } catch(final IOException e) {
          throw new IllegalStateException(e);
        }
      }

    };
  }

  public static final Iterator<CSVRow> readRows(final Reader r, final CSVReader reader) {
    return new Iterator<CSVRow>() {

      private final CSVHandler handler = new CSVAdapter() {

        private CSVRow cur;

        private int len;

        @Override
        public void colTitle(final CSVContext ctx, final String title) {
          ++len;
        }

        @Override
        public void cell(final CSVContext ctx, final String content) {
          final String name = reader.readColTitles() ? ctx.colName() : null;
          final int i = ctx.col();
          len = Math.max(i + 1, len);
          if(cur == null) {
            cur = new CSVRow(len);
          }
          cur.addCell(i, name, content);
        }

        @Override
        public void row(final CSVContext ctx) {
          if(cur != null) {
            try {
              rows.put(cur);
            } catch(final InterruptedException e) {
              Thread.currentThread().interrupt();
            }
            cur = null;
          }
        }

        @Override
        public void end(final CSVContext ctx) {
          row(ctx);
          finish = true;
        }

      };

      protected final BlockingQueue<CSVRow> rows = new LinkedBlockingQueue<CSVRow>(10);

      protected volatile boolean finish = false;

      {
        final CSVHandler h = handler;
        final Thread runner = new Thread() {

          @Override
          public void run() {
            try {
              reader.setHandler(h);
              reader.read(r);
            } catch(final IOException e) {
              e.printStackTrace();
            } finally {
              finish = true;
            }
          }

        };
        runner.setDaemon(true);
        runner.start();
        fetchNext();
      }

      private CSVRow cur;

      private void fetchNext() {
        while((cur = rows.poll()) == null) {
          if(finish) return;
        }
      }

      @Override
      public boolean hasNext() {
        return cur != null;
      }

      @Override
      public CSVRow next() {
        final CSVRow row = cur;
        fetchNext();
        return row;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  /**
   * Reads from a reader.
   * 
   * @param r The reader.
   * @throws IOException If an I/O Exception occurs.
   */
  public void read(final Reader r) throws IOException {
    if(handler == null) throw new IllegalStateException(
        "handler needs to be set first");
    final CSVHandler hnd = handler;
    final Context ctx = new Context(colTitle, rowTitle);
    hnd.start(ctx);
    char ignore = 0x0;
    char line = 0x0;
    boolean canString = true;
    boolean isString = false;
    boolean endString = false;
    boolean afterLn = false;
    StringBuilder current = null;
    int i;
    while((i = r.read()) != -1) {
      if(current == null) {
        current = new StringBuilder();
      }
      final char c = (char) i;
      if(c == ignore && ignore != 0x0) {
        continue;
      }
      afterLn = false;
      if(line == 0x0 && (c == '\r' || c == '\n')) {
        line = c;
        ignore = (c == '\r') ? '\n' : '\r';
      }
      if(c == string) {
        if(!endString) {
          if(!isString) {
            if(canString) {
              isString = true;
              continue;
            }
          } else {
            endString = true;
            continue;
          }
        }
        endString = false;
      } else if(endString) {
        endString = false;
        isString = false;
      }
      if(c == delimiter && !isString) {
        handle(hnd, current.toString(), ctx);
        current = null;
        canString = true;
        continue;
      }
      canString = false;
      if(c == line) {
        if(isString) {
          current.append(NL);
        } else {
          handle(hnd, current.toString(), ctx);
          line(ctx);
          canString = true;
          current = null;
        }
        afterLn = true;
        continue;
      }
      current.append(c);
    }
    if(current != null && (current.length() > 0 || !afterLn)) {
      handle(hnd, current.toString(), ctx);
    }
    hnd.end(ctx);
  }

  private static void handle(final CSVHandler hnd, final String content,
      final Context ctx) {
    switch(ctx.col()) {
      case -1:
        if(ctx.row() < 0) {
          break;
        }
        ctx.setRowName(content);
        hnd.rowTitle(ctx, content);
        break;
      case 0:
        if(ctx.row() >= 0) {
          hnd.row(ctx);
        }
        // no break
        //$FALL-THROUGH$
      default:
        if(ctx.row() < 0) {
          ctx.addColName(content);
          hnd.colTitle(ctx, content);
          break;
        }
        hnd.cell(ctx, content);
        break;
    }
    ctx.nextCell();
  }

  private static void line(final Context ctx) {
    ctx.nextRow();
  }

  /**
   * Sets the csv handler.
   * 
   * @param handler The handler.
   */
  public void setHandler(final CSVHandler handler) {
    this.handler = handler;
  }

  /**
   * Setter.
   * 
   * @param colTitle Whether to interpret the first row as column titles.
   */
  public void setReadColTitles(final boolean colTitle) {
    this.colTitle = colTitle;
  }

  /**
   * Getter.
   * 
   * @return Whether the first row is interpreted as column titles.
   */
  public boolean readColTitles() {
    return colTitle;
  }

  /**
   * Setter.
   * 
   * @param rowTitle Whether to interpret the first column as row titles.
   */
  public void setReadRowTitles(final boolean rowTitle) {
    this.rowTitle = rowTitle;
  }

  /**
   * Getter.
   * 
   * @return Whether the first column is interpreted as row titles.
   */
  public boolean readRowTitles() {
    return rowTitle;
  }

}
