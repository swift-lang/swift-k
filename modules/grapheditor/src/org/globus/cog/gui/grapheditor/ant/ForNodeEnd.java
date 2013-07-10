
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.NonSerializable;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;


/**
 * A node representing the end point of a for loop
 */
public class ForNodeEnd extends AntNode implements NonSerializable{
	private NodeComponent peer;
	
	public ForNodeEnd(NodeComponent peer) {
		this.peer = peer;
		loadIcon("images/dot.png");	
		setClassRendererClass(IconRenderer.class);
		setResizable(false);
	}
	
}

