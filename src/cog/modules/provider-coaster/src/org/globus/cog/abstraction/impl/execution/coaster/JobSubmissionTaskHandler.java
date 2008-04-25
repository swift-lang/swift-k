//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.coaster;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.DelegatedTaskHandler;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.commands.Command.Callback;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

public class JobSubmissionTaskHandler implements DelegatedTaskHandler,
        Callback {
    private static Logger logger = Logger
            .getLogger(JobSubmissionTaskHandler.class);

    private Task task, startServiceTask;
    private Map bootHandlers;
    private SubmitJobCommand jsc;
    private GSSCredential cred;
    private String jobid;
    private String url;
    private boolean cancel;
    private boolean autostart;

    public JobSubmissionTaskHandler() {
        this.autostart = true;
        bootHandlers = new HashMap();
    }

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        this.task = task;
        task.setStatus(Status.SUBMITTING);
        try {
            if (autostart) {
                url = ServiceManager.getDefault().reserveService(task,
                        getBootHandler(task));
                cred = getCredentials(task);
            }
            else {
                url = task.getService(0).getServiceContact().getContact();
            }
            KarajanChannel channel = CoasterChannelManager.getManager()
                    .reserveChannel(url, cred);
            jsc = new SubmitJobCommand(task);
            jsc.executeAsync(channel, this);
        }
        catch (Exception e) {
            throw new TaskSubmissionException("Could not submit job", e);
        }
    }

    private TaskHandler getBootHandler(Task t)
            throws InvalidServiceContactException, InvalidProviderException,
            ProviderMethodException, IllegalSpecException {
        String provider = getBootHandlerProvider(t);
        synchronized (bootHandlers) {
            TaskHandler th = (TaskHandler) bootHandlers.get(provider);
            if (th == null) {
                th = AbstractionFactory.newExecutionTaskHandler(provider);
                bootHandlers.put(provider, th);
            }
            return th;
        }
    }

    private String getBootHandlerProvider(Task t)
            throws InvalidServiceContactException, IllegalSpecException {
        String jm = getJobManager(t);
        if (jm == null) {
            throw new InvalidServiceContactException("Missing job manager");
        }
        String[] jmp = jm.split(":");
        if (jmp.length < 2) {
            throw new InvalidServiceContactException("Invalid job manager: "
                    + jm + ". Use <provider>:<remote-provider>[:<remote-job-manager>].");
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

    public void suspend() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public void resume() throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public synchronized void cancel() throws InvalidSecurityContextException,
            TaskSubmissionException {
        try {
            if (jobid != null) {
                KarajanChannel channel = CoasterChannelManager.getManager()
                        .reserveChannel(url, cred);
                CancelJobCommand cc = new CancelJobCommand(jobid);
                cc.execute(channel);
                cancel = false;
            }
            else {
                cancel = true;
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

    private GSSCredential getCredentials(Task task)
            throws InvalidSecurityContextException {
        SecurityContext sc = task.getService(0).getSecurityContext();
        if (sc == null) {
            GSSManager manager = ExtendedGSSManager.getInstance();
            try {
                return manager
                        .createCredential(GSSCredential.INITIATE_AND_ACCEPT);
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
        task.setStatus(s);
    }

    public void replyReceived(Command cmd) {
        if (cmd == jsc) {
            jobid = cmd.getInDataAsString(0);
            task.setStatus(Status.SUBMITTED);
            if (cancel) {
                try {
                    cancel();
                }
                catch (Exception e) {
                    logger.warn("Failed to cancel job " + jobid, e);
                }
            }
            jsc = null;
            if (logger.isInfoEnabled()) {
                logger.info("Submitted task " + task + ". Job id: " + jobid);
            }
            NotificationManager.getDefault().registerTask(jobid, task);
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
        js.setExecutable("/bin/echo");
        //js.addArgument("0");
        t.setSpecification(js);
        ExecutionService s = new ExecutionServiceImpl();
        // s.setServiceContact(new ServiceContactImpl("localhost"));
        //s.setServiceContact(new ServiceContactImpl("tp-grid1.ci.uchicago.edu"));
        s.setServiceContact(new ServiceContactImpl("tg-grid1.uc.teragrid.org"));
        // s.setServiceContact(new ServiceContactImpl("localhost:50013"));
        s.setProvider("coaster");
        //s.setJobManager("local:local");
        s.setJobManager("gt2:pbs");
        s.setSecurityContext(new SecurityContextImpl());
        t.setService(0, s);
        // JobSubmissionTaskHandler th = new JobSubmissionTaskHandler(
        // AbstractionFactory.newExecutionTaskHandler("local"));
        JobSubmissionTaskHandler th = new JobSubmissionTaskHandler();
        //th.setAutostart(true);
        th.submit(t);
        return t;
    }

    public static void main(String[] args) {
        try {
            long s = System.currentTimeMillis();
            Task[] ts = new Task[2];
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
            System.err.println("Total time: "
                    + (System.currentTimeMillis() - s));
            System.exit(0);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
