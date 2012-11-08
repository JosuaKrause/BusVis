package infovis.data;

import static java.lang.Integer.*;
import infovis.data.csv.CSVBusDataReader;
import infovis.data.gtfs.GTFSReader;
import infovis.data.gtfs.ZipGTFSDataProvider;
import infovis.util.IOUtil;
import infovis.util.Objects;
import infovis.util.VecUtil;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loader for bus data in {@code CSV} format.
 * 
 * @author Leo Woerteler
 */
public final class BusDataBuilder {

  /** Maps the external bus station ids to the internal ones. */
  private final Map<String, Integer> idMap = new HashMap<String, Integer>();
  /** Maps a line id to a bus line. */
  private final Map<String, BusLine> lineMap = new HashMap<String, BusLine>();
  /** Bus stations. */
  private final List<BusStation> stations = new ArrayList<BusStation>();
  /** Map from station IDs to the bus edges originating at this station. */
  private final List<List<BusEdge>> edges = new ArrayList<List<BusEdge>>();
  /** Walking distances. */
  private final List<List<Integer>> walkingDists = new ArrayList<List<Integer>>();
  /** The overview resource URL. */
  private final URL overview;

  /**
   * Constructor taking the path of the CSV files.
   * 
   * @param overview The overview resource URL, possibly <code>null</code>
   */
  public BusDataBuilder(final URL overview) {
    this.overview = overview;
  }

  /** The default character set - CP-1252 for Excel compatibility. */
  private static final Charset DEFAULT_CS = IOUtil.CP1252;

  /**
   * Loads a bus station manager from the given path.
   * 
   * @param path The path.
   * @param cs The charset or <code>null</code>.
   * @return The bus station manager.
   * @throws IOException I/O Exception.
   */
  public static BusStationManager loadPath(final String path, final String cs)
      throws IOException {
    return load(null, path, cs != null ? Charset.forName(cs) : DEFAULT_CS);
  }

  /**
   * Loads a bus station manager from the default resource path.
   * 
   * @param path The city.
   * @return The bus station manager.
   * @throws IOException I/O Exception.
   */
  public static BusStationManager loadDefault(final String path) throws IOException {
    return load(IOUtil.RESOURCES, path, DEFAULT_CS);
  }

  /**
   * Loads the bus system data from CSV files.
   * 
   * @param local The local resource path or <code>null</code> if a direct path
   *          is specified.
   * @param path data file path
   * @param cs The character set
   * @return The bus manager holding informations.
   * @throws IOException I/O exception
   */
  public static BusStationManager load(final String local, final String path,
      final Charset cs) throws IOException {
    final BusDataReader in;
    if(path.endsWith(".zip")) {
      if(!IOUtil.UTF8.equals(cs)) {
        System.err.println("Warning: character set '" + cs.displayName()
            + "' is not 'UTF-8'! Use second command line argument to change");
      }
      in = new GTFSReader(new ZipGTFSDataProvider());
    } else {
      in = new CSVBusDataReader();
    }
    final BusStationManager mngr = in.read(local, path, cs).finish();
    if(mngr.getStations().isEmpty()) throw new IllegalArgumentException(
        "provided source '" + IOUtil.getURL(local, path)
        + "' does not contain any stations.");
    return mngr;
  }

  /**
   * Parses a {@link BusTime}.
   * 
   * @param time time string in seconds after midnight
   * @return resulting {@link BusTime}
   */
  public static BusTime parseTime(final String time) {
    return BusTime.MIDNIGHT.later(0, parseInt(time));
  }

  /**
   * Creates a new bus station.
   * 
   * @param name The name.
   * @param id The id. If the id is already used an
   *          {@link IllegalArgumentException} is thrown.
   * @param lat The latitude.
   * @param lon The longitude.
   * @param abstractX The abstract x position.
   * @param abstractY The abstract y position.
   * @return The newly created bus station.
   */
  public BusStation createStation(final String name, final String id, final double lat,
      final double lon, final double abstractX, final double abstractY) {
    if(idMap.containsKey(id)) throw new IllegalArgumentException(
        "bus id: " + id + " already in use");
    // keep bus station ids dense
    final int realId = stations.size();
    idMap.put(id, realId);
    final List<BusEdge> edgeList = new ArrayList<BusEdge>();
    edges.add(edgeList);
    final List<Integer> walking = new ArrayList<Integer>();
    walkingDists.add(walking);
    final BusStation bus = new BusStation(name, realId, lat, lon,
        abstractX, abstractY, edgeList, walking);
    stations.add(bus);
    return bus;
  }

  /**
   * Creates a new bus line.
   * 
   * @param id The id of the line.
   * @param line The name.
   * @param color The color.
   * @return The bus line.
   */
  public BusLine createLine(final String id, final String line, final Color color) {
    if(lineMap.containsKey(id)) throw new IllegalArgumentException(
        "line id: " + id + " already in use");
    final BusLine res = new BusLine(line, color);
    lineMap.put(id, res);
    return res;
  }

  /**
   * Gets the line from the line map.
   * 
   * @param id line ID
   * @return associated line
   * @throws IllegalArgumentException if the ID has no associated line
   */
  public BusLine getLine(final String id) {
    final BusLine line = lineMap.get(Objects.requireNonNull(id));
    if(line == null) throw new IllegalArgumentException("Unknown line: " + id);
    return line;
  }

  /**
   * Adds an edge to this bus station.
   * 
   * @param station bus station to start from
   * @param line bus line
   * @param tourNr tour number, unique per line
   * @param dest The destination
   * @param start The start time.
   * @param end The end time.
   * @return added edge
   */
  public BusEdge addEdge(final BusStation station, final BusLine line, final int tourNr,
      final BusStation dest, final BusTime start, final BusTime end) {
    final BusEdge edge = new BusEdge(line, tourNr, station, dest, start, end);
    edges.get(station.getId()).add(edge);
    return edge;
  }

  /**
   * Adds an edge to this bus station.
   * 
   * @param stationID bus station ID
   * @param line bus line
   * @param tourNr tour number, unique per line
   * @param destID The destination's station ID
   * @param start The start time.
   * @param end The end time.
   * @return added edge
   */
  public BusEdge addEdge(final String stationID, final BusLine line, final int tourNr,
      final String destID, final BusTime start, final BusTime end) {
    return addEdge(getStation(stationID), line, tourNr, getStation(destID), start, end);
  }

  /**
   * Sets the walking distance between two bus stations.
   * 
   * @param a first station
   * @param b second station
   * @param secs walking time in seconds
   */
  public void setWalkingDistance(final BusStation a, final BusStation b, final int secs) {
    final int id1 = a.getId(), id2 = b.getId();
    set(walkingDists.get(id1), id2, secs, -1);
    set(walkingDists.get(id2), id1, secs, -1);
  }

  /**
   * Sets the walking distance between two bus stations.
   * 
   * @param id1 first station's ID
   * @param id2 second station's ID
   * @param secs walking time in seconds
   */
  public void setWalkingDistance(final String id1, final String id2, final int secs) {
    setWalkingDistance(getStation(id1), getStation(id2), secs);
  }

  /** Computes walking distances for the current set of stations. */
  public void calcWalkingDistances() {
    int pa = 0;
    for(final BusStation a : stations) {
      int pb = 0;
      for(final BusStation b : stations) {
        if(pb >= pa) {
          break;
        }
        final double walkDist = VecUtil.earthDistance(a.getLatitude(),
            a.getLongitude(), b.getLatitude(), b.getLongitude());
        // assuming 5 km/h ie. 5000m / 3600s
        final int walkSecs = (int) Math.ceil(walkDist * 60.0 * 60.0 / 5000.0);
        setWalkingDistance(a, b, walkSecs);
        ++pb;
      }
      ++pa;
    }
  }

  /**
   * Sets the value at a specific position in a list. If the list is too short,
   * it is extended by the given default element.
   * 
   * @param <T> type of the list's elements
   * @param list the list
   * @param pos position
   * @param val value to be set
   * @param def default element
   */
  private static <T> void set(final List<T> list, final int pos, final T val, final T def) {
    while(list.size() < pos) {
      list.add(def);
    }
    if(list.size() == pos) {
      list.add(val);
    } else {
      list.set(pos, val);
    }
  }

  /**
   * Gets the station from the stations map.
   * 
   * @param id station ID
   * @return associated station
   * @throws IllegalArgumentException if the ID has no associated station
   */
  public BusStation getStation(final String id) {
    final BusStation station = stations.get(Objects.requireNonNull(idMap.get(id)));
    if(station == null) throw new IllegalArgumentException("Unknown station: " + id);
    return station;
  }


  /**
   * Finishes the building process and returns the bus station manager.
   * 
   * @return bus station manager
   */
  public BusStationManager finish() {
    for(final List<BusEdge> e : edges) {
      Collections.sort(e);
    }
    return new BusStationManager(stations, overview);
  }

}
