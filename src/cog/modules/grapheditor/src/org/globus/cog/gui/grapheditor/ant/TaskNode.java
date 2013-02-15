
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.ant;

/**
 * A generic task. It constructs the state icons on the fly. Stopped tasks will
 * have the icon desaturated and darkened, while completed tasks will have another icon
 * overlayed, with either a check mark or an "x", depending on the success
 */
public class TaskNode extends AntNode {

	public TaskNode() {
		//set the canvas type
		setCanvasType(TaskCanvas.class);
		//set the type
		setComponentType("defaultAntNode");
		//set the component
	}

	public boolean supportsType(String type) {
		return true;
	}
}
