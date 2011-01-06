
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 9, 2003
 */
package org.globus.cog.karajan.viewer;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.generic.RootNode;

public class KarajanRootNode extends RootNode{
	private static Logger logger = Logger.getLogger(KarajanRootNode.class);

	public KarajanRootNode(){
		setCanvasType(KarajanRootCanvas.class);
	}
}
