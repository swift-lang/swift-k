//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.coaster;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.local.LocalRequestManager;
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
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.channels.ChannelException;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.commands.Command.Callback;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

public class JobSubmissionTaskHandler extends AbstractDelegatedTaskHandler implements Callback {
    private static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class);

    private static Set configured, configuring;

    static {
        configured = new HashSet();
        configuring = new HashSet();
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

    private Task startServiceTask;
    private SubmitJobCommand jsc;
    private GSSCredential cred;
    private String jobid;
    private String url;
    private String cancelMessage;
    private boolean cancel;
    private boolean autostart;

    public JobSubmissionTaskHandler() {
        this.autostart = true;
    }

    public void submit(Task task) throws IllegalSpecException, InvalidSecurityContextException,
            InvalidServiceContactException, TaskSubmissionException {
        checkAndSetTask(task);
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
            cred = getCredentials(task);
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
            setConfigured(channel);
        }
    }

    private void submitJob(KarajanChannel channel, Task task) throws ProtocolException {
        jsc = new SubmitJobCommand(task);
        jsc.executeAsync(channel, this);
    }

    private String getBootHandlerProvider(Task t) throws InvalidServiceContactException,
            IllegalSpecException {
        String jm = getJobManager(t);
        if (jm == null) {
            throw new InvalidServiceContactException("Missing job manager");
        }
        String[] jmp = jm.split(":");
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

    private GSSCredential getCredentials(Task task) throws InvalidSecurityContextException {
        SecurityContext sc = task.getService(0).getSecurityContext();
        if (sc == null) {
            GSSManager manager = ExtendedGSSManager.getInstance();
            try {
                return manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
            }
            catch (GSSException e) {
                throw new InvalidSecurityContextException(e);
            }
        }
        else {
            return (GSSCredential) sc.getCredentials();
        }
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
                    logger.warn("Failed to cancel job " + jobid, e);
                }
            }
            jsc = null;
            if (logger.isInfoEnabled()) {
                logger.info("Submitted task " + getTask() + ". Job id: " + jobid);
            }
            NotificationManager.getDefault().registerTask(jobid, getTask());
        }
    }

    public boolean getAutostart() {
        return autostart;
    }

    public void setAutostart(boolean autostart) {
        this.autostart = autostart;
    }

    private static Task submitTask() throws Exception {
        Task t = new TaskImpl();
        t.setType(Task.JOB_SUBMISSION);
        JobSpecification js = new JobSpecificationImpl();
        js.setExecutable("/bin/sleep");
        int base = (int) (rnd.nextDouble() * 20) + 5;
        js.addArgument(String.valueOf(base + (int) (rnd.nextDouble() * base)));
        js.setAttribute("maxwalltime", "00:00:" + String.valueOf(base * 2));
        js.setAttribute("slots", "8");
        js.setAttribute("lowOverallocation", "6");
        js.setAttribute("nodeGranularity", "8");
        js.setAttribute("maxNodes", "8");
        js.setAttribute("remoteMonitorEnabled", "true");
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
        // th.setAutostart(true);
        th.submit(t);
        return t;
    }
    
    private static Random rnd;

    public static void main(String[] args) {
        try {
        	rnd = new Random();
        	rnd.setSeed(10L);
            long s = System.currentTimeMillis();
            Task[] ts = new Task[2048];
            for (int i = 0; i < ts.length; i++) {
                ts[i] = submitTask();
                if (i % 100 == 0) {
                    System.err.println(i + " submitted");
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
