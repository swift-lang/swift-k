//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.toolbar;

//Local imports
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.io.IOException;
import java.util.Enumeration;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import org.globus.cog.gridface.impl.desktop.dnd.DesktopIconTransferable;
import org.globus.cog.gridface.impl.desktop.dnd.DesktopTransferHandler;
import org.globus.cog.gridface.impl.desktop.icons.AbstractIcon;
import org.globus.cog.gridface.impl.desktop.icons.DesktopIconGroup;
import org.globus.cog.gridface.impl.desktop.icons.GenericIconImpl;
import org.globus.cog.gridface.impl.desktop.interfaces.CanImportActionProxy;
import org.globus.cog.gridface.impl.desktop.interfaces.CoGTop;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopToolBar;
import org.globus.cog.gridface.impl.desktop.interfaces.ImportDataActionProxy;
import org.globus.cog.gridface.impl.desktop.interfaces.MouseActionProxy;

public class DesktopToolBarImpl
	extends JToolBar
	implements DesktopToolBar {
    
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DesktopToolBarImpl.class.getName());
    
	/** Reference to desktop */
	private CoGTop desktop;

	/** icons from desktop docked in toolbar*/
	private DesktopIconGroup iconsDockedFromDesktop = null;
	/** system icons */
	private DesktopIconGroup systemIcons = null;

	//This is a fix for the separator not appearing
	private boolean seperatorPresent=false;

	public DesktopToolBarImpl() {
		super(JToolBar.VERTICAL);
		iconsDockedFromDesktop = new DesktopIconGroup();
		systemIcons = new DesktopIconGroup();
		this.setFloatable(false);
	}

	public void addIcon(AbstractIcon newIcon, boolean systemIcon) {
			
		if(systemIcon){
			systemIcons.addElement(newIcon);
			newIcon.addMouseListener(desktop.getDefaultDesktopIconListener());
		} else{
			if(!seperatorPresent){
				//TODO for some reason this does not get displayed 
				//in the toolbar
				//super.addSeparator();
				super.add(new JLabel("_____"));
				seperatorPresent = true;
			}
			iconsDockedFromDesktop.addElement(newIcon);
		}
		newIcon.setToolTipText(newIcon.getText());
		newIcon.setText(null);
		
		super.add(newIcon);

		newIcon.setDesktop(this.getDesktop());

		this.updateUI();
	}

	public void removeIcon(AbstractIcon icon) {
		this.remove(icon);
		iconsDockedFromDesktop.removeElement(icon);
		systemIcons.removeElement(icon);
		this.repaint();
	}

	public void deselectAllIcons(){
		for(int i=0;i<iconsDockedFromDesktop.size();i++){
			((AbstractIcon)iconsDockedFromDesktop.elementAt(i)).setSelected(false);
		}
		for(int i=0;i<systemIcons.size();i++){
			((AbstractIcon)systemIcons.elementAt(i)).setSelected(false);
		}	
	}

	public CoGTop getDesktop() {
		return this.desktop;
	}

	public void setDesktop(CoGTop desktop) {
		this.desktop = desktop;
	}

	public DesktopIconGroup getIconsDockedFromDesktop() {
		return iconsDockedFromDesktop;
	}

	public DesktopIconGroup getSystemIcons() {
		return systemIcons;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessPreferences#savePreferences(java.util.prefs.Preferences)
	 */
	public void savePreferences(Preferences startNode) {
		try {
			startNode.sync();
			AbstractIcon curIcon = null;
			for (Enumeration e = getSystemIcons().elements();
				e.hasMoreElements();
				) {
				curIcon = (AbstractIcon) e.nextElement();
				//TODO added comment to note that this section should not be edited, 11/04/04
				Preferences sysiconNode = startNode.node("SystemIcons");
				sysiconNode.put("WARNING","SystemIcons SECTION SHOULD NOT BE EDITED, IT IS READ ONLY");
				curIcon.savePreferences(
				        sysiconNode.node(
								"ICON_ID_"
								+ new Integer(curIcon.getId()).toString()));
//				curIcon.savePreferences(
//					startNode.node(
//						"SystemIcons/"
//							+ "ICON_ID_"
//							+ new Integer(curIcon.getId()).toString()));
			}

			for (Enumeration e = getIconsDockedFromDesktop().elements();
				e.hasMoreElements();
				) {
				curIcon = (AbstractIcon) e.nextElement();
				curIcon.savePreferences(
					startNode.node(
						"DesktopDockedIcons/"
							+ "ICON_ID_"
							+ new Integer(curIcon.getId()).toString()));
			}
		} catch (BackingStoreException be) {
		    logger.error(be);
		}

	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessPreferences#loadPreferences(java.util.prefs.Preferences)
	 */
	public void loadPreferences(Preferences startNode) {
		//NOTE: Can only load docked icons from desktop 
	    logger.debug("Loading desktop docked icons from toolbar preferences");
	
		String[] iconClassName = null;
		AbstractIcon iconObject = null;
		Preferences iconsPref = startNode.node("DesktopDockedIcons");
		Preferences iconNodePref = null;

		try {

			String[] prefChildren = iconsPref.childrenNames();
			try {
				for (int i = 0; i < prefChildren.length; i++) {
					iconNodePref = iconsPref.node(prefChildren[i]);
					String iconType =
						iconNodePref.get("icon.type", GenericIconImpl.NATIVE);
					logger.debug("Loading icon in toolbar of type: "+iconType);
					
					iconClassName = iconType.split(":");
					iconObject =
						(AbstractIcon) Class
							.forName(iconClassName[0])
							.newInstance();
					iconObject.loadPreferences(iconNodePref);
						//Need to add listeners as addIcon in ToolBar does not do it.
						iconObject.addMouseListener(getDesktop().getDefaultDesktopIconListener());
						iconObject.addMouseMotionListener(getDesktop().getDefaultDesktopIconListener());
						iconObject.setTransferHandler(getDesktop().getDefaultDesktopTransferHandler());
						iconObject.setDropTarget(new DropTarget(iconObject, DesktopTransferHandler.getDropHandler()));

					this.addIcon(iconObject,false);
					
				}
			} catch (InstantiationException e) {
			    logger.error(e);
			} catch (IllegalAccessException e) {
			    logger.error(e);
			} catch (IllegalArgumentException e) {
			    logger.error(e);
			}
		} catch (ClassNotFoundException ex) {
		    logger.error(ex);
		} catch (BackingStoreException be) {
		    logger.error(be);
		}
	}

	/**
	 * @return Returns the canImportActionProxy.
	 */
	public final CanImportActionProxy getCanImportActionProxy() {
		return (CanImportActionProxy)this;
	}

	/**
	 * @return Returns the importDataActionProxy.
	 */
	public final ImportDataActionProxy getImportDataActionProxy() {
		return (ImportDataActionProxy)this;
	}

	/**
	 * @return Returns the mouseActionProxy.
	 */
	public final MouseActionProxy getMouseActionProxy() {
		//No mouse actions are recorded on toolbar
		return null;
	}

	public boolean canImportToComponent(JComponent dropComponent,
			DesktopIconGroup iconGroup) {
		//Can import all DesktopIconGroup icons
		return true;
	}
	public boolean importDataToComponent(JComponent dropComponent,
			Transferable t, JComponent dragComponent, Point dragPoint,
			Point dropPoint) {
		try {
			DesktopIconGroup iconGroup = (DesktopIconGroup) t
					.getTransferData(DesktopIconTransferable.groupIconDataFlavor);
			if (DesktopTransferHandler.isImportDesktopIconGroup) {
				DesktopToolBar toolbar = (DesktopToolBar) dropComponent;
				//Drop target is DesktopToolBar, move selected icons
				for (int i = 0; i < iconGroup.size(); i++) {
					AbstractIcon icon = (AbstractIcon) iconGroup.elementAt(i);
					icon.setSelected(false);
					toolbar.addIcon(icon, false);
					icon.getDesktop().removeIcon(icon);
				}
				return true;
			}

		} catch (UnsupportedFlavorException ufe) {
		    logger.error(ufe);
		} catch (IOException ioe) {
		    logger.error(ioe);
		} catch (Exception e) {
		    logger.error(e);
		}
		return false;
	}
}
