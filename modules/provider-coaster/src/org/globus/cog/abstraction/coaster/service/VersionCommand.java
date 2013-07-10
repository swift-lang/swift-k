//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2005
 */
package org.globus.cog.abstraction.coaster.service;

import org.globus.cog.abstraction.coaster.service.local.VersionHandler;
import org.globus.cog.coaster.commands.Command;


public class VersionCommand extends Command {
	public VersionCommand() {
		super(VersionHandler.NAME);
	}

	public String getServerVersion()  {
		return new String(getInData());
	}
}
