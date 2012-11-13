package infovis.data.out;

import static infovis.data.csv.CSVBusDataReader.*;
import infovis.data.BusEdge;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import au.com.bytecode.opencsv.CSVWriter;

public class BusDataWriter {

  private final BusStationManager manager;

  public BusDataWriter(final BusStationManager manager) {
    this.manager = manager;
  }

  public void write(final File folder, final Charset cs) throws IOException {
    final CSVWriter edges = getWriter(folder, EDGES, cs);
    final CSVWriter walks = getWriter(folder, WALKING_DIST, cs);
    final CSVWriter stations = getWriter(folder, STOPS, cs);
    final Set<BusLine> busLines = new HashSet<BusLine>();
    for(final BusStation s : manager.getStations()) {
      writeStation(stations, s);
      for(final BusEdge e : s.getEdges()) {
        busLines.add(e.getLine());
        writeEdges(edges, e);
      }
      final int id = s.getId();
      for(final BusStation lower : manager.getStations()) {
        if(id <= lower.getId()) {
          continue;
        }
        writeWalkingDist(walks, s, lower, s.walkingSeconds(lower));
      }
    }
    edges.close();
    walks.close();
    stations.close();
    final CSVWriter lines = getWriter(folder, LINES, cs);
    for(final BusLine l : busLines) {
      writeLine(lines, l);
    }
    lines.close();
  }

  private CSVWriter getWriter(final File folder, final String name, final Charset cs)
      throws FileNotFoundException {
    return new CSVWriter(new OutputStreamWriter(
        new FileOutputStream(new File(folder, name)), cs));
  }

  private void writeEdges(final CSVWriter out, final BusEdge edge) {
    out.writeNext(new String[] { edge.getLine().getName(), "" + edge.getTourNr(),
        "" + edge.getFrom().getId(), writeTime(edge.getStart()),
        writeTime(edge.getEnd()), "" + edge.getTo().getId()});
  }

  private void writeWalkingDist(final CSVWriter out, final BusStation from,
      final BusStation to, final int seconds) {
    out.writeNext(new String[] { "" + from.getId(), "" + to.getId(), "" + seconds});
  }

  private void writeLine(final CSVWriter out, final BusLine line) {
    final Color color = line.getColor();
    out.writeNext(new String[] { line.getName(), "" + color.getRed(),
        "" + color.getGreen(), "" + color.getBlue(), line.getFullName()});
  }

  private void writeStation(final CSVWriter out, final BusStation station) {
    out.writeNext(new String[] { station.getName(), "" + station.getId(),
        "" + station.getLatitude(), "" + station.getLongitude(),
        abstractCoordinate(station.getAbstractX()),
        abstractCoordinate(station.getAbstractY())});
  }

  private String abstractCoordinate(final double coordinate) {
    return Double.isNaN(coordinate) ? UNKNOWN : "" + coordinate;
  }

  private String writeTime(final BusTime time) {
    return "" + time.secondsFromMidnight();
  }

}
