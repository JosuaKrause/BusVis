package infovis.data.gtfs;

import infovis.util.Objects;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import au.com.bytecode.opencsv.CSVReader;

/**
 * An lazy implementation of {@link GTFSDataProvider} for ZIP files.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class LazyGTFSDataProvider implements GTFSDataProvider {

  /** The source URL. */
  private URL url;

  /** The character set. */
  private Charset cs;

  @Override
  public void setSource(final URL url, final Charset cs) throws IOException {
    this.url = url;
    this.cs = cs;
  }

  /**
   * Reads the file with the given name.
   * 
   * @param name The name in the ZIP.
   * @return The iterator over the entries.
   * @throws IOException I/O Exception.
   */
  protected Iterator<GTFSRow> readFile(final String name) throws IOException {
    Objects.requireNonNull(name);
    final ZipInputStream zip = new ZipInputStream(url.openStream());
    boolean found = false;
    ZipEntry cur;
    while((cur = zip.getNextEntry()) != null) {
      if(name.equals(cur.getName())) {
        found = true;
        break;
      }
      zip.closeEntry();
    }
    if(!found) {
      zip.close();
      final List<GTFSRow> empty = Collections.emptyList();
      return empty.iterator();
    }
    final CSVReader reader = new CSVReader(new InputStreamReader(zip, cs), ',', '"');
    final String[] names = reader.readNext();
    if(names == null) {
      zip.closeEntry();
      zip.close();
      final List<GTFSRow> empty = Collections.emptyList();
      return empty.iterator();
    }
    final Iterator<GTFSRow> res = new Iterator<GTFSRow>() {

      private GTFSRow row;

      {
        fillNext();
      }

      private void fillNext() throws IOException {
        final String[] cur = reader.readNext();
        if(cur == null) {
          zip.closeEntry();
          zip.close();
          row = null;
          return;
        }
        row = getRow(names, cur);
      }

      @Override
      public boolean hasNext() {
        return row != null;
      }

      @Override
      public GTFSRow next() {
        if(row == null) throw new NoSuchElementException();
        final GTFSRow res = row;
        try {
          fillNext();
        } catch(final IOException e) {
          throw new IllegalStateException(e);
        }
        return res;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
    return res;
  }

  /**
   * Converts strings to a GTFS row.
   * 
   * @param names The column names.
   * @param cur The strings.
   * @return The row.
   */
  public static GTFSRow getRow(final String[] names, final String[] cur) {
    final Map<String, String> map = new HashMap<String, String>();
    final int len = Math.min(names.length, cur.length);
    for(int i = 0; i < len; ++i) {
      map.put(names[i], cur[i]);
    }
    final GTFSRow row = new GTFSRow() {

      @Override
      public boolean hasField(final String name) {
        return map.containsKey(name);
      }

      @Override
      public String getField(final String name) {
        return map.get(name);
      }

    };
    return row;
  }

  @Override
  public Iterable<GTFSRow> stops() {
    return new Iterable<GTFSRow>() {

      @Override
      public Iterator<GTFSRow> iterator() {
        try {
          return readFile("stops.txt");
        } catch(final IOException e) {
          throw new IllegalStateException(e);
        }
      }

    };
  }

  @Override
  public Iterable<GTFSRow> routes() {
    return new Iterable<GTFSRow>() {

      @Override
      public Iterator<GTFSRow> iterator() {
        try {
          return readFile("routes.txt");
        } catch(final IOException e) {
          throw new IllegalStateException(e);
        }
      }

    };
  }

  @Override
  public Iterable<GTFSRow> trips() {
    return new Iterable<GTFSRow>() {

      @Override
      public Iterator<GTFSRow> iterator() {
        try {
          return readFile("trips.txt");
        } catch(final IOException e) {
          throw new IllegalStateException(e);
        }
      }

    };
  }

  @Override
  public Iterable<GTFSRow> stopTimes() {
    return new Iterable<GTFSRow>() {

      @Override
      public Iterator<GTFSRow> iterator() {
        try {
          return readFile("stop_times.txt");
        } catch(final IOException e) {
          throw new IllegalStateException(e);
        }
      }

    };
  }

}
