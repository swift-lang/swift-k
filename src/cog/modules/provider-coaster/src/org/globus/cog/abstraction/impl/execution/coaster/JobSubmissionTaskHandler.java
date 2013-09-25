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
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.ExtendedStatusListener;
import org.globus.cog.abstraction.coaster.service.local.CoasterResourceTracker;
import org.globus.cog.abstraction.coaster.service.local.LocalRequestManager;
import org.globus.cog.abstraction.coaster.service.local.LocalService;
import org.globus.cog.abstraction.impl.common.AbstractDelegatedTaskHandler;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.channels.ChannelException;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.commands.Command.Callback;
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

    private static boolean checkConfigured(KarajanChannel channel) throws InterruptedException {
        Object key = channel.getChannelContext();
        synchronized (configuring) {
            while (configuring.contains(key)) {
                configuring.wait(100);
            }
            boolean c = configured.contains(key);
            if (!c) {
                configuring.add(key);
            }
            return c;
        }
    }

    private static void setConfigured(KarajanChannel channel) {
        Object key = channel.getChannelContext();
        synchronized (configuring) {
            configuring.remove(key);
            configured.add(key);
            configuring.notifyAll();
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
            KarajanChannel channel = getChannel(task);
            configureService(channel, task);
            submitJob(channel, task);
        }
        catch (Exception e) {
            throw new TaskSubmissionException("Could not submit job", e);
        }
    }

    private KarajanChannel getChannel(Task task) throws InvalidServiceContactException,
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

    private void configureService(KarajanChannel channel, Task task) throws InterruptedException,
            ProtocolException, IOException {
        if (!checkConfigured(channel)) {
            ServiceConfigurationCommand scc = new ServiceConfigurationCommand(task);
            scc.execute(channel);
            
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
            setConfigured(channel);
        }
    }

    private void submitJob(KarajanChannel channel, Task task) throws ProtocolException {
        jsc = new SubmitJobCommand(task);
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
                KarajanChannel channel =
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

    private static Task submitTask() throws Exception {
        Task t = new TaskImpl();
        t.setType(Task.JOB_SUBMISSION);
        JobSpecification js = new JobSpecificationImpl();
        js.setExecutable("/bin/date");
        int base = (int) (rnd.nextDouble() * 20) + 5;
        //js.addArgument(String.valueOf(base + (int) (rnd.nextDouble() * base)));
        js.setAttribute("maxwalltime", "00:00:" + String.valueOf(base * 2));
        js.setAttribute("slots", "2");
        js.setAttribute("lowOverallocation", "6");
        js.setAttribute("nodeGranularity", "1");
        js.setAttribute("maxNodes", "2");
        js.setRedirected(true);
        //js.setAttribute("remoteMonitorEnabled", "true");
        t.setSpecification(js);
        ExecutionService s = new ExecutionServiceImpl();
        // s.setServiceContact(new ServiceContactImpl("localhost"));
        // s.setServiceContact(new
        // ServiceContactImpl("tp-grid1.ci.uchicago.edu"));
        // s.setServiceContact(new
        // ServiceContactImpl("tg-grid1.uc.teragrid.org"));
        s.setServiceContact(new ServiceContactImpl("localhost"));
        s.setProvider("coaster");
        s.setJobManager("local:local");
        // s.setJobManager("gt2:pbs");
        s.setSecurityContext(new SecurityContextImpl());
        t.setService(0, s);
        // JobSubmissionTaskHandler th = new JobSubmissionTaskHandler(
        // AbstractionFactory.newExecutionTaskHandler("local"));
        JobSubmissionTaskHandler th = new JobSubmissionTaskHandler();
        th.setAutostart(true);
        th.submit(t);
        return t;
    }

    private static Random rnd;

    public static void main(String[] args) {
        try {
        	rnd = new Random();
        	rnd.setSeed(10L);
            long s = System.currentTimeMillis();
            Task[] ts = new Task[1];
            for (int i = 0; i < ts.length; i++) {
                ts[i] = submitTask();
                if (i % 1 == 0) {
                    System.err.println((i + 1) + " submitted");
                }
            }
            for (int i = 0; i < ts.length; i++) {
                ts[i].waitFor();
                if (ts[i].getStatus().getStatusCode() == Status.FAILED) {
                    System.err.println("Job failed");
                    System.err.println(ts[i].getStatus().getMessage());
                    if (ts[i].getStatus().getException() != null) {
                        ts[i].getStatus().getException().printStackTrace();
                    }
                }
                else {
                    System.out.println(ts[i].getStdOutput());
                }
                if (i % 100 == 0 && i > 0) {
                    System.err.println(i + " done");
                }
            }
            System.err.println("All " + ts.length + " jobs done");
            System.err.println("Total time: " + (System.currentTimeMillis() - s));
            System.exit(0);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
