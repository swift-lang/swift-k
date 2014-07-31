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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 28, 2006
 */
package org.globus.cog.abstraction.impl.file;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.FileFragment;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.GridResource;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.ProgressMonitor;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public abstract class AbstractFileResource implements FileResource {
    public static final Logger logger = Logger.getLogger(AbstractFileResource.class);
    
    private String name;
    private Map<String, Object> attributes;
    private Service service;
    private Identity identity;
    private final int type = GridResource.FILE;
    private String protocol;
    private boolean started;
    
    protected AbstractFileResource(String name, String protocol,
            ServiceContact serviceContact, SecurityContext securityContext) {
        this(name, protocol, new ServiceImpl(protocol, serviceContact, securityContext));
    }
    
    protected AbstractFileResource(String name, String protocol,
            Service service) {
        attributes = new HashMap<String, Object>();
        identity = new IdentityImpl();
        this.name = name;
        if (protocol == null) {
            throw new NullPointerException();
        }
        this.protocol = protocol;
        this.service = service;
    }
    
    protected ServiceContact getAndCheckServiceContact() throws IllegalHostException {
        ServiceContact serviceContact = getServiceContact();
        if (serviceContact == null) {
            throw new IllegalHostException("No service contact specified");
        }
        return serviceContact;
    }
    
    protected SecurityContext getOrCreateSecurityContext(String provider, ServiceContact serviceContact) 
    throws InvalidProviderException, ProviderMethodException {
        SecurityContext securityContext = getSecurityContext();
        if (securityContext == null) {
            securityContext = AbstractionFactory.getSecurityContext("gsiftp", serviceContact);
        }
        return securityContext;
    }
    
    protected PasswordAuthentication getCredentialsAsPasswordAuthentication(SecurityContext securityContext) throws InvalidSecurityContextException {
        Object credentials = securityContext.getCredentials();
        if (credentials == null) {
            if (logger.isInfoEnabled()) {
                logger.info(name + ": credentials are null; using default username/password.");
            }
            
            return getDefaultUsernameAndPassword();
        }
        if (!(credentials instanceof PasswordAuthentication)) {
            throw new InvalidSecurityContextException("FTP only supports password authentication. Credentials supplied: " + 
                credentials.getClass().getName());
        }
        return (PasswordAuthentication) credentials;
    }
    
    protected PasswordAuthentication getDefaultUsernameAndPassword() {
        return new PasswordAuthentication("", new char[0]);
    }

    /** Set the name of the resource */
    public void setName(String name) {
        this.name = name;
    }

    /** Return name of the resource */
    public String getName() {
        return this.name;
    }

    /** set service contact */
    public void setServiceContact(ServiceContact serviceContact) {
        service.setServiceContact(serviceContact);
    }

    /** return service contact */
    public ServiceContact getServiceContact() {
        return service.getServiceContact();
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

    /** return protocol */
    public String getProtocol() {
        return this.protocol;
    }

    /** Set the appropriate SecurityContext for the FileResource */
    public void setSecurityContext(SecurityContext securityContext) {
        service.setSecurityContext(securityContext);
    }

    /** Get the securityContext for the remote resource */
    public SecurityContext getSecurityContext() {
        return service.getSecurityContext();
    }
    
    public Service getService() {
        return service;
    }
    
    public void setService(Service service) {
        this.service = service;
    }

    public boolean isStarted() {
        return started;
    }

    protected synchronized void setStarted(boolean started) {
        this.started = started;
    }

    /** set attribute for ftp resource */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Collection<String> getAttributeNames() {
        return this.attributes.keySet();
    }

    /** get attribute */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public void getFile(String remoteFileName, String localFileName)
            throws FileResourceException {
        getFile(new FileFragmentImpl(remoteFileName), new FileFragmentImpl(localFileName));
    }

    public void getFile(String remoteFileName, String localFileName,
            ProgressMonitor progressMonitor) throws FileResourceException {
        getFile(new FileFragmentImpl(remoteFileName), new FileFragmentImpl(localFileName), progressMonitor);
    }

    public void getFile(FileFragment remote, FileFragment local)
            throws FileResourceException {
        getFile(remote, local, null);
    }

    public void putFile(String localFileName, String remoteFileName)
            throws FileResourceException {
        putFile(new FileFragmentImpl(localFileName), new FileFragmentImpl(remoteFileName));
    }

    public void putFile(String localFileName, String remoteFileName,
            ProgressMonitor progressMonitor) throws FileResourceException {
        putFile(new FileFragmentImpl(localFileName), new FileFragmentImpl(remoteFileName), progressMonitor);
    }

    public void putFile(FileFragment local, FileFragment remote)
            throws FileResourceException {
        putFile(local, remote, null);
    }

    /** Equivalent to the cp -r command for copying directories */
    public void getDirectory(String remoteDirName, String localDirName)
            throws FileResourceException {
        File localDir = new File(localDirName);
        GridFile gridFile = null;

        if (!localDir.exists()) {
            localDir.mkdir();
        }

        if (isDirectory(remoteDirName) == false) {
            throw new DirectoryNotFoundException("Remote directory not found");
        }

        for (Iterator<GridFile> iterator = list(remoteDirName).iterator(); iterator
                .hasNext();) {
            gridFile = iterator.next();
            if (gridFile.isFile()) {
                getFile(remoteDirName + "/" + gridFile.getName(), localDirName
                        + File.separator + gridFile.getName());
            }
            else {
                getDirectory(remoteDirName + "/" + gridFile.getName(),
                        localDirName + File.separator + gridFile.getName());
            }
        }
    }

    /** Equivalent to cp -r command for copying directories */
    public void putDirectory(String localDirName, String remoteDirName)
            throws FileResourceException {
        File localDir = new File(localDirName);
        if (!localDir.exists()) {
            throw new DirectoryNotFoundException("Local directory not found");
        }

        if (localDir.isFile()) {
            throw new DirectoryNotFoundException(localDirName + "  is a file");
        }

        try {
            if (!exists(remoteDirName)) {
                createDirectory(remoteDirName);
            }
        }
        catch (FileNotFoundException fe) {
            throw new DirectoryNotFoundException(
                    "Cannot create the remote directory: " + remoteDirName);
        }

        if (!isDirectory(remoteDirName)) {
            throw new DirectoryNotFoundException(remoteDirName + " is a file");
        }

        String files[] = localDir.list();
        for (int index = 0; index < files.length; index++) {
            File localFile = new File(localDirName + File.separator
                    + files[index]);
            if (!localFile.isDirectory()) {
                putFile(localDirName + File.separator + files[index],
                        remoteDirName + "/" + files[index]);
            }
            else {
                putDirectory(localDirName + File.separator + files[index],
                        remoteDirName + "/" + files[index]);
            }
        }
    }

    /**
     * mget - Obtain multiple files from the remote server
     */
    public void getMultipleFiles(String[] remoteFileNames,
            String[] localFileNames) throws FileResourceException {

        // If the list of sources not equal to destination lists then error
        if (localFileNames.length != remoteFileNames.length)
            throw new IllegalArgumentException(
                    "Number of source and destination file names has to be the same");

        // Check every remote file name provided. If file, use getfile else use
        // getdir
        for (int index = 0; index < remoteFileNames.length; index++) {
            if (exists(remoteFileNames[index])) {
                if (isDirectory(remoteFileNames[index]) == false) {
                    getFile(remoteFileNames[index], localFileNames[index]);
                }
                else {
                    getDirectory(remoteFileNames[index], localFileNames[index]);
                }
            }
        }

    }

    /**
     * mget - Obtain multiple files from the ftp server
     */
    public void getMultipleFiles(String[] remoteFileNames, String localDirName)
            throws FileResourceException {
        for (int index = 0; index < remoteFileNames.length; index++) {
            // Extract only the file name to be appended to the destination
            // directory
            // in getfile or getdir
            String remoteFileName = remoteFileNames[index]
                    .substring(remoteFileNames[index].lastIndexOf("/") + 1);

            if (exists(remoteFileNames[index])) {
                // Check every remote file name provided. If file, use
                // getfile else use getdir
                if (isDirectory(remoteFileNames[index]) == false) {

                    getFile(remoteFileNames[index], localDirName
                            + File.separator + remoteFileName);
                }
                else {
                    getDirectory(remoteFileNames[index], localDirName
                            + File.separator + remoteFileName);
                }
            }
        }
    }

    /**
     * mput - copy multiple files to the resource
     */
    public void putMultipleFiles(String[] localFileNames, String remoteDirName)
            throws FileResourceException {

        for (int index = 0; index < localFileNames.length; index++) {
            // Check every remote file name provided. If file, use putfile else
            // use putdir
            File localFile = new File(localFileNames[index]);
            if (!localFile.isDirectory()) {
                putFile(localFileNames[index], remoteDirName + "/"
                        + localFile.getName());
            }
            else {
                putDirectory(localFileNames[index], remoteDirName + "/"
                        + localFile.getName());
            }
        }
    }

    /**
     * mput - copy multiple files into the resource server
     */
    public void putMultipleFiles(String[] localFileNames,
            String[] remoteFileNames) throws FileResourceException {
        // If list of sources not equal to list of destinations then error
        if (localFileNames.length != remoteFileNames.length)
            throw new IllegalArgumentException(
                    "Number of source and destination file names has to be the same");

        for (int index = 0; index < localFileNames.length; index++) {
            // Check every local file name provided. If file, use putfile else
            // use putdir
            File localFile = new File(localFileNames[index]);
            if (!localFile.isDirectory()) {
                putFile(localFileNames[index], remoteFileNames[index]);
            }
            else {
                putDirectory(localFileNames[index], remoteFileNames[index]);
            }
        }
    }

    public void createDirectories(String dir) throws FileResourceException {
        // TODO there is an assumption here on the path separators
        // I'd really suggest enforcing only one of them (hint: '/') at the
        // level of the
        // interface
        if (dir.equals("/")) {
            return;
        }
        try {
            if (!exists(dir)) {
                int i = dir.lastIndexOf('/');
                if (i <= 0) {
                    createDirectory(dir);
                }
                else {
                    createDirectories(dir.substring(0, i));
                    if (i != dir.length() - 1) {
                        createDirectory(dir);
                    }
                    else {
                        // trailing '/'
                    }
                }
            }
        }
        catch (FileResourceException e) {
            if (!isDirectory(dir)) {
                throw e;
            }
        }
    }

    public InputStream openInputStream(String name) throws FileResourceException {
        throw new UnsupportedOperationException();
    }

    public OutputStream openOutputStream(String name) throws FileResourceException {
        throw new UnsupportedOperationException();
    }

    public boolean supportsStreams() {
        return false;
    }

    public String toString() {
        return "FileResource: " + name;
    }

    public void thirdPartyTransfer(FileResource sourceResource,
            FileFragment source, FileFragment destination)
            throws FileResourceException {
        throw new UnsupportedOperationException("The " + getName() + " provider does not support third party transfers");
    }
    
    protected void checkNoPartialTransfers(FileFragment f1, FileFragment f2, String name) {
        if (f1.isFragment() || f2.isFragment()) {
            throw new UnsupportedOperationException("The " + name + " provider does not support partial transfers");
        }
    }

    /**
     * Convenience method to implement filtering. Providers should implement their
     * own, more efficient, filtering
     * @throws FileResourceException 
     * @throws DirectoryNotFoundException 
     */
    @Override
    public Collection<GridFile> list(String dir, FileResourceFileFilter filter) 
            throws DirectoryNotFoundException, FileResourceException {
        
        List<GridFile> nl = new ArrayList<GridFile>();
        Collection<GridFile> ol = list(dir);
        for (GridFile f : ol) {
            if (filter.accept(f)) {
                nl.add(f);
            }
        }
        return nl;
    }
}
