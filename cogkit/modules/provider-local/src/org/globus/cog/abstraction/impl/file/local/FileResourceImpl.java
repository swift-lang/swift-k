/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import org.globus.cog.abstraction.interfaces.FileFragment;
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
    
    private static File CWD = new File(".").getAbsoluteFile(); 

    public FileResourceImpl() {
        this(null);
    }

    public FileResourceImpl(String name) {
        super(name, "local", null, null);
    }
    
    protected FileResourceImpl(String name, String protocol) {
        super(name, protocol, null, null);
    }

    /** set user's home directory as the current directory */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, FileResourceException {
        setCurrentDirectory(CWD);
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
    public Collection<GridFile> list() {
        List<GridFile> files = new ArrayList<GridFile>();
        File[] f = cwd.listFiles();
        for (int i = 0; i < f.length; i++) {
            files.add(createGridFile(f[i]));
        }
        return files;
    }

    /** list contents of the given directory */
    public Collection<GridFile> list(String directory)
    throws FileResourceException {
        File tcwd = cwd;
        try {
            setCurrentDirectory(new File(directory));
            Collection<GridFile> list = list();
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

    @Override
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
        /*
         * exists() is funny when there is a broken symbolic link. It returns
         * false. But we want this method to also delete broken symbolic links. 
         */
        /*if (!localFile.exists()) {
            throw new FileNotFoundException(fileName + " not found.");
        }*/
        if (isDirectory(fileName) == true) {
            throw new FileResourceException("File is a directory ");
        }
        if (!localFile.delete()) {
            throw new FileResourceException("Could not delete file " + fileName);
        }
    }

    /** copy a file */
    public void getFile(FileFragment remote, FileFragment local,
            ProgressMonitor progressMonitor) throws FileResourceException {

        try {
            File src = resolve(remote.getFile());
            if (!src.exists()) {
                throw new FileNotFoundException("File not found: "
                        + src.getAbsolutePath());
            }
            File dst = resolve(local.getFile());
            // silently ignore requests for which source == destination
            if (dst.getCanonicalPath().equals(src.getCanonicalPath())) {
                return;
            }
            
            checkParameters(remote, local, src, dst);
            
            FileInputStream remoteStream = null;
            FileOutputStream localStream = null;
            try {
                remoteStream = new FileInputStream(src);
                localStream = new FileOutputStream(dst);
                remoteStream.skip(remote.getOffset());
                
                long crt = 0;
                long total = Math.min(src.length(), remote.getLength());
                if (logger.isDebugEnabled()) {
                    logger.debug(src + ": srclen = " + src.length() 
                        + ", len = " + remote.getLength() + ", total = " + total);
                }
                byte[] buf = new byte[16384];
                do {
                    if (logger.isDebugEnabled()) {
                        logger.debug(src + ": crt = " + crt + ", total - crt = " + (total - crt));
                    }
                    int read = remoteStream.read(buf, 0, (int) Math.min(buf.length, total - crt));
                    localStream.write(buf, 0, read);
                    crt += read;
                    if (progressMonitor != null) {
                        progressMonitor.progress(crt, total);
                    }
                } while (crt < total);
            }
            finally {
                if (remoteStream != null) {
                    remoteStream.close();
                }
                if (localStream != null) {
                    localStream.close();
                }
            }
        } 
        catch (IOException e) {
            throw new FileResourceException(e);
        }
    }

    private void checkParameters(FileFragment srcf, FileFragment dstf, File src, File dst) throws FileResourceException {
        long srcLen = src.length();
        if (srcf.getOffset() > srcLen) {
            throw new FileResourceException("Requested file offset (" 
                + srcf.getOffset() + ") is larger than the file size (" + srcLen + ")");
        }
    }

    public void putFile(FileFragment local, FileFragment remote,
            ProgressMonitor progressMonitor) throws FileResourceException {
        getFile(local, remote, progressMonitor);
    }

    /** copy a directory */
    @Override
    public void getDirectory(String remoteDirName, String localDirName)
            throws FileResourceException {

        File localDir = new File(localDirName);

        if (!localDir.exists()) {
            localDir.mkdir();
        }

        for (GridFile gridFile : list(remoteDirName)) {
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
    @Override
    public void putDirectory(String localDirName, String remoteDirName)
            throws FileResourceException {
        getDirectory(localDirName, remoteDirName);
    }

    /** copy multiple files */
    @Override
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
    @Override
    public void getMultipleFiles(String[] remoteFileNames, String localDirName)
            throws FileResourceException {
        for (int i = 0; i < remoteFileNames.length; i++) {
            File newFile = new File(remoteFileNames[i]);
            getFile(remoteFileNames[i], localDirName + newFile.getName());
        }
    }

    /** copy multiple files */
    @Override
    public void putMultipleFiles(String[] localFileNames,
            String[] remoteFileNames) throws FileResourceException {
        getMultipleFiles(localFileNames, remoteFileNames);
    }

    /** copy multiple files */
    @Override
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
        gridFile.setLastModified(new Date(f.lastModified()));

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
        gridFile.setWorldPermissions(allPermissions);

        return gridFile;
    }

    @Override
    public InputStream openInputStream(String name) throws FileResourceException {
        try {
            return new FileInputStream(name);
        }
        catch (java.io.FileNotFoundException e) {
            throw new FileResourceException(e);
        }
    }

    @Override
    public OutputStream openOutputStream(String name) throws FileResourceException {
        try {
            File p = new File(name).getAbsoluteFile().getParentFile();
            if (!p.mkdirs()) {
                if (!p.exists()) {
                    throw new FileResourceException("Cannot create directory " + p.getAbsolutePath());
                }
            }
            return new FileOutputStream(name);
        }
        catch (java.io.FileNotFoundException e) {
            throw new FileResourceException(e);
        }
    }

    @Override
    public boolean supportsStreams() {
        return true;
    }

    public boolean supportsPartialTransfers() {
        return true;
    }

    public boolean supportsThirdPartyTransfers() {
        return false;
    }
}
