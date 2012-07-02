package infovis.gui;

import static infovis.data.BusTime.*;
import infovis.ctrl.BusVisualization;
import infovis.ctrl.Controller;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.embed.Embedders;
import infovis.routing.RoutingAlgorithm;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A control panel to access the controller via GUI.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class ControlPanel extends JPanel implements BusVisualization {

  /** SVUID. */
  private static final long serialVersionUID = 1644268841480928696L;

  /** The station box. */
  protected final JComboBox box;

  /** The bus start time hours. */
  protected final JSpinner startHours;

  /** The bus start time minutes. */
  protected final JSpinner startMinutes;

  /** The last start minute time (used for changing the hours). */
  protected int lastStartMin = -1;

  /** The bus time label. */
  private final JLabel btLabel;

  /** The check box to select now as start time. */
  protected final JCheckBox now;

  /** Bus change time. */
  protected final JSpinner changeMinutes;

  /** The change time label. */
  private final JLabel ctLabel;

  /** The time window spinner. */
  protected final JSpinner timeWindow;

  /** The walk time window label. */
  private final JLabel twwLabel;

  /** Walk time in hours. */
  protected final JSpinner timeWalkHours;

  /** Walk time in minutes. */
  protected final JSpinner timeWalkMinutes;

  /** Minutes for the last walk. */
  protected int lastWalkMin = -1;

  /** The time window label. */
  private final JLabel twLabel;

  /** Maps bus station ids to indices in the combo box. */
  private final int[] indexMap;

  /** The algorithm box. */
  protected final JComboBox algoBox;

  /** The technique box. */
  protected final JComboBox embedBox;

  /**
   * A thin wrapper for the bus station name. Also allows the <code>null</code>
   * bus station, representing no selection.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  private static final class BusStationName {

    /** The associated bus station. */
    public final BusStation station;

    /** The name of the station. */
    private final String name;

    /**
     * Creates a bus station name object.
     * 
     * @param station The station.
     */
    public BusStationName(final BusStation station) {
      this.station = station;
      name = station != null ? station.getName() : "(no selection)";
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Creates a list of all bus station names.
   * 
   * @param ctrl The controller.
   * @return All bus station names.
   */
  private static BusStationName[] getStations(final Controller ctrl) {
    final Collection<BusStation> s = ctrl.getStations();
    final BusStation[] arr = s.toArray(new BusStation[s.size()]);
    Arrays.sort(arr, new Comparator<BusStation>() {

      @Override
      public int compare(final BusStation a, final BusStation b) {
        return a.getName().compareTo(b.getName());
      }

    });
    final BusStationName[] res = new BusStationName[arr.length + 1];
    res[0] = new BusStationName(null);
    for(int i = 0; i < arr.length; ++i) {
      res[i + 1] = new BusStationName(arr[i]);
    }
    return res;
  }

  /**
   * Creates a control panel.
   * 
   * @param ctrl The corresponding controller.
   */
  public ControlPanel(final Controller ctrl) {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    final Component space = Box.createRigidArea(new Dimension(5, 5));
    // routing selection
    final RoutingAlgorithm[] algos = Controller.getRoutingAlgorithms();
    if(algos.length != 1) {
      algoBox = new JComboBox(algos);
      algoBox.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(final ActionEvent e) {
          final RoutingAlgorithm routing = (RoutingAlgorithm)
              algoBox.getSelectedItem();
          if(routing != ctrl.getRoutingAlgorithm()) {
            ctrl.setRoutingAlgorithm(routing);
          }
        }

      });
      algoBox.setMaximumSize(algoBox.getPreferredSize());
      addHor(new JLabel("Routing:"), algoBox);
    } else {
      algoBox = null;
    }
    // embedder selection
    final Embedders[] embeds = Controller.getEmbedders();
    if(embeds.length != 1) {
      embedBox = new JComboBox(embeds);
      embedBox.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(final ActionEvent ae) {
          final Embedders e = (Embedders) embedBox.getSelectedItem();
          if(e != ctrl.getEmbedder()) {
            ctrl.setEmbedder(e);
          }
        }

      });
      final Dimension size = embedBox.getPreferredSize();
      embedBox.setMaximumSize(new Dimension(200, size.height));
      addHor(new JLabel("Positioning:"), embedBox);
    } else {
      embedBox = null;
    }
    // station selection
    final BusStationName[] stations = getStations(ctrl);
    indexMap = new int[ctrl.maxId() + 1];
    for(int i = 0; i < stations.length; ++i) {
      if(stations[i].station == null) {
        continue;
      }
      indexMap[stations[i].station.getId()] = i;
    }
    box = new JComboBox(stations);
    box.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final BusStation station = ((BusStationName) box.getSelectedItem()).station;
        if(station != ctrl.getSelectedStation()) {
          ctrl.selectStation(station);
          ctrl.focusStation();
        }
      }

    });
    box.setMaximumSize(box.getPreferredSize());
    addHor(new JLabel("Stations:"), box);
    add(new JSeparator(SwingConstants.HORIZONTAL));

    // start time
    final CyclicNumberModel hours = new CyclicNumberModel(0, 0, 23);
    startHours = new JSpinner(hours);
    startHours.setMaximumSize(new Dimension(60, 40));
    startHours.setPreferredSize(new Dimension(60, 40));
    startHours.addMouseWheelListener(new MouseWheelListener() {

      @Override
      public void mouseWheelMoved(final MouseWheelEvent e) {
        if(e.getWheelRotation() < 0) {
          startHours.setValue(startHours.getNextValue());
        } else {
          startHours.setValue(startHours.getPreviousValue());
        }
      }

    });

    final CyclicNumberModel minutes = new CyclicNumberModel(0, 0, 59);
    startMinutes = new JSpinner(minutes);
    startMinutes.setMaximumSize(new Dimension(60, 40));
    startMinutes.setPreferredSize(new Dimension(60, 40));
    startMinutes.addMouseWheelListener(new MouseWheelListener() {

      @Override
      public void mouseWheelMoved(final MouseWheelEvent e) {
        if(e.getWheelRotation() < 0) {
          startMinutes.setValue(startMinutes.getNextValue());
        } else {
          startMinutes.setValue(startMinutes.getPreviousValue());
        }
      }

    });

    startHours.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final BusTime startTime = getStartTime();
        if(!ctrl.getTime().equals(startTime)) {
          ctrl.setTime(startTime);
        }
      }

    });

    startMinutes.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final BusTime startTime = getStartTime();
        if(ctrl.getTime().equals(startTime)) return;
        if(lastStartMin == 59 && startMinutes.getValue().equals(0)) {
          startHours.setValue(startHours.getNextValue());
        } else if(lastStartMin == 0 && startMinutes.getValue().equals(59)) {
          startHours.setValue(startHours.getPreviousValue());
        }
        ctrl.setTime(getStartTime());
        lastStartMin = startTime.getMinute();
      }

    });

    btLabel = new JLabel();
    now = new JCheckBox("now");
    now.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        final boolean b = ctrl.isStartTimeNow();
        if(now.isSelected()) {
          if(!b) {
            ctrl.setNow();
          }
        } else {
          if(b) {
            ctrl.setTime(getStartTime());
          }
        }
      }
    });
    addHor(new JLabel("Start Time:"), startHours, startMinutes, btLabel, now, space);
    add(new JSeparator(SwingConstants.HORIZONTAL));

    // change time
    final SpinnerNumberModel cMinutes = new SpinnerNumberModel(0, -5, 60, 1);
    changeMinutes = new JSpinner(cMinutes);
    changeMinutes.setMaximumSize(new Dimension(60, 40));
    changeMinutes.setPreferredSize(new Dimension(60, 40));

    changeMinutes.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final int changeTime = getChangeTime();
        if(changeTime != ctrl.getChangeTime()) {
          ctrl.setChangeTime(getChangeTime());
        }
      }

    });

    ctLabel = new JLabel();
    addHor(new JLabel("Change Time:"), changeMinutes, ctLabel, space);
    add(new JSeparator(SwingConstants.HORIZONTAL));

    // time window
    final SpinnerNumberModel tWindow = new SpinnerNumberModel(0, 0, 24, 1);
    timeWindow = new JSpinner(tWindow);
    timeWindow.setMaximumSize(new Dimension(60, 40));
    timeWindow.setPreferredSize(new Dimension(60, 40));

    timeWindow.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final int time = (Integer) timeWindow.getValue();
        if(ctrl.getMaxTimeHours() != time) {
          ctrl.setMaxTimeHours(time);
        }
      }

    });

    twLabel = new JLabel();
    addHor(new JLabel("Max Wait:"), timeWindow, twLabel, space);
    add(new JSeparator(SwingConstants.HORIZONTAL));

    // walk time window
    final CyclicNumberModel walkHours = new CyclicNumberModel(0, 0, 23);
    timeWalkHours = new JSpinner(walkHours);
    timeWalkHours.setMaximumSize(new Dimension(60, 40));
    timeWalkHours.setPreferredSize(new Dimension(60, 40));

    final CyclicNumberModel walkMinutes = new CyclicNumberModel(0, 0, 59);
    timeWalkMinutes = new JSpinner(walkMinutes);
    timeWalkMinutes.setMaximumSize(new Dimension(60, 40));
    timeWalkMinutes.setPreferredSize(new Dimension(60, 40));

    timeWalkHours.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final int walkTime = getWalkTime();
        if(!(ctrl.getWalkTime() == walkTime)) {
          ctrl.setWalkTime(getWalkTime());
        }
      }

    });

    timeWalkMinutes.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        if(lastWalkMin % 60 == 59 && timeWalkMinutes.getValue().equals(0)) {
          timeWalkHours.setValue(timeWalkHours.getNextValue());
        } else if(lastWalkMin % 60 == 0 && timeWalkMinutes.getValue().equals(59)) {
          timeWalkHours.setValue(timeWalkHours.getPreviousValue());
        }
        final int walkTime = getWalkTime();
        if(!(ctrl.getWalkTime() == walkTime)) {
          ctrl.setWalkTime(getWalkTime());
        }
        lastWalkMin = walkTime;
      }

    });

    twwLabel = new JLabel();
    addHor(new JLabel("Max Walk:"), timeWalkHours, timeWalkMinutes, twwLabel, space);

    // end of layout
    add(Box.createVerticalGlue());
    ctrl.addBusVisualization(this);
  }

  /**
   * A cyclic number spinner for a JSpinner.
   * 
   * @author Marc Spicker
   */
  private class CyclicNumberModel extends SpinnerNumberModel {

    /**
     * Serial version ID.
     */
    private static final long serialVersionUID = 3042013777179841232L;

    /**
     * Constructor.
     * 
     * @param value Initial value.
     * @param min Minimum.
     * @param max Maximum.
     */
    public CyclicNumberModel(final int value, final int min, final int max) {
      super(value, min, max, 1);
    }

    @Override
    public Object getNextValue() {
      if(super.getValue().equals(super.getMaximum())) return super.getMinimum();
      return super.getNextValue();
    }

    @Override
    public Object getPreviousValue() {
      if(super.getValue().equals(super.getMinimum())) return super.getMaximum();
      return super.getPreviousValue();
    }

    @Override
    public Object getValue() {
      return super.getValue();
    }

  }

  /**
   * Returns the start time that is currently entered in the two spinners.
   * 
   * @return The current bus starting time.
   */
  protected BusTime getStartTime() {
    final int hour = ((Integer) startHours.getValue()).intValue();
    final int minute = ((Integer) startMinutes.getValue()).intValue();
    return new BusTime(hour, minute);
  }

  /**
   * Returns the walk time that is currently entered in the two spinners.
   * 
   * @return The current walk time.
   */
  protected int getWalkTime() {
    final int hour = ((Integer) timeWalkHours.getValue()).intValue();
    final int minute = ((Integer) timeWalkMinutes.getValue()).intValue();
    return hour * 60 + minute;
  }

  /**
   * Returns the change time that is currently entered in the two spinners.
   * 
   * @return The current bus change time.
   */
  protected int getChangeTime() {
    return ((Integer) changeMinutes.getValue()).intValue();
  }

  /**
   * Adds a number of components to the panel.
   * 
   * @param comps The components to add.
   */
  private void addHor(final Component... comps) {
    final JPanel hor = new JPanel();
    hor.setLayout(new BoxLayout(hor, BoxLayout.X_AXIS));
    for(final Component c : comps) {
      hor.add(Box.createRigidArea(new Dimension(5, 5)));
      if(c != null) {
        hor.add(c);
      }
    }
    hor.setAlignmentX(Component.LEFT_ALIGNMENT);
    add(hor);
  }

  @Override
  public void selectBusStation(final BusStation station) {
    box.setSelectedIndex(station != null ? indexMap[station.getId()] : 0);
  }

  @Override
  public void setStartTime(final BusTime time) {
    if(time == null) {
      startHours.setEnabled(false);
      startMinutes.setEnabled(false);
      now.setSelected(true);
      final Calendar cal = Calendar.getInstance();
      btLabel.setText(BusTime.fromCalendar(cal).pretty(isBlinkSecond(cal)));
      return;
    }
    startHours.setEnabled(true);
    startMinutes.setEnabled(true);
    now.setSelected(false);
    startHours.setValue(time.getHour());
    startMinutes.setValue(time.getMinute());
    btLabel.setText(time.pretty());
  }

  @Override
  public void overwriteDisplayedTime(final BusTime time, final boolean blink) {
    btLabel.setText(time.pretty(blink));
  }

  @Override
  public void setChangeTime(final int minutes) {
    changeMinutes.setValue(minutes);
    ctLabel.setText(BusTime.minutesToString(minutes));
  }

  @Override
  public void setEmbedder(final Embedders embed) {
    if(embedBox != null) {
      embedBox.setSelectedItem(embed);
    }
  }

  @Override
  public void undefinedChange(final Controller ctrl) {
    final int mth = ctrl.getMaxTimeHours();
    timeWindow.setValue(mth);
    twLabel.setText(BusTime.minutesToString(mth * BusTime.MINUTES_PER_HOUR));

    final int walkTime = ctrl.getWalkTime();
    timeWalkHours.setValue(walkTime / 60);
    timeWalkMinutes.setValue(walkTime % 60);
    twwLabel.setText(BusTime.minutesToString(walkTime));

    if(algoBox != null) {
      algoBox.setSelectedItem(ctrl.getRoutingAlgorithm());
    }
  }

  @Override
  public void focusStation() {
    // already covered by select bus station
  }

}
