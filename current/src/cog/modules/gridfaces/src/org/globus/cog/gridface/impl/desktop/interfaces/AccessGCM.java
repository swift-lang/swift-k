//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
/*
 * Created on Aug 9, 2004
 */
package org.globus.cog.gridface.impl.desktop.interfaces;

import org.globus.cog.gridface.interfaces.GridCommandManager;

public interface AccessGCM {
	public GridCommandManager getGCM();
	public void setGCM(GridCommandManager gcm);
	public boolean loadGCM();
	public boolean saveGCM();
}
