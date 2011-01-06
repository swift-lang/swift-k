
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.canvas;


import java.util.LinkedList;

import org.globus.cog.gui.grapheditor.RootContainer;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;

public abstract class AbstractCanvasRenderer implements CanvasRenderer {
	private CanvasView view;

	private java.util.List supportedViews;

	private GraphCanvas canvas;

	private RootContainer rootContainer;

	public AbstractCanvasRenderer() {
		supportedViews = new LinkedList();
	}

	public void setView(CanvasView view) {
		if (getView() != null) {
			getView().clean();
		}
		this.view = view;
		if (view != null) {
			this.view.setRenderer(this);
			getView().activate();
		}
	}

	public CanvasView getView() {
		return view;
	}

	public java.util.List getSupportedViews() {
		return supportedViews;
	}

	/**
	 * Adds a prototype view to this canvas.
	 * 
	 * @param view
	 */
	public void addSupportedView(CanvasView view) {
		supportedViews.add(view);
	}

	public void setCanvas(GraphCanvas canvas) {
		if (this.canvas != null) {
			this.canvas.removeCanvasEventListener(this);
		}
		this.canvas = canvas;
		if (this.view != null) {
			this.view.setRenderer(this);
		}
		if (this.canvas != null) {
			this.canvas.addCanvasEventListener(this);
		}
	}

	public GraphCanvas getCanvas() {
		return canvas;
	}

	public void canvasEvent(CanvasEvent e) {
		if (e.getType() == CanvasEvent.INVALIDATE) {
			if (getView() != null) {
				getView().invalidate();
			}
		}
	}

	public void dispose() {
		if (canvas != null) {
			canvas.removeCanvasEventListener(this);
		}
		if (view != null) {
			view.clean();
		}
		view = null;
	}

	public RootContainer getRootContainer() {
		return rootContainer;
	}

	public void setRootContainer(RootContainer rootContainer) {
		this.rootContainer = rootContainer;
	}

}
