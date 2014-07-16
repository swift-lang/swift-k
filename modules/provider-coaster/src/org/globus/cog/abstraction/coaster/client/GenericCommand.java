//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 25, 2013
 */
package org.globus.cog.abstraction.coaster.client;

import org.globus.cog.coaster.commands.Command;

public class GenericCommand extends Command {    
    public GenericCommand(String cmd, String[] args) {
        super(cmd);
        
        for (String s : args) {
            this.addOutData(s);
        }
    }
}
