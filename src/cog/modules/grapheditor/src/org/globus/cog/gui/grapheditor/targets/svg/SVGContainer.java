
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.svg;


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

public class SVGContainer extends AbstractRootContainer {
	private static Logger logger = Logger.getLogger(SVGContainer.class);

	public SVGContainer() {
		RendererFactory.addClassRenderer(GenericNode.class, "svg", GenericSVGNodeRenderer.class);
		RendererFactory.addClassRenderer(GenericEdge.class, "svg", GenericSVGEdgeRenderer.class);
		RendererFactory.addClassRenderer(RootCanvas.class, "svg", GenericSVGCanvasRenderer.class);
	}

	public void setRootNode(NodeComponent node) {
		super.setRootNode(node);
		if (node.getCanvas() == null) {
			node.createCanvas();
		}
		String prop = (String) node.getPropertyValue("svg.outputdir");
		if (prop != null) {
			logger.info("SVG output directory is " + prop);
		}
		else {
			node.setPropertyValue("svg.outputdir", "svg");
		}
		prop = (String) node.getPropertyValue("svg.outputfile");
		if (prop != null) {
			logger.info("SVG output file is " + prop);
		}
		else {
			node.setPropertyValue("svg.outputfile", "graph.svg");
		}
	}

	public void run() {
		try {
			if (getRootNode().hasProperty("_svg.node.renderer.index")) {
				getRootNode().removeProperty(getRootNode().getProperty("_svg.node.renderer.index"));
			}
			if (getRootNode().hasProperty("_svg.node.renderer.map")) {
				getRootNode().removeProperty(getRootNode().getProperty("_svg.node.renderer.map"));
			}
			File outputDirFile = new File((String) getRootNode().getPropertyValue("svg.outputdir"));
			outputDirFile.mkdirs();
			Writer wr = new FileWriter(new File((String) getRootNode().getPropertyValue(
				"svg.outputdir"), (String) getRootNode().getPropertyValue("svg.outputfile")));
			wr.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
			wr
				.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3c.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
			logger.debug("Rendering canvas...");
			((StreamView) getRootNode().getCanvas().newRenderer("svg").getView()).render(wr);
			wr.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
	}
}
