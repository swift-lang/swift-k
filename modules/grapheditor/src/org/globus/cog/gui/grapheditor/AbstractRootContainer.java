
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/* 
 * Created on Jan 26, 2004
 */
package org.globus.cog.gui.grapheditor;


import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.canvas.CanvasRenderer;
import org.globus.cog.gui.grapheditor.generic.RootContainerHelper;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;

public abstract class AbstractRootContainer implements RootContainer {
	private static Logger logger = Logger
		.getLogger(AbstractRootContainer.class);

	private NodeComponent rootNode;
	private CanvasRenderer canvasRenderer;

	public AbstractRootContainer() {
	}

	public void load(String fileName) {
		try {
			rootNode.createCanvas();
			RootContainerHelper.load(fileName, rootNode.getCanvas());
		}
		catch (Exception e) {
			logger.error("Failed to load " + fileName, e);
		}
	}

	public void save(String fileName) {
		try {
			RootContainerHelper.save(fileName, getRootNode().getCanvas());
		}
		catch (Exception e) {
			logger.error("Exception caught while saving file:");
			logger.error(e);
		}
	}

	public void run() {
	}

	public void setRootNode(NodeComponent node) {
		this.rootNode = node;
	}

	public NodeComponent getRootNode() {
		return rootNode;
	}

	protected void setCanvasRenderer(CanvasRenderer canvasRenderer) {
		this.canvasRenderer = canvasRenderer;
	}

	public CanvasRenderer getCanvasRenderer() {
		return canvasRenderer;
	}

	public void activate() {
	}

	public void dispose() {
		getCanvasRenderer().dispose();
		setCanvasRenderer(null);
		setRootNode(null);
	}
}
