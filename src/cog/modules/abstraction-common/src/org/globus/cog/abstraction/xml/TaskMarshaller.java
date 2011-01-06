// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.xml;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.globus.cog.abstraction.interfaces.ServiceContact;

/**
 * This class translates (marhals) an object of type {@link org.globus.cog.abstraction.interfaces.Task} 
 * into an XML format.  
 */
public class TaskMarshaller {

    public synchronized static void marshal(
            org.globus.cog.abstraction.interfaces.Task task, File xmlFile)
            throws MarshalException {
        Task xmlTask = new Task();

        marshal(task, xmlTask);

        try {
            FileWriter writer = new FileWriter(xmlFile);
            xmlTask.marshal(writer);
        } catch (Exception e) {
            throw new MarshalException("Cannot marshal the task", e);
        }
    }

    public synchronized static void marshal(
            org.globus.cog.abstraction.interfaces.Task task, Task xmlTask)
            throws MarshalException {
        //	set the task identity
        xmlTask.setIdentity(task.getIdentity().getValue());

        // set the task name
        String name = task.getName();
        if (name != null && name.length() > 0) {
            xmlTask.setName(name);
        }

        // set the task type
        String type;
        switch (task.getType()) {
        case org.globus.cog.abstraction.interfaces.Task.FILE_TRANSFER:
            type = "File Transfer";
            break;
        case org.globus.cog.abstraction.interfaces.Task.JOB_SUBMISSION:
            type = "Job Submission";
            break;
        case org.globus.cog.abstraction.interfaces.Task.INFORMATION_QUERY:
            type = "Information Query";
            break;
        case org.globus.cog.abstraction.interfaces.Task.FILE_OPERATION:
            type = "File Operation";
            break;
        default:
            type = Integer.toString(task.getType());
            break;
        }
        xmlTask.setType(type);

        // set the task provider
        String provider = task.getProvider();
        if (provider != null && provider.length() > 0) {
            xmlTask.setProvider(provider);
        }

        // set the services
        setServices(task, xmlTask);

        // set the task specification
        setSpecification(task, xmlTask);

        // set the task attributes
        Iterator en = task.getAttributeNames().iterator();
        AttributeList list = new AttributeList();
        while (en.hasNext()) {
            Attribute attribute = new Attribute();
            String attrName = (String) en.next();
            attribute.setName(attrName);
            attribute.setValue((String) task.getAttribute(attrName));
            list.addAttribute(attribute);
        }
        if (list.getAttributeCount() > 0) {
            xmlTask.setAttributeList(list);
        }

        // set the task status
        xmlTask.setStatus(task.getStatus().getStatusString());
    }

    private static void setServices(
            org.globus.cog.abstraction.interfaces.Task task, Task xmlTask) {
        Collection coll = task.getAllServices();
        Iterator iterator = coll.iterator();
        ServiceList serviceList = new ServiceList();
        while (iterator.hasNext()) {
            org.globus.cog.abstraction.interfaces.Service service = (org.globus.cog.abstraction.interfaces.Service) iterator
                    .next();
            Service xmlService = new Service();
            xmlService.setIdentity(service.getIdentity()
                    .getValue());

            String sname = service.getName();
            if (sname != null && sname.length() > 0) {
                xmlService.setName(sname);
            }

            String sprovider = service.getProvider();
            if (sprovider != null && sprovider.length() > 0) {
                xmlService.setProvider(sprovider);
            }

            ServiceContact serviceContact = service.getServiceContact();
            if (serviceContact != null && serviceContact.getContact() != null
                    && serviceContact.getContact().length() > 0) {
                xmlService
                        .setServiceContact(serviceContact.getContact().trim());
            }

            // set the service type
            String stype;
            switch (service.getType()) {
            case org.globus.cog.abstraction.interfaces.Service.FILE_TRANSFER:
                stype = "File Transfer";
                break;
            case org.globus.cog.abstraction.interfaces.Service.JOB_SUBMISSION:
                stype = "Job Submission";
                break;
            case org.globus.cog.abstraction.interfaces.Service.INFORMATION_QUERY:
                stype = "Information Query";
                break;
            case org.globus.cog.abstraction.interfaces.Service.FILE_OPERATION:
                stype = "File Operation";
                break;
            default:
                stype = Integer.toString(task.getType());
                break;
            }
            xmlService.setType(stype);

            // set the service attributes
            Enumeration en = service.getAllAttributes();
            AttributeList list = new AttributeList();
            while (en.hasMoreElements()) {
                Attribute attribute = new Attribute();
                String attrName = (String) en.nextElement();
                attribute.setName(attrName);
                attribute.setValue((String) service.getAttribute(attrName));
                list.addAttribute(attribute);
            }
            if (list.getAttributeCount() > 0) {
                xmlService.setAttributeList(list);
            }

            serviceList.addService(xmlService);
        }

        if (serviceList.getServiceCount() > 0) {
            xmlTask.setServiceList(serviceList);
        }

    }

    private static void setSpecification(
            org.globus.cog.abstraction.interfaces.Task task, Task xmlTask)
            throws MarshalException {
        //		set the specification
        org.globus.cog.abstraction.interfaces.Specification specification = task
                .getSpecification();
        if (specification == null) {
            throw new MarshalException("Unable to marshall task specification");
        } else {
            Specification xmlSpec = new Specification();
            switch (specification.getType()) {
            case org.globus.cog.abstraction.interfaces.Specification.JOB_SUBMISSION:
                JobSpecification xmlJobSpec = new JobSpecification();
                org.globus.cog.abstraction.interfaces.JobSpecification jobSpec = (org.globus.cog.abstraction.interfaces.JobSpecification) task
                        .getSpecification();

                String executable = jobSpec.getExecutable();
                if (executable == null || executable.length() == 0) {
                    throw new MarshalException(
                            "Executable for JobSpecification not provided");
                } else {
                    xmlJobSpec.setExecutable(executable);
                }

                String directory = jobSpec.getDirectory();
                if (directory != null && directory.length() > 0) {
                    xmlJobSpec.setDirectory(directory);
                }

                String arguments = jobSpec.getArgumentsAsString();
                if (arguments != null && arguments.length() > 0) {
                    xmlJobSpec.setArguments(arguments);
                }

                String stdOutput = jobSpec.getStdOutput();
                if (stdOutput != null && stdOutput.length() > 0) {
                    xmlJobSpec.setStdOutput(stdOutput);
                }

                String stdInput = jobSpec.getStdInput();
                if (stdInput != null && stdInput.length() > 0) {
                    xmlJobSpec.setStdInput(stdInput);
                }

                String stdError = jobSpec.getStdError();
                if (stdError != null && stdError.length() > 0) {
                    xmlJobSpec.setStdError(stdError);
                }

                xmlJobSpec.setBatchJob(jobSpec.isBatchJob());
                xmlJobSpec.setRedirected(jobSpec.isRedirected());
                xmlJobSpec.setLocalExecutable(jobSpec.isLocalExecutable());

                Enumeration en = jobSpec.getAllAttributes();
                AttributeList list = new AttributeList();
                while (en.hasMoreElements()) {
                    Attribute attribute = new Attribute();
                    String name = (String) en.nextElement();
                    attribute.setName(name);
                    attribute.setValue((String) jobSpec.getAttribute(name));
                    list.addAttribute(attribute);
                }
                if (list.getAttributeCount() > 0) {
                    xmlJobSpec.setAttributeList(list);
                }
                xmlSpec.setJobSpecification(xmlJobSpec);
                xmlTask.setSpecification(xmlSpec);
                break;

            case org.globus.cog.abstraction.interfaces.Specification.FILE_TRANSFER:
                FileTransferSpecification xmlFileSpec = new FileTransferSpecification();
                org.globus.cog.abstraction.interfaces.FileTransferSpecification fileSpec = (org.globus.cog.abstraction.interfaces.FileTransferSpecification) task
                        .getSpecification();

                String source = fileSpec.getSource();
                if (source == null || source.length() == 0) {
                    throw new MarshalException(
                            "Invalid source for FileTransferSpecification");
                } else {
                    xmlFileSpec.setSource(source);
                }

                String destination = fileSpec.getDestination();
                if (destination == null || destination.length() == 0) {
                    throw new MarshalException(
                            "Invalid destination for FileTransferSpecification");
                } else {
                    xmlFileSpec.setDestination(destination);
                }

                xmlFileSpec.setThirdParty(fileSpec.isThirdParty());

                Enumeration en1 = fileSpec.getAllAttributes();
                AttributeList list1 = new AttributeList();
                while (en1.hasMoreElements()) {
                    Attribute attribute = new Attribute();
                    String name = (String) en1.nextElement();
                    attribute.setName(name);
                    attribute.setValue((String) fileSpec.getAttribute(name));
                    list1.addAttribute(attribute);
                }

                if (list1.getAttributeCount() > 0) {
                    xmlFileSpec.setAttributeList(list1);
                }
                xmlSpec.setFileTransferSpecification(xmlFileSpec);
                xmlTask.setSpecification(xmlSpec);
                break;

            default:
                throw new MarshalException("Invalid specification type");
            }
        }
    }

}