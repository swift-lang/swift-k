// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridant.tasks;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.GenericTaskHandler;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;

public class GridExecute extends Task {
    private String name = null;
    private String serviceContact = null;
    private String provider = null;

    private String executable = null;
    private String directory = null;
    private String arguments = null;
    private String stdoutput = null;
    private String stderror = null;
    private String stdinput = null;
    private boolean redirect = true;
    private boolean localExecutable = false;
    private boolean batch = false;
    private Hashtable attributes = null;
    private long sleepTime = 2000;

    public void execute() throws BuildException {
        if (this.provider == null) {
            throw new BuildException("Missing \"provider\" attribute");
        }
        if (this.executable == null) {
            throw new BuildException("Missing \"executable\" attribute");
        }

        org.globus.cog.abstraction.interfaces.Task task = new TaskImpl();
        task.setProvider(this.provider.toLowerCase());
        task.setType(org.globus.cog.abstraction.interfaces.Task.JOB_SUBMISSION);
        if (this.name != null) {
            task.setName(this.name);
        }

        JobSpecification spec = new JobSpecificationImpl();
        spec.setExecutable(this.executable);

        if (this.directory != null) {
            spec.setDirectory(this.directory);
        }

        if (this.arguments != null) {
            spec.setArguments(this.arguments);
        }

        if (this.stdoutput != null) {
            spec.setStdOutput(this.stdoutput);
        }

        if (this.stderror != null) {
            spec.setStdError(this.stderror);
        }

        if (this.stdinput != null) {
            spec.setStdInput(this.stdinput);
        }

        spec.setBatchJob(this.batch);
        spec.setRedirected(this.redirect);
        spec.setLocalExecutable(this.localExecutable);

        if (this.attributes != null) {
            getAttributes(spec);
        }

        task.setSpecification(spec);

        Service service = new ServiceImpl(Service.JOB_SUBMISSION);
        service.setProvider(this.provider.toLowerCase());

        SecurityContext securityContext;
        try {
            securityContext = AbstractionFactory.newSecurityContext(this.provider
                    .toLowerCase());
        } catch (InvalidProviderException e1) {

            throw new BuildException("Invalid provider", e1);
        } catch (ProviderMethodException e1) {

            throw new BuildException("Invalid provider method", e1);
        }
        securityContext.setCredentials(null);
        service.setSecurityContext(securityContext);

        ServiceContact sc = new ServiceContactImpl(this.serviceContact);
        service.setServiceContact(sc);

        task.addService(service);

        GenericTaskHandler handler = new GenericTaskHandler();

        try {
            handler.submit(task);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException("Grid task failed", e);
        }

        while (!(task.isCompleted() || task.isFailed())) {
            try {
                // Need to Sleep and wait for the Job to complete
                Thread.sleep(getSleepTime());
            } catch (Exception e) {
                log("Thread unable to sleep: not a critical problem");
            }
        }
        Status status = task.getStatus();
        log("grid-execute -- " + status.getStatusString());
        log("Output: \n" + task.getStdOutput());
    }

    private void getAttributes(JobSpecification spec) {
        Enumeration enum = this.attributes.keys();
        while (enum.hasMoreElements()) {
            String name = (String) enum.nextElement();
            String value = (String) this.attributes.get(name);
            spec.setAttribute(name, value);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setServiceContact(String serviceContact) {
        this.serviceContact = serviceContact;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public void setStdoutput(String stdoutput) {
        this.stdoutput = stdoutput;
    }

    public void setStderror(String stderror) {
        this.stderror = stderror;
    }

    public void setStdinput(String stdinput) {
        this.stdinput = stdinput;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    public void setLocalexecutable(boolean localExecutable) {
        this.localExecutable = localExecutable;
    }

    public void setAttribute(String attributeString) {
        if (this.attributes == null) {
            this.attributes = new Hashtable();
        }
        StringTokenizer st = new StringTokenizer(attributeString, ",");
        String name = st.nextToken();
        String value = st.nextToken();
        this.attributes.put(name, value);
    }

    public long getSleepTime() {
        return this.sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }
}