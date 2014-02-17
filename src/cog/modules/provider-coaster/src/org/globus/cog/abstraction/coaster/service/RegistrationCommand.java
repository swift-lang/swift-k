//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.abstraction.coaster.service;

import org.globus.cog.abstraction.coaster.service.local.RegistrationHandler;
import org.globus.cog.coaster.commands.Command;

/**
 * Informs the client that the CoasterService is ready  
 * */
public class RegistrationCommand extends Command {
	public RegistrationCommand(String id, String url) {
		super(RegistrationHandler.NAME);
		addOutData(id);
		addOutData(url);
	}
}
