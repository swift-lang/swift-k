
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.html;


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

public class HtmlContainer extends AbstractRootContainer {
	private static Logger logger = Logger.getLogger(HtmlContainer.class);

	public HtmlContainer() {
		RendererFactory.addClassRenderer(GenericNode.class, "html",
			GenericHTMLNodeRenderer.class);
		RendererFactory.addClassRenderer(GenericEdge.class, "html",
			GenericHTMLEdgeRenderer.class);
		RendererFactory.addClassRenderer(RootCanvas.class, "html",
			GenericHTMLCanvasRenderer.class);
	}

	public void setRootNode(NodeComponent node) {
		super.setRootNode(node);
		if (node.getCanvas() == null) {
			node.createCanvas();
		}
		String prop = (String) node.getPropertyValue("html.outputdir");
		if (prop != null) {
			logger.info("HTML output directory is " + prop);
		}
		else {
			node.setPropertyValue("html.outputdir", "html");
		}

	}

	public void run() {
		try {
			File outputDirFile = new File((String) getRootNode()
				.getPropertyValue("html.outputdir"));
			outputDirFile.mkdirs();
			Writer wr = new FileWriter(new File((String) getRootNode()
				.getPropertyValue("html.outputdir"), "index.html"));
			wr
				.write("<html>\n<head>\n<META HTTP-EQUIV=\"PRAGMA\" CONTENT=\"NO-CACHE\">\n<title>Java CoG Kit Graph Viewer</title>\n</head>\n<body>");
			logger.debug("Rendering canvas...");
			((StreamView) getRootNode().getCanvas().newRenderer("html")
				.getView()).render(wr);
			wr.write("</body>\n</html>");
			wr.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
	}
}
