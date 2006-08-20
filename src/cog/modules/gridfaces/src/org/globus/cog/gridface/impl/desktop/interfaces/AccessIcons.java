//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.interfaces;

//Local imports
import java.awt.dnd.DropTarget;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.TransferHandler;

import org.globus.cog.gridface.impl.desktop.icons.AbstractIcon;
import org.globus.cog.gridface.impl.desktop.icons.DesktopIconGroup;
import org.globus.cog.gridface.impl.desktop.util.ObjectPair;

public interface AccessIcons {

	public Vector getAvailableIconTypes();
	public JMenu getAddNewIconMenu();
	
	public static final String sOK_button = "OK";
	public static final String sCancel_button = "Cancel";
	
	//API for icon access
	public void addIcon() throws Exception;
	public void addIcon(String text) throws Exception;
	public void addIcon(String iconType,String iconImage) throws Exception;
	public void addIcon(String text,String iconType,String iconImage) throws Exception;
	
	public void addIcon(String text,int xLoc, int yLoc) throws Exception;
	public void addIcon(String iconType,String iconImage,int xLoc, int yLoc) throws Exception;
	public void addIcon(String text,String iconType,String iconImage,int xLoc, int yLoc) throws Exception;
	public void addIcon(String applicationClass, ObjectPair arguments) throws Exception;
	public void addIcon(String applicationClass, ObjectPair arguments,String text) throws Exception;
	public void addIcon(String applicationClass, ObjectPair arguments,String text, String iconType,String iconImage) throws Exception;
	public void addIcon(String applicationClass, ObjectPair arguments, String text, String iconType, String iconImage,int xLoc, int yLoc) throws Exception;
	public void addIcon(AbstractIcon newIcon);
	public void addIcon(AbstractIcon newIcon, int xLoc, int yLoc,MouseListener mouseListener, MouseMotionListener mouseMotionListener, TransferHandler transferHandler, DropTarget dropTarget);
	
	public void placeIcon(AbstractIcon oldIcon);
	
	public void removeIcon(AbstractIcon icon);
	public void removeIcons(DesktopIconGroup icons) ;
	public void removeAllIcons();
	
	public AbstractIcon getIcon(int Id);
	public DesktopIconGroup getAllIcons();

	public DesktopIconGroup getSelectedIcons();
	public DesktopIconGroup getUnSelectedIcons();
	
	public void selectAllIcons();
	public void deselectAllIcons();

	public void invertIconSelection();
	
	public void selectIcons(DesktopIconGroup icons);
	public void deselectIcons(DesktopIconGroup icons);

	public void selectIcon(int iconId);
	public void deselectIcon(int iconId);

	public void dissableIcons(DesktopIconGroup icons);
	public void enableIcons(DesktopIconGroup icons);

	public void dissableIcon(int iconId);
	public void enableIcon(int iconId);
	
	public int captureIconSelection();
	public void clearCapturedIconSelection();
	public int releaseIconSelection();
	
	public void arrangeIcons();
	
}
