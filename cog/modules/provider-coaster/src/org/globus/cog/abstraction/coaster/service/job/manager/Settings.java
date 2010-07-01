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
import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public class Settings {
    public static final Logger logger = Logger.getLogger(Settings.class);

    public static final String[] NAMES =
            new String[] { "slots", "workersPerNode", "nodeGranularity", "allocationStepSize",
                    "maxNodes", "lowOverallocation", "highOverallocation",
                    "overallocationDecayFactor", "spread", "reserve", "maxtime", "project",
                    "queue", "remoteMonitorEnabled", "kernelprofile", "alcfbgpnat", 
                    "internalHostname", "hookClass", "workerManager" };

    /**
     * The maximum number of blocks that can be active at one time
     */
    private int slots = 20;
    private int workersPerNode = 1;
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
     * How long (timewise) the request should be based on the job walltime. os
     * is a factor for 1 jobs, and oe is a factor for +Inf jobs. Things
     * in-between are derived using x * ((os - oe) / x + oe.
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

    // this would cause bad things for workersPerNode > 1024
    private int maxNodes = Integer.MAX_VALUE / 1024;

    private int maxtime = Integer.MAX_VALUE;

    private final Set<URI> callbackURIs;

    private ServiceContact serviceContact;

    private String provider;

    private String jobManager;

    private SecurityContext securityContext;

    private String project;

    private String queue;

    private String kernelprofile;

    private boolean alcfbgpnat;

    private boolean remoteMonitorEnabled;

    private double parallelism = 0.01;

    private TimeInterval maxWorkerIdleTime = TimeInterval.fromSeconds(120);
    
    private String hookClass;
    
    private Hook hook;
    
    private String workerManager = "block";
    
    public Settings() {
        hook = new Hook();
        callbackURIs = new TreeSet<URI>();
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public int getWorkersPerNode() {
        return workersPerNode;
    }

    public void setWorkersPerNode(int workersPerNode) {
        this.workersPerNode = workersPerNode;
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

    public int getMaxtime() {
        return maxtime;
    }

    public void setMaxtime(int maxtime) {
        this.maxtime = maxtime;
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
        List<URI> l = new ArrayList<URI>();
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

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
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

    public String getKernelprofile() {
        return kernelprofile;
    }

    public void setKernelprofile(String kernelprofile) {
        this.kernelprofile = kernelprofile;
    }

    public boolean getAlcfbgpnat() {
        return alcfbgpnat;
    }

    public void setAlcfbgpnat(boolean alcfbgpnat) {
        this.alcfbgpnat = alcfbgpnat;
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

    public void set(String name, String value) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting " + name + " to " + value);
        }
        Method[] ms = getClass().getMethods();
        String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (int i = 0; i < ms.length; i++) {
            if (ms[i].getName().equals(setterName)) {
                if (ms[i].getParameterTypes()[0].equals(String.class)) {
                    ms[i].invoke(this, new Object[] { value });
                }
                else if (ms[i].getParameterTypes()[0].equals(int.class)) {
                    ms[i].invoke(this, new Object[] { Integer.valueOf(value) });
                }
                else if (ms[i].getParameterTypes()[0].equals(double.class)) {
                    ms[i].invoke(this, new Object[] { Double.valueOf(value) });
                }
                else if (ms[i].getParameterTypes()[0].equals(boolean.class)) {
                    ms[i].invoke(this, new Object[] { Boolean.valueOf(value) });
                }
                else if (ms[i].getParameterTypes()[0].equals(TimeInterval.class)) {
                    ms[i].invoke(this,
                        new Object[] { TimeInterval.fromSeconds(Integer.parseInt(value)) });
                }
                else {
                    throw new IllegalArgumentException("Don't know how to set option with type "
                            + ms[i].getParameterTypes()[0]);
                }
                return;
            }
        }
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
        sb.append("}\n");
        return sb.toString();
    }
}
