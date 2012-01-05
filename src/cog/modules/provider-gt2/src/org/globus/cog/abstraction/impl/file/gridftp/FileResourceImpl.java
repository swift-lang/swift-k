//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 10, 2005
 */
package org.globus.cog.abstraction.impl.file.gridftp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.impl.file.IrrecoverableResourceException;
import org.globus.cog.abstraction.impl.file.UnixPermissionsImpl;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.ftp.MlsxEntry;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;

public class FileResourceImpl extends
        org.globus.cog.abstraction.impl.file.gridftp.old.FileResourceImpl {

    private boolean old;

    public FileResourceImpl() throws Exception {
        super();
    }

    public FileResourceImpl(String name, ServiceContact serviceContact,
            SecurityContext securityContext) {
        super(name, serviceContact, securityContext);
    }

    public void start() throws IllegalHostException,
            InvalidSecurityContextException, FileResourceException {
        super.start();
        try {
            getGridFTPClient().quote("HELP MLSD");
        }
        catch (IOException e) {
        	throw translateException(e);
        }
        catch (ServerException e) {
            if (e.getMessage().indexOf("Unknown command MLSD") != -1) {
                old = true;
            }
            else {
                throw new IrrecoverableResourceException(
                        "Unexpected exception caught while querying server capabilities",
                        e);
            }
        }
    }

    public Collection<GridFile> list(String directory) throws FileResourceException {
        if (old) {
            if (directory != null) {
                return super.list(directory);
            }
            else {
                return super.list();
            }
        }
        else {
            try {
                initializeDataChannel(RETRIEVE);
                List<?> v;
                if (directory != null) {
                    v = this.getGridFTPClient().mlsd(directory);
                }
                else {
                    v = this.getGridFTPClient().mlsd();
                }
                List<GridFile> list = new ArrayList<GridFile>();
                String cwd = getCurrentDirectory() + "/";
                for (Object e : v) {
                    GridFile entry = convertEntry((MlsxEntry) e, cwd);
                    if (entry != null) {
                        //assert entry.getAbsolutePathName() != null : "convertEntry returned an entry with null absolute path";
                        //assert entry.getName() != null : "convertEntry returned an entry with null name";
                        list.add(entry);
                    }
                }
                return list;
            }
            catch (Exception e) {
                throw translateException(
                        "Could not get list of files from server", e);
            }
        }
    }

    private GridFile convertEntry(MlsxEntry entry, String apn) {
        GridFileImpl gfi = new GridFileImpl();
        gfi.setName(entry.getFileName());
        gfi.setAbsolutePathName(apn+"/"+entry.getFileName());
        String perm = entry.get("unix.mode");
        if (perm != null) {
            gfi.setUserPermissions(new UnixPermissionsImpl(perm.charAt(1)));
            gfi.setGroupPermissions(new UnixPermissionsImpl(perm.charAt(2)));
            gfi.setAllPermissions(new UnixPermissionsImpl(perm.charAt(3)));
        }
        gfi.setLastModified(entry.get("modify"));
        gfi.setSize(Long.parseLong(entry.get("size")));
        String type = entry.get("type");
        if ("dir".equals(type)) {
            gfi.setFileType(GridFile.DIRECTORY);
        }
        else if ("file".equals(type)) {
            gfi.setFileType(GridFile.FILE);
        }
        else if ("pdir".equals(type)) {
            // gfi.setFileType(GridFile.DIRECTORY);
            return null;
        }
        else if ("cdir".equals(type)) {
            // gfi.setFileType(GridFile.DIRECTORY);
            return null;
        }
        else {
            gfi.setFileType(GridFile.UNKNOWN);
        }
        return gfi;
    }

    public Collection<GridFile> list() throws FileResourceException {
        return list(null);
    }

    public boolean isDirectory(String dirName) throws FileResourceException {
        if (old) {
            return super.isDirectory(dirName);
        }
        else {
            /*
             * It's twice as fast as doing a cwd
             */
            try {
                this.getGridFTPClient().setPassiveMode(false);
                MlsxEntry me = this.getGridFTPClient().mlst(dirName);
                return me.get("type").endsWith("dir");
            }
            catch (ClientException e) {
                throw new FileResourceException(e);
            }
            catch (ServerException e) {
                if (e.getMessage() != null
                        && e.getMessage().indexOf("No such file or directory") != -1) {
                    return false;
                }
                else {
                    throw new FileResourceException("Failed to check if "
                            + dirName + " is a directory", e);
                }
            }
            catch (IOException e) {
            	throw new IrrecoverableResourceException(e);
            }
        }
    }
}
