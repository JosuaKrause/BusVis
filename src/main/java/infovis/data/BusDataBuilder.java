package infovis.data;

import static java.lang.Double.*;
import static java.lang.Integer.*;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Loader for bus data in {@code CSV} format.
 * 
 * @author Leo Woerteler
 */
public final class BusDataBuilder {

  /** Maps the external bus station ids to the internal ones. */
  private final Map<Integer, Integer> idMap = new HashMap<Integer, Integer>();
  /** Bus stations. */
  private final List<BusStation> stations = new ArrayList<BusStation>();
  /** Map from station IDs to the bus edges originating at this station. */
  private final List<List<BusEdge>> edges = new ArrayList<List<BusEdge>>();
  /** Walking distances. */
  private final List<List<Integer>> walkingDists = new ArrayList<List<Integer>>();
  /** The path to the resources. */
  private final String path;

  /**
   * Constructor taking the path of the CSV files.
   * 
   * @param path path of the CSV files, possibly <code>null</code>
   */
  public BusDataBuilder(final String path) {
    this.path = path;
  }

  /**
   * Loads the bus system data from CSV files.
   * 
   * @param path data file path
   * @return The bus manager holding the informations.
   * @throws IOException I/O exception
   */
  public static BusStationManager load(final String path) throws IOException {
    final BusDataBuilder builder = new BusDataBuilder(path);
    final File root = new File(path).getCanonicalFile();
    if(!root.exists()) throw new IllegalArgumentException(root + " does not exist.");

    final CSVReader stops = readerFor(new File(root, "stops.csv"));
    for(String[] stop; (stop = stops.readNext()) != null;) {
      double abstractX, abstractY;
      if(stop[4].equals("UNKNOWN")) {
        abstractX = abstractY = Double.NaN;
      } else {
        abstractX = parseDouble(stop[4]);
        abstractY = parseDouble(stop[5]);
      }
      builder.createStation(stop[0], parseInt(stop[1]), parseDouble(stop[3]) * 10000,
          -parseDouble(stop[2]) * 10000, abstractX, abstractY);
    }

    final CSVReader walk = readerFor(new File(root, "walking-dists.csv"));
    for(String[] line; (line = walk.readNext()) != null;) {
      builder.setWalkingDistance(parseInt(line[0]), parseInt(line[1]), parseInt(line[2]));
    }

    final Map<String, Color> colors = new HashMap<String, Color>();
    final CSVReader colorReader = readerFor(new File(root, "linecolor.csv"));
    for(String[] line; (line = colorReader.readNext()) != null;) {
      colors.put(line[0].replace('_', '/'),
          new Color(parseInt(line[1]), parseInt(line[2]), parseInt(line[3])));
    }

    final Map<String, BusLine> lines = new HashMap<String, BusLine>();
    for(final File line : new File(root, "lines").listFiles()) {
      final String name = line.getName().replace('_', '/').replace(".csv", "");

      if(!lines.containsKey(name)) {
        final Color color = colors.get(name);
        lines.put(name, createLine(name, color != null ? color
            : colors.get(name.replaceAll("\\D.*", ""))));
      }
      final BusLine busLine = lines.get(name);

      final CSVReader lineReader = readerFor(line);
      int tourNr = 0;
      for(String[] tour; (tour = lineReader.readNext()) != null; tourNr++) {
        int beforeID = -1;
        BusTime depart = null;
        for(int i = 0; i < tour.length; i++) {
          final int currentID = parseInt(tour[i++]);
          final BusTime arrive = parseTime(tour[i++]);

          if(beforeID >= 0) {
            builder.addEdge(beforeID, busLine, tourNr, currentID, depart, arrive);
          }

          if("-1".equals(tour[i])) {
            break;
          }
          beforeID = currentID;
          depart = parseTime(tour[i]);
        }
      }
    }
    return builder.finish();
  }

  /**
   * Parses a {@link BusTime}.
   * 
   * @param time time string
   * @return resulting {@link BusTime}
   */
  private static BusTime parseTime(final String time) {
    final String[] parts = time.split(":");
    return new BusTime(parseInt(parts[0]), parseInt(parts[1]), 0);
  }

  /**
   * Creates a {@link CSVReader} suitable for Microsoft Excel CSV files.
   * 
   * @param file CSV file
   * @return reader
   * @throws IOException I/O exception
   */
  private static CSVReader readerFor(final File file) throws IOException {
    return new CSVReader(new InputStreamReader(new BufferedInputStream(
        new FileInputStream(file)), "CP1252"), ';');
  }

  /**
   * Creates a new bus station.
   * 
   * @param name The name.
   * @param id The id. If the id is already used an
   *          {@link IllegalArgumentException} is thrown.
   * @param x The x position.
   * @param y The y position.
   * @param abstractX The abstract x position.
   * @param abstractY The abstract y position.
   * @return The newly created bus station.
   */
  public BusStation createStation(final String name, final int id, final double x,
      final double y, final double abstractX, final double abstractY) {
    if(idMap.containsKey(id)) throw new IllegalArgumentException("id: " + id
        + " already in use");
    // keep bus station ids dense
    final int realId = stations.size();
    idMap.put(id, realId);
    final List<BusEdge> edgeList = new ArrayList<BusEdge>();
    edges.add(edgeList);
    final List<Integer> walking = new ArrayList<Integer>();
    walkingDists.add(walking);
    final BusStation bus = new BusStation(name, realId, x, y, abstractX, abstractY,
        edgeList, walking);
    stations.add(bus);
    return bus;
  }

  /**
   * Creates a new bus line.
   * 
   * @param line The name.
   * @param color The color.
   * @return The bus line.
   */
  public static BusLine createLine(final String line, final Color color) {
    return new BusLine(line, color);
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
  public BusEdge addEdge(final int stationID, final BusLine line, final int tourNr,
      final int destID, final BusTime start, final BusTime end) {
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
  public void setWalkingDistance(final int id1, final int id2, final int secs) {
    setWalkingDistance(getStation(id1), getStation(id2), secs);
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
  private BusStation getStation(final int id) {
    final BusStation station = stations.get(idMap.get(id));
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
    return new BusStationManager(stations, path);
  }

}
