//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 18, 2005
 */
package org.globus.cog.karajan.debugger;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.globus.cog.karajan.util.ThreadedElement;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.events.EventBus;

public class DebuggerFrame extends JFrame implements WindowListener {
	private static final long serialVersionUID = -6898390085083727061L;
	
	private StackPanel stack;
	private SourcePanel source;
	private ThreadsPanel threads;
	private ButtonBar buttons;
	private ElementTree tree;
	private DebuggerHook hook;
	private ExecutionContext ec;
	
	public static final Font INTERFACE_FONT = Font.decode("sans-PLAIN-10");

	public DebuggerFrame(ElementTree tree) {
		this.tree = tree;
		hook = new DebuggerHook(tree);
		EventBus.setEventHook(hook);
		stack = new StackPanel();
		source = new SourcePanel(hook);
		source.addFile(tree.getName());
		buttons = new ButtonBar(this);
		threads = new ThreadsPanel(hook, stack, buttons);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(buttons, BorderLayout.SOUTH);
		JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		left.setDividerLocation(0.4);
		left.setTopComponent(new JScrollPane(threads));
		left.setBottomComponent(new JScrollPane(stack));
		JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		main.setTopComponent(left);
		main.setBottomComponent(source);
		main.setDividerLocation(0.3);
		getContentPane().add(main, BorderLayout.CENTER);
		ec = new ExecutionContext(tree);
		ec.start();
		this.addWindowListener(this);
	}

	public void buttonPressed(int i) {
		ThreadedElement te = threads.getSelected();
		if (te == null) {
			return;
		}
		if (i == ButtonBar.STEP_OVER) {
			hook.stepOver(te);
		}
		if (i == ButtonBar.STEP_INTO) {
			hook.stepInto(te);
		}
		if (i == ButtonBar.RUN) {
			hook.run(te);
		}
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		EventBus.removeEventHook();
		threads.resumeAll();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
	
	public void waitFor() throws InterruptedException {
		synchronized(ec) {
			while (!ec.done()) {
				ec.wait();
			}
		}
	}
}
