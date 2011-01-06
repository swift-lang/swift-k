
//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.directorybrowser;

import java.awt.datatransfer.DataFlavor;
import java.io.Serializable;
import java.net.URI;

import org.globus.cog.abstraction.interfaces.Identity;

public class DirectoryBrowserTransferHolder implements Serializable{
	
	private URI draggedURI;
	private String fileName;
	private transient Identity sessionId;
	private boolean dirCopy;
	
	public static DataFlavor getDataFlavor() {
		return  new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + 
				";class=org.globus.cog.gridface.impl.directorybrowser.DirectoryBrowserTransferHolder", 
				"DirectoryBrowserTransferHolderFlavor");
	}
	
	public URI getURI() {
		return draggedURI;
	}
	
	public void setURI(URI draggedURI){
		this.draggedURI = draggedURI;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName){
		this.fileName = fileName;
	}

	public Identity getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(Identity sessionId) {
		this.sessionId = sessionId;
	}
	
	public void setDirCopy(boolean dirCopy){
		this.dirCopy = dirCopy;
	}
	
	public boolean getDirCopy(){
		return this.dirCopy;
	}
}
