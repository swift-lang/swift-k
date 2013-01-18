
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 23, 2004
 */
package org.globus.cog.gui.grapheditor.targets.remote;

import java.awt.Dimension;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.canvas.AbstractCanvasRenderer;
import org.globus.cog.gui.grapheditor.canvas.CanvasEvent;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;

public class RemoteCanvasRenderer extends AbstractCanvasRenderer {
	private static Logger logger = Logger.getLogger(RemoteCanvasRenderer.class);

	public RemoteCanvasRenderer() {
		CanvasView view = new RemoteCanvasView();
		addSupportedView(view);
		setView(view);
		
	}

	public void setSize(Dimension dimension) {
	}

	public void canvasEvent(CanvasEvent e) {
		if (e.getType() == CanvasEvent.INVALIDATE) {
			super.canvasEvent(e);
			RemoteContainer.getContainer().updateGraph();
		}
	}
}
