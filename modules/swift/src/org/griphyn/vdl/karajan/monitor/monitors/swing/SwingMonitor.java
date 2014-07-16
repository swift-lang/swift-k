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
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.synth.SynthLookAndFeel;

import org.griphyn.vdl.karajan.monitor.StatefulItemClassSet;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.SystemStateListener;
import org.griphyn.vdl.karajan.monitor.common.DataSampler;
import org.griphyn.vdl.karajan.monitor.common.GlobalTimer;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.TaskItem;
import org.griphyn.vdl.karajan.monitor.monitors.AbstractMonitor;

public class SwingMonitor extends AbstractMonitor {
	private JFrame frame;
	private Timer timer;
	private Map<StatefulItemClass, ClassRenderer> tablemap;
	private GanttChart gantt;
	private JProgressBar progress;

	public SwingMonitor() {
	    setLookAndFeel();
		createFrame();
		tablemap = new HashMap<StatefulItemClass, ClassRenderer>();
	}

	private void setLookAndFeel() {
	    //setSynthLookAndFeel();
	    setNativeLookAndFeel();
    }

    private void setNativeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSynthLookAndFeel() {
        SynthLookAndFeel laf = new SynthLookAndFeel();
        try {
            laf.load(this.getClass().getClassLoader().getResourceAsStream("laf.xml"), SwingMonitor.class);
            UIManager.setLookAndFeel(laf);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createFrame() {
		frame = new JFrame();
		frame.setTitle("Swift System Monitor");
		frame.setSize(getSavedFrameSize());
		frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                saveFrameSize();
            } 
		});
	}

	private Dimension getSavedFrameSize() {
	    Preferences prefs = Preferences.userNodeForPackage(SwingMonitor.class);
	    return new Dimension(prefs.getInt("frameWidth", 800), prefs.getInt("frameHeight", 600));
    }
	
	private void saveFrameSize() {
	    Preferences prefs = Preferences.userNodeForPackage(SwingMonitor.class);
	    prefs.putInt("frameWidth", frame.getWidth());
	    prefs.putInt("frameHeight", frame.getHeight());
	}

    public void setState(SystemState state) {
		super.setState(state);
		DataSampler.install(state);
		createTabs(frame);
		frame.setVisible(true);
		
		GlobalTimer.getTimer().schedule(new TimerTask() {
            public void run() {
                update();
            }
        }, 1000, 1000);
	}

	private void createTabs(JFrame frame) {
	    frame.getContentPane().setLayout(new BorderLayout());
		JTabbedPane tabs = new JTabbedPane();
		frame.getContentPane().add(tabs, BorderLayout.CENTER);
		
		progress = new JProgressBar();
        progress.setString("");
        progress.setStringPainted(true);
        frame.getContentPane().add(progress, BorderLayout.SOUTH);
        progress.setString("Est. progress: 0%    Elapsed time: 00:00:00    Est. time left: N/A");
        
        Font orig = progress.getFont();
        progress.setFont(new Font(orig.getFamily(), Font.BOLD, orig.getSize() + 1));

		SummaryPanel summary = new SummaryPanel(getState());
		tabs.add("Summary", summary);
		
		GraphsPanel graphs = new GraphsPanel(getState());
		tabs.add("Graphs", graphs);
		
		StatefulItemClassSet<ApplicationItem> appSet =
		    getState().getItemClassSet(StatefulItemClass.APPLICATION);
		ClassRenderer applications = new ApplicationTable("Applications", appSet);
		tablemap.put(StatefulItemClass.APPLICATION, applications);
		tabs.add("Applications", (Component) applications);
		
		StatefulItemClassSet<TaskItem> taskSet = 
		    getState().getItemClassSet(StatefulItemClass.TASK);
		ClassRenderer tasks = new TasksRenderer("Tasks", taskSet);

		tablemap.put(StatefulItemClass.TASK, tasks);
		tabs.add("Tasks", (Component) tasks);
		
		gantt = new GanttChart(getState());
		tabs.add("Gantt Chart", gantt);
		
	}

	public void itemUpdated(SystemStateListener.UpdateType updateType, StatefulItem item) {
		ClassRenderer table = tablemap.get(item.getItemClass());
		if (table != null) {
			table.dataChanged();
		}
		if (gantt != null) {
		    gantt.itemUpdated(updateType, item);
		}
	}

    public void shutdown() {
    }

    @Override
    public void setParams(String params) {
    }
    
    private void update() {
        SystemState state = getState();
        int crt = state.getCompleted();
        int total = state.getTotal();
        progress.setMaximum(total);
        progress.setValue(crt);
        progress.setString(state.getGlobalProgressString());
    }
}
