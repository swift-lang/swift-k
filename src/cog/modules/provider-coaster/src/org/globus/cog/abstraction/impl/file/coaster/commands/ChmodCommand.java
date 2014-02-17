//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 24, 2008
 */
package org.globus.cog.abstraction.impl.file.coaster.commands;

import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.coaster.commands.Command;

public class ChmodCommand extends Command {
    public static final String NAME = "CHMOD"; 

    public ChmodCommand(GridFile f) {
        super(NAME);
        addOutData(f.getName());
        addOutData(f.getUserPermissions().toDigit());
        addOutData(f.getGroupPermissions().toDigit());
        addOutData(f.getWorldPermissions().toDigit());
    }
}
