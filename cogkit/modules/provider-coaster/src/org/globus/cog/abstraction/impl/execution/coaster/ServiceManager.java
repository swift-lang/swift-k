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
 * Created on Jan 17, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.coaster.service.ServiceShutdownCommand;
import org.globus.cog.abstraction.coaster.service.local.LocalService;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.execution.coaster.bootstrap.Bootstrap;
import org.globus.cog.abstraction.impl.execution.coaster.bootstrap.Digester;
import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.channels.IrrecoverableException;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.Command.Callback;
import org.globus.common.CoGProperties;
import org.ietf.jgss.GSSCredential;

public class ServiceManager implements StatusListener {
    public static final Logger logger = Logger.getLogger(ServiceManager.class);

    public static final String BOOTSTRAP_SCRIPT = "bootstrap.sh";
    public static final String BOOTSTRAP_JAR = "coaster-bootstrap.jar";

    public static final String TASK_ATTR_ID = "coaster:serviceid";
    
    public static final String ATTR_USER_HOME_OVERRIDE = "userHomeOverride";
    
    /**
     * Maximum time to wait for services to acknowledge shutdown
     */
    public static final int REAPER_MAX_WAIT_TIME = 10 * 1000;

    private static ServiceManager defaultManager;

    public static synchronized ServiceManager getDefault() {
        if (defaultManager == null) {
            defaultManager = new ServiceManager();
        }
        return defaultManager;
    }

    public static final boolean LOCALJVM = false;
    
    public static final boolean LOCALJVM_WHEN_LOCAL = true;

    private BootstrapService bootstrapService;
    private LocalService localService;
    private Map<Object, String> services;
    private Map<String, Object> credentials;
    private Map<String, TaskHandler> bootHandlers;
    private Set<Object> starting;
    private Map<Object, Integer> usageCount;
    private ServiceReaper serviceReaper;

    public ServiceManager() {
        services = new HashMap<Object, String>();
        credentials = new HashMap<String, Object>();
        starting = new HashSet<Object>();
        usageCount = new HashMap<Object, Integer>();
        bootHandlers = new HashMap<String, TaskHandler>();
        serviceReaper = new ServiceReaper();
        Runtime.getRuntime().addShutdownHook(serviceReaper);
    }

    private TaskHandler getBootHandler(String provider) throws InvalidServiceContactException,
            InvalidProviderException, ProviderMethodException, IllegalSpecException {
        synchronized (bootHandlers) {
            TaskHandler th = bootHandlers.get(provider);
            if (th == null) {
                th = AbstractionFactory.newExecutionTaskHandler(provider);
                bootHandlers.put(provider, th);
            }
            return th;
        }
    }
    
    public String reserveService(Service service, String bootHandlerProvider) throws TaskSubmissionException {
        return reserveService(service, bootHandlerProvider, null);
    }
 
    public String reserveService(Service service, String bootHandlerProvider, String userHomeOverride) throws TaskSubmissionException {
        ServiceContact contact = service.getServiceContact();
        if (logger.isDebugEnabled()) {
            logger.debug("Reserving service " + contact);
        }
        try {
            // beah. it's impossible to nicely abstract both concurrency
            // and normal program semantics
            String url = waitForStart(contact);
            if (url == null) {
                url =
                        startService(service, getBootHandler(bootHandlerProvider),
                            bootHandlerProvider, userHomeOverride);
            }
            increaseUsageCount(contact);
            return url;
        }
        catch (Exception e) {
            throw new TaskSubmissionException("Could not start coaster service", e);
        }
    }

    public String reserveService(Task task, String bootHandlerProvider)
            throws TaskSubmissionException {
        String userHomeOverride = null;
        Service service = getService(task);
        if (task.getType() == Task.JOB_SUBMISSION) {
            userHomeOverride = (String) service.getAttribute(ATTR_USER_HOME_OVERRIDE);
        }
        return reserveService(service, bootHandlerProvider, userHomeOverride);
    }
    
    private Service getService(Task task) {
        return task.getService(0);
    }

    protected String waitForStart(Object service) throws InterruptedException {
        synchronized (services) {
            while (starting.contains(service)) {
                services.wait(100);
            }
            String url = services.get(service);
            if (url == null) {
                starting.add(service);
            }
            return url;
        }
    }

    public void serviceIsActive(String id) {
        localService.heardOf(id);
    }

    // private static final String[] STRING_ARRAY = new String[0];

    protected String startService(final Service service, TaskHandler bootHandler, 
            String bootHandlerProvider, String userHomeOverride) throws Exception {
        ServiceContact contact = service.getServiceContact();
        SecurityContext sc = service.getSecurityContext();
        try {
            startLocalService();
             final Task t = buildTask(service, userHomeOverride);
            
            t.addStatusListener(this);
            if (logger.isDebugEnabled()) {
                logger.debug("Starting coaster service on " + contact + ". Task is " + t);
            }
            
            boolean ssh = "ssh".equalsIgnoreCase(bootHandlerProvider) || 
                "ssh-cl".equalsIgnoreCase(bootHandlerProvider);
            boolean local = "local".equals(bootHandlerProvider);
            
            if (ssh) {
                setupGSIProxy();
            }
            if (!LOCALJVM_WHEN_LOCAL && local) {
                // this is here for testing purposes
                setupGSIProxy();
                JobSpecification spec = (JobSpecification) t.getSpecification();
                spec.addEnvironmentVariable("X509_USER_PROXY", System.getProperty("X509_USER_PROXY"));
                spec.addEnvironmentVariable("X509_CERT_DIR", System.getProperty("X509_CERT_DIR"));
            }
            
            setSecurityContext(t, sc, bootHandlerProvider);
            
            if (LOCALJVM || (LOCALJVM_WHEN_LOCAL && local)) {
                final String ls = getLocalServiceURL();
                final String id = "l" + getRandomID();
                t.setAttribute(TASK_ATTR_ID, id);
                new Thread(new Runnable() {
                    public void run() {
                        CoasterService.main(new String[] { ls, id, "-local" });
                    }
                }).start();
            }
            else {
                bootHandler.submit(t);
            }
            String url = localService.waitForRegistration(t, (String) t.getAttribute(TASK_ATTR_ID));
            synchronized (services) {
                services.put(contact, url);
                if (sc != null) {
                    credentials.put(url, sc.getCredentials());
                }
            }
            return url;
        }
        finally {
            synchronized (services) {
                starting.remove(contact);
                services.notifyAll();
            }
        }
    }

    private void setupGSIProxy() throws IOException, GeneralSecurityException {
        if (!checkStandardProxy()) {
            /*
             *  only do the automatic CA if a standard proxy file does not exist
             *  to allow using things like GridFTP from the coaster service through
             *  delegation (which won't work with the auto-generated proxy).
             */
            logger.info("No standard proxy found. Using AutoCA.");
            AutoCA.Info result = AutoCA.getInstance().createProxy();
            System.setProperty("X509_USER_PROXY", result.proxyPath);
            System.setProperty("X509_CERT_DIR", result.caCertPath);
        }
        else {
            logger.info("Standard proxy file found. Disabling AutoCA.");
        }
    }

    private boolean checkStandardProxy() {
        File proxy = new File(CoGProperties.getDefault().getProxyFile());
        return proxy.exists();
    }

    private void setSecurityContext(Task t, SecurityContext sc, String provider)
            throws InvalidProviderException, ProviderMethodException {
        t.getService(0).setSecurityContext(AbstractionFactory.getSecurityContext(provider, t.getService(0).getServiceContact()));
    }

    public void statusChanged(StatusEvent event) {
        Task t = (Task) event.getSource();
        Status s = event.getStatus();
        if (s.isTerminal()) {
            if (logger.isInfoEnabled()) {
                logger.info("Service task " + t + " terminated. Removing service.");
            }
            String url;
            ServiceContact contact = getContact(t);
            synchronized (services) {
                url = services.remove(contact);
                if (url == null) {
                    logger.info("Service does not appear to be registered with this manager");
                }
                else {
                    credentials.remove(url);
                }
            }
            String msg =
                    "Coaster service ended. Reason: " + s.getMessage() + "\n\tstdout: "
                            + t.getStdOutput() + "\n\tstderr: " + t.getStdError();
            if (logger.isInfoEnabled()) {
                logger.info(msg);
            }
            NotificationManager.getDefault().serviceTaskEnded(contact, msg);
            try {
                if (url != null) {
                    GSSCredential cred =
                            (GSSCredential) t.getService(0).getSecurityContext().getCredentials();
                    CoasterChannel channel =
                            ChannelManager.getManager().getExistingChannel(url, cred);
                    if (channel != null) {
                        channel.getChannelContext().notifyRegisteredCommandsAndHandlers(
                            new IrrecoverableException(msg));
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
            Integer i = usageCount.get(service);
            if (i == null) {
                i = ZERO;
            }
            usageCount.put(service, i + 1);
        }
    }

    protected ServiceContact getContact(Task task) {
        return task.getService(0).getServiceContact();
    }

    protected SecurityContext getSecurityContext(Task task) {
        return task.getService(0).getSecurityContext();
    }

    private Task buildTask(Service service, String userHomeOverride) throws TaskSubmissionException {
        try {
            Task t = new TaskImpl();
            t.setType(Task.JOB_SUBMISSION);
            JobSpecification js = new JobSpecificationImpl();
            js.setExecutable("/bin/bash");
            js.addArgument("-l");
            js.addArgument("-c");
            String id = getRandomID();
            t.setAttribute(TASK_ATTR_ID, id);
            js.addArgument(loadBootstrapScript(new String[] { getBootstrapServiceURL(),
                    getLocalServiceURL(), getMD5(BOOTSTRAP_JAR), getMD5(Bootstrap.BOOTSTRAP_LIST),
                    id, service.getServiceContact().getHost(), 
                    userHomeOverride }));
            js.setDelegation(Delegation.FULL_DELEGATION);
            js.setStdOutputLocation(FileLocation.MEMORY);
            js.setStdErrorLocation(FileLocation.MEMORY);
           
            t.setSpecification(js);
            ExecutionService s = new ExecutionServiceImpl();
            s.setServiceContact(service.getServiceContact());
            s.setJobManager("fork");
            t.setService(0, s);
            return t;
        }
        catch (Exception e) {
            throw new TaskSubmissionException(e);
        }
    }

    private synchronized String loadBootstrapScript(String[] args) throws TaskSubmissionException {
        URL u = ServiceManager.class.getClassLoader().getResource(BOOTSTRAP_SCRIPT);
        if (u == null) {
            throw new TaskSubmissionException("Could not find bootstrap script in classpath");
        }
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("echo -e \"");
            BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream()));
            String line = br.readLine();
            while (line != null) {
                escape(sb, line.trim(), args);
                sb.append("\\x0a");
                line = br.readLine();
            }
            sb.append("\"|/bin/bash");
            return sb.toString();
        }
        catch (IOException e) {
            throw new TaskSubmissionException("Could not load bootstrap script", e);
        }
    }

    private void escape(StringBuffer sb, String line, String[] args) {
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            switch (c) {
                case '\'':
                    sb.append("\\x27");
                    break;
                case '`':
                    sb.append("\\x60");
                    break;
                case '"':
                    sb.append("\\x22");
                    break;
                case '<':
                    sb.append("\\x3c");
                    break;
                case '>':
                    sb.append("\\x3e");
                    break;
                case '$':
                    if (i + 1 < line.length()) {
                        int n = line.charAt(i + 1) - '0';
                        if (n >= 1 && n <= 9 && n <= args.length && args[n - 1] != null) {
                            i++;
                            sb.append('"');
                            sb.append(args[n - 1]);
                            sb.append('"');
                            // a hack to prevent blind substitution (e.g. inside
                            // a function).
                            args[n - 1] = null;
                            continue;
                        }
                    }
                    sb.append("\\x24");
                    break;
                default:
                    sb.append(c);
            }
        }
    }

    private String getBootstrapServiceURL() throws IOException {
        return startBootstrapService();
    }

    private String getMD5(String name) throws NoSuchAlgorithmException, IOException {
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
            try {
                localService = new LocalService();
            }
            catch (Exception e) {
                throw new IOException(e);
            }
            localService.start();
        }
    }

    private synchronized String getLocalServiceURL() throws IOException {
        startLocalService();
        return localService.getURL();
    }
    
    public LocalService getLocalService() throws IOException {
    	startLocalService();
    	return localService;
    }

    private String getRandomID() {
        int r;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            r = sr.nextInt();
        }
        catch (NoSuchAlgorithmException e) {
            r = (int) (Math.random() * Integer.MAX_VALUE);
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
            logger.info("Cleaning up...");
            count = services.size();
            int waited = 0;
            for (String url : services.values()) {
                Object cred = credentials.get(url);
                try {
                    logger.info("Shutting down service at " + url);
                    CoasterChannel channel =
                            ChannelManager.getManager().reserveChannel(url, (GSSCredential) cred);
                    logger.debug("Got channel " + channel);
                    ServiceShutdownCommand ssc = new ServiceShutdownCommand();
                    ssc.setMaxRetries(0);
                    ssc.executeAsync(channel, this);
                    ChannelManager.getManager().releaseChannel(channel);
                }
                catch (Exception e) {
                    logger.warn("Failed to shut down service " + url, e);
                }
            }
            synchronized (this) {
                while (count > 0 && waited < REAPER_MAX_WAIT_TIME) {
                    try {
                        wait(100);
                        waited += 100;
                    }
                    catch (InterruptedException e) {
                        return;
                    }
                }
            }
            logger.debug(" Done");
        }

        public synchronized void errorReceived(Command cmd, String msg, Exception t) {
            logger.warn("error " + msg);
            count--;
            notifyAll();
        }

        public synchronized void replyReceived(Command cmd) {
            // System.out.print("+");
            count--;
            notifyAll();
        }
    }
}
