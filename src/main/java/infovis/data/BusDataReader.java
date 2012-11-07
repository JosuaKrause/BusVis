package infovis.data;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A bus data reader obtains informations for a transit network and adds those
 * in a {@link BusDataBuilder}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface BusDataReader {

  /**
   * Reads information about a transit network from the given path with the
   * given character set.
   * 
   * @param local The local resource path or <code>null</code> if a direct path
   *          is specified.
   * @param path data file path
   * @param cs The character set
   * @return The bus data builder holding informations.
   * @throws IOException I/O Exception.
   */
  BusDataBuilder read(final String local, final String path, Charset cs)
      throws IOException;

}
