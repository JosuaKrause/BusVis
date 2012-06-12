package infovis.ctrl;

import static infovis.data.BusTime.*;
import infovis.data.BusStation;
import infovis.data.BusTime;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A control panel to access the controller via GUI.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class ControlPanel extends JPanel implements BusVisualization {

  /**
   * SVUID.
   */
  private static final long serialVersionUID = 1644268841480928696L;

  /**
   * The constraint during construction.
   */
  private GridBagConstraints constraint;

  /**
   * The station box.
   */
  protected final JComboBox box;

  /**
   * The bus time slider.
   */
  protected final JSlider bt;

  /**
   * The bus time label.
   */
  private final JLabel btLabel;

  /**
   * The change time slider.
   */
  protected final JSlider ct;

  /**
   * The change time label.
   */
  private final JLabel ctLabel;

  /**
   * The time window slider.
   */
  protected final JSlider tw;

  /**
   * The time window label.
   */
  private final JLabel twLabel;

  /**
   * Creates a control panel.
   * 
   * @param ctrl The corresponding controller.
   */
  public ControlPanel(final Controller ctrl) {
    setLayout(new GridBagLayout());
    constraint = new GridBagConstraints();
    constraint.gridx = 0;
    constraint.fill = GridBagConstraints.BOTH;
    // station selection
    box = new JComboBox(ctrl.getAllStations());
    box.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final Object station = box.getSelectedItem();
        if(station != ctrl.getSelectedStation()) {
          ctrl.selectStation((BusStation) station);
          ctrl.focusStation();
        }
      }

    });
    addHor(new JLabel("Stations:"), box);
    // start time
    bt = new JSlider(0, MIDNIGHT.minutesTo(MIDNIGHT.later(-1)));
    bt.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final int min = bt.getValue();
        final int to = MIDNIGHT.minutesTo(ctrl.getTime());
        if(min != to) {
          ctrl.setTime(MIDNIGHT.later(min));
        }
      }

    });
    btLabel = new JLabel();
    addHor(new JLabel("Start Time:"), bt, btLabel);
    // change time
    ct = new JSlider(-10, 60);
    ct.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final int min = ct.getValue();
        if(min != ctrl.getChangeTime()) {
          ctrl.setChangeTime(min);
        }
      }

    });
    ctLabel = new JLabel();
    addHor(new JLabel("Change Time:"), ct, ctLabel);
    // time window
    tw = new JSlider(0, 24);
    tw.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent arg0) {
        final int v = tw.getValue();
        if(ctrl.getMaxTimeHours() != v) {
          ctrl.setMaxTimeHours(v);
        }
      }

    });
    twLabel = new JLabel();
    addHor(new JLabel("Time window:"), tw, twLabel);
    // end of layout
    constraint = null;
    ctrl.addBusVisualization(this);
  }

  /**
   * Adds a number of components to the panel.
   * 
   * @param comps The components to add.
   */
  private void addHor(final JComponent... comps) {
    if(constraint == null) throw new IllegalStateException(
        "layouting already done");
    final JPanel hor = new JPanel();
    hor.setLayout(new BoxLayout(hor, BoxLayout.X_AXIS));
    boolean first = true;
    for(final JComponent c : comps) {
      if(first) {
        first = false;
      } else {
        hor.add(Box.createRigidArea(new Dimension(5, 5)));
      }
      if(c != null) {
        hor.add(c);
      }
    }
    add(hor, constraint);
  }

  @Override
  public void selectBusStation(final BusStation station) {
    box.setSelectedItem(station);
  }

  @Override
  public void setStartTime(final BusTime time) {
    bt.setValue(MIDNIGHT.minutesTo(time));
    btLabel.setText(time.pretty());
  }

  @Override
  public void setChangeTime(final int minutes) {
    ct.setValue(minutes);
    ctLabel.setText(BusTime.minutesToString(minutes));
  }

  @Override
  public void focusStation() {
    // already covered by select bus station
  }

  @Override
  public void undefinedChange(final Controller ctrl) {
    final int mth = ctrl.getMaxTimeHours();
    tw.setValue(mth);
    twLabel.setText(BusTime.minutesToString(mth * BusTime.MINUTES_PER_HOUR));
  }

}
