//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 28, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;


import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;

public class BlockTask extends TaskImpl {
    private Block block;
    private Settings settings;

    public BlockTask(Block block) {
        this.block = block;
        this.settings = block.getAllocationProcessor().getSettings();
    }

    public void initialize() throws InvalidServiceContactException, InvalidProviderException,
            ProviderMethodException {
        setType(Task.JOB_SUBMISSION);
        JobSpecification spec = buildSpecification();
        setSpecification(spec);
        setAttribute(spec, "maxwalltime", WallTime.format((int) block.getWalltime().getSeconds()));
        setAttribute(spec, "queue", settings.getQueue());
        setAttribute(spec, "project", settings.getProject());
        setAttribute(spec, "count", String.valueOf(block.getWorkerCount() / settings.getWorkersPerNode()));
        setRequiredService(1);
        setService(0, buildService());
    }

    private JobSpecification buildSpecification() {
        JobSpecification js = new JobSpecificationImpl();
        js.setExecutable("/usr/bin/perl");
        js.addArgument(block.getAllocationProcessor().getScript().getAbsolutePath());
        js.addArgument(settings.getCallbackURI().toString());
        js.addArgument(block.getId());
        js.addArgument(String.valueOf(settings.getWorkersPerNode()));
        js.setStdOutputLocation(FileLocation.MEMORY);
        js.setStdErrorLocation(FileLocation.MEMORY);
        return js;
    }

    private ExecutionService buildService() throws InvalidServiceContactException,
            InvalidProviderException, ProviderMethodException {
        ExecutionService s = new ExecutionServiceImpl();
        s.setServiceContact(settings.getServiceContact());
        s.setProvider(settings.getProvider());
        s.setJobManager(settings.getJobManager());
        s.setSecurityContext(settings.getSecurityContext());
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
}
