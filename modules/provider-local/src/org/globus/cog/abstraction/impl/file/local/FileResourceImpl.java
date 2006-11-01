// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.FileResourceUtil;
import org.globus.cog.abstraction.impl.file.GeneralException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.impl.file.PermissionsImpl;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.GridResource;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Permissions;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;

/**
 * enables access to local file system through the file resource interface
 * Supports absolute and relative path names
 */
public class FileResourceImpl implements FileResource {
    private final int type = GridResource.FILE;
    private Hashtable attributes = null;
    private final String protocol = FileResource.Local;
    private SecurityContext securityContext = null;
    private ServiceContact serviceContact = null;
    private String name = null;
    private File resource = null;
    private Identity identity = null;
    static Logger logger = Logger.getLogger(FileResourceImpl.class.getName());

    public FileResourceImpl() {
        identity = new IdentityImpl();
        attributes = new Hashtable();
    }

    public FileResourceImpl(String name) {
        this();
        this.name = name;
    }

    /** set name for the file resource */
    public void setName(String name) {
        this.name = name;
    }

    /** get name of the file resource */
    public String getName() {
        return name;
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

    /** return protocol ="file" */
    public String getProtocol() {
        return this.protocol;
    }

    /** set security context for the file resource */
    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    /** get security context of the file resource */
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    /** set service contact */
    public void setServiceContact(ServiceContact serviceContact) {
        this.serviceContact = serviceContact;
    }

    /** get service contact */
    public ServiceContact getServiceContact() {
        return serviceContact;
    }

    /** set user's home directory as the current directory */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, GeneralException {
        try {
            setCurrentDirectory(new File(".").getAbsoluteFile().toURI()
                    .getPath());
        } catch (Exception e) {
            throw new GeneralException(
                    "Exception in local Fileresourceimpl start()", e);
        }

    }

    /** close the file */
    public void stop() throws GeneralException {
        resource = null;
    }

    public boolean isStarted() {
        return resource != null;
    }

    /** equivalent to cd */
    public void setCurrentDirectory(String directory)
            throws DirectoryNotFoundException, GeneralException {
        try {

            if (this.isDirectory(resolveName(directory))) {
                resource = new File(resolveName(directory));
            } else {
                throw new DirectoryNotFoundException(
                        "Directory does not exist: " + directory);
            }
        } catch (Exception e) {
            throw new GeneralException("Exception in setCurrentDirectory ", e);
        }
    }

    /** return current path */
    public String getCurrentDirectory() throws GeneralException {
        return resource.toURI().getPath();
    }

    /**
     * This method checks to see if the given name is an absolute or a relative
     * path name. If its relative appends current path to it.
     */
    private String resolveName(String fileName) throws Exception {
        File newFile = new File(fileName);
        if (newFile.isAbsolute() == true) {
            return fileName;
        } else {
            return (getCurrentDirectory() + File.separator + fileName);
        }
    }

    /** list the contents of the current directory */
    public Collection list() throws GeneralException {
        Vector files = new Vector();
        String fileArray[] = null;
        try {
            fileArray = resource.list();
            for (int i = 0; i < fileArray.length; i++) {
                files.add(createGridFile(fileArray[i]));
            }
        } catch (Exception e) {
            throw new GeneralException("Could not list directory contents");
        }
        return files;
    }

    /** list contents of the given directory */
    public Collection list(String directory)
            throws DirectoryNotFoundException, GeneralException {
        try {
            if (!this.isDirectory(resolveName(directory))) {
                throw new DirectoryNotFoundException(
                        "Could not find directory: " + directory);
            }
            String currentPath = getCurrentDirectory();
            resource = new File(resolveName(directory));
            Collection list = list();
            setCurrentDirectory(currentPath);
            return list;
        } catch (IOException ne) {
            throw new DirectoryNotFoundException("Could not find directory: "
                    + directory + ne);
        } catch (Exception e) {
            throw new GeneralException(" Exception in list", e);
        }
    }

    /** make a new directory */
    public void createDirectory(String directory) throws GeneralException {
        try {
            String currentPath = getCurrentDirectory();
            this.resource = new File(resolveName(directory));
            resource.mkdir();
            setCurrentDirectory(currentPath);// [m] ???
        } catch (Exception e) {
            throw new GeneralException("Could not create directory: "
                    + directory);
        }
    }

    public void createDirectories(String directory) throws GeneralException {
        if (directory == null || directory.equals("")) {
            return;
        }
        File f = new File(directory);
        if (!f.mkdirs() && !f.exists()) {
            throw new GeneralException("Failed to create directory: "
                    + directory);
        }
    }

    /** delete the given directory. If force == true, recursive delete */
    public void deleteDirectory(String directory, boolean force)
            throws DirectoryNotFoundException, GeneralException {

        String currentPath = getCurrentDirectory();
        setCurrentDirectory(directory);
        Iterator fileNames = list().iterator();

        while ((fileNames != null) && (fileNames.hasNext()) && (force = true)) {
            File newFile = new File(((GridFile) fileNames.next())
                    .getAbsolutePathName());
            if (newFile.isFile() == true) {
                newFile.delete();
            } else {
                deleteDirectory(newFile.getAbsolutePath(), force);
            }
        }

        resource.delete();
        setCurrentDirectory(currentPath);
    }

    /** remove a file */
    public void deleteFile(String fileName) throws FileNotFoundException,
            GeneralException {
        try {

            File localFile = new File(resolveName(fileName));
            if (!localFile.exists()) {
                throw new FileNotFoundException(fileName + " not found.");
            }

            if (isDirectory(fileName) == true) {
                throw new GeneralException("File is a directory ");
            }

            localFile.delete();
        } catch (Exception e) {
            throw new GeneralException("Exception in deleting file ", e);
        }
    }

    /** copy a file */
    public void getFile(String remoteFileName, String localFileName)
            throws FileNotFoundException, GeneralException {
        try {
            File remote = new File(resolveName(remoteFileName));
            if (!remote.exists()) {
                throw new FileNotFoundException("File not found: "
                        + remote.getAbsolutePath());
            }
            File local = new File(resolveName(localFileName));
            // silently ignore requests for which source == destination
            if (remote.getCanonicalPath().equals(local.getCanonicalPath())) {
                return;
            }
            FileInputStream remoteStream = new FileInputStream(remote);
            FileOutputStream localStream = new FileOutputStream(local);
            int read;
            while ((read = remoteStream.read()) != -1) {
                localStream.write(read);
            }
            remoteStream.close();
            localStream.close();
        } catch (Exception e) {
            throw new GeneralException("Exception in getFile ", e);
        }

    }

    /** copy a file */
    public void putFile(String localFileName, String remoteFileName)
            throws FileNotFoundException, GeneralException {
        getFile(localFileName, remoteFileName);
    }

    /** copy a directory */
    public void getDirectory(String remoteDirName, String localDirName)
            throws DirectoryNotFoundException, GeneralException {

        File localDir = new File(localDirName);
        GridFile gridFile = null;
        if (!localDir.exists()) {
            localDir.mkdir();
        }

        try {
            Iterator fileNames = list(remoteDirName).iterator();

            while ((fileNames != null) && (fileNames.hasNext())) {
                gridFile = (GridFile) fileNames.next();
                if (gridFile.isFile() == true) {
                    getFile(gridFile.getAbsolutePathName(), localDirName
                            + File.separator + gridFile.getName());
                } else {
                    getDirectory(gridFile.getAbsolutePathName(), localDirName
                            + File.separator + gridFile.getName());
                }
            }

        } catch (DirectoryNotFoundException de) {
            throw new DirectoryNotFoundException(
                    "Directory not found in getdir " + de);
        } catch (Exception e) {
            throw new GeneralException("Exception in getdir ", e);
        }
    }

    /** copy a directory */
    public void putDirectory(String localDirName, String remoteDirName)
            throws DirectoryNotFoundException, GeneralException {
        getDirectory(localDirName, remoteDirName);
    }

    /** copy multiple files */
    public void getMultipleFiles(String[] remoteFileNames,
            String[] localFileNames) throws FileNotFoundException,
            GeneralException {
        if (remoteFileNames.length != localFileNames.length) {
            throw new GeneralException(
                    "Number of source files are not equal to the number of destination files");
        }
        for (int i = 0; i < remoteFileNames.length; i++) {
            getFile(remoteFileNames[i], localFileNames[i]);
        }
    }

    /** copy multiple files */
    public void getMultipleFiles(String[] remoteFileNames, String localDirName)
            throws FileNotFoundException, DirectoryNotFoundException,
            GeneralException {
        for (int i = 0; i < remoteFileNames.length; i++) {
            File newFile = new File(remoteFileNames[i]);
            getFile(remoteFileNames[i], localDirName + newFile.getName());
        }
    }

    /** copy multiple files */
    public void putMultipleFiles(String[] localFileNames,
            String[] remoteFileNames) throws FileNotFoundException,
            GeneralException {
        getMultipleFiles(localFileNames, remoteFileNames);
    }

    /** copy multiple files */
    public void putMultipleFiles(String[] localFileNames, String remoteDirName)
            throws FileNotFoundException, DirectoryNotFoundException,
            GeneralException {
        getMultipleFiles(localFileNames, remoteDirName);
    }

    /**
     * rename a file.
     */
    public void rename(String remoteFileName1, String remoteFileName2)
            throws FileNotFoundException, GeneralException {
        File file1 = new File(remoteFileName1);
        File file2 = new File(remoteFileName2);
        if (file1.renameTo(file2) == false) {
            throw new GeneralException(
                    "rename in local file resource impl failed. reasons unknown");
        }
    }

    /** chmod on a file. not implemented for local resource */
    public void changeMode(String filename, int mode)
            throws FileNotFoundException, GeneralException {
        throw new GeneralException("Not implemented for local file resource");
    }

    /** chmod for the gridFile. not implemented for local resource */
    public void changeMode(GridFile newGridFile)
            throws FileNotFoundException, GeneralException {
        throw new GeneralException("Not implemented for local file resource");
    }

    /** get file information */
    public GridFile getGridFile(String fileName)
            throws FileNotFoundException, GeneralException {
        return createGridFile(fileName);
    }

    /** return true of file exists */
    public boolean exists(String filename) throws FileNotFoundException,
            GeneralException {
        try {

            File tempFile = new File(resolveName(filename));
            return tempFile.exists();
        } catch (IOException ioe) {
            throw new FileNotFoundException("File not found ", ioe);
        } catch (Exception e) {
            throw new GeneralException("Exception in exists ", e);
        }

    }

    /** return true if input is a directory */
    public boolean isDirectory(String dirName) throws GeneralException {
        try {
            File isDir = new File(resolveName(dirName));
            return isDir.isDirectory();
        } catch (Exception e) {
            throw new GeneralException("Exception in local isDirectory ", e);
        }
    }

    /** submit a workflow to local resource. not implemented */
    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException {
    }

    /** set an attribute */
    public void setAttribute(String name, Object value) {
        attributes.put(name.toLowerCase(), value);
    }

    public Enumeration getAllAttributes() {
        return this.attributes.elements();
    }

    /** get an attribute */
    public Object getAttribute(String name) {
        return attributes.get(name.toLowerCase());
    }

    /** obtain file information in the form of a gridfile */
    private GridFile createGridFile(Object obj) throws GeneralException {
        String fileName = (String) obj;
        GridFile gridFile = new GridFileImpl();
        try {

            File fileInfo = new File(resolveName(fileName));
            gridFile.setAbsolutePathName(fileInfo.getAbsolutePath());
            gridFile.setLastModified(String.valueOf(new Date(fileInfo
                    .lastModified())));

            if (fileInfo.isFile() == true) {
                gridFile.setFileType(GridFile.FILE);
            }
            if (fileInfo.isDirectory() == true) {
                gridFile.setFileType(GridFile.DIRECTORY);
            }

            gridFile.setName(fileInfo.getName());
            gridFile.setSize(fileInfo.length());

            Permissions userPermissions = new PermissionsImpl();
            Permissions groupPermissions = new PermissionsImpl();
            Permissions allPermissions = new PermissionsImpl();
            gridFile.setUserPermissions(userPermissions);
            gridFile.setGroupPermissions(groupPermissions);
            gridFile.setAllPermissions(allPermissions);
        } catch (Exception e) {
            throw new GeneralException("Exception in creating grid file", e);
        }
        return gridFile;
    }

}
