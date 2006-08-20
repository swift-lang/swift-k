// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Sshtools - Java SSH2 API
 * 
 * Copyright (C) 2002 Lee David Painter.
 * 
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.globus.cog.abstraction.impl.execution.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;

import com.sshtools.j2ssh.session.SessionChannelClient;

public class Exec extends Ssh {
	static Logger logger = Logger.getLogger(Exec.class.getName());
	private String cmd;
	private String dir;
	private StringBuffer taskOutput = new StringBuffer();
	private StringBuffer taskError = new StringBuffer();

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getCmd() {
		return cmd;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String string) {
		dir = string;
	}

	public void execute() throws IllegalSpecException, InvalidSecurityContextException,
			InvalidServiceContactException, TaskSubmissionException {
		super.execute();
		try {
			SessionChannelClient session = ssh.openSessionChannel();

			session = ssh.openSessionChannel();
			executeCommand(session);

			if (!session.isClosed()) {
				session.close();
			}

			ssh.disconnect();
		}
		catch (IOException sshe) {
			logger.error(sshe);
			throw new TaskSubmissionException("SSH Connection failed: " + sshe.getMessage(), sshe);
		}
	}

	public void executeCommand(SessionChannelClient session) throws TaskSubmissionException {
		try {
			if (getCmd() == null) {
				throw new TaskSubmissionException("No executable specified");
			}
			if (!session.startShell()) {
				throw new TaskSubmissionException("Failed to start shell");
			}
			logger.debug("Executing " + getCmd());
			if (getDir() != null) {
				session.getOutputStream().write(("cd " + getDir() + "\n").getBytes());
			}
			session.getOutputStream().write((getCmd() + "\n").getBytes());
			session.getOutputStream().write("exit\n".getBytes());
			BufferedReader stdout = new BufferedReader(new InputStreamReader(
					session.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(
					session.getStderrInputStream()));

			/*
			 * Read all output sent to stdout (line by line) and print it to our
			 * own stdout.
			 */
			char[] bufout = new char[1024];
			char[] buferr = new char[1024];

			if (output || error) {
				while (true) {
					int charsout = 0;
					int charserr = 0;
					if (output) {
						if (stdout.ready()) {
							charsout = stdout.read(bufout, 0, 1024);
						}
						else {
							charsout = 0;
						}
						if (charsout > 0) {
							this.taskOutput.append(new String(bufout, 0, charsout));
						}
					}
					if (error) {
						if (stderr.ready()) {
							charserr = stderr.read(buferr, 0, 1024);
						}
						else {
							charserr = 0;
						}
						if (charserr > 0) {
							this.taskError.append(new String(buferr, 0, charserr));
						}
					}

					if (session.getInputStream().isClosed()) {
						break;
					}
					if (charsout + charserr == 0) {
						Thread.sleep(20);
					}
				}
			}

			Integer exitcode = session.getExitCode();

			if (exitcode != null) {
				if (exitcode.intValue() != 0) {
					logger.info("Exit code " + exitcode.toString());
				}
			}
		}
		catch (IOException ioe) {
			throw new TaskSubmissionException("The session failed: " + ioe.getMessage(), ioe);
		}
		catch (InterruptedException e) {
			throw new TaskSubmissionException("The session was interrupted", e);
		}
		finally {
		}
	}

	public String getTaskOutput() {
		return this.taskOutput.toString();
	}

	public String getTaskError() {
		return this.taskError.toString();
	}
}