//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 17, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.ServiceShutdownCommand;
import org.globus.cog.abstraction.coaster.service.local.LocalService;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.commands.Command.Callback;
import org.ietf.jgss.GSSCredential;

public class ServiceManager implements StatusListener {
    public static final Logger logger = Logger
            .getLogger(ServiceManager.class);

    public static final String BOOTSTRAP_SCRIPT = "bootstrap.sh";
    public static final String BOOTSTRAP_JAR = "coaster-bootstrap.jar";
    public static final String BOOTSTRAP_LIST = "coaster-bootstrap.list";

    public static final String TASK_ATTR_ID = "coaster:serviceid";

    private static ServiceManager defaultManager;

    public static synchronized ServiceManager getDefault() {
        if (defaultManager == null) {
            defaultManager = new ServiceManager();
        }
        return defaultManager;
    }

    private String bootstrapScript;
    private BootstrapService bootstrapService;
    private LocalService localService;
    private Map services;
    private Map credentials;
    private Set starting;
    private Map usageCount;
    private ServiceReaper serviceReaper;

    public ServiceManager() {
        services = new HashMap();
        credentials = new HashMap();
        starting = new HashSet();
        usageCount = new HashMap();
        serviceReaper = new ServiceReaper();
        Runtime.getRuntime().addShutdownHook(serviceReaper);
    }

    public String reserveService(Task task, TaskHandler bootHandler,
            String bootHandlerProvider) throws TaskSubmissionException {
        if (logger.isDebugEnabled()) {
            logger.debug("Reserving service for " + task);
        }
        try {
            Object service = getService(task);
            // beah. it's impossible to nicely abstract both concurrency
            // and normal program semantics
            String url = waitForStart(service);
            if (url == null) {
                url = startService(task, bootHandler, bootHandlerProvider,
                        service);
            }
            increaseUsageCount(service);
            return url;
        }
        catch (Exception e) {
            throw new TaskSubmissionException(
                    "Could not start coaster service", e);
        }
    }

    protected String waitForStart(Object service) throws InterruptedException {
        synchronized (services) {
            while (starting.contains(service)) {
                services.wait(100);
            }
            String url = (String) services.get(service);
            if (url == null) {
                starting.add(service);
            }
            return url;
        }
    }

    public void serviceIsActive(String id) {
        localService.heardOf(id);
    }

    protected String startService(Task task, TaskHandler bootHandler,
            String bootHandlerProvider, Object service) throws Exception {
        try {
            startLocalService();
            Task t = buildTask(task);
            setSecurityContext(t, task, bootHandlerProvider);
            t.addStatusListener(this);
            if (logger.isDebugEnabled()) {
                logger.debug("Starting coaster service on "
                        + task.getService(0) + ". Task is " + t);
            }
            bootHandler.submit(t);
            String url = localService.waitForRegistration(t, (String) t
                    .getAttribute(TASK_ATTR_ID));
            synchronized (services) {
                services.put(service, url);
                credentials.put(url, task.getService(0).getSecurityContext()
                        .getCredentials());
            }
            return url;
        }
        finally {
            synchronized (services) {
                starting.remove(service);
                services.notifyAll();
            }
        }
    }

    private void setSecurityContext(Task t, Task orig, String provider)
            throws InvalidProviderException, ProviderMethodException {
        t.getService(0).setSecurityContext(
                AbstractionFactory.newSecurityContext(provider));
    }

    public void statusChanged(StatusEvent event) {
        Task t = (Task) event.getSource();
        Status s = event.getStatus();
        if (s.isTerminal()) {
            if (logger.isInfoEnabled()) {
                logger.info("Service task " + t
                        + " terminated. Removing service.");
            }
            String url;
            synchronized (services) {
                Object service = getService(t);
                url = (String) services.remove(service);
                if (url == null) {
                    logger
                            .info("Service does not appear to be registered with this manager");
                }
                else {
                    credentials.remove(url);
                }
            }
            try {
                if (url != null) {
                    GSSCredential cred = (GSSCredential) t.getService(0)
                            .getSecurityContext().getCredentials();
                    KarajanChannel channel = ChannelManager.getManager()
                            .getExistingChannel(url, cred);
                    if (channel != null) {
                        channel.close();
                    }
                }
            }
            catch (Exception e) {
                logger.info("Failed to close channel", e);
            }
        }
    }
    private static final Integer ZERO = new Integer(0);

    protected void increaseUsageCount(Object service) {
        synchronized (usageCount) {
            Integer i = (Integer) usageCount.get(service);
            if (i == null) {
                i = ZERO;
            }
            usageCount.put(service, new Integer(i.intValue() + 1));
        }
    }

    protected Object getService(Task task) {
        return task.getService(0).getServiceContact().toString();
    }

    private Task buildTask(Task orig) throws TaskSubmissionException {
        try {
            Task t = new TaskImpl();
            t.setType(Task.JOB_SUBMISSION);
            JobSpecification js = new JobSpecificationImpl();
            js.setExecutable("/bin/bash");
            js.addArgument("-l");
            js.addArgument("-c");
            js.addArgument(loadBootstrapScript());
            js.addArgument(getBootstrapServiceURL());
            js.addArgument(getLocalServiceURL());
            js.addArgument(getMD5(BOOTSTRAP_JAR));
            js.addArgument(getMD5(BOOTSTRAP_LIST));
            js.setDelegation(Delegation.FULL_DELEGATION);
            String id = getRandomID();
            t.setAttribute(TASK_ATTR_ID, id);
            js.addArgument(id);
            js.addArgument(orig.getService(0).getServiceContact().getHost());
            js.setStdOutputLocation(FileLocation.MEMORY);
            js.setStdErrorLocation(FileLocation.MEMORY);
            t.setSpecification(js);
            ExecutionService s = new ExecutionServiceImpl();
            s.setServiceContact(orig.getService(0).getServiceContact());
            s.setJobManager("fork");
            t.setService(0, s);
            return t;
        }
        catch (Exception e) {
            throw new TaskSubmissionException(e);
        }
    }

    private synchronized String loadBootstrapScript()
            throws TaskSubmissionException {
        if (bootstrapScript != null) {
            return bootstrapScript;
        }
        URL u = ServiceManager.class.getClassLoader().getResource(
                BOOTSTRAP_SCRIPT);
        if (u == null) {
            throw new TaskSubmissionException(
                    "Could not find bootstrap script in classpath");
        }
        try {
            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(u
                    .openStream()));
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            bootstrapScript = sb.toString();
            return bootstrapScript;
        }
        catch (IOException e) {
            throw new TaskSubmissionException(
                    "Could not load bootsrap script", e);
        }
    }

    private String getBootstrapServiceURL() throws IOException {
        return startBootstrapService();
    }

    private String getMD5(String name) throws NoSuchAlgorithmException,
            IOException {
        File f = bootstrapService.getFile(name);
        return Digester.computeMD5(f);
    }

    private synchronized String startBootstrapService() throws IOException {
        if (bootstrapService == null) {
            bootstrapService = new BootstrapService();
            bootstrapService.start();
        }
        return bootstrapService.getURL();
    }

    private synchronized void startLocalService() throws IOException {
        if (localService == null) {
            localService = new LocalService();
            localService.start();
        }
    }

    private synchronized String getLocalServiceURL() throws IOException {
        startLocalService();
        return localService.getURL();
    }

    private String getRandomID() {
        int r;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            r = sr.nextInt();
        }
        catch (NoSuchAlgorithmException e) {
            r = (int) Math.random() * Integer.MAX_VALUE;
        }
        if (r < 0) {
            return '0' + String.valueOf(-r);
        }
        else {
            return '1' + String.valueOf(r);
        }
    }

    private class ServiceReaper extends Thread implements Callback {
        private int count;

        public ServiceReaper() {
            setName("Coaster service reaper");
        }

        public void run() {
            System.out.println("Cleaning up...");
            Iterator i = services.values().iterator();
            count = services.size();
            while (i.hasNext()) {
                String url = (String) i.next();
                Object cred = credentials.get(url);
                try {
                    System.out.println("Shutting down service at " + url);
                    KarajanChannel channel = ChannelManager.getManager()
                            .reserveChannel(url, (GSSCredential) cred);
                    System.out.println("Got channel " + channel);
                    ServiceShutdownCommand ssc = new ServiceShutdownCommand();
                    ssc.setReplyTimeout(10);
                    ssc.setMaxRetries(0);
                    ssc.executeAsync(channel, this);
                    ChannelManager.getManager().releaseChannel(channel);
                }
                catch (Exception e) {
                    logger.warn("Failed to shut down service " + url, e);
                }
            }
            synchronized (this) {
                while (count > 0) {
                    try {
                        wait(100);
                    }
                    catch (InterruptedException e) {
                        return;
                    }
                }
            }
            System.out.println(" Done");
        }

        public synchronized void errorReceived(Command cmd, String msg,
                Exception t) {
            System.out.print("-");
            count--;
            notifyAll();
        }

        public synchronized void replyReceived(Command cmd) {
            System.out.print("+");
            count--;
            notifyAll();
        }
    }
}
