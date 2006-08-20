// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.broker.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.broker.impl.BrokerImpl;
import org.globus.cog.broker.impl.ClassAdImpl;
import org.globus.cog.broker.interfaces.Broker;
import org.globus.cog.broker.interfaces.ClassAd;

import condor.classad.Expr;
import condor.classad.RecordExpr;

public class DynamicBrokering implements StatusListener {
    static Logger logger = Logger.getLogger(DynamicBrokering.class.getName());
    private Service service1, service2;
    private Vector taskList = new Vector();
    private Broker broker = null;

    private void createServices() {
        File file1 = new File("adv/lucky-exec.adv");
        try {
            // create services with classAds
            ClassAd classAd = new ClassAdImpl(file1);
            this.service1 = getService(classAd);
        } catch (FileNotFoundException e) {
            logger.error("Cannot find service advertisements", e);
            System.exit(1);
        }

        // or create services programatically
        SecurityContext securityContext = null;
        try {
            securityContext = AbstractionFactory.newSecurityContext("GT2");
        } catch (Exception e1) {
            logger.error("Cannot retrieve GT2 security context", e1);
            System.exit(1);
        }
        ServiceContact serviceContact = new ServiceContactImpl(
                "wiggum.mcs.anl.gov");
        this.service2 = new ServiceImpl("GT2", Service.JOB_SUBMISSION,
                serviceContact, securityContext);
        this.service2.setName("Wiggum-Exec");
    }

    private void createTasks() {
        for (int i = 0; i < 3; i++) {
            // create tasks with classAds
            Task task = new TaskImpl("job" + i, Task.JOB_SUBMISSION);
            JobSpecification jspec = new JobSpecificationImpl();
            jspec.setExecutable("/bin/date");
            jspec.setRedirected(true);
            task.setSpecification(jspec);
            ClassAd classAd = null;
            File file1 = new File("adv/task.adv");
            try {
                classAd = new ClassAdImpl(file1);
            } catch (FileNotFoundException e) {
                logger.error("Cannot find task advertisement", e);
                System.exit(1);
            }
            task.setAttribute("classad", classAd);
            task.addStatusListener(this);
            this.taskList.add(task);
        }

        // or create tasks without classAds
        Task task = new TaskImpl("job4", Task.JOB_SUBMISSION);
        JobSpecification jspec = new JobSpecificationImpl();
        jspec.setExecutable("/bin/date");
        jspec.setRedirected(true);
        task.setSpecification(jspec);
        task.addStatusListener(this);
        this.taskList.add(task);

        task = new TaskImpl("job5", Task.JOB_SUBMISSION);
        jspec = new JobSpecificationImpl();
        jspec.setExecutable("/bin/date");
        jspec.setRedirected(true);
        task.setSpecification(jspec);
        task.addStatusListener(this);
        this.taskList.add(task);
    }

    private void createBroker() {
        // create a new broker
        this.broker = new BrokerImpl();

        //add all available services
        this.broker.addService(this.service1);
        this.broker.addService(this.service2);

        // set the max number of parallel tasks
        this.broker.setConcurrency(10);

        // start the broker thread
        this.broker.start();

        // submit the collection of tasks
        // or you can submit them one by one
        this.broker.submit(taskList);
    }

    public static void main(String[] args) {
        DynamicBrokering db = new DynamicBrokering();
        db.createServices();
        db.createTasks();
        db.createBroker();
    }

    public void statusChanged(StatusEvent event) {
        ExecutableObject eo = event.getSource();
        Status status = event.getStatus();
        logger.debug("Status of task " + eo.getName() + " changed to: "
                + status.getStatusString());

        if (status.getStatusCode() == Status.SUBMITTED) {
            /*
             * logger.debug("Task Name=" + eo.getName()); logger.debug("Provider =" +
             * ((Task) eo).getProvider()); logger.debug("ServiceContact =" +
             * ((Task) eo).getService(0).getServiceContact() .getContact());
             */
        }

        if (status.getStatusCode() == Status.FAILED) {
            logger.error("Error of task " + eo.getName(), ((Task) eo)
                    .getStatus().getException());
        }

        if (status.getStatusCode() == Status.COMPLETED) {
            logger.debug("Output of task " + eo.getName() + ": "
                    + ((Task) eo).getStdOutput());
            // System.exit(1);
        }
    }

    private Service getService(ClassAd classAd) {
        Service service = new ServiceImpl();
        RecordExpr recordExpr = classAd.getAd();
        Expr expr = null;

        // get the name
        expr = recordExpr.lookup("Name");
        if (expr != null) {
            try {
                service.setName(expr.stringValue());
            } catch (Exception e) {
                service.setName("");
            }
        }

        // get the provider
        expr = recordExpr.lookup("Provider");
        if (expr != null) {
            try {
                service.setProvider(expr.stringValue());
            } catch (Exception e) {
                service.setProvider(null);
            }
        }

        //      get the type
        expr = recordExpr.lookup("ServiceType");
        if (expr != null) {
            try {
                String type = expr.stringValue();
                if (type.equalsIgnoreCase("JobSubmission")) {
                    service.setType(Task.JOB_SUBMISSION);
                } else if (type.equalsIgnoreCase("FileTransfer")) {
                    service.setType(Task.FILE_TRANSFER);
                } else if (type.equalsIgnoreCase("FileOperation")) {
                    service.setType(Task.FILE_OPERATION);
                } else if (type.equalsIgnoreCase("InformationQuery")) {
                    service.setType(Task.INFORMATION_QUERY);
                } else {
                    service.setType(0);
                }
            } catch (Exception e) {
                service.setType(0);
            }
        }

        //      get the service contact
        expr = recordExpr.lookup("ServiceContact");
        ServiceContact serviceContact = null;
        if (expr != null) {
            try {
                serviceContact = new ServiceContactImpl(expr.stringValue());

            } catch (Exception e) {
                serviceContact = null;
            }
            service.setServiceContact(serviceContact);
        }

        //get securityContext
        SecurityContext securityContext = null;
        try {
            securityContext = AbstractionFactory.newSecurityContext(service
                    .getProvider());
        } catch (Exception e) {
            securityContext = null;
        }
        service.setSecurityContext(securityContext);

        return service;
    }

}