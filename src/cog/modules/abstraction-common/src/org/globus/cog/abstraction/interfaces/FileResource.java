// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

import java.util.Collection;
import java.util.Enumeration;

import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.GeneralException;
import org.globus.cog.abstraction.impl.file.IllegalHostException;

/**
 * This interface provides a list of methods that could be used to - establish
 * and maintain connections with remote file servers - browse directories -
 * upload and download files/directories - view and change access permissions
 *  
 */
public interface FileResource extends GridResource {

    public static final String FTP = "ftp";
    public static final String GridFTP = "gridftp";
    public static final String WebDAV = "webdav";
    public static final String Local = "local";

    /**
     * Returns the provider protocol implemented by this
     * <code>FileResource</code>
     */
    public String getProtocol();

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

    /** Establishes the connection to the remote service contact */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, GeneralException;

    /** Closes the connection to the file resource */
    public void stop() throws GeneralException;

    /**
     * Returns true if a connection to the service has been made
     */
    public boolean isStarted();

    /**
     * Changes the current directory to the given directory
     */
    public void setCurrentDirectory(String directoryName)
            throws DirectoryNotFoundException, GeneralException;

    /**
     * Returns the current working directory
     */
    public String getCurrentDirectory() throws GeneralException;

    /**
     * Returns the list of files in the current working directory
     */
    public Collection list() throws GeneralException;

    /**
     * Returns the list of files in the given directory
     */
    public Collection list(String directoryName)
            throws DirectoryNotFoundException, GeneralException;

    /** Creates a new directory with the given name */
    public void createDirectory(String directoryName) throws GeneralException;
    
    /**
     * Creates the specified directory and all required directories in the
     * hierarchy if they do not exist
     */
    public void createDirectories(String directoryName) throws GeneralException;

    /**
     * Deletes the specified directory. If the "force" flag is true, delete
     * non-empty directory too
     */
    public void deleteDirectory(String directoryName, boolean force)
            throws DirectoryNotFoundException, GeneralException;

    /**
     * Deletes the given file
     */
    public void deleteFile(String fileName) throws FileNotFoundException,
            GeneralException;

    /**
     * Transfer a <code>remoteFileName</code> file from the file resource and
     * name it as <code>localFileName</code> on the local machine
     */
    public void getFile(String remoteFileName, String localFileName)
            throws FileNotFoundException, GeneralException;

    /**
     * Upload the <code>localFileName</code> from the local machine to
     * <code>remoteFileName</code> on the file resource
     */
    public void putFile(String localFileName, String remoteFileName)
            throws FileNotFoundException, GeneralException;

    /**
     * Transfer the entire directory <code>remoteDirectoryName</code> from the
     * file resource and name it as <code>localDirectoryName</code> on the
     * local machine
     */
    public void getDirectory(String remoteDirectoryName,
            String localDirectoryName) throws DirectoryNotFoundException,
            GeneralException;

    /**
     * Upload the <code>localDirectoryName</code> directory from the local
     * machine to <code>remoteDirectoryName</code> on the file resource
     */
    public void putDirectory(String localDirectoryName,
            String remoteDirectoryName) throws DirectoryNotFoundException,
            GeneralException;

    /**
     * Copy an array of files from the file resource into the local file system
     */
    public void getMultipleFiles(String[] remoteFileNames,
            String[] localFileNames) throws FileNotFoundException,
            GeneralException;

    /**
     * Copy an array of files from the file resource into the given local
     * directory
     */
    public void getMultipleFiles(String[] remoteFileNames,
            String localDirectoryName) throws FileNotFoundException,
            DirectoryNotFoundException, GeneralException;

    /**
     * Copy an array of files from the local file system into the file resource
     */
    public void putMultipleFiles(String[] localFileNames,
            String[] remoteFileNames) throws FileNotFoundException,
            GeneralException;

    /**
     * Copy an array of files from the local file system into the given remote
     * directory on this file resource
     */
    public void putMultipleFiles(String[] localFileNames,
            String remoteDirectoryName) throws FileNotFoundException,
            DirectoryNotFoundException, GeneralException;

    /**
     * Rename a file on the file resource
     */
    public void rename(String oldFileName, String newFileName)
            throws FileNotFoundException, GeneralException;

    /**
     * Changes the permissions on the file if authorized to do so
     */
    public void changeMode(String fileName, int mode)
            throws FileNotFoundException, GeneralException;

    /**
     * Changes the permissions on the file if authorized to do so
     */
    public void changeMode(GridFile gridFile) throws FileNotFoundException,
            GeneralException;

    /**
     * Get information of a file from the file resource
     */
    public GridFile getGridFile(String fileName) throws FileNotFoundException,
            GeneralException;

    /**
     * Return true if the file exists on the file resource
     */
    public boolean exists(String fileName) throws FileNotFoundException,
            GeneralException;

    /**
     * Return true if the name points to a directory in the file resource
     */
    public boolean isDirectory(String directoryName) throws GeneralException;

    /**
     * Executes a non-interactive workflow of commands on the FileResource
     */
    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException;

    /**
     * Sets attributes for the file resource instance
     */
    public void setAttribute(String name, Object value);

    public Enumeration getAllAttributes();

    /**
     * Returns attribute value for the given attribute name
     */
    public Object getAttribute(String name);
}