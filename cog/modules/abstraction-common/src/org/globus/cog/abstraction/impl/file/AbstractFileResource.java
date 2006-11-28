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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.GridResource;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public abstract class AbstractFileResource implements FileResource {
    private String name;
    private Map attributes;
    private ServiceContact serviceContact;
    private SecurityContext securityContext;
    private Identity identity;
    private final int type = GridResource.FILE;
    private String protocol;
    private boolean started;

    protected AbstractFileResource() {
        this(null, null, null, null);
    }

    protected AbstractFileResource(String name, String protocol,
            ServiceContact serviceContact, SecurityContext securityContext) {
        attributes = new HashMap();
        identity = new IdentityImpl();
        this.name = name;
        this.protocol = protocol;
        this.serviceContact = serviceContact;
        this.securityContext = securityContext;
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
        this.serviceContact = serviceContact;
    }

    /** return service contact */
    public ServiceContact getServiceContact() {
        return serviceContact;
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
        this.securityContext = securityContext;
    }

    /** Get the securityContext for the remote resource */
    public SecurityContext getSecurityContext() {
        return this.securityContext;
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

    public Collection getAttributeNames() {
        return this.attributes.keySet();
    }

    /** get attribute */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    /** Equivalent to the cp -r command for copying directories */
    public void getDirectory(String remoteDirName, String localDirName)
            throws FileResourceException, IOException {
        File localDir = new File(localDirName);
        GridFile gridFile = null;

        if (!localDir.exists()) {
            localDir.mkdir();
        }

        if (isDirectory(remoteDirName) == false) {
            throw new DirectoryNotFoundException("Remote directory not found");
        }

        for (Iterator iterator = list(remoteDirName).iterator(); iterator
                .hasNext();) {
            gridFile = (GridFile) iterator.next();
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
            throws FileResourceException, IOException {
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
            String[] localFileNames) throws FileResourceException, IOException {

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
            throws FileResourceException, IOException {
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
            throws FileResourceException, IOException {

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
            String[] remoteFileNames) throws FileResourceException, IOException {
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

    public void createDirectories(String dir) throws FileResourceException,
            IOException {
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
}
