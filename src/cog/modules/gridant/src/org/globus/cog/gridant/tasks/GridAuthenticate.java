
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridant.tasks;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.globus.tools.proxy.GridProxyInit;
import org.globus.tools.ui.util.UITools;


public class GridAuthenticate extends Task
{
    public void execute() throws BuildException
    {
        GridProxyInit gpiFrame = new GridProxyInit(null, true);
        gpiFrame.setRunAsApplication(false);
        gpiFrame.setCloseOnSuccess(true);
        gpiFrame.saveProxy(true);
        WindowListener listener = new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                log("Grid authentication aborted by user");
            }
        };
        gpiFrame.addWindowListener(listener);
        gpiFrame.pack();
        UITools.center(null, gpiFrame);
        gpiFrame.setVisible(true);
    }
}
