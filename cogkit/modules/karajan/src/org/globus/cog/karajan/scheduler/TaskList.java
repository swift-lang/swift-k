/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Aug 7, 2003
 */
package org.globus.cog.karajan.scheduler;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public class TaskList extends JFrame implements ActionListener{
	private static final long serialVersionUID = -6316765268251834858L;
	
	private TaskHandler handler;
	private List handlers;
	private JTable table;
	private AbstractTaskModel model;
	private JProgressBar mem, queue;
	private int maxq = 1;
	private JButton kill;

	public TaskList() {
		super("Task List");
		getContentPane().setLayout(new BorderLayout());
		mem = new JProgressBar();
		mem.setStringPainted(true);
		queue = new JProgressBar();
		queue.setStringPainted(true);
		mem.setMinimum(0);
		queue.setMinimum(0);
		JPanel p = new JPanel(new GridLayout(3,1));
		JPanel q = new JPanel(new FlowLayout());
		p.add(q);
		kill = new JButton("Kill task");
		q.add(kill);
		kill.addActionListener(this);
		p.add(mem);
		p.add(queue);
		getContentPane().add(p, BorderLayout.SOUTH);
	}
	
	public TaskList(List handlers) {
		this();
		this.handlers = handlers;
		model = new MultipleHandlerTaskModel(handlers);
		table = new JTable(model);
		table.setAutoCreateColumnsFromModel(true);
		table.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
		getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		update(0,0);
		table.setSize(640,200);
		table.setPreferredSize(new Dimension(640, 200));
		pack();
	}
	
	public TaskList(TaskHandler handler) {
		this();
		this.handler = handler;
		model = new SingleHandlerTaskModel(handler);
		table = new JTable(model);
		table.setAutoCreateColumnsFromModel(true);
		table.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
		getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		update(0,0);
		table.setSize(640,200);
		table.setPreferredSize(new Dimension(640, 200));
		pack();
	}
	
	public void setTaskHandler(TaskHandler th){
		handler = th;
		if (model instanceof SingleHandlerTaskModel) {
			((SingleHandlerTaskModel) model).setTaskHandler(th);
		}
	}
	
	public final void update(int queued, int running){
		model.update();
		long crt = Runtime.getRuntime().totalMemory()/ (1024*1024);
		long free = Runtime.getRuntime().freeMemory() / (1024*1024);
		long max = Runtime.getRuntime().maxMemory() / (1024*1024);
		mem.setMaximum((int)crt);
		mem.setValue((int)(crt-free));
		mem.setString("Heap: "+crt+"MB, Free: "+free+"MB, Max: "+max+"MB");
		if (queued+running>maxq){
		    maxq = queued+running;
		}
		queue.setMaximum(maxq);
		queue.setValue(queued);
		queue.setString("Queued: "+queued+", Running: "+running);
	}

	public void actionPerformed(ActionEvent e) {
		Task t = model.getTaskAtRow(table.getSelectedRow());
		if ((handler != null) && (t != null)){
			try {
				handler.cancel(t);
			}
			catch (InvalidSecurityContextException e1) {
				e1.printStackTrace();
			}
			catch (TaskSubmissionException e1) {
				e1.printStackTrace();
			}		
			catch (Exception e1){
				e1.printStackTrace();
			}
		}
	}

}
