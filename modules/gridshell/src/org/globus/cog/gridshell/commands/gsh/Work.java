/*
 * 
 */
package org.globus.cog.gridshell.commands.gsh;

import java.beans.PropertyChangeEvent;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.commands.AbstractShellCommand;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Scope;

/**
 * 
 */
public class Work extends AbstractShellCommand {
    private static final Logger logger = Logger.getLogger(Work.class);
    
    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridshell.interfaces.Scope)
     */
    public GetOpt createGetOpt(Scope scope) {        
        return new GetOptImpl(scope);
    }

    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.interfaces.Command#execute()
     */
    public Object execute() throws Exception {
        for(int i=0;i<Math.pow(10,1000);i++) {
            if(logger.isDebugEnabled() && i%100==0) {
                logger.debug("i="+i);
            }
        }
        this.setStatusCompleted();
        return null;
    }

    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.interfaces.Command#destroy()
     */
    public Object destroy() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub
        
    }

}
