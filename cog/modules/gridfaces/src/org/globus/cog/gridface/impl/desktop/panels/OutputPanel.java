//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.panels;

import org.globus.cog.gridface.interfaces.GridCommand;


public class OutputPanel  extends AbstractOutputPanel{

	public OutputPanel(GridCommand command) {
		super("Output",command);
		if(!loadFileAttributeFromCommand("stdoutput")){
			keys.add("Output");
			load(null, command);
		}
	}
	
}
