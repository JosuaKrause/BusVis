package infovis.gui;

import infovis.busvis.BusvisCanvas;
import infovis.ctrl.Controller;
import infovis.data.BusStationManager;
import infovis.overview.Overview;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

/**
 * The main window.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class MainWindow extends JFrame {

  /** Controller. */
  final Controller ctrl;

  /**
   * Creates the main window.
   * 
   * @param m The bus station manager.
   */
  public MainWindow(final BusStationManager m) {
    ctrl = new Controller(m, this);
    final Overview over = new Overview(ctrl, 400, 400);
    final JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, over,
        new ControlPanel(ctrl));
    left.setDividerLocation(400);
    left.setOneTouchExpandable(true);
    final BusvisCanvas mainCanvas = BusvisCanvas.createBusCanvas(ctrl, 900, 900);
    final JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, mainCanvas);
    add(pane);
    pack();
    mainCanvas.reset();
    mainCanvas.addAction(KeyEvent.VK_F, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        setExtendedState(getExtendedState() == Frame.MAXIMIZED_BOTH ? Frame.NORMAL
            : Frame.MAXIMIZED_BOTH);
      }

    });
    addWindowStateListener(new WindowStateListener() {

      @Override
      public void windowStateChanged(final WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            if(ctrl.getSelectedStation() == null) {
              mainCanvas.reset();
            }
          }

        });
      }

    });
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        over.loadSVG(ctrl);
      }

    });
  }

  @Override
  public void dispose() {
    ctrl.quit(true);
    super.dispose();
  }

}
