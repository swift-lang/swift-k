//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.panels;

import org.globus.cog.gridface.interfaces.GridCommand;

public class ErrorPanel extends AbstractOutputPanel{
	public ErrorPanel(GridCommand command) {
		super("Exception",command);
		
		if(!loadFileAttributeFromCommand("stderror")){
			keys.add("ExceptionString");
			load(null,command);
		}	
	}
}
