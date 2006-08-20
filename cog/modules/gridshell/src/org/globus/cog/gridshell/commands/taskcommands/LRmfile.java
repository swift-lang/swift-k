/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import org.globus.cog.gridshell.tasks.StartTask;

/**
 * 
 */
public class LRmfile extends Rmfile {
    /**
     * Just override our connection
     */
    public StartTask getConnection() {
        return getConnectionManager().getDefaultConnection();
    }
}
