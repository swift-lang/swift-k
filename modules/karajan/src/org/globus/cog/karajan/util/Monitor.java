//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 23, 2005
 */
package org.globus.cog.karajan.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.globus.cog.karajan.workflow.events.EventBus;

public class Monitor extends Thread {
	private JFrame win;
	private List displays;
	public static final Font FONT = Font.decode("Arial-10");
	public static final Color[] colors = new Color[] { Color.LIGHT_GRAY, Color.CYAN, Color.YELLOW,
			Color.MAGENTA };

	private int cindex;

	public Monitor() {
		displays = new LinkedList();
		win = new JFrame();
		win.setTitle("Resource monitor");
		win.getContentPane().setLayout(new GridLayout(0, 1));
		win.getContentPane().add(makeDisplay(new HeapAlloc()));
		win.getContentPane().add(makeDisplay(new HeapSize()));
		win.getContentPane().add(makeDisplay(new ThreadCount()));
		win.getContentPane().add(makeDisplay(new WorkerCount()));
		win.setSize(80, 64 * displays.size());
	}

	private Component makeDisplay(Source source) {
		Display d = new Display(source, colors[cindex]);
		cindex = (cindex + 1) % colors.length;
		displays.add(d);
		return d;
	}

	public void run() {
		win.show();
		while (!win.isVisible()) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
			}
		}
		while (win.isVisible()) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
			}
			Iterator i = displays.iterator();
			while (i.hasNext()) {
				Display d = (Display) i.next();
				d.update();
			}
		}
	}

	private class Display extends JPanel {
		private static final long serialVersionUID = 8829047962526635171L;
		
		private final Source source;
		private final Color color;
		private final Chart chart;
		private final JLabel v, n;
		private final String unit;

		public Display(Source source, Color color) {
			this.source = source;
			this.color = color;
			this.unit = source.getUnit();
			this.setLayout(new BorderLayout());
			chart = new Chart(color);
			chart.setSize(40, 30);
			add(chart, BorderLayout.CENTER);
			v = new JLabel();
			v.setFont(FONT);
			add(v, BorderLayout.SOUTH);
			n = new JLabel();
			n.setText(source.getName());
			n.setFont(FONT);
			add(n, BorderLayout.NORTH);
		}

		public void update() {
			int y = source.sample();
			v.setText(y + " " + unit);
			chart.addData(y);
		}
	}

	private class Chart extends Component {
		private static final long serialVersionUID = 8649788214104626075L;
		
		private final LinkedList data;
		private int maxy;
		private final Color color;
		private double scale = 1.0;

		public Chart(Color color) {
			this.color = color;
			data = new LinkedList();
			this.setSize(40, 30);
		}

		public void addData(int v) {
			data.add(new Integer(v));
			while (data.size() > getWidth()) {
				data.removeFirst();
			}
			int max = 1;
			Iterator i = data.iterator();
			while (i.hasNext()) {
				Number n = (Number) i.next();
				if (n.intValue() > max) {
					max = n.intValue();
				}
			}
			double ratio = (double) max / getHeight() / scale;
			if (ratio > 0.95) {
				scale = scale * 1.25;
			}
			else if (ratio < 0.5) {
				scale = scale * 0.75;
			}
			repaint();
		}

		public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			int x = 0;
			int h = getHeight();
			Iterator i = data.iterator();
			while (i.hasNext()) {
				Number n = (Number) i.next();
				int y = (int) (n.intValue() / scale);
				g2d.setColor(Color.BLACK);
				g2d.drawLine(x, h - y, x, 0);
				g2d.setColor(color);
				g2d.drawLine(x, h, x, h - y);
				x++;
			}
			g2d.setColor(Color.BLACK);
			g2d.fillRect(x + 1, 0, getWidth() - x - 1, h);
			g2d.drawRect(0, 0, getWidth() - 1, h - 1);
		}
	}

	private interface Source {
		int sample();

		String getUnit();

		String getName();
	}

	private class HeapAlloc implements Source {
		public int sample() {
			return (int) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
		}

		public String getUnit() {
			return "MB";
		}

		public String getName() {
			return "Allocated memory";
		}
	}

	private class HeapSize implements Source {
		public int sample() {
			return (int) (Runtime.getRuntime().totalMemory()) / 1024 / 1024;
		}

		public String getUnit() {
			return "MB";
		}

		public String getName() {
			return "Heap size";
		}
	}

	private class ThreadCount implements Source {
		public int sample() {
			return Thread.activeCount();
		}

		public String getUnit() {
			return "threads";
		}

		public String getName() {
			return "Total threads";
		}
	}

	private class WorkerCount implements Source {
		public int sample() {
			return EventBus.DEFAULT_WORKER_COUNT;
		}

		public String getUnit() {
			return "workers";
		}

		public String getName() {
			return "Total workers";
		}
	}
}
