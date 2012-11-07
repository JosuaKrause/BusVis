package infovis.data.gtfs;

import infovis.util.IOUtil;

import java.io.IOException;
import java.util.Collection;

/**
 * A general transfer feed specification (GTFS) implementation in order to
 * obtain our internal transit network format.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 *
 */
public class GTFSReader {

  /** The GTFS data provider. */
  private final GTFSDataProvider data;

  /**
   * Creates a GTFS reader for the given data provider.
   * 
   * @param data The GTFS data provider.
   */
  public GTFSReader(final GTFSDataProvider data) {
    this.data = data;
  }

  public void doStuff() {
    final Collection<GTFSRow> routes = data.routes();
    for(final GTFSRow r : routes) {
      System.out.println(r.getField("route_id") + ": " + r.getField("route_long_name"));
    }
  }

  public static void main(final String[] args) throws IOException {
    final GTFSReader reader = new GTFSReader(new ZipGTFSDataProvider(IOUtil.getURL(
        IOUtil.RESOURCES, "gtfs/gtfs.zip")));
    reader.doStuff();
  }

}
