package infovis.data.csv;

import static infovis.data.BusDataBuilder.*;
import static java.lang.Double.*;
import static java.lang.Integer.*;
import infovis.data.BusDataBuilder;
import infovis.data.BusDataReader;
import infovis.data.BusLine;
import infovis.data.BusTime;
import infovis.util.IOUtil;
import infovis.util.Objects;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Reading transit data in CSV format specified in <code>readme.md</code>.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class CSVBusDataReader implements BusDataReader {

  @Override
  public BusDataBuilder read(final String local, final String path, final Charset cs)
      throws IOException {
    final URL overview = IOUtil.getURL(local, path + "/abstract.svg");
    final BusDataBuilder builder = new BusDataBuilder(
        IOUtil.hasContent(overview) ? overview : null);

    final CSVReader stops = Objects.requireNonNull(IOUtil.readerFor(local, path,
        "stops.csv", cs));
    for(String[] stop; (stop = stops.readNext()) != null;) {
      double abstractX, abstractY;
      if("UNKNOWN".equals(stop[4])) {
        abstractX = abstractY = NaN;
      } else {
        abstractX = parseDouble(stop[4]);
        abstractY = parseDouble(stop[5]);
      }
      builder.createStation(stop[0], stop[1], parseDouble(stop[3]),
          parseDouble(stop[2]), abstractX, abstractY);
    }

    final CSVReader walk = IOUtil.readerFor(local, path, "walking-dists.csv", cs);
    if(walk != null) {
      for(String[] dist; (dist = walk.readNext()) != null;) {
        builder.setWalkingDistance(dist[0], dist[1], parseInt(dist[2]));
      }
    } else {
      builder.calcWalkingDistances();
    }

    final CSVReader lineReader = Objects.requireNonNull(IOUtil.readerFor(local, path,
        "lines.csv", cs));
    for(String[] line; (line = lineReader.readNext()) != null;) {
      final Color c = new Color(parseInt(line[1]), parseInt(line[2]), parseInt(line[3]));
      builder.createLine(line[0], line[0].replace('_', '/'), c);
    }

    final CSVReader edgeReader = Objects.requireNonNull(
        IOUtil.readerFor(local, path, "edges.csv", cs));
    for(String[] edge; (edge = edgeReader.readNext()) != null;) {
      final BusLine line = builder.getLine(edge[0]);
      final int tourNr = parseInt(edge[1]);
      final String from = edge[2];
      final String to = edge[5];
      final BusTime start = parseTime(edge[3]);
      final BusTime end = parseTime(edge[4]);
      builder.addEdge(from, line, tourNr, to, start, end);
    }

    return builder;
  }

}
