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
 * Created on Feb 22, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.util.Iterator;

import javax.swing.event.TableModelEvent;

import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.griphyn.vdl.karajan.monitor.common.StatefulItemModel;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.TaskItem;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ActionListener;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Button;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Component;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Dialog;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Key;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Table;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Terminal;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.TextArea;
import org.griphyn.vdl.karajan.monitor.monitors.swing.FilteringTaskTable;
import org.griphyn.vdl.karajan.monitor.monitors.swing.SimpleTableClassRenderer.Model;

public class STable extends Table implements ActionListener {
    
    private Dialog d, term;
    private Button close, wterm;
    private Task termTask;
    private String termWorkerId;

    public boolean keyboardEvent(Key key) {
        if (key.isEnter()) {
            createDialog();
            return true;
        }
        else {
            return super.keyboardEvent(key);
        }
    }

    protected void createDialog() {
		d = new Dialog();
		d.setTitle("Details");
		d.setSize(getScreen().getWidth() * 2 / 3, getScreen().getHeight() * 1 / 3);
		d.center(getScreen());
		TextArea ta = new TextArea();
		ta.setText(getText());
		d.add(ta);
		ta.setSize(d.getWidth() - 2, d.getHeight() - 4);
		ta.setLocation(1, 1);
		boolean term = hasCoasterWorker();
		int btncount = term ? 1 : 0;
		close = new Button("&Close");
		close.setLocation(d.getWidth() / 2 - 7 - (btncount) * 9, d.getHeight() - 2);
		close.setSize(15, 1);
		close.addActionListener(this);
		d.add(close);
		
		if (term) {
		    wterm = new Button("Worker &Terminal");
		    wterm.setLocation(d.getWidth() / 2 - 7 + (btncount) * 9, d.getHeight() - 2);
		    wterm.setSize(15, 1);
		    wterm.addActionListener(this);
		    d.add(wterm);
		}
		
		d.display(getScreen());
		close.focus();
	}

    protected String getText() {
        StatefulItemModel model = (StatefulItemModel) getModel();
        return format(model.getItem(getSelectedRow()));
    }

    protected boolean hasCoasterWorker() {
        StatefulItemModel model = (StatefulItemModel) getModel();
        StatefulItem si = model.getItem(getSelectedRow());
        if (si instanceof TaskItem) {
            Task t = ((TaskItem) si).getTask();
            if (t == null) {
                return false;
            }
            else {
                Status s = t.getStatus();
                if (s == null) {
                    return false;
                }
                else {
                    if (s.getMessage() != null) {
                        String msg = s.getMessage();
                        int index = msg.indexOf("workerid=");
                        if (index == -1) {
                            return false;
                        }
                        else {
                            termTask = t;
                            termWorkerId = msg.substring(index + "workerid=".length());
                            return true;
                        }
                    }
                    else {
                        return false;
                    }
                }
            }
        }
        else {
            return false;
        }
    }

    protected StatefulItem getItem() {
        Model model = (Model) getModel();
        return model.getItem(getSelectedRow());
    }

    public void actionPerformed(Component source) {
        if (source == close) {
            d.close();
        }
        else if (source == wterm) {
            openWorkerTerminal();
        }
    }

    private void openWorkerTerminal() {
        term = new Dialog();
        term.setTitle("Worker terminal");
        term.setSize(getScreen().getWidth() * 3 / 4, getScreen().getHeight() * 3 / 4);
        Terminal t = new Terminal();
        t.setSize(term.getWidth() - 2, term.getHeight() - 2);
        t.setLocation(1, 1);
        t.append("Type exit to close terminal");
        t.append("");
        t.setInputHandler(new WorkerTerminalInputHandler(term, t, termTask, termWorkerId));
        t.setPrompt(termTask.getService(0).getServiceContact().getContact() + "/" + termWorkerId + "$ ");
        term.add(t);
        term.center(getScreen());
        term.display(getScreen());
    }

    protected static String format(Object o) {
        if (o instanceof TaskItem) {
            Task t = ((TaskItem) o).getTask();
            if (t == null) {
                return "?";
            }
            if (t.getType() == Task.FILE_TRANSFER) {
                FileTransferSpecification spec = (FileTransferSpecification) t
                    .getSpecification();
                StringBuffer sb = new StringBuffer();
                sb.append("Source:      ");
                sb.append(spec.getSource());
                sb.append('\n');
                sb.append("Destination: ");
                sb.append(spec.getDestination());
                sb.append('\n');
                return sb.toString();
            }
            else if (t.getType() == Task.JOB_SUBMISSION) {
                JobSpecification spec = (JobSpecification) t.getSpecification();
                StringBuffer sb = new StringBuffer();
                sb.append("Executable: ");
                sb.append(spec.getExecutable());
                sb.append("\nArguments: ");
                sb.append(spec.getArguments());
                sb.append("\nDirectory: ");
                sb.append(spec.getDirectory());
                sb.append("\nStatus: ");
                sb.append(t.getStatus().getStatusString());
                String msg = t.getStatus().getMessage();
                if (msg != null) {
                    sb.append(" ");
                    sb.append(t.getStatus().getMessage());
                }
                sb.append("\nAttributes: ");
                Iterator i = spec.getAttributeNames().iterator();
                while (i.hasNext()) {
                    String name = (String) i.next();
                    sb.append(name);
                    sb.append("=");
                    sb.append(spec.getAttribute(name));
                    if (i.hasNext()) {
                        sb.append(", ");
                    }
                }
                return sb.toString();
            }
            else {
                return t.toString();
            }
        }
        else if (o instanceof ApplicationItem) {
            ApplicationItem app = (ApplicationItem) o;
            StringBuffer sb = new StringBuffer();
            sb.append("Name: ");
            sb.append(app.getName());
            sb.append("\nArguments: ");
            sb.append(app.getArguments());
            sb.append("\nHost: ");
            sb.append(app.getHost());
            sb.append("\nStart time: ");
            sb.append(app.getStartTime());
            return sb.toString();
        }
        else {
            return String.valueOf(o);
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (getModel() instanceof FilteringTaskTable.Model) {
            FilteringTaskTable.Model model = (FilteringTaskTable.Model) getModel();
            model.invalidate();
        }
        super.tableChanged(e);
    }
}
