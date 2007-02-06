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
import java.util.Vector;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.WebdavResource;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.AbstractFileResource;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.impl.file.IrrecoverableResourceException;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.ProgressMonitor;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;

/**
 * Implements file resource API for webdav resource Supports only absolute path
 * names
 */
public class FileResourceImpl extends AbstractFileResource {
    private WebdavResource davClient = null;
    public static final Logger logger = Logger.getLogger(FileResourceImpl.class
            .getName());

    /** throws exception */
    public FileResourceImpl() throws Exception {
        this(null, new ServiceContactImpl(), AbstractionFactory
                .newSecurityContext("WebDAV"));
    }

    /** constructor to be used normally */
    public FileResourceImpl(String name, ServiceContact serviceContact,
            SecurityContext securityContext) {
        super(name, FileResource.WebDAV, serviceContact, securityContext);
    }

    /**
     * Create the davClient and authenticate with the resource. serviceContact
     * should be in the form of a url
     */
    public void start() throws IllegalHostException,
            InvalidSecurityContextException, FileResourceException {
        try {
            String contact = getServiceContact().getContact().toString();
            if (!contact.startsWith("http")) {
                contact = "http://" + contact;
            }
            HttpURL hrl = new HttpURL(contact);
            PasswordAuthentication credentials = (PasswordAuthentication) getSecurityContext()
                    .getCredentials();
            String username = credentials.getUserName();
            String password = String.valueOf(credentials.getPassword());
            hrl.setUserinfo(username, password);
            davClient = new WebdavResource(hrl);
            setStarted(true);
        }
        catch (URIException ue) {
            throw new IllegalHostException(
                    "Error while communicating with the webdav server", ue);
        }
        catch (IOException e) {
            throw new IrrecoverableResourceException(e);
        }
    }

    /** Stop the davClient from connecting to the server */
    public void stop() throws FileResourceException {
        try {
            davClient.close();
            setStarted(false);
        }
        catch (IOException e) {
            throw new IrrecoverableResourceException(e);
        }
    }

    /** Equivalent to cd command */
    public void setCurrentDirectory(String directory)
            throws FileResourceException {
        try {
            davClient.setPath(directory);
        }
        catch (IOException e) {
            throw new IrrecoverableResourceException(e);
        }
    }

    /** Return Current Directory's name */
    public String getCurrentDirectory() throws FileResourceException {
        return davClient.getPath();
    }

    /** Equivalent to ls command in the current directory */
    public Collection list() throws FileResourceException {
        Vector listVector = new Vector();

        if (davClient.isCollection() == true) {
            String[] listArray = davClient.list();
            for (int i = 0; i < listArray.length; i++) {
                String fileName = getCurrentDirectory() + "/" + listArray[i];
                listVector.add(createGridFile(fileName));
            }
        }
        else {
            listVector.add(createGridFile(davClient.getName()));
        }
        return listVector;
    }

    /** Equivalent to ls command on the given directory */
    public Collection list(String directory) throws FileResourceException {
        // Store currentDir
        String currentDirectory = getCurrentDirectory();
        // Change directory
        setCurrentDirectory(directory);

        Collection list = list();
        // Come back to original directory
        setCurrentDirectory(currentDirectory);

        return list;
    }

    /** Equivalent to mkdir */
    public void createDirectory(String directory) throws FileResourceException {
        try {
            String currentPath = getCurrentDirectory();
            if (davClient.mkcolMethod(directory) == false) {
                throw new FileResourceException("Failed to create directory");
            }
            setCurrentDirectory(currentPath);
        }
        catch (IOException e) {
            throw new IrrecoverableResourceException(e);
        }
    }

    /**
     * Remove directory and its files if force = true. Else remove directory
     * only if empty
     */
    public void deleteDirectory(String directory, boolean force)
            throws FileResourceException {
        try {
            Collection list = list(directory);
            if (list == null || force == true) {
                davClient.deleteMethod(directory);
            }
        }
        catch (IOException e) {
            throw new IrrecoverableResourceException(e);
        }
    }

    /** Equivalent to rm command */
    public void deleteFile(String file) throws FileResourceException {
        try {
            davClient.deleteMethod(file);
        }
        catch (IOException e) {
            throw new IrrecoverableResourceException(e);
        }
    }

    public void getFile(String remoteFilename, String localFileName)
            throws FileResourceException {
        getFile(remoteFilename, localFileName, null);
    }

    /** Equivalent to cp/copy command */
    public void getFile(String remoteFilename, String localFileName,
            ProgressMonitor progressMonitor) throws FileResourceException {
        try {
            File localFile = new File(localFileName);
            davClient.getMethod(remoteFilename, localFile);
        }
        catch (IOException e) {
            throw new IrrecoverableResourceException(e);
        }
    }

    public void putFile(String localFileName, String remoteFileName)
            throws FileResourceException {
        putFile(localFileName, remoteFileName, null);
    }

    /** Copy a local file to a remote file. Default option 'overwrite' */
    public void putFile(String localFileName, String remoteFileName,
            ProgressMonitor progressMonitor) throws FileResourceException {
        try {
            File localFile = new File(localFileName);
            davClient.putMethod(remoteFileName, localFile);
        }
        catch (IOException e) {
            throw new IrrecoverableResourceException(e);
        }
    }

    /**
     * Rename a remote file.
     */
    public void rename(String remoteFileName1, String remoteFileName2)
            throws FileResourceException {
        throw new UnsupportedOperationException(
                "rename not implemented for  webdav");
    }

    /** Changes the permissions on the file if authorized to do so */
    public void changeMode(String filename, int mode)
            throws FileResourceException {
        throw new UnsupportedOperationException(
                "chmod(filename, mode) not implemented for  webdav");
    }

    /** Changes the permissions on the file if authorized to do so */
    public void changeMode(GridFile newGridFile) throws FileResourceException {
        throw new UnsupportedOperationException(
                "chmod(GridFile) not implemented for  webdav");
    }

    /** get file information */
    public GridFile getGridFile(String fileName) throws FileResourceException {
        return createGridFile(fileName);
    }

    /**
     * Returns true if the file exists. uses setCurrentDirectory. return false
     * if the user does not have permissions to access directory
     */
    public boolean exists(String filename) throws FileResourceException {
        String currentPath = getCurrentDirectory();
        boolean result = false;

        setCurrentDirectory(filename);
        result = davClient.exists();
        setCurrentDirectory(currentPath);

        return result;
    }

    /**
     * Is this filename a directory. uses setCurrentDirectory(). Returns false
     * if the user does not have permissions to access directory
     */
    public boolean isDirectory(String dirName) throws FileResourceException {
        boolean isDir = true;
        String currentDirectory = getCurrentDirectory();

        setCurrentDirectory(dirName);
        if (davClient.isCollection() == false)
            isDir = false;
        else
            isDir = true;
        setCurrentDirectory(currentDirectory);

        return isDir;
    }

    /** Not implemented in Webdav * */
    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException {
        throw new UnsupportedOperationException();
    }

    /** method to create gridfile */
    private GridFile createGridFile(Object obj) throws FileResourceException {
        try {
            GridFile gridFile = new GridFileImpl();
            String fileName = (String) obj;

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

            return gridFile;
        }
        catch (IOException e) {
            throw new IrrecoverableResourceException(e);
        }
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

}
