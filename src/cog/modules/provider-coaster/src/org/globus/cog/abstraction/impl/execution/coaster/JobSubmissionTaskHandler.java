//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.coaster;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.ExtendedStatusListener;
import org.globus.cog.abstraction.coaster.service.local.CoasterResourceTracker;
import org.globus.cog.abstraction.coaster.service.local.LocalRequestManager;
import org.globus.cog.abstraction.coaster.service.local.LocalService;
import org.globus.cog.abstraction.impl.common.AbstractDelegatedTaskHandler;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.ChannelException;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.Command.Callback;
import org.ietf.jgss.GSSCredential;

public class JobSubmissionTaskHandler extends AbstractDelegatedTaskHandler implements Callback, ExtendedStatusListener {
    private static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class);

    private static Set<Object> configured, configuring;
    
    private static Map<Service, TaskSubmissionException> checkedServices = 
        new HashMap<Service, TaskSubmissionException>();

    static {
        configured = new HashSet<Object>();
        configuring = new HashSet<Object>();
    }

    private static String checkConfigured(CoasterChannel channel, Task task) throws InterruptedException {
        Service s = task.getService(0);
        synchronized (s) {
            while (s.getAttribute("coaster:configuring") != null) {
                s.wait(100);
            }
            String configId = (String) s.getAttribute("coaster:configid");
            if (configId == null) {
                s.setAttribute("coaster:configuring", Boolean.TRUE);
            }
            else {
                task.setAttribute("coaster:configid", configId);
            }
            return configId;
        }
    }

    private static void setConfigured(CoasterChannel channel, Task task, String configId) {
        Service s = task.getService(0);
        synchronized (s) {
            s.removeAttribute("coaster:configuring");
            s.setAttribute("coaster:configid", configId);
            s.notifyAll();
        }
    }

    // private Task startServiceTask;
    private SubmitJobCommand jsc;
    private GSSCredential cred;
    private String jobid;
    private String url;
    private String cancelMessage;
    private boolean cancel;
    private boolean autostart;

    public JobSubmissionTaskHandler() {
        this(true);
    }

    public JobSubmissionTaskHandler(boolean autostart) {
        this.autostart = autostart;
    }

    public void submit(Task task) throws IllegalSpecException, InvalidSecurityContextException,
            InvalidServiceContactException, TaskSubmissionException {
        checkAndSetTask(task);
        validateTaskSettings();
        task.setStatus(Status.SUBMITTING);
        try {
            CoasterChannel channel = getChannel(task);
            String configId = configureService(channel, task);
            submitJob(channel, task, configId);
        }
        catch (Exception e) {
            throw new TaskSubmissionException("Could not submit job", e);
        }
    }

    private CoasterChannel getChannel(Task task) throws InvalidServiceContactException,
            IllegalSpecException, TaskSubmissionException, InvalidSecurityContextException,
            ChannelException {
        if (autostart) {
            String provider = getBootHandlerProvider(task);
            url = ServiceManager.getDefault().reserveService(task, provider);
            task.getService(0).setAttribute("coaster-url", url);
        }
        else {
            url = task.getService(0).getServiceContact().getContact();
        }
        return ChannelManager.getManager().reserveChannel(url, cred, LocalRequestManager.INSTANCE);
    }

    private String configureService(CoasterChannel channel, Task task) throws InterruptedException,
            ProtocolException, IOException {
        String configId = checkConfigured(channel, task);
        if (configId == null) {
            ServiceConfigurationCommand scc = new ServiceConfigurationCommand(task);
            byte[] reply = scc.execute(channel);
            configId = new String(reply);
            
            Object rt = task.getService(0).getAttribute("resource-tracker");
            if (rt != null) {
                if (rt instanceof CoasterResourceTracker) {
                    LocalService ls = (LocalService) channel.getChannelContext().getService();
                    ls.addResourceTracker(channel.getChannelContext(), 
                        task.getService(0), (CoasterResourceTracker) rt);
                }
                else {
                    logger.warn("Invalid resource tracker specified: " + rt.getClass() 
                        + " does not implement " + CoasterResourceTracker.class);
                }
            }
            setConfigured(channel, task, configId);
        }
        return configId;
    }

    private void submitJob(CoasterChannel channel, Task task, String configId) throws ProtocolException {
        jsc = new SubmitJobCommand(task, configId);
        jsc.executeAsync(channel, this);
    }
    
    private static final Pattern COLON = Pattern.compile(":");

    private String getBootHandlerProvider(Task t) throws InvalidServiceContactException,
            IllegalSpecException {
        String jm = getJobManager(t);
        if (jm == null) {
            throw new InvalidServiceContactException("Missing job manager");
        }
        String[] jmp = COLON.split(jm);
        if (jmp.length < 2) {
            throw new InvalidServiceContactException("Invalid job manager: " + jm
                    + ". Use <provider>:<remote-provider>[:<remote-job-manager>].");
        }
        return jmp[0];
    }

    private String getJobManager(Task t) throws IllegalSpecException {
        Service s = t.getService(0);
        if (s == null) {
            throw new IllegalSpecException("Missing service");
        }
        if (s instanceof ExecutionService) {
            return ((ExecutionService) s).getJobManager();
        }
        else {
            throw new IllegalSpecException("Service must be an ExecutionService");
        }
    }

    public void suspend() throws InvalidSecurityContextException, TaskSubmissionException {
    }

    public void resume() throws InvalidSecurityContextException, TaskSubmissionException {
    }

    public synchronized void cancel(String message) throws InvalidSecurityContextException,
            TaskSubmissionException {
        // TODO shouldn't this be setting the task status?
        try {
            if (jobid != null) {
                CoasterChannel channel =
                        ChannelManager.getManager().reserveChannel(url, cred,
                            LocalRequestManager.INSTANCE);
                CancelJobCommand cc = new CancelJobCommand(jobid);
                cc.execute(channel);
                cancel = false;
                getTask().setStatus(new StatusImpl(Status.CANCELED, message, null));
            }
            else {
                cancel = true;
                cancelMessage = message;
            }
        }
        catch (Exception e) {
            throw new TaskSubmissionException(e);
        }
    }

    private String getJavaHome(Task task) {
        JobSpecification js = (JobSpecification) task.getSpecification();
        return js.getEnvironmentVariable("JAVA_HOME");
    }

    public void errorReceived(Command cmd, String msg, Exception t) {
        Status s = new StatusImpl(Status.FAILED, msg, t);
        getTask().setStatus(s);
    }

    public void replyReceived(Command cmd) {
        if (cmd == jsc) {
            jobid = cmd.getInDataAsString(0);
            getTask().setStatus(Status.SUBMITTED);
            if (cancel) {
                try {
                    cancel(cancelMessage);
                }
                catch (Exception e) {
                    logger.warn("Failed to cancel jobid: " + jobid + " " + e.getMessage());
                }
            }
            jsc = null;
            if (logger.isDebugEnabled()) {
                logger.debug("Submitted task " + getTask() + ". Job id: " + jobid);
            }
            NotificationManager.getDefault().registerListener(jobid, getTask(), this);
        }
    }

    public void statusChanged(Status s, String out, String err) {
        Task t = getTask();
        if (out != null) {
            t.setStdOutput(out);
        }
        if (err != null) {
            t.setStdError(err);
        }
        t.setStatus(s);
    }

    public boolean getAutostart() {
        return autostart;
    }

    public void setAutostart(boolean autostart) {
        this.autostart = autostart;
    }
    
    private void validateTaskSettings() throws TaskSubmissionException {
        synchronized (JobSubmissionTaskHandler.class) {
            Task task = getTask();
            Service s = task.getService(0);
            TaskSubmissionException e = checkedServices.get(s);
            if (e != null) {
                throw e;
            }
            else if (!checkedServices.containsKey(s)) {
                try {
                    validateTask();
                    checkedServices.put(s, null);
                }
                catch (IllegalArgumentException ee) {
                    e = new TaskSubmissionException(ee.getMessage());
                    checkedServices.put(task.getService(0), e);
                    throw e;
                }
            }
        }
    }
    
    private void validateTask() {
        checkPositiveInt("slots", 0);
        checkPositiveInt("maxNodes", 0);
        checkPositiveInt("maxTime", 0);
        Integer nodeGranularity = getInt("nodeGranularity");
        Integer maxNodes = getInt("maxNodes");
        if (nodeGranularity != null && maxNodes != null) {
            if (nodeGranularity > maxNodes) {
                throw new IllegalArgumentException("nodeGranularity > maxNodes (" + 
                    nodeGranularity + " > " + maxNodes + ")");
            }
        }
        
        checkGreaterOrEqualThan("lowOverallocation", 1);
        checkGreaterThan("highOverallocation", 1);
        
        checkGreaterThan("allocationStepSize", 0);
        checkLessOrEqualThan("allocationStepSize", 1);
        
        checkGreaterOrEqualThan("spread", 0);
        checkLessOrEqualThan("spread", 1);
        
        checkGreaterOrEqualThan("parallelism", 0);
        checkLessOrEqualThan("parallelism", 1);
    }

    private void checkPositiveInt(String name, int i) {
        Integer v = getInt(name);
        if (v != null && v <= 0) {
            throw new IllegalArgumentException(name + " must be > 0 (currently " + v + ")");
        }
    }

    private void checkGreaterOrEqualThan(String name, double d) {
        Double v = getDouble(name);
        if (v != null && v < d) {
            throw new IllegalArgumentException(name + " must be >= " + d + " (currently " + v + ")");
        }
    }

    private void checkLessOrEqualThan(String name, double d) {
        Double v = getDouble(name);
        if (v != null && v > d) {
            throw new IllegalArgumentException(name + " must be <= " + d + " (currently " + v + ")");
        }
    }

    private void checkLessThan(String name, double d) {
        Double v = getDouble(name);
        if (v != null && v >= d) {
            throw new IllegalArgumentException(name + " must be < " + d + " (currently " + v + ")");
        }
    }

    private void checkGreaterThan(String name, double d) {
        Double v = getDouble(name);
        if (v != null && v <= d) {
            throw new IllegalArgumentException(name + " must be > " + d + " (currently " + v + ")");
        }
    }

    private Double getDouble(String name) {
        Object v = ((JobSpecification) getTask().getSpecification()).getAttribute(name.toLowerCase());
        if (v == null) {
            return null;
        }
        else if (v instanceof String) {
            return Double.parseDouble((String) v);
        }
        else if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        else {
            throw new IllegalArgumentException("Invalid valid for " + name + ": " + v + "; must be a floating point number.");
        }
    }

    private Integer getInt(String name) {
        Object v = ((JobSpecification) getTask().getSpecification()).getAttribute(name.toLowerCase());
        if (v == null) {
            return null;
        }
        else if (v instanceof String) {
            return Integer.parseInt((String) v);
        }
        else {
            throw new IllegalArgumentException("Invalid valid for " + name + ": " + v + "; must be an integer.");
        }
    }
}
