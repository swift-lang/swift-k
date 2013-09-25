
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


import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.StreamRenderer;
import org.globus.cog.gui.grapheditor.generic.AbstractGenericNodeRenderer;

public class GenericDotNodeRenderer extends AbstractGenericNodeRenderer implements StreamRenderer {
	private static Logger logger = Logger.getLogger(GenericDotNodeRenderer.class);

	public GenericDotNodeRenderer() {
	}

	public void render(Writer wr) throws IOException {
		wr.write("\t" + getComponent().getPropertyValue("nodeid") + " [label=\"" + getLabel()
			+ "\"];\n");
	}
}
