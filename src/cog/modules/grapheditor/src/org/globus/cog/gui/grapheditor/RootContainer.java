
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/* 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor;

import org.globus.cog.gui.grapheditor.canvas.CanvasRenderer;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;

public interface RootContainer extends Runnable{
	public void setRootNode(NodeComponent node);
	
	public void load(String fileName);
	
	public void save(String fileName);
	
	public CanvasRenderer getCanvasRenderer();
	
	public void activate();
	
	public void dispose();
}
