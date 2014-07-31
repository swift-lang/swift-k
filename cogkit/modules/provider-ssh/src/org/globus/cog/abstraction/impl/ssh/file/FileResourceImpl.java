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

package org.globus.cog.abstraction.impl.ssh.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.AbstractFileResource;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IrrecoverableResourceException;
import org.globus.cog.abstraction.impl.ssh.ConnectionID;
import org.globus.cog.abstraction.impl.ssh.SSHChannel;
import org.globus.cog.abstraction.impl.ssh.SSHChannelManager;
import org.globus.cog.abstraction.impl.ssh.execution.Exec;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileFragment;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.ProgressMonitor;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;

import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.SftpFileInputStream;
import com.sshtools.j2ssh.sftp.SftpFileOutputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;

/**
 * File resource interface implementation through SSH.
 */
public class FileResourceImpl extends AbstractFileResource {
    private SSHChannel channel;
    private SftpSubsystemClient sftp;

    public static final Logger logger = Logger.getLogger(FileResource.class);
    private String cwd;
    private Exec exec;
    private ConnectionID id;

    public FileResourceImpl() throws Exception {
        this(null, null, null);
    }

    public FileResourceImpl(String name, ServiceContact serviceContact,
            SecurityContext securityContext) {
        super(name, "SSH", serviceContact, securityContext);
        exec = new Exec();
        exec.setOutMem(true);
        cwd = "";
    }

    public void start() throws InvalidSecurityContextException,
            FileResourceException {
        
        ServiceContact serviceContact = getAndCheckServiceContact();
        
        String host = serviceContact.getHost();
        int port = serviceContact.getPort();
        if (port == -1) {
            port = 22;
        }
        
        
        try {
            SecurityContext securityContext = getOrCreateSecurityContext("ssh", serviceContact);
            
            channel = SSHChannelManager.getDefault().getChannel(host, port, 
                securityContext.getCredentials());
            id = channel.getBundle().getId();
            sftp = new SftpSubsystemClient();
            if (!channel.getSession().startSubsystem(sftp)) {
                throw new TaskSubmissionException(
                        "Failed to start the SFTP subsystem on " + id);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully started SFTP subsystem on " + id);
            }
            cwd = sftp.getDefaultDirectory();
            setStarted(true);
        }
        catch (Exception se) {
            throw translateException(
                    "Error while communicating with the SSH server on " + host
                            + ":" + port, se);
        }
    }

    private FileResourceException translateException(String msg, Exception prev) {
        if (prev instanceof IOException) {
            return new IrrecoverableResourceException(msg, prev);
        }
        else {
            return new FileResourceException(msg, prev);
        }
    }

    public void stop() throws FileResourceException {
        try {
            SSHChannelManager.getDefault().releaseChannel(channel);
            setStarted(false);
        }
        catch (Exception e) {
            throw translateException("Error closing SSH session on " + id, e);
        }
    }

    public void setCurrentDirectory(String directory)
            throws FileResourceException {
        this.cwd = absPath(directory);
    }

    public String getCurrentDirectory() throws FileResourceException {
        return cwd;
    }

    public Collection<GridFile> list() throws FileResourceException {
        return list("");
    }

    public Collection<GridFile> list(String directory) throws FileResourceException {
        try {
            String absPath = absPath(directory);
            SftpFile f = new SftpFile(absPath, sftp.getAttributes(absPath));
            List<?> l = new ArrayList<Object>();
            sftp.listChildren(f, l);
            return translateList(l);
        }
        catch (Exception e) {
            throw translateException("Cannot list contents of " + directory, e);
        }
    }

    protected List<GridFile> translateList(List<?> l) {
        List<GridFile> t = new ArrayList<GridFile>(l.size());
        for (Object o : l) {
            SftpFile f = (SftpFile) o;
            GridFile g = new GridFileImpl();
            g.setAbsolutePathName(f.getAbsolutePath());
            g.setName(f.getFilename());
            setAttributes(g, f.getAttributes());
            t.add(g);
        }
        return t;
    }

    protected void setAttributes(GridFile g, FileAttributes attrs) {
        if (attrs.isDirectory()) {
            g.setFileType(GridFile.DIRECTORY);
        }
        else if (attrs.isBlock() || attrs.isCharacter()) {
            g.setFileType(GridFile.DEVICE);
        }
        else if (attrs.isLink()) {
            g.setFileType(GridFile.SOFTLINK);
        }
        else {
            g.setFileType(GridFile.FILE);
        }
        g.setLastModified(attrs.getModTimeString());
        g.setSize(attrs.getSize().longValue());
    }

    protected String absPath(String path) {
        if (path.startsWith("/")) {
            return path;
        }
        else if ("".equals(cwd)) {
            return path;
        }
        else {
            return cwd + '/' + path;
        }
    }

    public void createDirectory(String directory) throws FileResourceException {
        if (logger.isDebugEnabled()) {
            logger.debug("mkdir " + id + ":" + directory);
        }
        try {
            sftp.makeDirectory(absPath(directory));
        }
        catch (Exception e) {
            throw translateException("Cannot create directory \"" + directory
                    + "\"", e);
        }
    }

    public void deleteDirectory(String directory, boolean force)
            throws FileResourceException {
        directory = absPath(directory);

        if (!isDirectory(directory)) {
            throw new DirectoryNotFoundException(directory
                    + " is not a valid directory");
        }

        try {
            if (force) {
                for (GridFile f : list(directory)) {
                    if (f.isFile()) {
                        sftp.removeFile(directory + "/" + f.getName());
                    }
                    else {
                        deleteDirectory(directory + "/" + f.getName(), force);
                    }

                }
            }
            sftp.removeDirectory(directory);
        }
        catch (Exception e) {
            throw translateException("Cannot delete directory \"" + directory
                    + "\"", e);
        }
    }

    public void deleteFile(String file) throws FileResourceException {
        try {
            sftp.removeFile(absPath(file));
        }
        catch (Exception e) {
            throw translateException("Cannot delete file \"" + file + "\"", e);
        }
    }

    public void getFile(FileFragment remote, FileFragment local,
            final ProgressMonitor progressMonitor) throws FileResourceException {
        checkNoPartialTransfers(remote, local, "ssh");
        
        File localFile = new File(local.getFile());
        try {
            SftpFile file = sftp.openFile(absPath(remote.getFile()),
                    SftpSubsystemClient.OPEN_READ);
            long total = file.getAttributes().getSize().longValue();
            byte[] buffer = new byte[65535];
            BufferedInputStream in = new BufferedInputStream(
                    new SftpFileInputStream(file));
            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(localFile));
            int read;
            long crt = 0;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                crt += read;
                if (progressMonitor != null) {
                    progressMonitor.progress(total, crt);
                }
            }
            in.close();
            out.close();
        }
        catch (Exception e) {
            throw translateException("Cannot transfer \"" + remote.getFile()
                    + "\" to \"" + local.getFile() + "\"", e);
        }
    }

    /** Copy a local file to a remote file. Default option 'overwrite' */
    public void putFile(FileFragment local, FileFragment remote,
            final ProgressMonitor progressMonitor) throws FileResourceException {
        if (local.isFragment() || remote.isFragment()) {
            throw new UnsupportedOperationException("The SSH provider does not support partial transfers");
        }
        File localFile = new File(local.getFile());
        try {
            FileAttributes attrs = new FileAttributes();
            // Open with rw as setting all permissiosn does not work untill we
            // have created the file
            //
            attrs.setPermissions("rw");

            SftpFile file = sftp.openFile(absPath(remote.getFile()),
                    SftpSubsystemClient.OPEN_WRITE
                            | SftpSubsystemClient.OPEN_CREATE
                            | SftpSubsystemClient.OPEN_TRUNCATE, attrs);

            byte[] buffer = new byte[65535];
            BufferedInputStream in = new BufferedInputStream(
                    new FileInputStream(localFile));
            BufferedOutputStream out = new BufferedOutputStream(
                    new SftpFileOutputStream(file));
            int read;
            long total = localFile.length();
            long crt = 0;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                crt += read;
                if (progressMonitor != null) {
                    progressMonitor.progress(total, crt);
                }
            }

            in.close();
            out.close();
        }
        catch (Exception e) {
            throw translateException("Cannot transfer \"" + local.getFile()
                    + "\" to \"" + remote.getFile() + "\"", e);
        }
    }

    public void rename(String remoteFileName1, String remoteFileName2)
            throws FileResourceException {
        try {
            sftp.renameFile(absPath(remoteFileName1), absPath(remoteFileName2));
        }
        catch (Exception e) {
            throw translateException("Cannot rename \"" + remoteFileName1
                    + "\" to \"" + remoteFileName2 + "\"", e);
        }
    }

    public void changeMode(String filename, int mode)
            throws FileResourceException {
        try {
            // hehe. Surely this won't work properly
            sftp.changePermissions(filename, mode);
        }
        catch (Exception e) {
            throw translateException("Cannot change permissions for \""
                    + filename + "\"", e);
        }
    }

    public GridFile getGridFile(String fileName) throws FileResourceException {
        try {
            GridFile g = new GridFileImpl();
            g.setAbsolutePathName(absPath(fileName));
            FileAttributes attrs = sftp.getAttributes(g.getAbsolutePathName());
            setAttributes(g, attrs);
            return g;
        }
        catch (Exception e) {
            throw translateException("Cannot get file information for \""
                    + fileName + "\"", e);
        }
    }

    public void changeMode(GridFile newGridFile) throws FileResourceException {
        String newPermissions = newGridFile.getUserPermissions().toString()
                + newGridFile.getGroupPermissions().toString()
                + newGridFile.getWorldPermissions().toString();

        changeMode(newGridFile.getAbsolutePathName(), Integer
                .parseInt(newPermissions));
    }

    public boolean exists(String filename) throws FileResourceException {
        try {
            FileAttributes attrs = sftp.getAttributes(absPath(filename));
            return true;
        }
        catch (Exception e) {
            if ("No such file".equals(e.getMessage())) {
                return false;
            }
            else {
                throw translateException(
                        "Cannot determine the existence of the file", e);
            }
        }
    }

    public boolean isDirectory(String dirName) throws FileResourceException {
        return getGridFile(dirName).isDirectory();
    }

    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException {
        throw new TaskSubmissionException("Not implemented");
    }

    public boolean supportsPartialTransfers() {
        return false;
    }

    public boolean supportsThirdPartyTransfers() {
        return false;
    }
}
