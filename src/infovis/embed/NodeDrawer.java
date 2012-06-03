package infovis.embed;

import java.awt.Graphics2D;
import java.awt.Shape;

public interface NodeDrawer {

  void drawNode(Graphics2D g, SpringNode n);

  Shape nodeClickArea(SpringNode n);

  void clickedAt(SpringNode n);

}
