// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 18, 2005
 */
package org.globus.cog.karajan.debugger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.globus.cog.karajan.util.ThreadedElement;

public class ThreadsPanel extends DebuggerPanel implements BreakpointListener, FocusListener {
	private static final long serialVersionUID = 7970850092601861034L;
	
	private DebuggerHook hook;
	private Map threads, elements;
	private StackPanel stack;
	private ThreadedElement selected;
	private ButtonBar buttons;

	public ThreadsPanel(DebuggerHook hook, StackPanel stack, ButtonBar buttons) {
		super();
		this.hook = hook;
		this.stack = stack;
		this.buttons = buttons;
		hook.addBreakpointListener(this);
		threads = new Hashtable();
		elements = new Hashtable();
		this.setLayout(new SimpleLayout());
		setBackground(Color.WHITE);
		this.setPreferredSize(new Dimension(160, 200));
	}

	public void breakpointReached(ThreadedElement te) {
		stepReached(te);
	}

	public void stepReached(ThreadedElement te) {
		ThreadEntry entry;
		boolean isSelected = false;
		if (threads.containsKey(te.getThread())) {
			entry = (ThreadEntry) threads.get(te.getThread());
			remove(entry);
		}

		if (selected != null) {
			if (selected.getThread().equals(te.getThread())) {
				isSelected = true;
				stack.addStack(hook.getStack(te));
			}
		}
		else {
			isSelected = true;
		}
		entry = new ThreadEntry(te, hook.getStack(te));

		entry.addFocusListener(this);
		threads.put(te.getThread(), entry);
		elements.put(entry, te);
		add(entry);
		validate();
		if (isSelected) {
			buttons.setState(ButtonBar.SUSPENDED);
			if (selected != null) {
				ThreadEntry oldsel = (ThreadEntry) threads.get(selected.getThread());
				if (oldsel != null) {
					oldsel.setSelected(false);
				}
			}
			entry.setSelected(true);
			selected = te;
		}
		repaint();
	}

	public void resumed(ThreadedElement te) {
		if (threads.containsKey(te.getThread())) {
			ThreadEntry entry = (ThreadEntry) threads.get(te.getThread());
			remove(entry);
		}
		if (selected != null) {
			if (selected.getThread().equals(te.getThread())) {
				buttons.setState(ButtonBar.RUNNING);
			}
		}		
		repaint();
	}

	public void focusGained(FocusEvent e) {
		ThreadEntry te = (ThreadEntry) e.getComponent();
		stack.addStack(te.getStack());
		selected = (ThreadedElement) elements.get(te);
		buttons.setState(ButtonBar.SUSPENDED);
	}

	public void focusLost(FocusEvent e) {
	}

	public ThreadedElement getSelected() {
		return selected;
	}

	public void resumeAll() {
		Iterator i = elements.values().iterator();
		while (i.hasNext()) {
			hook.run((ThreadedElement) i.next());
		}
	}
}
