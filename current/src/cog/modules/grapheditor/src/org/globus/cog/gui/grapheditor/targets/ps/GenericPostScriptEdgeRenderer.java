
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

import java.awt.Point;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.StreamRenderer;
import org.globus.cog.gui.grapheditor.edges.EdgeRenderer;

public class GenericPostScriptEdgeRenderer extends EdgeRenderer implements StreamRenderer {
	private static Logger logger = Logger.getLogger(GenericPostScriptEdgeRenderer.class);
	private static HashSet initialized;
	
	static {
		initialized = new HashSet();
	}

	public void render(Writer wr) throws IOException {
		if (!initialized.contains(wr)) {
			initialized.add(wr);
			wr.write("% l alpha x y\n");
			wr.write("/arrow {\n");
			wr.write("gsave translate rotate 0 0 moveto\n");
			wr.write("dup 0 rlineto stroke 0 moveto -5 -3 rlineto 5 3 rmoveto -5 3 rlineto stroke grestore} bind def\n");
		}
		Point pf = getEdge().getControlPoint(0);
		Point pt = getEdge().getControlPoint(1);
		double l = pf.distance(pt);
		double alpha = Math.atan2(pt.y - pf.y, pt.x - pf.x)*180/Math.PI;
		wr.write(l+" "+alpha+" "+pf.x+" "+pf.y+" arrow\n");
	}

}
