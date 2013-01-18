
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.canvas.views;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.canvas.CanvasRenderer;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.canvas.transformation.GraphTransformation;
import org.globus.cog.gui.grapheditor.canvas.transformation.IdentityTransformation;
import org.globus.cog.util.graph.GraphInterface;

/**
 * Implements basic methods for a view
 */
public abstract class AbstractView implements CanvasView {
	private static Logger logger = Logger.getLogger(AbstractView.class);
	
	private String name;
	private List transformations;
	private GraphInterface graph;
	private String type;
	private CanvasRenderer renderer;
	private boolean active;

	public AbstractView() {
		setTransformation(new IdentityTransformation());
		setType("view");
	}

	public void setTransformation(GraphTransformation transformation) {
		if (transformation == null) {
			logger.warn("setTransformation called with a null transformation");
			return;
		}
		transformations = new LinkedList();
		transformations.add(transformation);
	}

	public void addTransformation(GraphTransformation transformation) {
		if (transformation == null) {
			logger.warn("setTransformation called with a null transformation");
			return;
		}
		transformations.add(0, transformation);
	}

	public List getTransformations() {
		return transformations;
	}

	public String getName() {
		if (name == null) {
			throw new RuntimeException(getClass().getName() + ": name is null. " + "Did you initialize it?");
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public CanvasView getNewInstance(GraphCanvas canvas) {
		try {
			CanvasView newView = (CanvasView) getClass().newInstance();
			newView.setRenderer(renderer);
			return newView;
		}
		catch (InstantiationException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void invalidate() {
		if (!active) {
			logger.warn("Invalidate called on a non active view", new Throwable());
		}
		if (getCanvas() == null) {
			return;
		}
		Iterator i = getTransformations().listIterator();
		graph = getCanvas().getGraph();
		while (i.hasNext()) {
			GraphTransformation t = (GraphTransformation) i.next();
			graph = t.transform(graph);
		}
	}

	public GraphInterface getGraph() {
		return graph;
	}
	
	public void setGraph(GraphInterface graph) {
		this.graph = graph;
	}

	public void reLayout() {
		invalidate();
	}

	public void setViewport(Rectangle rect) {
		//By default, ignore it
		//Views which want to take advantage of it for scalability purposes, can
		//implement it
	}

	public Rectangle getViewport() {
		return null;
	}

	public boolean isSelective() {
		return false;
	}

	public void clean() {
		active = false;
	}
	
	public void activate() {
		active = true;
	}

	public CanvasRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(CanvasRenderer renderer) {
		this.renderer = renderer;
		this.graph = renderer.getCanvas().getGraph();
	}
	
	public GraphCanvas getCanvas() {
		if (renderer != null) {
			return renderer.getCanvas();
		}
		return null;
	}

	public boolean isActive() {
		return active;
	}

}
