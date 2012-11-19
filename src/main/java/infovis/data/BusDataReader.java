package infovis.data;

import infovis.util.Resource;

import java.io.IOException;

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
   * @param r The resource.
   * @return The bus data builder holding informations.
   * @throws IOException I/O Exception.
   */
  BusDataBuilder read(Resource r) throws IOException;

}
