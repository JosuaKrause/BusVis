package infovis.data.gtfs;

import java.util.Collection;


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
  Collection<GTFSRow> stops();

  /**
   * Getter.
   * 
   * @return Provides a reader of the content of the file
   *         <code>routes.txt</code>.
   */
  Collection<GTFSRow> routes();

  /**
   * Getter.
   * 
   * @return Provides a reader of the content of the file <code>trips.txt</code>
   *         .
   */
  Collection<GTFSRow> trips();

  /**
   * Getter.
   * 
   * @return Provides a reader of the content of the file
   *         <code>stop_times.txt</code>.
   */
  Collection<GTFSRow> stopTimes();

}
