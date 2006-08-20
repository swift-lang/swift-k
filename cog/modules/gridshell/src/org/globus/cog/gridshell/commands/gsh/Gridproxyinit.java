/*
 * 
 */
package org.globus.cog.gridshell.commands.gsh;

import java.beans.PropertyChangeEvent;

import org.globus.cog.gridshell.commands.AbstractShellCommand;
import org.globus.cog.gridshell.getopt.app.GetOptImpl;
import org.globus.cog.gridshell.getopt.interfaces.GetOpt;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gui.util.UITools;
import org.globus.gsi.CertUtil;
import org.globus.tools.proxy.GridProxyInit;

/**
 * @author rwinch
 */
public class Gridproxyinit extends AbstractShellCommand {
    public static final String PROXY_INIT_CLASSNAME = "org.globus.pkcs11.tools.PKCS11ProxyInit";
    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.commands.AbstractShellCommand#createGetOpt(org.globus.cog.gridshell.interfaces.Scope)
     */
    public GetOpt createGetOpt(Scope scope) {
        GetOptImpl result = new GetOptImpl(scope);
        return result;
    }

    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.interfaces.Command#execute()
     */
    public Object execute() throws Exception {
        CertUtil.init();

        GridProxyInit gpiFrame = new GridProxyInit(null, true);
        gpiFrame.setRunAsApplication(false);
        gpiFrame.saveProxy(true);
        gpiFrame.pack();
        UITools.center(null, gpiFrame);
        gpiFrame.setVisible(true);
        this.setStatusCompleted();
        return null;
    }

    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.interfaces.Command#destroy()
     */
    public Object destroy() throws Exception {
        // do nothing
        return null;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // doesn't handle propertychange events
        
    }

}
