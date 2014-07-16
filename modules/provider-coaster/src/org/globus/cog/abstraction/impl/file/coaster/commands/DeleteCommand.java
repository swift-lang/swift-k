//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 24, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.commands;

import org.globus.cog.coaster.commands.Command;

public class DeleteCommand extends Command {
    public static final String NAME = "DEL"; 

    public DeleteCommand(String name) {
        super(NAME);
        addOutData(name);
    }
}
