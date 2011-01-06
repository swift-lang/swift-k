// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.beans;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.common.Version;
import org.globus.gram.Gram;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.globus.gram.WaitingForCommitException;
import org.globus.io.gass.server.JobOutputListener;
import org.globus.io.gass.server.JobOutputStream;
import org.globus.rsl.Binding;
import org.globus.rsl.Bindings;
import org.globus.rsl.NameOpValue;
import org.globus.rsl.RSLParser;
import org.globus.rsl.RslNode;
import org.globus.rsl.Value;
import org.globus.rsl.VarRef;
import org.globus.util.deactivator.Deactivator;
import org.ietf.jgss.GSSException;

/**
 * Bean which does the same functionality as
 * org.globus.tools.GlobusRun.  Instead of redirecting the output to
 * Standard input and output as in GlobusRun, it redirects to a Print
 * Stream. It also can store the output of the Job that is run in a
 * String and return it as Message.
 */
public class GridRun {

	private static Logger logger = Logger.getLogger(GridRun.class.getName());
	private PrintStream out = System.out;
	private PrintStream err = System.err;
	private boolean system = true;
	private String args[] = null;
	private String rsl = "&";
	private String rmc = null;
	private String executable;
	private String directory;
	private String arguments;
	private String environment;
	private boolean done = false;
	private String msg = "";

	private OutputListener stderrListener, stdoutListener;
	private org.globus.io.gass.server.GassServer gassServer;
	private static final String message = "\n"
		+ "Syntax: java GridRun [options] [RSL String]\n"
		+ "        java GridRun -version\n" + "        java GridRun -help\n\n"
		+ "\tOptions\n" + "\t-help | -usage\n" + "\t\tDisplay help.\n"
		+ "\t-v | -version\n" + "\t\tDisplay version.\n"
		+ "\t-f <rsl filename> | -file <rsl filename>\n"
		+ "\t\tRead RSL from the local file <rsl filename>. The RSL must be\n"
		+ "\t\tbe a single job request.\n" + "\t-q | -quiet\n"
		+ "\t\tQuiet mode (do not print diagnostic messages)\n"
		+ "\t-o | -output-enable\n"
		+ "\t\tUse the GASS Server library to redirect standout output\n"
		+ "\t\tand standard error to globusrun. Implies -quiet.\n" + "\t-s | -server\n"
		+ "\t\t$(GLOBUSRUN_GASS_URL) can be used to access files local\n"
		+ "\t\tto the submission machine via GASS. Implies -output-enable\n"
		+ "\t\tand -quiet.\n" + "\t-w | -write-allow\n"
		+ "\t\tEnable the GASS Server library and allow writing to\n"
		+ "\t\tGASS URLs. Implies -server and -quiet.\n"
		+ "\t-r <resource manager> | -resource-manager <resource manager>\n"
		+ "\t\tSubmit the RSL job request to the specified resource manager.\n"
		+ "\t\tA resource manager can be specified in the following ways: \n"
		+ "\t\t - host\n" + "\t\t - host:port\n" + "\t\t - host:port/service\n"
		+ "\t\t - host/service\n" + "\t\t - host:/service\n" + "\t\t - host::subject\n"
		+ "\t\t - host:port:subject\n" + "\t\t - host/service:subject\n"
		+ "\t\t - host:/service:subject\n" + "\t\t - host:port/service:subject\n"
		+ "\t\tFor those resource manager contacts which omit the port, \n"
		+ "\t\tservice or subject field the following defaults are used:\n"
		+ "\t\tport = 2119\n" + "\t\tservice = jobmanager \n"
		+ "\t\tsubject = subject based on hostname\n"
		+ "\t\tThis is a required argument when submitting a single RSL\n"
		+ "\t\trequest.\n" + "\t-k | -kill <job ID>\n"
		+ "\t\tKill a disconnected globusrun job.\n" + "\t-status <job ID>\n"
		+ "\t\tPrint the current status of the specified job.\n" + "\t-b | -batch\n"
		+ "\t\tCause globusrun to terminate after the job is successfully\n"
		+ "\t\tsubmitted, without waiting for its completion. Useful for batch\n"
		+ "\t\tjobs. This option cannot be used together with either -server\n"
		+ "\t\tor -interactive, and is also incompatible with multi-request jobs.\n"
		+ "\t\tThe \"handle\" or job ID of the submitted job will be written on\n"
		+ "\t\tstdout.\n" + "\t-stop-manager <job ID>\n"
		+ "\t\tCause globusrun to stop the job manager, without killing the\n"
		+ "\t\tjob. If the save_state RSL attribute is present, then a\n"
		+ "\t\tjob manager can be restarted by using the restart RSL attribute.\n"
		+ "\t-fulldelegation\n" + "\t\tPerform full delegation when submitting jobs.\n\n"
		+ "\tDiagnostic Options\n" + "\t-p | -parse\n"
		+ "\t\tParse and validate the RSL only. Does not submit the job to\n"
		+ "\t\ta GRAM gatekeeper. Multi-requests are not supported.\n"
		+ "\t-a | -authenticate-only\n"
		+ "\t\tSubmit a gatekeeper \"ping\" request only. Do not parse the\n"
		+ "\t\tRSL or submit the job request. Requires the -resource-manger\n"
		+ "\t\targument.\n" + "\t-d | -dryrun\n"
		+ "\t\tSubmit the RSL to the job manager as a \"dryrun\" test\n"
		+ "\t\tThe request will be parsed and authenticated. The job manager\n"
		+ "\t\twill execute all of the preliminary operations, and stop\n"
		+ "\t\tjust before the job request would be executed.\n\n"
		+ "\tNot Supported Options\n" + "\t-n | -no-interrupt\n";

	public static final int GLOBUSRUN_ARG_QUIET = 2;
	public static final int GLOBUSRUN_ARG_DRYRUN = 4;
	public static final int GLOBUSRUN_ARG_PARSE_ONLY = 8;
	public static final int GLOBUSRUN_ARG_AUTHENTICATE_ONLY = 16;
	public static final int GLOBUSRUN_ARG_USE_GASS = 32;
	public static final int GLOBUSRUN_ARG_ALLOW_READS = 64;
	public static final int GLOBUSRUN_ARG_ALLOW_WRITES = 128;
	public static final int GLOBUSRUN_ARG_BATCH = 512;
	public static final int GLOBUSRUN_ARG_ALLOW_OUTPUT = 4096;
	public static final int GLOBUSRUN_ARG_FULL_DELEGATION = 8192;

	private boolean quiet = false;


	private void status(String handle) {
		GramJob jb = new GramJob("");

		try {
			jb.setID(handle);
			Gram.jobStatus(jb);
			msg(jb.getStatusAsString());
		}
		catch (MalformedURLException e) {
			error("Error: Invalid job handle: " + e.getMessage());
			if (system)
				System.exit(1);
			else
				return;
		}
		catch (GSSException e) {
			error("Credentials error: " + e.getMessage());
			if (system)
				System.exit(1);
			else
				return;
		}
		catch (GramException e) {
			if (e.getErrorCode() == GramException.ERROR_CONTACTING_JOB_MANAGER) {
				msg("DONE");
				if (system)
					System.exit(0);
				else
					return;
			}
			else {
				error("Failed to get job status: " + e.getMessage());
				if (system)
					System.exit(1);
				else
					return;
			}
		}

		if (system)
			System.exit(0);
		else
			return;
	}

	private void stopManager(String handle) {
		GramJob jb = new GramJob("");

		try {
			jb.setID(handle);
			jb.signal(GramJob.SIGNAL_STOP_MANAGER);
		}
		catch (MalformedURLException e) {
			error("Error: Invalid job handle: " + e.getMessage());
			if (system)
				System.exit(1);
			else
				return;
		}
		catch (GSSException e) {
			error("Credentials error: " + e.getMessage());
			if (system)
				System.exit(1);
			else
				return;
		}
		catch (GramException e) {
			error("Error stopping job manager: " + e.getMessage());
			if (system)
				System.exit(1);
			else
				return;
		}

		if (system)
			System.exit(0);
		else
			return;
	}

	private void kill(String handle) {
		GramJob jb = new GramJob("");
		try {
			jb.setID(handle);
			Gram.cancel(jb);
		}
		catch (MalformedURLException e) {
			error("Invalid job handle");
			if (system)
				System.exit(1);
			else
				return;
		}
		catch (Exception e) {
			error("GRAM Job cancel failed: " + e.getMessage());
			if (system)
				System.exit(1);
			else
				return;
		}

		msg("GRAM Job cancel successful");
		if (system)
			System.exit(0);
		else
			return;
	}

	private int ping(String rmc) {
		try {
			Gram.ping(rmc);
		}
		catch (Exception e) {
			msg("GRAM Authentication test failed: " + e.getMessage());
			return 1;
		}

		msg("GRAM Authentication test successful");
		return 0;
	}

	private String readRSL(String file) {
		BufferedReader reader = null;
		String line = null;
		StringBuffer rsl = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			rsl = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				rsl.append(line.trim());
			}
			return rsl.toString();
		}
		catch (IOException e) {
			error("Failed to read rsl file : " + e.getMessage());
			//   System.exit(1);
			return null;
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException ee) {
				}
			}
		}
	}


	private void displaySyntax() {
		error("");
		error("Syntax : java GridRun [-help] [-f RSL file] [-s][-b][-d][...] [-r RM] [RSL]");
		error("");
		error("Use -help to display full usage.");
	}

	public GridRun() {
		this(null, System.out, System.err, true);
	}


	public GridRun(String args[], PrintStream out, PrintStream err, boolean system) {
		this.out = out;
		this.err = err;
		this.args = args;
		this.system = system;
	}

	public void executeJob() {

		boolean ping = false;
		boolean error = false;

		int options = 0;
		//enable the display;
		options |= GLOBUSRUN_ARG_USE_GASS | GLOBUSRUN_ARG_QUIET
			| GLOBUSRUN_ARG_ALLOW_OUTPUT;

		if (args != null) {
			if (args.length == 0) {
				if ((rmc == null) || (rsl == null)) {
					error(message);
					if (system)
						System.exit(0);
					else
						return;
				}

			}
			else {
				rsl = null;
			}

			for (int i = 0; i < args.length; i++) {

				if (args[i].charAt(0) != '-' && i + 1 == args.length) {
					// rsl spec
					if (rsl != null) {
						error = true;
						error("Error: RSL already specifed");
						break;
					}

					rsl = args[i];
					rsl = "" + rsl + "";
				}
				else if (args[i].equalsIgnoreCase("-status")) {

					// job status
					++i;
					if (i == args.length) {
						error = true;
						error("Error: -status requires a job handle");
						break;
					}
					else {
						status(args[i]);
					}

				}
				else if (args[i].equals("-k") || args[i].equalsIgnoreCase("-kill")) {

					// kill job
					++i;
					if (i == args.length) {
						error = true;
						error("Error: -kill requires a job handle");
						break;
					}
					else {
						kill(args[i]);
					}

				}
				else if (args[i].equals("-r")
					|| args[i].equalsIgnoreCase("-resource-manager")) {

					// resource manager contact
					++i;
					if (i == args.length) {
						error = true;
						error("Error: -r requires resource manager contact");
						break;
					}
					else {
						rmc = args[i];
					}

				}
				else if (args[i].equals("-b") || args[i].equalsIgnoreCase("-batch")) {

					// batch job
					options |= GLOBUSRUN_ARG_BATCH;

				}
				else if (args[i].equals("-d") || args[i].equalsIgnoreCase("-dryrun")) {

					// dryrun
					options |= GLOBUSRUN_ARG_DRYRUN;

				}
				else if (args[i].equalsIgnoreCase("-fulldelegation")) {

					// perform full delegation
					options |= GLOBUSRUN_ARG_FULL_DELEGATION;


				}
				else if (args[i].equalsIgnoreCase("-stop-manager")) {

					++i;
					if (i == args.length) {
						error = true;
						error("Error: -stop-manager requires job ID");
						break;
					}
					else {
						stopManager(args[i]);
					}

				}
				else if (args[i].equals("-a")
					|| args[i].equalsIgnoreCase("-authenticate-only")) {

					// ping request
					options |= GLOBUSRUN_ARG_AUTHENTICATE_ONLY;

				}
				else if (args[i].equals("-o")
					|| args[i].equalsIgnoreCase("-output-enable")) {

					// redirect output
					options |= GLOBUSRUN_ARG_USE_GASS | GLOBUSRUN_ARG_QUIET
						| GLOBUSRUN_ARG_ALLOW_OUTPUT;

				}
				else if (args[i].equals("-w") || args[i].equalsIgnoreCase("-write-allow")) {

					options |= GLOBUSRUN_ARG_ALLOW_WRITES | GLOBUSRUN_ARG_ALLOW_READS
						| GLOBUSRUN_ARG_USE_GASS | GLOBUSRUN_ARG_QUIET;

				}
				else if (args[i].equals("-f") || args[i].equalsIgnoreCase("-file")) {

					// read from file
					i++;
					if (i == args.length) {
						error = true;
						error("Error: -file requires a filename");
						break;
					}
					else {
						rsl = readRSL(args[i]);
					}

				}
				else if (args[i].equals("-s") || args[i].equalsIgnoreCase("-server")) {

					// enable gass url
					options |= GLOBUSRUN_ARG_ALLOW_READS | GLOBUSRUN_ARG_USE_GASS
						| GLOBUSRUN_ARG_QUIET;

				}
				else if (args[i].equals("-q") || args[i].equalsIgnoreCase("-quiet")) {

					// quiet mode
					options |= GLOBUSRUN_ARG_QUIET;

				}
				else if (args[i].equals("-p") || args[i].equalsIgnoreCase("-parse")) {

					// parse only
					options |= GLOBUSRUN_ARG_PARSE_ONLY;

				}
				else if (args[i].equals("-v") || args[i].equalsIgnoreCase("-version")) {

					// display version info
					error(Version.getVersion());
					if (system)
						System.exit(1);
					else
						return;

				}
				else if (args[i].equalsIgnoreCase("-help")
					|| args[i].equalsIgnoreCase("-usage")) {

					error(message);
					if (system)
						System.exit(1);
					else
						return;

				}
				else {
					msg("Error: argument #" + i + " (" + args[i] + ") : unknown");
					error = true;
				}
			}
		}
		if ((options & GLOBUSRUN_ARG_BATCH) != 0
			&& (options & GLOBUSRUN_ARG_USE_GASS) != 0) {
			error = true;
			error("Error: option -s and -b are exclusive");
		}

		if (error) {
			displaySyntax();
			if (system)
				System.exit(-1);
			else
				return;
		}


		if ((options & GLOBUSRUN_ARG_AUTHENTICATE_ONLY) != 0) {

			if (rmc == null) {
				error("Error: No resource manager contact specified "
					+ "for authentication test.");
				displaySyntax();
				if (system)
					System.exit(-1);
				else
					return;
			}

			if (ping(rmc) == 1) {
				if (system)
					System.exit(1);
				else
					return;
			}
		}

		if (rsl == null) {
			error("Error: Must specify a RSL string.");
			displaySyntax();
			if (system)
				System.exit(-1);
			else
				return;
		}

		RslNode rslTree = null;
		try {
			rslTree = RSLParser.parse(rsl);//"&(executable=/usr/bin/env)");
		}
		catch (Throwable e) {
			error("Error: Cannot parse RSL: " + e.getMessage());
			if (system)
				System.exit(-1);
			else
				return;
		}

		// check if the rsl is boolean? if not, System.exit with Bad RSL!

		if ((options & GLOBUSRUN_ARG_PARSE_ONLY) != 0) {
			msg("RSL Parsed Successfully...\n");
			if (system)
				System.exit(0);
			else
				return;
		}
		if (rslTree.getOperator() != RslNode.MULTI && rmc == null) {
			error("Error: No resource manager contact");
			displaySyntax();
			if (system)
				System.exit(-1);
			else
				return;
		}

		if ((options & GLOBUSRUN_ARG_USE_GASS) != 0) {

			String gassUrl = null;

			int server_options = org.globus.io.gass.server.GassServer.STDOUT_ENABLE
				| org.globus.io.gass.server.GassServer.STDERR_ENABLE;

			if ((options & GLOBUSRUN_ARG_ALLOW_READS) != 0) {
				server_options |= org.globus.io.gass.server.GassServer.READ_ENABLE;
			}

			if ((options & GLOBUSRUN_ARG_ALLOW_WRITES) != 0) {
				server_options |= org.globus.io.gass.server.GassServer.WRITE_ENABLE;
			}

			try {
				gassServer = new org.globus.io.gass.server.GassServer();
				gassServer.setOptions(server_options);

				gassUrl = gassServer.getURL();


				stderrListener = new OutputListener(this);
				stdoutListener = new OutputListener(this);

				gassServer.registerJobOutputStream("err", new JobOutputStream(
					stderrListener));
				gassServer.registerJobOutputStream("out", new JobOutputStream(
					stdoutListener));
				gassServer.registerDefaultDeactivator();

				logger.debug(gassServer);

			}
			catch (IOException e) {
				error("Gass server initialization failed: " + e.getMessage());
				if (system)
					System.exit(1);
				else
					return;
			}/* catch (GlobusProxyException e) {
			 error("Credentials error: " + e.getMessage());
			 if (system) System.exit(1);  else return;
			 }*/

			// this will update the tree appriopriately
			rslOutputSubst(rslTree, gassUrl, (options & GLOBUSRUN_ARG_ALLOW_OUTPUT) != 0);

		}

		if ((options & GLOBUSRUN_ARG_DRYRUN) != 0) {
			rslDryrunSubst(rslTree);
		}

		quiet = ((options & GLOBUSRUN_ARG_QUIET) != 0);

		String finalRsl = rslTree.toRSL(true);
		logger.debug("RSL: " + finalRsl);

		if (rslTree.getOperator() == RslNode.MULTI) {
			multiRun(rslTree, options);
		}
		else {
			gramRun(finalRsl, rmc, options);
		}
		return;
	}

	private void println(String str) {
		//if (!quiet) {
		msg(str);
		//}
	}

	private void gramRun(String rsl, String rmc, int options) {
		JobListener jobListener = null;
		GramJob job = new GramJob(rsl);


		if ((options & GLOBUSRUN_ARG_BATCH) != 0) {
			jobListener = new BatchJobListener();
		}
		else {
			jobListener = new InteractiveJobListener(quiet);
		}
		job.addListener(jobListener);


		Exception exception = null;
		boolean sendCommit = false;

		try {
			job.request(rmc, false, !((options & GLOBUSRUN_ARG_FULL_DELEGATION) != 0));
		}
		catch (WaitingForCommitException e) {
			try {
				job.signal(GramJob.SIGNAL_COMMIT_REQUEST);
				sendCommit = true;
			}
			catch (Exception ee) {
				exception = ee;
			}
		}
		catch (Exception e) {
			exception = e;
		}

		if (exception instanceof GramException) {

			int err = ((GramException) exception).getErrorCode();
			displayError(err, options);
			if (system)
				System.exit(err);
			else
				return;
		}
		else if (exception instanceof GSSException) {

			error("Security error: " + exception.getMessage());
			if (system)
				System.exit(1);
			else
				return;

		}
		else if (exception != null) {
			error("GRAM Job submisson failed: " + exception.getMessage());
			if (system)
				System.exit(1);
			else
				return;
		}

		// submission must be successful at this point.


		if ((options & GLOBUSRUN_ARG_BATCH) != 0) {
			msg(job.getIDAsString());
		}
		if (jobListener instanceof BatchJobListener) {
			logger.debug("Waiting for the job to start up...");
		}
		else {
			logger.debug("Waiting for the job to complete...");
		}

		try {
			jobListener.waitFor();
		}
		catch (InterruptedException e) {
		}

		job.removeListener(jobListener);

		int err = jobListener.getError();

		if (jobListener.isFinished()) {
			if (sendCommit) {
				logger.debug("Sending COMMIT_END signal");
				try {
					job.signal(GramJob.SIGNAL_COMMIT_END);
				}
				catch (GramException e) {
					logger.debug("Signal failed", e);
					err = e.getErrorCode();
					error("Signal failed: " + GramException.getMessage(err));
					if (system)
						System.exit(err);
					else
						return;

				}
				catch (GSSException e) {
					logger.debug("Signal failed", e);
					error("Security error: " + e.getMessage());
					if (system)
						System.exit(1);
					else
						return;


				}
			}
		}
		else if ((options & GLOBUSRUN_ARG_BATCH) != 0) {
			try {
				job.unbind();
			}
			catch (GramException e) {
				logger.debug("Unbind failed", e);
				err = e.getErrorCode();
				error("Unbind failed: " + GramException.getMessage(err));
				if (system)
					System.exit(err);
				else
					return;

			}
			catch (GSSException e) {
				logger.debug("Unbind failed", e);
				error("Security error: " + e.getMessage());
				if (system)
					System.exit(1);
				else
					return;

			}
		}

		displayError(err, options);
		logger.debug("Exiting...");
		if (system) {
			Deactivator.deactivateAll();
			System.exit(err);
		}
		else
			return;
		/*	}else {
		 logger.debug("Exiting...");
		 //	    Deactivator.deactivateAll();
		 msg("Job listener is null");
		 }

		 if (stdoutListener != null||stderrListener !=null) {
		 logger.debug("Waiting for a job to complete...");

		 synchronized(stdoutListener) {
		 try {
		 stdoutListener.wait();
		 stderrListener.wait();
		 } catch(InterruptedException e) {
		 }
		 }
		 gassServer.unregisterJobOutputStream("err");
		 gassServer.unregisterJobOutputStream("out");
		 if (done) {
		 logger.debug("Sending COMMIT_END signal");
		 try {
		 job.signal(GramJob.SIGNAL_COMMIT_END);
		 } catch(Exception ee) {
		 }
		 }

		 logger.debug("Exiting...");
		 //	    Deactivator.deactivateAll();
		 if (system)
		 System.exit(0);
		 else return;
		 } else {
		 logger.debug("Exiting...");
		 //	    Deactivator.deactivateAll();
		 }*/

	}

	private void displayError(int errorCode, int options) {
		if (errorCode == GramException.DRYRUN && (options & GLOBUSRUN_ARG_DRYRUN) != 0) {
			println("Dryrun successful");
		}
		else if (errorCode != 0) {
			error("GRAM Job submission failed: " + GramException.getMessage(errorCode)
				+ " (error code " + errorCode + ")");
		}
	}

	private void multiRun(RslNode rslTree, int options) {
		if ((options & GLOBUSRUN_ARG_BATCH) != 0) {
			error("Error: Batch mode (-b) not supported for multi-request");
			if (system)
				System.exit(1);
			else
				return;
		}

		MultiJobListener listener = new MultiJobListener(quiet, this);

		List jobs = rslTree.getSpecifications();
		Iterator iter = jobs.iterator();
		RslNode node;
		NameOpValue nv;
		String rmc;
		String rsl;
		while (iter.hasNext()) {
			node = (RslNode) iter.next();
			rsl = node.toRSL(true);
			nv = node.getParam("resourceManagerContact");
			if (nv == null) {
				error("Error: No resource manager contact for job.");
				continue;
			}
			else {
				Object obj = nv.getFirstValue();
				if (obj instanceof Value) {
					rmc = ((Value) obj).getValue();
					multiRunSub(rsl, rmc, options, listener);
				}
			}
		}

		logger.debug("Waiting for jobs to complete...");
		synchronized (listener) {
			try {
				listener.wait();
			}
			catch (InterruptedException e) {
			}
		}

		logger.debug("Exiting...");
		//	Deactivator.deactivateAll();
	}

	private void multiRunSub(String rsl, String rmc, int options,
		MultiJobListener listener) {
		GramJob job = new GramJob(rsl);

		job.addListener(listener);

		Exception exception = null;

		try {
			job.request(rmc, false, !((options & GLOBUSRUN_ARG_FULL_DELEGATION) != 0));
		}
		catch (WaitingForCommitException e) {
			try {
				job.signal(GramJob.SIGNAL_COMMIT_REQUEST);
			}
			catch (Exception ee) {
				exception = ee;
			}
		}
		catch (Exception e) {
			exception = e;
		}

		if (exception instanceof GramException) {
			if (((GramException) exception).getErrorCode() == GramException.DRYRUN
				&& (options & GLOBUSRUN_ARG_DRYRUN) != 0) {
				println("Dryrun successful");
			}
			else {
				error("GRAM Job submission failed: " + exception.getMessage()
					+ " (error code " + ((GramException) exception).getErrorCode()
					+ ", rsl: " + rsl + ")");
			}
			return;
		}
		else if (exception != null) {
			error("GRAM Job submisson failed: " + exception.getMessage());
			return;
		}

		listener.runningJob();
		println("GRAM Job submission successful (rmc: " + rmc + ")");
	}

	private void rslDryrunSubst(RslNode rslTree) {
		if (rslTree.getOperator() == RslNode.MULTI) {
			List specs = rslTree.getSpecifications();
			Iterator iter = specs.iterator();
			RslNode node;
			while (iter.hasNext()) {
				node = (RslNode) iter.next();
				rslDryrunSubst(node);
			}
		}
		else {
			rslTree.put(new NameOpValue("dryrun", NameOpValue.EQ, "yes"));
		}
	}

	private void rslOutputSubst(RslNode rslTree, String gassUrl, boolean allowOutput) {
		if (rslTree.getOperator() == RslNode.MULTI) {
			List specs = rslTree.getSpecifications();
			Iterator iter = specs.iterator();
			RslNode node;
			while (iter.hasNext()) {
				node = (RslNode) iter.next();
				rslOutputSubst(node, gassUrl, allowOutput);
			}
		}
		else {
			Binding bd = new Binding("GLOBUSRUN_GASS_URL", gassUrl);
			Bindings bind = rslTree.getBindings("rsl_substitution");
			if (bind == null) {
				bind = new Bindings("rsl_substitution");
				rslTree.put(bind);
			}
			bind.add(bd);

			if (allowOutput) {
				Value value = null;
				value = new VarRef("GLOBUSRUN_GASS_URL", null, new Value("/dev/stdout"));

				rslTree.put(new NameOpValue("stdout", NameOpValue.EQ, value));

				value = new VarRef("GLOBUSRUN_GASS_URL", null, new Value("/dev/stderr"));

				rslTree.put(new NameOpValue("stderr", NameOpValue.EQ, value));
			}
		}
	}

	public void msg(String s) {
		out.println(s);
		msg += s + "\n";
	}

	public void msg(int n) {
		msg(n + "");
	}

	public void msg(Object o) {
		msg(o.toString() + "");
	}

	public void error(int n) {
		error(n + "");
	}

	public void error(Object o) {
		error(o.toString() + "");
	}

	public void error(String s) {
		err.println("GRAM ERROR MESSAGE: \n" + s);
		msg += "GRAM ERROR MESSAGE: \n" + s + "\n";
	}

	public PrintStream getOutputStream() {
		return out;
	}

	public void setOutputStream(PrintStream out) {
		this.out = out;
	}

	public PrintStream getErrorStream() {
		return err;
	}

	public void setErrorStream(PrintStream out) {
		this.err = out;
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String args[]) {
		this.args = args;
	}

	public boolean getSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

	public void setMessage(String msg) {
		this.msg = msg;
	}

	public String getMessage() {
		return msg;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
		rsl += RSLstring("executable", this.executable);
	}

	public String getExecutable() {
		return executable;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
		rsl += RSLstring("directory", this.directory);
	}

	public String getDirectory() {
		return directory;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
		rsl += RSLstring("arguments", this.arguments);
	}

	public String getArguments() {
		return arguments;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
		rsl += RSLstring(" environment", this.environment);
	}

	public String getEnvironment() {
		return environment;
	}

	public void setRemoteContact(String rmc) {
		this.rmc = rmc;
	}

	public String getRemoteContact() {
		return rmc;
	}

	public void setRsl(String rsl) {
		this.rsl = rsl;
	}

	public String getRsl() {
		return rsl;
	}

	private String RSLstring(String name, String contents) {
		if (name == null) {
			return new String("");
		}
		else {
			return new String("(" + name + "=" + contents + ")");
		}
	}
}

abstract class JobListener implements GramJobListener {

	protected int status = 0;
	protected int error = 0;

	public abstract void waitFor() throws InterruptedException;

	public int getError() {
		return error;
	}

	public int getStatus() {
		return status;
	}

	public boolean isFinished() {
		return (status == GramJob.STATUS_DONE || status == GramJob.STATUS_FAILED);
	}
}

class BatchJobListener extends JobListener {

	private boolean called = false;

	// waits for first notification
	public synchronized void waitFor() throws InterruptedException {
		while (!called) {
			wait();
		}
	}

	public synchronized void statusChanged(GramJob job) {
		if (!called) {
			called = true;
			status = job.getStatus();
			error = job.getError();
			notify();
		}
	}
}

class InteractiveJobListener extends JobListener {

	private boolean quiet;
	private boolean finished = false;

	public InteractiveJobListener(boolean quiet) {
		this.quiet = quiet;
	}

	// waits for DONE or FAILED status
	public synchronized void waitFor() throws InterruptedException {
		while (!finished) {
			wait();
		}
	}

	public synchronized void statusChanged(GramJob job) {
		if (!quiet) {
			//	    System.out.println("Job: "+ job.getStatusAsString());
		}
		status = job.getStatus();
		if (status == GramJob.STATUS_DONE) {
			finished = true;
			error = 0;
			notify();
		}
		else if (job.getStatus() == GramJob.STATUS_FAILED) {
			finished = true;
			error = job.getError();
			notify();
		}
	}
}

class MultiJobListener implements GramJobListener {

	private boolean quiet;
	private int runningJobs = 0;
	private GridRun grun = null;

	public MultiJobListener(boolean quiet, GridRun grun) {
		this.quiet = quiet;
		this.grun = grun;
	}

	public void runningJob() {
		runningJobs++;
	}

	public synchronized void done() {
		runningJobs--;
		if (runningJobs <= 0) {
			synchronized (this) {
				notifyAll();
			}
		}
	}

	public void statusChanged(GramJob job) {

		if (!quiet) {
			grun.msg("Job status:");
			grun.msg("Job ID : " + job.getIDAsString());
			grun.msg("Status : " + job.getStatusAsString());
		}

		if (job.getStatus() == GramJob.STATUS_DONE) {
			done();
		}
		else if (job.getStatus() == GramJob.STATUS_FAILED) {
			grun.msg(job.getError());
			done();
		}
	}

}

class OutputListener implements JobOutputListener {

	GridRun grun = null;

	public OutputListener(GridRun grun) {
		this.grun = grun;
	}
	private StringBuffer outputBuf = null;

	public void outputChanged(String output) {
		grun.msg("\n \n");
		grun.msg(output);
		return;
	}

	public void outputClosed() {
	}

	public String getOutput() {
		if (outputBuf == null)
			return null;
		else
			return outputBuf.toString();
	}

	public boolean hasData() {
		return (outputBuf != null);
	}

}

