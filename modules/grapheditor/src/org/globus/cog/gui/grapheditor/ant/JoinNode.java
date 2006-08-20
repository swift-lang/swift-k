
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.NonSerializable;

/**
 * Used between two parallel sections following each other sequentially
 */
public class JoinNode extends AntNode implements NonSerializable{
	public JoinNode() {
		loadIcon("images/dot.png");
		setClassRendererClass(IconRenderer.class);
		setResizable(false);
	}
}

