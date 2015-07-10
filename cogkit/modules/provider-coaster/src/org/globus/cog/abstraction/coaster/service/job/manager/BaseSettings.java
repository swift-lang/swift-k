//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 6, 2015
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public class BaseSettings extends AbstractSettings {
    public static final Logger logger = Logger.getLogger(BaseSettings.class);
    
    public static final String[] NAMES =
        new String[] { "internalHostname", "hookClass",
                       "workerManager", "workerLoggingLevel",
                       "workerLoggingDirectory",
                       "ldLibraryPath", "reserve", "coresPerNode", "jobsPerNode", "workersPerNode",
                       "useHashBang", "perfTraceWorker", "perfTraceInterval",
                       "workerCopies",
                       "directory"};

    /**
     * TODO: clarify what this does
     */
    private String coresPerNode = "1";
    
    private int jobsPerNode = 1;
    
    private final Set<URI> callbackURIs;

    private ServiceContact serviceContact;
    
    private String provider;

    private String jobManager;

    private SecurityContext securityContext;
    
    private TimeInterval reserve = TimeInterval.fromSeconds(60);

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

    
    public BaseSettings() {
        callbackURIs = new TreeSet<URI>();
        hook = new Hook();
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
    
    public TimeInterval getReserve() {
        return reserve;
    }

    public void setReserve(TimeInterval reserve) {
        this.reserve = reserve;
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
    
    
    public String getUseHashBang() {
        return useHashBang;
    }

    public void setUseHashBang(String uhb) {
        this.useHashBang = uhb;
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
        
    public String getCoresPerNode() {
        return coresPerNode;
    }

    public void setCoresPerNode(String coresPerNode) {
        this.coresPerNode = coresPerNode;
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
}
