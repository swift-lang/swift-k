
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.directorybrowser;

import java.net.URI;

import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.gridface.impl.commands.COPYDirCommandImpl;
import org.globus.cog.gridface.impl.commands.COPYFileCommandImpl;
import org.globus.cog.gridface.impl.commands.GETDIRCommandImpl;
import org.globus.cog.gridface.impl.commands.GETFILECommandImpl;
import org.globus.cog.gridface.impl.commands.PUTDIRCommandImpl;
import org.globus.cog.gridface.impl.commands.PUTFILECommandImpl;
import org.globus.cog.gridface.impl.commands.URLCOPYCommandImpl;
import org.globus.cog.gridface.interfaces.GridCommand;


public class CopyCommander {

	private static boolean isThirdPartyCapable(URI uri){
		if(uri.getScheme().equals("gridftp")) {
			return true;
		} else if (uri.getScheme().equals("gsiftp")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 */
	public static GridCommand getFile(URI uriRemote, URI uriLocal) {
		GridCommand command = new GETFILECommandImpl();
		command.addArgument(uriRemote.getPath());
		command.addArgument(uriLocal.getPath());
		return command;
	}
	
	
	/**
	 * 
	 */
	public static GridCommand putFile(URI uriLocal, URI uriRemote){
		GridCommand command = new PUTFILECommandImpl();
		command.addArgument(uriLocal.getPath());
		command.addArgument(uriRemote.getPath());
		return command;
	}
	
	
	public static GridCommand getDir(URI uriRemote, URI uriLocal) {
		GridCommand command = new GETDIRCommandImpl();
		command.addArgument(uriRemote.getPath());
		command.addArgument(uriLocal.getPath());
		return command;
	}
	
	public static GridCommand putDir(URI uriLocal, URI uriRemote) {
		GridCommand command = new PUTDIRCommandImpl();
		command.addArgument(uriLocal.getPath());
		command.addArgument(uriRemote.getPath());
		return command;
	}
	
	public static GridCommand thirdPartyCopyFile(URI uriSource, URI uriDestination) {
		GridCommand command = new URLCOPYCommandImpl();
		command.setAttribute("provider", "GT2");
		
		//Cog4 changes
//		command.setAttribute("sourcehost", uriSource.getScheme()+"://"+uriSource.getHost());
//		if(uriSource.getPort() != -1){
//			command.setAttribute("sourceport",new Integer(uriSource.getPort()).toString());
//		}
//		command.setAttribute("destinationhost", uriDestination.getScheme()+"://"+uriDestination.getHost());
//		if(uriDestination.getPort() != -1){
//			command.setAttribute("destinationport",new Integer(uriDestination.getPort()).toString());
//		}
    	
		command.setAttribute("source", uriSource.toASCIIString());
		command.setAttribute("destination", uriDestination.toASCIIString());
		return command;
	}
	
	public static GridCommand ftpToFtpFileCopy(URI uriSource, URI uriDestination) {
		GridCommand command = new COPYFileCommandImpl();
		command.addArgument(uriSource.getPath());
		command.addArgument(uriDestination.getPath());
		return command;
	}
	
	public static GridCommand ftpToFtpDirCopy(URI uriSource, URI uriDestination) {
		GridCommand command = new COPYDirCommandImpl();
		command.addArgument(uriSource.getPath());
		command.addArgument(uriDestination.getPath());
		return command;
	}
	
	
	
	public static GridCommand copy(URI source, Identity sourceSessionId, 
			URI destination, Identity destinationSessionId, boolean dirCopy){
		
		//are we copying a directory?
		/*boolean dirCopy = false;
		if(source.toASCIIString().endsWith("/"))
			dirCopy = true;*/
		
		if(CopyCommander.isThirdPartyCapable(source) && CopyCommander.isThirdPartyCapable(destination)) {
			GridCommand returnCommand = CopyCommander.thirdPartyCopyFile(source, destination);
			returnCommand.setAttribute("sessionid", sourceSessionId);
			return returnCommand;
			
			//the source is local, so use the destination file transfer object to complete the 
			//transfer
		} else if(source.getScheme().equalsIgnoreCase("file")) {
			GridCommand copyCommand;
			if(dirCopy){
				copyCommand = CopyCommander.putDir(source, destination);
				copyCommand.setAttribute("provider", destination.getScheme());
				copyCommand.setAttribute("sessionid", destinationSessionId);
			}
			else{
				copyCommand = CopyCommander.putFile(source, destination);
				copyCommand.setAttribute("provider", destination.getScheme());
				copyCommand.setAttribute("sessionid", destinationSessionId);
			}
			
			return copyCommand;
			
			//the destination is local, so use the source filetransferobject to complete
			//the transfer
		} else if(destination.getScheme().equalsIgnoreCase("file")) {
			GridCommand copyCommand;
			if(dirCopy){
				copyCommand = CopyCommander.getDir(source, destination);
				copyCommand.setAttribute("provider", source.getScheme());
				copyCommand.setAttribute("sessionid", sourceSessionId);
			}
			else{
				copyCommand = CopyCommander.getFile(source, destination);
				copyCommand.setAttribute("provider", source.getScheme());
				copyCommand.setAttribute("sessionid", sourceSessionId);
			}
			return copyCommand;
			
		} else {
			//This is a copy where we have to transfer it locallay first because we are 
			//not able to transfer the files between two remote servers...ie, two ftp servers
			//or a gsiftp and ftp server
			GridCommand copyCommand;
			if(dirCopy){
				copyCommand = CopyCommander.ftpToFtpDirCopy(source, destination);
			} else {
				copyCommand = CopyCommander.ftpToFtpFileCopy(source, destination);
			}
			copyCommand.setAttribute("sourceProvider", source.getScheme());
			copyCommand.setAttribute("sourceSessionId", sourceSessionId);
			
			copyCommand.setAttribute("destinationProvider", destination.getScheme());
			copyCommand.setAttribute("destinationSessionId", destinationSessionId);
			
			return copyCommand;
		}
	}
}
