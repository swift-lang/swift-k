//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.dnd;
/*
 * Transferable implementation for DesktopIcon objects
 */

//Local imports
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;

import org.globus.cog.gridface.impl.desktop.icons.DesktopIconGroup;

public class DesktopIconTransferable implements Transferable,Serializable {

	//According to Drag-n-Drop tutorial, I can only transfer data between
	//my application and not outside when using the mime types..
	public static DataFlavor groupIconDataFlavor =
		createDesktopIconGroupFlavor();
	

	//The following will enable application to application drag and drop
	//but requires Serialization for all interacting classes
	//		public static DataFlavor groupIconDataFlavor = new DataFlavor(DesktopIconGroup.class,
	//			"DesktopIconGroup_Vector");

	/** flavors supported */
	private static DataFlavor[] flavors = { groupIconDataFlavor };

	/** icons being transfered */
	private DesktopIconGroup myIcons;

	DesktopIconTransferable(DesktopIconGroup icons) {
		this.myIcons = icons;
	}

	public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException, IOException {
		if (!isDataFlavorSupported(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		return myIcons;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(flavor)){
				return true;
			}
		}
		return false;
	}

	private static DataFlavor createDesktopIconGroupFlavor() {
		return new DataFlavor(
			DataFlavor.javaJVMLocalObjectMimeType
				+ ";class=org.globus.cog.gridface.impl.desktop.icons.DesktopIconGroup",
			"DesktopIconGroup_Vector");

			
	}
}
