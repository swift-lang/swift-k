
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 23, 2004
 */
package org.globus.cog.gui.grapheditor.canvas;

import javax.swing.Icon;


public class ForwardingStatusManager implements StatusManager {
	private GraphCanvas canvas;
	
	public ForwardingStatusManager(GraphCanvas canvas) {
		this.canvas = canvas;
	}

	public void setDefaultText(String text) {
		canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.SET_DEFAULT_TEXT, text, null, 0));
	}

	public void push(String msg) {
		canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.PUSH, msg, null, 0));
	}

	public void push(String msg, Icon icon) {
		canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.PUSH, msg, icon, 0));
	}

	public void pop() {
		canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.POP, null, null, 0));
	}

	public void initializeProgress(int size) {
		canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.INITIALIZE_PROGRESS, null, null, size));	
	}

	public void setProgress(int size) {
		canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.SET_PROGRESS, null, null, size));
	}

	public void stepProgress() {
		canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.STEP_PROGRESS, null, null, 0));
	}

	public void removeProgress() {
		canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.REMOVE_PROGRESS, null, null, 0));
	}
	
	public void error(String message, Exception details) {
		canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.ERROR, message, null, details, 0));
	}
	
	public void warning(String message, Exception details) {
		canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.WARNING, message, null, details, 0));
	}

	public void info(String message) {
        canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.INFO, message, null, null, 0));
	}

	public void debug(String message) {
        canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.DEBUG, message, null, null, 0));
	}

	public void out(String message) {
        canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.OUT, message, null, null, 0));
	}

	public void err(String message) {
        canvas.fireStatusEvent(new StatusEvent(canvas, StatusEvent.ERR, message, null, null, 0));
	}
}
