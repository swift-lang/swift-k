
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

import org.globus.cog.gridface.interfaces.GridCommand;

public interface CommandGraph{

	public void add(GridCommand command);

	public void remove(GridCommand command);

}