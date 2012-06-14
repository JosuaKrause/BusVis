package infovis.gui;

import infovis.ctrl.Controller;
import infovis.data.BusStationManager;
import infovis.embed.BusCanvas;
import infovis.overview.Overview;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

/**
 * The main window.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class MainWindow extends JFrame {

  /**
   * SVUID.
   */
  private static final long serialVersionUID = 1471398627061096012L;

  /**
   * Creates the main window.
   * 
   * @param m The bus station manager.
   */
  public MainWindow(final BusStationManager m) {
    final Controller ctrl = new Controller(m, this);
    final JPanel left = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    final Overview over = new Overview(ctrl, 200, 200);
    left.add(over);
    left.add(new ControlPanel(ctrl));
    final BusCanvas mainCanvas = BusCanvas.createBusCanvas(ctrl, 800, 600);
    final JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, mainCanvas);
    add(pane);
    pack();
    mainCanvas.reset();
    mainCanvas.addAction(KeyEvent.VK_F, new AbstractAction() {

      private static final long serialVersionUID = 3038019958008049173L;

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
            mainCanvas.reset();
          }

        });
      }

    });
  }

}