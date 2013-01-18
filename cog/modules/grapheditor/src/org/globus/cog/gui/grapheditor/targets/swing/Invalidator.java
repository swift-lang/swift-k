
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 30, 2004
 *  
 */
package org.globus.cog.gui.grapheditor.targets.swing;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;

public class Invalidator extends Thread {
	private static Logger logger = Logger.getLogger(Invalidator.class);
	private static HashSet views = new HashSet();
	private CanvasView view;

	public Invalidator(CanvasView view) {
		this.view = view;
	}

	public void run() {
		synchronized (views) {
			if (views.contains(view)) {
				return;
			}
		}
		try {
			synchronized (view.getRenderer().getCanvas()) {
				if (view instanceof GraphView) {
					((GraphView) view).invalidate(true);
				}
				else {
					view.invalidate();
				}
			}
		}
		catch (Exception e) {
			view.getRenderer().getCanvas().getStatusManager().error("Exception caught while invalidating view", e);
			logger.warn("Exception caught while invalidating view", e);
		}
		finally {
			synchronized (views) {
				views.remove(view);
			}
		}
	}
}
