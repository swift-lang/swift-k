
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.dot;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.AbstractRootContainer;
import org.globus.cog.gui.grapheditor.RendererFactory;
import org.globus.cog.gui.grapheditor.canvas.views.StreamView;
import org.globus.cog.gui.grapheditor.generic.GenericEdge;
import org.globus.cog.gui.grapheditor.generic.GenericNode;
import org.globus.cog.gui.grapheditor.generic.RootCanvas;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;

public class DotContainer extends AbstractRootContainer {
	private static Logger logger = Logger.getLogger(DotContainer.class);

	public DotContainer() {
		RendererFactory.addClassRenderer(GenericNode.class, "dot", GenericDotNodeRenderer.class);
		RendererFactory.addClassRenderer(GenericEdge.class, "dot", GenericDotEdgeRenderer.class);
		RendererFactory.addClassRenderer(RootCanvas.class, "dot", GenericDotCanvasRenderer.class);
	}

	public void setRootNode(NodeComponent node) {
		super.setRootNode(node);
		if (node.getCanvas() == null) {
			node.createCanvas();
		}
		String prop = (String) node.getPropertyValue("dot.outputdir");
		if (prop != null) {
			logger.info("Dot output directory is " + prop);
		}
		else {
			node.setPropertyValue("dot.outputdir", "dot");
		}
		prop = (String) node.getPropertyValue("dot.outputfile");
		if (prop != null) {
			logger.info("Dot output file is " + prop);
		}
		else {
			node.setPropertyValue("dot.outputfile", "graph.dot");
		}
	}

	public void run() {
		try {
			File outputDirFile = new File((String) getRootNode().getPropertyValue("dot.outputdir"));
			outputDirFile.mkdirs();
			Writer wr = new FileWriter(new File((String) getRootNode().getPropertyValue(
				"dot.outputdir"), (String) getRootNode().getPropertyValue("dot.outputfile")));
			wr.write("digraph cog_graph {\n");
			wr.write("graph [label=\"Java CoG Kit Graph Editor\"];\n");
			
			logger.debug("Rendering canvas...");
			((StreamView) getRootNode().getCanvas().newRenderer("dot").getView()).render(wr);
			wr.write("}");
			wr.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
	}
}
