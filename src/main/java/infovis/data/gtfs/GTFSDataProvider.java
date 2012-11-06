package infovis.data.gtfs;

import au.com.bytecode.opencsv.CSVReader;

/**
 * An interface providing some of the GTFS sources.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface GTFSDataProvider {

  /**
   * Getter.
   * 
   * @return Provides a reader of the content of the file <code>stops.txt</code>
   *         .
   */
  CSVReader stops();

  /**
   * Getter.
   * 
   * @return Provides a reader of the content of the file
   *         <code>routes.txt</code>.
   */
  CSVReader routes();

  /**
   * Getter.
   * 
   * @return Provides a reader of the content of the file <code>trips.txt</code>
   *         .
   */
  CSVReader trips();

  /**
   * Getter.
   * 
   * @return Provides a reader of the content of the file
   *         <code>stop_times.txt</code>.
   */
  CSVReader stopTimes();

}
