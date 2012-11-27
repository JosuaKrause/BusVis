package infovis.data.csv;

import static infovis.data.BusDataBuilder.*;
import static java.lang.Double.*;
import static java.lang.Integer.*;
import infovis.data.BusDataBuilder;
import infovis.data.BusDataReader;
import infovis.data.BusLine;
import infovis.data.BusTime;
import infovis.util.ChangeAwareProperties;
import infovis.util.Resource;

import java.awt.Color;
import java.io.IOException;
import java.util.Properties;

import jkit.io.csv.CSVReader;
import jkit.io.csv.CSVRow;

/**
 * Reading transit data in CSV format specified in <code>readme.md</code>.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class CSVBusDataReader implements BusDataReader {

  /** The replacement value for unknown abstract positions. */
  public static final String UNKNOWN = "UNKNOWN";

  /** The walking distances table. */
  public static final String WALKING_DIST = "walking-dists.csv";

  /** The properties file. */
  public static final String PROPS = "settings.ini";

  /** The bus line table. */
  public static final String LINES = "lines.csv";

  /** The edge table. */
  public static final String EDGES = "edges.csv";

  /** The stop table. */
  public static final String STOPS = "stops.csv";

  /** The schematic overview. */
  public static final String ABSTRACT = "abstract.svg";

  /** The properties. */
  private Properties prop;

  /** Creates a bus data reader with automatic property detection. */
  public CSVBusDataReader() {
    this(null);
  }

  /**
   * Uses the given properties for the reader.
   * 
   * @param prop The properties.
   */
  public CSVBusDataReader(final Properties prop) {
    this.prop = prop;
  }

  @Override
  public BusDataBuilder read(final Resource r) throws IOException {
    final CSVReader reader = new CSVReader();
    final Resource overview = r.getFile(ABSTRACT);

    final ChangeAwareProperties ownProps = loadProperties(r);

    final BusDataBuilder builder = new BusDataBuilder(
        overview.hasContent() ? overview : null, prop);

    for(final CSVRow stop : CSVReader.readRows(r.getFile(STOPS), reader)) {
      double abstractX, abstractY;
      if(UNKNOWN.equals(stop.get(4))) {
        abstractX = abstractY = NaN;
      } else {
        abstractX = parseDouble(stop.get(4));
        abstractY = parseDouble(stop.get(5));
      }
      builder.createStation(stop.get(0), stop.get(1), parseDouble(stop.get(2)),
          parseDouble(stop.get(3)), abstractX, abstractY);
    }

    final Iterable<CSVRow> walk = CSVReader.readRows(r.getFile(WALKING_DIST), reader);
    if(walk != null) {
      for(final CSVRow dist : walk) {
        builder.setWalkingDistance(dist.get(0), dist.get(1), parseInt(dist.get(2)));
      }
    } else {
      builder.calcWalkingDistances();
    }

    for(final CSVRow line : CSVReader.readRows(r.getFile(LINES), reader)) {
      final Color c = new Color(parseInt(line.get(1)), parseInt(line.get(2)),
          parseInt(line.get(3)));
      final String id = line.get(0);
      final String name = id.replace('_', '/');
      String longName;
      if(line.hasIndex(4)) {
        longName = line.get(4);
      } else {
        longName = "Line " + name;
      }
      builder.createLine(id, name, longName, c);
    }

    for(final CSVRow edge : CSVReader.readRows(r.getFile(EDGES), reader)) {
      final BusLine line = builder.getLine(edge.get(0));
      final int tourNr = parseInt(edge.get(1));
      final String from = edge.get(2);
      final String to = edge.get(5);
      final BusTime start = parseTime(edge.get(3));
      final BusTime end = parseTime(edge.get(4));
      builder.addEdge(from, line, tourNr, to, start, end);
    }

    builder.computeEdgeMatrix();

    if(ownProps != null) {
      if(ownProps.storeIfChanged(r.toDump().getFile(PROPS), "created automatically")) {
        System.out.println("Written INI file");
      }
      prop = null;
    }

    return builder;
  }

  /**
   * Loads properties if automatic property detection is enabled.
   * 
   * @param r The folder.
   * @return The created change aware properties.
   */
  private ChangeAwareProperties loadProperties(final Resource r) {
    if(prop != null) return null;

    final ChangeAwareProperties res = new ChangeAwareProperties();
    prop = res;
    final Resource p = r.toDump().getFile(PROPS);
    try {
      if(p.hasContent()) {
        prop.load(p.reader());
      } else {
        // use system INI
        final Resource sysProp = r.getFile(PROPS);
        if(sysProp.hasContent()) {
          prop.load(sysProp.reader());
        }
      }
    } catch(final IOException e) {
      // ignore
    }
    return res;
  }

}
