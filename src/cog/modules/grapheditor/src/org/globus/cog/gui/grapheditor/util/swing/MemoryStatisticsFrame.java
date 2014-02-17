
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 10, 2004
 *
 */
package org.globus.cog.gui.grapheditor.util.swing;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

public class MemoryStatisticsFrame extends JFrame implements ActionListener, WindowListener {
	private Timer timer;

	private JLabel lTotal;

	private JLabel lFree;

	private JProgressBar pFree;

	public MemoryStatisticsFrame() {
		timer = new Timer(1000, this);
		timer.start();
		this.setTitle("Memory monitor");
		JComponent c = (JComponent) getContentPane();
		c.setBorder(BorderFactory.createTitledBorder("Memory Statistics"));
		c.setLayout(new GridLayout(0, 3));
		c.add(new JLabel("Total Memory: "));
		c.add(lTotal = new JLabel("-"));
		c.add(new JLabel());
		c.add(new JLabel("Free Memory: "));
		c.add(lFree = new JLabel("-"));
		c.add(pFree = new JProgressBar());
		setSize(300, 80);
		addWindowListener(this);
		updateStats();
	}

	public void actionPerformed(ActionEvent e) {
		updateStats();
	}

	public void updateStats() {
		long total = Runtime.getRuntime().maxMemory();
		long free = Runtime.getRuntime().freeMemory() + total - Runtime.getRuntime().totalMemory();
		total /= 1024;
		free /= 1024;
		lTotal.setText(total + " KB");
		long pfree = (free * 100 / total);
		lFree.setText(free + " KB (" + pfree + "%)");
		pFree.setMaximum(100);
		pFree.setValue((int) pfree);
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		timer.stop();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

}
