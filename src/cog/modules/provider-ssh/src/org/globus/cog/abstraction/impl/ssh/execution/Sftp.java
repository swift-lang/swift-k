
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



/*
 *  Sshtools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.globus.cog.abstraction.impl.ssh.execution;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.ssh.SSHTask;

import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.sftp.SftpFileInputStream;
import com.sshtools.j2ssh.sftp.SftpFileOutputStream;
import com.sshtools.j2ssh.sftp.SftpSubsystemClient;

public class Sftp implements SSHTask {
	static Logger logger = Logger.getLogger(Sftp.class.getName());
	private String dest;
	private String get;
	private String put;
	private String mkdir;
	private String rmdir;
	private String delete;
	private String permissions;

	public void setDest(String dest) {
		this.dest = dest;
	}

	public String getDest() {
		return dest;
	}

	public void setGet(String get) {
		this.get = get;
	}

	public String getGet() {
		return get;
	}

	public void setPut(String put) {
		this.put = put;
	}

	public String getPut() {
		return put;
	}

	public void setMkdir(String mkdir) {
		this.mkdir = mkdir;
	}

	public String getMkdir() {
		return mkdir;
	}

	public void setRmdir(String rmdir) {
		this.rmdir = rmdir;
	}

	public String getRmdir() {
		return rmdir;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setDelete(String delete) {
		this.delete = delete;
	}

	public String getDelete() {
		return delete;
	}

	public void execute(SessionChannelClient session)
		throws
			IllegalSpecException,
			InvalidSecurityContextException,
			InvalidServiceContactException,
			TaskSubmissionException, JobException {
		if ((get != null) && (dest == null)) {
			logger.debug("You must supply a destination for the get operation");
		}

		if ((put != null) && (dest == null)) {
			logger.debug(
				"You must supply a destination and permissions for the put operation");
		}

		if ((get != null) && (put != null)) {
			logger.debug("You cannot specify a get and put together, use seperate tasks");
		}
		try {
			SftpSubsystemClient sftp = new SftpSubsystemClient();

			if (!session.startSubsystem(sftp)) {
				throw new TaskSubmissionException("Failed to start the SFTP subsystem");
			}

			executeSFTP(sftp);
		}
		catch (IOException sshe) {
			logger.debug(sshe);
			throw new TaskSubmissionException("SSH Connection failed: " + sshe.getMessage());
		}
	}

	public void executeSFTP(SftpSubsystemClient sftp) throws TaskSubmissionException {

		String rmdir = getRmdir();

		if (rmdir != null) {
			logger.debug("Deleting directory " + rmdir);

			try {
				SftpFile file = sftp.openDirectory(rmdir);
				file.close();
				sftp.removeDirectory(rmdir);
				logger.debug("Deleted directory");
			}
			catch (IOException ioe) {
				logger.debug("Directory does not exist!");
			}
		}

		String mkdir = getMkdir();

		if (mkdir != null) {
			logger.debug("Creating directory " + mkdir);

			try {
				sftp.openDirectory(mkdir);
				logger.debug("Directory already exists!");
			}
			catch (IOException ioe) {
				try {
					sftp.makeDirectory(mkdir);
					logger.debug("Directory created");
				}
				catch (IOException ioe2) {
					logger.debug("mkdir failed: " + ioe2.getMessage());
				}
			}
		}

		String delete = getDelete();

		if (delete != null) {
			logger.debug("Deleting file " + delete);

			try {
				SftpFile file = sftp.openFile(delete, SftpSubsystemClient.OPEN_READ);
				file.close();
				sftp.removeFile(delete);
				logger.debug("File deleted");
			}
			catch (IOException ioe) {
				logger.debug("File does not exist!");
			}
		}

		String get = getGet();
		String dest = getDest();
		String put = getPut();
		String permissions = getPermissions();

		if ((get != null) && (dest != null)) {
			logger.debug("Getting " + get + " into " + dest);

			try {
				SftpFile file = sftp.openFile(get, SftpSubsystemClient.OPEN_READ);
				byte[] buffer = new byte[65535];
				BufferedInputStream in = new BufferedInputStream(new SftpFileInputStream(file));
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
				int read;

				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}

				in.close();
				out.close();
				logger.debug("Get complete");
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				throw new TaskSubmissionException("get failed: " + ioe.getMessage());
			}
		}

		if ((put != null) && (dest != null)) {
			logger.debug("Putting " + put + " into " + dest);

			try {
				FileAttributes attrs = new FileAttributes();

				// Open with rw as setting all permissiosn does not work untill we have created the file
				//
				logger.debug("Creating " + dest + " with default rw permissions");
				attrs.setPermissions("rw");

				SftpFile file =
					sftp.openFile(
						dest,
						SftpSubsystemClient.OPEN_WRITE
							| SftpSubsystemClient.OPEN_CREATE
							| SftpSubsystemClient.OPEN_TRUNCATE,
						attrs);

				if (permissions != null) {
					logger.debug("Setting " + put + " permissions to " + permissions);
					attrs = sftp.getAttributes(file);
					attrs.setPermissions(permissions);
					sftp.setAttributes(file, attrs);
				}

				byte[] buffer = new byte[65535];
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(put));
				BufferedOutputStream out = new BufferedOutputStream(new SftpFileOutputStream(file));
				int read;

				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}

				in.close();
				out.close();
				logger.debug("Put complete");

			}
			catch (IOException ioe) {
				logger.debug(ioe);
				throw new TaskSubmissionException("put failed: " + ioe.getMessage());
			}
		}
	}
}
