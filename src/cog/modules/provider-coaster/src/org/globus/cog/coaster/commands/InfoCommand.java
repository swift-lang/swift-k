//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.coaster.commands;

import org.globus.cog.coaster.ProtocolException;


public class InfoCommand extends Command {
    public static final String NAME = "INFO";
    
	private final String type, options;
	
	public InfoCommand(String type, String options) {
	    super(NAME);
		this.type = type;
		this.options = options;
	}
	
	public void send() throws ProtocolException {
		addOutData(type);
		addOutData(options);
		super.send();
	}
}
