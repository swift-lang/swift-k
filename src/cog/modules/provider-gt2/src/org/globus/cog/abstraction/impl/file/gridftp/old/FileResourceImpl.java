// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file.gridftp.old;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.impl.file.PermissionsImpl;
import org.globus.cog.abstraction.impl.file.ftp.AbstractFTPFileResource;
import org.globus.cog.abstraction.impl.file.gridftp.DataChannelAuthenticationType;
import org.globus.cog.abstraction.impl.file.gridftp.DataChannelProtectionType;
import org.globus.cog.abstraction.impl.file.gridftp.GridFTPSecurityContext;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Permissions;
import org.globus.cog.abstraction.interfaces.ProgressMonitor;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.ftp.Buffer;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.DataSink;
import org.globus.ftp.DataSinkStream;
import org.globus.ftp.DataSource;
import org.globus.ftp.DataSourceStream;
import org.globus.ftp.FileInfo;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.MarkerListener;
import org.globus.ftp.MlsxEntry;
import org.globus.ftp.Session;
import org.globus.ftp.exception.ServerException;
import org.ietf.jgss.GSSCredential;

/**
 * Implements FileResource API for accessing gridftp server Supports relative
 * and absolute path names
 */
public class FileResourceImpl extends AbstractFTPFileResource {
    public static final Logger logger = Logger
            .getLogger(FileResourceImpl.class);

    /**
     * By default JGlobus sets this to 6000 ms. Experience has proved that it
     * may be too low.
     */
    public static final int MAX_REPLY_WAIT_TIME = 30000; // ms

    private GridFTPClient gridFTPClient;

    /** throws InvalidProviderException */
    public FileResourceImpl() throws Exception {
        this(null, new ServiceContactImpl(), AbstractionFactory
                .newSecurityContext("GridFTP"));
    }

    /** constructor be used normally */
    public FileResourceImpl(String name, ServiceContact serviceContact,
            SecurityContext securityContext) {
        super(name, FileResource.GridFTP, serviceContact, securityContext);
    }

    /**
     * Create the gridFTPClient and authenticate with the resource.
     * 
     * @throws FileResourceException
     * @throws FileResourceException
     */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, FileResourceException {

        try {
            String host = getServiceContact().getHost();
            int port = getServiceContact().getPort();
            if (port == -1) {
                port = 2811;
            }
            gridFTPClient = new GridFTPClient(host, port);
            gridFTPClient.setClientWaitParams(MAX_REPLY_WAIT_TIME,
                    Session.DEFAULT_WAIT_DELAY);
            GSSCredential proxy = (GSSCredential) getSecurityContext()
                    .getCredentials();
            gridFTPClient.authenticate(proxy);
            gridFTPClient.setType(Session.TYPE_IMAGE);
            setSecurityOptions(gridFTPClient);

            setStarted(true);
        }
        catch (Exception e) {
            throw translateException(
                    "Error communicating with the GridFTP server", e);
        }
    }

    protected void setSecurityOptions(GridFTPClient client)
            throws ServerException, IOException {
        DataChannelAuthenticationType dcau = GridFTPSecurityContext
                .getDataChannelAuthentication(getSecurityContext());
        if (dcau != null) {
            if (dcau.equals(DataChannelAuthenticationType.NONE)) {
                client
                        .setDataChannelAuthentication(DataChannelAuthentication.NONE);
            }
            else if (dcau.equals(DataChannelAuthenticationType.SELF)) {
                client
                        .setDataChannelAuthentication(DataChannelAuthentication.SELF);
            }
        }
        DataChannelProtectionType prot = GridFTPSecurityContext
                .getDataChannelProtection(getSecurityContext());
        if (prot != null) {
            if (prot.equals(DataChannelProtectionType.CLEAR)) {
                client
                        .setDataChannelProtection(GridFTPSession.PROTECTION_CLEAR);
            }
            else if (prot.equals(DataChannelProtectionType.CONFIDENTIAL)) {
                client
                        .setDataChannelProtection(GridFTPSession.PROTECTION_CONFIDENTIAL);
            }
            else if (prot.equals(DataChannelProtectionType.PRIVATE)) {
                client
                        .setDataChannelProtection(GridFTPSession.PROTECTION_PRIVATE);
            }
            else if (prot.equals(DataChannelProtectionType.SAFE)) {
                client.setDataChannelProtection(GridFTPSession.PROTECTION_SAFE);
            }
        }
    }

    /**
     * Stop the gridFTPClient from connecting to the server
     * 
     * @throws FileResourceException
     */
    public void stop() throws FileResourceException {
        try {
            gridFTPClient.close();
            setStarted(false);
        }
        catch (Exception e) {
            throw translateException("Error stopping the resource", e);
        }
    }

    /** Equivalent to cd command */
    public void setCurrentDirectory(String directory)
            throws FileResourceException {
        try {
            gridFTPClient.changeDir(directory);
        }
        catch (Exception e) {
            throw translateException("Could not set current directory to \""
                    + directory + "\"", e);
        }
    }

    /**
     * Return Current Directory's name
     * 
     * @throws FileResourceException
     */
    public String getCurrentDirectory() throws FileResourceException {
        try {
            return gridFTPClient.getCurrentDir();
        }
        catch (Exception e) {
            throw translateException("Cannot get the current directory", e);
        }
    }

    /** Equivalent to ls command in the current directory */
    public Collection list() throws FileResourceException {

        Vector gridFileList = new Vector();
        try {
            gridFTPClient.setPassiveMode(true);
            Enumeration list = gridFTPClient.list().elements();
            while (list.hasMoreElements()) {
                gridFileList.add(createGridFile((FileInfo) list.nextElement()));
            }
            return gridFileList;

        }
        catch (Exception e) {
            throw translateException(
                    "Cannot list the elements of the current directory", e);
        }
    }

    /** Equivalent to ls command on the given directory */
    public Collection list(String directory) throws FileResourceException {
        // Store currentDir
        String currentDirectory = getCurrentDirectory();
        // Change directory
        setCurrentDirectory(directory);

        Collection list = list();
        // restore original directory
        setCurrentDirectory(currentDirectory);

        return list;
    }

    /** Equivalent to mkdir */
    public void createDirectory(String directory) throws FileResourceException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("createDirectory(" + directory + ")");
            }
            gridFTPClient.makeDir(directory);
        }
        catch (Exception e) {
            throw translateException("Cannot create directory " + directory, e);
        }
    }

    /**
     * Remove directory and its files if force = true. Else remove directory
     * only if empty
     */
    public void deleteDirectory(String directory, boolean force)
            throws DirectoryNotFoundException, FileResourceException {

        GridFile gridFile = null;

        if (!isDirectory(directory)) {
            throw new DirectoryNotFoundException(directory
                    + " is not a valid directory");
        }

        try {
            if (force == true) {
                for (Iterator iterator = list(directory).iterator(); iterator
                        .hasNext();) {
                    gridFile = (GridFile) iterator.next();
                    if (gridFile.isFile()) {
                        gridFTPClient.deleteFile(directory + "/"
                                + gridFile.getName());
                    }
                    else {
                        if (!(gridFile.getName().equals(".") || gridFile
                                .getName().equals(".."))) {
                            deleteDirectory(directory + "/"
                                    + gridFile.getName(), force);
                        }
                    }

                }
            }
            gridFTPClient.deleteDir(directory);
        }
        catch (Exception e) {
            throw translateException("Cannot delete " + directory, e);
        }
    }

    /** Equivalent to rm file command */
    public void deleteFile(String file) throws FileResourceException {
        try {
            gridFTPClient.deleteFile(file);
        }
        catch (Exception e) {
            throw translateException("Cannot delete " + file, e);
        }
    }

    /** get a remote file to the local stream */
    public void get(String remoteFileName, DataSink sink,
            MarkerListener mListener) throws FileResourceException {
        try {
            gridFTPClient.setPassiveMode(true);
            gridFTPClient.get(remoteFileName, sink, mListener);
        }
        catch (Exception e) {
            throw translateException("Cannot retrieve " + remoteFileName, e);
        }
    }

    /** get a remote file */
    public void get(String remoteFileName, File localFile)
            throws FileResourceException {
        try {
            gridFTPClient.setPassiveMode(true);
            gridFTPClient.get(remoteFileName, localFile);
        }
        catch (Exception e) {
            throw translateException("Cannot retrieve " + remoteFileName
                    + " to " + localFile, e);
        }

    }

    public void getFile(String remoteFileName, String localFileName)
            throws FileResourceException {
        getFile(remoteFileName, localFileName, null);
    }

    /** Equivalent to cp/copy command */
    public void getFile(String remoteFileName, String localFileName,
            final ProgressMonitor progressMonitor) throws FileResourceException {
        File localFile = new File(localFileName);
        try {
            gridFTPClient.setPassiveMode(true);
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
            gridFTPClient.get(remoteFileName, sink, null);
        }
        catch (Exception e) {
            throw translateException("Exception in getFile", e);
        }
    }

    public void putFile(String localFileName, String remoteFileName)
            throws FileResourceException {
        putFile(localFileName, remoteFileName, null);
    }

    /** Copy a local file to a remote file. Default option 'overwrite' */
    public void putFile(String localFileName, String remoteFileName,
            final ProgressMonitor progressMonitor) throws FileResourceException {

        final File localFile = new File(localFileName);
        try {
            gridFTPClient.setPassiveMode(true);
            final long size = localFile.length();
            DataSource source;
            if (progressMonitor != null) {
                source = new DataSourceStream(new FileInputStream(localFile)) {
                    public Buffer read() throws IOException {
                        progressMonitor.progress(totalRead, size);
                        return super.read();
                    }
                    
                    public long totalSize() {
                        return localFile.length();
                    }
                };
            }
            else {
                source = new DataSourceStream(new FileInputStream(localFile)) {
                    public long totalSize() {
                        return localFile.length();
                    }
                };
            }
            gridFTPClient.put(remoteFileName, source, null, false);
        }
        catch (Exception e) {
            throw translateException(e);
        }
    }

    /** put a local file into remote resource */
    public void put(File localFile, String remoteFileName, boolean append)
            throws FileResourceException {

        try {
            gridFTPClient.setPassiveMode(true);
            gridFTPClient.put(localFile, remoteFileName, append);
        }
        catch (Exception e) {
            throw translateException("Cannot transfer " + localFile + " to "
                    + remoteFileName, e);
        }
    }

    /**
     * put the input from a stream into a remote resource. unique to gridftp
     * file resource.
     */
    public void put(DataSource source, String remoteFileName,
            MarkerListener mListener) throws FileResourceException {
        try {
            gridFTPClient.setPassiveMode(true);
            gridFTPClient.put(remoteFileName, source, mListener);
        }
        catch (Exception e) {
            throw translateException("Cannot transfer to " + remoteFileName, e);
        }
    }

    /**
     * Rename a remote file.
     */
    public void rename(String remoteFileName1, String remoteFileName2)
            throws FileResourceException {
        try {
            gridFTPClient.rename(remoteFileName1, remoteFileName2);
        }
        catch (Exception e) {
            throw translateException("Renaming of " + remoteFileName1 + " to "
                    + remoteFileName2 + " failed", e);
        }
    }

    /**
     * Changes the permissions on the file if authorized to do so
     */
    public void changeMode(String filename, int mode)
            throws FileResourceException {
        String cmd = "chmod " + mode + " " + filename; // or something else
        try {
            gridFTPClient.site(cmd);
        }
        catch (Exception e) {
            throw translateException("Cannot change the file permissions for "
                    + filename, e);
        }
    }

    /** Returns true if the file exists */
    public boolean exists(String filename) throws FileResourceException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("exists(" + filename + ")");
            }
            return gridFTPClient.exists(filename);
        }
        catch (ServerException e) {
            return false;
        }
        catch (Exception e) {
            throw translateException(e);
        }
    }

    /**
     * Is this filename a directory. works if user has permissions to change to
     * the given directory
     */
    public boolean isDirectory(String dirName) throws FileResourceException {
        boolean isDir = true;
        String currentDirectory = getCurrentDirectory();
        try {
            setCurrentDirectory(dirName);
        }
        catch (Exception e) {
            isDir = false;
        }
        finally {
            try {
                setCurrentDirectory(currentDirectory);
            }
            catch (Exception e) {
                // do nothihng
            }
        }
        return isDir;
    }

    /** get remote file information */
    public GridFile getGridFile(String fileName) throws FileResourceException {
        try {
            MlsxEntry e = gridFTPClient.mlst(fileName);
            return createGridFile(e);
        }
        catch (Exception e) {
            throw translateException("Failed to retrieve file information about " + fileName, e);
        }
    }

    /** change permissions to a remote file */
    public void changeMode(GridFile newGridFile) throws FileResourceException {

        String newPermissions = newGridFile.getUserPermissions().toString()
                + newGridFile.getGroupPermissions().toString()
                + newGridFile.getAllPermissions().toString();

        logger.error(newGridFile.getAbsolutePathName());

        changeMode(newGridFile.getAbsolutePathName(), Integer
                .parseInt(newPermissions));
    }

    /** Not implemented in GridFTP * */
    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException {
        throw new UnsupportedOperationException("sumbit");
    }

    /** create the file information object */
    private GridFile createGridFile(FileInfo fi) throws FileResourceException {

        GridFile gridFile = new GridFileImpl();

        String directory = getCurrentDirectory();
        if (directory.endsWith("/")) {
            gridFile.setAbsolutePathName(directory + fi.getName());
        }
        else {
            gridFile.setAbsolutePathName(directory + "/" + fi.getName());
        }

        gridFile.setLastModified(fi.getDate());

        if (fi.isFile()) {
            gridFile.setFileType(GridFile.FILE);
        }
        if (fi.isDirectory()) {
            gridFile.setFileType(GridFile.DIRECTORY);
        }
        if (fi.isDevice()) {
            gridFile.setFileType(GridFile.DEVICE);
        }
        // Grr. softlink and all the other ones are orthogonal
        if (fi.isSoftLink()) {
            gridFile.setFileType(GridFile.SOFTLINK);
        }

        gridFile.setMode(fi.getModeAsString());
        gridFile.setName(fi.getName());
        gridFile.setSize(fi.getSize());

        gridFile.setUserPermissions(getPermissions(fi.userCanRead(), fi
                .userCanWrite(), fi.userCanExecute()));
        gridFile.setGroupPermissions(getPermissions(fi.groupCanRead(), fi
                .groupCanWrite(), fi.groupCanExecute()));
        gridFile.setAllPermissions(getPermissions(fi.allCanRead(), fi
                .allCanWrite(), fi.allCanExecute()));

        return gridFile;
    }
    
    private GridFile createGridFile(MlsxEntry e) throws FileResourceException {

        GridFile gridFile = new GridFileImpl();

        String directory = getCurrentDirectory();
        if (directory.endsWith("/")) {
            gridFile.setAbsolutePathName(directory + e.getFileName());
        }
        else {
            gridFile.setAbsolutePathName(directory + "/" + e.getFileName());
        }

        gridFile.setLastModified(e.get(MlsxEntry.MODIFY));

        String type = e.get(MlsxEntry.TYPE);
        if (MlsxEntry.TYPE_FILE.equals(type)) {
            gridFile.setFileType(GridFile.FILE);
        }
        if (MlsxEntry.TYPE_DIR.equals(type) || MlsxEntry.TYPE_PDIR.equals(type) || MlsxEntry.TYPE_CDIR.equals(type)) {
            gridFile.setFileType(GridFile.DIRECTORY);
        }

        gridFile.setName(e.getFileName());
        gridFile.setSize(Long.parseLong(e.get(MlsxEntry.SIZE)));

        return gridFile;
    }

    protected Permissions getPermissions(boolean r, boolean w, boolean x) {
        Permissions perm = new PermissionsImpl();
        perm.setRead(r);
        perm.setWrite(w);
        perm.setExecute(x);
        return perm;
    }

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

    protected GridFTPClient getGridFTPClient() {
        return gridFTPClient;
    }
}