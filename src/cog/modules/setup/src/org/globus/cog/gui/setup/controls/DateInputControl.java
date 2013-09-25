
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.controls;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.Timer;

import org.globus.cog.gui.setup.util.BusyFeedback;
import org.globus.cog.gui.setup.util.Callback;
import org.globus.cog.gui.setup.util.NTPThreadedClient;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.SimpleGridLayout;

/**
 *  A simple control to dispay the date/time in real time
 */
public class DateInputControl extends GridContainer implements ActionListener, Callback {
	private JTextField date;
	private JButton probe;
	private Timer timer;
	private DateFormat sysDateFormat;
	private Date sysDate;
	private NTPThreadedClient NTP, autoNTP;
	private BusyFeedback BF;
	private int counter;
	private String offset;
	private long iOffset;

	public DateInputControl() {
		super(1, 2);

		counter = 0;
		offset = "offset: unknown";
		iOffset = 0;
		BF = null;

		NTP = null;

		sysDateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
		sysDate = new Date();

		timer = new Timer(100, this);

		date = new JTextField("");
		date.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 24));
		date.setEditable(false);

		probe = new JButton("Retest");
		probe.setPreferredSize(new Dimension(100, 24));
		probe.setAlignmentX(RIGHT_ALIGNMENT);
		probe.addActionListener(this);

		add(date);
		add(probe);

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == probe) {
			BusyFeedback.setMessage("Contacting time server. Please wait.");
			BusyFeedback.setCallback(this);
			BusyFeedback.show();
			synchronized (probe) {
				NTP = new NTPThreadedClient("time.nist.gov");
				NTP.setCallback(this);
				NTP.start();
				NTP.update();
			}
		}
		if (e.getSource() == timer) {
			counter--;
			if (counter <= 0) {
				counter = 100;
				autoNTP.update();
			}
		}
		sysDate.setTime(System.currentTimeMillis());
		date.setText(sysDateFormat.format(sysDate) + ", " + offset);
	}

	public boolean offsetBigger(long offset) {
		return (Math.abs(this.iOffset) > offset);
	}

	public void callback(Object source, Object data) {
		if (source == NTP) {
			synchronized (probe) {
				NTP.removeCallback();
				BusyFeedback.hide();
			}
			if (data == null) {
				iOffset = NTP.getVariation();
				offset = "offset: " + String.valueOf(iOffset) + "ms";
			}
			else {
				JOptionPane.showMessageDialog(null, data, "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
		if (source == autoNTP) {
			if (data == null) {
				iOffset = autoNTP.getVariation();
				offset = "offset: " + String.valueOf(iOffset) + "ms";
			}
		}
		if (source instanceof BusyFeedback) {
			if (NTP != null) {
				NTP.removeCallback();
			}
			BusyFeedback.removeCallback();
			BusyFeedback.hide();
		}
	}

	public void start() {
		autoNTP = new NTPThreadedClient("time.nist.gov");
		autoNTP.setCallback(this);
		autoNTP.start();
		timer.start();
	}

	public void stop() {
		autoNTP.quit();
		autoNTP = null;
		timer.stop();
	}
}
