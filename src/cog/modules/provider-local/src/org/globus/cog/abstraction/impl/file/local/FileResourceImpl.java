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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.AbstractFileResource;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.impl.file.PermissionsImpl;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Permissions;

/**
 * enables access to local file system through the file resource interface
 * Supports absolute and relative path names
 */
public class FileResourceImpl extends AbstractFileResource {
    private File resource = null;
    public static final Logger logger = Logger
            .getLogger(FileResourceImpl.class.getName());

    public int ltoken;

    public FileResourceImpl() {
        super();
    }

    public FileResourceImpl(String name) {
        super(name, FileResource.Local, null, null);
    }

    /** set user's home directory as the current directory */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, FileResourceException {
        setCurrentDirectory(new File(".").getAbsoluteFile().toURI().getPath());
        setStarted(true);
    }

    /** close the file */
    public void stop() {
        resource = null;
        setStarted(false);
    }

    /** equivalent to cd */
    public void setCurrentDirectory(String directory)
            throws FileResourceException {
        if (this.isDirectory(resolveName(directory))) {
            resource = new File(resolveName(directory));
        } else {
            throw new DirectoryNotFoundException("Directory does not exist: "
                    + directory);
        }
    }

    /** return current path */
    public String getCurrentDirectory() {
        return resource.toURI().getPath();
    }

    /**
     * This method checks to see if the given name is an absolute or a relative
     * path name. If its relative appends current path to it.
     */
    private String resolveName(String fileName) {
        File newFile = new File(fileName);
        if (newFile.isAbsolute() == true) {
            return fileName;
        } else {
            return (getCurrentDirectory() + File.separator + fileName);
        }
    }

    /** list the contents of the current directory */
    public Collection list() {
        ArrayList files = new ArrayList();
        String fileArray[] = null;

        fileArray = resource.list();
        for (int i = 0; i < fileArray.length; i++) {
            files.add(createGridFile(fileArray[i]));
        }

        return files;
    }

    /** list contents of the given directory */
    public Collection list(String directory) throws FileResourceException {
        if (!this.isDirectory(resolveName(directory))) {
            throw new DirectoryNotFoundException("Could not find directory: "
                    + directory);
        }
        String currentPath = getCurrentDirectory();
        resource = new File(resolveName(directory));
        Collection list = list();
        setCurrentDirectory(currentPath);
        return list;
    }

    /**
     * make a new directory
     * 
     * @throws FileResourceException
     */
    public void createDirectory(String directory)
            throws FileResourceException {
        String currentPath = getCurrentDirectory();
        this.resource = new File(resolveName(directory));
        resource.mkdir();
        setCurrentDirectory(currentPath);// [m] ???
    }

    public void createDirectories(String directory)
            throws FileResourceException {
        if (directory == null || directory.equals("")) {
            return;
        }
        File f = new File(directory);
        if (!f.mkdirs() && !f.exists()) {
            throw new FileResourceException("Failed to create directory: "
                    + directory);
        }
    }

    /** delete the given directory. If force == true, recursive delete */
    public void deleteDirectory(String directory, boolean force)
            throws FileResourceException {

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
    public void deleteFile(String fileName) throws FileResourceException {
        File localFile = new File(resolveName(fileName));
        if (!localFile.exists()) {
            throw new FileNotFoundException(fileName + " not found.");
        }

        if (isDirectory(fileName) == true) {
            throw new FileResourceException("File is a directory ");
        }

        localFile.delete();
    }

    /** copy a file */
    public void getFile(String remoteFileName, String localFileName)
            throws FileResourceException {

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
        } catch (IOException e) {
            throw new FileResourceException(e);
        }
    }

    /** copy a file */
    public void putFile(String localFileName, String remoteFileName)
            throws FileResourceException {
        getFile(localFileName, remoteFileName);
    }

    /** copy a directory */
    public void getDirectory(String remoteDirName, String localDirName)
            throws FileResourceException {

        File localDir = new File(localDirName);
        GridFile gridFile = null;
        if (!localDir.exists()) {
            localDir.mkdir();
        }

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
    }

    /** copy a directory */
    public void putDirectory(String localDirName, String remoteDirName)
            throws FileResourceException {
        getDirectory(localDirName, remoteDirName);
    }

    /** copy multiple files */
    public void getMultipleFiles(String[] remoteFileNames,
            String[] localFileNames) throws FileResourceException {
        if (remoteFileNames.length != localFileNames.length) {
            throw new IllegalArgumentException(
                    "Number of source files are not equal to the number of destination files");
        }
        for (int i = 0; i < remoteFileNames.length; i++) {
            getFile(remoteFileNames[i], localFileNames[i]);
        }
    }

    /** copy multiple files */
    public void getMultipleFiles(String[] remoteFileNames, String localDirName)
            throws FileResourceException {
        for (int i = 0; i < remoteFileNames.length; i++) {
            File newFile = new File(remoteFileNames[i]);
            getFile(remoteFileNames[i], localDirName + newFile.getName());
        }
    }

    /** copy multiple files */
    public void putMultipleFiles(String[] localFileNames,
            String[] remoteFileNames) throws FileResourceException {
        getMultipleFiles(localFileNames, remoteFileNames);
    }

    /** copy multiple files */
    public void putMultipleFiles(String[] localFileNames, String remoteDirName)
            throws FileResourceException {
        getMultipleFiles(localFileNames, remoteDirName);
    }

    /**
     * rename a file.
     */
    public void rename(String remoteFileName1, String remoteFileName2)
            throws FileResourceException {
        File file1 = new File(remoteFileName1);
        File file2 = new File(remoteFileName2);
        if (file1.renameTo(file2) == false) {
            throw new FileResourceException(
                    "rename in local file resource impl failed. reasons unknown");
        }
    }

    /** chmod on a file. not implemented for local resource */
    public void changeMode(String filename, int mode)
            throws FileResourceException {
        throw new UnsupportedOperationException(
                "Not implemented for local file resource");
    }

    /** chmod for the gridFile. not implemented for local resource */
    public void changeMode(GridFile newGridFile) throws FileResourceException {
        throw new UnsupportedOperationException(
                "Not implemented for local file resource");
    }

    /** get file information */
    public GridFile getGridFile(String fileName) throws FileResourceException {
        return createGridFile(fileName);
    }

    /** return true of file exists */
    public boolean exists(String filename) throws FileResourceException {
        File tempFile = new File(resolveName(filename));
        return tempFile.exists();
    }

    /** return true if input is a directory */
    public boolean isDirectory(String dirName) throws FileResourceException {
        File isDir = new File(resolveName(dirName));
        return isDir.isDirectory();
    }

    /** submit a workflow to local resource. not implemented */
    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException {
    }

    /** obtain file information in the form of a gridfile */
    private GridFile createGridFile(Object obj) {
        String fileName = (String) obj;
        GridFile gridFile = new GridFileImpl();

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

        return gridFile;
    }

}
