
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.directorybrowser;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import org.globus.cog.abstraction.interfaces.Identity;

public class DirectoryBrowserTransferable implements Transferable, Serializable {
	
	private DirectoryBrowserTransferHolder dataHolder;

	private DataFlavor directoryBrowserDataFlavor = DirectoryBrowserTransferHolder.getDataFlavor();
	
	public DirectoryBrowserTransferable(URI draggedURI, String fileName, Identity sessionId, boolean dirCopy) {
		dataHolder = new DirectoryBrowserTransferHolder();
		dataHolder.setSessionId(sessionId);
		dataHolder.setURI(draggedURI);
		dataHolder.setFileName(fileName);
		dataHolder.setDirCopy(dirCopy);
	}
	
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { directoryBrowserDataFlavor, DataFlavor.stringFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if(flavor.equals(directoryBrowserDataFlavor))
			return true;
		if(flavor.equals(DataFlavor.stringFlavor))
			return true;
		return false;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
		}
		if(flavor.equals(directoryBrowserDataFlavor))
			return dataHolder;
		if(flavor.equals(DataFlavor.stringFlavor))
			return dataHolder.getURI().toString();
		return null;
	}
}
