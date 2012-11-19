package infovis.data.gtfs;

import infovis.util.Resource;

import java.io.IOException;


/**
 * An interface providing some of the GTFS sources.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface GTFSDataProvider {

  /**
   * Sets the source of the data. Informations are replaced.
   * 
   * @param r The resource.
   * @throws IOException I/O Exception.
   */
  void setSource(Resource r) throws IOException;

  /**
   * Getter.
   * 
   * @return Provides a reader for the content of the file
   *         <code>stops.txt</code> .
   */
  Iterable<GTFSRow> stops();

  /**
   * Getter.
   * 
   * @return Provides a reader for the content of the file
   *         <code>routes.txt</code>.
   */
  Iterable<GTFSRow> routes();

  /**
   * Getter.
   * 
   * @return Provides a reader for the content of the file
   *         <code>trips.txt</code> .
   */
  Iterable<GTFSRow> trips();

  /**
   * Getter.
   * 
   * @return Provides a reader for the content of the file
   *         <code>stop_times.txt</code>.
   */
  Iterable<GTFSRow> stopTimes();

  /**
   * Getter.
   * 
   * @return Provides a reader for the content of the file
   *         <code>calendar.txt</code>.
   */
  Iterable<GTFSRow> calendar();

  /**
   * Getter.
   * 
   * @return Provides a reader for the content of the file
   *         <code>calendar_dates.txt</code>.
   */
  Iterable<GTFSRow> calendarDates();

}
