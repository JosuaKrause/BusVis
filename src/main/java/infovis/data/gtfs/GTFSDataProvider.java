package infovis.data.gtfs;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;


/**
 * An interface providing some of the GTFS sources.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface GTFSDataProvider {

  /**
   * Sets the source of the data. Informations are replaced.
   * 
   * @param url The source of the data.
   * @param cs The character set.
   * @throws IOException I/O Exception.
   */
  void setSource(URL url, Charset cs) throws IOException;

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
