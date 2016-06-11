/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.util.Collection;
import java.util.Date;
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
        TRIGGERS['\"'] = true;
    }
    
    public static enum RunMode {
        PLAIN, // run directly on the node on which the LRM starts the job
        SSH, // ssh to each node in the nodefile
        MPIRUN, // run using mpirun
        APRUN, // run using aprun
        SRUN, // SLURM specific
        IBRUN,
        SRUN_OR_IBRUN,
        CUSTOM; 
    }

    protected File script;
    protected String stdout, stderr, exitcode, jobid;
    protected final JobSpecification spec;
    protected final Task task;
    protected final ProcessListener listener;
    protected Job job;
    
    /**
       Number of program invocations
     */
    protected int count = 1;
    
    /**
       depth: number of available threads per node
     */
    protected int depth = 1;


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
    
    
    /**
     * Figures out how to start the job (i.e. mpirun, ssh to nodes, etc.)
     * based on the jobType.
     * 
     * The general idea is this:
     *  jobType = "single":
     *      just run the command (hoping that you got dropped on some lead-node rather than the login node)
     *  
     *  jobType = "multiple:
     *      ssh to each node in the node file and run the command
     *  
     *  jobType = "MPI":
     *      use mpirun
     *      
     */
    protected RunMode getRunMode(JobSpecification spec) {
        String jobType = (String) spec.getAttribute("jobType");
        if (logger.isDebugEnabled()) {
            logger.debug("Job type: " + jobType);
        }
        if (spec.getAttribute("runCommand") != null) {
            return RunMode.CUSTOM;
        }
        if (jobType == null) {
            return RunMode.SSH;
        }
        if (jobType.equals("single")) {
            return RunMode.PLAIN;
        }
        if (jobType.equals("multiple")) {
            return RunMode.SSH;
        }
        if (jobType.equals("MPI")) {
            return RunMode.MPIRUN;
        }
        throw new IllegalArgumentException("Unknown job type: " + jobType);
    }

    protected void writePreamble(Writer wr, RunMode runMode, String nodeFile, String exitcodefile) throws IOException {
        if (spec.getDirectory() != null) {
            wr.write("cd " + quote(spec.getDirectory(), 0) + " && ");
        }
        switch (runMode) {
            case SSH:
                writeSSHPreamble(wr, nodeFile, exitcodefile);
                break;
            case PLAIN:
                break;
            case MPIRUN:
                wr.write("mpirun -np " + count + " -hostfile " + nodeFile + " ");
                String mpiOpts = (String) spec.getAttribute("mpiOptions");
                if (mpiOpts != null) {
                    wr.write(mpiOpts);
                    wr.write(" ");
                }
                break;
            case APRUN:
                wr.write("aprun -n " + count + " -N 1 -cc none -d " +
                     depth + " -F exclusive ");
                break;
            case IBRUN:
                wr.write("ibrun ");
                break;
            case SRUN:
                wr.write("srun ");
                break;
            case SRUN_OR_IBRUN:
                wr.write("RUNCOMMAND=$( command -v ibrun || command -v srun )\n");
                wr.write("$RUNCOMMAND ");
                break;
            case CUSTOM:
                String cmd = replaceVars((String) spec.getAttribute("runCommand"), nodeFile);
                wr.write(cmd);
                wr.write(' ');
                break;
        }
        //Reverting commit 5c30017012706b27500731c07e242e7c24c6dd76
        // getQuotingLevel is overriden in SlurmExecutor to return 2
        // for every other it returns 1
        int quotingLevel = getQuotingLevel(runMode);
        if (quotingLevel == 2) {
            wr.write("\"/bin/bash -c \\\"");
        }
        else {
            wr.write("/bin/bash -c \"");
        }
    }

    protected void writeCommand(Writer wr, RunMode runMode) throws IOException {
        // quote currently is broken and does not return the
        // correct quoted string based on the quotingLevel.
        // This is
        int quotingLevel = 1; //getQuotingLevel(runMode);

        wr.write(quote(spec.getExecutable(), quotingLevel));
        writeQuotedList(wr, spec.getArgumentsAsList(), quotingLevel);

        if (spec.getStdInput() != null) {
            wr.write(" < " + quote(spec.getStdInput(), quotingLevel));
        }
    }

    protected void writePostamble(Writer wr, RunMode runMode, String exitCodeFile, 
            String stdout, String stderr, String lrmOutSuffix) throws IOException {
        switch (runMode) {
            case SSH:
                writeRedirects(wr, "$ECF.$INDEX", runMode, stdout, stderr);
                break;
            default:
                writeRedirects(wr, exitCodeFile, runMode, stdout, stderr);
        }
        switch (runMode) {
            case SSH:
                writeSSHPostamble(wr);
                break;
            default:
                // nothing
        }
        if (lrmOutSuffix != null) {
            String lrmOut = quote(stdout + lrmOutSuffix);
            wr.write("\nif [ -f " + lrmOut + " ]; then cat " + lrmOut + " >> " + quote(stdout) + "; fi\n");
            String lrmErr = quote(stderr + lrmOutSuffix);
            wr.write("\nif [ -f " + lrmErr + " ]; then cat " + lrmErr + " >> " + quote(stderr) + "; fi\n");
        }
    }

    private void writeRedirects(Writer wr, String exitCodeFile, RunMode runMode, String stdout, String stderr) throws IOException {
        int quotingLevel = getQuotingLevel(runMode);
        //Reverting commit 5c30017012706b27500731c07e242e7c24c6dd76
        if (quotingLevel == 2) {
            wr.write("; echo \\$? > " + exitCodeFile + "\\\" \" ");
        }
        else{
            wr.write("; echo \\$? > " + exitCodeFile + "\" ");
        }

        wr.write(" 1>>");
        wr.write(quote(stdout, 1));
        wr.write(" 2>>");
        wr.write(quote(stderr, 1));
    }

    protected void writeWrapper(Writer wr, String sJobType, String nodeFile) throws IOException {
        String wrapper = getProperties().getProperty("wrapper." + sJobType);
        if (logger.isDebugEnabled()) {
            logger.debug("Wrapper: " + wrapper);
        }
        if (wrapper != null) {
            wrapper = replaceVars(wrapper, nodeFile);
            wr.write(wrapper);
            wr.write(' ');
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Wrapper after variable substitution: " + wrapper);
        }
    }

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
    
    protected int getQuotingLevel(RunMode runMode) {
        return 1;
    }
    
    protected String quote(String s) {
        return quote(s, 0);
    }
        
    protected String quote(String s, int level) {
        switch (level) {
            case 0:
                return quote(s, "");
            case 1:
                return quote(s, "\\");
            case 2:
                return quote(s, "\\\\\\");
            default:
                throw new IllegalArgumentException("Unknown quoting level: " + level);
        }
    }

    protected String quote(String s, String outerQ) {
        if ("".equals(s)) {
            return outerQ + "\"" + outerQ + "\"";
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
            sb.append(outerQ);
            sb.append("\"");
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append(outerQ);
                sb.append(outerQ);
                sb.append("\\");
            }
            sb.append(c);
        }
        if (quotes) {
            sb.append(outerQ);
            sb.append("\"");
        }
        return sb.toString();
    }

    /**
       @param list May be null or empty
       @throws IOException
    */
    protected void writeQuotedList(Writer writer, List<String> list, int quotingLevel) throws IOException {
        if (list != null && list.size() > 0) {
            writer.write(' ');
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String a = it.next();
                writer.write(quote(a, quotingLevel));
                if (it.hasNext()) {
                    writer.write(' ');
                }
            }
        }
    }

    protected String replaceVars(String str, String hostFile) {
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
                        else if (name.equals("nodefile") || name.equals("hostfile")) {
                            sb.append(hostFile);
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
    
    protected void writeSSHPreamble(Writer wr, String nodeFile, String exitcodefile) throws IOException {
        wr.write("NODES=`cat " + nodeFile + "`\n");
        wr.write("ECF=" + exitcodefile + "\n");
        wr.write("INDEX=0\n");
        wr.write("for NODE in $NODES; do\n");
        wr.write("  echo \"N\" >$ECF.$INDEX\n");
        wr.write("  ssh $NODE ");
    }

    protected void writeSSHPostamble(Writer wr) throws IOException {
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

    protected String join(Collection<String> names, String separator) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> i = names.iterator();
        while (i.hasNext()) {
            sb.append(i.next());
            if (i.hasNext()) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }
    
    protected final int parseAndValidateInt(Object obj, String name) {
        try {
            assert(obj != null);
            return Integer.parseInt(obj.toString());
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Illegal value for " + name + ". Must be an integer.");
        }
    }

    protected final void writeHeader(Writer writer) throws IOException {
        writer.write("#!/bin/bash\n\n");
        writer.write("#CoG This script generated by CoG\n");
        writer.write("#CoG   by class: " + getClass() + '\n');
        writer.write("#CoG   on date: " + new Date() + "\n\n");
    }
}
