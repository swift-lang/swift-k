//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 28, 2005
 */
package org.globus.cog.gui.grapheditor.targets.swing.util;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

/**
 * Links a switch type action with a frame, such that when the action
 * is selected, the window is visible and the other way around. Also, when
 * the window is closed, the action is deselected.
 */
public class WindowVisibilitySwitchLink implements CanvasActionListener, WindowListener {
	private CanvasAction action;
	private Controler controller;
	private JFrame window;
	
	public WindowVisibilitySwitchLink(CanvasAction action, Controler creator) {
		this.action = action;
		this.controller = creator;
		action.addCanvasActionListener(this);
		if (action.isSelected()) {
			showWindow();
		}
	}
	
	private synchronized void showWindow() {
		if (window != null) {
			return;
		}
		window = controller.createWindow();
		window.addWindowListener(this);
		window.setVisible(true);
	}
	
	private synchronized void closeWindow() {
		if (window == null) {
			return;
		}
		controller.windowClosing(window);
		window.setVisible(false);
		window.dispose();
		window = null;
	}

	public void canvasActionPerformed(CanvasActionEvent e) {
		if (e.getCanvasAction().isSelected()) {
			showWindow();
		}
		else {
			closeWindow();
		}
		e.setConsumed(true);
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		closeWindow();
		action.setSelected(false);
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}
	
	public static abstract class Controler {
		protected abstract JFrame createWindow();
		
		protected void windowClosing(JFrame window) {
			
		}
	}
}
