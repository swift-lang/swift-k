/*
 * Created on Feb 22, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.util.Iterator;

import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.JobSpecification;
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
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.TextArea;
import org.griphyn.vdl.karajan.monitor.monitors.swing.SimpleTableClassRenderer.Model;

public class STable extends Table implements ActionListener {
	private Dialog d;
	private Button close;
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
		d.setSize(getScreen().getWidth() * 2 / 3, getScreen().getHeight() * 1 / 3);
		d.center(getScreen());
		TextArea ta = new TextArea();
		ta.setText(getText());
		d.add(ta);
		ta.setSize(d.getWidth() - 2, d.getHeight() - 4);
		ta.setLocation(1, 1);
		close = new Button("&Close");
		close.setLocation(d.getWidth() / 2 - 5, d.getHeight() - 2);
		close.setSize(9, 1);
		close.addActionListener(this);
		d.add(close);
		d.display(getScreen());
		close.focus();
	}

	protected String getText() {
		StatefulItemModel model = (StatefulItemModel) getModel();
		return format(model.getItem(getSelectedRow()));
	}
	
	protected StatefulItem getItem() {
		Model model = (Model) getModel();
		return model.getItem(getSelectedRow());
	}

	public void actionPerformed(Component source) {
		if (source == close) {
			d.close();
		}
	}
	
	protected static String format(Object o) {
		if (o instanceof TaskItem) {
			Task t = ((TaskItem) o).getTask();
			if (t == null) {
				return "?";
			}
			if (t.getType() == Task.FILE_TRANSFER) {
				FileTransferSpecification spec = (FileTransferSpecification) t.getSpecification();
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
}
