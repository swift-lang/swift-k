
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.directorybrowser;

import javax.swing.tree.DefaultMutableTreeNode;

import org.globus.cog.abstraction.interfaces.GridFile;

public class GridFileTreeNode extends DefaultMutableTreeNode {
	
	public GridFileTreeNode(GridFile gridFile){
		super(gridFile);	
	}
	
	public String toString() {
		return ((GridFile) this.getUserObject()).getName();
	}
}
