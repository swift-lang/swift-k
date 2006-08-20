//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 10, 2005
 */
package org.globus.cog.abstraction.impl.file.gridftp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.GeneralException;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
import org.globus.cog.abstraction.impl.file.UnixPermissionsImpl;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.ftp.MlsxEntry;

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

	public void start() throws IllegalHostException, InvalidSecurityContextException,
			GeneralException {
		super.start();
		try {
			getGridFTPClient().quote("HELP MLSD");
		}
		catch (Exception e) {
			if (e.getMessage().indexOf("Unknown command MLSD") != -1) {
				old = true;
			}
			else {
				throw new GeneralException(
						"Unexpected exception caught while querying server capabilities", e);
			}
		}
	}

	public Collection list() throws GeneralException {
		if (old) {
			return super.list();
		}
		else {
			try {
				this.getGridFTPClient().setPassiveMode(true);
				Vector v = this.getGridFTPClient().mlsd();
				ArrayList list = new ArrayList();
				Iterator i = v.iterator();
				while (i.hasNext()) {
                    GridFile entry = convertEntry((MlsxEntry) i.next());
                    if (entry != null) {
                        list.add(entry);
                    }
				}
				return list;
			}
			catch (Exception e) {
				throw new GeneralException("Could not get list of files from server", e);
			}
		}
	}

	private GridFile convertEntry(MlsxEntry entry) {
		GridFileImpl gfi = new GridFileImpl();
		gfi.setName(entry.getFileName());
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
			//gfi.setFileType(GridFile.DIRECTORY);
            return null;
		}
		else if ("cdir".equals(type)) {
			//gfi.setFileType(GridFile.DIRECTORY);
            return null;
		}
		else {
			gfi.setFileType(GridFile.UNKNOWN);
		}
		return gfi;
	}

	public Collection list(String directory) throws DirectoryNotFoundException, GeneralException {
		if (old) {
			return super.list(directory);
		}
		else {
			try {
				this.getGridFTPClient().setPassiveMode(true);
				Vector v = this.getGridFTPClient().mlsd(directory);
				ArrayList list = new ArrayList();
				Iterator i = v.iterator();
				while (i.hasNext()) {
                    GridFile gf = convertEntry((MlsxEntry) i.next()); 
                    if (gf != null) {
                        list.add(gf);
                    }
				}
				return list;
			}
			catch (Exception e) {
				throw new GeneralException("Could not get list of files in " + directory
						+ "from server", e);
			}
		}
	}

	public boolean isDirectory(String dirName) throws GeneralException {
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
			catch (Exception e) {
				throw new GeneralException(e.getMessage(), e);
			}
		}
	}
}
