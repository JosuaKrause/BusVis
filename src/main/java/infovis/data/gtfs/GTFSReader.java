package infovis.data.gtfs;

import infovis.data.BusDataBuilder;
import infovis.data.BusDataReader;
import infovis.util.IOUtil;
import infovis.util.Objects;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A general transfer feed specification (GTFS) implementation in order to
 * obtain our internal transit network format.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 *
 */
public class GTFSReader implements BusDataReader {

  /** The GTFS data provider. */
  private final GTFSDataProvider data;

  /**
   * Creates a GTFS reader for the given data provider.
   * 
   * @param data The GTFS data provider.
   */
  public GTFSReader(final GTFSDataProvider data) {
    this.data = Objects.requireNonNull(data);
  }

  @Override
  public BusDataBuilder read(final String local, final String path, final Charset cs)
      throws IOException {
    data.setSource(IOUtil.getURL(local, path), cs);
    final BusDataBuilder builder = new BusDataBuilder(null);
    // TODO Auto-generated method stub
    return builder;
  }

}
