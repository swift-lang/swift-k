//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 14, 2009
 */
package org.globus.cog.abstraction.coaster.rlog;

import org.globus.cog.coaster.commands.Command;

public class RemoteLogCommand extends Command {
    public static final String NAME = "RLOG";
    
    public enum Type {
        STDOUT, STDERR, FATAL, ERROR, WARN, INFO, DEBUG;
    }
    
    public RemoteLogCommand(String msg) {
        this(Type.INFO, msg);
    }
    
    public RemoteLogCommand(Type type, String msg) {
        super(NAME);
        addOutData(type.toString());
        addOutData(msg);
    }
}
