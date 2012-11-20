package infovis.data.gtfs;

import infovis.util.Objects;
import infovis.util.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jkit.io.csv.CSVReader;
import jkit.io.csv.CSVRow;

/**
 * An lazy implementation of {@link GTFSDataProvider} for ZIP files.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class LazyGTFSDataProvider implements GTFSDataProvider {

  /** The resource. */
  private Resource r;

  /** The CSV reader for GTFS files. */
  private final CSVReader reader = new CSVReader(',', '"', true, false);

  @Override
  public void setSource(final Resource r) throws IOException {
    this.r = r;
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
    final ZipInputStream zip = new ZipInputStream(r.getURL().openStream());
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
    final Iterator<CSVRow> it = CSVReader.readRows(
        new InputStreamReader(zip, r.getCharset()), reader);
    final Iterator<GTFSRow> res = new Iterator<GTFSRow>() {

      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public GTFSRow next() {
        return new GTFSRow() {

          private final CSVRow row = it.next();

          @Override
          public boolean hasField(final String name) {
            return row.has(name);
          }

          @Override
          public String getField(final String name) {
            return row.get(name);
          }

        };
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
    return res;
  }

  /**
   * Creates an iterable for the given file in the ZIP archive.
   * 
   * @param file The file.
   * @return The iterable.
   */
  private Iterable<GTFSRow> iterable(final String file) {
    return new Iterable<GTFSRow>() {

      @Override
      public Iterator<GTFSRow> iterator() {
        try {
          return readFile(file);
        } catch(final IOException e) {
          throw new IllegalStateException(e);
        }
      }

    };
  }

  @Override
  public Iterable<GTFSRow> stops() {
    return iterable("stops.txt");
  }

  @Override
  public Iterable<GTFSRow> routes() {
    return iterable("routes.txt");
  }

  @Override
  public Iterable<GTFSRow> trips() {
    return iterable("trips.txt");
  }

  @Override
  public Iterable<GTFSRow> stopTimes() {
    return iterable("stop_times.txt");
  }

  @Override
  public Iterable<GTFSRow> calendar() {
    return iterable("calendar.txt");
  }

  @Override
  public Iterable<GTFSRow> calendarDates() {
    return iterable("calendar_dates.txt");
  }

}
