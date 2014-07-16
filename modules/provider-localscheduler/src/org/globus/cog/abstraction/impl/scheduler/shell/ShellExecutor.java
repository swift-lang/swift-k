//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessException;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.StagingSet;
import org.globus.cog.abstraction.interfaces.StagingSetEntry;
import org.globus.cog.abstraction.interfaces.StagingSetEntry.Mode;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.gsi.gssapi.auth.AuthorizationException;

public class ShellExecutor extends AbstractExecutor {
	public static final Logger logger = Logger.getLogger(ShellExecutor.class);
	
	private Properties props;
	private static volatile int idCounter;
	private String name, jobid;
	private boolean active;
	
	public ShellExecutor(Properties props, Task task, ProcessListener listener) {
		super(task, listener);
		this.props = props;
		name = "cog-" + IDF.format(idCounter++);
	}

    private static NumberFormat IDF = new DecimalFormat("000000");
			
	protected void writeSpec(String name, String value, Writer wr) 
	        throws IOException {
        if (value != null) {
            wr.write(name);
            wr.write('=');
            wr.write(String.valueOf(value));
            wr.write('\n');
        }
    }
	
	private static class ProcessStreams {
	    private Process process;
	    public Writer stdin;
	    private Reader stdout, stderr;
	    private StringBuilder out, err;
	    
	    public ProcessStreams(Process process) {
	        this.process = process;
	        this.stdin = new OutputStreamWriter(process.getOutputStream());
	        this.stdout = new InputStreamReader(process.getInputStream());
	        this.stderr = new InputStreamReader(process.getErrorStream());
	        out = new StringBuilder();
	        err = new StringBuilder();
	    }	    
	}

	@Override
    public void start() throws AuthorizationException, IOException, ProcessException {
        Process p = Runtime.getRuntime().exec(props.getSubmitCommand());
        ProcessStreams ps = new ProcessStreams(p);
        
        JobSpecification spec = this.getSpec();
                
        // Basic stuff
        if (!spec.getExecutableLocation().equals(FileLocation.REMOTE)) {
            throw new IllegalArgumentException("Only remote executables supported");
        }
        writeSpec("directory", escape(spec.getDirectory()), ps.stdin);
        writeSpec("executable", escape(spec.getExecutable()), ps.stdin);
        if (spec.getArgumentsAsList() != null) {
            for (String arg : spec.getArgumentsAsList()) {
                writeSpec("arg", escape(arg), ps.stdin);
            }
        }
        
        // Standard streams
        if (spec.getStdInputLocation().overlaps(FileLocation.MEMORY)) {
            throw new IllegalArgumentException("In-memory STDIN not supported");
        }
        
        if (spec.getStdInput() != null) {
            writeSpec("stdin.location", spec.getStdInputLocation().toString(), ps.stdin);
            writeSpec("stdin.path", spec.getStdInput(), ps.stdin);
        }
        
        writeStandardFileSpec(spec.getStdOutput(), spec.getStdOutputLocation(), "stdout", ps);
        writeStandardFileSpec(spec.getStdError(), spec.getStdErrorLocation(), "stderr", ps);
        
        // Environment variables
        for (String en : spec.getEnvironmentVariableNames()) {
            writeSpec("env." + en, escape(spec.getEnvironmentVariable(en)), ps.stdin);
        }
        
        // Attributes
        for (String attr : spec.getAttributeNames()) {
            Object o = spec.getAttribute(attr);
            if (o != null) {
                writeSpec("attr." + attr, escape(o.toString()), ps.stdin);
            }
        }
        
        // Staging stuff
        writeStagingSpec(spec.getStageIn(), "stagein", false, ps);
        writeStagingSpec(spec.getStageOut(), "stageout", true, ps);
        
        // Cleanup
        if (spec.getCleanUpSet() != null) {
            for (String ce : spec.getCleanUpSet()) {
                writeSpec("cleanup", escape(ce), ps.stdin);
            }
        }
        
        ps.stdin.close();
        
        try {
            int ec = p.waitFor();
            if (logger.isDebugEnabled()) {
                logger.debug(props.getSubmitCommandName() + " done (exit code " + ec + ")");
            }
            if (ec != 0) {
                throw new ProcessException(props.getSubmitCommand() + " failed: " + read(ps.stderr));
            }
        }
        catch (InterruptedException e) {
            throw new ProcessException(e);
        }
        
        parseOutput(ps.stdout);
        active = true;
        if (logger.isDebugEnabled()) {
            logger.debug("Submitted job with id '" + jobid + "'");
        }
        getQueuePoller().addJob(
            job = createJob(jobid, stdout, spec.getStdOutputLocation(), stderr,
                spec.getStdErrorLocation(), exitcode, this));
    }
	
	
	private void parseOutput(Reader r) throws IOException, ProcessException {
        BufferedReader br = new BufferedReader(r);
        String line;
        while ((line = br.readLine()) != null) {
            String[] els = line.split("=", 2);
            if (els.length < 2) {
                throw new ProcessException("Invalid output from '" + 
                        props.getSubmitCommand() + "': " + line);
            }
            if ("jobid".equals(els[0])) {
                jobid = els[1];
                if ("".equals(jobid)) {
                    throw new ProcessException("Received empty job id from '" + 
                            props.getSubmitCommand() + "'");
                }
            }
            else if ("stdout.path".equals(els[0])) {
                stdout = els[1];
            }
            else if ("stderr.path".equals(els[0])) {
                stderr = els[1];
            }
            else {
                throw new ProcessException("Invalid output from '" +
                        props.getSubmitCommand() + "': " + line);
            }
        }
        if (jobid == null) {
            throw new ProcessException("No job id received from '" + props.getSubmitCommand() + "'");
        }
    }
	
	@Override
    public void processCompleted(int exitCode) {
	    active = false;
        super.processCompleted(exitCode);
    }


    @Override
    public void processFailed(String message) {
        active = false;
        super.processFailed(message);
    }


    @Override
    public void processFailed(Exception e) {
        active = false;
        super.processFailed(e);
    }


    public void cancel() throws TaskSubmissionException {
        if (!active) {
            throw new TaskSubmissionException("Can only cancel an active task");
        }
        String[] cmdline = new String[] { getProperties().getRemoveCommand(), jobid };
        try {
            logger.debug("Canceling job: jobid=" + jobid);
            Process process = Runtime.getRuntime().exec(cmdline, null, null);
            int ec = process.waitFor();
            if (ec != 0) {
                throw new TaskSubmissionException("Failed to cancel task: " + 
                        read(new InputStreamReader(process.getInputStream())));
            }
        }
        catch (InterruptedException e) {
            throw new TaskSubmissionException(
                "Thread interrupted while waiting for "
                        + getProperties().getRemoveCommandName() + " to finish");
        }
        catch (IOException e) {
            throw new TaskSubmissionException("Failed to cancel task", e);
        }
    }

    private String escape(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\n' || c == '\\') {
                sb.append("\\");
                if (c == '\n') {
                    sb.append('n');
                }
                else {
                    sb.append('\\');
                }
            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String read(Reader r) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(r);
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append('\n');
            line = br.readLine();
        }
        return sb.toString();
    }

    private void writeStagingSpec(StagingSet ss, String name, boolean modes, ProcessStreams pc) throws IOException {
        if (ss == null || ss.isEmpty()) {
            return;
        }
        for (StagingSetEntry se : ss) {
            writeSpec(name + ".source", escape(se.getSource()), pc.stdin);
            writeSpec(name + ".destination", escape(se.getDestination()), pc.stdin);
            if (modes) {
                writeSpec(name + ".mode", stagingModeToStr(se.getMode()), pc.stdin);
            }
        }
    }

    private String stagingModeToStr(EnumSet<Mode> mode) {
        int s = 0;
        for (Mode m : mode) {
            s += m.getId();
        }
        return String.valueOf(s);
    }

    private void writeStandardFileSpec(String path, FileLocation loc, String name, ProcessStreams pc) 
            throws IOException {
        if (path == null) {
            if (loc.overlaps(FileLocation.MEMORY)) {
                writeSpec(name + ".location", "tmp", pc.stdin);
            }
        }
        else {
            writeSpec("stdout.location", loc.remove(FileLocation.MEMORY).toString(), pc.stdin);
            writeSpec("stdout.path", path, pc.stdin);
        }
    }

    @Override
    protected void writeScript(Writer wr, String exitcode, String stdout, String stderr)
            throws IOException {
        // not used
    }

    @Override
	protected String getName() {
		return "Shell";
	}

	@Override
	protected AbstractProperties getProperties() {
		return props;
	}

	@Override
	protected Job createJob(String jobid, String stdout,
			FileLocation stdOutputLocation, String stderr,
			FileLocation stdErrorLocation, String exitcode,
			AbstractExecutor executor) {
		return new Job(jobid, stdout, stdOutputLocation, stderr,
				stdErrorLocation, exitcode, executor);
	}

	private static QueuePoller poller;

	@Override
	protected AbstractQueuePoller getQueuePoller() {
		synchronized(ShellExecutor.class) {
			if (poller == null) {
				poller = new QueuePoller(props);
				poller.start();
			}
			return poller;
		}
	}
}
