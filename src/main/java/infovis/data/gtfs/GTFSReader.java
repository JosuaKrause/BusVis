package infovis.data.gtfs;

import static java.lang.Double.*;
import static java.lang.Integer.*;
import infovis.data.BusDataBuilder;
import infovis.data.BusDataReader;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.util.IOUtil;
import infovis.util.Objects;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A general transfer feed specification (GTFS) implementation in order to
 * obtain our internal transit network format.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 *
 */
public class GTFSReader implements BusDataReader {

  /** The GTFS data provider. */
  private final GTFSDataProvider data;

  /** Mapping from a trip segment to the corresponding station with times. */
  private final Map<TripSegment, TripStation> trips = new HashMap<TripSegment, TripStation>();

  /** Mapping from a trip id to corresponding bus lines. */
  private final Map<String, BusLine> tripMap = new HashMap<String, BusLine>();

  /**
   * Creates a GTFS reader for the given data provider.
   * 
   * @param data The GTFS data provider.
   */
  public GTFSReader(final GTFSDataProvider data) {
    this.data = Objects.requireNonNull(data);
  }

  @Override
  public BusDataBuilder read(final String local, final String path, final Charset cs)
      throws IOException {
    data.setSource(IOUtil.getURL(local, path), cs);
    final BusDataBuilder builder = new BusDataBuilder(null);
    for(final GTFSRow row : data.stops()) {
      final String id = row.getField("stop_id");
      final String name = row.getField("stop_name");
      final double lat = parseDouble(row.getField("stop_lat"));
      final double lon = parseDouble(row.getField("stop_lon"));
      // TODO make alias for parent_station and using location_type
      builder.createStation(name, id, lat, lon, NaN, NaN);
    }
    builder.calcWalkingDistances();
    for(final GTFSRow row : data.routes()) {
      final String id = row.getField("route_id");
      final String name = row.getField("route_long_name");
      final String c = row.getField("route_color");
      Color color;
      try {
        color = new Color(0xff000000 | Integer.parseInt(c, 16));
      } catch(final NumberFormatException e) {
        color = null;
      }
      // TODO use WHITE as default color
      builder.createLine(id, name, Objects.nonNull(color, Color.BLUE));
    }
    // TODO use trips and calendar to find valid trips
    for(final GTFSRow row : data.trips()) {
      tripMap.put(row.getField("trip_id"), builder.getLine(row.getField("route_id")));
    }
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for(final GTFSRow row : data.stopTimes()) {
      final String tripId = row.getField("trip_id");
      final BusTime arrival = getTime(row.getField("arrival_time"));
      final BusTime departure = getTime(row.getField("departure_time"));
      final BusStation station = builder.getStation(row.getField("stop_id"));
      final int seq = Integer.parseInt(row.getField("stop_sequence"));
      if(seq < min) {
        min = seq;
      }
      if(seq > max) {
        max = seq;
      }
      trips.put(new TripSegment(tripId, seq),
          new TripStation(station, arrival, departure));
    }
    buildEdges(builder, min, max);
    return builder;
  }

  /**
   * Builds edges from the previously read trips.
   * 
   * @param builder The builder to build to.
   * @param min The minimal sequence number.
   * @param max The maximal sequence number.
   */
  private void buildEdges(final BusDataBuilder builder, final int min, final int max) {
    int tourNr = 0;
    for(final Entry<String, BusLine> trip : tripMap.entrySet()) {
      final String tripId = trip.getKey();
      TripStation curStation = null;
      int p = min;
      for(; p < max; ++p) {
        // when only a segment with max exists then there can be no edge
        if((curStation = trips.get(new TripSegment(tripId, p))) != null) {
          break;
        }
      }
      if(curStation == null) {
        continue;
      }
      TripStation nextStation = null;
      while(p < max) {
        ++p;
        for(; p <= max; ++p) {
          if((nextStation = trips.get(new TripSegment(tripId, p))) != null) {
            break;
          }
        }
        if(nextStation == null) {
          break;
        }
        builder.addEdge(curStation.station, trip.getValue(),
            tourNr, nextStation.station, curStation.departure, nextStation.arrival);
        curStation = nextStation;
        nextStation = null;
        ++tourNr;
      }
    }
  }

  /**
   * Parses times of the format <code>HH:MM:SS</code> with 24 hours but possibly
   * more and omittable leading zeros.
   * 
   * @param time The time string.
   * @return The actual time.
   */
  public static final BusTime getTime(final String time) {
    final String[] sections = time.split(":");
    if(sections.length != 3) throw new IllegalArgumentException("invalid format: " + time);
    try {
      final int h = parseInt(sections[0]);
      final int m = parseInt(sections[1]);
      final int s = parseInt(sections[2]);
      return BusTime.MIDNIGHT.later(h * 60 + m, s);
    } catch(final NumberFormatException e) {
      throw new IllegalArgumentException("invalid format: " + time, e);
    }
  }

  /**
   * A trip segment containing a trip id and a sequence number.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  private static class TripSegment {

    /** The trip id. */
    public final String tripId;

    /** The sequence number. */
    public final int pos;

    /**
     * Creates a trip segment.
     * 
     * @param tripId The id.
     * @param pos The sequence number.
     */
    public TripSegment(final String tripId, final int pos) {
      this.tripId = Objects.requireNonNull(tripId);
      this.pos = pos;
    }

    @Override
    public boolean equals(final Object o) {
      if(!(o instanceof TripSegment)) return false;
      final TripSegment t = (TripSegment) o;
      return t.tripId.equals(tripId) && t.pos == pos;
    }

    @Override
    public int hashCode() {
      return tripId.hashCode() + 31 * pos;
    }

  }

  /**
   * A station within a trip containing arrival and departure times.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  private static class TripStation {

    /** The station. */
    public final BusStation station;

    /** The arrival time. */
    public final BusTime arrival;

    /** The departure time. */
    public final BusTime departure;

    /**
     * Creates a trip station.
     * 
     * @param station The station.
     * @param arrival The arrival time.
     * @param departure The departure time.
     */
    public TripStation(final BusStation station, final BusTime arrival,
        final BusTime departure) {
      this.station = Objects.requireNonNull(station);
      this.arrival = Objects.requireNonNull(arrival);
      this.departure = Objects.requireNonNull(departure);
    }

  }

}
