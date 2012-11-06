package infovis.data.gtfs;

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

}
