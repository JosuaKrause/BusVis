package jkit.io.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Provides an interface to write a CSV file.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class CSVWriter implements Closeable {

  /** The print writer. */
  private PrintWriter out;

  /** The delimiter. */
  private final char delimiter;

  /** The string indicator. */
  private final String string;

  /** Check string for invalid characters. */
  private final String check;

  /** Whether its the first cell in a row. */
  private boolean first;

  /**
   * Creates a csv writer with default delimiters.
   * 
   * @param out The print writer to write to.
   */
  public CSVWriter(final PrintWriter out) {
    this(out, ';', '"');
  }

  /**
   * Creates a csv writer with the given delimiters.
   * 
   * @param out The print writer to write to.
   * @param delimiter The cell delimiter.
   * @param string The string delimiter.
   */
  public CSVWriter(final PrintWriter out, final char delimiter,
      final char string) {
    this.out = out;
    this.delimiter = delimiter;
    this.string = "" + string;
    check = "\r\n" + delimiter + string;
    first = true;
  }

  /**
   * Writes a cell in the current row.
   * 
   * @param content The content of the cell.
   */
  public void writeCell(final String content) {
    ensureOpen();
    if(first) {
      first = false;
    } else {
      out.print(delimiter);
    }
    out.print(sanitize(content));
  }

  /**
   * Whether there are any invalid characters in the string.
   * 
   * @param haystack The string to check.
   * @param chars The invalid characters.
   * @return <code>true</code> if invalid characters are contained.
   */
  private static boolean hasChars(final String haystack, final String chars) {
    for(int i = 0; i < chars.length(); ++i) {
      if(haystack.indexOf(chars.charAt(i)) >= 0) return true;
    }
    return false;
  }

  /**
   * Sanitizes the given string.
   * 
   * @param content The string.
   * @return The sanitized string.
   */
  private String sanitize(final String content) {
    if(!hasChars(content, check)) return content;
    return string + content.replace(string, string + string) + string;
  }

  /** Starts the next row. */
  public void writeRow() {
    ensureOpen();
    out.println();
    first = true;
  }

  /** Ensures that the writer is still open. */
  private void ensureOpen() {
    if(out == null) throw new IllegalStateException("already closed");
  }

  @Override
  public void close() throws IOException {
    if(out != null) {
      if(!first) {
        out.println();
      }
      out.close();
      out = null;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

}
