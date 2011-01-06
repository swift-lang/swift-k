
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

import java.net.URI;

import org.globus.cog.abstraction.interfaces.Identity;


public interface FileTransferObject extends ConnectionObject{
	
	public void execute(GridCommand command, boolean backGround) throws Exception;
	
	public void setUsername(String username);
	public void setPassword(String password);
	
	/** Retrieve file properties **/
	public GridCommand getProperties(URI uri);

	/** Set properties of the file pointed by URI **/
	public GridCommand setProperties(URI uri);
	
	public void setSessionId(Identity  identity);
	public Identity getSessionId();

	public GridCommand thirdPartyCopyFile(URI uriSource, URI uriDestination);
	
	/** Get a file. Called through paste *
	 * @throws Exception*/
	public GridCommand getFile(URI uriRemote, URI uriLocal);

	/** Put a file. Called through copy *
	 * @throws Exception*/
	public GridCommand putFile(URI uriLocal, URI uriRemote);

	/** Get a Dir. Called through paste **/
	public GridCommand getDir(URI uriRemote, URI uriLocal);

	/** Put a Dir. Called through copy **/
	public GridCommand putDir(URI uriLocal, URI uriRemote);

	/** Remove file pointed by URI from host**/
	public GridCommand rmfile(URI uri);

	/** Remove directory pointed to URI from host **/
	public GridCommand rmdir(URI uri);

	/** Set Current Directory **/
	public GridCommand setCurrentDirectory(URI uri);

	/** Get Current Directory **/
	public GridCommand getCurrentDirectory();

	/** Make directory **/
	public GridCommand makeDirectory(URI uri);

	/** List all files for current directory **/
	public GridCommand ls();

	/** List all files of specified directory **/
	public GridCommand ls(URI uri);

	/** Check if file or directory specified by the URI exists **/
	public GridCommand exists(URI uri);

	/** Get size of the file specified by URI **/
	public GridCommand size(URI uri);

	/** Is the URI a directory? **/
	public GridCommand isDirectory(URI uri);

}
