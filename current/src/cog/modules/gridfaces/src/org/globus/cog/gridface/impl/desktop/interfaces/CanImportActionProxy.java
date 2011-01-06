//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.interfaces;

import javax.swing.JComponent;

import org.globus.cog.gridface.impl.desktop.icons.DesktopIconGroup;

public interface CanImportActionProxy extends ActionProxy {
	public boolean canImportToComponent(JComponent dropComponent, DesktopIconGroup iconGroup);
	
}
