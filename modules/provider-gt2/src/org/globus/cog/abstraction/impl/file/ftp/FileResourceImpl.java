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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.FileResourceUtil;
import org.globus.cog.abstraction.impl.file.GeneralException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.GridResource;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Permissions;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.ftp.FTPClient;
import org.globus.ftp.FileInfo;
import org.globus.ftp.Session;
import org.globus.ftp.exception.ServerException;

/**
 * File resource interface implementation for FTP Servers. Supports relative and
 * absolute path names.
 */
public class FileResourceImpl implements FileResource {
    private ServiceContact serviceContact;
    private final String protocol = FileResource.FTP;
    private SecurityContext securityContext;
    private FTPClient ftpClient;
    private String name = null;
    private Identity identity = null;
    private final int type = GridResource.FILE;
    private Hashtable attributes = null;
    static Logger logger = Logger.getLogger(FileResource.class.getName());
    private boolean started;

    /** throws invalidprovider exception */
    public FileResourceImpl() throws Exception {
        this.identity = new IdentityImpl();
        this.attributes = new Hashtable();
        serviceContact = new ServiceContactImpl();
        securityContext = AbstractionFactory.newSecurityContext("FTP");
    }

    /** the constructor to be used normally */
    public FileResourceImpl(String name, ServiceContact serviceContact,
            SecurityContext securityContext) {
        this.identity = new IdentityImpl();
        this.name = name;
        this.serviceContact = serviceContact;
        this.securityContext = securityContext;
        this.attributes = new Hashtable();
    }

    /** Set the name of the resource */
    public void setName(String name) {
        this.name = name;
    }

    /** Return name of the resource */
    public String getName() {
        return this.name;
    }

    /** set service contact */
    public void setServiceContact(ServiceContact serviceContact) {
        this.serviceContact = serviceContact;
    }

    /** return service contact */
    public ServiceContact getServiceContact() {
        return serviceContact;
    }

    /** Set identity of the resource */
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    /** return identity of the resource */
    public Identity getIdentity() {
        return this.identity;
    }

    /** return type = FILE which is defined in GridResource */
    public int getType() {
        return this.type;
    }

    /** return protocol ="ftp" */
    public String getProtocol() {
        return this.protocol;
    }

    /** Set the appropriate SecurityContext for the FileResource */
    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    /** Get the securityContext for the remote resource */
    public SecurityContext getSecurityContext() {
        return this.securityContext;

    }

    /**
     * Create the ftpClient and authenticate with the resource.
     */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, GeneralException {

        try {
            String host = serviceContact.getHost();
            int port = serviceContact.getPort();
            if (port == -1) {
                port = 21;
            }
            ftpClient = new FTPClient(host, port);
            PasswordAuthentication credentials = (PasswordAuthentication) securityContext
                    .getCredentials();
            String username = credentials.getUserName();
            String password = String.valueOf(credentials.getPassword());

            ftpClient.authorize(username, password);
            ftpClient.setType(Session.TYPE_IMAGE);
            started = true;
        } catch (ServerException se) {
            throw new IllegalHostException(
                    "Error while communicating with the FTP server", se);
        } catch (Exception e) {
            throw new GeneralException("Cannot connect to the FTP server", e);
        }
    }

    /** Stop the ftpClient from connecting to the server */
    public void stop() throws GeneralException {
        try {
            ftpClient.close();
            started = false;
        } catch (Exception e) {
            throw new GeneralException("Error while stopping the FTP server", e);
        }
    }

    public boolean isStarted() {
        return started;
    }

    /** Equivalent to cd command */
    public void setCurrentDirectory(String directory)
            throws DirectoryNotFoundException, GeneralException {
        try {
            ftpClient.changeDir(directory);
        } catch (IOException ie) {
            throw new DirectoryNotFoundException(directory
                    + " is not a valid directory", ie);
        } catch (Exception e) {
            throw new GeneralException("Cannot set the current directory", e);
        }
    }

    /** Return Current path */
    public String getCurrentDirectory() throws GeneralException {
        try {
            return ftpClient.getCurrentDir();
        } catch (Exception e) {
            throw new GeneralException("Cannot get the current directory", e);
        }
    }

    /** Equivalent to ls command in the current directory */
    public Collection list() throws GeneralException {
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

        } catch (Exception e) {
            throw new GeneralException(
                    "Cannot list the elements of the current directory", e);
        }

    }

    /** Equivalent to ls command on the given directory */
    public Collection list(String directory) throws DirectoryNotFoundException,
            GeneralException {

        //	Store currentDir
        String currentDirectory = getCurrentDirectory();
        //	Change directory
        setCurrentDirectory(directory);
        Collection list = null;
        try {
            ftpClient.setType(Session.TYPE_ASCII);
            list = list();
            ftpClient.setType(Session.TYPE_IMAGE);
        } catch (Exception e) {
            throw new GeneralException("Error in list directory", e);
        }

        //	Come back to original directory
        setCurrentDirectory(currentDirectory);
        return list;
    }

    /** Equivalent to mkdir */
    public void createDirectory(String directory) throws GeneralException {
        try {
            ftpClient.makeDir(directory);
        } catch (Exception e) {
            throw new GeneralException("Cannot create the directory", e);
        }
    }
    
    public void createDirectories(String directory) throws GeneralException {
        FileResourceUtil.createDirectories(this, directory);
    }

    /**
     * Remove directory and its files if force = true. Else remove directory
     * only if empty
     */
    public void deleteDirectory(String directory, boolean force)
            throws DirectoryNotFoundException, GeneralException {

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
                        deleteDirectory(directory + "/" + gridFile.getName(), force);
                    }

                }
            }
            if (!list(directory).iterator().hasNext()) {
                ftpClient.deleteDir(directory);
            }
        } catch (Exception e) {
            throw new GeneralException("Cannot delete the given directory", e);
        }
    }

    /** Equivalent to rm command on a file */
    public void deleteFile(String file) throws FileNotFoundException,
            GeneralException {
        try {
            ftpClient.deleteFile(file);
        } catch (IOException ie) {
            throw new FileNotFoundException(file + " is not a valid file", ie);
        } catch (Exception e) {
            throw new GeneralException("Cannot delete the given file", e);
        }
    }

    /** Equivalent to cp/copy command */
    public void getFile(String remoteFilename, String localFileName)
            throws FileNotFoundException, GeneralException {
        String currentDirectory = getCurrentDirectory();
        File localFile = new File(localFileName);
        try {
            ftpClient.setPassive();
            ftpClient.setLocalActive();
            ftpClient.get(remoteFilename, localFile);
        } catch (Exception e) {
            try {
                start();
                setCurrentDirectory(currentDirectory);
            } catch (Exception ge) {
                throw new GeneralException("get Failed. Connection closed", ge);
            }

            throw new GeneralException("Cannot retrieve the given file", e);
        }
    }

    /** Copy a local file to a remote file. Default option 'overwrite' */
    public void putFile(String localFileName, String remoteFileName)
            throws FileNotFoundException, GeneralException {
        String currentDirectory = getCurrentDirectory();
        File localFile = new File(localFileName);
        try {
            ftpClient.setPassive();
            ftpClient.setLocalActive();
            ftpClient.put(localFile, remoteFileName, false);
        } catch (Exception e) {
            try {
                start();
                setCurrentDirectory(currentDirectory);
            } catch (Exception ge) {
                throw new GeneralException("put Failed. Connection closed", ge);
            }

            throw new GeneralException("Cannot transfer the given file", e);
        }
    }

    /** Equivalent to the cp -r command for copying directories */
    public void getDirectory(String remoteDirName, String localDirName)
            throws DirectoryNotFoundException, GeneralException {
        File localDir = new File(localDirName);
        GridFile gridFile = null;

        if (!localDir.exists()) {
            localDir.mkdir();
        }

        if (isDirectory(remoteDirName) == false) {
            throw new DirectoryNotFoundException("Remote directory not found");
        }

        for (Iterator iterator = list(remoteDirName).iterator(); iterator
                .hasNext();) {
            gridFile = (GridFile) iterator.next();
            try {
                if (gridFile.isFile()) {
                    getFile(remoteDirName + "/" + gridFile.getName(),
                            localDirName + File.separator + gridFile.getName());
                } else {
                    getDirectory(remoteDirName + "/" + gridFile.getName(),
                            localDirName + File.separator + gridFile.getName());
                }
            } catch (Exception ie) {
                throw new GeneralException("Cannot transfer directory", ie);
            }
        }
    }

    /** Equivalent to cp -r command for copying directories */
    public void putDirectory(String localDirName, String remoteDirName)
            throws DirectoryNotFoundException, GeneralException {
        File localDir = new File(localDirName);
        if (!localDir.exists()) {
            throw new DirectoryNotFoundException("Local directory not found");
        }

        if (localDir.isFile()) {
            throw new DirectoryNotFoundException(localDirName + "  is a file");
        }

        try {
            if (!exists(remoteDirName)) {
                createDirectory(remoteDirName);
            }
        } catch (FileNotFoundException fe) {
            throw new DirectoryNotFoundException(
                    "Cannot create the remote directory: " + remoteDirName);
        }

        if (!isDirectory(remoteDirName)) {
            throw new DirectoryNotFoundException(remoteDirName + " is a file");
        }

        String files[] = localDir.list();
        for (int index = 0; index < files.length; index++) {
            File localFile = new File(localDirName + File.separator
                    + files[index]);
            try {
                if (!localFile.isDirectory()) {
                    putFile(localDirName + File.separator + files[index],
                            remoteDirName + "/" + files[index]);
                } else {
                    putDirectory(localDirName + File.separator + files[index],
                            remoteDirName + "/" + files[index]);
                }
            } catch (Exception e) {
                throw new GeneralException("Cannot transfer the directory", e);
            }
        }
    }

    /**
     * mget - Obtain multiple files from the remote server
     */
    public void getMultipleFiles(String[] remoteFileNames, String[] localFileNames)
            throws FileNotFoundException, GeneralException {

        //If the list of sources not equal to destination lists then error
        if (localFileNames.length != remoteFileNames.length)
            throw new GeneralException(
                    "Number of source and destination file names has to be the same");

        // Check every remote file name provided. If file, use getfile else use
        // getdir
        for (int index = 0; index < remoteFileNames.length; index++) {
            try {
                if (exists(remoteFileNames[index])) {
                    if (isDirectory(remoteFileNames[index]) == false) {
                        getFile(remoteFileNames[index], localFileNames[index]);
                    } else {
                        getDirectory(remoteFileNames[index], localFileNames[index]);
                    }
                }
            } catch (Exception e) {
                throw new GeneralException("Cannot perform mGet", e);
            }
        }

    }

    /**
     * mget - Obtain multiple files from the ftp server
     */
    public void getMultipleFiles(String[] remoteFileNames, String localDirName)
            throws FileNotFoundException, DirectoryNotFoundException,
            GeneralException {
        for (int index = 0; index < remoteFileNames.length; index++) {
            try {
                // Extract only the file name to be appended to the destination
                // directory
                // in getfile or getdir
                String remoteFileName = remoteFileNames[index]
                        .substring(remoteFileNames[index].lastIndexOf("/") + 1);

                if (exists(remoteFileNames[index])) {
                    //					Check every remote file name provided. If file, use
                    // getfile else use getdir
                    if (isDirectory(remoteFileNames[index]) == false) {

                        getFile(remoteFileNames[index], localDirName
                                + File.separator + remoteFileName);
                    } else {
                        getDirectory(remoteFileNames[index], localDirName
                                + File.separator + remoteFileName);
                    }
                }
            } catch (Exception e) {
                throw new GeneralException("Cannot perform mGet", e);
            }
        }
    }

    /**
     * mput - copy multiple files into the ftp server
     */
    public void putMultipleFiles(String[] localFileNames, String[] remoteFileNames)
            throws FileNotFoundException, GeneralException {
        //If list of sources not equal to list of destinations then error
        if (localFileNames.length != remoteFileNames.length)
            throw new GeneralException(
                    "Number of source and destination file names has to be the same");

        for (int index = 0; index < localFileNames.length; index++) {
            //			Check every local file name provided. If file, use putfile else
            // use putdir
            File localFile = new File(localFileNames[index]);
            try {
                if (!localFile.isDirectory()) {
                    putFile(localFileNames[index], remoteFileNames[index]);
                } else {
                    putDirectory(localFileNames[index], remoteFileNames[index]);
                }
            } catch (Exception e) {
                throw new GeneralException("Cannot perform mput", e);
            }
        }
    }

    /**
     * mput - copy multiple files to the ftp server
     */
    public void putMultipleFiles(String[] localFileNames, String remoteDirName)
            throws FileNotFoundException, DirectoryNotFoundException,
            GeneralException {

        for (int index = 0; index < localFileNames.length; index++) {
            //			Check every remote file name provided. If file, use putfile else
            // use putdir
            File localFile = new File(localFileNames[index]);
            try {
                if (!localFile.isDirectory()) {
                    putFile(localFileNames[index], remoteDirName + "/"
                            + localFile.getName());
                } else {
                    putDirectory(localFileNames[index], remoteDirName + "/"
                            + localFile.getName());
                }
            } catch (Exception e) {
                throw new GeneralException("Cannot perform mput", e);
            }
        }
    }


    /**
     * rename a remote file.
     */
    public void rename(String remoteFileName1, String remoteFileName2)
            throws FileNotFoundException, GeneralException {
        try {
            ftpClient.rename(remoteFileName1, remoteFileName2);
        } catch (IOException ie) {
            throw new FileNotFoundException(
                    "File not found or cannot be renamed", ie);
        } catch (Exception e) {
            throw new GeneralException("Rename for ftp failed", e);
        }
    }

    /**
     * Changes the permissions on the file if authorized to do so
     */
    public void changeMode(String filename, int mode) throws FileNotFoundException,
            GeneralException {
        String cmd = "chmod " + mode + " " + filename; //or something else
        try {
            ftpClient.site(cmd);
        } catch (IOException ie) {
            throw new FileNotFoundException("File " + filename
                    + " is not a valid file", ie);
        } catch (Exception e) {
            throw new GeneralException("Cannot change the file permissions", e);
        }
    }

    /** get file information */
    public GridFile getGridFile(String fileName) throws FileNotFoundException,
            GeneralException {

        try {

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
        } catch (Exception e) {
            throw new GeneralException("Error in getGridFile ", e);
        }
        return null;
    }

    /** change mode for the file if authorized to do so */
    public void changeMode(GridFile newGridFile) throws FileNotFoundException,
            GeneralException {

        String newPermissions = newGridFile.getUserPermissions().toString()
                + newGridFile.getGroupPermissions().toString()
                + newGridFile.getAllPermissions().toString();

        logger.error(newGridFile.getAbsolutePathName());

        changeMode(newGridFile.getAbsolutePathName(), Integer
                .parseInt(newPermissions));
    }

    /** returns true if the file exists */
    public boolean exists(String filename) throws FileNotFoundException,
            GeneralException {
        try {
            return ftpClient.exists(filename);
        } catch (IOException ie) {
            throw new FileNotFoundException("File " + filename
                    + " is not a valid file", ie);
        } catch (Exception e) {
            throw new GeneralException(
                    "Cannot determine the existence of the file", e);
        }
    }

    /**
     * return true if the input is a directory in the ftp resource. works only
     * if you have permissions to change to the specified directory.
     */
    public boolean isDirectory(String dirName) throws GeneralException {
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

    /** execute workflow in ftp resource. not implemented */
    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException {
        throw new TaskSubmissionException(
                "Cannot perform submit. Operation not implemented for ftp");
    }

    /** set attribute for ftp resource */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Enumeration getAllAttributes() {
        return this.attributes.elements();
    }

    /** get attribute */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    private GridFile createGridFile(Object obj) throws GeneralException {

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
                        + (String) fileNames[i]);
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
