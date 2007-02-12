//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.cobalt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessException;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.gsi.gssapi.auth.AuthorizationException;
import org.ietf.jgss.GSSException;

public class CobaltExecutor implements ProcessListener {
    public static final Logger logger = Logger.getLogger(CobaltExecutor.class);

    private JobSpecification spec;
    private Task task;
    private static QueuePoller poller;
    private ProcessListener listener;
    private String stdout, stderr, exitcode;
    private File script;
    private String cqsub;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    public CobaltExecutor(Task task, ProcessListener listener) {
        this.task = task;
        this.spec = (JobSpecification) task.getSpecification();
        this.listener = listener;
        this.cqsub = Properties.getProperties().getCQSub();
    }

    private static synchronized QueuePoller getProcessPoller() {
        if (poller == null) {
            poller = new QueuePoller();
            poller.start();
        }
        return poller;
    }

    public void start() throws AuthorizationException, GSSException,
            IOException, ProcessException {
        File scriptdir = new File(System.getProperty("user.home")
                + File.separatorChar + ".globus" + File.separatorChar
                + "scripts");
        scriptdir.mkdirs();
        if (!scriptdir.exists()) {
            throw new IOException("Failed to create script directory ("
                    + scriptdir + ")");
        }
        script = File.createTempFile("cobalt", ".sh", scriptdir);
        stdout = spec.getStdOutput() == null ? script.getAbsolutePath()
                + ".stdout" : spec.getStdOutput();
        stderr = spec.getStdError() == null ? script.getAbsolutePath()
                + ".stderr" : spec.getStdError();
        exitcode = script.getAbsolutePath() + ".exitcode";
        writeScript(new BufferedWriter(new FileWriter(script)), exitcode,
                stdout, stderr);
        if (logger.isDebugEnabled()) {
            logger.debug("Wrote submit script to " + script);
        }
        String[] cmdline = buildCMDLine(script.getAbsolutePath());

        Process process = Runtime.getRuntime().exec(cmdline, null, null);

        try {
            process.getOutputStream().close();
        }
        catch (IOException e) {
        }

        try {
            int code = process.waitFor();
            if (code != 0) {
                throw new ProcessException(
                        "Could not submit job (qsub reported an exit code of "
                                + code + "). "
                                + getOutput(process.getErrorStream()));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("QSub done (exit code " + code + ")");
            }
        }
        catch (InterruptedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Interrupted exception while waiting for qsub", e);
            }
            if (listener != null) {
                listener
                        .processFailed("The submission process was interrupted");
            }
        }

        String jobid = getOutput(process.getInputStream());

        getProcessPoller().addJob(
                new Job(jobid, spec.isRedirected() ? stdout : null, spec
                        .isRedirected() ? stderr : null, exitcode, this));
    }

    private void error(String message) {
        listener.processFailed(message);
    }

    protected void writeScript(Writer wr, String exitcodefile, String stdout,
            String stderr) throws IOException {
        if (spec.getStdInput() != null) {
            throw new IOException("The Cobalt provider cannot redirect STDIN");
        }
        Iterator i = spec.getEnvironmentVariableNames().iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            wr.write(name);
            wr.write('=');
            wr.write(quote(spec.getEnvironmentVariable(name)));
            wr.write('\n');
        }
        wr.write(quote(spec.getExecutable()));
        List args = spec.getArgumentsAsList();
        if (args != null && args.size() > 0) {
            wr.write(' ');
            i = args.iterator();
            while (i.hasNext()) {
                wr.write(quote((String) i.next()));
                wr.write(' ');
            }
        }
        wr.write(" 1>" + quote(stdout) + ' ');
        wr.write(" 2>" + quote(stderr) + '\n');
        wr.write("/bin/echo $? >" + exitcodefile + '\n');
        wr.close();
    }
    
    protected void addAttr(String attrName, String option, List l) {
        Object value = spec.getAttribute(attrName);
        if (value != null) {
            l.add(option);
            l.add(String.valueOf(value));
        }
    }

    protected String[] buildCMDLine(String script) {
        List l = new ArrayList();
        l.add(cqsub);
        addAttr("queue", "-q", l);
        Collection names = spec.getEnvironmentVariableNames();
        if (names != null && names.size() > 0) {
            l.add("-e");
            StringBuffer sb = new StringBuffer();
            Iterator i = names.iterator();
            while (i.hasNext()) {
                String name = (String) i.next();
                sb.append(name);
                sb.append('=');
                sb.append(quote(spec.getEnvironmentVariable(name)));
                if (i.hasNext()) {
                    sb.append(':');
                }
            }
            l.add(sb.toString());
        }
        addAttr("mode", "-m", l);
        //We're gonna treat this as the process count
        addAttr("count", "-c", l);
        addAttr("project", "-p", l);
        addAttr("queue", "-q", l);
        addAttr("maxwalltime", "-t", l);
        if (spec.getDirectory() != null) {
            l.add("-C");
            l.add(spec.getDirectory());
        }
        l.add("/bin/sh");
        l.add(script);
        return (String[]) l.toArray(EMPTY_STRING_ARRAY);
    }

    protected String quote(String s) {
        boolean quotes = false;
        if (s.indexOf(' ') != -1) {
            quotes = true;
        }
        StringBuffer sb = new StringBuffer();
        if (quotes) {
            sb.append('"');
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append('\\');
                break;
            }
            sb.append(c);
        }
        if (quotes) {
            sb.append('"');
        }
        return sb.toString();
    }

    protected String getOutput(InputStream is) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Waiting for output from qsub");
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String out = br.readLine();
        if (logger.isDebugEnabled()) {
            logger.debug("Output from qsub is: \"" + out + "\"");
        }
        if ("".equals(out)) {
            throw new IOException("Qsub returned empty job ID");
        }
        return out;
    }

    protected void cleanup() {
        script.delete();
        new File(exitcode).delete();
        if (spec.getStdOutput() == null && stdout != null) {
            new File(stdout).delete();
        }
        if (spec.getStdError() == null && stderr != null) {
            new File(stderr).delete();
        }
    }

    public void processCompleted(int exitCode) {
        cleanup();
        if (listener != null) {
            listener.processCompleted(exitCode);
        }
    }

    public void processFailed(String message) {
        cleanup();
        if (listener != null) {
            listener.processFailed(message);
        }
    }

    public void statusChanged(int status) {
        if (listener != null) {
            listener.statusChanged(status);
        }
    }

    public void stderrUpdated(String stderr) {
        if (listener != null) {
            listener.stderrUpdated(stderr);
        }
    }

    public void stdoutUpdated(String stdout) {
        if (listener != null) {
            listener.stdoutUpdated(stdout);
        }
    }

    public void processFailed(Exception e) {
        cleanup();
        if (listener != null) {
            listener.processFailed(e);
        }
    }
}
