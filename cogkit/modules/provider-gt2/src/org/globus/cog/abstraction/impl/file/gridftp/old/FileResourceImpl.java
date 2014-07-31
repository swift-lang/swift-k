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

package org.globus.cog.abstraction.impl.file.gridftp.old;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
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
import org.globus.cog.abstraction.impl.file.ftp.AbstractFTPFileResource;
import org.globus.cog.abstraction.impl.file.gridftp.DataChannelAuthenticationType;
import org.globus.cog.abstraction.impl.file.gridftp.DataChannelProtectionType;
import org.globus.cog.abstraction.impl.file.gridftp.GridFTPSecurityContext;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileFragment;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
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
import org.globus.ftp.HostPort;
import org.globus.ftp.InputStreamDataSink;
import org.globus.ftp.Marker;
import org.globus.ftp.MarkerListener;
import org.globus.ftp.MlsxEntry;
import org.globus.ftp.OutputStreamDataSource;
import org.globus.ftp.PerfMarker;
import org.globus.ftp.Session;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.PerfMarkerException;
import org.globus.ftp.exception.ServerException;
import org.globus.ftp.vanilla.Reply;
import org.globus.ftp.vanilla.TransferState;
import org.ietf.jgss.GSSCredential;

/**
 * Implements FileResource API for accessing gridftp server Supports relative
 * and absolute path names
 */
public class FileResourceImpl extends AbstractFTPFileResource implements MarkerListener {
    public static final Logger logger = Logger
        .getLogger(FileResourceImpl.class);

    protected static final boolean STORE = true;
    protected static final boolean RETRIEVE = false;

    /**
     * By default JGlobus sets this to 6000 ms. Experience has proved that it
     * may be too low.
     */
    public static final int MAX_REPLY_WAIT_TIME = 30000; // ms

    private GridFTPClient gridFTPClient;
    private boolean dataChannelReuse;
    private boolean dataChannelInitialized;
    private boolean dataChannelDirection;
    
    private int tcpBufferSize;
    private boolean bufferSizeChanged;

    /** throws InvalidProviderException */
    public FileResourceImpl() {
        this(null, null, null);
    }

    /** constructor be used normally */
    public FileResourceImpl(String name, ServiceContact serviceContact,
            SecurityContext securityContext) {
        super((name == null && !(serviceContact == null)) ? serviceContact.toString() : name, 
                "gsiftp", serviceContact, securityContext);
    }

    /**
     * Create the gridFTPClient and authenticate with the resource.
     * 
     * @throws FileResourceException
     * @throws FileResourceException
     */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, FileResourceException {
        
        ServiceContact serviceContact = getAndCheckServiceContact();

        String host = serviceContact.getHost();
        int port = serviceContact.getPort();
        if (port == -1) {
            port = 2811;
        }
        
        if (getName() == null) {
            setName(host + ":" + port);
        }
        
        try {
            SecurityContext securityContext = getOrCreateSecurityContext("gsiftp", serviceContact);            
            
            gridFTPClient = new GridFTPClient(host, port);
            Reply r = gridFTPClient.getLastReply();

            if (logger.isDebugEnabled()) {
                logger.debug("Initial reply: " + r.getMessage());
            }
            if (r != null
                    && r.getMessage().indexOf("Virtual Broken GridFTP Server") != -1 
                    || "false".equals(System.getProperty("gridFTPDataChannelReuse"))) {
                dataChannelReuse = false;
            }
            else {
                dataChannelReuse = true;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Data channel reuse: " + dataChannelReuse);
            }
            gridFTPClient.setClientWaitParams(MAX_REPLY_WAIT_TIME, Session.DEFAULT_WAIT_DELAY);
            
            GSSCredential cred = (GSSCredential) securityContext.getCredentials();
            gridFTPClient.authenticate(cred);
            gridFTPClient.setType(Session.TYPE_IMAGE);
            if (dataChannelReuse) {
                gridFTPClient.setMode(GridFTPSession.MODE_EBLOCK);
            }
            setSecurityOptions(gridFTPClient);

            setStarted(true);
        }
        catch (Exception e) {
            throw translateException(
                "Error connecting to the GridFTP server at " + host + ":" + port, e);
        }
    }

    public boolean getDataChannelReuse() {
        return dataChannelReuse;
    }

    public void setDataChannelReuse(boolean dataChannelReuse) {
        this.dataChannelReuse = dataChannelReuse;
        this.dataChannelInitialized = false;
    }

    protected void initializeDataChannel(boolean mode) throws ClientException,
            ServerException, IOException {
        if (!dataChannelInitialized || !dataChannelReuse
                || dataChannelDirection != mode) {
            if (!dataChannelReuse) {
                //always use passive mode in non-EBLOCK mode
                //to avoid firewall issues
                mode = true;
            }
            gridFTPClient.setPassiveMode(mode);
            dataChannelInitialized = true;
            dataChannelDirection = mode;
        }
    }

    protected void resetDataChannel() {
        dataChannelInitialized = false;
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

    @Override
    public void setAttribute(String name, Object value) {
        if ("tcp.buffer.size".equals(name)) {
            if (value instanceof Number) {
                tcpBufferSize = ((Number) value).intValue();
            }
            else if (value instanceof String) {
                tcpBufferSize = Integer.parseInt((String) value);
            }
            else if (value == null) {
                tcpBufferSize = 0;
            }
            bufferSizeChanged = true;
        }
        super.setAttribute(name, value);
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
    public Collection<GridFile> list() throws FileResourceException {

        List<GridFile> gridFileList = new ArrayList<GridFile>();
        try {
            this.initializeDataChannel(RETRIEVE);
            Enumeration<?> list = gridFTPClient.list().elements();
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
    public Collection<GridFile> list(String directory) throws FileResourceException {
        // Store currentDir
        String currentDirectory = getCurrentDirectory();
        // Change directory
        setCurrentDirectory(directory);
        
        try {
            return list();
        }
        finally {
            // restore original directory
            setCurrentDirectory(currentDirectory);
        }
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

        if (!isDirectory(directory)) {
            throw new DirectoryNotFoundException(directory
                    + " is not a valid directory");
        }

        try {
            if (force == true) {
                for (GridFile f : list(directory)) {
                    if (f.isFile()) {
                        gridFTPClient.deleteFile(directory + "/" + f.getName());
                    }
                    else {
                        if (!(f.getName().equals(".") || f.getName().equals(".."))) {
                            deleteDirectory(directory + "/"
                                    + f.getName(), force);
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
            initializeDataChannel(RETRIEVE);
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
            initializeDataChannel(RETRIEVE);
            gridFTPClient.get(remoteFileName, localFile);
        }
        catch (Exception e) {
            throw translateException("Cannot retrieve " + remoteFileName
                    + " to " + localFile, e);
        }
    }


    /** Equivalent to cp/copy command */
    public void getFile(FileFragment remote, FileFragment local,
            final ProgressMonitor progressMonitor) throws FileResourceException {
        if (local.isFragment()) {
            throw new UnsupportedOperationException("The local file cannot be a fragment");
        }
        File localFile = new File(local.getFile());
        try {
            DataSink sink;
            final long size = getGridFile(remote.getFile()).getSize();
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
            setBufferSize();
            initializeDataChannel(RETRIEVE);
            if (remote.isFragment()) {
                gridFTPClient.extendedGet(remote.getFile(), remote.getOffset(), remote.getLength(), sink, null);
            }
            else {
                gridFTPClient.get(remote.getFile(), sink, null);
            }
        }
        catch (Exception e) {
            throw translateException("Exception in getFile", e);
        }
    }

    /** Copy a local file to a remote file. Default option 'overwrite' */
    public void putFile(FileFragment local, FileFragment remote,
            final ProgressMonitor progressMonitor) throws FileResourceException {
        
        final File localFile = new File(local.getFile());
        try {
            final long size;
            DataSource source;
            FileInputStream fis = new FileInputStream(localFile);
            if (local.isFragment()) {
                fis.skip(local.getOffset());
                if (local.getLength() == FileFragment.MAX_LENGTH) {
                    size = localFile.length();
                }
                else {
                    size = local.getLength();
                }
            }
            else {
                size = localFile.length();
            }
            if (progressMonitor != null) {
                source = new DataSourceStream(fis) {
                    public Buffer read() throws IOException {
                        progressMonitor.progress(totalRead, size);
                        return super.read();
                    }

                    public long totalSize() {
                        return size;
                    }
                };
            }
            else {
                source = new DataSourceStream(fis) {
                    public long totalSize() {
                        return size;
                    }
                };
            }
            setBufferSize();
            initializeDataChannel(STORE);
            if (remote.isFragment()) {
                gridFTPClient.extendedPut(remote.getFile(), remote.getOffset(), source, null);
            }
            else {
                gridFTPClient.put(remote.getFile(), source, null, false);
            }
        }
        catch (Exception e) {
            throw translateException(e);
        }
    }
    
    private void setBufferSize() throws ServerException, IOException {
        if (bufferSizeChanged) {
            if (tcpBufferSize != 0) {
                gridFTPClient.setTCPBufferSize(tcpBufferSize);
            }
            bufferSizeChanged = false;
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
                // do nothing
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
            throw translateException(
                "Failed to retrieve file information about " + fileName, e);
        }
    }

    /** change permissions to a remote file */
    public void changeMode(GridFile newGridFile) throws FileResourceException {

        String newPermissions = newGridFile.getUserPermissions().toString()
                + newGridFile.getGroupPermissions().toString()
                + newGridFile.getWorldPermissions().toString();

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

        gridFile.setUserPermissions(PermissionsImpl.instance(fi.userCanRead(), fi
            .userCanWrite(), fi.userCanExecute()));
        gridFile.setGroupPermissions(PermissionsImpl.instance(fi.groupCanRead(), fi
            .groupCanWrite(), fi.groupCanExecute()));
        gridFile.setWorldPermissions(PermissionsImpl.instance(fi.allCanRead(), fi
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
        if (MlsxEntry.TYPE_DIR.equals(type) || MlsxEntry.TYPE_PDIR.equals(type)
                || MlsxEntry.TYPE_CDIR.equals(type)) {
            gridFile.setFileType(GridFile.DIRECTORY);
        }

        gridFile.setName(e.getFileName());
        gridFile.setSize(Long.parseLong(e.get(MlsxEntry.SIZE)));

        return gridFile;
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
    
    public InputStream openInputStream(String name)
            throws FileResourceException {
        InputStreamDataSink sink = null;
        try {
            initializeDataChannel(RETRIEVE);
            
            sink = new InputStreamDataSink();

            TransferState state = gridFTPClient.asynchGet(name, sink, null);
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
            initializeDataChannel(STORE);
            
            source = new OutputStreamDataSource(16384);
            
            TransferState state = gridFTPClient.asynchPut(name, source, null, false);        
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
        return true;
    }

    public boolean supportsThirdPartyTransfers() {
        return true;
    }

    @Override
    public void thirdPartyTransfer(FileResource sourceResource,
            FileFragment source, FileFragment destination)
            throws FileResourceException {
        if (!(sourceResource instanceof FileResourceImpl)) {
            throw new IllegalArgumentException("The source resource must be " + getName());
        }
        FileResourceImpl srcr = (FileResourceImpl) sourceResource;
        try {
            srcr.gridFTPClient.setType(Session.TYPE_IMAGE);
            this.gridFTPClient.setType(Session.TYPE_IMAGE);
            
            srcr.gridFTPClient.setMode(GridFTPSession.MODE_EBLOCK);
            this.gridFTPClient.setMode(GridFTPSession.MODE_EBLOCK);
            
            setBufferSize();
            if (tcpBufferSize != 0) {
                srcr.gridFTPClient.setTCPBufferSize(tcpBufferSize);
            }
            
            lastBytesTransfered = new ArrayList<Long>();
            
            HostPort hp = this.gridFTPClient.setPassive();
            srcr.gridFTPClient.setActive(hp);
            
            srcr.gridFTPClient.extendedTransfer(source.getFile(),
                source.getOffset(), source.getLength(), this.gridFTPClient,
                destination.getFile(), destination.getOffset(), logger.isDebugEnabled() ? this : null);
        }
        catch (Exception e) {
            throw translateException("Transfer failed", e);
        }
    }
    
    private List<Long> lastBytesTransfered = new ArrayList<Long>();
    private static long lastBytes = 0, crtBytes = 0;
    private static long lastTime = 0, firstTime = System.currentTimeMillis();

    public synchronized void markerArrived(Marker m) {
        if (m instanceof PerfMarker) {
            PerfMarker pm = (PerfMarker) m;
            int stripe = 0;
            try {
                if (pm.hasStripeIndex()) {
                    stripe = (int) pm.getStripeIndex();
                }
                while (lastBytesTransfered.size() <= stripe) {
                    lastBytesTransfered.add(0L);
                }
                if (pm.hasStripeBytesTransferred()) {
                    long crt = pm.getStripeBytesTransferred();
                    crtBytes += crt - lastBytesTransfered.get(stripe);
                    lastBytesTransfered.set(stripe, crt);
                }
                long now = System.currentTimeMillis();
                if (now - lastTime > 1000) {
                    String msg = "[GridFTP bandwidth] running average: " + units((crtBytes - lastBytes) / (now - lastTime) * 1000) + 
                            "B/s, average: " + units(crtBytes / (now - firstTime + 1) * 1000) + 
                            "B/s, total: " + units(crtBytes) + "B, per window: " + units(crtBytes - lastBytes) +
                            "B, window: " + (now - lastTime) / 1000 + 
                            "s, time: " + (now - firstTime) / 1000 + "s";
                    logger.debug(msg);
                    lastTime = now;
                    lastBytes = crtBytes;
                }
            }
            catch (PerfMarkerException e) {
                logger.info("Cannot get performance marker information", e);
            }
        }
    }
    
    private static final String[] U = { "", "K", "M", "G" };
    private static final NumberFormat NF = new DecimalFormat("###.##");

    public static String units(long v) {
        double dv = v;
        int index = 0;
        while (dv > 1024 && index < U.length - 1) {
            dv = dv / 1024;
            index++;
        }
        return NF.format(dv) + " " + U[index];
    }    
}
