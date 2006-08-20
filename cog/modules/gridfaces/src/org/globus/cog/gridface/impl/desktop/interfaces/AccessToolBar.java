//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
/*
 * Created on Aug 6, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.globus.cog.gridface.impl.desktop.interfaces;

import org.globus.cog.gridface.impl.desktop.toolbar.DesktopToolBarImpl;


public interface AccessToolBar {
	//Desktop toolbar
	public DesktopToolBarImpl getToolBar();
	public void setToolBar(DesktopToolBarImpl toolbar);
}
