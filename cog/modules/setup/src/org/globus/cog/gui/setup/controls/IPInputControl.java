
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.controls;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.globus.cog.gui.setup.util.BusyFeedback;
import org.globus.cog.gui.setup.util.Callback;
import org.globus.cog.gui.setup.util.HSpacer;
import org.globus.cog.gui.setup.util.IPProber;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.SimpleGridLayout;

/**
 *  Class to handle the input of the IP address of the LOCAL MACHINE
 */
public class IPInputControl extends GridContainer implements ActionListener, Callback {
	private JTextField IP;
	private JButton probe;
	private String probedIP;
	private IPProber IPP;

	public IPInputControl(String InitialIP) {
		super(1, 3);

		IP = new JTextField(InitialIP);
		IP.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 24));

		probe = new JButton("Probe");
		probe.addActionListener(this);
		probe.setPreferredSize(new Dimension(70, 24));

		add(new HSpacer(10));
		add(IP);
		add(probe);

		probeIP();

		IPP = null;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == probe) {
			BusyFeedback.setMessage("Probing IP. Please wait.");
			BusyFeedback.setCallback(this);
			BusyFeedback.show();
			IPP = new IPProber();
			IPP.setCallback(this);
			IPP.start();
		}
	}

	public void probeIP() {
		probedIP = null;
		try {
			probedIP = InetAddress.getLocalHost().getHostAddress();
		}
		catch (Exception e) {
		}
	}

	public String getProbedIP() {
		return probedIP;
	}

	public String getIP() {
		return IP.getText();
	}

	public void setIP(String IP) {
		if (IP == null) {
			probeIP();
			this.IP.setText(probedIP);
		}
		else {
			this.IP.setText(IP);
		}
	}

	public void callback(Object source, Object data) {
		if (source instanceof IPProber) {
			if (data != null) {
				probedIP = (String) data;
				IP.setText(probedIP);
			}
			BusyFeedback.hide();
		}
		if (source instanceof BusyFeedback) {
			if (IPP != null) {
				IPP.removeCallback();
			}
			BusyFeedback.removeCallback();
			BusyFeedback.hide();
		}
	}
}
