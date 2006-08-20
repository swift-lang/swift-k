//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
//Created on Sep 15, 2004

package org.globus.cog.gridface.impl.desktop.interfaces;

import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.beans.VetoableChangeListener;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.TransferHandler;
import javax.swing.event.InternalFrameListener;

import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.desktop.AbstractDesktopContainer;
import org.globus.cog.gridface.impl.desktop.dnd.DesktopTransferHandler;
import org.globus.cog.gridface.impl.desktop.frames.listener.DesktopInternalFrameListener;
import org.globus.cog.gridface.impl.desktop.icons.listener.DesktopIconListener;
import org.globus.cog.gridface.impl.desktop.listener.DesktopListener;
import org.globus.cog.gridface.impl.util.Logger;
import org.globus.cog.gridface.interfaces.Desktop;
/**
 * Swing Implementation of the Desktop Gridface
 */
public interface CoGTop extends Desktop, StatusListener,AccessIcons, AccessPopup, AccessClose,
		AccessToolBar, AccessActionProxy, AccessPreferences,Logger {

	public void resetDefaultIconLocation();
	
	//Access the Main Desktop Frame
	public AbstractDesktopContainer getDesktopFrame();
	public void setDesktopFrame(AbstractDesktopContainer desktopFrm);
	
	public int getId();
	
	//Desktopframe API
	public void addFrame(JInternalFrame newFrame);

	public void addFrame(JInternalFrame newFrame,
			TransferHandler transferHandler, DropTarget dropTarget,
			VetoableChangeListener vetoChangeListener,
			InternalFrameListener internalFrameListener);

	public void removeFrame(JInternalFrame frame);

	public boolean containsFrame(JInternalFrame checkFrame);


	//Load default listeners
	public void setDefaultListeners();

	public DesktopInternalFrameListener getDefaultDesktopFrameListener();

	public DesktopIconListener getDefaultDesktopIconListener();

	public DesktopListener getDefaultDesktopListener();

	public DesktopTransferHandler getDefaultDesktopTransferHandler();

	public void setDefaultDesktopFrameListener(DesktopInternalFrameListener listener);

	public void setDefaultDesktopIconListener(DesktopIconListener listener);

	public void setDefaultDesktopListener(DesktopListener listener);

	public void setDefaultDesktopTransferHandler(DesktopTransferHandler handler);

	//Desktop action logger
	public void startLogger();

	//Desktop menubar
	public JMenuBar getMenuBar();

	public void setMenuBar(JMenuBar desktopMenuBar);

	public boolean addMenuBarMenu(JMenu newMenu);
	
	public Rectangle getBounds();
}
