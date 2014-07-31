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

package org.globus.cog.abstraction.impl.file.fake;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.AbstractFileResource;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.impl.file.PermissionsImpl;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileFragment;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Permissions;
import org.globus.cog.abstraction.interfaces.ProgressMonitor;
import org.globus.cog.abstraction.interfaces.ServiceContact;

/**
 * enables access to local file system through the file resource interface
 * Supports absolute and relative path names
 */
public class FileResourceImpl extends AbstractFileResource {
    public static final String PROTOCOL = "local";
    public static final ServiceContact LOCALHOST = new ServiceContactImpl("localhost");
    
    public static final Logger logger = Logger
            .getLogger(FileResourceImpl.class);

    public FileResourceImpl() {
        this(null);
    }

    public FileResourceImpl(String name) {
        super(name, PROTOCOL, LOCALHOST, null);
    }

    /** set user's home directory as the current directory */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, FileResourceException {
        setStarted(true);
    }

    /** close the file */
    public void stop() {
        setStarted(false);
    }

    /** equivalent to cd */
    public void setCurrentDirectory(String directory)
            throws FileResourceException {
    }

    private void setCurrentDirectory(File f) throws FileResourceException {
    }

    /** return current path */
    public String getCurrentDirectory() {
        return ".";
    }

    /** list the contents of the current directory */
    public Collection<GridFile> list() {
        List<GridFile> files = new ArrayList<GridFile>();
        File[] f = new File[] { new File("a.txt"), new File("b.txt") };
        for (int i = 0; i < f.length; i++) {
            files.add(createGridFile(f[i]));
        }
        return files;
    }

    /** list contents of the given directory */
    public Collection<GridFile> list(String directory) throws FileResourceException {
        return list();
    }

    /**
     * make a new directory
     * 
     * @throws FileResourceException
     */
    public void createDirectory(String directory)
            throws FileResourceException {
    }

    public void createDirectories(String directory)
            throws FileResourceException {
    }

    public void deleteDirectory(String dir, boolean force) throws FileResourceException {
    }

    private void deleteDirectory(File f, boolean force) throws FileResourceException {
    }

    /** remove a file */
    public void deleteFile(String fileName) throws FileResourceException {
    }

    /** copy a file */
    public void getFile(FileFragment remote, FileFragment local,
            ProgressMonitor progressMonitor) throws FileResourceException {
        if (progressMonitor != null) {
            progressMonitor.progress(1024, 1024);
        }
    }

    public void putFile(FileFragment local, FileFragment remote,
            ProgressMonitor progressMonitor) throws FileResourceException {
        getFile(local, remote, progressMonitor);
    }

    /** copy a directory */
    public void getDirectory(String remoteDirName, String localDirName)
            throws FileResourceException {
    }

    /** copy a directory */
    public void putDirectory(String localDirName, String remoteDirName)
            throws FileResourceException {
    }

    /** copy multiple files */
    public void getMultipleFiles(String[] remoteFileNames,
            String[] localFileNames) throws FileResourceException {
    }

    /** copy multiple files */
    public void getMultipleFiles(String[] remoteFileNames, String localDirName)
            throws FileResourceException {
    }

    /** copy multiple files */
    public void putMultipleFiles(String[] localFileNames,
            String[] remoteFileNames) throws FileResourceException {
    }

    /** copy multiple files */
    public void putMultipleFiles(String[] localFileNames, String remoteDirName)
            throws FileResourceException {
    }

    /**
     * rename a file.
     */
    public void rename(String remoteFileName1, String remoteFileName2)
            throws FileResourceException {
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
        return createGridFile(new File(fileName));
    }

    /** return true of file exists */
    public boolean exists(String filename) throws FileResourceException {
        return true;
    }

    /** return true if input is a directory */
    public boolean isDirectory(String dirName) throws FileResourceException {
        return false;
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
        gridFile.setWorldPermissions(allPermissions);

        return gridFile;
    }

    public InputStream openInputStream(String name) throws FileResourceException {
        return new ByteArrayInputStream(new byte[0]);
    }

    public OutputStream openOutputStream(String name) throws FileResourceException {
        return new ByteArrayOutputStream();
    }

    public boolean supportsStreams() {
        return true;
    }

    @Override
    public boolean supportsPartialTransfers() {
        return false;
    }

    @Override
    public boolean supportsThirdPartyTransfers() {
        return false;
    }
}
