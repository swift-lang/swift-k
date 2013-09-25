
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.util;

import java.util.Stack;

import javax.swing.Icon;

import org.globus.cog.gui.grapheditor.canvas.LogConsole;
import org.globus.cog.util.ImageLoader;

public class StatusManager {
	private Stack m;
	private Stack i;
	private Icon busy, idle;
	private StatusRenderer renderer;
	private boolean progress;
	private String defaultText;
	private static StatusManager defaultSM = new StatusManager(new ConsoleStatusRenderer());
	private LogConsole lc;

	public StatusManager(StatusRenderer renderer) {
		this.renderer = renderer;
		m = new Stack();
		i = new Stack();
		ImageLoader il = new ImageLoader();
		busy = il.loadImage("images/16x16/co/status-busy.png");
		idle = il.loadImage("images/16x16/co/status-idle.png");
	}

	public void setDefaultText(String text) {
		defaultText = text;
		if (m.isEmpty()) {
			if (renderer != null) {
				renderer.setStatusText(text);
				renderer.setStatusIcon(idle);

			}
		}
	}

	public void push(String msg) {
		push(msg, busy);
	}

	public void push(String msg, Icon icon) {
		if (renderer == null) {
			return;
		}
		progress = false;
		m.push(msg);
		i.push(icon);
		if (renderer != null) {
			renderer.setStatusText(msg);
			renderer.setStatusIcon(icon);
		}
	}

	public void pop() {
		if (m.isEmpty()) {
			if (renderer != null) {
				renderer.setStatusText(defaultText);
				renderer.setStatusIcon(idle);
			}
			return;
		}
		if (progress) {
			removeProgress();
		}
		m.pop();
		i.pop();
		if (renderer != null) {

			if (m.isEmpty()) {
				renderer.setStatusText(defaultText);
				renderer.setStatusIcon(idle);
			}
			else {
				renderer.setStatusText((String) m.peek());
				renderer.setStatusIcon((Icon) i.peek());
			}
		}
	}

	public void initializeProgress(int size) {
		progress = true;
		if (renderer != null) {
			renderer.initializeProgress(size);
		}
	}

	public void setProgress(int size) {
		if (renderer != null) {
			renderer.setProgress(size);
		}
	}

	public void stepProgress() {

		if (renderer != null) {
			renderer.incrementProgress();
		}
	}

	public void removeProgress() {
		progress = false;
		if (renderer != null) {
			renderer.removeProgress();
		}
	}
	
	public void setLogConsole(LogConsole lc) {
		this.lc = lc;
	}
	
	public static void setDefault(StatusManager defaultSM) {
		StatusManager.defaultSM = defaultSM;
	}
	
	public static StatusManager getDefault() {
		return StatusManager.defaultSM;
	}
}
