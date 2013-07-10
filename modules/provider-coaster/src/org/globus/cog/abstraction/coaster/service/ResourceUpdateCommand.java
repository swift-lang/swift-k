//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 7, 2011
 */
package org.globus.cog.abstraction.coaster.service;

import org.globus.cog.coaster.commands.Command;

public class ResourceUpdateCommand extends Command {
    public static final String NAME = "RESOURCEUPDATE";
    
    public ResourceUpdateCommand(String name, String value) {
        super(NAME);
        addOutData(name);
        addOutData(value);
    }
}
