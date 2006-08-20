
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.directorybrowser;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JComponent;

public class DirectoryBrowserDropTargetListener implements DropTargetListener, Serializable {
	
	DirectoryBrowserImpl db;
	DirectoryBrowserTransferHandler localTransferHandler;
	private DataFlavor dbDataFlavor = DirectoryBrowserTransferHolder.getDataFlavor();
	
	public DirectoryBrowserDropTargetListener(DirectoryBrowserImpl db) {
		this.db = db;
		this.localTransferHandler = (DirectoryBrowserTransferHandler) db.tree.getTransferHandler();
	}
	
	public void dragEnter(DropTargetDragEvent dragEvent) {
		dragEvent.acceptDrag(dragEvent.getDropAction());
	}
	
	
	public void dragOver(DropTargetDragEvent dragEvent) {
		//System.out.println("checking drag equals");
		//if(db.tree.getDropTarget() != dragEvent.getSource()) {
		//	System.out.println("going to allow drag");
			//JTree tree  = (JTree) dragEvent.getDropTargetContext().getComponent();
			//int row = tree.getClosestRowForLocation(dragEvent.getLocation().x, dragEvent.getLocation().y);
			//tree.setSelectionRow(row);
		//} else {
			//This prevents users from dragging in the same tree.  This will probably change at
			//some point.
		//	dragEvent.rejectDrag();
		//}
	}
	
	public void dropActionChanged(DropTargetDragEvent arg0) {
	}
	
	public void dragExit(DropTargetEvent arg0) {
	}
	
	public void drop(DropTargetDropEvent dropEvent) {
		DirectoryBrowserTransferHolder dbholder = null;
		try {
			dbholder = (DirectoryBrowserTransferHolder) dropEvent.getTransferable().getTransferData(dbDataFlavor);
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
		}
		
		
		if(dropEvent.isLocalTransfer() && (!dbholder.getSessionId().equals(db.getFileTransferObject().getSessionId()))) {
			int row = db.tree.getClosestRowForLocation(dropEvent.getLocation().x, dropEvent.getLocation().y);
			db.tree.setSelectionRow(row);
			JComponent c = (JComponent) dropEvent.getDropTargetContext().getComponent();
			localTransferHandler.importData(c, dropEvent.getTransferable());
			dropEvent.acceptDrop(dropEvent.getDropAction());
		} else {
			dropEvent.rejectDrop();
		}
	}
}

