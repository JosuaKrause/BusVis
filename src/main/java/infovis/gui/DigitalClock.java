package infovis.gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Simple digital clock.
 * 
 * @author Marc Spicker
 */
public class DigitalClock extends JPanel {

  /** The hour text field. */
  private final JTextField hours;

  /** The minute text field. */
  private final JTextField minutes;

  /** Space label that displays the ":". */
  private final JLabel space;

  /** Constructor. */
  public DigitalClock() {
    final Dimension dim = new Dimension(50, 40);

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    hours = new JTextField("00");
    hours.setBackground(Color.BLACK);
    hours.setForeground(Color.WHITE);
    hours.setEditable(false);
    hours.setFocusable(false);
    hours.setPreferredSize(dim);
    hours.setMaximumSize(dim);
    hours.setHorizontalAlignment(SwingConstants.CENTER);
    hours.setFont(hours.getFont().deriveFont(25.0f));
    add(hours);

    space = new JLabel();
    space.setFont(space.getFont().deriveFont(25.0f));
    space.setText(":");
    add(space);

    minutes = new JTextField("00");
    minutes.setBackground(Color.BLACK);
    minutes.setForeground(Color.WHITE);
    minutes.setEditable(false);
    minutes.setFocusable(false);
    minutes.setPreferredSize(dim);
    minutes.setMaximumSize(dim);
    minutes.setHorizontalAlignment(SwingConstants.CENTER);
    minutes.setFont(minutes.getFont().deriveFont(25.0f));
    add(minutes);
  }

  /**
   * Sets the time of this clock.
   * 
   * @param hour The hour to display.
   * @param minute The minute to display.
   * @param blinking If the ":" between the hour and minute fields is not shown.
   */
  public void setTime(final int hour, final int minute, final boolean blinking) {
    final String h = String.format("%02d", hour);
    final String m = String.format("%02d", minute);
    hours.setText(h);
    minutes.setText(m);
    space.setText(blinking ? " " : ":");
  }

}
