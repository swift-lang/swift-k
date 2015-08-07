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
 * Created on Apr 28, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;


import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.CleanUpSetImpl;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StagingSetEntryImpl;
import org.globus.cog.abstraction.impl.common.StagingSetImpl;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.execution.coaster.bootstrap.Bootstrap;
import org.globus.cog.abstraction.interfaces.CleanUpSet;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.StagingSet;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.abstraction.interfaces.TaskHandlerCapabilities;
import org.globus.cog.abstraction.interfaces.TaskHandlerCapabilities.Key;

public class BlockTask extends TaskImpl {
    public static final Logger logger = Logger.getLogger(BlockTask.class);

    private final Block block;
    private final BaseSettings settings;
    private final BlockTaskSubmitter submitter;

    public BlockTask(Block block, BlockTaskSubmitter submitter) {
        this.block = block;
        this.settings = block.getAllocationProcessor().getSettings();
        this.submitter = submitter;
    }

    public void initialize() throws InvalidProviderException, ProviderMethodException {
        setType(Task.JOB_SUBMISSION);
        TaskHandler handler = submitter.getHandler(settings.getProvider());
        JobSpecification spec = buildSpecification(handler.getCapabilities());
        setSpecification(spec);
        setName("B" + block.getId());
        setAttribute(spec, "maxwalltime", WallTime.format((int) block.getWalltime().getSeconds()));
        setAttribute(spec, "jobsPerNode", settings.getJobsPerNode());
        setAttribute(spec, "coresPerNode", settings.getCoresPerNode());
        
        int count = block.getWorkerCount() / settings.getJobsPerNode();
        
        setAttribute(spec, "jobType", "multiple");

        // Here, count means number of worker script invocations
        setAttribute(spec, "count", String.valueOf(count));
        setAttribute(spec, "hostCount", String.valueOf(count));
        for (String name : settings.getAttributeNames()) {
        	setAttribute(spec, name, settings.getAttribute(name));
        }
        
        String libraryPath = settings.getLdLibraryPath();
        if (libraryPath != null)
            spec.addEnvironmentVariable("LD_LIBRARY_PATH",
                                        libraryPath);
        // TODO: What is this?
        String workerCopies = settings.getWorkerCopies();
        if (workerCopies != null) {
            String workerCopiesFixed =
              workerCopies.trim()
              .replaceAll("\n", ",")
              .replaceAll(" ", "");
            spec.addEnvironmentVariable("WORKER_COPIES",
                                        workerCopiesFixed);
        }
        spec.addEnvironmentVariable("WORKER_LOGGING_LEVEL", settings.getWorkerLoggingLevel());
        if (logger.isTraceEnabled()) {
            logger.trace("Worker logging level: " + settings.getWorkerLoggingLevel());
        }
        setRequiredService(1);
        setService(0, buildService());
        if (logger.isDebugEnabled()) {
        	logger.debug("Block task spec: " + spec);
        	logger.debug("Block Task Service: " + getService(0));
        }
    }

    private JobSpecification buildSpecification(TaskHandlerCapabilities cap) {
        JobSpecification js = new JobSpecificationImpl();
        File script = block.getAllocationProcessor().getScript();
        
        boolean staging = cap.supportsAnyOf(Key.FULL_FILE_STAGING, 
            Key.SIMPLE_FILE_STAGING, Key.JOB_DIR_STAGING);
        
        String scriptArg;
        if (staging) {
            scriptArg = script.getName();
            addStagingSpec(js, script, scriptArg);
        }
        else {
            scriptArg = script.getAbsolutePath();
        }
        
        String os = settings.getAttribute("OS");
        
        boolean isWindows = os != null && os.toLowerCase().contains("win");
        String executable;
        
        if (isWindows) {
        	executable = "perl";
        }
        else {
        	executable = "/usr/bin/perl";
        }
        
        if ("true".equals(settings.getUseHashBang())) {
            if (!"false".equals(settings.getPerfTraceWorker())) {
                js.setExecutable("strace");
                js.addArgument("-T");
                js.addArgument("-f");
                js.addArgument("-tt");
                
                js.addArgument("-o");
                js.addArgument(settings.getWorkerLoggingDirectory() + "/block-" + block.getId() + ".perf");
                
                js.addArgument("-e");
                js.addArgument("trace=" + settings.getPerfTraceWorker());
                
                js.addArgument(scriptArg);
            }
            else {
                js.setExecutable(scriptArg);
            }
        }
        else {
            if (!"false".equals(settings.getPerfTraceWorker())) {
                js.setExecutable("strace");
                js.addArgument("-T");
                js.addArgument("-f");
                js.addArgument("-tt");
                
                js.addArgument("-o");
                js.addArgument(settings.getWorkerLoggingDirectory() + "/block-" + block.getId() + ".perf");
                
                js.addArgument("-e");
                js.addArgument("trace=" + settings.getPerfTraceWorker());
                
                js.addArgument(executable);
            }
            else {
                js.setExecutable(executable);
            }
            js.addArgument(scriptArg);
        }
        
        // Cobalt on Intrepid, if no directory is specified, assumes $CWD for the
        // job directory.
        // If $CWD happens to be /scratch/something it has a filter in place
        // that rejects the job with the warning that /scratch/something is not accessible
        // on the worker node. And we don't care about the $CWD for the worker.
        
        // The problem re-surfaced on beagle, where PBS picks the head node ~/
        // as directory if not otherwise specified. This leads to a failure.
        // Since this choice of directory doesn't matter for the worker, one can
        // always pick "/", which should be valid on all machines.
        // Some care needs to be taken when staging is involved, since
        // the worker script needs to be staged to a writeable directory
                
        //if (settings.getProvider().equals("cobalt") && settings.getDirectory() == null) {
        if (!staging) {
            js.setDirectory("/");
        }
        else {
            js.setDirectory(settings.getDirectory());
        }
    
        js.addArgument(join(settings.getCallbackURIs(), ","));
        js.addArgument(block.getId());
    
        if (settings.getWorkerLoggingLevel().equals("NONE")) {
          js.addArgument("NOLOGGING");
        }
        else {
        	String logDir = settings.getWorkerLoggingDirectory();
        	if (logDir.equals("DEFAULT")) {
        		js.addArgument(Bootstrap.LOG_DIR.getAbsolutePath());
        	}
        	else {
        		js.addArgument(logDir);
        	}
        }
    
        // logger.debug("arguments: " + js.getArguments());
    
        js.setStdOutputLocation(FileLocation.MEMORY);
        js.setStdErrorLocation(FileLocation.MEMORY);
    
        return js;
    }

    private void addStagingSpec(JobSpecification js, File script, String scriptArg) {
        StagingSet ss = new StagingSetImpl();
        ss.add(new StagingSetEntryImpl(script.getAbsolutePath(), scriptArg));
        js.setStageIn(ss);
        
        CleanUpSet cs = new CleanUpSetImpl();
        cs.add(scriptArg);
        js.setCleanUpSet(cs);
    }

    private String join(Collection<?> c, String sep) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> i = c.iterator();
        while (i.hasNext()) {
            sb.append(i.next().toString());
            if (i.hasNext()) {
                sb.append(sep);
            }
        }
        return sb.toString();
    }

    private ExecutionService buildService() {
        ExecutionService s = new ExecutionServiceImpl();
        ServiceContact serviceContact = settings.getServiceContact();
        if (serviceContact != null) {
            s.setServiceContact(serviceContact);
        }
        s.setProvider(settings.getProvider());
        String jm = settings.getJobManager();
        if (jm != null) {
            s.setJobManager(jm);
        }
        SecurityContext sc = settings.getSecurityContext();
        if (sc != null) {
            s.setSecurityContext(sc);
        }
        return s;
    }

    public void setAttribute(JobSpecification spec, String name, Object value) {
        if (value != null) {
            spec.setAttribute(name, value);
        }
    }

    public void setAttribute(String name, int value) {
        super.setAttribute(name, String.valueOf(value));
    }

    public void submit() {
        submitter.submit(block);
    }
}
