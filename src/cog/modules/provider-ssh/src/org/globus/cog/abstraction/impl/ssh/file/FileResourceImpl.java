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
import java.util.Iterator;
import java.util.List;

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
import org.globus.cog.abstraction.impl.file.IrrecoverableResourceException;
import org.globus.cog.abstraction.impl.ssh.ConnectionID;
import org.globus.cog.abstraction.impl.ssh.SSHChannel;
import org.globus.cog.abstraction.impl.ssh.SSHChannelManager;
import org.globus.cog.abstraction.impl.ssh.execution.Exec;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
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
        this(null, new ServiceContactImpl(), AbstractionFactory
                .newSecurityContext("SSH"));
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
        try {
            String host = getServiceContact().getHost();
            int port = getServiceContact().getPort();
            if (port == -1) {
                port = 22;
            }
            channel = SSHChannelManager.getDefault().getChannel(host, port,
                    getSecurityContext().getCredentials());
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
                    "Error while communicating with the SSH server on " + id, se);
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

    public Collection list() throws FileResourceException {
        return list("");
    }

    public Collection list(String directory) throws FileResourceException {
        try {
            String absPath = absPath(directory);
            SftpFile f = new SftpFile(absPath, sftp.getAttributes(absPath));
            List l = new ArrayList();
            sftp.listChildren(f, l);
            return translateList(l);
        }
        catch (Exception e) {
            throw translateException("Cannot list contents of " + directory, e);
        }
    }

    protected List translateList(List l) {
        List t = new ArrayList(l.size());
        Iterator i = l.iterator();
        while (i.hasNext()) {
            SftpFile f = (SftpFile) i.next();
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
                        sftp.removeFile(directory + "/" + gridFile.getName());
                    }
                    else {
                        deleteDirectory(directory + "/" + gridFile.getName(),
                                force);
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

    public void getFile(String remoteFilename, String localFileName)
            throws FileResourceException {
        getFile(remoteFilename, localFileName, null);
    }

    public void getFile(String remoteFilename, String localFileName,
            final ProgressMonitor progressMonitor) throws FileResourceException {
        File localFile = new File(localFileName);
        try {
            SftpFile file = sftp.openFile(absPath(remoteFilename),
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
            throw translateException("Cannot transfer \"" + remoteFilename
                    + "\" to \"" + localFileName + "\"", e);
        }
    }

    public void putFile(String localFileName, String remoteFileName)
            throws FileResourceException {
        putFile(localFileName, remoteFileName, null);
    }

    /** Copy a local file to a remote file. Default option 'overwrite' */
    public void putFile(String localFileName, String remoteFileName,
            final ProgressMonitor progressMonitor) throws FileResourceException {
        File localFile = new File(localFileName);
        try {
            FileAttributes attrs = new FileAttributes();
            // Open with rw as setting all permissiosn does not work untill we
            // have created the file
            //
            attrs.setPermissions("rw");

            SftpFile file = sftp.openFile(absPath(remoteFileName),
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
            throw translateException("Cannot transfer \"" + localFileName
                    + "\" to \"" + remoteFileName + "\"", e);
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
                + newGridFile.getAllPermissions().toString();

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

}
