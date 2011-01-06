
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.globus.cog.gui.setup.CoGSetup;

public class GridSetup extends Task
{
    public void execute() throws BuildException
    {
        try
        {
            CoGSetup setup = new CoGSetup();
            setup.run();
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new BuildException("Grid setup unsuccessful");
        }
    }

}
