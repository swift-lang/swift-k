/*
 * 
 */
package org.globus.cog.gridshell.commands.taskcommands;

import org.globus.cog.gridshell.tasks.StartTask;

/**
 * 
 */
public class LLs extends Ls {
    /**
     * Just override our connection
     */
    public StartTask getConnection() {
        return getConnectionManager().getDefaultConnection();
    }
}
