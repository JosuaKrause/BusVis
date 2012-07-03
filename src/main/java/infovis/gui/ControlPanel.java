package infovis.gui;

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
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
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

  /** The fast forward mode icon. */
  private static final Icon FFW_MODE =
      new ImageIcon("src/main/resources/pics/Fast-forward.gif");

  /** The fast forward stop icon. */
  private static final Icon FFW_STOP = new ImageIcon("src/main/resources/pics/Stop.gif");

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

  /** Bus change time spinner. */
  protected final JSpinner changeMinutes;

  /** Bus change time slider. */
  protected final JSlider changeMinutesSlider;

  /** The time window spinner. */
  protected final JSpinner timeWindow;

  /** The time windows slider. */
  protected final JSlider timeWindowSlider;

  /** Walk time in hours. */
  protected final JSpinner timeWalkHours;

  /** Walk time in minutes. */
  protected final JSpinner timeWalkMinutes;

  /** Walk time slider. */
  protected final JSlider timeWalkSlider;

  /** Minutes for the last walk. */
  protected int lastWalkMin = -1;

  /** Maps bus station ids to indices in the combo box. */
  private final int[] indexMap;

  /** The algorithm box. */
  protected final JComboBox algoBox;

  /** The technique box. */
  protected final JComboBox embedBox;

  /** The ffw slider. */
  protected final JSlider ffwSlider;

  /** The ffw button. */
  protected final JButton ffwButton;

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

  } // BusStationName

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
   * Cyclic mouse wheel listener that corresponds to a JSpinner. Increases /
   * descreases the spinner value depending on the direction of the mousewheel
   * movement.
   * 
   * @author Marc Spicker
   */
  private class CyclicMouseWheelListener implements MouseWheelListener {

    /** Parent spinner. */
    private final JSpinner parent;

    /**
     * Constructor.
     * 
     * @param parent The parent spinner.
     */
    public CyclicMouseWheelListener(final JSpinner parent) {
      this.parent = parent;
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
      if(e.getWheelRotation() < 0) {
        for(int i = 0; i < -e.getWheelRotation(); ++i) {
          parent.setValue(parent.getNextValue());
        }
      } else {
        for(int i = 0; i < e.getWheelRotation(); ++i) {
          parent.setValue(parent.getPreviousValue());
        }
      }
    }

  } // CyclicMouseWheelListener

  /**
   * Mouse wheel listener that corresponds to a JSpinner. Increases / descreases
   * the spinner value depending on the direction of the mousewheel movement.
   * 
   * @author Marc Spicker
   */
  private class SimpleMouseWheelListener implements MouseWheelListener {

    /** Parent spinner. */
    private final JSpinner parent;

    /** Minimum value. */
    private final int minVal;

    /** Maximum value. */
    private final int maxVal;

    /**
     * Constructor.
     * 
     * @param parent Corresponding spinner.
     * @param minVal Minimum value.
     * @param maxVal Maximum value.
     */
    public SimpleMouseWheelListener(final JSpinner parent, final int minVal,
        final int maxVal) {
      this.parent = parent;
      this.minVal = minVal;
      this.maxVal = maxVal;
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
      if(e.getWheelRotation() < 0) {
        for(int i = 0; i < -e.getWheelRotation(); ++i) {
          if(!parent.getValue().equals(maxVal)) {
            parent.setValue(parent.getNextValue());
          }
        }
      } else {
        for(int i = 0; i < e.getWheelRotation(); ++i) {
          if(!parent.getValue().equals(minVal)) {
            parent.setValue(parent.getPreviousValue());
          }
        }
      }
    }

  } // SimpleMouseWheelListener

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
      if(getValue().equals(getMaximum())) return getMinimum();
      return super.getNextValue();
    }

    @Override
    public Object getPreviousValue() {
      if(getValue().equals(getMinimum())) return getMaximum();
      return super.getPreviousValue();
    }

  } // CyclicNumberModel

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
    startHours.addMouseWheelListener(new CyclicMouseWheelListener(startHours));

    final CyclicNumberModel minutes = new CyclicNumberModel(0, 0, 59);
    startMinutes = new JSpinner(minutes);
    startMinutes.setMaximumSize(new Dimension(60, 40));
    startMinutes.setPreferredSize(new Dimension(60, 40));
    startMinutes.addMouseWheelListener(new CyclicMouseWheelListener(startMinutes));

    startHours.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final BusTime startTime = getStartTime();
        if(ctrl.getTime().equals(startTime)) return;
        ctrl.setTime(startTime);
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

    // sets the width of the label
    btLabel = new JLabel("24:00h");
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
    addHor(new JLabel("Start Time:"), startHours, new JLabel(":"),
        startMinutes, now, btLabel, new JLabel(" "));

    // fast forward
    ffwSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 60, 1);
    ffwSlider.setMajorTickSpacing(10);
    ffwSlider.setMinorTickSpacing(1);
    ffwSlider.setPaintTicks(true);
    ffwSlider.setPaintLabels(true);
    ffwSlider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final int min = ffwSlider.getValue();
        if(ctrl.getFastForwardStep() != min) {
          ctrl.setFastForwardStep(min);
        }
      }

    });

    ffwButton = new JButton(FFW_MODE);
    ffwButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        ctrl.setFastForwardMode(!ctrl.isInFastForwardMode());
      }

    });
    addHor(ffwSlider, ffwButton);
    add(new JSeparator(SwingConstants.HORIZONTAL));

    // change time

    changeMinutesSlider = new JSlider(-5, 60, 0);
    changeMinutesSlider.setMajorTickSpacing(5);
    changeMinutesSlider.setMinorTickSpacing(1);
    changeMinutesSlider.setPaintTicks(true);

    final Hashtable<Integer, JLabel> cmLabels = new Hashtable<Integer, JLabel>();
    cmLabels.put(-5, new JLabel("-5"));
    cmLabels.put(0, new JLabel("0"));
    cmLabels.put(5, new JLabel("5"));
    cmLabels.put(10, new JLabel("10"));
    cmLabels.put(20, new JLabel("20"));
    cmLabels.put(40, new JLabel("40"));
    cmLabels.put(60, new JLabel("60"));
    changeMinutesSlider.setLabelTable(cmLabels);

    changeMinutesSlider.setPaintLabels(true);
    changeMinutesSlider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final int changeTime = changeMinutesSlider.getValue();
        if(changeTime != ctrl.getChangeTime()) {
          changeMinutes.setValue(changeTime);
          ctrl.setChangeTime(changeTime);
        }
      }

    });

    final SpinnerNumberModel cMinutes = new SpinnerNumberModel(0, -5, 60, 1);
    changeMinutes = new JSpinner(cMinutes);
    changeMinutes.setMaximumSize(new Dimension(60, 40));
    changeMinutes.setPreferredSize(new Dimension(60, 40));
    changeMinutes.addMouseWheelListener(
        new SimpleMouseWheelListener(changeMinutes, -5, 60));

    changeMinutes.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final int changeTime = getChangeTime();
        if(changeTime != ctrl.getChangeTime()) {
          changeMinutesSlider.setValue(changeTime);
          ctrl.setChangeTime(getChangeTime());
        }
      }

    });

    addHor(new JLabel("Change Time:"), changeMinutes, new JLabel("min"),
        changeMinutesSlider, space);
    add(new JSeparator(SwingConstants.HORIZONTAL));

    // time window
    final SpinnerNumberModel tWindow = new SpinnerNumberModel(0, 0, 24, 1);
    timeWindow = new JSpinner(tWindow);
    timeWindow.setMaximumSize(new Dimension(60, 40));
    timeWindow.setPreferredSize(new Dimension(60, 40));
    timeWindow.addMouseWheelListener(new SimpleMouseWheelListener(timeWindow, 0, 24));

    timeWindow.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final int time = (Integer) timeWindow.getValue();
        if(ctrl.getMaxTimeHours() != time) {
          timeWindowSlider.setValue(time);
          ctrl.setMaxTimeHours(time);
        }
      }

    });

    timeWindowSlider = new JSlider(0, 24, 0);
    timeWindowSlider.setMajorTickSpacing(3);
    timeWindowSlider.setMinorTickSpacing(1);
    timeWindowSlider.setPaintTicks(true);
    timeWindowSlider.setPaintLabels(true);
    timeWindowSlider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final int time = timeWindowSlider.getValue();
        if(time != ctrl.getMaxTimeHours()) {
          timeWindow.setValue(time);
          ctrl.setMaxTimeHours(time);
        }
      }

    });

    addHor(new JLabel("Max Wait:"), timeWindow, new JLabel("h"), timeWindowSlider, space);
    add(new JSeparator(SwingConstants.HORIZONTAL));

    // walk time window

    final SpinnerNumberModel walkHours = new SpinnerNumberModel(0, 0, 1, 1);
    timeWalkHours = new JSpinner(walkHours);
    timeWalkHours.setMaximumSize(new Dimension(60, 40));
    timeWalkHours.setPreferredSize(new Dimension(60, 40));
    timeWalkHours.addMouseWheelListener(new SimpleMouseWheelListener(timeWalkHours, 0, 1));

    final CyclicNumberModel walkMinutes = new CyclicNumberModel(0, 0, 59);
    timeWalkMinutes = new JSpinner(walkMinutes);
    timeWalkMinutes.setMaximumSize(new Dimension(60, 40));
    timeWalkMinutes.setPreferredSize(new Dimension(60, 40));
    timeWalkMinutes.addMouseWheelListener(new MouseWheelListener() {

      @Override
      public void mouseWheelMoved(final MouseWheelEvent e) {
        if(e.getWheelRotation() < 0) {
          for(int i = 0; i < -e.getWheelRotation(); ++i) {
            if(!(timeWalkHours.getValue().equals(1) && timeWalkMinutes.getValue().equals(59))) {
              timeWalkMinutes.setValue(timeWalkMinutes.getNextValue());
            }
          }
        } else {
          for(int i = 0; i < e.getWheelRotation(); ++i) {
            if(!(timeWalkHours.getValue().equals(0) && timeWalkMinutes.getValue().equals(0))) {
              timeWalkMinutes.setValue(timeWalkMinutes.getPreviousValue());
            }
          }
        }
      }

    });

    timeWalkHours.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final int walkTime = getWalkTime();
        if(!(ctrl.getWalkTime() == walkTime)) {
          timeWalkSlider.setValue(walkTime);
          ctrl.setWalkTime(walkTime);
        }
      }

    });

    timeWalkMinutes.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        if(lastWalkMin % 60 == 59 && timeWalkMinutes.getValue().equals(0)) {
          if(timeWalkHours.getValue().equals(1)) {
            timeWalkMinutes.setValue(59);
          }
          timeWalkHours.setValue(timeWalkHours.getNextValue());
        } else if(lastWalkMin % 60 == 0 && timeWalkMinutes.getValue().equals(59)) {
          timeWalkHours.setValue(timeWalkHours.getPreviousValue());
        }
        final int walkTime = getWalkTime();
        if(ctrl.getWalkTime() != walkTime) {
          ctrl.setWalkTime(getWalkTime());
        }
        timeWalkSlider.setValue(getWalkTime());
        lastWalkMin = walkTime;
      }

    });

    timeWalkSlider = new JSlider(0, 119, 0);
    timeWalkSlider.setMajorTickSpacing(20);
    timeWalkSlider.setMinorTickSpacing(5);
    timeWalkSlider.setPaintTicks(true);

    final Hashtable<Integer, JLabel> twLabels = new Hashtable<Integer, JLabel>();
    twLabels.put(0, new JLabel("0"));
    twLabels.put(20, new JLabel("20"));
    twLabels.put(40, new JLabel("40"));
    twLabels.put(60, new JLabel("60"));
    twLabels.put(80, new JLabel("80"));
    twLabels.put(119, new JLabel("120"));
    timeWalkSlider.setLabelTable(twLabels);

    timeWalkSlider.setPaintLabels(true);
    timeWalkSlider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(final ChangeEvent e) {
        final int walkTime = timeWalkSlider.getValue();
        if(ctrl.getWalkTime() != walkTime) {
          timeWalkHours.setValue(walkTime / 60);
          timeWalkMinutes.setValue(walkTime % 60);
          ctrl.setWalkTime(walkTime);
        }
      }

    });

    addHor(new JLabel("Max Walk:"), timeWalkHours, new JLabel("h"),
        timeWalkMinutes, new JLabel("min"), timeWalkSlider, space);

    // end of layout
    add(Box.createVerticalGlue());
    ctrl.addBusVisualization(this);
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
  public void setStartTime(final BusTime time, final boolean ffwMode) {
    final boolean nowMode = time == null;
    final boolean canEdit = !nowMode && !ffwMode;
    startHours.setEnabled(canEdit);
    startMinutes.setEnabled(canEdit);
    ffwSlider.setEnabled(!nowMode);
    ffwButton.setEnabled(!nowMode);
    now.setSelected(nowMode);
    if(nowMode) {
      final BusTime now = BusTime.now();
      btLabel.setText(now.pretty(now.isBlinkSecond()));
    } else {
      startHours.setValue(time.getHour());
      startMinutes.setValue(time.getMinute());
      btLabel.setText("");
    }
  }

  @Override
  public void overwriteDisplayedTime(final BusTime time, final boolean blink) {
    btLabel.setText(now.isSelected() ? time.pretty(blink) : "");
  }

  @Override
  public void setChangeTime(final int minutes) {
    changeMinutes.setValue(minutes);
    changeMinutesSlider.setValue(minutes);
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
    timeWindowSlider.setValue(mth);

    final int walkTime = ctrl.getWalkTime();
    timeWalkHours.setValue(walkTime / 60);
    timeWalkMinutes.setValue(walkTime % 60);
    timeWalkSlider.setValue(walkTime);

    if(algoBox != null) {
      algoBox.setSelectedItem(ctrl.getRoutingAlgorithm());
    }
  }

  @Override
  public void fastForwardChange(final boolean ffwMode, final int ffwMinutes) {
    if(ffwMode) {
      ffwButton.setIcon(FFW_STOP);
      ffwButton.setToolTipText("Stop");
    } else {
      ffwButton.setIcon(FFW_MODE);
      ffwButton.setToolTipText("Fast-Forward");
    }
    ffwSlider.setValue(ffwMinutes);
    ffwSlider.setToolTipText(ffwMinutes + "min");
  }

  @Override
  public void focusStation() {
    // already covered by select bus station
  }

}
