package infovis.data.gtfs;

import infovis.util.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import au.com.bytecode.opencsv.CSVReader;

/**
 * An implementation of {@link GTFSDataProvider} for ZIP files.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class ZipGTFSDataProvider implements GTFSDataProvider {

  /** The content of <code>stops.txt</code>. */
  private final Collection<GTFSRow> stops = new ArrayList<GTFSRow>();

  /** The content of <code>routes.txt</code>. */
  private final Collection<GTFSRow> routes = new ArrayList<GTFSRow>();

  /** The content of <code>trips.txt</code>. */
  private final Collection<GTFSRow> trips = new ArrayList<GTFSRow>();

  /** The content of <code>stop_times.txt</code>. */
  private final Collection<GTFSRow> stopTimes = new ArrayList<GTFSRow>();

  /**
   * Reads the given URL as ZIP file.
   * 
   * @param url The URL for the GTFS data.
   * @throws IOException I/O Exception.
   */
  public ZipGTFSDataProvider(final URL url) throws IOException {
    final Map<String, Collection<GTFSRow>> fileMap = new HashMap<String, Collection<GTFSRow>>();
    fileMap.put("stops.txt", stops);
    fileMap.put("routes.txt", routes);
    fileMap.put("trips.txt", trips);
    fileMap.put("stop_times.txt", stopTimes);
    final ZipInputStream zip = new ZipInputStream(url.openStream());
    ZipEntry cur;
    while((cur = zip.getNextEntry()) != null) {
      final Collection<GTFSRow> to = fileMap.get(cur.getName());
      if(to != null) {
        readFile(zip, to);
      }
      zip.closeEntry();
    }
    zip.close();
  }

  /**
   * Reads the content of an input stream.
   * 
   * @param in The input stream.
   * @param sink The collection to store the result in.
   * @throws IOException I/O Exception.
   */
  private static void readFile(final InputStream in, final Collection<GTFSRow> sink)
      throws IOException {
    readFile(new CSVReader(new InputStreamReader(in, IOUtil.UTF8), ',', '"'), sink);
  }

  /**
   * Reads the content of a {@link CSVReader}.
   * 
   * @param in The reader.
   * @param sink The collection to store the result in.
   * @throws IOException I/O Exception.
   */
  private static void readFile(final CSVReader in, final Collection<GTFSRow> sink)
      throws IOException {
    // first row are names
    final String[] names = in.readNext();
    if(names == null) return;
    String[] cur;
    while((cur = in.readNext()) != null) {
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
      sink.add(row);
    }
  }

  @Override
  public Collection<GTFSRow> stops() {
    return stops;
  }

  @Override
  public Collection<GTFSRow> routes() {
    return routes;
  }

  @Override
  public Collection<GTFSRow> trips() {
    return trips;
  }

  @Override
  public Collection<GTFSRow> stopTimes() {
    return stopTimes;
  }

}
