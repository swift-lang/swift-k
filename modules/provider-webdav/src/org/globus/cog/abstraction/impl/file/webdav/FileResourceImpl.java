// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file.webdav;

import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.WebdavResource;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.FileResourceUtil;
import org.globus.cog.abstraction.impl.file.GeneralException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.GridResource;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;

/**
 * Implements file resource API for webdav resource Supports only
 * absolute path names
 */
public class FileResourceImpl implements FileResource {
    private final String protocol = FileResource.WebDAV;
    private ServiceContact serviceContact = null;
    private SecurityContext securityContext = null;
    private String name = null;
    private Identity identity = null;
    private final int type = GridResource.FILE;
    private Hashtable attributes = null;
    private WebdavResource davClient = null;
    static Logger logger = Logger.getLogger(FileResourceImpl.class.getName());
    private boolean started;

    /** throws exception */
    public FileResourceImpl() throws Exception {
        this.identity = new IdentityImpl();
        this.attributes = new Hashtable();
        serviceContact = new ServiceContactImpl();
        securityContext = AbstractionFactory.newSecurityContext("WebDAV");
    }

    /** constructor to be used normally */
    public FileResourceImpl(String name, ServiceContact serviceContact,
            SecurityContext securityContext) {
        this.identity = new IdentityImpl();
        this.name = name;
        this.serviceContact = serviceContact;
        this.securityContext = securityContext;
        this.attributes = new Hashtable();
    }

    /** Set the name of the resource */
    public void setName(String name) {
        this.name = name;
    }

    /** Return name of the resource */
    public String getName() {
        return this.name;
    }

    /** Set identity of the resource */
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    /** Return identity of the resource */
    public Identity getIdentity() {
        return this.identity;
    }

    /** Return type = FILE which is defined in GridResource */
    public int getType() {
        return this.type;
    }

    /** return protocol ="http" */
    public String getProtocol() {
        return this.protocol;
    }

    /** set service contact */
    public void setServiceContact(ServiceContact serviceContact) {
        this.serviceContact = serviceContact;
    }

    /** get service contact */
    public ServiceContact getServiceContact() {
        return serviceContact;
    }

    /** Set the appropriate SecurityContext for the FileResource */
    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    /** Get the securityContext for the remote resource */
    public SecurityContext getSecurityContext() {
        return this.securityContext;

    }

    /**
     * Create the davClient and authenticate with the resource. serviceContact
     * should be in the form of a url
     */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, GeneralException {
        try {
            String contact = serviceContact.getContact().toString();
            if (!contact.startsWith("http")) {
                contact = "http://" + contact;
            }
            HttpURL hrl = new HttpURL(contact);
            PasswordAuthentication credentials = (PasswordAuthentication) securityContext
                    .getCredentials();
            String username = credentials.getUserName();
            String password = String.valueOf(credentials.getPassword());
            hrl.setUserinfo(username, password);
            davClient = new WebdavResource(hrl);
            started = true;
        } catch (URIException ue) {
            throw new IllegalHostException(
                    "Error while communicating with the webdav server", ue);
        } catch (Exception e) {
            throw new GeneralException("Cannot connect to the webdav server", e);
        }
    }

    /** Stop the davClient from connecting to the server */
    public void stop() throws GeneralException {
        try {
            davClient.close();
            started = false;
        } catch (Exception e) {
            throw new GeneralException(
                    "Error while stopping the Webdav server", e);
        }
    }

    public boolean isStarted() {
        return started;
    }

    /** Equivalent to cd command */
    public void setCurrentDirectory(String directory)
            throws DirectoryNotFoundException, GeneralException {

        try {
            davClient.setPath(directory);
        } catch (IOException ie) {
            throw new DirectoryNotFoundException(directory
                    + " is not a valid directory", ie);
        } catch (Exception e) {
            throw new GeneralException("Cannot set the current directory", e);
        }
    }

    /** Return Current Directory's name */
    public String getCurrentDirectory() throws GeneralException {
        try {
            return davClient.getPath();
        } catch (Exception e) {
            throw new GeneralException("Cannot get the current directory", e);
        }
    }

    /** Equivalent to ls command in the current directory */
    public Collection list() throws GeneralException {
        Vector listVector = new Vector();
        try {
            if (davClient.isCollection() == true) {
                String[] listArray = davClient.list();
                for (int i = 0; i < listArray.length; i++) {
                    String fileName = getCurrentDirectory() + "/"
                            + listArray[i];
                    listVector.add(createGridFile(fileName));
                }
            } else {
                listVector.add(createGridFile(davClient.getName()));
            }
            return listVector;
        } catch (Exception e) {
            throw new GeneralException(
                    "Cannot list the elements of the current directory", e);
        }
    }

    /** Equivalent to ls command on the given directory */
    public Collection list(String directory) throws DirectoryNotFoundException,
            GeneralException {
        //	Store currentDir
        String currentDirectory = getCurrentDirectory();
        //	Change directory
        setCurrentDirectory(directory);

        Collection list = list();
        //	Come back to original directory
        setCurrentDirectory(currentDirectory);

        return list;
    }

    /** Equivalent to mkdir */
    public void createDirectory(String directory) throws GeneralException {
        try {
            String currentPath = getCurrentDirectory();
            if (davClient.mkcolMethod(directory) == false) {
                throw new GeneralException("Cannot Create Directory");
            }
            setCurrentDirectory(currentPath);
        } catch (Exception e) {
            throw new GeneralException("Cannot create the directory", e);
        }
    }
    
    public void createDirectories(String directory) throws GeneralException {
        FileResourceUtil.createDirectories(this, directory);
    }

    /**
     * Remove directory and its files if force = true. Else remove directory
     * only if empty
     */
    public void deleteDirectory(String directory, boolean force)
            throws DirectoryNotFoundException, GeneralException {
        try {
            Collection list = list(directory);
            if (list == null || force == true) {
                davClient.deleteMethod(directory);
            }
        } catch (Exception e) {
            throw new GeneralException("Cannot delete the given directory", e);
        }
    }

    /** Equivalent to rm command */
    public void deleteFile(String file) throws FileNotFoundException,
            GeneralException {
        try {
            davClient.deleteMethod(file);
        } catch (IOException ie) {
            throw new FileNotFoundException(file + " is not a valid file", ie);
        } catch (Exception e) {
            throw new GeneralException("Cannot delete the given file", e);
        }
    }

    /** Equivalent to cp/copy command */
    public void getFile(String remoteFilename, String localFileName)
            throws FileNotFoundException, GeneralException {

        File localFile = new File(localFileName);
        try {
            davClient.getMethod(remoteFilename, localFile);
        } catch (IOException ie) {
            throw new FileNotFoundException(
                    "The local file or the remote file is not a valid file", ie);
        } catch (Exception e) {
            throw new GeneralException("Cannot retrieve the given file", e);
        }
    }

    /** Copy a local file to a remote file. Default option 'overwrite' */
    public void putFile(String localFileName, String remoteFileName)
            throws FileNotFoundException, GeneralException {

        File localFile = new File(localFileName);
        try {
            davClient.putMethod(remoteFileName, localFile);
        } catch (IOException ie) {
            throw new FileNotFoundException(
                    "The local file or the remote file is not a valid file", ie);
        } catch (Exception e) {
            throw new GeneralException("Cannot transfer the given file", e);
        }
    }

    /** Equivalent to the cp -r command */
    public void getDirectory(String remoteDirName, String localDirName)
            throws DirectoryNotFoundException, GeneralException {
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
            try {
                if (!isDirectory(gridFile.getAbsolutePathName())) {
                    getFile(gridFile.getAbsolutePathName(), localDirName
                            + File.separator + gridFile.getName());
                } else {
                    getDirectory(gridFile.getAbsolutePathName(), localDirName
                            + File.separator + gridFile.getName());
                }
            } catch (Exception ex) {
                throw new GeneralException("General Exception", ex);
            }
        }
    }

    /** Equivalent to cp -r command */
    public void putDirectory(String localDirName, String remoteDirName)
            throws DirectoryNotFoundException, GeneralException {
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
        } catch (FileNotFoundException fe) {
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
            try {
                if (!localFile.isDirectory()) {
                    putFile(localDirName + File.separator + files[index],
                            remoteDirName + "/" + files[index]);
                } else {
                    putDirectory(localDirName + File.separator + files[index],
                            remoteDirName + "/" + files[index]);
                }
            } catch (Exception e) {
                throw new GeneralException("Cannot transfer the directory", e);
            }
        }
    }

    /**
     * mget - copy multiple files from remote server
     */
    public void getMultipleFiles(String[] remoteFileNames, String[] localFileNames)
            throws FileNotFoundException, GeneralException {

        //If list of sources is not equal to list of destinations then error
        if (localFileNames.length != remoteFileNames.length)
            throw new GeneralException(
                    "Number of source and destination file names has to be the same");

        //Check every remote file name. If it is a file use getfile else use
        // getdir
        for (int index = 0; index < remoteFileNames.length; index++) {
            try {
                if (exists(remoteFileNames[index])) {
                    if (isDirectory(remoteFileNames[index]) == false) {
                        getFile(remoteFileNames[index], localFileNames[index]);
                    } else {
                        getDirectory(remoteFileNames[index], localFileNames[index]);
                    }
                }
            } catch (Exception e) {
                throw new GeneralException("Cannot perform mGet", e);
            }
        }

    }

    /**
     * mget - copy multiple files from remote server to local dir
     */
    public void getMultipleFiles(String[] remoteFileNames, String localDirName)
            throws FileNotFoundException, DirectoryNotFoundException,
            GeneralException {

        for (int index = 0; index < remoteFileNames.length; index++) {
            try {
                // Get the file name only to append to localdir
                String remoteFileName = remoteFileNames[index]
                        .substring(remoteFileNames[index].lastIndexOf("/") + 1);

                //				Check every remote file name. If it is a file use getfile
                // else use getdir
                if (exists(remoteFileNames[index])) {
                    if (isDirectory(remoteFileNames[index]) == false) {

                        getFile(remoteFileNames[index], localDirName
                                + File.separator + remoteFileName);
                    } else {
                        getDirectory(remoteFileNames[index], localDirName
                                + File.separator + remoteFileName);
                    }
                }
            } catch (Exception e) {
                throw new GeneralException("Cannot perform mGet", e);
            }
        }
    }

    /**
     * mput - copy multiple files from local machine to remote destinations
     */
    public void putMultipleFiles(String[] localFileNames, String[] remoteFileNames)
            throws FileNotFoundException, GeneralException {
        // If list of source not equal to list of destinations then error
        if (localFileNames.length != remoteFileNames.length)
            throw new GeneralException(
                    "Number of source and destination file names has to be the same");

        //Check every file name given. If file is a directory use putdir else
        // use putfile
        for (int index = 0; index < localFileNames.length; index++) {
            File localFile = new File(localFileNames[index]);
            try {
                if (!localFile.isDirectory()) {
                    putFile(localFileNames[index], remoteFileNames[index]);
                } else {
                    putDirectory(localFileNames[index], remoteFileNames[index]);
                }
            } catch (Exception e) {
                throw new GeneralException("Cannot perform mput", e);
            }
        }
    }

    /**
     * mput - copy multiple files from local machines to remote directory
     */
    public void putMultipleFiles(String[] localFileNames, String remoteDirName)
            throws FileNotFoundException, DirectoryNotFoundException,
            GeneralException {

        //Check every file name. If file name is a directory use putdir else
        // use putfile
        for (int index = 0; index < localFileNames.length; index++) {
            File localFile = new File(localFileNames[index]);
            try {
                if (!localFile.isDirectory()) {
                    putFile(localFileNames[index], remoteDirName + "/"
                            + localFile.getName());
                } else {
                    putDirectory(localFileNames[index], remoteDirName + "/"
                            + localFile.getName());
                }
            } catch (Exception e) {
                throw new GeneralException("Cannot perform mput", e);
            }
        }
    }

    /**
     * Rename a remote file.
     */
    public void rename(String remoteFileName1, String remoteFileName2)
            throws FileNotFoundException, GeneralException {
        throw new GeneralException("rename not implemented for  webdav");
    }

    /** Changes the permissions on the file if authorized to do so */
    public void changeMode(String filename, int mode) throws FileNotFoundException,
            GeneralException {
        throw new GeneralException(
                "chmod(filename, mode) not implemented for  webdav");
    }

    /** Changes the permissions on the file if authorized to do so */
    public void changeMode(GridFile newGridFile) throws FileNotFoundException,
            GeneralException {
        throw new GeneralException(
                "chmod(GridFile) not implemented for  webdav");
    }

    /** get file information */
    public GridFile getGridFile(String fileName) throws FileNotFoundException,
            GeneralException {
        return createGridFile(fileName);
    }

    /**
     * Returns true if the file exists. uses setCurrentDirectory. return false
     * if the user does not have permissions to access directory
     */
    public boolean exists(String filename) throws FileNotFoundException,
            GeneralException {
        String currentPath = getCurrentDirectory();
        boolean result = false;
        try {
            setCurrentDirectory(filename);
            result = davClient.exists();
            setCurrentDirectory(currentPath);
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    /**
     * Is this filename a directory. uses setCurrentDirectory(). Returns false
     * if the user does not have permissions to access directory
     */
    public boolean isDirectory(String dirName) throws GeneralException {
        boolean isDir = true;
        String currentDirectory = getCurrentDirectory();
        try {
            setCurrentDirectory(dirName);
            if (davClient.isCollection() == false)
                isDir = false;
            else
                isDir = true;
            setCurrentDirectory(currentDirectory);
        } catch (Exception e) {
            isDir = false;
        }
        return isDir;
    }

    /** Not implemented in Webdav * */
    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException {
    }

    /** Set an attribute * */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Enumeration getAllAttributes() {
        return this.attributes.elements();
    }

    /** Get an attribute * */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    /** method to create gridfile */
    private GridFile createGridFile(Object obj) throws GeneralException {

        GridFile gridFile = new GridFileImpl();
        String fileName = (String) obj;

        try {

            String currentPath = getCurrentDirectory();
            davClient.setPath(fileName);
            gridFile.setAbsolutePathName(davClient.getPath());

            gridFile.setLastModified(String.valueOf(new Date(davClient
                    .getGetLastModified())));

            if (davClient.isCollection() == false) {
                gridFile.setFileType(GridFile.FILE);
            }
            if (davClient.isCollection() == true) {
                gridFile.setFileType(GridFile.DIRECTORY);
            }

            gridFile.setName(davClient.getName());
            gridFile.setSize(davClient.getGetContentLength());

            davClient.setPath(currentPath);

        } catch (Exception e) {
            throw new GeneralException("Exception in creating grid file ", e);
        }
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
                } else {
                    removeLocalDirectory(newFile.getAbsolutePath());
                }
            }
        }
        tempFile.delete();
    }

}
