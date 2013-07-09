/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Feb 13, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.griphyn.vdl.karajan.monitor.SystemStateListener;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.TaskItem;

public class GanttChart extends JPanel implements SystemStateListener, ActionListener,
		ChangeListener {
	private JTable table, header;
	private HeaderModel hmodel;
	private ChartModel cmodel;
	private List<Job> jobs;
	private Map<String, Job> jobmap;
	private JScrollPane csp, hsp;
	private JSpinner scalesp;
	private long firstEvent;
	private Timer timer;
	private double scale;
	private JLabel ctime;

	public GanttChart() {
		scale = 1.0 / SCALE;
		jobs = new ArrayList<Job>();
		jobmap = new HashMap<String, Job>();

		header = new JTable() {
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				return new Dimension(50, d.height);
			}
		};
		header.setModel(hmodel = new HeaderModel());
		header.setShowHorizontalLines(true);
		header.setPreferredScrollableViewportSize(new Dimension(100, 10));
		header.setDefaultRenderer(Job.class, new JobNameRenderer());

		table = new JTable();
		table.setModel(cmodel = new ChartModel());
		table.setShowHorizontalLines(true);
		table.setDefaultRenderer(Job.class, new JobRenderer());
		JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		jp.add(table, BorderLayout.CENTER);

		csp = new JScrollPane(jp);
		csp.setColumnHeaderView(new Tickmarks());
		csp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		csp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		csp.setRowHeaderView(header);

		setLayout(new BorderLayout());
		add(csp, BorderLayout.CENTER);
		add(createTools(), BorderLayout.NORTH);
		timer = new Timer(1000, this);
		timer.start();
	}

	private JComponent createTools() {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		JLabel l;
		p.add(l = new JLabel("Scale: "), BorderLayout.CENTER);
		l.setAlignmentX(1.0f);
		l.setHorizontalAlignment(SwingConstants.RIGHT);
		p.add(scalesp = new JSpinner(new SpinnerNumberModel(1, 0.01, 100, 0.01)), BorderLayout.EAST);
		p.add(ctime = new JLabel("Current time: 0s"));
		scalesp.addChangeListener(this);
		return p;
	}

	public void stateChanged(ChangeEvent e) {
		scale = ((Number) scalesp.getValue()).doubleValue() / SCALE;
		repaint();
	}

	public void itemUpdated(SystemStateListener.UpdateType updateType, StatefulItem item) {
		if (firstEvent == 0) {
			firstEvent = System.currentTimeMillis();
		}
		if (item.getItemClass().equals(StatefulItemClass.APPLICATION)) {
			ApplicationItem ai = (ApplicationItem) item;
			if (updateType == SystemStateListener.UpdateType.ITEM_ADDED) {
				addJob(ai);
			}
			else if (updateType == SystemStateListener.UpdateType.ITEM_REMOVED) {
				Job j = jobmap.get(item.getID());
				j.end();
			}
		}
		else if (item.getItemClass().equals(StatefulItemClass.TASK)) {
			TaskItem ti = (TaskItem) item;
			if (ti.getTask() != null && item.getParent() != null) {
				Job job = jobmap.get(item.getParent().getID());
				if (job == null) {
					return;
				}
				Task task = ti.getTask();
				if (task.getType() == Task.FILE_OPERATION) {
					if (updateType == SystemStateListener.UpdateType.ITEM_ADDED) {
						job.addPretask();
					}
					else if (updateType == SystemStateListener.UpdateType.ITEM_REMOVED) {
						job.removePretask();
					}
				}
				else if (task.getType() == Task.JOB_SUBMISSION) {
					if (updateType == SystemStateListener.UpdateType.ITEM_UPDATED) {
						job.setJobStatus(ti.getStatus());
						hmodel.fireTableDataChanged();
					}
				}
			}
		}
		repaint();
	}

	protected void addJob(ApplicationItem ai) {
		Job j = new Job(ai);
		j.start();
		jobmap.put(ai.getID(), j);
		jobs.add(j);
		hmodel.fireTableStructureChanged();
		cmodel.fireTableStructureChanged();
	}

	public void actionPerformed(ActionEvent e) {
		if (firstEvent != 0) {
			ctime.setText("Current time: " + (System.currentTimeMillis() - firstEvent) / 1000 + "s");
		}
		cmodel.fireTableDataChanged();
	}

	private class HeaderModel extends AbstractTableModel {

		public HeaderModel() {

		}

		public Class<?> getColumnClass(int columnIndex) {
			return Job.class;
		}

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return jobs.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return jobs.get(rowIndex);
		}

	}

	private class ChartModel extends AbstractTableModel {

		public ChartModel() {

		}

		public Class<?> getColumnClass(int columnIndex) {
			return Job.class;
		}

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return jobs.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return jobs.get(rowIndex);
		}
	}

	private class Tickmarks extends Component {
		public void paint(Graphics g) {
			super.paint(g);
			Rectangle r = g.getClipBounds();
			double start = toReal(r.x);
			double end = toReal(r.x + r.width);
			double sticks = 10 * Math.pow(10, (int) (Math.log(scale) / Math.log(10))) / scale;
			double x = r.x;
			int count = 0;
			while (x < r.x + r.width) {
				int ix = (int)x;
				g.drawLine(ix, 0, ix, 3);
				if (count % 5 == 0) {
					g.drawLine(ix, 0, ix, 10);
					g.drawString(formatTime(toReal(ix)), ix + 2, 14);
				}
				x += sticks;
				count++;
			}
		}

		public Dimension getPreferredSize() {
			return new Dimension(1, 15);
		}

		private double toReal(int screen) {
			return screen / scale;
		}
		
		private String formatTime(double ms) {
			return TF.format(ms / 1000) + "s";
		}
	}
	
	private static final NumberFormat TF;
	
	static {
		TF = new DecimalFormat();
		TF.setMaximumFractionDigits(2);
		TF.setMinimumFractionDigits(2);
	}

	private class Job {
		private ApplicationItem ai;
		private List<Event> events;
		private int pretasks;

		public Job(ApplicationItem ai) {
			events = new ArrayList<Event>();
			this.ai = ai;
		}

		public void setJobStatus(int statusCode) {
			add(new JobEvent(statusCode));
		}

		public void addPretask() {
			add(new PretaskEvent(++pretasks));
		}

		public void removePretask() {
			add(new PretaskEvent(--pretasks));
		}

		public void end() {
			add(new EndEvent());
		}

		public void start() {
			add(new StartEvent());
		}

		private void add(Event event) {
			synchronized (events) {
				events.add(event);
			}
		}

		public String getName() {
			return ai.getName();
		}
	}

	private abstract class Event {
		public final int type;
		public int time;

		public Event(int type) {
			this.time = (int) (System.currentTimeMillis() - firstEvent);
			this.type = type;
		}
	}

	private class StartEvent extends Event {
		public static final int TYPE = 1;

		public StartEvent() {
			super(TYPE);
		}
	}

	private class EndEvent extends Event {
		public static final int TYPE = 2;

		public EndEvent() {
			super(TYPE);
		}
	}

	private class PretaskEvent extends Event {
		public static final int TYPE = 3;

		public int count;

		public PretaskEvent(int count) {
			super(TYPE);
			this.count = count;
		}
	}

	private class JobEvent extends Event {
		public static final int TYPE = 4;

		public int status;

		public JobEvent(int status) {
			super(TYPE);
			this.status = status;
		}
	}

	private class JobNameRenderer implements TableCellRenderer {
		private JLabel label;

		public JobNameRenderer() {
			label = new JLabel();
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			label.setText(((Job) value).getName());
			return label;
		}

	}

	private class JobRenderer implements TableCellRenderer {
		private JobComponent jc;

		public JobRenderer() {
			jc = new JobComponent();
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			jc.setJob((Job) value);
			return jc;
		}
	}

	public static final int SCALE = 500;
	public static final Color QUEUED = new Color(255, 230, 0, 128);
	public static final Color RUNNING = new Color(0, 255, 0, 128);

	private class JobComponent extends Component {
		private Job job;

		public JobComponent() {

		}

		public Job getJob() {
			return job;
		}

		public void setJob(Job job) {
			this.job = job;
		}

		public void paint(Graphics g) {
			List<Event> events;
			synchronized (job.events) {
				events = new ArrayList<Event>(job.events);
			}
			if (events.size() == 0) {
				return;
			}
			int ox = 0, ex = 0;
			boolean endcap = false;
			Iterator<Event> i = events.iterator();
			while (i.hasNext()) {
				Event e = i.next();
				if (e.type == StartEvent.TYPE) {
					ox = e.time;
				}
				else if (e.type == EndEvent.TYPE) {
					ex = e.time;
					endcap = true;
				}
			}

			if (!endcap) {
				ex = (int) (System.currentTimeMillis() - firstEvent);
			}

			g.setColor(Color.BLACK);
			ox = (int) (ox * scale);
			ex = (int) (ex * scale);
			g.drawLine(ox, 1, ox, 11);
			g.drawLine(ox + 1, 1, ox + 1, 11);
			if (endcap) {
				g.drawLine(ex, 1, ex, 11);
				g.drawLine(ex - 1, 1, ex - 1, 11);
			}
			g.drawLine(ox, 5, ex, 5);
			g.drawLine(ox, 6, ex, 6);

			Color crt = null;
			int lx = ox;
			i = events.iterator();
			while (i.hasNext()) {
				Event e = i.next();
				int x = (int) (e.time * scale);
				// System.err.println(crt+", "+lx+", "+x);
				if (crt != null) {
					g.setColor(crt);
					g.fillRect(lx, 2, x - lx, 9);
					lx = x;
				}
				if (e.type == PretaskEvent.TYPE) {
					int count = ((PretaskEvent) e).count;
					crt = new Color(64, 64, 255, Math.max(count, 10) * 15);
				}
				else if (e.type == JobEvent.TYPE) {
					int status = ((JobEvent) e).status;
					switch (status) {
						case Status.SUBMITTED: {
							crt = QUEUED;
							break;
						}
						case Status.ACTIVE: {
							crt = RUNNING;
							break;
						}
						case Status.FAILED:
						case Status.COMPLETED: {
							crt = null;
							break;
						}
					}
				}
				else {
					continue;
				}
				lx = x;
			}
			if (crt != null) {
				g.setColor(crt);
				g.fillRect(lx, 2, ex - lx, 9);
			}
		}
	}
}
