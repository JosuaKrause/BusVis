package infovis.data.csv;

import static infovis.data.BusDataBuilder.*;
import static java.lang.Double.*;
import static java.lang.Integer.*;
import infovis.data.BusDataBuilder;
import infovis.data.BusDataReader;
import infovis.data.BusLine;
import infovis.data.BusTime;
import infovis.util.IOUtil;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import jkit.io.csv.CSVReader;
import jkit.io.csv.CSVRow;

/**
 * Reading transit data in CSV format specified in <code>readme.md</code>.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class CSVBusDataReader implements BusDataReader {

  public static final String UNKNOWN = "UNKNOWN";

  public static final String WALKING_DIST = "walking-dists.csv";

  public static final String LINES = "lines.csv";

  public static final String EDGES = "edges.csv";

  public static final String STOPS = "stops.csv";

  public static final String ABSTRACT = "abstract.svg";

  @Override
  public BusDataBuilder read(final String local, final String path, final Charset cs)
      throws IOException {
    final CSVReader reader = new CSVReader();
    final URL overview = IOUtil.getURL(local, path, ABSTRACT);
    final BusDataBuilder builder = new BusDataBuilder(
        IOUtil.hasContent(overview) ? overview : null);

    for(final CSVRow stop : CSVReader.readRows(local, path, STOPS, cs, reader)) {
      double abstractX, abstractY;
      if(UNKNOWN.equals(stop.get(4))) {
        abstractX = abstractY = NaN;
      } else {
        abstractX = parseDouble(stop.get(4));
        abstractY = parseDouble(stop.get(5));
      }
      builder.createStation(stop.get(0), stop.get(1), parseDouble(stop.get(3)),
          parseDouble(stop.get(2)), abstractX, abstractY);
    }

    final Iterable<CSVRow> walk = CSVReader.readRows(local, path, WALKING_DIST, cs,
        reader);
    if(walk != null) {
      for(final CSVRow dist : walk) {
        builder.setWalkingDistance(dist.get(0), dist.get(1), parseInt(dist.get(2)));
      }
    } else {
      builder.calcWalkingDistances();
    }

    for(final CSVRow line : CSVReader.readRows(local, path, LINES, cs, reader)) {
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

    for(final CSVRow edge : CSVReader.readRows(local, path, EDGES, cs, reader)) {
      final BusLine line = builder.getLine(edge.get(0));
      final int tourNr = parseInt(edge.get(1));
      final String from = edge.get(2);
      final String to = edge.get(5);
      final BusTime start = parseTime(edge.get(3));
      final BusTime end = parseTime(edge.get(4));
      builder.addEdge(from, line, tourNr, to, start, end);
    }

    return builder;
  }

}
