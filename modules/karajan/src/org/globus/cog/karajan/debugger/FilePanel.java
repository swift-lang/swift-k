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
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

import org.globus.cog.karajan.util.ThreadedElement;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class FilePanel extends JPanel implements BreakpointListener, MouseListener {
	private static final long serialVersionUID = 140747497796502653L;
	
	private String file;
	private StringBuffer sb;
	private DebuggerTextPane text;
	private JScrollPane sp;
	private DebuggerHook hook;
	private SourcePanel sourcePanel;
	private Set breakpoints;

	public static final Color COLOR_BREAKPOINT = new Color(255, 60, 0);

	private ArrayList currentLines = new ArrayList();

	public FilePanel(String file, DebuggerHook hook, SourcePanel sourcePanel) {
		this.hook = hook;
		this.sourcePanel = sourcePanel;
		setBackground(Color.WHITE);
		hook.addBreakpointListener(this);
		this.file = file;
		breakpoints = new HashSet();
		sb = new StringBuffer();
		try {
			BufferedReader fr;
			try {
				fr = new BufferedReader(new FileReader(file));
			}
			catch (Exception e) {
				try {
					fr = new BufferedReader(new InputStreamReader(
							getClass().getClassLoader().getResourceAsStream(file)));
				}
				catch (Exception ee) {
					fr = new BufferedReader(new CharArrayReader("Source not found".toCharArray()));
				}
			}

			text = new DebuggerTextPane();

			StyledDocument doc = text.getStyledDocument();

			KarajanHighlighter kh = new KarajanHighlighter();
			String line = fr.readLine();
			int count = 0;
			text.addLineMapping(0, 0);
			while (line != null) {
				sb.append(line);
				sb.append('\n');
				text.addLineMapping(sb.length(), ++count);
				line = fr.readLine();
			}
			fr.close();
			kh.setCurrentText(sb.toString());
			while (kh.hasMoreTokens()) {
				String token = kh.nextToken();
				AttributeSet style = kh.getStyle();
				doc.insertString(doc.getLength(), token, style);
			}

			setLayout(new BorderLayout());

			sp = new JScrollPane(text);

			add(sp, BorderLayout.CENTER);
			text.setLocation(0, 0);
			text.addMouseListener(this);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void breakpointReached(ThreadedElement te) {
	}

	public void stepReached(ThreadedElement te) {
	}

	public void setCurrentLine(int line, ThreadingContext thread) {
		for (int i = 0; i < currentLines.size(); i++) {
			TCL tcl = (TCL) currentLines.get(i);
			if (thread.isSubContext(tcl.tc)) {
				text.removeLineHighlight(tcl.l, Color.BLUE);
				text.addLineHighlight(line, Color.BLUE);
				ensureVisible(line);
				sourcePanel.setSelected(this);
				tcl.l = line;
				return;
			}
		}
		if (line != -1) {
			TCL tcl = new TCL();
			tcl.tc = thread;
			tcl.l = line;
			currentLines.add(tcl);
			text.addLineHighlight(line, Color.BLUE);
			ensureVisible(line);
		}
	}

	public void toggleBreakpoint(int line, boolean enabled) {
		if (enabled) {
			text.addLineHighlight(line - 1, COLOR_BREAKPOINT);
		}
		else {
			text.removeLineHighlight(line - 1, COLOR_BREAKPOINT);
		}
	}

	private synchronized void save() {
		sourcePanel.saveBreakpoints();
	}
	
	public String getFile() {
		return file;
	}
	
	public Set getBreakpoints() {
		return breakpoints;
	}

	protected void ensureVisible(int line) {
		try {
			Rectangle rect = text.getLineBounds(line);
			if (rect != DebuggerTextPane.BADLOC && rect != null) {
				Rectangle vrect = text.getVisibleRect();
				if (rect.y + 40 > vrect.y + vrect.height) {
					text.scrollRectToVisible(new Rectangle(0, (int) (rect.getY()
							+ vrect.getHeight() - 80), 1, 1));
					repaint();
				}
				else if (rect.y < vrect.y) {
					text.scrollRectToVisible(new Rectangle(0, (int) (rect.getY()
							+ vrect.getHeight() - 80), 1, 1));
					repaint();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class TCL {
		ThreadingContext tc;
		int l;
	}

	public void resumed(ThreadedElement te) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			int line = text.getLine(e.getY());
			Integer iline = new Integer(line);
			FlowElement fe = hook.findElement(file, line);
			if (fe != null) {
				if (breakpoints.contains(iline)) {
					breakpoints.remove(iline);
					hook.removeBreakpoint(fe);
					toggleBreakpoint(line, false);
				}
				else {
					breakpoints.add(iline);
					hook.addBreakpoint(hook.findElement(file, line));
					toggleBreakpoint(line, true);
				}
				save();
				System.out.println("Breakpoint toggled for "+fe);
			}
			else {
				System.err.println("Could not set breakpoint on line "+line);
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
	}
}