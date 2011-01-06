// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridant.tasks;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.FileTransferTask;
import org.globus.cog.abstraction.impl.common.task.GenericTaskHandler;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;

public class GridCopy extends Task {
    private String name = null;
    private String source = null;
    private String destination = null;
    private URI sourceURI = null;
    private URI destinationURI = null;
    private boolean thirdparty = true;
    private long sleepTime = 2000;

    public void execute() throws BuildException {
        if (this.source == null) {
            throw new BuildException("Missing source-uri");
        }
        if (this.destination == null) {
            throw new BuildException("Missing destination-uri");
        }

        try {
            this.sourceURI = new URI(source);
        } catch (URISyntaxException e1) {

            throw new BuildException("Invalid source-uri: " + this.source, e1);
        }
        try {
            this.destinationURI = new URI(destination);
        } catch (URISyntaxException e2) {

            throw new BuildException("Invalid destination-uri: "
                    + this.destination, e2);
        }
        org.globus.cog.abstraction.interfaces.Task task = new FileTransferTask();
        if (this.name != null) {
            task.setName(this.name);
        }

        FileTransferSpecification spec = new FileTransferSpecificationImpl();
        spec.setSource(sourceURI.getPath());
        spec.setDestination(destinationURI.getPath());
        spec.setThirdParty(this.thirdparty);
        task.setSpecification(spec);

        // create the source service
        SecurityContext sourceSecurityContext;
        try {
            sourceSecurityContext = AbstractionFactory.newSecurityContext(sourceURI
                    .getScheme());
        } catch (InvalidProviderException e4) {

            throw new BuildException("Invalid provider: "
                    + sourceURI.getScheme(), e4);
        } catch (ProviderMethodException e4) {

            throw new BuildException("Invalid provider method", e4);
        }
        // selects the default credentials
        sourceSecurityContext.setCredentials(null);

        ServiceContact sourceServiceContact = new ServiceContactImpl();
        sourceServiceContact.setHost(sourceURI.getHost());
        sourceServiceContact.setPort(sourceURI.getPort());

        Service sourceService = new ServiceImpl(sourceURI.getScheme(),
                Service.FILE_TRANSFER, sourceServiceContact,
                sourceSecurityContext);

        // create the destination service
        SecurityContext destinationSecurityContext = null;
        try {
            destinationSecurityContext = AbstractionFactory
                    .newSecurityContext(destinationURI.getScheme());
        } catch (InvalidProviderException e3) {

            throw new BuildException("Invalid provider: "
                    + destinationURI.getScheme(), e3);
        } catch (ProviderMethodException e3) {

            throw new BuildException("Invalid provider method", e3);
        }
        // selects the default credentials
        destinationSecurityContext.setCredentials(null);

        ServiceContact destinationServiceContact = new ServiceContactImpl();
        destinationServiceContact.setHost(destinationURI.getHost());
        destinationServiceContact.setPort(destinationURI.getPort());

        Service destinationService = new ServiceImpl(
                destinationURI.getScheme(), Service.FILE_TRANSFER,
                destinationServiceContact, destinationSecurityContext);

        // add the source service at index 0
        task.setService(Service.FILE_TRANSFER_SOURCE_SERVICE, sourceService);

        // add the destination service at index 1
        task.setService(Service.FILE_TRANSFER_DESTINATION_SERVICE,
                destinationService);

        GenericTaskHandler handler = new GenericTaskHandler();

        try {
            handler.submit(task);
        } catch (Exception e) {
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
        log("grid-copy -- " + status.getStatusString());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setThirdparty(boolean thirdparty) {
        this.thirdparty = thirdparty;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }
}