package infovis.data.csv;

import static infovis.data.BusDataBuilder.*;
import static java.lang.Double.*;
import static java.lang.Integer.*;
import infovis.data.BusDataBuilder;
import infovis.data.BusDataReader;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.util.IOUtil;
import infovis.util.Objects;
import infovis.util.VecUtil;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        abstractX = abstractY = Double.NaN;
      } else {
        abstractX = parseDouble(stop[4]);
        abstractY = parseDouble(stop[5]);
      }
      builder.createStation(stop[0], parseInt(stop[1]), parseDouble(stop[3]),
          parseDouble(stop[2]), abstractX, abstractY);
    }

    final CSVReader walk = IOUtil.readerFor(local, path, "walking-dists.csv", cs);
    if(walk != null) {
      for(String[] dist; (dist = walk.readNext()) != null;) {
        builder.setWalkingDistance(parseInt(dist[0]), parseInt(dist[1]), parseInt(dist[2]));
      }
    } else {
      final Collection<BusStation> s = builder.stations();
      int pa = 0;
      for(final BusStation a : s) {
        int pb = 0;
        for(final BusStation b : s) {
          if(pb >= pa) {
            break;
          }
          final double walkDist = VecUtil.earthDistance(a.getLatitude(),
              a.getLongitude(), b.getLatitude(), b.getLongitude());
          // assuming 5 km/h ie. 5000m / 3600s
          final int walkSecs = (int) Math.ceil(walkDist * 60.0 * 60.0 / 5000.0);
          builder.setWalkingDistance(a, b, walkSecs);
          ++pb;
        }
        ++pa;
      }
    }

    final Map<String, BusLine> lines = new HashMap<String, BusLine>();
    final CSVReader lineReader = Objects.requireNonNull(IOUtil.readerFor(local, path,
        "lines.csv", cs));
    for(String[] line; (line = lineReader.readNext()) != null;) {
      final Color c = new Color(parseInt(line[1]), parseInt(line[2]), parseInt(line[3]));
      lines.put(line[0], createLine(line[0].replace('_', '/'), c));
    }

    final CSVReader edgeReader = Objects.requireNonNull(
        IOUtil.readerFor(local, path, "edges.csv", cs));
    for(String[] edge; (edge = edgeReader.readNext()) != null;) {
      final BusLine line = lines.get(edge[0]);
      final int tourNr = parseInt(edge[1]), from = parseInt(edge[2]), to = parseInt(edge[5]);
      final BusTime start = parseTime(edge[3]), end = parseTime(edge[4]);
      builder.addEdge(from, line, tourNr, to, start, end);
    }

    return builder;
  }

}
