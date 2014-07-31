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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.abstraction.xml;

import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.Unmarshaller;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;

/**
 * This class translates (unmarshals) an XML file into an object of type {@link org.globus.cog.abstraction.interfaces.Task}. 
 */
public class TaskUnmarshaller {
    static Logger logger = Logger.getLogger(TaskUnmarshaller.class.getName());

    public synchronized static org.globus.cog.abstraction.interfaces.Task unmarshal(
            File xmlFile) throws UnmarshalException {
        Task xmlTask = null;
        try {
            FileReader reader = new FileReader(xmlFile);
            xmlTask = (Task) Unmarshaller.unmarshal(Task.class, reader);
        } catch (Exception e) {
            throw new UnmarshalException("Cannot unmarshal task", e);
        }

        return unmarshal(xmlTask);
    }

    public synchronized static org.globus.cog.abstraction.interfaces.Task unmarshal(
            Task xmlTask) throws UnmarshalException {

        org.globus.cog.abstraction.interfaces.Task task = new TaskImpl();

        //	set the task identity
        String xmlIdentity = xmlTask.getIdentity();
        if (xmlIdentity != null && xmlIdentity.length() > 0) {
            Identity identity = new IdentityImpl();
            identity.setValue(xmlIdentity.trim());
            task.setIdentity(identity);
        }

        // set the task name
        task.setName(xmlTask.getName());

        // set the task type
        String type = xmlTask.getType();
        if (type == null || type.length() == 0) {
            throw new UnmarshalException("Cannot determine the type of task");
        } else {
            if (type.trim().equalsIgnoreCase("file transfer")
                    || type.trim().equalsIgnoreCase("filetransfer")) {
                task
                        .setType(org.globus.cog.abstraction.interfaces.Task.FILE_TRANSFER);
            } else if (type.trim().equalsIgnoreCase("job submission")
                    || type.trim().equalsIgnoreCase("jobsubmission")) {
                task
                        .setType(org.globus.cog.abstraction.interfaces.Task.JOB_SUBMISSION);
            } else {
                throw new UnmarshalException(
                        "Cannot determine the task type: "
                                + type
                                + ". Supported types are File Transfer and Job Submission");
            }
        }

        // set the task provider
        task.setProvider(xmlTask.getProvider());

        // set the task specification
        setServices(task, xmlTask);

        // set the task specification
        setSpecification(task, xmlTask);

        // set the task attributes
        AttributeList attrList = xmlTask.getAttributeList();
        if (attrList != null) {
            Enumeration en = attrList.enumerateAttribute();
            while (en.hasMoreElements()) {
                Attribute attribute = (Attribute) en.nextElement();
                task.setAttribute(attribute.getName().trim(), attribute
                        .getValue().trim());
            }
        }

        // set the task status
        String status = xmlTask.getStatus();
        if (status != null && status.length() > 0) {
            task.setStatus(getStatusCode(status.trim()));
        }

        // the submitted and completed times of the task will be re-computed
        return task;
    }

    private static void setServices(
            org.globus.cog.abstraction.interfaces.Task task, Task xmlTask)
            throws UnmarshalException {
        ServiceList serviceList = xmlTask.getServiceList();
        for (int i = 0; i < serviceList.getServiceCount(); i++) {
            Service xmlService = serviceList.getService(i);
            org.globus.cog.abstraction.interfaces.Service service = new ServiceImpl();

            Identity identity = new IdentityImpl();
            identity.setValue(xmlService.getIdentity());
            service.setIdentity(identity);

            service.setName(xmlService.getName());

            String type = xmlService.getType();
            if (type != null && type.length() > 0) {
                if (type.trim().equalsIgnoreCase("file transfer")
                        || type.trim().equalsIgnoreCase("filetransfer")) {
                    service
                            .setType(org.globus.cog.abstraction.interfaces.Service.FILE_TRANSFER);
                } else if (type.trim().equalsIgnoreCase("job submission")
                        || type.trim().equalsIgnoreCase("jobsubmission")) {
                    service
                            .setType(org.globus.cog.abstraction.interfaces.Service.JOB_SUBMISSION);
                } else if (type.trim().equalsIgnoreCase("file operation")
                        || type.trim().equalsIgnoreCase("fileoperation")) {
                    service
                            .setType(org.globus.cog.abstraction.interfaces.Service.FILE_OPERATION);
                }
            }
            String provider = xmlService.getProvider();
            // set the service contact
            String serviceContact = xmlService.getServiceContact();
            if (serviceContact != null && serviceContact.length() > 0) {
                ServiceContact contact = new ServiceContactImpl(serviceContact
                        .trim());
                service.setServiceContact(contact);
            }
            
            if (provider != null && provider.length() > 0) {
                service.setProvider(provider.trim());

                try {
                    service.setSecurityContext(AbstractionFactory.newSecurityContext(provider, service.getServiceContact()));
                } catch (Exception e) {
                    throw new UnmarshalException(
                            "Cannot establish the appropriate security context",
                            e);
                }
            }


            // set the service attributes
            AttributeList attrList = xmlService.getAttributeList();
            if (attrList != null) {
                Enumeration en = attrList.enumerateAttribute();
                while (en.hasMoreElements()) {
                    Attribute attribute = (Attribute) en.nextElement();
                    service.setAttribute(attribute.getName().trim(), attribute
                            .getValue().trim());
                }
            }

            task.setService(i, service);
        }
    }

    private static void setSpecification(
            org.globus.cog.abstraction.interfaces.Task task, Task xmlTask)
            throws UnmarshalException {
        //		set the specification
        Specification xmlSpecification = xmlTask.getSpecification();

        if (xmlSpecification == null) {
            throw new UnmarshalException(
                    "Unable to unmarshall task specification");
        } else {
            switch (task.getType()) {
            case org.globus.cog.abstraction.interfaces.Task.JOB_SUBMISSION:

                JobSpecification xmlJobSpec = xmlSpecification
                        .getJobSpecification();
                org.globus.cog.abstraction.interfaces.JobSpecification jobSpec = new JobSpecificationImpl();

                String executable = xmlJobSpec.getExecutable();
                if (executable == null || executable.length() == 0) {
                    throw new UnmarshalException(
                            "Executable for JobSpecification not provided");
                } else {
                    jobSpec.setExecutable(executable.trim());
                }

                String directory = xmlJobSpec.getDirectory();
                if (directory != null && directory.length() > 0) {
                    jobSpec.setDirectory(directory.trim());
                }

                String arguments = xmlJobSpec.getArguments();
                if (arguments != null && arguments.length() > 0) {
                    jobSpec.setArguments(arguments.trim());
                }

                String stdOutput = xmlJobSpec.getStdOutput();
                if (stdOutput != null && stdOutput.length() > 0) {
                    jobSpec.setStdOutput(stdOutput.trim());
                }

                String stdInput = xmlJobSpec.getStdInput();
                if (stdInput != null && stdInput.length() > 0) {
                    jobSpec.setStdInput(stdInput.trim());
                }

                String stdError = xmlJobSpec.getStdError();
                if (stdError != null && stdError.length() > 0) {
                    jobSpec.setStdError(stdError.trim());
                }

                jobSpec.setBatchJob(xmlJobSpec.getBatchJob());
                jobSpec.setRedirected(xmlJobSpec.getRedirected());
                jobSpec.setLocalExecutable(xmlJobSpec.getLocalExecutable());

                AttributeList attrList = xmlJobSpec.getAttributeList();
                if (attrList != null) {
                    Enumeration en = attrList.enumerateAttribute();
                    while (en.hasMoreElements()) {
                        Attribute attribute = (Attribute) en.nextElement();
                        jobSpec.setAttribute(attribute.getName().trim(),
                                attribute.getValue().trim());
                    }
                }

                task.setSpecification(jobSpec);
                break;

            case org.globus.cog.abstraction.interfaces.Task.FILE_TRANSFER:
                FileTransferSpecification xmlFileSpec = xmlSpecification
                        .getFileTransferSpecification();
                org.globus.cog.abstraction.interfaces.FileTransferSpecification fileSpec = new FileTransferSpecificationImpl();

                String source = xmlFileSpec.getSource();
                if (source == null || source.length() == 0) {
                    throw new UnmarshalException(
                            "Invalid source for FileTransferSpecification");
                } else {
                    fileSpec.setSource(source.trim());
                }

                String destination = xmlFileSpec.getDestination();
                if (destination == null || destination.length() == 0) {
                    throw new UnmarshalException(
                            "Invalid destination for FileTransferSpecification");
                } else {
                    fileSpec.setDestination(destination.trim());
                }

                fileSpec.setThirdParty(xmlFileSpec.getThirdParty());

                AttributeList attrList1 = xmlFileSpec.getAttributeList();
                if (attrList1 != null) {
                    Enumeration en1 = attrList1.enumerateAttribute();
                    while (en1.hasMoreElements()) {
                        Attribute attribute = (Attribute) en1.nextElement();
                        fileSpec.setAttribute(attribute.getName().trim(),
                                attribute.getValue().trim());
                    }
                }

                task.setSpecification(fileSpec);

                break;

            default:
                throw new UnmarshalException("Invalid specification type");
            }
        }
    }

    private static int getStatusCode(String statusString) {
        // Every status other than completed will be transitioned to unsubmitted
        // completed tasks will not be re-executed
        if (statusString.equalsIgnoreCase("Active")) {
            return Status.ACTIVE;
        } else if (statusString.equalsIgnoreCase("Canceled")) {
            return Status.CANCELED;
        } else if (statusString.equalsIgnoreCase("Completed")) {
            return Status.COMPLETED;
        } else if (statusString.equalsIgnoreCase("Failed")) {
            return Status.FAILED;
        } else if (statusString.equalsIgnoreCase("Resumed")) {
            return Status.RESUMED;
        } else if (statusString.equalsIgnoreCase("Submitting")) {
            return Status.SUBMITTING;
        } else if (statusString.equalsIgnoreCase("Submitted")) {
            return Status.SUBMITTED;
        } else if (statusString.equalsIgnoreCase("Suspended")) {
            return Status.SUSPENDED;
        } else if (statusString.equalsIgnoreCase("Unsubmitted")) {
            return Status.UNSUBMITTED;
        } else {
            return Status.UNSUBMITTED;
        }
    }
}