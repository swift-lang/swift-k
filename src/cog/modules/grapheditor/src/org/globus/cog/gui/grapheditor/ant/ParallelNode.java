
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;


public class ParallelNode extends TaskNode {
	public ParallelNode() {
		setComponentType("parallel");
		setCanvasType(ParallelCanvas.class);
		loadIcon("images/ant-parallel.png");
		setName("parallel");
	}
	
	public boolean supportsType(String type){
		return getComponentType().equals(type);
	}
}

