package infovis.gui;

import infovis.busvis.BusvisCanvas;
import infovis.ctrl.Controller;
import infovis.data.BusStationManager;
import infovis.overview.Overview;
import infovis.util.Screenshot;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
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
    final ControlPanel cp = new ControlPanel(ctrl);
    final JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, over, cp);
    left.setDividerLocation(400);
    left.setOneTouchExpandable(true);
    final BusvisCanvas mainCanvas = BusvisCanvas.createBusCanvas(ctrl, 900, 900);
    final JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, mainCanvas);
    add(pane);
    pack();
    mainCanvas.reset();
    addAction(KeyEvent.VK_F, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final boolean isMax = getExtendedState() == Frame.MAXIMIZED_BOTH;
        setExtendedState(isMax ? Frame.NORMAL : Frame.MAXIMIZED_BOTH);
      }

    });
    addAction(KeyEvent.VK_P, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent ae) {
        System.out.println("saving screenshots...");
        final File dir = new File("./pics/");
        try {
          Screenshot.saveSVG(dir, "vis", mainCanvas);
        } catch(final IOException e) {
          e.printStackTrace();
        }
        try {
          Screenshot.savePNG(dir, "all", getRootPane());
        } catch(final IOException e) {
          e.printStackTrace();
        }
        System.out.println("done");
      }

    });
    addAction(KeyEvent.VK_Q, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        ctrl.quit(false);
      }

    });
    addAction(KeyEvent.VK_R, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        ctrl.selectStation(null);
        ctrl.focusStation();
      }

    });
    addAction(KeyEvent.VK_V, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        mainCanvas.reset();
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
    final JRootPane root = getRootPane();
    root.setFocusable(true);
    root.grabFocus();
    mainCanvas.setFocusComponent(root);
    over.setFocusComponent(root);
  }

  /**
   * Adds a keyboard action event.
   * 
   * @param key The key id, given by {@link KeyEvent}. (Constants beginning with
   *          <code>VK</code>)
   * @param a The action that is performed.
   */
  public void addAction(final int key, final Action a) {
    final JRootPane root = getRootPane();
    final Object token = new Object();
    final InputMap input = root.getInputMap();
    input.put(KeyStroke.getKeyStroke(key, 0), token);
    final ActionMap action = root.getActionMap();
    action.put(token, a);
  }

  @Override
  public void dispose() {
    ctrl.quit(true);
    super.dispose();
  }

}
