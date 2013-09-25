
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;


/**
 * "sequential" actually
 */
public class SerialNode extends TaskNode{
	public SerialNode() {
		setComponentType("sequential");
		setCanvasType(SerialCanvas.class);
		loadIcon("images/ant-sequential.png");
		setName("sequential");
	}
	
	public boolean supportsType(String type){
		return getComponentType().equals(type);
	}
}

