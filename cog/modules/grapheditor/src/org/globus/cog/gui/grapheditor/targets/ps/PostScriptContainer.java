
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.ps;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.AbstractRootContainer;
import org.globus.cog.gui.grapheditor.RendererFactory;
import org.globus.cog.gui.grapheditor.canvas.AbstractCanvas;
import org.globus.cog.gui.grapheditor.canvas.views.StreamView;
import org.globus.cog.gui.grapheditor.generic.GenericEdge;
import org.globus.cog.gui.grapheditor.generic.GenericNode;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;

public class PostScriptContainer extends AbstractRootContainer {
	private static Logger logger = Logger.getLogger(PostScriptContainer.class);

	public PostScriptContainer() {
		RendererFactory.addClassRenderer(GenericNode.class, "postscript",
			GenericPostScriptNodeRenderer.class);
		RendererFactory.addClassRenderer(GenericEdge.class, "postscript",
			GenericPostScriptEdgeRenderer.class);
		RendererFactory.addClassRenderer(AbstractCanvas.class, "postscript",
			GenericPostScriptCanvasRenderer.class);
	}

	public void setRootNode(NodeComponent node) {
		super.setRootNode(node);
		String prop = (String) node.getPropertyValue("html.outputdir");
		if (prop != null) {
			logger.info("PostScript output directory is " + prop);
		}
		else {
			node.setPropertyValue("postscript.outputdir", "postscript");
		}
	}

	public void run() {
		try {
			if (getRootNode().hasProperty("postscript.iconcache")) {
				getRootNode().removeProperty(getRootNode().getProperty("postscript.iconcache"));
			}
			if (getRootNode().hasProperty("postscript.iconindex")) {
				getRootNode().removeProperty(getRootNode().getProperty("postscript.iconindex"));
			}
			File outputDirFile = new File((String) getRootNode().getPropertyValue(
				"postscript.outputdir"));
			outputDirFile.mkdirs();
			Writer wr = new FileWriter(new File((String) getRootNode().getPropertyValue(
				"postscript.outputdir"), (String) getRootNode().getPropertyValue(
				"postscript.outputfile")));
			wr.write("%!PS-Adobe-2.0 EPSF-3.0\n");
			((StreamView) getRootNode().getCanvas().newRenderer("postscript").getView()).render(wr);
			wr.write("showpage\n");
			wr.write("grapheditor restore\n");
			wr.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
	}
}
