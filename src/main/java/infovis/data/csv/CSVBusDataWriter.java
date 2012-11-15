package infovis.data.csv;

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
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import jkit.io.csv.CSVWriter;

/**
 * Writes the bus data in the internal format to the disk.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class CSVBusDataWriter {

  /** The bus manager to write the informations of. */
  private final BusStationManager manager;

  /**
   * Creates a bus data writer for the given manager.
   * 
   * @param manager The bus manager.
   */
  public CSVBusDataWriter(final BusStationManager manager) {
    this.manager = manager;
  }

  /**
   * Actually writes the data to the specified path.
   * 
   * @param folder The path.
   * @param cs The character encoding.
   * @throws IOException I/O Exception.
   */
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
    return new CSVWriter(new PrintWriter(new OutputStreamWriter(
        new FileOutputStream(new File(folder, name)), cs), true));
  }

  private void writeEdges(final CSVWriter out, final BusEdge edge) {
    out.writeCell(edge.getLine().getName());
    out.writeCell("" + edge.getTourNr());
    out.writeCell("" + edge.getFrom().getId());
    out.writeCell(writeTime(edge.getStart()));
    out.writeCell(writeTime(edge.getEnd()));
    out.writeCell("" + edge.getTo().getId());
    out.writeRow();
  }

  private void writeWalkingDist(final CSVWriter out, final BusStation from,
      final BusStation to, final int seconds) {
    out.writeCell("" + from.getId());
    out.writeCell("" + to.getId());
    out.writeCell("" + seconds);
    out.writeRow();
  }

  private void writeLine(final CSVWriter out, final BusLine line) {
    final Color color = line.getColor();
    out.writeCell(line.getName());
    out.writeCell("" + color.getRed());
    out.writeCell("" + color.getGreen());
    out.writeCell("" + color.getBlue());
    out.writeCell(line.getFullName());
    out.writeRow();
  }

  private void writeStation(final CSVWriter out, final BusStation station) {
    out.writeCell(station.getName());
    out.writeCell("" + station.getId());
    out.writeCell("" + station.getLatitude());
    out.writeCell("" + station.getLongitude());
    out.writeCell(abstractCoordinate(station.getAbstractX()));
    out.writeCell(abstractCoordinate(station.getAbstractY()));
    out.writeRow();
  }

  private String abstractCoordinate(final double coordinate) {
    return Double.isNaN(coordinate) ? UNKNOWN : "" + coordinate;
  }

  private String writeTime(final BusTime time) {
    return "" + time.secondsFromMidnight();
  }

}
