//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.dnd;
/*
 * Handles Drag and Drop events on Desktop
 */

//Local imports
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.globus.cog.gridface.impl.desktop.AbstractDesktop;
import org.globus.cog.gridface.impl.desktop.AbstractDesktopContainer;
import org.globus.cog.gridface.impl.desktop.frames.DesktopInternalFrameImpl;
import org.globus.cog.gridface.impl.desktop.icons.DesktopIconGroup;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessActionProxy;
import org.globus.cog.gridface.impl.desktop.interfaces.CanImportActionProxy;
import org.globus.cog.gridface.impl.desktop.interfaces.CoGTop;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopInternalFrame;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopIcon;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopToolBar;
import org.globus.cog.gridface.impl.desktop.interfaces.ImportDataActionProxy;
import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;
import org.globus.cog.gridface.impl.util.Logger;
import org.globus.cog.gridface.impl.util.LoggerImpl;
import org.globus.cog.gridface.interfaces.Desktop;
public class DesktopTransferHandler extends TransferHandler {
	Logger desktopLogger = LoggerImpl.getInstance(AbstractDesktop.sDefaultLoggerName);
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbstractDesktopContainer.class.getName());
	
	//Transfer data
	//Apparently, if iconGroup is null it stops native icon drop
	//after initializing.. bug 2111
	//DesktopIconGroup iconGroup = null;
	DesktopIconGroup iconGroup = new DesktopIconGroup();
	
	//true if either linux or windows import
	public static boolean isSystemImportToDesktop ;
	public static boolean isSystemLinuxImportToDesktop;  //is a linux native import
	public static boolean isSystemWindowsImportToDesktop;//is a windows native import
	//true if native desktop icon group import
	public static boolean isImportDesktopIconGroup ;
	
	/**
	 * First method being called to initiate drag, usually by mouseDragged in
	 * MouseMotionListener.  This method overrides TransferHandler exportAsDrag
	 * and saves drag initiating component and the drag location.
	 */
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		setDragComponent(comp);
		Point dragPoint = ((MouseEvent) e).getPoint();
		setDragPoint(dragPoint);
		super.exportAsDrag(comp, e, action);
	}
	/**
	 * Second method called after initiating drag to get supported actions
	 *
	 * @return source action supported
	 */
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	/**
	 * Third method called during drag and drop.  Creates DestkopIconTransferable
	 * object containing the selected icons on our desktop
	 *
	 * @return DesktopIconTransferable object containing selected icons on desktop
	 */
	protected Transferable createTransferable(JComponent dragComponent) {
			if (dragComponent.getParent() instanceof CoGTop) {
				iconGroup =	((DesktopIcon) dragComponent).getDesktop().getSelectedIcons();
			} else if (dragComponent.getParent() instanceof DesktopToolBar) {
				iconGroup = new DesktopIconGroup();
				iconGroup.addElement(((DesktopIcon) dragComponent));
			}
		return new DesktopIconTransferable(iconGroup);
		}

	/**
	 * @return true if data flavor is supported
	 */
	public boolean canImport(JComponent dropComponent, DataFlavor[] flavors) {	
		isSystemImportToDesktop = false;
		  isSystemLinuxImportToDesktop = false;  //is a linux native import
		  isSystemWindowsImportToDesktop = false;//is a windows native import
		//true if native desktop icon group import
		isImportDesktopIconGroup = false;
		
		for (int i = 0; i < flavors.length; i++) {
			if (DesktopIconTransferable
				.groupIconDataFlavor
				.equals(flavors[i])) {
				isImportDesktopIconGroup= true;
			}
			else if(DataFlavor.javaFileListFlavor.equals(flavors[i]) && DesktopUtilities.isWindowsPlatform()) {
				isSystemWindowsImportToDesktop = true;
			}else if(DataFlavor.stringFlavor.equals(flavors[i]) && DesktopUtilities.isLinuxPlatform()){
				isSystemLinuxImportToDesktop = true;
			}
		}
		isSystemImportToDesktop= (isSystemLinuxImportToDesktop || 
									isSystemWindowsImportToDesktop)
									&& (dropComponent instanceof CoGTop);
		
		
		if(dropComponent instanceof AccessActionProxy){
			CanImportActionProxy actionProxy = ((AccessActionProxy)dropComponent).getCanImportActionProxy();
			if(actionProxy != null){
				if(isImportDesktopIconGroup && !isSystemImportToDesktop){
					return actionProxy.canImportToComponent(dropComponent,iconGroup);
				}else{
					return isSystemImportToDesktop;
				}
			}
		}	
		return false;
	}

	/**
	 * Most important method during drop operation.  This method is called by
	 * the drop method in DropHandler after a succesful drop.  This method
	 * performs actions of moving icons if drop is on desktop, or launching
	 * if drop is on icon.
	 *
	 * @return true if import was successful
	 */
	public boolean importData(JComponent c, Transferable t) {
		if (canImport(c, t.getTransferDataFlavors()) && (c instanceof AccessActionProxy)) {
			ImportDataActionProxy actionProxy = ((AccessActionProxy)c).getImportDataActionProxy();
			if(actionProxy != null){
				return ((ImportDataActionProxy)actionProxy).importDataToComponent(c,t,getDragComponent(),getDragPoint(),getDropPoint());
			}
		}
		return false;
	}

	/** Point where drag initiated on the component */
	protected int dragPoint_x;
	protected int dragPoint_y;

	/** Point where drop happened on Desktop */
	protected int dropPoint_x;
	protected int dropPoint_y;

	/** Component where drag was initiated */
	protected JComponent dragComponent;
	/** Component on which drop happened */
	protected JComponent dropComponent;

	/** DropHandler to handle drop actions during drag */
	protected static DropHandler dropHandler = new DropHandler();
	/** @return return drop handler */
	public static DropHandler getDropHandler() {
		return dropHandler;
	}
	
	//Setter and getters for drag/drop location and
	//drag/drop initiating components
	public Point getDropPoint() {
		return new Point(dropPoint_x, dropPoint_y);
	}

	public void setDropPoint(Point dropPoint) {
		this.dropPoint_x = (int) dropPoint.getX();
		this.dropPoint_y = (int) dropPoint.getY();
	}

	public Point getDragPoint() {
		return new Point(dragPoint_x, dragPoint_y);
	}

	public void setDragPoint(Point dragPoint) {
		this.dragPoint_x = (int) dragPoint.getX();
		this.dragPoint_y = (int) dragPoint.getY();
	}

	public JComponent getDragComponent() {
		return this.dragComponent;
	}

	public void setDragComponent(JComponent dragComponent) {
		this.dragComponent = dragComponent;
	}

	public JComponent getDropComponent() {
		return this.dropComponent;
	}

	public void setDropComponent(JComponent dropComponent) {
		this.dropComponent = dropComponent;
	}

	/**
	 * Inner class to implement mouse actions during drag and drop
	 */
	private static class DropHandler
		implements DropTargetListener, Serializable {

		protected boolean canImport;

		protected boolean actionSupported(int action) {
			return (action & (COPY_OR_MOVE)) != NONE;
		}

		public void dragEnter(DropTargetDragEvent e) {
			DataFlavor[] flavors = e.getCurrentDataFlavors();

			JComponent c = (JComponent) e.getDropTargetContext().getComponent();
			DesktopTransferHandler importer =
				(DesktopTransferHandler) c.getTransferHandler();

			if (importer != null && importer.canImport(c, flavors)) {
				canImport = true;
			} else {
				canImport = false;
			}

			int dropAction = e.getDropAction();
			if (canImport && actionSupported(dropAction)) {
				e.acceptDrag(dropAction);
			} else {
				e.rejectDrag();
			}
		}

		public void dragOver(DropTargetDragEvent e) {
			int dropAction = e.getDropAction();
						
			if (canImport && actionSupported(dropAction)) {
				JComponent dropComp =
					(JComponent) e.getDropTargetContext().getComponent();
				DesktopTransferHandler importer =
					(DesktopTransferHandler) dropComp.getTransferHandler();
				//Select components you drag over...only if Drag Component
				//is instance of AccessActionProxy
				if(importer.getDragComponent() instanceof AccessActionProxy){
					if (dropComp instanceof DesktopIcon) {
						if(!importer.iconGroup.contains(dropComp)){
							((DesktopIcon) dropComp).setSelected(true);
						}
					} else if (dropComp instanceof DesktopInternalFrame) {
						try {
							((DesktopInternalFrameImpl) dropComp).setSelected(true);
						} catch (PropertyVetoException exception) {
							exception.printStackTrace();
						}
					} else if (dropComp instanceof Desktop) {
					    //Dragging over desktop
					}
				
					e.acceptDrag(dropAction);
				}
			} else {
				e.rejectDrag();
			}
		}

		public void dragExit(DropTargetEvent e) {
			JComponent dropComp =
				(JComponent) e.getDropTargetContext().getComponent();
			DesktopTransferHandler importer =
				(DesktopTransferHandler) dropComp.getTransferHandler();
			
			//exiting drag without drop should unselect component that drag over
			//selected
			//only if Drag Component
			//is instance of AccessActionProxy
			if(importer.getDragComponent() instanceof AccessActionProxy){
				if (dropComp instanceof DesktopIcon) {
					if(!importer.iconGroup.contains(dropComp)){
						((DesktopIcon) dropComp).setSelected(false);
					}
				} else if (dropComp instanceof DesktopInternalFrameImpl) {
					try {
						((javax.swing.JInternalFrame) dropComp).setSelected(false);
					} catch (PropertyVetoException exception) {
						exception.printStackTrace();
					}
				}
			}

		}

		public void drop(DropTargetDropEvent e) {
			int dropAction = e.getDropAction();
		
			JComponent c = (JComponent) e.getDropTargetContext().getComponent();
			DesktopTransferHandler importer =
				(DesktopTransferHandler) c.getTransferHandler();

			if (canImport && importer != null && actionSupported(dropAction)) {
				e.acceptDrop(dropAction);
				try {
					if(!importer.iconGroup.contains(c)){
						Transferable t = e.getTransferable();
						//set drop point and component being dropped on
						importer.setDropPoint(e.getLocation());
						importer.setDropComponent(c);
						e.dropComplete(importer.importData(c, t));
					}else{
					    logger.warn("Cannot drop within icon selection");
					}
					

				} catch (RuntimeException re) {
					e.dropComplete(false);
				}
			} else {
				e.rejectDrop();
			}
		}

		public void dropActionChanged(DropTargetDragEvent e) {
			int dropAction = e.getDropAction();

			if (canImport && actionSupported(dropAction)) {
				e.acceptDrag(dropAction);
			} else {
				e.rejectDrag();
			}
		}
	}

}
