// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 18, 2005
 */
package org.globus.cog.karajan.debugger;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class ButtonBar extends DebuggerPanel implements ActionListener {
	private static final long serialVersionUID = -3349690103692620622L;
	
	public static final int RUNNING = 0;
	public static final int STOPPED = 1;
	public static final int SUSPENDED = 2;
	
	public static final int RUN = 0;
	public static final int PAUSE = 1;
	public static final int STOP = 2;
	public static final int STEP_OVER = 3;
	public static final int STEP_INTO = 4;
	public static final int STEP_OUT = 5;
	
	private Map buttonMap;
	
	JButton stepOver, stepInto, stepOut, run, pause, stop;
	DebuggerFrame frame; 

	public ButtonBar(DebuggerFrame frame) {
		super();
		this.setFocusable(false);
		buttonMap = new Hashtable();
		this.frame = frame;
		run = new JButton("F2 - Run");
		bind(run, "F2", RUN);
		pause = new JButton("F3 - Pause");
		bind(pause, "F3", PAUSE);
		stop = new JButton("F4 - Stop");
		bind(stop, "F4", STOP);
		stepInto = new JButton("F7 - Step Into");
		bind(stepInto, "F7", STEP_INTO);
		stepOver = new JButton("F8 - Step Over");
		bind(stepOver, "F8", STEP_OVER);
		stepOut = new JButton("F9 - Step Out");
		bind(stepOut, "F9", STEP_OUT);
		setLayout(new FlowLayout());
		add(run);
		add(pause);
		add(stop);
		add(stepInto);
		add(stepOver);
		add(stepOut);
		setFont(DebuggerFrame.INTERFACE_FONT);
	}
	
	private void bind(JButton button, String key, int value) {
		buttonMap.put(button, new Integer(value));
		new KeyAction(button, key);
	}

	public void add(JButton button) {
		button.setFont(DebuggerFrame.INTERFACE_FONT);
		button.addActionListener(this);
		super.add(button);
	}

	public void setState(int state) {
		if (state == RUNNING) {
			run.setEnabled(false);
			pause.setEnabled(true);
			stop.setEnabled(true);
			stepInto.setEnabled(false);
			stepOver.setEnabled(false);
			stepOut.setEnabled(false);
		}
		else if (state == STOPPED) {
			run.setEnabled(true);
			pause.setEnabled(false);
			stop.setEnabled(false);
			stepInto.setEnabled(false);
			stepOver.setEnabled(false);
			stepOut.setEnabled(false);
		}
		else if (state == SUSPENDED) {
			run.setEnabled(true);
			pause.setEnabled(false);
			stop.setEnabled(true);
			stepInto.setEnabled(true);
			stepOver.setEnabled(true);
			stepOut.setEnabled(true);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Integer value = (Integer) buttonMap.get(e.getSource());
		if (value != null) {
			frame.buttonPressed(value.intValue());
		}
	}

	public static class KeyAction extends AbstractAction {
		private static final long serialVersionUID = 4840239616461407863L;
		
		private JButton button;

		public KeyAction(JButton button, String key) {
			this.button = button;
			button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key),
					"pressed");
			button.getActionMap().put("pressed", this);
		}

		public void actionPerformed(ActionEvent e) {
			button.doClick();
		}
	}
}
