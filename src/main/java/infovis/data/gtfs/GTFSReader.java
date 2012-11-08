package infovis.data.gtfs;

import static java.lang.Double.*;
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
    for(final GTFSRow row : data.stops()) {
      final String id = row.getField("stop_id");
      final String name = row.getField("stop_name");
      final double lat = parseDouble(row.getField("stop_lat"));
      final double lon = parseDouble(row.getField("stop_lon"));
      // TODO make alias for parent_station
      builder.createStation(name, id, lat, lon, NaN, NaN);
    }
    // TODO Auto-generated method stub
    return builder;
  }

}
