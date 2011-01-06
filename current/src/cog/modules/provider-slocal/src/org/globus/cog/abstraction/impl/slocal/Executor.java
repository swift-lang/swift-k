//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.slocal;

import java.io.File;
import java.io.IOException;

import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.util.GridMap;
import org.globus.gsi.gssapi.auth.AuthorizationException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;

public class Executor {
	private JobSpecification spec;
	private Task task;
	private Object cred;
	private static ProcessPoller poller = new ProcessPoller();
	private ProcessListener listener;
	private BinaryOutputListener out, err;

	public Executor(Task task, Object cred, ProcessListener listener) {
		this.task = task;
		this.spec = (JobSpecification) task.getSpecification();
		this.cred = cred;
		this.listener = listener;
	}

	public Object getCred() {
		return cred;
	}

	public void setCred(Object cred) {
		this.cred = cred;
	}

	private ProcessPoller getProcessPoller() {
		synchronized (poller) {
			if (!poller.isAlive()) {
				poller.start();
			}
		}
		return poller;
	}

	public void start() throws AuthorizationException, GSSException, IOException, ProcessException {
		if (System.getProperty("COG_INSTALL_PATH") == null) {
			throw new ProcessException("COG_INSTALL_PATH is not set");
		}
		File wrapper = new File(Properties.getProperties().getWrapper());
		if (!wrapper.exists()) {
			throw new ProcessException("Wrapper is inaccessible ("
					+ Properties.getProperties().getWrapper() + ")");
		}
		String name = null;
		if (cred instanceof GSSName) {
			name = ((GSSName) cred).toString();
		}
		else if (cred instanceof GSSCredential) {
			name = ((GSSCredential) cred).getName().toString();
		}
		else {
			throw new AuthorizationException("Invalid credential: " + cred);
		}
		String currentUser = System.getProperty("user.name");
		String userName = GridMap.getGridMap(Properties.getProperties().getGridMap()).getUserID(
				name);
		if (userName == null) {
			throw new AuthorizationException("No local mapping for " + name);
		}
		boolean sudo = true;
		if (currentUser != null && currentUser.equals(userName)) {
			sudo = false;
		}
		File dir;

		StringBuffer sb = new StringBuffer();
		if (sudo) {
			sb.append(Properties.getProperties().getSudo());
			sb.append(' ');
		}
		if (spec.getDirectory() == null) {
			if (sudo) {
				sb.append("-H -S ");
			}
			dir = new File(".");
		}
		else {
			dir = new File(spec.getDirectory());
		}
		if (sudo) {
			sb.append("-u ");
			sb.append(userName);
			sb.append(" -- ");
		}
		else {
			sb.append(Properties.getProperties().getNoSudo());
			sb.append(' ');
		}

		sb.append(Properties.getProperties().getWrapper());
		sb.append(' ');
		if (spec.getStdInput() == null) {
			sb.append("- ");
		}
		else {
			sb.append(spec.getStdInput());
		}

		if (spec.getStdOutput() == null) {
			sb.append("- ");
		}
		else {
			sb.append(spec.getStdOutput());
		}

		if (spec.getStdError() == null) {
			sb.append("- ");
		}
		else {
			sb.append(spec.getStdError());
		}

		sb.append(spec.getExecutable());
		String args = spec.getArgumentsAsString();
		if (args != null) {
			sb.append(' ');
			sb.append(args);
		}

		Process process = Runtime.getRuntime().exec(sb.toString(), null, dir);

		try {
			process.getOutputStream().close();
		}
		catch (IOException e) {
		}

		StreamProcessor stdout = null, stderr = null;
		// process output
		if (spec.getStdOutput() == null && spec.isRedirected()) {
			BinaryOutputListener bol = (BinaryOutputListener) task.getAttribute("binaryOutputListener");
			if (bol != null) {
				stdout = new BinaryStreamProcessor(process.getInputStream(), bol);
			}
			else {
				stdout = new BinaryStreamProcessor(process.getInputStream(),
						out = new OutputListener());
			}
		}

		// process error
		if (spec.getStdError() == null && spec.isRedirected()) {
			BinaryOutputListener bol = (BinaryOutputListener) task.getAttribute("binaryErrorListener");
			if (bol != null) {
				stderr = new SudoErrorStreamProcessor(process.getErrorStream(), bol, listener);
			}
			else {
				stderr = new SudoErrorStreamProcessor(process.getErrorStream(),
						err = new OutputListener(), listener);
			}
		}

		getProcessPoller().addProcessor(new ProcessProcessor(process, stdout, stderr, listener));
	}

	private class OutputListener implements BinaryOutputListener {
		StringBuffer sb = new StringBuffer();

		public void dataReceived(byte[] data, int offset, int length) {
			sb.append(new String(data, offset, length));
			if (this == out) {
				task.setStdOutput(sb.toString());
			}
			else {
				task.setStdError(sb.toString());
			}
		}
	}

	private void error(String message) {
		listener.processFailed(message);
	}
}
