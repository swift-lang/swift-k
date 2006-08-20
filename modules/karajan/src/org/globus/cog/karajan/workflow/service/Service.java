//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 18, 2005
 */
package org.globus.cog.karajan.workflow.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;
import org.globus.cog.util.GridMap;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.auth.SelfAuthorization;
import org.globus.net.BaseServer;
import org.globus.net.ServerSocketFactory;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class Service extends BaseServer {
	private static final Logger logger = Logger.getLogger(Service.class);
	private ServiceContext context = new ServiceContext(this);
	private Thread serverThread;
	private boolean restricted;

	public Service(GSSCredential cred) throws IOException {
		this(cred, 0);
	}

	public Service(boolean secure, int port) throws IOException {
		super(secure, port);
		super.initialize();
	}

	public Service(GSSCredential cred, int port) throws IOException {
		super(cred, port);
		super.initialize();
	}

	public Service(GSSCredential cred, int port, InetAddress bindTo) throws IOException {
		super(cred, port);
		if (bindTo != null) {
			this._server = ServerSocketFactory.getDefault().createServerSocket(port, 50, bindTo);
		}
		super.initialize();
	}

	public Service(int port) throws IOException {
		this(true, port);
	}

	public Service(boolean secure, int port, InetAddress bindTo) throws IOException {
		super(secure, port);
		if (bindTo != null) {
			this._server.close();
			this._server = ServerSocketFactory.getDefault().createServerSocket(port, 50, bindTo);
		}
		super.initialize();
	}

	public void initialize() {
		// prevent the server from being started by BaseServer constructors
	}

	public static GSSCredential initializeCredentials(boolean personal, String hostcert,
			String hostkey) throws GlobusCredentialException, GSSException {
		if (!personal) {
			return new GlobusGSSCredentialImpl(new GlobusCredential(hostcert != null ? hostcert
					: "/etc/grid-security/hostcert.pem", hostkey != null ? hostkey
					: "/etc/grid-security/hostkey.pem"), GSSCredential.INITIATE_AND_ACCEPT);
		}
		else {
			return new GlobusGSSCredentialImpl(GlobusCredential.getDefaultCredential(),
					GSSCredential.INITIATE_AND_ACCEPT);
		}
	}

	protected void handleConnection(Socket socket) {
		logger.debug("Got connection");
		try {
			ConnectionHandler handler = new ConnectionHandler(this, socket);
			handler.start();
		}
		catch (Exception e) {
			logger.warn("Could not start connection handler", e);
		}
	}

	public String getContactString() {
		return "https://" + getHost() + ":" + getPort();
	}

	protected void start() {
		if (serverThread == null) {
			accept = true;
			serverThread = new Thread(this);
			serverThread.setName("Server: " + getContactString());
			serverThread.start();
		}
	}

	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	public ServiceContext getContext() {
		return context;
	}

	public static void main(String[] args) {
		ArgumentParser ap = new ArgumentParser();
		ap.setExecutableName("cog-workflow-service");
		ap.addFlag("personal", "Starts the service in personal mode. In personal mode, "
				+ "the service uses the user credential, and does not use a restricted "
				+ "execution environment. This is the default mode.");
		ap.addFlag("shared", "Starts the service in shared mode. In shared mode, "
				+ "the service uses the host credentials, provides a restricted execution "
				+ "environment, and uses the grid-map file for authorization.");
		ap.addFlag("nosec", "Starts the service without security");
		ap.addFlag("local",
				"Binds the socket to the local host. Connections from the network will not be possible.");
		ap.addOption("port",
				"Specifies the port that the service should be started on. The default is 1984.",
				"port-number", ArgumentParser.OPTIONAL);
		ap.addOption(
				"gridmap",
				"Specifies the location of the grid map file, which is used for "
						+ "mapping certificate distinguished names to local user accounts. This options is "
						+ "only meaningful if the service is started in shared mode. The default is "
						+ GridMap.DEFAULT_GRID_MAP, "file", ArgumentParser.OPTIONAL);
		ap.addOption("hostcert", "Indicates the location of the host certificate. This option "
				+ "is only used in shared mode. The default is /etc/grid-security/hostcert.pem",
				"file", ArgumentParser.OPTIONAL);
		ap.addOption("hostkey", "Indicates the location of the host key. This option "
				+ "is only used in shared mode. The default is /etc/grid-security/hostkey.pem",
				"file", ArgumentParser.OPTIONAL);
		ap.addFlag("help", "Displays usage information");
		ap.addAlias("port", "p");
		try {
			ap.parse(args);
		}
		catch (ArgumentParserException e) {
			ap.usage();
			System.exit(1);
		}
		if (ap.isPresent("help")) {
			ap.usage();
			System.exit(1);
		}

		int port = 1984;
		if (ap.isPresent("port")) {
			port = ap.getIntValue("port");
		}
		boolean personal = true;
		String hostcert = null;
		String hostkey = null;
		boolean bind = false;
		boolean nosec = false;
		if (ap.isPresent("shared")) {
			if (ap.isPresent("gridmap")) {
				System.setProperty("grid.mapfile", ap.getStringValue("gridmap"));
			}
			personal = false;
			if (ap.isPresent("personal")) {
				System.err.println("-shared and -personal are mutually exclusive");
				System.exit(1);
			}
			if (ap.isPresent("hostcert")) {
				hostcert = ap.getStringValue("hostcert");
			}
			if (ap.isPresent("hostkey")) {
				hostkey = ap.getStringValue("hostkey");
			}
			Task task = new TaskImpl();
			JobSpecification spec = new JobSpecificationImpl();
			task.setType(Task.JOB_SUBMISSION);
			spec.setExecutable("test");
			task.setSpecification(spec);
			try {
				TaskHandler handler = AbstractionFactory.newExecutionTaskHandler("local");
				handler.submit(task);
				throw new Exception();
			}
			catch (InvalidSecurityContextException e) {
				// This means that it's the gridmapped local provider
			}
			catch (Exception e) {
				System.err.println("The service will not run in shared mode with the default local provider.");
				System.err.println("Please install the grid-mapped local provider");
				System.exit(2);
			}
		}
		if (ap.isPresent("nosec")) {
			nosec = true;
		}
		if (ap.isPresent("local")) {
			bind = true;
		}

		try {
			Service service;
			InetAddress bindTo = null;
			if (bind) {
				bindTo = InetAddress.getLocalHost();
			}
			if (nosec) {
				service = new Service(false, port, bindTo);
			}
			else {
				service = new Service(initializeCredentials(personal, hostcert, hostkey), port,
						bindTo);
			}
			if (personal) {
				service.setAuthorization(new SelfAuthorization());
				service.setRestricted(false);
			}
			else {
				service.setAuthorization(new GridMapAuthorization());
				service.setRestricted(true);
			}
			if (bind) {
				service.getContext().setLocal(true);
			}
			service.start();
			System.out.println("Service started on port " + service.getPort());
			while (true) {
				Thread.sleep(10000);
			}
		}
		catch (Exception e) {
			System.err.println("Could not start service:\n\t" + e.getMessage());
			System.exit(1);
		}
	}
}
