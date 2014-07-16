//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 10, 2009
 */
package org.globus.cog.abstraction.impl.scheduler.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.gsi.gssapi.auth.AuthorizationException;

/**
 *   Set log level to DEBUG to not delete generated submit script
 * */
public abstract class AbstractExecutor implements ProcessListener {
    public static final Logger logger = Logger
        .getLogger(AbstractExecutor.class);

    protected static final boolean[] TRIGGERS;

    static {
        TRIGGERS = new boolean[128];
        TRIGGERS[' '] = true;
        TRIGGERS['\n'] = true;
        TRIGGERS['\t'] = true;
        TRIGGERS['|'] = true;
        TRIGGERS['\\'] = true;
        TRIGGERS['>'] = true;
        TRIGGERS['<'] = true;
    }

    protected File script;
    protected String stdout, stderr, exitcode, jobid;
    protected final JobSpecification spec;
    protected final Task task;
    protected final ProcessListener listener;
    protected Job job;

    protected AbstractExecutor(Task task, ProcessListener listener) {
        this.task = task;
        this.spec = (JobSpecification) task.getSpecification();
        this.listener = listener;
        validate(task);
    }

    public void start() throws AuthorizationException, IOException, ProcessException {

    	String scriptdirPath = System.getProperty("script.dir");
    	if (scriptdirPath == null) {
    		scriptdirPath = System.getProperty("user.home")
                    + File.separatorChar + ".globus" + File.separatorChar
                    + "scripts";
    	}
    	File scriptdir = new File(scriptdirPath);

        scriptdir.mkdirs();
        if (!scriptdir.exists()) {
            throw new IOException("Failed to create script directory ("
                    + scriptdir + ")");
        }
        script = File.createTempFile(getName(), ".submit", scriptdir);
        if (!logger.isDebugEnabled()) {
            script.deleteOnExit();
        }
        stdout = spec.getStdOutput() == null ? script.getAbsolutePath()
                + ".stdout" : spec.getStdOutput();
        stderr = spec.getStdError() == null ? script.getAbsolutePath()
                + ".stderr" : spec.getStdError();
        exitcode = script.getAbsolutePath() + ".exitcode";

        if (logger.isDebugEnabled()) {
            logger.debug("Writing " + getName() + " script to " + script);
        }

        String[] cmdline = buildCommandLine(scriptdir, script, exitcode,
            stdout, stderr);

        if (logger.isDebugEnabled()) {
            logCommandLine(cmdline);
        }
        Process process = Runtime.getRuntime().exec(cmdline, null, null);

        try {
            process.getOutputStream().close();
        }
        catch (IOException e) {
        }

        try {
            int code = process.waitFor();
            if (logger.isDebugEnabled()) {
                logger.debug(getProperties().getSubmitCommandName()
                        + " done (exit code " + code + ")");
            }
            if (code != 0) {
                String errorText = getOutput(process.getInputStream())
                        + getOutput(process.getErrorStream());
                throw new ProcessException("Could not submit job ("
                        + getProperties().getSubmitCommandName()
                        + " reported an exit code of " + code + "). "
                        + errorText);
            }
        }
        catch (InterruptedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Interrupted exception while waiting for "
                        + getProperties().getSubmitCommandName(), e);
            }
            if (listener != null) {
                listener.processFailed("The submission process was interrupted");
            }
        }

        String output = getOutput(process.getInputStream());
        jobid = parseSubmitCommandOutput(output);
        if (logger.isDebugEnabled()) {
            logger.debug("Submitted job with id '" + jobid + "'");
        }

        if (jobid.length() == 0) {
            String errorText = getOutput(process.getErrorStream());
            if (listener != null)
                listener.processFailed("Received empty jobid!\n" +
                                       output + "\n" + errorText);
        }

        process.getInputStream().close();

        getQueuePoller().addJob(
            job = createJob(jobid, stdout, spec.getStdOutputLocation(), stderr,
                spec.getStdErrorLocation(), exitcode, this));
    }

    protected abstract Job createJob(String jobid, String stdout,
            FileLocation stdOutputLocation, String stderr,
            FileLocation stdErrorLocation, String exitcode,
            AbstractExecutor executor);

    protected void logCommandLine(String[] cmdline) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cmdline.length; i++) {
            sb.append(cmdline[i]);
            if (i < cmdline.length - 1) {
                sb.append(" ");
            }
        }
        logger.debug("Command line: " + sb.toString());
    }

    protected String[] buildCommandLine(File jobdir, File script,
            String exitcode, String stdout, String stderr)
    throws IOException {

        writeScript(new BufferedWriter(new FileWriter(script)), exitcode,
            stdout, stderr);
        if (logger.isDebugEnabled()) {
            logger.debug("Wrote " + getName() + " script to " + script);
        }

        String[] params = getAdditionalSubmitParameters();
        if (params == null) {
            params = new String[0];
        }

        String[] cmdline = new String[2 + params.length];
        cmdline[0] = getProperties().getSubmitCommand();
        for (int i = 0; i < params.length; i++) {
            cmdline[i + 1] = params[i];
        }
        cmdline[cmdline.length - 1] = script.getAbsolutePath();

        return cmdline;
    }

    protected String[] getAdditionalSubmitParameters() {
        return null;
    }

    public void cancel() throws TaskSubmissionException {
        if (jobid == null || jobid.length() == 0) {
            throw new TaskSubmissionException("Can only cancel an active task");
        }
        String[] cmdline = new String[] { getProperties().getRemoveCommand(),
                jobid };
        try {
            logger.debug("Canceling job: jobid=" + jobid);
            Process process = Runtime.getRuntime().exec(cmdline, null, null);
            int ec = process.waitFor();
            if (ec != 0) {
                throw new TaskSubmissionException("Failed to cancel task. "
                        + getProperties().getRemoveCommandName()
                        + " returned with an exit code of " + ec);
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

    /**
       Overriding methods may perform miscellaneous Task
       processing here
     */
    protected void validate(Task task) {
    }

    protected abstract AbstractProperties getProperties();

    protected abstract String getName();

    protected String getOutput(InputStream is) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Waiting for output from "
                    + getProperties().getSubmitCommandName());
        }
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String out = br.readLine();
            while (out != null) {
                sb.append(out);
                out = br.readLine();
            }
        }
        catch (IOException e) {
            sb.append("<Error reading from stream>");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Output from "
                    + getProperties().getSubmitCommandName() + " is: \""
                    + sb.toString() + "\"");
        }
        sb.append('\n');
        return sb.toString();
    }

    protected String parseSubmitCommandOutput(String out) throws IOException {
        if ("".equals(out)) {
            throw new IOException(getProperties().getSubmitCommandName()
                    + " returned an empty job ID");
        }
        return out.trim();
    }

    protected abstract void writeScript(Writer wr, String exitcode,
            String stdout, String stderr) throws IOException;

    protected JobSpecification getSpec() {
        return spec;
    }

    protected Task getTask() {
        return task;
    }

    protected void cleanup() {
        if (!logger.isDebugEnabled()) {
            script.delete();
            new File(exitcode).delete();
            if (spec.getStdOutput() == null && stdout != null) {
                new File(stdout).delete();
            }
            if (spec.getStdError() == null && stderr != null) {
                new File(stderr).delete();
            }
        }
    }

    @Override
	public void processCompleted(int exitCode) {
        cleanup();
        if (listener != null) {
            listener.processCompleted(exitCode);
        }
    }

    @Override
	public void processFailed(String message) {
        cleanup();
        if (listener != null) {
            listener.processFailed(message);
        }
    }

    @Override
	public void statusChanged(int status) {
        if (listener != null) {
            listener.statusChanged(status);
        }
    }

    @Override
	public void stderrUpdated(String stderr) {
        if (listener != null) {
            listener.stderrUpdated(stderr);
        }
    }

    @Override
	public void stdoutUpdated(String stdout) {
        if (listener != null) {
            listener.stdoutUpdated(stdout);
        }
    }

    @Override
	public void processFailed(Exception e) {
        cleanup();
        if (listener != null) {
            listener.processFailed(e);
        }
    }

    protected void error(String message) {
        listener.processFailed(message);
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    protected abstract AbstractQueuePoller getQueuePoller();

    public String getJobid() {
        return jobid;
    }

    public Job getJob() {
        return job;
    }

    protected String quote(String s) {
        if ("".equals(s)) {
            return "\"\"";
        }
        boolean quotes = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 128 && TRIGGERS[c]) {
                quotes = true;
                break;
            }
        }
        if (!quotes) {
            return s;
        }
        StringBuffer sb = new StringBuffer();
        if (quotes) {
            sb.append('"');
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append('\\');
            }
            sb.append(c);
        }
        if (quotes) {
            sb.append('"');
        }
        return sb.toString();
    }

    /**
       @param list May be null or empty
       @throws IOException
    */
    protected void writeQuotedList(Writer writer, List<String> list) throws IOException
    {
        if (list != null && list.size() > 0) {
            writer.write(' ');
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                writer.write(quote(it.next()));
                if (it.hasNext())
                    writer.write(' ');
            }
        }
    }

    protected String replaceVars(String str) {
        StringBuffer sb = new StringBuffer();
        boolean escaped = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\') {
                if (escaped) {
                    sb.append('\\');
                }
                else {
                    escaped = true;
                }
            }
            else {
                if (c == '$' && !escaped) {
                    if (i == str.length() - 1) {
                        sb.append('$');
                    }
                    else {
                        int e = str.indexOf(' ', i);
                        if (e == -1) {
                            e = str.length();
                        }
                        String name = str.substring(i + 1, e);
                        Object attr = getSpec().getAttribute(name);
                        if (attr != null) {
                            sb.append(attr.toString());
                        }
                        else {
                            sb.append('$');
                            sb.append(name);
                        }
                        i = e;
                    }
                }
                else {
                    sb.append(c);
                }
                escaped = false;
            }
        }
        return sb.toString();
    }

    protected void writeMultiJobPostamble(Writer wr, String stdout, String stderr) throws IOException {
        wr.write("; echo \\\\\\$? > $ECF.$INDEX \" \\\" ");
        wr.write("1>>");
        wr.write(quote(stdout));
        wr.write(" 2>>");
        wr.write(quote(stderr));
        wr.write(" &\n");
        wr.write("  INDEX=$((INDEX + 1))\n");
        wr.write("done\n");
        wr.write("wait\n");
        wr.write("EC=\"0\"\n");
        wr.write("INDEX=0\n");
        wr.write("PATH=$PATH:/bin:/usr/bin\n");
        wr.write("for NODE in $NODES; do\n");
        wr.write("  TEC=\"N\"\n");
        wr.write("  while [ \"$TEC\" = \"N\" ]; do\n");
        wr.write("    read TEC < $ECF.$INDEX\n");
        wr.write("    if [ \"$TEC\" = \"N\" ]; then\n");
        wr.write("      sleep 1\n");
        wr.write("    fi\n");
        wr.write("  done\n");
        wr.write("  rm $ECF.$INDEX\n");
        wr.write("  if [ \"$EC\" = \"0\" -a \"$TEC\" != \"0\" ]; then\n");
        wr.write("    EC=$TEC\n");
        wr.write("    echo $EC > $ECF\n");
        wr.write("  fi\n");
        wr.write("  INDEX=$((INDEX + 1))\n");
        wr.write("done\n");
        wr.write("echo $EC > $ECF\n");
    }
}
