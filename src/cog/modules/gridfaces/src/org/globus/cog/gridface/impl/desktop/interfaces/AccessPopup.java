//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.interfaces;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public interface AccessPopup {

	//Desktop popup menu
	public JPopupMenu getPopup();
	public void setPopup(JPopupMenu desktopPopup);
	public boolean addPopupMenuItem(JMenuItem newMenuItem);
	public void showPopupAt(int xLoc, int yLoc);
	
	public void configurePopup(JPopupMenu popup);
}
