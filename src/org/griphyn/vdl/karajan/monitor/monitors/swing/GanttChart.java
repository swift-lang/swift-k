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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.SystemStateListener;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.TaskItem;

public class GanttChart extends JPanel implements SystemStateListener, ActionListener,
		ChangeListener {
    
    public static final double INITIAL_SCALE = 1;
    public static final Color QUEUED = new Color(255, 230, 0, 200);
    public static final Color RUNNING = new Color(0, 255, 0, 200);
    public static final Color LINE_COLOR = UIManager.getColor("Label.foreground");

    
	private JTable table, header;
	private HeaderModel hmodel;
	private ChartModel cmodel;
	private List<Job> jobs;
	private Map<String, Job> jobmap;
	private JScrollPane csp;
	private JScrollBar hsb;
	private JSpinner scalesp;
	private long firstEvent;
	private double scale;
	private int offset, maxX;
	private JLabel ctime;
	private boolean scrollVerticallyOnNextUpdate;
	private SystemState state;

	public GanttChart(SystemState state) {
	    this.state = state;
		scale = INITIAL_SCALE;
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
		table.setDoubleBuffered(true);
		table.setModel(cmodel = new ChartModel());
		table.setShowHorizontalLines(true);
		table.setDefaultRenderer(Job.class, new JobRenderer());
		JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		jp.add(table, BorderLayout.CENTER);

		csp = new JScrollPane(jp);
		csp.setColumnHeaderView(new Tickmarks());
		csp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		csp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		csp.setRowHeaderView(header);
		csp.getVerticalScrollBar().getModel().addChangeListener(this);
		
		hsb = new JScrollBar(JScrollBar.HORIZONTAL);
		hsb.setVisible(true);
		hsb.getModel().addChangeListener(this);

		setLayout(new BorderLayout());
		add(csp, BorderLayout.CENTER);
		add(createTools(), BorderLayout.NORTH);
		add(hsb, BorderLayout.SOUTH);
		
		state.schedule(new TimerTask() {
            @Override
            public void run() {
                GanttChart.this.actionPerformed(null);
            }
		}, 1000, 1000);
	}
	
	private JComponent createTools() {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		JLabel l;
		p.add(l = new JLabel("Scale: "), BorderLayout.CENTER);
		l.setAlignmentX(1.0f);
		l.setHorizontalAlignment(SwingConstants.RIGHT);
		p.add(scalesp = new JSpinner(new SpinnerNumberModel(1, 0.01, 100, 0.05)), BorderLayout.EAST);
		p.add(ctime = new JLabel("Current time: 0s"));
		scalesp.addChangeListener(this);
		return p;
	}

	public void stateChanged(ChangeEvent e) {
	    if (e.getSource() == scalesp) {
	        scale = ((Number) scalesp.getValue()).doubleValue() * INITIAL_SCALE;
	        repaint();
	    }
	    else if (e.getSource() == hsb.getModel()) {
	        BoundedRangeModel m = hsb.getModel();
	        if (offset != m.getValue()) {
    	        offset = m.getValue();
    	        repaint();
	        }
	    }
	    else if (e.getSource() == csp.getVerticalScrollBar().getModel()) {
	        if (scrollVerticallyOnNextUpdate) {
	            scrollVerticallyOnNextUpdate = false;
	            csp.getVerticalScrollBar().getModel().setValue(Integer.MAX_VALUE);
	        }
	    }
	}

	public void itemUpdated(SystemStateListener.UpdateType updateType, StatefulItem item) {
		if (firstEvent == 0) {
			firstEvent = state.getCurrentTime();
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
						repaint();
					}
				}
			}
		}
		repaint();
	}
	
	public void updateMaxX(int maxX) {
	    int extent = table.getWidth();
        int empty = extent * 3 / 4;
        maxX += empty;
	    if (this.maxX < maxX) {
	        int oldMaxX = this.maxX;
	        this.maxX = maxX;
	        
	        int visible = (maxX - empty) - offset;
	        int oldVisible = (oldMaxX - empty) - offset;
	        
	        if (visible > empty && oldVisible < empty) {
	            int newOffset = maxX - extent;
	            if (newOffset > offset) {
	                offset = newOffset;
	                hsb.getModel().setRangeProperties(offset, table.getWidth(), 0, maxX, false);
	                repaint();
	            }
	        }
	        else {
	            hsb.getModel().setMaximum(maxX);
	        }
	    }
	    if (hsb.getModel().getExtent() != table.getWidth()) {
	        hsb.getModel().setExtent(table.getWidth());
	    }
	}

	protected void addJob(ApplicationItem ai) {
		Job j = new Job(ai);
		j.start();
		jobmap.put(ai.getID(), j);
		jobs.add(j);
		BoundedRangeModel m = csp.getVerticalScrollBar().getModel();
		if (m.getValue() + m.getExtent() == m.getMaximum()) {
		    scrollVerticallyOnNextUpdate = true;
		}
		hmodel.fireTableRowsInserted(jobs.size(), jobs.size());
        cmodel.fireTableRowsInserted(jobs.size(), jobs.size());
	}

	public void actionPerformed(ActionEvent e) {
		if (firstEvent != 0) {
			ctime.setText("Current time: " + (state.getCurrentTime() - firstEvent) / 1000 + "s");
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
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			int width = getWidth();
			int X = 75;
			// - space major ticks at least X pixels
			// - scale = width / time
			// - timeForXPixels = X / scale
			// - the major tick size must the smallest power of 10
			//   larger than timeForXPixels
			
			double timeFor100Pixels = 75 / scale;
			int majorTickMagnitude = (int) Math.ceil((Math.log(timeFor100Pixels) / Math.log(10)));
			double majorTickSize = Math.pow(10, majorTickMagnitude);
			double minorTickSize = majorTickSize / 10;
			
			// the first pixel we could draw
			int left = - (offset % width);
			int right = width;
			
			double minorLeft = round(minorTickSize, pixelToTime(left));
			double tright = pixelToTime(right);
			
			int minorCount = 1 + (int) ((tright - minorLeft) / minorTickSize);
			
			// draw minor ticks
			for (int i = 0; i < minorCount; i++) {
			    int ix = timeToPixel(minorLeft + i * minorTickSize);
			    g.drawLine(ix, 0, ix, 3);
			}
			
			double majorLeft = round(majorTickSize, pixelToTime(left));
            
            int majorCount = 1 + (int) ((tright - majorLeft) / majorTickSize);
            
            // draw major ticks
            for (int i = 0; i < majorCount; i++) {
                double time = majorLeft + i * majorTickSize;
                int ix = timeToPixel(time);
                g.drawLine(ix, 0, ix, 10);
                g.drawString(formatTime(time), ix + 2, 14);
            }
		}

		private double round(double order, double value) {
            return order * (int) (value / order);
        }

        private double pixelToTime(int x) {
            int absoluteX = x + offset;
            return absoluteX / scale;
        }
        
        private int timeToPixel(double time) {
            return (int) (time * scale) - offset;
        }

        public Dimension getPreferredSize() {
			return new Dimension(1, 15);
		}

		private double toReal(int screen) {
			return screen / scale;
		}
		
		private double toReal(double screen) {
            return screen / scale;
        }
		
		private String formatTime(double s) {
			return TF.format(s) + "s";
		}
	}
	
	private static final NumberFormat TF;
	
	static {
		TF = new DecimalFormat();
		TF.setMaximumFractionDigits(2);
		TF.setMinimumFractionDigits(0);
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
			this.time = (int) (state.getCurrentTime() - firstEvent);
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
		    // we work in milliseconds here and the scale
		    // is in pixels per second
		    double scale = GanttChart.this.scale / 1000;
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
				ex = (int) (state.getCurrentTime() - firstEvent);
			}

			g.setColor(LINE_COLOR);
			ox = (int) (ox * scale) - offset;
			ex = (int) (ex * scale) - offset;
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
				int x = (int) (e.time * scale) - offset;
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
			updateMaxX(ex + offset);
		}
	}
}
