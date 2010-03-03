//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.sge;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;

public class SGEExecutor extends AbstractExecutor {
    public static final Logger logger = Logger.getLogger(SGEExecutor.class);

    public SGEExecutor(Task task, ProcessListener listener) {
        super(task, listener);
    }

    protected void writeAttr(String attrName, String arg, Writer wr,
            String defaultValue) throws IOException {
        Object value = getSpec().getAttribute(attrName);
        if (value != null) {
            wr.write("#$ " + arg + String.valueOf(value) + '\n');
        }
        else if (defaultValue != null) {
            wr.write("#$ " + arg + String.valueOf(defaultValue) + '\n');
        }
    }

    protected void writeAttr(String attrName, String arg, Writer wr)
            throws IOException {
        writeAttr(attrName, arg, wr, null);
    }

    protected void writeWallTime(Writer wr) throws IOException {
        Object walltime = getSpec().getAttribute("maxwalltime");
        if (walltime != null) {
            wr.write("#$ -l h_rt="
                    + WallTime.normalize(walltime.toString(), "sge-native")
                    + '\n');
        }
    }

    protected void writeScript(Writer wr, String exitcodefile, String stdout,
            String stderr) throws IOException {
        Task task = getTask();
        JobSpecification spec = getSpec();
        String type = (String) spec.getAttribute("jobType");
        boolean multiple = false;
        if ("multiple".equals(type)) {
            multiple = true;
        }

        wr.write("#!/bin/bash\n");
        wr.write("#$ -N " + task.getName() + '\n');
        // ranger requires this. might as well be default
        wr.write("#$ -V\n");
        writeAttr("project", "-A ", wr);

        writeAttr("count", "-pe "
                + getAttribute(spec, "pe", getSGEProperties().getDefaultPE())
                + " ", wr, "1");

        writeWallTime(wr);
        writeAttr("queue", "-q ", wr, "normal");
        if (spec.getStdInput() != null) {
            wr.write("#$ -i " + quote(spec.getStdInput()) + '\n');
        }
        wr.write("#$ -o " + quote(stdout) + '\n');
        wr.write("#$ -e " + quote(stderr) + '\n');

        if (!spec.getEnvironmentVariableNames().isEmpty()) {
            wr.write("#$ -v ");
            Iterator i = spec.getEnvironmentVariableNames().iterator();
            while (i.hasNext()) {
                String name = (String) i.next();
                wr.write(name);
                wr.write('=');
                wr.write(quote(spec.getEnvironmentVariable(name)));
                if (i.hasNext()) {
                    wr.write(',');
                }
            }
            wr.write('\n');
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Job type: " + type);
        }
        if (type != null) {
            String wrapper = Properties.getProperties().getProperty(
                "wrapper." + type);
            if (logger.isDebugEnabled()) {
                logger.debug("Wrapper: " + wrapper);
            }
            if (wrapper != null) {
                wrapper = replaceVars(wrapper);
                wr.write(wrapper);
                wr.write(' ');
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Wrapper after variable substitution: " + wrapper);
            }
        }
        if (spec.getDirectory() != null) {
            wr.write("cd " + quote(spec.getDirectory()) + " && ");
        }

        if (multiple) {
            writeMultiJobPreamble(wr, exitcodefile);
        }
        wr.write(quote(spec.getExecutable()));
        List args = spec.getArgumentsAsList();
        if (args != null && args.size() > 0) {
            wr.write(' ');
            Iterator i = args.iterator();
            while (i.hasNext()) {
                wr.write(quote((String) i.next()));
                if (i.hasNext()) {
                    wr.write(' ');
                }
            }
        }

        if (spec.getStdInput() != null) {
            wr.write(" < " + quote(spec.getStdInput()));
        }
        if (multiple) {
            writeMultiJobPostamble(wr);
        }
        else {
            wr.write('\n');
            wr.write("/bin/echo $? >" + exitcodefile + '\n');
        }
        wr.close();
    }
    
    protected void writeMultiJobPreamble(Writer wr, String exitcodefile)
            throws IOException {
        wr.write("NODES=`cat $PE_HOSTFILE | awk '{ for(i=0;i<$2;i++){print $1} }'`\n");
        wr.write("ECF=" + exitcodefile + "\n");
        wr.write("INDEX=0\n");
        wr.write("for NODE in $NODES; do\n");
        wr.write("  echo \"N\" >$ECF.$INDEX\n");
        wr.write("  ssh $NODE /bin/bash -c \\\" \"");
    }

    private String getAttribute(JobSpecification spec, String name,
            String defaultValue) {
        Object value = spec.getAttribute(name);
        if (value == null) {
            return defaultValue;
        }
        else {
            return value.toString();
        }
    }

    protected String getName() {
        return "SGE";
    }

    protected AbstractProperties getProperties() {
        return Properties.getProperties();
    }
    
    protected Properties getSGEProperties() {
        return (Properties) getProperties();
    }

    protected String parseSubmitCommandOutput(String out) throws IOException {
        out = out.trim();
        StringBuilder sb = new StringBuilder();
        for (int i = out.length() - 1; i > 0; i--) {
            if (!Character.isDigit(out.charAt(i))) {
                break;
            }
            else {
                sb.append(out.charAt(i));
            }
        }
        sb.reverse();
        if (logger.isInfoEnabled()) {
            logger.info("Job id from qsub: " + sb.toString().trim());
        }
        return sb.toString().trim();
    }

    private static final String[] QSUB_PARAMS = new String[] { "-terse" };

    protected String[] getAdditionalSubmitParameters() {
        return QSUB_PARAMS;
    }

    protected Job createJob(String jobid, String stdout,
            FileLocation stdOutputLocation, String stderr,
            FileLocation stdErrorLocation, String exitcode,
            AbstractExecutor executor) {
        return new Job(jobid, stdout, stdOutputLocation, stderr,
            stdErrorLocation, exitcode, executor);
    }

    private static QueuePoller poller;

    protected AbstractQueuePoller getQueuePoller() {
        synchronized (SGEExecutor.class) {
            if (poller == null) {
                poller = new QueuePoller(getProperties());
                poller.start();
            }
            return poller;
        }
    }
}
