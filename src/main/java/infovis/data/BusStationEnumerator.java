package infovis.data;

import java.util.Collection;

/**
 * Enumerates all registered bus stations.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface BusStationEnumerator {

  /**
   * Getter.
   * 
   * @return All registered {@link BusStation}s.
   */
  Collection<BusStation> getStations();

  /**
   * Getter.
   * 
   * @return The highest id of all bus stations. The set of bus station ids is
   *         dense so this can be used to create an array mapping to bus
   *         stations. <code>Object[] map = new Object[enum.maxId()+1];</code>
   */
  int maxId();

  /**
   * Getter.
   * 
   * @param id The id of a bus station.
   * @return The bus station with the given id.
   */
  BusStation getForId(int id);

}
