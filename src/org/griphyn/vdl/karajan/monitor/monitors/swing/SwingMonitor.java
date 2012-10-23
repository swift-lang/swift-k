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

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.griphyn.vdl.karajan.monitor.StatefulItemClassSet;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.monitors.AbstractMonitor;

public class SwingMonitor extends AbstractMonitor {
	private JFrame frame;
	private Timer timer;
	private Map tablemap;
	private GanttChart gantt;

	public SwingMonitor() {
		createFrame();
		tablemap = new HashMap();
	}

	private void createFrame() {
		frame = new JFrame();
		frame.setTitle("Swift System Monitor");
		frame.setSize(800, 600);
		frame.setVisible(true);
	}

	public void setState(SystemState state) {
		super.setState(state);
		createTabs(frame);
	}

	private void createTabs(JFrame frame) {
		JTabbedPane tabs = new JTabbedPane();
		frame.getContentPane().add(tabs);

		ClassRenderer workflows = new SimpleTableClassRenderer("Workflows",
				getState().getItemClassSet(StatefulItemClass.WORKFLOW));
		tablemap.put(StatefulItemClass.WORKFLOW, workflows);
		tabs.add("Workflows", (Component) workflows);
		
		StatefulItemClassSet<ApplicationItem> appSet =
		    getState().getItemClassSet(StatefulItemClass.APPLICATION);
		ClassRenderer applications = new ApplicationTable("Applications", appSet);
		tablemap.put(StatefulItemClass.APPLICATION, applications);
		tabs.add("Applications", (Component) applications);
		
		ClassRenderer tasks = new TasksRenderer("Tasks",
				getState().getItemClassSet(StatefulItemClass.TASK));
		tablemap.put(StatefulItemClass.TASK, tasks);
		tabs.add("Tasks", (Component) tasks);
		
		gantt = new GanttChart();
		tabs.add("Gantt Chart", gantt);
	}

	public void itemUpdated(int updateType, StatefulItem item) {
		ClassRenderer table = (ClassRenderer) tablemap.get(item.getItemClass());
		if (table != null) {
			table.dataChanged();
		}
		gantt.itemUpdated(updateType, item);
	}

    public void shutdown() {
    }
}
