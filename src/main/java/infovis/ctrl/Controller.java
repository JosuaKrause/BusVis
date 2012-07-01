package infovis.ctrl;

import infovis.data.BusStation;
import infovis.data.BusStationEnumerator;
import infovis.data.BusStationManager;
import infovis.data.BusTime;
import infovis.embed.Embedders;
import infovis.routing.RouteFinder;
import infovis.routing.RoutingAlgorithm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

/**
 * The controller of the visualizations.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 *
 */
public final class Controller implements BusStationEnumerator {

  /** The list of active visualizations. */
  private final List<BusVisualization> vis = new ArrayList<BusVisualization>();

  /** The bus station manager. */
  private final BusStationManager manager;

  /** The frame. */
  private final JFrame frame;

  /** The currently selected bus station. */
  private BusStation curSelection;

  /** The current start time. */
  protected volatile BusTime curStartTime = BusTime.now();

  /** The current change time. */
  private volatile int curChangeTime = 3;

  /** Current maximum walking time. */
  private volatile int currWalkTime = 5;

  /** Current positioning technique. */
  private Embedders embed = EMBEDDERS[0];

  /** Timer for real-time view. */
  private final Timer timer = new Timer(true);

  /**
   * Creates a new controller.
   * 
   * @param manager The manager.
   * @param frame The frame.
   */
  public Controller(final BusStationManager manager, final JFrame frame) {
    this.manager = manager;
    this.frame = frame;
    startTimer();
  }

  /**
   * Starts a timer that periodically refreshes the time when
   * {@link #isStartTimeNow()} returns <code>true</code>. The refresh happens
   * exact at the beginning of a minute.
   */
  protected void startTimer() {
    final Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.SECOND, 0);
    final TimerTask task = new TimerTask() {

      private int minute = calendar.get(Calendar.MINUTE);

      @Override
      public void run() {
        if(isStartTimeNow()) {
          final Calendar now = Calendar.getInstance();
          final int min = now.get(Calendar.MINUTE);
          overwriteDisplayedTime(BusTime.fromCalendar(now), BusTime.isBlinkSecond(now));
          if(min != minute) {
            // we may lose a user update here
            // but very rare (only if the user clicks _very_ fast)
            setTime(curStartTime);
            minute = min;
          }
        }
      }

    };
    timer.scheduleAtFixedRate(task, calendar.getTime(), BusTime.MILLISECONDS_PER_SECOND);
  }

  /**
   * Getter.
   * 
   * @return The resource path.
   */
  public String getResourcePath() {
    return manager.getPath();
  }

  /**
   * Sets the window title.
   * 
   * @param title The title or <code>null</code>.
   */
  public void setTitle(final String title) {
    final StringBuilder sb = new StringBuilder("BusVis");
    if(curSelection != null) {
      sb.append(" - ");
      sb.append(curSelection.getName());
      if(isStartTimeNow()) {
        sb.append(" now");
      } else {
        sb.append(" at ");
        sb.append(curStartTime.pretty());
      }
      sb.append(" with ");
      sb.append(BusTime.minutesToString(curChangeTime));
      sb.append(" change");
    }
    if(title != null) {
      sb.append(" - (");
      sb.append(title);
      sb.append(")");
    }
    frame.setTitle(sb.toString());
  }

  /**
   * All positioning techniques.
   */
  private static final Embedders[] EMBEDDERS = new Embedders[] {
      Embedders.STRESS,

      // Embedders.EDGE,

      Embedders.CIRCULAR,

      // Embedders.SPRING,
  };

  /**
   * Getter.
   * 
   * @return All registered positioning techniques.
   */
  public static Embedders[] getEmbedders() {
    return EMBEDDERS;
  }

  /**
   * All routing algorithms.
   */
  private static final RoutingAlgorithm[] ALGOS = new RoutingAlgorithm[] {
    new RouteFinder(),

    // new FastRouteFinder(),
  };

  /**
   * Getter.
   * 
   * @return Returns all routing algorithms.
   */
  public static RoutingAlgorithm[] getRoutingAlgorithms() {
    return ALGOS;
  }

  /**
   * The currently selected routing algorithm.
   */
  private RoutingAlgorithm algo = ALGOS[0];

  /**
   * Setter.
   * 
   * @param algo Sets the current routing algorithm.
   */
  public void setRoutingAlgorithm(final RoutingAlgorithm algo) {
    this.algo = algo;
    for(final BusVisualization v : vis) {
      v.undefinedChange(this);
    }
  }

  /**
   * Getter.
   * 
   * @return The current routing algorithm.
   */
  public RoutingAlgorithm getRoutingAlgorithm() {
    return algo;
  }

  /**
   * Quits the application.
   * 
   * @param disposed if the thread was already disposed
   */
  public void quit(final boolean disposed) {
    if(!disposed) {
      frame.dispose();
    }
    timer.cancel();
  }

  /**
   * Sets the maximum time window.
   * 
   * @param hours The maximal number of hours.
   */
  public void setMaxTimeHours(final int hours) {
    manager.setMaxTimeHours(hours);
    for(final BusVisualization v : vis) {
      v.undefinedChange(this);
    }
  }

  /**
   * Getter.
   * 
   * @return The maximal number of hours a route can take.
   */
  public int getMaxTimeHours() {
    return manager.getMaxTimeHours();
  }

  /**
   * Selects a bus station.
   * 
   * @param station The station.
   */
  public void selectStation(final BusStation station) {
    curSelection = station;
    for(final BusVisualization v : vis) {
      v.selectBusStation(station);
    }
    setTitle(null);
  }

  /**
   * Getter.
   * 
   * @return The currently selected station.
   */
  public BusStation getSelectedStation() {
    return curSelection;
  }

  /**
   * Setter.
   * 
   * @param start The starting time.
   */
  public void setTime(final BusTime start) {
    curStartTime = start;
    for(final BusVisualization v : vis) {
      v.setStartTime(start);
    }
    setTitle(null);
  }

  /**
   * Overwrites the displayed time with the given value. The time must not
   * affect the actual start time.
   * 
   * @param time The new value.
   * @param blink Whether the colon should be displayed.
   */
  public void overwriteDisplayedTime(final BusTime time, final boolean blink) {
    for(final BusVisualization v : vis) {
      v.overwriteDisplayedTime(time, blink);
    }
  }

  /**
   * Getter.
   * 
   * @return Whether now is selected as start time.
   */
  public boolean isStartTimeNow() {
    return getTime() == null;
  }

  /**
   * Selects the start time as now.
   */
  public void setNow() {
    setTime(null);
  }

  /**
   * Getter.
   * 
   * @return The current starting time.
   */
  public BusTime getTime() {
    return curStartTime;
  }

  /**
   * Setter.
   * 
   * @param time The line changing time in minutes.
   */
  public void setChangeTime(final int time) {
    curChangeTime = time;
    for(final BusVisualization v : vis) {
      v.setChangeTime(time);
    }
    setTitle(null);
  }

  /**
   * Getter.
   * 
   * @return The line changing time in minutes.
   */
  public int getChangeTime() {
    return curChangeTime;
  }

  /**
   * Sets the positioning technique.
   * 
   * @param embed The technique.
   */
  public void setEmbedder(final Embedders embed) {
    if(this.embed == embed) return;
    this.embed = embed;
    for(final BusVisualization v : vis) {
      v.setEmbedder(embed);
    }
  }

  /**
   * Getter.
   * 
   * @return The currently used positioning technique.
   */
  public Embedders getEmbedder() {
    return embed;
  }

  /**
   * Adds a bus visualization.
   * 
   * @param v The visualization.
   */
  public void addBusVisualization(final BusVisualization v) {
    if(v == null) throw new NullPointerException("v");
    if(vis.contains(v)) throw new IllegalStateException("visualization already added");
    vis.add(v);
    v.setEmbedder(embed);
    v.setChangeTime(curChangeTime);
    v.setStartTime(curStartTime);
    v.selectBusStation(curSelection);
    v.undefinedChange(this);
  }

  /**
   * Removes a visualization.
   * 
   * @param v The visualization.
   */
  public void removeBusVisualization(final BusVisualization v) {
    vis.remove(v);
  }

  @Override
  public Collection<BusStation> getStations() {
    return manager.getStations();
  }

  @Override
  public int maxId() {
    return manager.maxId();
  }

  @Override
  public BusStation getForId(final int id) {
    return manager.getForId(id);
  }

  /**
   * Sets the focus on the current selected bus station.
   */
  public void focusStation() {
    for(final BusVisualization v : vis) {
      v.focusStation();
    }
  }

  /**
   * Getter.
   * 
   * @return The bus station manager.
   */
  public BusStationManager getBusStationManager() {
    return manager;
  }

  /**
   * Getter.
   * 
   * @return current maximum walking time
   */
  public int getWalkTime() {
    return currWalkTime;
  }

  /**
   * Setter.
   * 
   * @param newWalkTime new maximum walk time
   */
  public void setWalkTime(final int newWalkTime) {
    currWalkTime = newWalkTime;
    for(final BusVisualization v : vis) {
      v.undefinedChange(this);
    }
  }

}
