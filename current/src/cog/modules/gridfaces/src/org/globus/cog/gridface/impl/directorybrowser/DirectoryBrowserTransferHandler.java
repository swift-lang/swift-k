
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.directorybrowser;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.gridface.interfaces.GridCommand;

public class DirectoryBrowserTransferHandler extends TransferHandler {
	
	private DataFlavor directoryBrowserDataFlavor = DirectoryBrowserTransferHolder.getDataFlavor();
	
	private DirectoryBrowserImpl db = null;
	
	public DirectoryBrowserTransferHandler(DirectoryBrowserImpl db) {
		this.db = db;
	}
	
	public void exportAsDrag(JComponent c, InputEvent e, int action) {
		super.exportAsDrag(c, e, action);
	}
	
	public int getSourceActions(JComponent c) {
	  	return DnDConstants.ACTION_COPY_OR_MOVE;
	  }
	
	/**
	 * This creates the object that is getting transferred from one component to another.
	 * 
	 */
	protected Transferable createTransferable(JComponent c) {
		boolean dirCopy = db.getWorker().getGridFileFromTreePath(db.getSelectedItemsTreePath()).isDirectory();
		return new DirectoryBrowserTransferable(db.getSelectedURI(), db.getSelected(), db.getFileTransferObject().getSessionId(), dirCopy);
	}
	
	
	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		for(int i =0; i<flavors.length; i++) {
			if(directoryBrowserDataFlavor.equals(flavors[i]))
				return true;
		}
		return true;
	}
	
	
	public boolean importData(JComponent comp, Transferable t){
		DirectoryBrowserTransferHolder dbt = null;
		try {
			 dbt = (DirectoryBrowserTransferHolder) t.getTransferData(directoryBrowserDataFlavor);
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		URI dragURI = dbt.getURI();
		String fileName = dbt.getFileName();
		Identity dragSessionId = dbt.getSessionId();
		boolean dirCopy = dbt.getDirCopy();
		
		//create the drop URI by putting the closest directory to the selected items name together
		//with the name of the file being transferred
		URI dropURI = db.getSelectedItemsDirURI();

		//9/21/04 
		//BEFORE:
		//dropURI = dropURI.resolve(fileName);
		//Problem: this did not keep the double // for path
		//NOW:
		try{
		dropURI = new URI(dropURI.toString()+fileName);
		}catch(URISyntaxException es){
			es.printStackTrace();
		}
		//
		
		
		Identity dropSessionId = db.getFileTransferObject().getSessionId();

		//The first two arguments come from the transferable that we were given
		//The second two arguements come from the db instance we have a reference to
		GridCommand copyCommand = CopyCommander.copy(dragURI, dragSessionId, dropURI, dropSessionId, dirCopy);
		
		//After the copycommander has prepared the appropriate commands for us
		//we add ourselves as the listener so that we can be notified when the command finishes
		//and refresh the appropriate node
		copyCommand.addStatusListener(db.getWorker());
		
		TreeNode node =  (TreeNode) db.getSelectedItemsTreePath().getLastPathComponent();
		TreePath path;
		
		//we want the selected node if we've dropped a file on directory
		if(node.getAllowsChildren()) {
			path = db.getSelectedItemsTreePath();
		}
		//otherwise we want the parent of the selected node
		else {
			path = db.getSelectedItemsTreePath().getParentPath();
		}
		//add the path to the hashtable
		db.getWorker().addCommand(copyCommand, path);
		try {
			db.getFileTransferObject().execute(copyCommand, true);
		} catch (Exception e2) {
			return false;
		}
		db.notifyUser("Transfer started.");
		return true;
	}
	
	protected void exportDone(JComponent source,  Transferable data,  int action) {
	}

}
