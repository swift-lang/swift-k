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

package org.globus.cog.abstraction.impl.file.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.PasswordAuthentication;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.impl.file.PermissionsImpl;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileFragment;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.ProgressMonitor;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.ftp.Buffer;
import org.globus.ftp.DataSink;
import org.globus.ftp.DataSinkStream;
import org.globus.ftp.DataSource;
import org.globus.ftp.DataSourceStream;
import org.globus.ftp.FTPClient;
import org.globus.ftp.FileInfo;
import org.globus.ftp.InputStreamDataSink;
import org.globus.ftp.OutputStreamDataSource;
import org.globus.ftp.Session;
import org.globus.ftp.vanilla.TransferState;

/**
 * File resource interface implementation for FTP Servers. Supports relative and
 * absolute path names.
 */
public class FileResourceImpl extends AbstractFTPFileResource {
    public static final String PROTOCOL = "ftp";
    
    public static final String ANONYMOUS_USERNAME = "anonymous";
    public static final char[] ANONYMOUS_PASSWORD;
    static {
        String pwd = "none@example.com";
        ANONYMOUS_PASSWORD = new char[pwd.length()];
        pwd.getChars(0, pwd.length(), ANONYMOUS_PASSWORD, 0);
    }
    
    private FTPClient ftpClient;
    public static final Logger logger = Logger.getLogger(FileResource.class
        .getName());

    /** throws invalidprovider exception */
    public FileResourceImpl() {
        this(null, null, null);
    }

    /** the constructor to be used normally */
    public FileResourceImpl(String name, ServiceContact serviceContact,
            SecurityContext securityContext) {
        super(name, PROTOCOL, serviceContact, securityContext);
    }

    /**
     * Create the ftpClient and authenticate with the resource.
     * 
     * @throws FileResourceException
     *             if an exception occurs during the resource start-up
     */
    public void start() throws InvalidSecurityContextException, IllegalHostException,
            FileResourceException {

        ServiceContact serviceContact = getAndCheckServiceContact();
        
        String host = getServiceContact().getHost();
        int port = getServiceContact().getPort();
        if (port == -1) {
            port = 21;
        }
        
        if (getName() == null) {
            setName(host + ":" + port);
        }
        
        
        try {
            SecurityContext securityContext = getOrCreateSecurityContext("ftp", serviceContact);
            
            PasswordAuthentication credentials = getCredentialsAsPasswordAuthentication(securityContext); 
            
            ftpClient = new FTPClient(host, port);

            String username = credentials.getUserName();
            String password = String.valueOf(credentials.getPassword());

            ftpClient.authorize(username, password);
            ftpClient.setType(Session.TYPE_IMAGE);
            setStarted(true);
        }
        catch (Exception e) {
            throw translateException(
                "Error connecting to the FTP server at " + host + ":" + port, e);
        }
    }
    
    @Override
    protected PasswordAuthentication getDefaultUsernameAndPassword() {
        return new PasswordAuthentication(ANONYMOUS_USERNAME, ANONYMOUS_PASSWORD);
    }

    /**
     * Stop the ftpClient from connecting to the server
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public void stop() throws FileResourceException {
        try {
            ftpClient.close();
            setStarted(false);
        }
        catch (Exception e) {
            throw translateException("Error while stopping the FTP server", e);
        }
    }

    /**
     * Equivalent to cd command
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public void setCurrentDirectory(String directory)
            throws FileResourceException {
        try {
            ftpClient.changeDir(directory);
        }
        catch (Exception e) {
            throw translateException("Cannot set the current directory", e);
        }
    }

    /**
     * Return Current path
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public String getCurrentDirectory() throws FileResourceException {
        try {
            return ftpClient.getCurrentDir();
        }
        catch (Exception e) {
            throw translateException("Cannot get the current directory", e);
        }
    }

    /**
     * Equivalent to ls command in the current directory
     * 
     * @throws IOException
     * @throws FileResourceException
     */
    public Collection<GridFile> list() throws FileResourceException {
        List<GridFile> gridFileList = new ArrayList<GridFile>();
        try {
            ftpClient.setPassive();
            ftpClient.setLocalActive();
            ftpClient.setType(Session.TYPE_ASCII);

            Enumeration<?> list = ftpClient.list().elements();
            ftpClient.setType(Session.TYPE_IMAGE);

            while (list.hasMoreElements()) {
                gridFileList.add(createGridFile(list.nextElement()));
            }
            return gridFileList;
        }
        catch (Exception e) {
            throw translateException(
                "Cannot list the elements of the current directory", e);
        }
    }

    /**
     * Equivalent to ls command on the given directory
     * 
     * @throws FileResourceException
     */
    public Collection<GridFile> list(String directory) throws FileResourceException {

        // Store currentDir
        String currentDirectory = getCurrentDirectory();
        // Change directory
        setCurrentDirectory(directory);
        Collection<GridFile> list = null;
        try {
            ftpClient.setType(Session.TYPE_ASCII);
            list = list();
            ftpClient.setType(Session.TYPE_IMAGE);
        }
        catch (Exception e) {
            throw translateException("Error in list directory", e);
        }

        // Come back to original directory
        setCurrentDirectory(currentDirectory);
        return list;
    }

    /** Equivalent to mkdir */
    public void createDirectory(String directory) throws FileResourceException {
        try {
            ftpClient.makeDir(directory);
        }
        catch (Exception e) {
            throw translateException("Cannot create the directory", e);
        }
    }

    /**
     * Remove directory and its files if force = true. Else remove directory
     * only if empty
     */
    public void deleteDirectory(String directory, boolean force)
            throws FileResourceException {

        if (!isDirectory(directory)) {
            throw new DirectoryNotFoundException(directory
                    + " is not a valid directory");
        }

        try {
            if (force) {
                for (GridFile f : list(directory)) {
                    if (f.isFile()) {
                        ftpClient.deleteFile(directory + "/" + f.getName());
                    }
                    else {
                        deleteDirectory(directory + "/" + f.getName(), force);
                    }
                }
            }
            if (list(directory).isEmpty()) {
                ftpClient.deleteDir(directory);
            }
        }
        catch (Exception e) {
            throw translateException("Cannot delete the given directory", e);
        }
    }

    /** Equivalent to rm command on a file */
    public void deleteFile(String file) throws FileResourceException {
        try {
            ftpClient.deleteFile(file);
        }
        catch (Exception e) {
            throw translateException("Cannot delete the given file", e);
        }
    }

    public void getFile(FileFragment remote, FileFragment local)
            throws FileResourceException {
        getFile(remote, local, null);
    }

    /** Equivalent to cp/copy command */
    public void getFile(FileFragment remote, FileFragment local,
            final ProgressMonitor progressMonitor) throws FileResourceException {
        if (remote.isFragment() || local.isFragment()) {
            throw new UnsupportedOperationException("The FTP provider does not support partial transfers");
        }
        String currentDirectory = getCurrentDirectory();
        File localFile = new File(local.getFile());
        try {
            ftpClient.setPassive();
            ftpClient.setLocalActive();
            final long size = localFile.length();
            DataSink sink;
            if (progressMonitor != null) {
                // The sink is used to follow progress
                sink = new DataSinkStream(new FileOutputStream(localFile)) {
                    public void write(Buffer buffer) throws IOException {
                        super.write(buffer);
                        progressMonitor.progress(offset, size);
                    }
                };
            }
            else {
                sink = new DataSinkStream(new FileOutputStream(localFile));
            }
            ftpClient.get(remote.getFile(), sink, null);
        }
        catch (Exception e) {
            throw translateException("Cannot retrieve the given file", e);
        }
    }

    public void putFile(FileFragment local, FileFragment remote)
            throws FileResourceException {
        putFile(local, remote, null);
    }

    /** Copy a local file to a remote file. Default option 'overwrite' */
    public void putFile(FileFragment local, FileFragment remote,
            final ProgressMonitor progressMonitor) throws FileResourceException {
        if (local.isFragment() || remote.isFragment()) {
            throw new UnsupportedOperationException("The FTP provider does not support partial transfers");
        }
        String currentDirectory = getCurrentDirectory();
        File localFile = new File(local.getFile());
        try {
            ftpClient.setPassive();
            ftpClient.setLocalActive();
            final long size = localFile.length();
            DataSource source;
            if (progressMonitor != null) {
                source = new DataSourceStream(new FileInputStream(localFile)) {
                    public Buffer read() throws IOException {
                        progressMonitor.progress(totalRead, size);
                        return super.read();
                    }
                };
            }
            else {
                source = new DataSourceStream(new FileInputStream(localFile));
            }
            ftpClient.put(remote.getFile(), source, null, false);
        }
        catch (Exception e) {
            throw translateException("Cannot transfer the given file", e);
        }
    }

    /**
     * rename a remote file.
     */
    public void rename(String remoteFileName1, String remoteFileName2)
            throws FileResourceException {
        try {
            ftpClient.rename(remoteFileName1, remoteFileName2);
        }
        catch (Exception e) {
            throw translateException("Rename for ftp failed", e);
        }
    }

    /**
     * Changes the permissions on the file if authorized to do so
     */
    public void changeMode(String filename, int mode)
            throws FileResourceException {
        String cmd = "chmod " + mode + " " + filename; // or something else
        try {
            ftpClient.site(cmd);
        }
        catch (Exception e) {
            throw translateException("Cannot change the file permissions", e);
        }
    }

    /** get file information */
    public GridFile getGridFile(String fileName) throws FileResourceException {

        String directory = null;
        int endIndex = fileName.lastIndexOf("/");
        if (endIndex < 0) {
            directory = getCurrentDirectory();
        }
        else {
            directory = fileName.substring(0, endIndex);
            fileName = fileName.substring(endIndex + 1, fileName.length());
        }

        for (GridFile f : list(directory)) {
            if (f.getName().equals(fileName)) {
                return f;
            }
        }
        return null;
    }

    /** change mode for the file if authorized to do so */
    public void changeMode(GridFile newGridFile) throws FileResourceException {

        String newPermissions = newGridFile.getUserPermissions().toString()
                + newGridFile.getGroupPermissions().toString()
                + newGridFile.getWorldPermissions().toString();

        logger.error(newGridFile.getAbsolutePathName());

        changeMode(newGridFile.getAbsolutePathName(), Integer
            .parseInt(newPermissions));
    }

    /** returns true if the file exists */
    public boolean exists(String filename) throws FileResourceException {
        try {
            return ftpClient.exists(filename);
        }
        catch (Exception e) {
            throw translateException(
                "Cannot determine the existence of the file", e);
        }
    }

    /**
     * return true if the input is a directory in the ftp resource. works only
     * if you have permissions to change to the specified directory.
     */
    public boolean isDirectory(String dirName) throws FileResourceException {
        boolean isDir = true;
        String currentDirectory = getCurrentDirectory();
        try {
            setCurrentDirectory(dirName);
        }
        catch (FileResourceException e) {
            isDir = false;
        }
        finally {
            try {
                setCurrentDirectory(currentDirectory);
            }
            catch (Exception e) {
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
        }
        else {
            gridFile.setAbsolutePathName(directory + "/" + fileInfo.getName());
        }
        try {
            gridFile.setLastModified(new SimpleDateFormat().parse(fileInfo.getDate()));
        }
        catch (ParseException e) {
            gridFile.setLastModified(new Date(0));
        }

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

        gridFile.setUserPermissions(PermissionsImpl.instance(fileInfo.userCanRead(), 
            fileInfo.userCanWrite(), fileInfo.userCanExecute()));
        gridFile.setGroupPermissions(PermissionsImpl.instance(fileInfo.groupCanRead(), 
            fileInfo.groupCanWrite(), fileInfo.groupCanExecute()));
        gridFile.setWorldPermissions(PermissionsImpl.instance(fileInfo.allCanRead(), 
            fileInfo.allCanWrite(), fileInfo.allCanExecute()));
        
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
                }
                else {
                    removeLocalDirectory(newFile.getAbsolutePath());
                }
            }
        }
        tempFile.delete();
    }

    public InputStream openInputStream(String name)
            throws FileResourceException {
        InputStreamDataSink sink = null;
        try {
            ftpClient.setPassive();
            ftpClient.setLocalActive();

            sink = new InputStreamDataSink();

            TransferState state = ftpClient.asynchGet(name, sink, null);
            state.waitForStart();
            
            return sink.getInputStream();
        }
        catch (Exception e) {
            if (sink != null) {
                try {
                    sink.close();
                }
                catch (IOException ee) {
                    logger.warn("Failed to close FTP sink", ee);
                }
            }
            throw translateException("Failed to open FTP stream", e);
        }
    }

    public OutputStream openOutputStream(String name)
            throws FileResourceException {
        OutputStreamDataSource source = null;
        try {
            ftpClient.setPassive();
            ftpClient.setLocalActive();
            
            source = new OutputStreamDataSource(16384);
            
            TransferState state = ftpClient.asynchPut(name, source, null, false);        
            state.waitForStart();

            return source.getOutputStream();
        }
        catch (Exception e) {
            if (source != null) {
                try {
                    source.close();
                }
                catch (IOException ee) {
                    logger.warn("Failed to close FTP source", ee);
                }
            }
            throw translateException("Failed to open FTP stream", e);
        }
    }
    
    public boolean supportsStreams() {
        return true;
    }

    public boolean supportsPartialTransfers() {
        return false;
    }

    public boolean supportsThirdPartyTransfers() {
        return false;
    }
}
