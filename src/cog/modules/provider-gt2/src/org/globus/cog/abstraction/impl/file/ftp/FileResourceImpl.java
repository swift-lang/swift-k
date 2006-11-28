// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file.ftp;

import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
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
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Permissions;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.ftp.FTPClient;
import org.globus.ftp.FileInfo;
import org.globus.ftp.Session;
import org.globus.ftp.exception.FTPException;
import org.globus.ftp.exception.ServerException;

/**
 * File resource interface implementation for FTP Servers. Supports relative and
 * absolute path names.
 */
public class FileResourceImpl extends AbstractFileResource {
    private FTPClient ftpClient;
    public static final Logger logger = Logger.getLogger(FileResource.class
            .getName());

    /** throws invalidprovider exception */
    public FileResourceImpl() throws Exception {
        this(null, new ServiceContactImpl(), AbstractionFactory
                .newSecurityContext("FTP"));
    }

    /** the constructor to be used normally */
    public FileResourceImpl(String name, ServiceContact serviceContact,
            SecurityContext securityContext) {
        super(name, FileResource.FTP, serviceContact, securityContext);
    }

    /**
     * Create the ftpClient and authenticate with the resource.
     * 
     * @throws IOException
     */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, IOException {

        try {
            String host = getServiceContact().getHost();
            int port = getServiceContact().getPort();
            if (port == -1) {
                port = 21;
            }
            ftpClient = new FTPClient(host, port);
            PasswordAuthentication credentials = (PasswordAuthentication) getSecurityContext()
                    .getCredentials();
            String username = credentials.getUserName();
            String password = String.valueOf(credentials.getPassword());

            ftpClient.authorize(username, password);
            ftpClient.setType(Session.TYPE_IMAGE);
            setStarted(true);
        } catch (ServerException se) {
            throw new IllegalHostException(
                    "Error while communicating with the FTP server", se);
        }
    }

    /**
     * Stop the ftpClient from connecting to the server
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public void stop() throws IOException, FileResourceException {
        try {
            ftpClient.close();
            setStarted(false);
        } catch (ServerException e) {
            throw new FileResourceException(
                    "Error while stopping the FTP server", e);
        }
    }

    /**
     * Equivalent to cd command
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public void setCurrentDirectory(String directory) throws IOException,
            FileResourceException {
        try {
            ftpClient.changeDir(directory);
        } catch (ServerException e) {
            throw new FileResourceException("Cannot set the current directory",
                    e);
        }
    }

    /**
     * Return Current path
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public String getCurrentDirectory() throws IOException,
            FileResourceException {
        try {
            return ftpClient.getCurrentDir();
        } catch (ServerException e) {
            throw new FileResourceException("Cannot get the current directory",
                    e);
        }
    }

    /**
     * Equivalent to ls command in the current directory
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public Collection list() throws IOException, FileResourceException {
        Vector gridFileList = new Vector();
        try {
            ftpClient.setPassive();
            ftpClient.setLocalActive();
            ftpClient.setType(Session.TYPE_ASCII);

            Enumeration list = ftpClient.list().elements();
            ftpClient.setType(Session.TYPE_IMAGE);

            while (list.hasMoreElements()) {
                gridFileList.add(createGridFile(list.nextElement()));
            }
            return gridFileList;

        } catch (FTPException e) {
            throw new FileResourceException(
                    "Cannot list the elements of the current directory", e);
        }
    }

    /**
     * Equivalent to ls command on the given directory
     * 
     * @throws FileResourceException
     */
    public Collection list(String directory) throws IOException,
            FileResourceException {

        // Store currentDir
        String currentDirectory = getCurrentDirectory();
        // Change directory
        setCurrentDirectory(directory);
        Collection list = null;
        try {
            ftpClient.setType(Session.TYPE_ASCII);
            list = list();
            ftpClient.setType(Session.TYPE_IMAGE);
        } catch (ServerException e) {
            throw new FileResourceException("Error in list directory", e);
        }

        // Come back to original directory
        setCurrentDirectory(currentDirectory);
        return list;
    }

    /** Equivalent to mkdir */
    public void createDirectory(String directory) throws FileResourceException,
            IOException {
        try {
            ftpClient.makeDir(directory);
        } catch (ServerException e) {
            throw new FileResourceException("Cannot create the directory", e);
        }
    }

    /**
     * Remove directory and its files if force = true. Else remove directory
     * only if empty
     */
    public void deleteDirectory(String directory, boolean force)
            throws FileResourceException, IOException {

        GridFile gridFile = null;

        if (!isDirectory(directory)) {
            throw new DirectoryNotFoundException(directory
                    + " is not a valid directory");
        }

        try {
            if (force) {
                for (Iterator iterator = list(directory).iterator(); iterator
                        .hasNext();) {
                    gridFile = (GridFile) (iterator.next());
                    if (gridFile.isFile()) {
                        ftpClient.deleteFile(directory + "/"
                                + gridFile.getName());
                    } else {
                        deleteDirectory(directory + "/" + gridFile.getName(),
                                force);
                    }

                }
            }
            if (!list(directory).iterator().hasNext()) {
                ftpClient.deleteDir(directory);
            }
        } catch (ServerException e) {
            throw new FileResourceException(
                    "Cannot delete the given directory", e);
        }
    }

    /** Equivalent to rm command on a file */
    public void deleteFile(String file) throws FileResourceException,
            IOException {
        try {
            ftpClient.deleteFile(file);
        } catch (Exception e) {
            throw new FileResourceException("Cannot delete the given file", e);
        }
    }

    /** Equivalent to cp/copy command */
    public void getFile(String remoteFilename, String localFileName)
            throws FileResourceException, IOException {
        String currentDirectory = getCurrentDirectory();
        File localFile = new File(localFileName);
        try {
            ftpClient.setPassive();
            ftpClient.setLocalActive();
            ftpClient.get(remoteFilename, localFile);
        } catch (FTPException e) {
            throw new FileResourceException("Cannot retrieve the given file", e);
        }
    }

    /** Copy a local file to a remote file. Default option 'overwrite' */
    public void putFile(String localFileName, String remoteFileName)
            throws FileResourceException, IOException {
        String currentDirectory = getCurrentDirectory();
        File localFile = new File(localFileName);
        try {
            ftpClient.setPassive();
            ftpClient.setLocalActive();
            ftpClient.put(localFile, remoteFileName, false);
        } catch (FTPException e) {
            throw new FileResourceException("Cannot transfer the given file", e);
        }
    }

    /**
     * rename a remote file.
     */
    public void rename(String remoteFileName1, String remoteFileName2)
            throws FileResourceException, IOException {
        try {
            ftpClient.rename(remoteFileName1, remoteFileName2);
        } catch (ServerException e) {
            throw new FileResourceException("Rename for ftp failed", e);
        }
    }

    /**
     * Changes the permissions on the file if authorized to do so
     */
    public void changeMode(String filename, int mode)
            throws FileResourceException, IOException {
        String cmd = "chmod " + mode + " " + filename; // or something else
        try {
            ftpClient.site(cmd);
        } catch (ServerException e) {
            throw new FileResourceException(
                    "Cannot change the file permissions", e);
        }
    }

    /** get file information */
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

        Collection gridFiles = list(directory);
        Iterator iterator = gridFiles.iterator();
        while (iterator.hasNext()) {
            GridFile gridFile = (GridFile) iterator.next();
            if (gridFile.getName().equals(fileName)) {
                return gridFile;
            }
        }
        return null;
    }

    /** change mode for the file if authorized to do so */
    public void changeMode(GridFile newGridFile) throws FileResourceException,
            IOException {

        String newPermissions = newGridFile.getUserPermissions().toString()
                + newGridFile.getGroupPermissions().toString()
                + newGridFile.getAllPermissions().toString();

        logger.error(newGridFile.getAbsolutePathName());

        changeMode(newGridFile.getAbsolutePathName(), Integer
                .parseInt(newPermissions));
    }

    /** returns true if the file exists */
    public boolean exists(String filename) throws FileResourceException,
            IOException {
        try {
            return ftpClient.exists(filename);
        } catch (ServerException e) {
            throw new FileResourceException(
                    "Cannot determine the existence of the file", e);
        }
    }

    /**
     * return true if the input is a directory in the ftp resource. works only
     * if you have permissions to change to the specified directory.
     */
    public boolean isDirectory(String dirName) throws FileResourceException,
            IOException {
        boolean isDir = true;
        String currentDirectory = getCurrentDirectory();
        try {
            setCurrentDirectory(dirName);
        } catch (FileResourceException e) {
            isDir = false;
        } finally {
            try {
                setCurrentDirectory(currentDirectory);
            } catch (Exception e) {
                // do nothihng
                // ???
            }
        }
        return isDir;
    }

    /** execute workflow in ftp resource. not implemented */
    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException {
        throw new TaskSubmissionException(
                "Cannot perform submit. Operation not implemented for ftp");
    }

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

    /** Delete the specified local directory */
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

}
