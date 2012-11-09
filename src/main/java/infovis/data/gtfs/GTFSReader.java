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
import infovis.util.Stopwatch;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A general transfer feed specification (GTFS) implementation in order to
 * obtain our internal transit network format. This reader is not thread safe
 * and should be used only once.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class GTFSReader implements BusDataReader {

  /** The GTFS data provider. */
  private final GTFSDataProvider data;

  /** Mapping from a trip segment to the corresponding station with times. */
  private final Map<TripSegment, TripStation> trips = new HashMap<TripSegment, TripStation>();

  /** Mapping from a trip id to corresponding bus lines. */
  private final Map<String, BusLine> tripMap = new HashMap<String, BusLine>();

  /** Maps to the parent of the station. */
  private final Map<String, String> stationParent = new HashMap<String, String>();

  /** The current builder. Note that only one builder at a time can be used. */
  private BusDataBuilder builder;

  /** Whether this reader was used before. */
  private final AtomicBoolean used = new AtomicBoolean(false);

  /**
   * Creates a GTFS reader for the given data provider.
   * 
   * @param data The GTFS data provider.
   */
  public GTFSReader(final GTFSDataProvider data) {
    this.data = Objects.requireNonNull(data);
  }

  /**
   * Resolves the id of a station.
   * 
   * @param id The id of a station.
   * @return The station.
   */
  public BusStation getStation(final String id) {
    return builder.getStation(stationParent.get(id));
  }

  /**
   * A temporal representation of a station.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  private static final class TempStation {

    /** The id of the station. */
    private final String id;

    /** The name of the station. */
    private final String name;

    /** The latitude of the station. */
    private final double lat;

    /** The longitude of the station. */
    private final double lon;

    /**
     * Creates a station.
     * 
     * @param id The id.
     * @param name The name.
     * @param lat The latitude.
     * @param lon The longitude.
     */
    public TempStation(final String id, final String name,
        final double lat, final double lon) {
      this.id = id;
      this.name = name;
      this.lat = lat;
      this.lon = lon;
    }

    /**
     * Creates the actual station.
     * 
     * @param builder The builder.
     */
    public void create(final BusDataBuilder builder) {
      builder.createStation(name, id, lat, lon, NaN, NaN);
    }

  }

  @Override
  public BusDataBuilder read(final String local, final String path, final Charset cs)
      throws IOException {
    if(used.getAndSet(true)) throw new IllegalStateException(
        "this reader was used before");
    final Stopwatch a = new Stopwatch();
    final Stopwatch t = new Stopwatch();
    System.out.println("Loading " + path + "...");
    data.setSource(IOUtil.getURL(local, path), cs);
    builder = new BusDataBuilder(null);
    System.out.println("Took " + t.reset());
    System.out.println("Reading stations...");
    readStations();
    System.out.println(builder.stationCount() + " stations (" + t.reset() + ")");
    System.out.println("Computing walk distances...");
    builder.calcWalkingDistances();
    System.out.println(builder.walkingCount() + " walking edges (" + t.reset() + ")");
    System.out.println("Reading lines...");
    readLines();
    System.out.println(builder.lineCount() + " lines (" + t.reset() + ")");
    System.out.println("Reading trips...");
    readTrips();
    System.out.println(tripMap.size() + " trips (" + t.reset() + ")");
    System.out.println("Reading stop times...");
    readStopTimes();
    System.out.println(trips.size() + " used trips (" + t.reset() + ")");
    System.out.println("Building edges...");
    buildEdges();
    System.out.println(builder.edgeCount() + " edges (" + t.reset() + ")");
    System.out.println("Loading took " + a.current());
    return builder;
  }

  /** Reads the stations. */
  private void readStations() {
    final Map<String, TempStation> stations = new HashMap<String, TempStation>();
    for(final GTFSRow row : data.stops()) {
      final String id = Objects.requireNonNull(row.getField("stop_id"));
      final String name = Objects.requireNonNull(row.getField("stop_name"));
      double lat = parseDouble(row.getField("stop_lat"));
      double lon = parseDouble(row.getField("stop_lon"));
      // TODO nyc gtfs switched lat and lon
      final double t = lat;
      lat = lon;
      lon = t;
      // ---
      final String parent = row.getField("parent_station");
      stationParent.put(id, Objects.nonNull(parent, id));
      stations.put(id, new TempStation(id, name, lat, lon));
    }
    final Set<TempStation> created = new HashSet<TempStation>();
    for(final Entry<String, TempStation> e : stations.entrySet()) {
      final String id = e.getKey();
      final String parent = stationParent.get(id);
      TempStation t = stations.get(parent);
      if(t == null) {
        // fake parent
        stationParent.put(id, id);
        t = e.getValue();
      }
      if(!created.contains(t)) {
        t.create(builder);
        created.add(t);
      }
    }
  }

  /** Reads the lines. */
  private void readLines() {
    for(final GTFSRow row : data.routes()) {
      final String id = Objects.requireNonNull(row.getField("route_id"));
      final String name = Objects.requireNonNull(row.getField("route_short_name"));
      final String longName = Objects.requireNonNull(row.getField("route_long_name"));
      final String c = row.getField("route_color");
      Color color;
      if(c == null) {
        color = null;
      } else {
        try {
          color = new Color(0xff000000 | Integer.parseInt(c, 16));
        } catch(final NumberFormatException e) {
          color = null;
        }
      }
      builder.createLine(id, name, name + " - " + longName,
          Objects.nonNull(color, Color.WHITE));
    }
  }

  /** Reads the trips. */
  private void readTrips() {
    // TODO use trips and calendar to find valid trips
    for(final GTFSRow row : data.trips()) {
      tripMap.put(Objects.requireNonNull(row.getField("trip_id")),
          builder.getLine(Objects.requireNonNull(row.getField("route_id"))));
    }
  }

  /** The minimal sequence number. */
  private int minSeq = Integer.MAX_VALUE;

  /** The maximal sequence number. */
  private int maxSeq = Integer.MIN_VALUE;

  /** Reads the stop times. */
  private void readStopTimes() {
    for(final GTFSRow row : data.stopTimes()) {
      final String tripId = Objects.requireNonNull(row.getField("trip_id"));
      final BusTime arrival = getTime(row.getField("arrival_time"));
      final BusTime departure = getTime(row.getField("departure_time"));
      final BusStation station = getStation(Objects.requireNonNull(row.getField("stop_id")));
      final int seq = Integer.parseInt(row.getField("stop_sequence"));
      if(seq < minSeq) {
        minSeq = seq;
      }
      if(seq > maxSeq) {
        maxSeq = seq;
      }
      trips.put(new TripSegment(tripId, seq),
          new TripStation(station, arrival, departure));
    }
  }

  /**
   * Builds edges from the previously read trips.
   */
  private void buildEdges() {
    int tourNr = 0;
    for(final Entry<String, BusLine> trip : tripMap.entrySet()) {
      final String tripId = trip.getKey();
      TripStation curStation = null;
      int p = minSeq;
      for(; p < maxSeq; ++p) {
        // when only a segment with max exists then there can be no edge
        if((curStation = trips.get(new TripSegment(tripId, p))) != null) {
          break;
        }
      }
      if(curStation == null) {
        continue;
      }
      TripStation nextStation = null;
      while(p < maxSeq) {
        ++p;
        for(; p <= maxSeq; ++p) {
          if((nextStation = trips.get(new TripSegment(tripId, p))) != null) {
            break;
          }
        }
        if(nextStation == null) {
          break;
        }
        builder.addEdge(curStation.station, trip.getValue(), tourNr,
            nextStation.station, curStation.departure, nextStation.arrival);
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
