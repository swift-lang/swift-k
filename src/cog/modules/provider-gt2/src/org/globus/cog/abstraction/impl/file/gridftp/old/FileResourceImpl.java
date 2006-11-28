// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file.gridftp.old;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.AbstractFileResource;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.impl.file.gridftp.DataChannelAuthenticationType;
import org.globus.cog.abstraction.impl.file.gridftp.DataChannelProtectionType;
import org.globus.cog.abstraction.impl.file.gridftp.GridFTPSecurityContext;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Permissions;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.DataSink;
import org.globus.ftp.DataSource;
import org.globus.ftp.FileInfo;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.MarkerListener;
import org.globus.ftp.Session;
import org.globus.ftp.exception.FTPException;
import org.globus.ftp.exception.ServerException;
import org.ietf.jgss.GSSCredential;

/**
 * Implements FileResource API for accessing gridftp server Supports relative
 * and absolute path names
 */
public class FileResourceImpl extends AbstractFileResource {
    public static final Logger logger = Logger
            .getLogger(FileResourceImpl.class);

    /**
     * By default JGlobus sets this to 6000 ms. Experience has proved that it
     * may be too low.
     */
    public static final int MAX_REPLY_WAIT_TIME = 12000; // ms

    private GridFTPClient gridFTPClient;

    /** throws InvalidProviderException */
    public FileResourceImpl() throws Exception {
        this(null, new ServiceContactImpl(), AbstractionFactory
                .newSecurityContext("GridFTP"));
    }

    /** constructor be used normally */
    public FileResourceImpl(String name, ServiceContact serviceContact,
            SecurityContext securityContext) {
        super(name, FileResource.GridFTP, serviceContact, securityContext);
    }

    /**
     * Create the gridFTPClient and authenticate with the resource.
     * 
     * @throws FileResourceException
     * @throws FileResourceException
     */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, IOException, FileResourceException {

        try {
            String host = getServiceContact().getHost();
            int port = getServiceContact().getPort();
            if (port == -1) {
                port = 2811;
            }
            gridFTPClient = new GridFTPClient(host, port);
            gridFTPClient.setClientWaitParams(MAX_REPLY_WAIT_TIME,
                    Session.DEFAULT_WAIT_DELAY);
            GSSCredential proxy = (GSSCredential) getSecurityContext()
                    .getCredentials();
            gridFTPClient.authenticate(proxy);
            gridFTPClient.setType(Session.TYPE_IMAGE);

            setSecurityOptions(gridFTPClient);

            setStarted(true);
        } catch (ServerException se) {
            throw new FileResourceException(
                    "Error while communicating with the GridFTP server", se);
        }
    }

    protected void setSecurityOptions(GridFTPClient client)
            throws ServerException, IOException {
        DataChannelAuthenticationType dcau = GridFTPSecurityContext
                .getDataChannelAuthentication(getSecurityContext());
        if (dcau != null) {
            if (dcau.equals(DataChannelAuthenticationType.NONE)) {
                client
                        .setDataChannelAuthentication(DataChannelAuthentication.NONE);
            } else if (dcau.equals(DataChannelAuthenticationType.SELF)) {
                client
                        .setDataChannelAuthentication(DataChannelAuthentication.SELF);
            }
        }
        DataChannelProtectionType prot = GridFTPSecurityContext
                .getDataChannelProtection(getSecurityContext());
        if (prot != null) {
            if (prot.equals(DataChannelProtectionType.CLEAR)) {
                client
                        .setDataChannelProtection(GridFTPSession.PROTECTION_CLEAR);
            } else if (prot.equals(DataChannelProtectionType.CONFIDENTIAL)) {
                client
                        .setDataChannelProtection(GridFTPSession.PROTECTION_CONFIDENTIAL);
            } else if (prot.equals(DataChannelProtectionType.PRIVATE)) {
                client
                        .setDataChannelProtection(GridFTPSession.PROTECTION_PRIVATE);
            } else if (prot.equals(DataChannelProtectionType.SAFE)) {
                client.setDataChannelProtection(GridFTPSession.PROTECTION_SAFE);
            }
        }
    }

    /**
     * Stop the gridFTPClient from connecting to the server
     * 
     * @throws FileResourceException
     */
    public void stop() throws IOException, FileResourceException {
        try {
            gridFTPClient.close();
            setStarted(false);
        } catch (ServerException e) {
            throw new FileResourceException(
                    "Error stopping the GridFTP server", e);
        }
    }

    /** Equivalent to cd command */
    public void setCurrentDirectory(String directory)
            throws DirectoryNotFoundException, IOException {
        try {
            gridFTPClient.changeDir(directory);
        } catch (ServerException ie) {
            throw new DirectoryNotFoundException(directory
                    + " is not a valid directory", ie);
        }
    }

    /**
     * Return Current Directory's name
     * 
     * @throws FileResourceException
     */
    public String getCurrentDirectory() throws IOException,
            FileResourceException {
        try {
            return gridFTPClient.getCurrentDir();
        } catch (ServerException e) {
            throw new FileResourceException("Cannot get the current directory",
                    e);
        }
    }

    /** Equivalent to ls command in the current directory */
    public Collection list() throws FileResourceException, IOException {

        Vector gridFileList = new Vector();
        try {
            gridFTPClient.setPassiveMode(true);
            Enumeration list = gridFTPClient.list().elements();
            while (list.hasMoreElements()) {
                gridFileList.add(createGridFile(list.nextElement()));
            }
            return gridFileList;

        } catch (FTPException e) {
            throw new FileResourceException(
                    "Cannot list the elements of the current directory", e);
        }
    }

    /** Equivalent to ls command on the given directory */
    public Collection list(String directory) throws FileResourceException,
            IOException {
        // Store currentDir
        String currentDirectory = getCurrentDirectory();
        // Change directory
        setCurrentDirectory(directory);

        Collection list = list();
        // restore original directory
        setCurrentDirectory(currentDirectory);

        return list;
    }

    /** Equivalent to mkdir */
    public void createDirectory(String directory) throws FileResourceException,
            IOException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("createDirectory(" + directory + ")");
            }
            gridFTPClient.makeDir(directory);
        } catch (ServerException e) {
            throw new FileResourceException("Cannot create directory "
                    + directory, e);
        }
    }

    /**
     * Remove directory and its files if force = true. Else remove directory
     * only if empty
     */
    public void deleteDirectory(String directory, boolean force)
            throws DirectoryNotFoundException, FileResourceException,
            IOException {

        GridFile gridFile = null;

        if (!isDirectory(directory)) {
            throw new DirectoryNotFoundException(directory
                    + " is not a valid directory");
        }

        try {
            if (force == true) {
                for (Iterator iterator = list(directory).iterator(); iterator
                        .hasNext();) {
                    gridFile = (GridFile) iterator.next();
                    if (gridFile.isFile()) {
                        gridFTPClient.deleteFile(directory + "/"
                                + gridFile.getName());
                    } else {
                        if (!(gridFile.getName().equals(".") || gridFile
                                .getName().equals(".."))) {
                            deleteDirectory(directory + "/"
                                    + gridFile.getName(), force);
                        }
                    }

                }
            }
            gridFTPClient.deleteDir(directory);
        } catch (ServerException e) {
            throw new FileResourceException(
                    "Cannot delete the given directory", e);
        }
    }

    /** Equivalent to rm file command */
    public void deleteFile(String file) throws FileNotFoundException,
            IOException {
        try {
            gridFTPClient.deleteFile(file);
        } catch (ServerException e) {
            throw new FileNotFoundException("Cannot delete the given file", e);
        }
    }

    /** get a remote file to the local stream */
    public void get(String remoteFileName, DataSink sink,
            MarkerListener mListener) throws FileResourceException, IOException {
        String currentDirectory = getCurrentDirectory();
        try {
            gridFTPClient.setPassiveMode(true);
            gridFTPClient.get(remoteFileName, sink, mListener);
        } catch (FTPException e) {
            throw new FileResourceException("Cannot retrieve the given file", e);
        }
    }

    /** get a remote file */
    public void get(String remoteFileName, File localFile)
            throws FileResourceException, IOException {
        String currentDirectory = getCurrentDirectory();
        try {
            gridFTPClient.setPassiveMode(true);
            gridFTPClient.get(remoteFileName, localFile);
        } catch (FTPException e) {
            throw new FileResourceException("Cannot retrieve the given file", e);
        }

    }

    /** Equivalent to cp/copy command */
    public void getFile(String remoteFileName, String localFileName)
            throws FileResourceException, IOException {
        File localFile = new File(localFileName);
        String currentDirectory = getCurrentDirectory();
        try {
            gridFTPClient.setPassiveMode(true);
            gridFTPClient.get(remoteFileName, localFile);
        } catch (FTPException e) {
            throw new FileResourceException("Exception in getFile", e);
        }
    }

    /** Copy a local file to a remote file. Default option 'overwrite' */
    public void putFile(String localFileName, String remoteFileName)
            throws FileResourceException, IOException {

        String currentDirectory = getCurrentDirectory();
        File localFile = new File(localFileName);
        try {
            gridFTPClient.setPassiveMode(true);
            gridFTPClient.put(localFile, remoteFileName, false);
        } catch (FTPException e) {
            throw new FileResourceException("Cannot transfer the given file", e);
        }
    }

    /** put a local file into remote resource */
    public void put(File localFile, String remoteFileName, boolean append)
            throws FileResourceException, IOException {

        String currentDirectory = getCurrentDirectory();
        try {
            gridFTPClient.setPassiveMode(true);
            gridFTPClient.put(localFile, remoteFileName, append);
        } catch (FTPException e) {
            throw new FileResourceException("Cannot transfer the given file", e);
        }
    }

    /**
     * put the input from a stream into a remote resource. unique to gridftp
     * file resource.
     */
    public void put(DataSource source, String remoteFileName,
            MarkerListener mListener) throws FileResourceException, IOException {
        String currentDirectory = getCurrentDirectory();
        try {
            gridFTPClient.setPassiveMode(true);
            gridFTPClient.put(remoteFileName, source, mListener);
        } catch (FTPException e) {
            throw new FileResourceException("Cannot transfer the given file", e);
        }
    }
    
    /**
     * Rename a remote file.
     */
    public void rename(String remoteFileName1, String remoteFileName2)
            throws FileResourceException, IOException {
        try {
            gridFTPClient.rename(remoteFileName1, remoteFileName2);
        } catch (ServerException e) {
            throw new FileResourceException("Rename for gridftp failed", e);
        }
    }

    /**
     * Changes the permissions on the file if authorized to do so
     */
    public void changeMode(String filename, int mode)
            throws FileResourceException, IOException {
        String cmd = "chmod " + mode + " " + filename; // or something else
        try {
            gridFTPClient.site(cmd);
        } catch (ServerException e) {
            throw new FileResourceException(
                    "Cannot change the file permissions.", e);
        }
    }

    /** Returns true if the file exists */
    public boolean exists(String filename) throws IOException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("exists(" + filename + ")");
            }
            return gridFTPClient.exists(filename);
        } catch (ServerException e) {
            return false;
        }
    }

    /**
     * Is this filename a directory. works if user has permissions to change to
     * the given directory
     */
    public boolean isDirectory(String dirName) throws FileResourceException,
            IOException {
        boolean isDir = true;
        String currentDirectory = getCurrentDirectory();
        try {
            setCurrentDirectory(dirName);
        } catch (Exception e) {
            isDir = false;
        } finally {
            try {
                setCurrentDirectory(currentDirectory);
            } catch (Exception e) {
                // do nothihng
            }
        }
        return isDir;
    }

    /** get remote file information */
    public GridFile getGridFile(String fileName) throws FileResourceException,
            IOException {

        String directory = null;
        int endIndex = fileName.lastIndexOf("/");
        if (endIndex < 0) {
            directory = getCurrentDirectory();
        } else {
            directory = fileName.substring(0, endIndex);
            fileName = fileName.substring(endIndex + 1, fileName.length());
        }

        Iterator gridFiles = list(directory).iterator();

        while (gridFiles.hasNext()) {
            GridFile gridFile = (GridFile) gridFiles.next();
            if (gridFile.getName().equals(fileName)) {
                return gridFile;
            }
        }

        return null;
    }

    /** change permissions to a remote file */
    public void changeMode(GridFile newGridFile) throws FileResourceException,
            IOException {

        String newPermissions = newGridFile.getUserPermissions().toString()
                + newGridFile.getGroupPermissions().toString()
                + newGridFile.getAllPermissions().toString();

        logger.error(newGridFile.getAbsolutePathName());

        changeMode(newGridFile.getAbsolutePathName(), Integer
                .parseInt(newPermissions));
    }

    /** Not implemented in GridFTP * */
    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException {
    }

    /** create the file information object */
    private GridFile createGridFile(Object obj) throws FileResourceException,
            IOException {

        GridFile gridFile = new GridFileImpl();

        FileInfo fileInfo = (FileInfo) obj;

        String directory = getCurrentDirectory();
        if (directory.endsWith("/")) {
            gridFile.setAbsolutePathName(directory + fileInfo.getName());
        } else {
            gridFile.setAbsolutePathName(directory + "/" + fileInfo.getName());
        }

        gridFile.setLastModified(fileInfo.getDate());

        if (fileInfo.isFile() == true) {
            gridFile.setFileType(GridFile.FILE);
        }
        if (fileInfo.isDirectory() == true) {
            gridFile.setFileType(GridFile.DIRECTORY);
        }
        if (fileInfo.isDevice() == true) {
            gridFile.setFileType(GridFile.DEVICE);
        }
        if (fileInfo.isSoftLink() == true) {
            gridFile.setFileType(GridFile.SOFTLINK);
        }

        gridFile.setMode(fileInfo.getModeAsString());
        gridFile.setName(fileInfo.getName());
        gridFile.setSize(fileInfo.getSize());

        Permissions userPermissions = gridFile.getUserPermissions();
        Permissions groupPermissions = gridFile.getGroupPermissions();
        Permissions allPermissions = gridFile.getAllPermissions();

        userPermissions.setRead(fileInfo.userCanRead());
        userPermissions.setWrite(fileInfo.userCanWrite());
        userPermissions.setExecute(fileInfo.userCanExecute());

        groupPermissions.setRead(fileInfo.groupCanRead());
        groupPermissions.setWrite(fileInfo.groupCanWrite());
        groupPermissions.setExecute(fileInfo.groupCanExecute());

        allPermissions.setRead(fileInfo.allCanRead());
        allPermissions.setWrite(fileInfo.allCanWrite());
        allPermissions.setExecute(fileInfo.allCanExecute());

        gridFile.setUserPermissions(userPermissions);
        gridFile.setGroupPermissions(groupPermissions);
        gridFile.setAllPermissions(allPermissions);

        return gridFile;
    }

    private void removeLocalDirectory(String tempDirName) {
        File tempFile = new File(tempDirName);
        String[] fileNames = tempFile.list();
        if (fileNames != null) {
            for (int i = 0; i < fileNames.length; i++) {
                File newFile = new File(tempDirName + File.separator
                        + fileNames[i]);
                if (newFile.isFile() == true) {
                    newFile.delete();
                } else {
                    removeLocalDirectory(newFile.getAbsolutePath());
                }
            }
        }
        tempFile.delete();
    }

    protected GridFTPClient getGridFTPClient() {
        return gridFTPClient;
    }
}