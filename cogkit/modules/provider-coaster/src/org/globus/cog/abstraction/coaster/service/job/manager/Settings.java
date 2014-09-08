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
 * Created on Apr 23, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public class Settings {
    public static final Logger logger = Logger.getLogger(Settings.class);

    /**
       Coasters will only consider settings listed here.
       workersPerNode is only included for its error message
     */
    public static final String[] NAMES =
        new String[] { "slots", "jobsPerNode", "workersPerNode",
                       "nodeGranularity", "allocationStepSize",
                       "maxNodes", "lowOverallocation",
                       "highOverallocation",
                       "overallocationDecayFactor",
                       "spread", "reserve", "maxtime",
                       "remoteMonitorEnabled",
                       "internalHostname", "hookClass",
                       "workerManager", "workerLoggingLevel",
                       "workerLoggingDirectory",
                       "ldLibraryPath", "workerCopies",
                       "directory", "useHashBang",
                       "parallelism",
                       "coresPerNode",
                       "perfTraceWorker", "perfTraceInterval"};

    /**
     * The maximum number of blocks that can be active at one time
     */
    private int slots = 20;
    private int jobsPerNode = 1;

    /**
     * TODO: clarify what this does
     */
    private String coresPerNode = "1";

    /**
     * How many nodes to allocate at once
     */
    private int nodeGranularity = 1;

    /**
     * When there is a need to allocate new blocks, how many should be used for
     * current jobs versus how many should be kept for future jobs (0.0 - 1.0)
     */
    private double allocationStepSize = 0.1;

    /**
     * How long (timewise) the request should be based on the job walltime.
     * lowOverallocation is the factor for 1s jobs
     * highOverallocation is the factor for +Inf jobs.
     * Things in-between are derived using x * ((os - oe) / x + oe.
     *
     * For example, with oe = 100, a bunch of jobs of walltime 1 will generate
     * blocks about 100 long.
     */
    private double lowOverallocation = 10, highOverallocation = 1;

    private double overallocationDecayFactor = 1.0 / 1000.0;

    /**
     * How to spread the size of blocks being allocated. 0 means no spread (all
     * blocks allocated in one iteration have the same size), and 1.0 is maximum
     * spread (first block will have minimal size, and the last block will be
     * twice the median).
     */
    private double spread = 0.9;

    /**
     * Maximum idle time of a block
     */
    private int exponentialSpread = 0;

    private TimeInterval reserve = TimeInterval.fromSeconds(60);

    // this would cause bad things for jobsPerNode > 1024
    private int maxNodes = Integer.MAX_VALUE / 1024;

    private TimeInterval maxtime = TimeInterval.DAY.multiply(360);

    private final Set<URI> callbackURIs;

    private ServiceContact serviceContact;

    private String provider;

    private String jobManager;

    private SecurityContext securityContext;

    private boolean remoteMonitorEnabled;

	/**
	 * Adjusts the metric used for block sizes.
	 *
	 * Essentially when you pick a box, there is a choice of how you
	 * are going to pick the length vs. width of the box for the same
	 * volume.
	 *
	 * Though it's used a bit in reverse. A parallelism of 0 means
	 * that the size of a job will be its natural width * its height ^
	 * parallelism (the latter being 1). Since the height is always 1,
	 * then you can only fit 2 jobs in a volume 2 block, which has to
	 * have a width of 2.
	 *
	 * A parallelism of 1 means that the height is the actual
	 * walltime, which is realistic, but the parallelism will be
	 * whatever the block width happens to be. So basically it
	 * determines how much relative weight is given to the walltime
	 * vs. the number of CPUs needed.
	 */
    private double parallelism = 0.01;

    private TimeInterval maxWorkerIdleTime = TimeInterval.fromSeconds(120);

    private String hookClass;

    private Hook hook;

    private String workerManager = "block";

    private String workerLoggingLevel = "NONE";

    private String workerLoggingDirectory = "DEFAULT";

    private String workerLibraryPath = null;

    private String workerCopies = null;

    private String directory = null;

    private String useHashBang = null;

    private String perfTraceWorker = "false";

    private int perfTraceInterval = -1;

    private final Map<String, String> attributes;

    /**
     * A pass-through setting for SGE, parallel environment
    */
    private String pe;

    public Settings() {
        hook = new Hook();
        callbackURIs = new TreeSet<URI>();
        attributes = new HashMap<String, String>();
    }

    /**
       Formerly "slots": the maximum number of Coasters Blocks
     */
    public int getMaxBlocks() {
        return slots;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public int getJobsPerNode() {
        return jobsPerNode;
    }

    public void setJobsPerNode(int jobsPerNode) {
        this.jobsPerNode = jobsPerNode;
    }

    @Deprecated
    public int getWorkersPerNode() {
        return jobsPerNode;
    }

    @Deprecated
    public void setWorkersPerNode(int jobsPerNode) {
        this.jobsPerNode = jobsPerNode;
        logger.warn("site setting workersPerNode has been replaced " +
                    "with jobsPerNode!");
    }

    public int getNodeGranularity() {
        return nodeGranularity;
    }

    public void setNodeGranularity(int nodeGranularity) {
        this.nodeGranularity = nodeGranularity;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public double getAllocationStepSize() {
        return allocationStepSize;
    }

    public void setAllocationStepSize(double sz) {
        this.allocationStepSize = sz;
    }

    public double getLowOverallocation() {
        return lowOverallocation;
    }

    public void setLowOverallocation(double os) {
        this.lowOverallocation = os;
    }

    public double getHighOverallocation() {
        return highOverallocation;
    }

    public void setHighOverallocation(double oe) {
        this.highOverallocation = oe;
    }

    public double getOverallocationDecayFactor() {
        return overallocationDecayFactor;
    }

    public void setOverallocationDecayFactor(double overallocationDecayFactor) {
        this.overallocationDecayFactor = overallocationDecayFactor;
    }

    public double getSpread() {
        return spread;
    }

    public void setSpread(double spread) {
        this.spread = spread;
    }

    public int getExponentialSpread() {
        return exponentialSpread;
    }

    public void setExponentialSpread(int exponentialSpread) {
        this.exponentialSpread = exponentialSpread;
    }

    public TimeInterval getReserve() {
        return reserve;
    }

    public void setReserve(TimeInterval reserve) {
        this.reserve = reserve;
    }

    public TimeInterval getMaxtime() {
        return maxtime;
    }

    public void setMaxtime(String maxtime) {
        this.maxtime = TimeInterval.fromSeconds(WallTime.timeToSeconds(maxtime));
    }

    public String getWorkerManager() {
        return workerManager;
    }

    public void setWorkerManager(String workerManager) {
        this.workerManager = workerManager;
    }

    public String getInternalHostname() {
        return getCallbackURI().getHost();
    }

    public String getWorkerLoggingLevel() {
        return workerLoggingLevel;
    }

    public void setWorkerLoggingDirectory(String directory) {
        workerLoggingDirectory = directory;
    }

    public String getWorkerLoggingDirectory() {
        return workerLoggingDirectory;
    }

    public String getPerfTraceWorker() {
        return perfTraceWorker;
    }


    public void setPerfTraceWorker(String perfTraceWorker) {
        this.perfTraceWorker = perfTraceWorker;
    }


    public int getPerfTraceInterval() {
        return perfTraceInterval;
    }


    public void setPerfTraceInterval(int perfTraceInterval) {
        this.perfTraceInterval = perfTraceInterval;
    }


    /**
     * The following values are considered valid:
     * <dl>
     * <dt>"NONE"</dt><dd>disables worker logging completely (no files are created)</dd>
     * <dt>"ERROR"</dt><dd>only log errors</dd>
     * <dt>"WARN"</dt><dd>log errors and warnings</dd>
     * <dt>"INFO"</dt><dd>log errors, warnings, and info messages (this should still produce minimal output)</dd>
     * <dt>"DEBUG"</dt><dd>log errors, warnings, info messages and debugging messages. This typically results in
     * lots of output</dd>
     * <dt>"TRACE"</dt><dd>In addition to what "DEBUG" logs, also log detailed information such as network communication</dd>
     * </dl>
     */
    public void setWorkerLoggingLevel(String workerLoggingLevel) {
        if (workerLoggingLevel != null) {
            workerLoggingLevel =
                workerLoggingLevel.trim().toUpperCase();
        }
        this.workerLoggingLevel = workerLoggingLevel;
    }

    public void setInternalHostname(String internalHostname) {
        logger.debug("setInternalHostname: " + internalHostname);
        if (internalHostname != null) { // override automatically determined
            try {
                URI original = getCallbackURI();
                setCallbackURI(new URI(original.getScheme(), original.getUserInfo(),
                            internalHostname, original.getPort(), original.getPath(),
                            original.getQuery(), original.getFragment()));
                if (! original.toString().equals(getCallbackURI().toString())) {
                    logger.warn("original callback URI is " + original);
                    logger.warn("callback URI has been overridden to " + getCallbackURI());
                }
            }
            catch (URISyntaxException use) {
                throw new RuntimeException(use);
            }
            // TODO nasty exception in the line above
        }
    }

    public Collection<URI> getLocalContacts(int port) {
        Set<URI> l = new HashSet<URI>();
        try {
            Enumeration<NetworkInterface> e1 = NetworkInterface.getNetworkInterfaces();
            while (e1.hasMoreElements()) {
                NetworkInterface ni = e1.nextElement();
                Enumeration<InetAddress> e2 = ni.getInetAddresses();
                while (e2.hasMoreElements()) {
                    InetAddress addr = e2.nextElement();
                    if (addr instanceof Inet6Address)
                        continue;
                    if (!"127.0.0.1".equals(addr.getHostAddress())) {
                        l.add(new URI("http://" + addr.getHostAddress() + ":" + port));
                    }
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("Local contacts: " + l);
            }
            return l;
        }
        catch (SocketException e) {
            logger.warn("Could not get network interface addresses", e);
            return null;
        }
        catch (URISyntaxException e) {
            logger.warn("Could not build URI from local network interface addresses", e);
            return null;
        }
    }

    public URI getCallbackURI() {
        if (callbackURIs.isEmpty()) {
            return null;
        }
        else {
            return callbackURIs.iterator().next();
        }
    }

    public void setCallbackURI(URI callbackURI) {
        callbackURIs.clear();
        callbackURIs.add(callbackURI);
    }

    public void setCallbackURIs(Collection<URI> callbackURIs) {
        this.callbackURIs.clear();
        this.callbackURIs.addAll(callbackURIs);
    }

    public Collection<URI> getCallbackURIs() {
        return callbackURIs;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public ServiceContact getServiceContact() {
        return serviceContact;
    }

    public void setServiceContact(ServiceContact serviceContact) {
        this.serviceContact = serviceContact;
    }

    public String getJobManager() {
        return jobManager;
    }

    public void setJobManager(String jobManager) {
        this.jobManager = jobManager;
    }

    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public boolean getRemoteMonitorEnabled() {
        return remoteMonitorEnabled;
    }

    public boolean isRemoteMonitorEnabled() {
        return remoteMonitorEnabled;
    }

    public TimeInterval getMaxWorkerIdleTime() {
        return maxWorkerIdleTime;
    }

    public void setMaxWorkerIdleTime(TimeInterval maxWorkerIdleTime) {
        this.maxWorkerIdleTime = maxWorkerIdleTime;
    }

    public void setRemoteMonitorEnabled(boolean monitor) {
        this.remoteMonitorEnabled = monitor;
    }

    public double getParallelism() {
        return parallelism;
    }

    public void setParallelism(double parallelism) {
        this.parallelism = parallelism;
    }

    public String getCoresPerNode() {
        return coresPerNode;
    }

    public void setCoresPerNode(String coresPerNode) {
        this.coresPerNode=coresPerNode;
    }
    public String getHookClass() {
        return hookClass;
    }

    public Hook getHook() {
    	return hook;
    }

    public void setHookClass(String hookClass) {
        this.hookClass = hookClass;
        try {
            this.hook = (Hook) Class.forName(hookClass.trim()).newInstance();
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }

    public String getLdLibraryPath() {
        return workerLibraryPath;
    }

    /**
       Instructs the worker to set LD_LIBRARY_PATH to path
       in its and its children's environment
     */
    public void setLdLibraryPath(String path) {
        workerLibraryPath = path;
    }

    public String getWorkerCopies() {
        return workerCopies;
    }

    public void setWorkerCopies(String copies) {
        workerCopies = copies;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getUseHashBang() {
        return useHashBang;
    }

    public void setUseHashBang(String uhb) {
        this.useHashBang = uhb;
    }

    public void setAttribute(String name, String value) {
    	attributes.put(name, value);
    }

    public Collection<String> getAttributeNames() {
    	return attributes.keySet();
    }

    public String getAttribute(String name) {
    	return attributes.get(name);
    }

    public void set(String name, String value)
        throws IllegalArgumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting " + name + " to " + value);
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("Empty string Settings key "
                                            + "(value was \"" + value + "\"");
        }

        boolean complete = false;
        Method[] methods = getClass().getMethods();
        String setterName = "set" +
            Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase(setterName)) {
                    set(method, value);
                    complete = true;
                    break;
                }
            }
        }
        catch (InvocationTargetException e) {
            throw new IllegalArgumentException
                ("Cannot set: " + name + " to: " + value);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (!complete) {
        	setAttribute(name, value);
        }
    }

    void set(Method method, String value)
        throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = method.getParameterTypes()[0];
        Object[] args = null;
        if (clazz.equals(String.class)) {
            args = new Object[] { value };
        }
        else if (clazz.equals(int.class)) {
            args = new Object[] { Integer.valueOf(value) };
        }
        else if (clazz.equals(double.class)) {
            args = new Object[] { Double.valueOf(value) };
        }
        else if (clazz.equals(boolean.class)) {
            args = new Object[] { Boolean.valueOf(value) };
        }
        else if (clazz.equals(TimeInterval.class)) {
            args = new Object[]
                { TimeInterval.fromSeconds(Integer.parseInt(value)) };
        }
        else if (clazz.equals(ServiceContact.class)) {
            args = new Object[]
                { new ServiceContactImpl(value) };
        }
        else {
            throw new IllegalArgumentException
                ("Don't know how to set option with type " + clazz);
        }
        method.invoke(this, args);
    }

    private static final Object[] NO_ARGS = new Object[0];

    private Object get(String name) throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Method[] ms = getClass().getMethods();
        String getterName = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (int i = 0; i < ms.length; i++) {
            if (ms[i].getName().equals(getterName)) {
                return ms[i].invoke(this, NO_ARGS);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Settings {\n");
        for (int i = 0; i < NAMES.length; i++) {
            sb.append("\t");
            sb.append(NAMES[i]);
            sb.append(" = ");
            try {
                sb.append(String.valueOf(get(NAMES[i])));
            }
            catch (Exception e) {
                sb.append("<exception>");
                logger.warn(e);
            }
            sb.append('\n');
        }
        sb.append("\tattributes = " + attributes + "\n");
        sb.append("\tcallbackURIs = " + getCallbackURIs() + "\n");
        sb.append("}\n");
        return sb.toString();
    }
}
