// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file.http;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.file.AbstractFileResource;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.FileFragment;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.ProgressMonitor;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;

/**
 * Implements file resource API for webdav resource Supports only absolute path
 * names
 */
public class FileResourceImpl extends AbstractFileResource {
    public static final Logger logger = Logger
            .getLogger(FileResourceImpl.class);

    private HttpClient client;
    private String contact, cwd;

    public FileResourceImpl() throws Exception {
        this(null, null, null);
    }

    public FileResourceImpl(String name, ServiceContact serviceContact,
            SecurityContext securityContext) {
        super(name, "http", serviceContact, securityContext);
    }

    public void start() throws IllegalHostException,
            InvalidSecurityContextException, FileResourceException {
        ServiceContact serviceContact = getAndCheckServiceContact();
        
        contact = "http://" + serviceContact.getContact().toString();
        cwd = "";
        client = new HttpClient();
        setStarted(true);
    }

    public void stop() throws FileResourceException {
        setStarted(false);
    }

    public void setCurrentDirectory(String directory)
            throws FileResourceException {
        cwd = directory;
    }

    public String getCurrentDirectory() throws FileResourceException {
        return cwd;
    }

    public Collection<GridFile> list() throws FileResourceException {
        return list(cwd);
    }

    public Collection<GridFile> list(String directory) throws FileResourceException {
        // hmm
        return Collections.emptyList();
    }

    public void createDirectory(String directory) throws FileResourceException {
        throw new UnsupportedOperationException("createDirectory");
    }

    public void deleteDirectory(String directory, boolean force)
            throws FileResourceException {
        throw new UnsupportedOperationException("deleteDirectory");
    }

    public void deleteFile(String file) throws FileResourceException {
        throw new UnsupportedOperationException("deleteFile");
    }

    public void getFile(FileFragment remote, FileFragment local,
            ProgressMonitor progressMonitor) throws FileResourceException {
        checkNoPartialTransfers(remote, local, "http");
        GetMethod m = new GetMethod(contact + '/' + remote.getFile());
        try {
            int code = client.executeMethod(m);
            try {
                if (code != HttpStatus.SC_OK) {
                    throw new FileResourceException("Failed to get "
                            + remote.getFile() + " from " + contact
                            + ". Server returned " + code + " ("
                            + HttpStatus.getStatusText(code) + ").");
                }
                long total = -1;
                Header clh = m.getResponseHeader("Content-Length");
                if (clh != null && clh.getValue() != null) {
                    total = Long.parseLong(clh.getValue());
                }
                boolean pm = (progressMonitor != null) && total != -1;
                InputStream is = m.getResponseBodyAsStream();
                FileOutputStream fos = new FileOutputStream(local.getFile());
                byte buf[] = new byte[16384];
                long crt = 0;
                int read = 0;
                while (read != -1) {
                    read = is.read(buf);
                    if (read != -1) {
                        fos.write(buf, 0, read);
                        crt += read;
                        if (pm) {
                            progressMonitor.progress(crt, total);
                        }
                    }
                    else {
                    	if (pm) {
                            progressMonitor.progress(total, total);
                        }
                    }
                }
                fos.close();
            }
            finally {
                m.releaseConnection();
            }
        }
        catch (FileResourceException e) {
            throw e;
        }
        catch (Exception e) {
            throw new FileResourceException(e);
        }
    }


    public void putFile(FileFragment local, FileFragment remote,
            ProgressMonitor progressMonitor) throws FileResourceException {
        throw new UnsupportedOperationException("putFile");
    }

    public void rename(String remoteFileName1, String remoteFileName2)
            throws FileResourceException {
        throw new UnsupportedOperationException("rename");
    }

    public void changeMode(String filename, int mode)
            throws FileResourceException {
        throw new UnsupportedOperationException("changeMode");
    }

    public void changeMode(GridFile newGridFile) throws FileResourceException {
        throw new UnsupportedOperationException("changeMode");
    }

    public GridFile getGridFile(String fileName) throws FileResourceException {
        HeadMethod m = new HeadMethod(contact + '/' + fileName);
        try {
            int code = client.executeMethod(m);
            try {
                if (code != HttpStatus.SC_OK) {
                    throw new FileResourceException("Failed to get file information about "
                            + fileName + " from " + contact
                            + ". Server returned " + code + " ("
                            + HttpStatus.getStatusText(code) + ").");
                }
                GridFile gf = new GridFileImpl();
                gf.setName(fileName);
                Header clh = m.getResponseHeader("Content-Length");
                gf.setSize(Long.parseLong(clh.getValue()));
                return gf;
            }
            finally {
                m.releaseConnection();
            }
        }
        catch (FileResourceException e) {
            throw e;
        }
        catch (Exception e) {
            throw new FileResourceException(e);
        }
    }

    public boolean exists(String filename) throws FileResourceException {
        HeadMethod m = new HeadMethod(contact + '/' + filename);
        try {
            int code = client.executeMethod(m);
            try {
                if (code != HttpStatus.SC_OK) {
                    return false;
                }
                else {
                    return true;
                }
            }
            finally {
                m.releaseConnection();
            }
        }
        catch (Exception e) {
            throw new FileResourceException(e);
        }
    }

    public boolean isDirectory(String dirName) throws FileResourceException {
        return false;
    }

    public void submit(ExecutableObject commandWorkflow)
            throws IllegalSpecException, TaskSubmissionException {
        throw new UnsupportedOperationException("submit");
    }

    public boolean supportsPartialTransfers() {
        return false;
    }

    public boolean supportsThirdPartyTransfers() {
        return false;
    }
}
