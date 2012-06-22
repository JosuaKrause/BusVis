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

}
