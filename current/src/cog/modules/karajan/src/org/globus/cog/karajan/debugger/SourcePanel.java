// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 18, 2005
 */
package org.globus.cog.karajan.debugger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JTabbedPane;

import org.globus.cog.karajan.util.ThreadedElement;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.globus.cog.karajan.workflow.nodes.Include;

public class SourcePanel extends DebuggerPanel implements BreakpointListener {
	private static final long serialVersionUID = -6260387593839220155L;
	
	private Map files;
	private JTabbedPane tabs;
	private DebuggerHook hook;

	public SourcePanel(DebuggerHook hook) {
		this.hook = hook;
		hook.addBreakpointListener(this);
		setLayout(new BorderLayout());
		tabs = new JTabbedPane();
		add(tabs, BorderLayout.CENTER);
		files = new Hashtable();
		setPreferredSize(new Dimension(640, 480));
		loadBreakpoints();
	}

	public void addFile(String file) {
		FilePanel fp = new FilePanel(file, hook, this);
		if (breakpoints.containsKey(file)) {
			Set s = (Set) breakpoints.get(file);
			Iterator i = s.iterator();
			while (i.hasNext()) {
				Integer ln = (Integer) i.next();
				fp.toggleBreakpoint(ln.intValue(), true);
				fp.getBreakpoints().add(ln);
			}
		}
		files.put(file, fp);
		tabs.add(file.substring(1 + file.lastIndexOf('/')), fp);
		tabs.setSelectedComponent(fp);
	}

	public void breakpointReached(ThreadedElement te) {
		stepReached(te);
	}

	public void stepReached(ThreadedElement te) {
		int line = getLine(te);
		FilePanel panel = getPanel(te);
		if (panel != null) {
			panel.setCurrentLine(line, te.getThread());
		}
	}

	private FilePanel getPanel(ThreadedElement te) {
		String file = getFileName(te);
		if (file == null) {
			System.err.println("Unknown file");
			return null;
		}
		else {
			if (!files.containsKey(file)) {
				addFile(file);
			}
			return (FilePanel) files.get(file);
		}
	}

	private int getLine(ThreadedElement te) {
		Integer line = (Integer) te.getElement().getProperty(FlowElement.LINE);
		if (line == null) {
			line = (Integer) te.getElement().getParent().getProperty(FlowElement.LINE);
		}
		if(line != null) {
			return line.intValue() - 1;
		}
		else {
			return -1;
		}
	}

	private String getFileName(ThreadedElement te) {
		FlowElement fe = te.getElement();
		if (fe instanceof Include) {
			//this sucks
			return (String) FlowNode.getTreeProperty(FlowElement.FILENAME, fe.getParent());
		}
		else {
			return (String) FlowNode.getTreeProperty(FlowElement.FILENAME, fe);
		}
	}

	public void resumed(ThreadedElement te) {
		FilePanel panel = getPanel(te);
		if (panel != null) {
			panel.setCurrentLine(-1, te.getThread());
		}
	}

	public void setSelected(FilePanel panel) {
		if (tabs.getSelectedComponent() != panel) {
			tabs.setSelectedComponent(panel);
		}
	}

	public void saveBreakpoints() {
		Component[] c = tabs.getComponents();
		for (int i = 0; i < c.length; i++) {
			FilePanel fp = (FilePanel) c[i];
			String fileName = fp.getFile();
			breakpoints.put(fileName, new HashSet(fp.getBreakpoints()));
		}
		try {
			BufferedWriter wr = new BufferedWriter(new FileWriter(new File("breakpoints.kdbg")));

			Iterator i = breakpoints.keySet().iterator();
			while (i.hasNext()) {
				String fileName = (String) i.next();
				Set bp = (Set) breakpoints.get(fileName);
				Iterator j = bp.iterator();
				while (j.hasNext()) {
					wr.write(fileName + " " + j.next().toString() + '\n');
				}
			}
			wr.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Map breakpoints = new Hashtable();

	public void loadBreakpoints() {
		try {
			BufferedReader dbgr = new BufferedReader(new FileReader("breakpoints.kdbg"));
			String line = dbgr.readLine();
			while (line != null) {
				try {
					String[] s = line.split(" ");
					int lno = Integer.parseInt(s[1]);
					Set b = (Set) breakpoints.get(s[0]);
					if (b == null) {
						b = new HashSet();
						breakpoints.put(s[0], b);
					}
					b.add(new Integer(lno));
					hook.addBreakpoint(hook.findElement(s[0], lno));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				line = dbgr.readLine();
			}
		}
		catch (Exception e) {

		}
	}
}
