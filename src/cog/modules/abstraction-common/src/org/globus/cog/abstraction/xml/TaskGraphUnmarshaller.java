// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.xml;

import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;

import org.exolab.castor.xml.Unmarshaller;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.taskgraph.DependencyImpl;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphImpl;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Status;

/**
 * This class translates (unmarshals) an XML file into an object of type {@link org.globus.cog.abstraction.interfaces.TaskGraph}. 
 */
public class TaskGraphUnmarshaller {

    public synchronized static org.globus.cog.abstraction.interfaces.TaskGraph unmarshal(
            File xmlFile) throws UnmarshalException {
        TaskGraph xmlTaskGraph = null;

        try {
            FileReader reader = new FileReader(xmlFile);
            xmlTaskGraph = (TaskGraph) Unmarshaller.unmarshal(TaskGraph.class,
                    reader);
        } catch (Exception e) {
            throw new UnmarshalException("Cannot unmarshal task graph", e);
        }

        return unmarshal(xmlTaskGraph);
    }

    public synchronized static org.globus.cog.abstraction.interfaces.TaskGraph unmarshal(
            TaskGraph xmlTaskGraph) throws UnmarshalException {
        org.globus.cog.abstraction.interfaces.TaskGraph taskGraph = new TaskGraphImpl();

        // set identity
        String xmlIdentity = xmlTaskGraph.getIdentity();
        if (xmlIdentity != null && xmlIdentity.length() > 0) {
            Identity identity = new IdentityImpl();
            identity.setValue(xmlIdentity.trim());
            taskGraph.setIdentity(identity);
        }

        // set the task name
        String name = xmlTaskGraph.getName();
        if (name != null && name.length() > 0) {
            taskGraph.setName(name.trim());
        }

        // set the failureHandlingPollicy
        String policy = xmlTaskGraph.getFailureHandlingPolicy();
        if (policy != null && policy.length() > 0) {
            taskGraph.setFailureHandlingPolicy(getPolicyCode(policy.trim()));
        }

        // set tasks
        setTasks(taskGraph, xmlTaskGraph);

        // set taskgraphs
        setTaskGraphs(taskGraph, xmlTaskGraph);

        // set dependencies
        setDependency(taskGraph, xmlTaskGraph);

        // set the attributes
        AttributeList attrList = xmlTaskGraph.getAttributeList();
        if (attrList != null) {
            Enumeration en = attrList.enumerateAttribute();
            while (en.hasMoreElements()) {
                Attribute attribute = (Attribute) en.nextElement();
                taskGraph.setAttribute(attribute.getName().trim(), attribute
                        .getValue().trim());
            }
        }

        // set the status
        String status = xmlTaskGraph.getStatus();
        if (status != null && status.length() > 0) {
            taskGraph.setStatus(getStatusCode(status.trim()));
        }

        // the submitted and completed times of the task graph will be
        // re-computed

        return taskGraph;
    }

    private static void setTasks(
            org.globus.cog.abstraction.interfaces.TaskGraph taskGraph,
            TaskGraph xmlTaskGraph) throws UnmarshalException {
        Enumeration en = xmlTaskGraph.enumerateTask();

        while (en.hasMoreElements()) {
            Task xmlTask = (Task) en.nextElement();
            org.globus.cog.abstraction.interfaces.Task task = TaskUnmarshaller
                    .unmarshal(xmlTask);

            try {
                taskGraph.add(task);
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    private static void setTaskGraphs(
            org.globus.cog.abstraction.interfaces.TaskGraph taskGraph,
            TaskGraph xmlTaskGraph) throws UnmarshalException {
        Enumeration en = xmlTaskGraph.enumerateTaskGraph();

        while (en.hasMoreElements()) {
            TaskGraph xmlTG = (TaskGraph) en.nextElement();
            org.globus.cog.abstraction.interfaces.TaskGraph tg = TaskGraphUnmarshaller
                    .unmarshal(xmlTG);

            try {
                taskGraph.add(tg);
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    private static void setDependency(
            org.globus.cog.abstraction.interfaces.TaskGraph taskGraph,
            TaskGraph xmlTaskGraph) {
        DependencyList xmlDependencyList = xmlTaskGraph.getDependencyList();

        org.globus.cog.abstraction.interfaces.Dependency dependency = new DependencyImpl();
        if (xmlDependencyList == null){
            return;
        }
        
        Enumeration en = xmlDependencyList.enumerateDependency();
        while (en.hasMoreElements()) {
            Dependency xmlDependency = (Dependency) en.nextElement();

            Identity from = new IdentityImpl();
            //this obviously ignores the namespace
            from.setValue(xmlDependency.getFrom().trim());

            Identity to = new IdentityImpl();
            to.setValue(xmlDependency.getTo().trim());

            dependency.add(taskGraph.get(from), taskGraph.get(to));
        }

        taskGraph.setDependency(dependency);

    }

    private static int getStatusCode(String statusString) {
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
            return Status.UNKNOWN;
        }
    }

    private static int getPolicyCode(String policyString) {
        if (policyString.equalsIgnoreCase("AbortOnFailure")) {
            return org.globus.cog.abstraction.interfaces.TaskGraph.AbortOnFailure;
        } else if (policyString.equalsIgnoreCase("ContinueOnFailure")) {
            return org.globus.cog.abstraction.interfaces.TaskGraph.ContinueOnFailure;
        } else {
            return org.globus.cog.abstraction.interfaces.TaskGraph.AbortOnFailure;
        }
    }
}