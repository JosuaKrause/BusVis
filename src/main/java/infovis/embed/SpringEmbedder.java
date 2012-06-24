package infovis.embed;


/**
 * Simulates a spring embedder.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class SpringEmbedder extends AbstractEmbedder {

  /**
   * The weighter, defining edges between nodes.
   */
  private final Weighter weighter;

  /**
   * Creates a spring embedder and automatically starts it.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public SpringEmbedder(final Weighter weighter, final NodeDrawer drawer) {
    super(drawer);
    this.weighter = weighter;
  }

  /**
   * Whether the nodes should be reset.
   */
  private boolean reset;

  @Override
  protected boolean step() {
    if(weighter.getReferenceNode() == null) {
      if(!reset) {
        for(final SpringNode n : weighter.nodes()) {
          n.setPosition(weighter.getDefaultPosition(n));
        }
        reset = true;
      }
    } else {
      reset = false;
    }
    double mx = 0;
    double my = 0;
    double m = 0;
    for(final SpringNode n : weighter.nodes()) {
      n.move(weighter);
      mx += n.getDx();
      my += n.getDy();
      ++m;
    }
    mx /= -m;
    my /= -m;
    for(final SpringNode n : weighter.nodes()) {
      if(correctMovement) {
        n.addMove(mx, my);
      }
      n.step();
    }
    return true;
  }

  /**
   * Whether to correct the movement of the nodes by removing overall movements.
   */
  private boolean correctMovement;

  /**
   * Getter.
   * 
   * @return Whether the movement is corrected.
   */
  public boolean isCorrectingMovement() {
    return correctMovement;
  }

  /**
   * Setter.
   * 
   * @param correctMovement Sets whether to correct overall movements.
   */
  public void setCorrectMovement(final boolean correctMovement) {
    this.correctMovement = correctMovement;
  }

}
