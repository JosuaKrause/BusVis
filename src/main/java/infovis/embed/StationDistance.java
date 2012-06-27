package infovis.embed;

import infovis.ctrl.Controller;
import infovis.data.BusEdge;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.data.EdgeMatrix;
import infovis.routing.RoutingManager;
import infovis.routing.RoutingManager.CallBack;
import infovis.routing.RoutingResult;
import infovis.util.Interpolator;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Weights the station network after the distance from one start station.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class StationDistance implements Weighter {

  /**
   * The backing map for the spring nodes.
   */
  private final Map<SpringNode, BusStation> map;

  /**
   * The reverse backing map for the spring nodes.
   */
  private final SpringNode[] rev;

  /**
   * Dummy routes for uninitialized routings.
   */
  private final RoutingResult[] dummyRoutes;

  /**
   * The routes from the bus station, may be <code>null</code>.
   */
  protected volatile RoutingResult[] routes;

  /**
   * The current reference time.
   */
  protected BusTime time = new BusTime(12, 0);

  /**
   * The change time for lines.
   */
  protected int changeTime = 5;

  /**
   * The start bus station or <code>null</code> if there is none.
   */
  protected BusStation from;

  /**
   * The factor to scale the distances.
   */
  private double factor = .1;

  /**
   * The controller.
   */
  protected final Controller ctrl;

  /**
   * The undirected edge matrix.
   */
  private final EdgeMatrix matrix;

  /**
   * The routing manager.
   */
  private final RoutingManager rm = RoutingManager.newInstance();

  /**
   * The animator to be notified when something has changed.
   */
  private Animator animator;

  private Fader fader;

  /**
   * Creates a station distance without a reference station.
   * 
   * @param ctrl The controller.
   */
  public StationDistance(final Controller ctrl) {
    this.ctrl = ctrl;
    matrix = new EdgeMatrix(ctrl.getBusStationManager());
    dummyRoutes = new RoutingResult[ctrl.maxId() + 1];
    for(int id = 0; id < dummyRoutes.length; ++id) {
      dummyRoutes[id] = new RoutingResult(ctrl.getForId(id)); // dummy results
    }
    routes = dummyRoutes;
    map = new HashMap<SpringNode, BusStation>();
    rev = new SpringNode[ctrl.maxId() + 1];
    for(final BusStation s : ctrl.getStations()) {
      final SpringNode node = new SpringNode();
      node.setPosition(s.getDefaultX(), s.getDefaultY());
      map.put(node, s);
      rev[s.getId()] = node;
    }
  }

  public void setFader(final Fader fader) {
    this.fader = fader;
  }

  /**
   * Sets the values for the distance.
   * 
   * @param from The reference station.
   * @param time The reference time.
   * @param changeTime The change time.
   */
  public void set(final BusStation from, final BusTime time, final int changeTime) {
    fader.setPredict(from);
    if(from == null) {
      putSettings(dummyRoutes, from, time, changeTime);
      return;
    }
    final CallBack<RoutingResult[]> cb = new CallBack<RoutingResult[]>() {

      @Override
      public void callBack(final RoutingResult[] result) {
        putSettings(result, from, time, changeTime);
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
   */
  protected synchronized void putSettings(final RoutingResult[] route,
      final BusStation from, final BusTime time, final int changeTime) {
    routes = route;
    matrix.refreshHighlights(routes);
    if(from != StationDistance.this.from) {
      fader.initialize(StationDistance.this.from, System.currentTimeMillis(),
          Interpolator.NORMAL);
    }
    changes = ((time != null && StationDistance.this.time != null) &&
        (StationDistance.this.time != time || StationDistance.this.changeTime != changeTime))
        ? FAST_ANIMATION_CHANGE : NORMAL_CHANGE;
    StationDistance.this.from = from;
    StationDistance.this.time = time;
    StationDistance.this.changeTime = changeTime;
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

  public Controller getController() {
    return ctrl;
  }

  public BusStation getStation(final SpringNode n) {
    return map.get(n);
  }

  public EdgeMatrix getMatrix() {
    return matrix;
  }

  /**
   * Whether the weights have changed.
   */
  protected volatile int changes;

  @Override
  public int changes() {
    final int res = changes;
    changes = NO_CHANGE;
    return res;
  }

  /**
   * Signals undefined changes.
   */
  public void changeUndefined() {
    set(from, time, changeTime);
  }

  /**
   * Setter.
   * 
   * @param from Sets the reference station.
   */
  public void setFrom(final BusStation from) {
    set(from, time, changeTime);
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
   */
  public void setTime(final BusTime time) {
    set(from, time, changeTime);
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
    set(from, time, changeTime);
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

  /**
   * Getter.
   * 
   * @return The distance factor.
   */
  public double getFactor() {
    return factor;
  }

  /**
   * The minimal distance between nodes.
   */
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
  public double weight(final SpringNode f, final SpringNode t) {
    if(from == null || t == f) return 0;
    final BusStation fr = map.get(f);
    if(fr.equals(from)) return 0;
    final BusStation to = map.get(t);
    if(to.equals(from) && !getRoute(fr).isNotReachable()) return factor
        * getRoute(fr).minutes();
    return -minDist;
  }

  @Override
  public boolean hasWeight(final SpringNode f, final SpringNode t) {
    if(from == null || t == f) return false;
    final BusStation fr = map.get(f);
    if(fr.equals(from)) return false;
    final BusStation to = map.get(t);
    if(to.equals(from)) return !getRoute(fr).isNotReachable();
    return true;
  }

  @Override
  public Iterable<SpringNode> nodes() {
    return map.keySet();
  }

  @Override
  public double springConstant() {
    return 0.75;
  }

  @Override
  public Point2D getDefaultPosition(final SpringNode node) {
    final BusStation station = map.get(node);
    return new Point2D.Double(station.getDefaultX(), station.getDefaultY());
  }

  @Override
  public SpringNode getReferenceNode() {
    return getNode(from);
  }

  /**
   * Getter.
   * 
   * @param station The station.
   * @return The corresponding node.
   */
  public SpringNode getNode(final BusStation station) {
    return station == null ? null : rev[station.getId()];
  }

  @Override
  public List<WeightedEdge> edgesTo(final SpringNode to) {
    final BusStation station = getStation(to);
    final RoutingResult r = getRoute(station);
    if(r.isStartNode() || r.isNotReachable()) return Collections.emptyList();
    final Collection<BusEdge> e = r.getEdges();
    final WeightedEdge[] edges = new WeightedEdge[e.size()];
    BusTime start = getTime();
    SpringNode cur = getReferenceNode();
    int i = 0;
    for(final BusEdge be : e) {
      final BusTime end = be.getEnd();
      final SpringNode next = getNode(be.getTo());
      edges[i++] = new WeightedEdge(cur, next, factor * start.minutesTo(end));
      start = end;
      cur = next;
    }
    return Arrays.asList(edges);
  }

}
