package infovis.data;

import static java.lang.Double.*;
import static java.lang.Integer.*;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Loader for bus data in {@code CSV} format.
 * 
 * @author Leo Woerteler
 */
public final class BusData {
  /** Hidden default constructor. */
  private BusData() {
    // never used
  }

  /**
   * Loads the bus system data from CSV files.
   * 
   * @param path data file path
   * @return The bus manager holding the informations.
   * @throws IOException I/O exception
   */
  public static BusStationManager load(final String path) throws IOException {
    final BusStationManager manager = new BusStationManager(path);
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
      manager.createStation(stop[0], parseInt(stop[1]), parseDouble(stop[3]) * 10000,
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
      final BusLine busLine = new BusLine(name, color != null ? color
          : colors.get(name.replaceAll("\\D.*", "")));

      final CSVReader lineReader = readerFor(line);
      int tourNr = 0;
      for(String[] tour; (tour = lineReader.readNext()) != null; tourNr++) {
        BusStation before = null;
        BusTime depart = null;
        for(int i = 0; i < tour.length; i++) {
          final BusStation current = manager.getForId(parseInt(tour[i++]));
          final BusTime arrive = parse(tour[i++]);

          if(before != null) {
            before.addEdge(busLine, tourNr, current, depart, arrive);
          }

          if("-1".equals(tour[i])) {
            break;
          }
          before = current;
          depart = parse(tour[i]);
        }
      }
    }
    return manager;
  }

  /**
   * Parses a {@link BusTime}.
   * 
   * @param time time string
   * @return resulting {@link BusTime}
   */
  private static BusTime parse(final String time) {
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
}
