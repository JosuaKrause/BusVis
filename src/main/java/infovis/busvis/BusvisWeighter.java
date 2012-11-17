package infovis.busvis;

import static infovis.busvis.Weighter.ChangeType.*;
import infovis.ctrl.Controller;
import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.data.EdgeMatrix;
import infovis.routing.RoutingManager;
import infovis.routing.RoutingManager.CallBack;
import infovis.routing.RoutingResult;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Weights the station network after the distance from one start station.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class BusvisWeighter implements Weighter {

  /** The backing map for the spring nodes. */
  private final BusStation[] map;

  /** The reverse backing map for the spring nodes. */
  private final LayoutNode[] rev;

  /** Collection of all {@link LayoutNode}s. */
  private final List<LayoutNode> nodes;

  /** Dummy routes for uninitialized routings. */
  private final RoutingResult[] dummyRoutes;

  /** The routes from the bus station, may be <code>null</code>. */
  private volatile RoutingResult[] routes;

  /** The current reference time. */
  private BusTime time = new BusTime(12, 0, 0);

  /** The change time for lines. */
  private int changeTime = 5;

  /** Whether we are in fast forward mode. */
  private boolean ffw;

  /** The start bus station or <code>null</code> if there is none. */
  private BusStation from;

  /** The factor to scale the distances. */
  private double factor = .1;

  /** The controller. */
  private final Controller ctrl;

  /** The undirected edge matrix. */
  private final EdgeMatrix matrix;

  /** The routing manager. */
  private final RoutingManager rm = RoutingManager.newInstance();

  /** The animator to be notified when something has changed. */
  private Animator animator;

  /** The fader. */
  private Fader fader;

  /**
   * Creates a station distance without a reference station.
   * 
   * @param ctrl The controller.
   */
  public BusvisWeighter(final Controller ctrl) {
    this.ctrl = ctrl;
    matrix = ctrl.getBusStationManager().getEdgeMatrix();
    dummyRoutes = new RoutingResult[ctrl.maxId() + 1];
    for(int id = 0; id < dummyRoutes.length; ++id) {
      dummyRoutes[id] = new RoutingResult(ctrl.getForId(id)); // dummy results
    }
    routes = dummyRoutes;
    final int length = ctrl.maxId() + 1;
    map = new BusStation[length];
    rev = new LayoutNode[length];
    nodes = Collections.unmodifiableList(Arrays.asList(rev));
    for(final BusStation s : ctrl.getStations()) {
      final LayoutNode node = new LayoutNode(s.getId());
      node.setPosition(s.getDefaultX(), s.getDefaultY());
      map[node.getId()] = s;
      rev[s.getId()] = node;
    }
  }

  /**
   * Setter.
   * 
   * @param fader The associated fader.
   */
  public void setFader(final Fader fader) {
    this.fader = fader;
  }

  /**
   * Sets the values for the distance.
   * 
   * @param from The reference station.
   * @param time The reference time.
   * @param changeTime The change time.
   * @param ffw Whether we are in fast forward mode.
   */
  public void set(final BusStation from, final BusTime time,
      final int changeTime, final boolean ffw) {
    fader.setPredict(from);
    if(from == null) {
      putSettings(dummyRoutes, from, time, changeTime, ffw);
      return;
    }
    final CallBack<RoutingResult[]> cb = new CallBack<RoutingResult[]>() {

      @Override
      public void callBack(final RoutingResult[] result) {
        putSettings(result, from, time, changeTime, ffw);
      }

    };
    rm.findRoutes(ctrl, from, null, time != null ? time : BusTime.now(), changeTime,
        ctrl.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR, ctrl.getWalkTime(),
        ctrl.getRoutingAlgorithm(), cb);
  }

  /**
   * Puts the new settings.
   * 
   * @param route The routes.
   * @param from The start station.
   * @param time The start time.
   * @param changeTime The change time.
   * @param ffw Whether we are in fast forward mode.
   */
  protected synchronized void putSettings(final RoutingResult[] route,
      final BusStation from, final BusTime time, final int changeTime, final boolean ffw) {
    routes = route;
    matrix.refreshHighlights(routes);
    if(from != this.from) {
      fader.initialize(this.from, LayoutNode.NORMAL);
    }
    final BusTime old = this.time;
    if(ffw) {
      changes = this.ffw ? FAST_FORWARD_CHANGE : PREPARE_CHANGE;
    } else if(time == null) {
      changes = old == null ? REALTIME_CHANGE : PREPARE_CHANGE;
    } else if(old != null && (old != time || this.changeTime != changeTime)) {
      changes = FAST_ANIMATION_CHANGE;
    } else {
      changes = NORMAL_CHANGE;
    }
    this.from = from;
    this.time = time;
    this.changeTime = changeTime;
    this.ffw = ffw;
    animator.forceNextFrame();
  }

  @Override
  public void setAnimator(final Animator animator) {
    this.animator = animator;
  }

  @Override
  public boolean inAnimation() {
    return fader.inFade();
  }

  /**
   * Getter.
   * 
   * @return The controller.
   */
  public Controller getController() {
    return ctrl;
  }

  /**
   * Getter.
   * 
   * @param id The id.
   * @return The corresponding station.
   */
  public BusStation getStation(final int id) {
    return map[id];
  }

  /**
   * Getter.
   * 
   * @param n The spring node.
   * @return The corresponding station.
   */
  public BusStation getStation(final LayoutNode n) {
    return getStation(n.getId());
  }

  /**
   * Getter.
   * 
   * @return The edge matrix.
   */
  public EdgeMatrix getMatrix() {
    return matrix;
  }

  /** Whether the weights have changed. */
  protected volatile ChangeType changes;

  @Override
  public ChangeType changes() {
    final ChangeType res = changes;
    changes = NO_CHANGE;
    return res != null ? res : NO_CHANGE;
  }

  /** Signals undefined changes. */
  public void changeUndefined() {
    set(from, time, changeTime, ffw);
  }

  /**
   * Setter.
   * 
   * @param from Sets the reference station.
   */
  public void setFrom(final BusStation from) {
    set(from, time, changeTime, ffw);
  }

  /**
   * Getter.
   * 
   * @return The reference station.
   */
  public BusStation getFrom() {
    return from;
  }

  /**
   * Setter.
   * 
   * @param time Sets the reference time.
   * @param ffw Whether we are in fast forward mode.
   */
  public void setTime(final BusTime time, final boolean ffw) {
    set(from, time, changeTime, ffw);
  }

  /**
   * Getter.
   * 
   * @return The reference time.
   */
  public BusTime getTime() {
    return time;
  }

  /**
   * Setter.
   * 
   * @param changeTime Sets the change time.
   */
  public void setChangeTime(final int changeTime) {
    set(from, time, changeTime, ffw);
  }

  /**
   * Getter.
   * 
   * @return The change time.
   */
  public int getChangeTime() {
    return changeTime;
  }

  /**
   * Setter.
   * 
   * @param factor Sets the distance factor.
   */
  public void setFactor(final double factor) {
    this.factor = factor;
  }

  @Override
  public double getFactor() {
    return factor;
  }

  /** The minimal distance between nodes. */
  private double minDist = 15;

  /**
   * Setter.
   * 
   * @param minDist Sets the minimal distance between nodes.
   */
  public void setMinDist(final double minDist) {
    this.minDist = minDist;
  }

  /**
   * Getter.
   * 
   * @return The minimal distance between nodes.
   */
  public double getMinDist() {
    return minDist;
  }

  /**
   * Getter.
   * 
   * @param s The station.
   * @return The route to the station.
   */
  public RoutingResult getRoute(final BusStation s) {
    return routes[s.getId()];
  }

  @Override
  public double weight(final LayoutNode f, final LayoutNode t) {
    if(from == null || t == f) return 0;
    final BusStation fr = getStation(f);
    if(fr.equals(from)) return 0;
    final BusStation to = getStation(t);
    if(to.equals(from)) {
      if(getRoute(fr).isReachable()) {
        final double secs = factor * getRoute(fr).seconds();
        return secs / BusTime.SECONDS_PER_MINUTE;
      }
      return Double.NaN;
    }
    return -minDist;
  }

  @Override
  public boolean hasWeight(final LayoutNode f, final LayoutNode t) {
    if(from == null || t == f) return false;
    final BusStation fr = getStation(f);
    if(fr.equals(from)) return false;
    final BusStation to = getStation(t);
    if(to.equals(from)) return getRoute(fr).isReachable();
    return true;
  }

  @Override
  public List<LayoutNode> nodes() {
    return nodes;
  }

  @Override
  public double springConstant() {
    return 0.75;
  }

  @Override
  public Point2D getDefaultPosition(final LayoutNode node) {
    final BusStation station = getStation(node);
    return new Point2D.Double(station.getDefaultX(), station.getDefaultY());
  }

  @Override
  public LayoutNode getReferenceNode() {
    return getNode(from);
  }

  /**
   * Getter.
   * 
   * @param station The station.
   * @return The corresponding node.
   */
  public LayoutNode getNode(final BusStation station) {
    return station == null ? null : rev[station.getId()];
  }

  /**
   * Getter.
   * 
   * @param i The node id.
   * @return The node.
   */
  public LayoutNode getNode(final int i) {
    return rev[i];
  }

  @Override
  public List<WeightedEdge> edgesTo(final LayoutNode to) {
    final BusStation station = getStation(to);
    final RoutingResult r = getRoute(station);
    if(r.isStartNode() || !r.isReachable()) return Collections.emptyList();
    final Collection<BusEdge> e = r.getEdges();
    final WeightedEdge[] edges = new WeightedEdge[e.size()];
    BusTime start = getTime();
    if(start == null) {
      start = BusTime.now();
    }
    LayoutNode cur = getReferenceNode();
    int i = 0;
    for(final BusEdge be : e) {
      final BusTime end = be.getEnd();
      final LayoutNode next = getNode(be.getTo());
      edges[i++] = new WeightedEdge(cur, next, factor * start.secondsTo(end)
          / BusTime.SECONDS_PER_MINUTE);
      start = end;
      cur = next;
    }
    return Arrays.asList(edges);
  }

}
