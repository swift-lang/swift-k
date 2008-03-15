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
import java.util.List;

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
import org.globus.cog.abstraction.interfaces.ProgressMonitor;

/**
 * enables access to local file system through the file resource interface
 * Supports absolute and relative path names
 */
public class FileResourceImpl extends AbstractFileResource {
    public static final Logger logger = Logger
            .getLogger(FileResourceImpl.class);
    
    private File cwd;

    /** This object is used to prevent non-threadsafe use of File.mkdirs. */
    private static Object mkdirsLock = new Object();

    public FileResourceImpl() {
        super();
    }

    public FileResourceImpl(String name) {
        super(name, FileResource.Local, null, null);
    }

    /** set user's home directory as the current directory */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, FileResourceException {
        setCurrentDirectory(new File(".").getAbsoluteFile());
        setStarted(true);
    }

    /** close the file */
    public void stop() {
        setStarted(false);
    }

    /** equivalent to cd */
    public void setCurrentDirectory(String directory)
            throws FileResourceException {
        setCurrentDirectory(new File(directory));
    }
    
    private void setCurrentDirectory(File f) throws FileResourceException {
        f = resolve(f);
        if (!f.exists()) {
            throw new DirectoryNotFoundException("Directory does not exist: " + f.getAbsolutePath());
        }
        if (!f.isDirectory()) {
            throw new DirectoryNotFoundException("Not a directory: " + f.getAbsolutePath());
        }
        if (!f.isAbsolute()) {
            throw new Error("Only absolute paths allowed beyond this point.");
        }
        cwd = f;
    }

    /** return current path */
    public String getCurrentDirectory() {
        return cwd.getAbsolutePath();
    }

    /**
     * This method checks to see if the given name is an absolute or a relative
     * path name. If its relative appends current path to it.
     */
    protected File resolve(File f) {
        if (f.isAbsolute()) {
            return f;
        }
        else {
            return new File(cwd.getAbsolutePath() + File.separatorChar + f.getPath());
        }
    }

    protected File resolve(String sf) {
        File f = new File(sf);
        if (f.isAbsolute()) {
            return f;
        }
        else {
            return new File(cwd.getAbsolutePath() + File.separatorChar + f.getPath());
        }
    }
    /** list the contents of the current directory */
    public Collection list() {
        List files = new ArrayList();
        File[] f = cwd.listFiles();
        for (int i = 0; i < f.length; i++) {
            files.add(createGridFile(f[i]));
        }
        return files;
    }

    /** list contents of the given directory */
    public Collection list(String directory) throws FileResourceException {
        File tcwd = cwd;
        try {
            setCurrentDirectory(new File(directory));
            Collection list = list();
            return list;
        }
        finally {
            setCurrentDirectory(tcwd);
        }
    }

    /**
     * make a new directory
     * 
     * @throws FileResourceException
     */
    public void createDirectory(String directory)
            throws FileResourceException {
        File f = resolve(directory);
        if (!f.mkdir() && !f.exists()) {
            throw new FileResourceException("Failed to create directory: " + directory);
        }
    }

    public void createDirectories(String directory)
            throws FileResourceException {
        if (directory == null || directory.equals("")) {
            return;
        }
        File f = resolve(directory);
        synchronized(mkdirsLock) {
            if (!f.mkdirs() && !f.exists()) {
                throw new FileResourceException("Failed to create directory: " + directory);
            }
        }
    }

    public void deleteDirectory(String dir, boolean force) throws FileResourceException {
        deleteDirectory(resolve(dir), force);
    }
    
    private void deleteDirectory(File f, boolean force) throws FileResourceException {
        File[] fs = f.listFiles();
        if (force) {
            for (int i = 0; i < fs.length; i++) {
                if (fs[i].isFile()) {
                    if (!fs[i].delete()) {
                        throw new FileResourceException("Could not delete directory: "
                        + f.getAbsolutePath() + ". Failed to delete file: " + fs[i].getAbsolutePath());
                    }
                }
                else {
                    deleteDirectory(fs[i], true);
                }
            }
            fs = f.listFiles();
        }
        if (fs.length != 0) {
            throw new FileResourceException("Could not delete directory: "
                    + f.getAbsolutePath() + ". Directory not empty.");
        }
        else {
            if (f.delete()) {
                throw new FileResourceException("Could not delete directory: "
                        + f.getAbsolutePath());
            }
        }
    }
    
    /** remove a file */
    public void deleteFile(String fileName) throws FileResourceException {
        File localFile = resolve(fileName);
        if (!localFile.exists()) {
            throw new FileNotFoundException(fileName + " not found.");
        }
        if (isDirectory(fileName) == true) {
            throw new FileResourceException("File is a directory ");
        }
        if (!localFile.delete()) {
            throw new FileResourceException("Could not delete file " + fileName);
        }
    }

    public void getFile(String remoteFileName, String localFileName)
            throws FileResourceException {
        getFile(remoteFileName, localFileName, null);
    }

    /** copy a file */
    public void getFile(String remoteFileName, String localFileName,
            ProgressMonitor progressMonitor) throws FileResourceException {

        try {
            File src = resolve(remoteFileName);
            if (!src.exists()) {
                throw new FileNotFoundException("File not found: "
                        + src.getAbsolutePath());
            }
            File dst = resolve(localFileName);
            // silently ignore requests for which source == destination
            if (dst.getCanonicalPath().equals(src.getCanonicalPath())) {
                return;
            }
            FileInputStream remoteStream = new FileInputStream(src);
            FileOutputStream localStream = new FileOutputStream(dst);
            long crt = 0;
            long total = src.length();
            byte[] buf = new byte[16384];
            int read;
            while ((read = remoteStream.read(buf)) != -1) {
                localStream.write(buf, 0, read);
                crt += read;
                if (progressMonitor != null) {
                    progressMonitor.progress(crt, total);
                }
            }
            remoteStream.close();
            localStream.close();
        } catch (IOException e) {
            throw new FileResourceException(e);
        }
    }

    public void putFile(String localFileName, String remoteFileName,
            ProgressMonitor progressMonitor) throws FileResourceException {
        getFile(localFileName, remoteFileName, progressMonitor);
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
        return createGridFile(resolve(fileName));
    }

    /** return true of file exists */
    public boolean exists(String filename) throws FileResourceException {
        return resolve(filename).exists();
    }

    /** return true if input is a directory */
    public boolean isDirectory(String dirName) throws FileResourceException {
        return resolve(dirName).isDirectory();
    }

    /** submit a workflow to local resource. not implemented */
    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException {
    }

    /** obtain file information in the form of a gridfile */
    private GridFile createGridFile(File f) {
        GridFile gridFile = new GridFileImpl();

        gridFile.setAbsolutePathName(f.getAbsolutePath());
        gridFile.setLastModified(String.valueOf(new Date(f
                .lastModified())));

        if (f.isFile() == true) {
            gridFile.setFileType(GridFile.FILE);
        }
        if (f.isDirectory() == true) {
            gridFile.setFileType(GridFile.DIRECTORY);
        }

        gridFile.setName(f.getName());
        gridFile.setSize(f.length());

        Permissions userPermissions = new PermissionsImpl();
        Permissions groupPermissions = new PermissionsImpl();
        Permissions allPermissions = new PermissionsImpl();
        gridFile.setUserPermissions(userPermissions);
        gridFile.setGroupPermissions(groupPermissions);
        gridFile.setAllPermissions(allPermissions);

        return gridFile;
    }

}
