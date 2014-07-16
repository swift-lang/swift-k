// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.examples.invocation;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.common.task.WSInvocationSpecificationImpl;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.abstraction.interfaces.WSInvocationSpecification;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

/*
 * This clas serves as an example demonstrating the pattern to do remote Web
 * service invocation with the abstractions framework. It also forms the basis
 * of the cog-ws-invoke command.
 */
public class ServiceInvocation implements StatusListener {
    static Logger logger = Logger.getLogger(ServiceInvocation.class.getName());

    private String method = null;

    private Task task = null;

    private String serviceContact = null;

    private String provider = null;

    private String arguments = null;

    private String attributes = null;

    private boolean commandLine = false;

    public void prepareTask() throws Exception {
        /*
         * Create a new job submission task with the given task name.
         */
        this.task = new TaskImpl(this.method, Task.WS_INVOCATION);
        logger.debug("Task Identity: " + this.task.getIdentity().toString());

        /*
         * Generate a new WSInvocationSpecification with all the given
         * attributes.
         */
        WSInvocationSpecification spec = new WSInvocationSpecificationImpl();

        spec.setMethod(this.method);

        if (arguments != null) {
            spec.setArguments(arguments);
        }

        /*
         * All additional attributes that are not available as an API for the
         * WSInvocationSpecification interface can be provided as a task
         * attribute.
         */
        if (this.attributes != null) {
            setAttributes(spec);
        }
        this.task.setSpecification(spec);

        /*
         * Create an invocation service for this task.
         */
        Service service = new ServiceImpl(Service.WS_INVOCATION);
        service.setProvider(this.provider.toLowerCase());

        SecurityContext securityContext = AbstractionFactory
                .getSecurityContext(provider, new ServiceContactImpl(serviceContact));
        securityContext.setCredentials(null);
        service.setSecurityContext(securityContext);

        ServiceContact sc = new ServiceContactImpl(this.serviceContact);
        service.setServiceContact(sc);

        this.task.addService(service);

        /*
         * Add a task listerner for this task. This allows the task to be
         * executed asynchronously. The client can continue with other
         * activities and gets asynchronously notified every time the status of
         * the task changes.
         */
        this.task.addStatusListener(this);
    }

    public Task getExecutionTask() {
        return this.task;
    }

    private void submitTask() throws Exception {
        TaskHandler handler = AbstractionFactory
                .newExecutionTaskHandler(provider);
        try {
            handler.submit(this.task);
        } catch (InvalidSecurityContextException ise) {
            System.out.println("Security Exception: " + ise.getMessage());
            logger.debug("Stack trace: ", ise);
            System.exit(1);
        } catch (TaskSubmissionException tse) {
            System.out.println("Submission Exception: " + tse.getMessage());
            logger.debug("Stack trace: ", tse);
            System.exit(1);
        } catch (IllegalSpecException ispe) {
            System.out.println("Specification Exception: " + ispe.getMessage());
            logger.debug("Stack trace: ", ispe);
            System.exit(1);
        } catch (InvalidServiceContactException isce) {
            System.out.println("Service Contact Exception");
            logger.debug("Stack trace: ", isce);
            System.exit(1);
        }
        //wait
        while (true) {
            Thread.sleep(1000);
        }
    }

    private void setAttributes(WSInvocationSpecification spec) {
        String att = getAttributes();
        StringTokenizer st = new StringTokenizer(att, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() > 0) {
                StringTokenizer st2 = new StringTokenizer(token, "=");
                while (st2.hasMoreTokens()) {
                    String name = st2.nextToken().trim();
                    String value = st2.nextToken().trim();
                    spec.setAttribute(name, value);
                }
            }
        }
    }

    public void statusChanged(StatusEvent event) {
        Status status = event.getStatus();
        logger.debug("Status changed to " + status.getStatusString());
        if (status.getStatusCode() == Status.FAILED) {
            if (event.getStatus().getMessage() != null) {
                System.out.println("Job failed: "
                        + event.getStatus().getMessage());
            } else if (event.getStatus().getException() != null) {
                System.out.println("Job failed: ");
                event.getStatus().getException().printStackTrace();
            } else {
                System.out.println("Job failed");
            }
            if (this.commandLine) {
                System.exit(1);
            }
        }
        if (status.getStatusCode() == Status.COMPLETED) {
            System.out.println("Job completed");
            if (this.task.getStdOutput() != null) {
                System.out.println(this.task.getStdOutput());
            }
            if (this.task.getStdError() != null) {
                System.err.println(this.task.getStdError());
            }
            if (this.commandLine) {
                System.exit(0);
            }
        }
    }

    public static void main(String args[]) {
        ArgumentParser ap = new ArgumentParser();
        ap.setExecutableName("cog-ws-invoke");
        ap.addOption("service", "Remote Web service location", "ws-location",
                ArgumentParser.NORMAL);
        ap.addAlias("service", "s");
        ap.addOption("method", "Method name", "name", ArgumentParser.NORMAL);
        ap.addAlias("method", "m");
        ap.addOption("provider", "Provider; available providers: "
                + AbstractionProperties.getProviders().toString(), "provider",
                ArgumentParser.OPTIONAL);
        ap.addAlias("provider", "p");
        ap.addOption("arguments", "Arguments. If more than one, use quotes",
                "string", ArgumentParser.OPTIONAL);
        ap.addAlias("arguments", "args");
        ap
                .addOption(
                        "attributes",
                        "Additional task specification attributes. Attributes can be specified as \"name=value[,name=value]\"",
                        "string", ArgumentParser.OPTIONAL);
        ap.addAlias("attributes", "a");
        ap.addFlag("verbose",
                "If enabled, display information about what is being done");
        ap.addAlias("verbose", "v");
        ap.addFlag("help", "Display usage");
        ap.addAlias("help", "h");
        try {
            ap.parse(args);
            if (ap.isPresent("help")) {
                ap.usage();
            } else {
                ap.checkMandatory();
                try {
                    ServiceInvocation invocation = new ServiceInvocation();
                    invocation.setCommandLine(true);
                    invocation.setServiceContact(ap.getStringValue("service"));
                    invocation.setProvider(ap.getStringValue("provider", "WS"));
                    invocation.setMethod(ap.getStringValue("method"));
                    invocation.setArguments(ap
                            .getStringValue("arguments", null));
                    invocation.setAttributes(ap.getStringValue("attributes",
                            null));
                    invocation.prepareTask();
                    invocation.submitTask();
                } catch (Exception e) {
                    logger.error("Exception in main", e);
                }
            }
        } catch (ArgumentParserException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            ap.usage();
        }
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String name) {
        this.method = name;
    }

    public String getServiceContact() {
        return serviceContact;
    }

    public void setServiceContact(String serviceContact) {
        this.serviceContact = serviceContact;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }
    public boolean isCommandLine() {
        return commandLine;
    }
    public void setCommandLine(boolean commandLine) {
        this.commandLine = commandLine;
    }
}