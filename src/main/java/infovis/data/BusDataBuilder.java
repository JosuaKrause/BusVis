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
import java.util.Map.Entry;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Loader for bus data in {@code CSV} format.
 * 
 * @author Leo Woerteler
 */
public final class BusDataBuilder {
  /** Bus stations. */
  private final Map<Integer, BusStation> stations = new HashMap<Integer, BusStation>();
  /** Container for all bus stations. */
  private final BusStationManager manager;
  /** Map from station IDs to the bus edges originating at this station. */
  private final Map<Integer, List<BusEdge>> edges = new HashMap<Integer, List<BusEdge>>();

  /**
   * Constructor taking the path of the CSV files.
   * 
   * @param path path of the CSV files, possibly <code>null</code>
   */
  public BusDataBuilder(final String path) {
    manager = new BusStationManager(stations, path);
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

    final Map<String, Color> colors = new HashMap<String, Color>();
    final CSVReader colorReader = readerFor(new File(root, "linecolor.csv"));
    for(String[] line; (line = colorReader.readNext()) != null;) {
      colors.put(line[0].replace('_', '/'),
          new Color(parseInt(line[1]), parseInt(line[2]), parseInt(line[3])));
    }

    for(final File line : new File(root, "lines").listFiles()) {
      final String name = line.getName().replace('_', '/').replace(".csv", "");
      final Color color = colors.get(name);
      final BusLine busLine = createLine(name, color != null ? color
          : colors.get(name.replaceAll("\\D.*", "")));

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
    return new BusTime(parseInt(parts[0]), parseInt(parts[1]));
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
    if(id < 0) throw new IllegalArgumentException("id '" + id
        + "' has to be non-negative");
    if(stations.containsKey(id)) throw new IllegalArgumentException("id: " + id
        + " already in use");
    final List<BusEdge> edgeList = new ArrayList<BusEdge>();
    edges.put(id, edgeList);
    final BusStation bus = new BusStation(manager, name, id, x, y, abstractX, abstractY,
        edgeList);
    stations.put(id, bus);
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
   * Gets the station from the stations map.
   * 
   * @param id station ID
   * @return associated station
   * @throws IllegalArgumentException if the ID has no associated station
   */
  private BusStation getStation(final int id) {
    final BusStation station = stations.get(id);
    if(station == null) throw new IllegalArgumentException("Unknown station: " + id);
    return station;
  }

  /**
   * Finishes the building process and returns the bus station manager.
   * 
   * @return bus station manager
   */
  public BusStationManager finish() {
    for(final Entry<Integer, List<BusEdge>> e : edges.entrySet()) {
      Collections.sort(e.getValue());
    }
    return manager;
  }
}