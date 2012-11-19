package infovis.ctrl;

import infovis.data.BusStation;
import infovis.data.BusStationEnumerator;
import infovis.data.BusStationManager;
import infovis.data.BusTime;
import infovis.layout.Layouts;
import infovis.routing.RouteFinder;
import infovis.routing.RoutingAlgorithm;
import infovis.util.Objects;
import infovis.util.Resource;

import java.util.ArrayList;
import java.util.BitSet;
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

  /** How many seconds are considered realtime. */
  public static final int REALTIME = 1;

  /** The list of active visualizations. */
  private final List<BusVisualization> vis = new ArrayList<BusVisualization>();

  /** The bus station manager. */
  private final BusStationManager manager;

  /** The frame. */
  private final JFrame frame;

  /** The currently selected bus station. */
  private BusStation curSelection;

  /** The current start time. */
  private volatile BusTime curStartTime = BusTime.NOON;

  /** The current change time. */
  private volatile int curChangeTime = 3;

  /** Current maximum walking time. */
  private volatile int currWalkTime = 5;

  /** Current positioning technique. */
  private Layouts embed = LAYOUTS[0];

  /** Timer for real-time view and ffw mode. */
  private final Timer timer = new Timer(true);

  /** Whether we are in ffw mode. */
  private volatile boolean ffwMode;

  /** The minutes that are forwarded per second in ffw mode. */
  private volatile int ffwMinutes = 1;

  /** The second when the now-mode refreshes. */
  protected int mod;

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
   * exact at the beginning of a minute. It is also used for the fast-forward
   * mode.
   */
  private void startTimer() {
    final Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.SECOND, 0);
    final TimerTask task = new TimerTask() {

      @Override
      public void run() {
        if(isStartTimeNow()) {
          final BusTime curr = BusTime.now();
          overwriteDisplayedTime(curr, curr.isBlinkSecond());
          if(curr.getSecond() % REALTIME == mod) {
            setTime(getTime());
          }
        } else if(isInFastForwardMode()) {
          BusTime time = getTime();
          if(time == null) {
            // we stop now-mode here
            time = BusTime.now();
          }
          setTime(time.later(getFastForwardStep(), 0));
        }
      }

    };
    timer.scheduleAtFixedRate(task, calendar.getTime(), BusTime.MILLISECONDS_PER_SECOND);
  }

  /**
   * Getter.
   * 
   * @return The resource or <code>null</code> if no resource is specified.
   */
  public Resource getOverview() {
    return manager.getOverview();
  }

  /**
   * Getter.
   * 
   * @return Whether there exists an overview.
   */
  public boolean hasOverview() {
    return getOverview() != null;
  }

  /**
   * Sets the window title.
   * 
   * @param title The title or <code>null</code>.
   */
  public void setTitle(final String title) {
    final StringBuilder sb = new StringBuilder(frame.getName());
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

  /** All positioning techniques. */
  private static final Layouts[] LAYOUTS = new Layouts[] {
    Layouts.CIRCULAR,

    Layouts.STRESS,
  };

  /**
   * Getter.
   * 
   * @return All registered positioning techniques.
   */
  public static Layouts[] getLayouts() {
    return LAYOUTS;
  }

  /** All routing algorithms. */
  private static final RoutingAlgorithm[] ALGOS = new RoutingAlgorithm[] {
    new RouteFinder(),
  };

  /**
   * Getter.
   * 
   * @return Returns all routing algorithms.
   */
  public static RoutingAlgorithm[] getRoutingAlgorithms() {
    return ALGOS;
  }

  /** The currently selected routing algorithm. */
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
    final boolean ffwMode = this.ffwMode;
    if(start == null) {
      mod = (BusTime.now().getSecond() + 3) % REALTIME;
    }
    curStartTime = start;
    for(final BusVisualization v : vis) {
      v.setStartTime(start, ffwMode);
    }
    if(ffwMode && start == null) {
      setFastForwardMode(false);
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

  /** Selects the start time as now. */
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
  public void setEmbedder(final Layouts embed) {
    if(this.embed == embed) return;
    this.embed = embed;
    for(final BusVisualization v : vis) {
      v.setLayout(embed);
    }
  }

  /**
   * Getter.
   * 
   * @return The currently used positioning technique.
   */
  public Layouts getEmbedder() {
    return embed;
  }

  /**
   * Adds a bus visualization.
   * 
   * @param v The visualization.
   */
  public void addBusVisualization(final BusVisualization v) {
    Objects.requireNonNull(v);
    if(vis.contains(v)) throw new IllegalStateException("visualization already added");
    vis.add(v);
    v.setLayout(embed);
    v.setChangeTime(curChangeTime);
    v.setStartTime(curStartTime, ffwMode);
    v.selectBusStation(curSelection);
    v.fastForwardChange(ffwMode, ffwMinutes);
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

  /**
   * Getter.
   * 
   * @return Whether we are in fast forward mode.
   */
  public boolean isInFastForwardMode() {
    return ffwMode;
  }

  /**
   * Setter.
   * 
   * @param ffwMode Fast forward mode.
   */
  public void setFastForwardMode(final boolean ffwMode) {
    this.ffwMode = ffwMode;
    ffwChange();
    if(!ffwMode) {
      setTime(getTime());
    }
  }

  /**
   * Getter.
   * 
   * @return The fast forward step in minutes.
   */
  public int getFastForwardStep() {
    return ffwMinutes;
  }

  /**
   * Setter.
   * 
   * @param ffwMinutes The fast forward step in minutes.
   */
  public void setFastForwardStep(final int ffwMinutes) {
    if(ffwMinutes < 1) throw new IllegalArgumentException("ffMinutes < 1: " + ffwMinutes);
    this.ffwMinutes = ffwMinutes;
    ffwChange();
  }

  /** Signals a change to the fast forward mode. */
  private void ffwChange() {
    final boolean ffwMode = this.ffwMode;
    final int ffwMinutes = this.ffwMinutes;
    for(final BusVisualization v : vis) {
      v.fastForwardChange(ffwMode, ffwMinutes);
    }
  }

  /** All secondary selected stations. */
  private final BitSet secSel = new BitSet();

  /**
   * Getter.
   * 
   * @return Whether any node is secondary selected.
   */
  public boolean hasSecondarySelection() {
    return !secSel.isEmpty();
  }

  /** The cache for secondary selected ids. */
  private int[] idCache;

  /**
   * Getter.
   * 
   * @return A list of the ids of all secondary selected stations.
   */
  public int[] secondarySelectedIds() {
    if(idCache == null) {
      final int[] res = new int[secSel.cardinality()];
      int pos = 0;
      for(int i = secSel.nextSetBit(0); i >= 0; i = secSel.nextSetBit(i + 1)) {
        res[pos++] = i;
      }
      idCache = res;
    }
    return idCache;
  }

  /**
   * Toggler.
   * 
   * @param station The station whose secondary selection will be toggled.
   */
  public void toggleSecondarySelected(final BusStation station) {
    idCache = null;
    secSel.flip(station.getId());
    refreshAll();
  }

  /** Refreshes all visualizations. */
  private void refreshAll() {
    for(final BusVisualization v : vis) {
      v.refresh();
    }
  }

  /**
   * Getter.
   * 
   * @param station The station.
   * @return If the station is secondary selected.
   */
  public boolean isSecondarySelected(final BusStation station) {
    return secSel.get(station.getId());
  }

  /** Clears all secondary selection. */
  public void clearSecondarySelection() {
    idCache = null;
    secSel.clear();
    refreshAll();
  }

  /** Whether to show the legend. */
  private boolean showLegend = true;

  /** Toggles the legend. */
  public void toggleLegend() {
    showLegend = !showLegend;
    refreshAll();
  }

  /**
   * Getter.
   * 
   * @return Whether to show the legend.
   */
  public boolean showLegend() {
    return showLegend;
  }

}
