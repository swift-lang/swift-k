// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.FileResourceFileFilter;
import org.globus.cog.abstraction.impl.file.IllegalHostException;

/**
 * This interface provides a list of methods that could be used to - establish
 * and maintain connections with remote file servers - browse directories -
 * upload and download files/directories - view and change access permissions
 * 
 * Given the distributed nature of most of the implementations of this
 * interface, errors will occur. Most methods implementing the actual operations
 * throw exceptions that can be divided into two categories:
 * <ol>
 * <li> Exceptions that are caused by semantically invalid arguments or states
 * (FileResourceException)
 * <li> Exceptions that are caused by improper functioning of the mechanism used
 * to implement this interface (IOException).
 * </ol>
 * 
 * Implementations should be careful about how exceptions are handled, because
 * higher level code may rely on the proper distinction between the two.
 * 
 * Additionally some distinction should be provided between fatal and non-fatal
 * errors, where fatal errors are errors which can prevent the resource from
 * further proper functioning and should be handled by restarting the resource
 * (Note: this must be done at the interface level)
 * 
 */
public interface FileResource extends GridResource {

	@Deprecated 
    public static final String FTP = "ftp";
	@Deprecated
    public static final String GridFTP = "gridftp";
	@Deprecated
    public static final String WebDAV = "webdav";
	@Deprecated
    public static final String Local = "local";

    /**
     * Returns the provider protocol implemented by this
     * <code>FileResource</code>
     */
    public String getProtocol();
    
    void setService(Service service);
    
    Service getService();

    /**
     * Sets the service contact for this <code>FileResource</code>
     */
    public void setServiceContact(ServiceContact serviceContact);

    /**
     * Returns the service contact associated with this
     * <code>FileResource</code>
     */
    public ServiceContact getServiceContact();

    /**
     * Sets the security context for this <code>FileResource</code>
     */
    public void setSecurityContext(SecurityContext securityContext);

    /**
     * Returns the security context associated with this
     * <code>FileResource</code>
     */
    public SecurityContext getSecurityContext();

    /**
     * Establishes the connection to the remote service contact
     * 
     * @throws FileResourceException
     */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, FileResourceException;

    /** Closes the connection to the file resource */
    public void stop() throws FileResourceException;

    /**
     * Returns true if a connection to the service has been made
     */
    public boolean isStarted();

    /**
     * Changes the current directory to the given directory
     * 
     * @throws FileResourceException
     */
    public void setCurrentDirectory(String directoryName)
            throws FileResourceException;

    /**
     * Returns the current working directory
     * 
     * @throws FileResourceException
     */
    public String getCurrentDirectory() throws FileResourceException;

    /**
     * Returns the list of files in the current working directory as a
     * collection of GridFile objects
     * 
     * @throws FileResourceException
     */
    public Collection<GridFile> list() throws FileResourceException;

    /**
     * Returns the list of files in the given directory as a
     * collection of GridFile objects
     * 
     * @throws FileResourceException
     */
    public Collection<GridFile> list(String directoryName)
            throws DirectoryNotFoundException, FileResourceException;
    
    public Collection<GridFile> list(String dir, FileResourceFileFilter filter) 
            throws DirectoryNotFoundException, FileResourceException;

    /**
     * Creates a new directory with the given name
     * 
     * @throws FileResourceException
     */
    public void createDirectory(String directoryName)
            throws FileResourceException;

    /**
     * Creates the specified directory and all required directories in the
     * hierarchy if they do not exist
     * 
     * @throws FileResourceException
     */
    public void createDirectories(String directoryName)
            throws FileResourceException;

    /**
     * Deletes the specified directory. If the "force" flag is true, delete
     * non-empty directory too
     * 
     * @throws FileResourceException
     */
    public void deleteDirectory(String directoryName, boolean force)
            throws DirectoryNotFoundException, FileResourceException;

    /**
     * Deletes the given file
     * 
     * @throws FileResourceException
     */
    public void deleteFile(String fileName) throws FileResourceException;

    /**
     * Transfer a <code>remoteFileName</code> file from the file resource and
     * name it as <code>localFileName</code> on the local machine
     * 
     * @throws FileResourceException
     * @deprecated Use {@link #getFile(FileFragment, FileFragment)}
     */
    public void getFile(String remoteFileName, String localFileName)
            throws FileResourceException;
    
    public void getFile(FileFragment remote, FileFragment local) 
            throws FileResourceException;

    /**
     * Retrieve a remote file while providing progress updates. Progress updates
     * are done on a best effort basis, and some implementations may not support
     * this feature.
     * 
     * @param remoteFileName
     *            the path to the source file
     * @param localFileName
     *            the path to the destination file
     * @param progressMonitor
     *            a progress monitor to be used for providing progress
     *            information. Can be <code>null</code>
     * 
     * @throws FileResourceException
     *             in case a problems occurs during the transfer
     * @deprecated use {@link #getFile(FileFragment, FileFragment, ProgressMonitor)}
     */
    public void getFile(String remoteFileName, String localFileName,
            ProgressMonitor progressMonitor) throws FileResourceException;
    
    public void getFile(FileFragment remote, FileFragment local, 
            ProgressMonitor progressMonitor) throws FileResourceException;

    /**
     * Upload the <code>localFileName</code> from the local machine to
     * <code>remoteFileName</code> on the file resource
     * 
     * @throws FileResourceException
     * 
     * @deprecated use {@link #putFile(FileFragment, FileFragment)}
     */
    public void putFile(String localFileName, String remoteFileName)
            throws FileResourceException;
    
    public void putFile(FileFragment local, FileFragment remote) 
            throws FileResourceException;
    
    /**
     * Transfer a local file to the remote resource while providing transfer
     * progress updates. Progress updates are done on a best effort basis, and
     * some implementations may not support this feature.
     * 
     * @param localFileName
     *            the path to the source file
     * @param remoteFileName
     *            the path to the destination file
     * @param progressMonitor
     *            the progress monitor to use for progress updates. Can be
     *            <code>null</code>
     *            
     * @deprecated use {@link #putFile(FileFragment, FileFragment, ProgressMonitor)}
     */
    public void putFile(String localFileName, String remoteFileName,
            ProgressMonitor progressMonitor) throws FileResourceException;

    
    public void putFile(FileFragment local, FileFragment remote, 
            ProgressMonitor progressMonitor) throws FileResourceException;


    /**
     * Transfer the entire directory <code>remoteDirectoryName</code> from the
     * file resource and name it as <code>localDirectoryName</code> on the
     * local machine
     * 
     * @throws FileResourceException
     */
    public void getDirectory(String remoteDirectoryName,
            String localDirectoryName) throws FileResourceException;

    /**
     * Upload the <code>localDirectoryName</code> directory from the local
     * machine to <code>remoteDirectoryName</code> on the file resource
     * 
     * @throws FileResourceException
     */
    public void putDirectory(String localDirectoryName,
            String remoteDirectoryName) throws FileResourceException;

    /**
     * Copy an array of files from the file resource into the local file system
     * 
     * @throws FileResourceException
     */
    public void getMultipleFiles(String[] remoteFileNames,
            String[] localFileNames) throws FileResourceException;

    /**
     * Copy an array of files from the file resource into the given local
     * directory
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public void getMultipleFiles(String[] remoteFileNames,
            String localDirectoryName) throws FileResourceException;

    /**
     * Copy an array of files from the local file system into the file resource
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public void putMultipleFiles(String[] localFileNames,
            String[] remoteFileNames) throws FileResourceException;

    /**
     * Copy an array of files from the local file system into the given remote
     * directory on this file resource
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public void putMultipleFiles(String[] localFileNames,
            String remoteDirectoryName) throws FileResourceException;

    /**
     * Rename a file on the file resource
     * 
     * @throws IOException
     */
    public void rename(String oldFileName, String newFileName)
            throws FileResourceException;

    /**
     * Changes the permissions on the file if authorized to do so
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public void changeMode(String fileName, int mode)
            throws FileResourceException;

    /**
     * Changes the permissions on the file if authorized to do so
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public void changeMode(GridFile gridFile) throws FileResourceException;

    /**
     * Get information of a file from the file resource
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public GridFile getGridFile(String fileName) throws FileResourceException;

    /**
     * Return true if the file exists on the file resource
     * 
     * @throws FileResourceException
     */
    public boolean exists(String fileName) throws FileResourceException;

    /**
     * Return true if the name points to a directory in the file resource
     */
    public boolean isDirectory(String directoryName)
            throws FileResourceException;

    /**
     * Executes a non-interactive workflow of commands on the FileResource
     */
    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException;

    /**
     * Sets attributes for the file resource instance
     */
    public void setAttribute(String name, Object value);

    public Collection<String> getAttributeNames();

    /**
     * Returns attribute value for the given attribute name
     */
    public Object getAttribute(String name);
    
    public InputStream openInputStream(String name) throws FileResourceException;
    
    public OutputStream openOutputStream(String name) throws FileResourceException;
    
    boolean supportsStreams();
    
    boolean supportsPartialTransfers();
    
    boolean supportsThirdPartyTransfers();
    
    void thirdPartyTransfer(FileResource sourceResource, FileFragment source, FileFragment destination) 
            throws FileResourceException;

}
