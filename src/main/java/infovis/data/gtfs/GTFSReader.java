package infovis.data.gtfs;

import static java.lang.Double.*;
import static java.lang.Integer.*;
import infovis.data.BusDataBuilder;
import infovis.data.BusDataReader;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.data.csv.CSVBusDataReader;
import infovis.data.csv.CSVBusDataWriter;
import infovis.util.ChangeAwareProperties;
import infovis.util.Objects;
import infovis.util.Resource;
import infovis.util.Stopwatch;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
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
  public BusDataBuilder read(final Resource r) throws IOException {
    if(used.getAndSet(true)) throw new IllegalStateException(
        "this reader was used before");
    final Resource dump = r.toDump();
    final Resource root = dump.getParent();
    final Resource ini = dump.changeExtensionTo("ini");
    final ChangeAwareProperties prop = new ChangeAwareProperties();
    if(ini.hasContent()) {
      prop.load(ini.reader());
    } else {
      // use system ini
      final Resource sysIni = r.changeExtensionTo("ini");
      if(sysIni.hasContent()) {
        prop.load(sysIni.reader());
      }
    }
    final String doCache = prop.getProperty("cache");
    final boolean caching = dump.hasDirectFile()
        && (doCache == null || "true".equals(doCache));
    prop.setProperty("cache", "" + caching);
    if(caching) {
      final Resource stops = root.getFile(CSVBusDataReader.STOPS);
      boolean modified;
      if(r.hasDirectFile()) {
        final File zip = r.directFile();
        modified = zip.lastModified() >= stops.directFile().lastModified();
      } else {
        modified = false;
      }
      if(stops.hasContent() && !modified) {
        final Stopwatch t = new Stopwatch();
        System.out.println("Loading cached from " + root);
        final BusDataReader in = new CSVBusDataReader(prop);
        builder = in.read(root);
        System.out.println("Loading took " + t.current());
        writeProperties(prop, ini);
        return builder;
      }
    }
    doRead(r, prop, caching);
    if(caching) {
      System.out.println("Writing cache to " + root);
      final Stopwatch t = new Stopwatch();
      final CSVBusDataWriter out = new CSVBusDataWriter(builder.finish());
      out.write(root);
      System.out.println("Took " + t.current());
    }
    writeProperties(prop, ini);
    return builder;
  }

  /**
   * Writes properties to the INI file if possible.
   * 
   * @param prop The properties.
   * @param ini The INI file.
   * @throws IOException I/O Exception.
   */
  private static void writeProperties(final ChangeAwareProperties prop,
      final Resource ini) throws IOException {
    if(ini.hasDirectFile()) {
      if(prop.storeIfChanged(ini, "created automatically")) {
        System.out.println("Written INI file");
      }
    }
  }

  /**
   * Reads the zipped GTFS data.
   * 
   * @param r The resource.
   * @param prop The properties.
   * @param caching Whether caching is enabled.
   * @throws IOException I/O Exception
   */
  private void doRead(final Resource r,
      final Properties prop, final boolean caching) throws IOException {
    // TODO more properties of the GTFS format could be implemented
    final Stopwatch a = new Stopwatch();
    final Stopwatch t = new Stopwatch();
    System.out.println("Loading " + r);
    data.setSource(r);
    builder = new BusDataBuilder(null, prop);
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
    readTrips(prop, caching);
    System.out.println(tripMap.size() + " trips (" + t.reset() + ")");
    System.out.println("Reading stop times...");
    readStopTimes();
    System.out.println(trips.size() + " used trips (" + t.reset() + ")");
    System.out.println("Building edges...");
    buildEdges();
    System.out.println(builder.edgeCount() + " edges (" + t.reset() + ")");
    System.out.println("Building edge matrix...");
    builder.computeEdgeMatrix();
    System.out.println("Done (" + t.reset() + ")");
    System.out.println("Loading took " + a.current());
  }

  /** Reads the stations. */
  private void readStations() {
    final Map<String, TempStation> stations = new HashMap<String, TempStation>();
    for(final GTFSRow row : data.stops()) {
      final String id = Objects.requireNonNull(row.getField("stop_id"));
      final String name = Objects.requireNonNull(row.getField("stop_name"));
      final double lat = parseDouble(row.getField("stop_lat"));
      final double lon = parseDouble(row.getField("stop_lon"));
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

  /**
   * Reads the trips.
   * 
   * @param prop The properties.
   * @param caching Whether caching is enabled.
   */
  private void readTrips(final Properties prop, final boolean caching) {
    final String d = prop.getProperty("date");
    Calendar date = Calendar.getInstance();
    if(d == null) {
      prop.setProperty("date", "today");
    } else {
      if(!"today".equals(d)) {
        try {
          date = Calendar.getInstance();
          date.setTime(DATE_PARSER.parse(d));
        } catch(final ParseException e) {
          prop.setProperty("date", "today");
        }
      }
    }
    if(caching) {
      prop.setProperty("date", DATE_PARSER.format(date.getTime()));
    }
    final Set<String> serviceIds = new HashSet<String>();
    final Set<String> invalidServiceIds = new HashSet<String>();
    readCalendarDates(date, serviceIds, invalidServiceIds);
    readCalendar(date, serviceIds);
    for(final GTFSRow row : data.trips()) {
      final String sid = row.getField("service_id");
      if(invalidServiceIds.contains(sid) || !serviceIds.contains(sid)) {
        continue;
      }
      tripMap.put(Objects.requireNonNull(row.getField("trip_id")),
          builder.getLine(Objects.requireNonNull(row.getField("route_id"))));
    }
  }

  /** The GTFS date format. */
  private static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyyMMdd");

  /** A table to look up GTFS names for weekdays. */
  private static final String[] DOW_TABLE = {
    "sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"
  };

  /**
   * Whether both calendar objects point to the same date.
   * 
   * @param a The first calendar object.
   * @param b The second calendar object.
   * @return Whether they point to the same date in regard to day of month,
   *         month, and year.
   */
  private static boolean equalDate(final Calendar a, final Calendar b) {
    if(a.get(Calendar.YEAR) != b.get(Calendar.YEAR)) return false;
    if(a.get(Calendar.MONTH) != b.get(Calendar.MONTH)) return false;
    if(a.get(Calendar.DAY_OF_MONTH) != b.get(Calendar.DAY_OF_MONTH)) return false;
    return true;
  }

  /**
   * Reads the calendar file and sets the valid service ids.
   * 
   * @param date The current date.
   * @param serviceIds The set of valid service ids.
   */
  private void readCalendar(final Calendar date, final Set<String> serviceIds) {
    final String dow = DOW_TABLE[date.get(Calendar.DAY_OF_WEEK) - 1];
    final Calendar begin = Calendar.getInstance();
    final Calendar end = Calendar.getInstance();
    for(final GTFSRow row : data.calendar()) {
      try {
        begin.setTime(DATE_PARSER.parse(row.getField("start_date")));
        end.setTime(DATE_PARSER.parse(row.getField("end_date")));
      } catch(final ParseException e) {
        e.printStackTrace();
        continue;
      }
      if(!((date.after(begin) || equalDate(date, begin))
          && (date.before(end) || equalDate(date, end)))) {
        continue;
      }
      if(!row.getField(dow).equals("1")) {
        continue;
      }
      serviceIds.add(row.getField("service_id"));
    }
  }

  /**
   * Reads the calendar dates file. The file can activate or deactivate service
   * ids for special dates.
   * 
   * @param date The current date.
   * @param serviceIds Valid service ids.
   * @param invalidServiceIds Invalid service ids.
   */
  private void readCalendarDates(final Calendar date,
      final Set<String> serviceIds, final Set<String> invalidServiceIds) {
    final Calendar cur = Calendar.getInstance();
    for(final GTFSRow row : data.calendarDates()) {
      try {
        cur.setTime(DATE_PARSER.parse(row.getField("date")));
      } catch(final ParseException e) {
        e.printStackTrace();
        continue;
      }
      if(!equalDate(date, cur)) {
        continue;
      }
      final String sid = row.getField("service_id");
      final String exc = row.getField("exception_type");
      final Set<String> toAddTo = exc.equals("1") ? serviceIds : invalidServiceIds;
      toAddTo.add(sid);
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
