//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------


package org.globus.cog.gridface.impl.desktop.interfaces;

import javax.swing.SwingConstants;

import org.globus.cog.gridface.impl.desktop.icons.AbstractIcon;
import org.globus.cog.gridface.impl.desktop.icons.DesktopIconGroup;

public interface DesktopToolBar extends AccessDesktop, AccessActionProxy, 
AccessPreferences,ImportDataActionProxy,CanImportActionProxy {
    public static final int HORIZONTAL = SwingConstants.HORIZONTAL;
    public static final int VERTICAL   = SwingConstants.VERTICAL;
    
	public void addIcon(AbstractIcon newIcon, boolean systemIcon);

	public void removeIcon(AbstractIcon icon);

	public void deselectAllIcons();

	public DesktopIconGroup getIconsDockedFromDesktop();

	public DesktopIconGroup getSystemIcons();
}
