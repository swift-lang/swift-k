
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.xml;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Enumeration;

import org.globus.cog.abstraction.impl.common.taskgraph.DependencyPair;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Status;

/**
* This class translates (marhals) an object of type {@link org.globus.cog.abstraction.interfaces.TaskGraph} 
* into an XML format.  
*/
public class TaskGraphMarshaller {

    public synchronized static void marshal(
        org.globus.cog.abstraction.interfaces.TaskGraph taskGraph,
        File xmlFile)
        throws MarshalException {
        TaskGraph xmlTaskGraph = new TaskGraph();

        marshal(taskGraph, xmlTaskGraph);

        try {
            FileWriter writer = new FileWriter(xmlFile);
            xmlTaskGraph.marshal(writer);
        } catch (Exception e) {
            throw new MarshalException("Cannot marshal the task graph", e);
        }
    }

    public synchronized static void marshal(
        org.globus.cog.abstraction.interfaces.TaskGraph taskGraph,
        TaskGraph xmlTaskGraph)
        throws MarshalException {
        // set identity
        xmlTaskGraph.setIdentity(taskGraph.getIdentity().getValue());

        //set name
        String name = taskGraph.getName();
        if (name != null && name.length() > 0) {
            xmlTaskGraph.setName(name);
        }

        // set the status
        xmlTaskGraph.setFailureHandlingPolicy(
            getPolicyString(taskGraph.getFailureHandlingPolicy()));

        if (name != null && name.length() > 0) {
            xmlTaskGraph.setName(name);
        }

        // set tasks
        setTasks(taskGraph, xmlTaskGraph);

        // set taskgraphs
        setTaskGraphs(taskGraph, xmlTaskGraph);

        // set dependencies
        setDependency(taskGraph, xmlTaskGraph);

        // set the attributes
        Enumeration en = taskGraph.getAllAttributes();
        AttributeList list = new AttributeList();
        while (en.hasMoreElements()) {
            Attribute attribute = new Attribute();
            String attrName = (String) en.nextElement();
            attribute.setName(attrName);
            attribute.setValue((String) taskGraph.getAttribute(attrName));
            list.addAttribute(attribute);
        }
        if (list.getAttributeCount() > 0) {
            xmlTaskGraph.setAttributeList(list);
        }

        // set the status
        xmlTaskGraph.setStatus(
            getStatusString(taskGraph.getStatus().getStatusCode()));

        // set the submitted and completed time
        Calendar submittedTime = taskGraph.getSubmittedTime();
        Calendar completedTime = taskGraph.getCompletedTime();
        if (submittedTime != null) {
            xmlTaskGraph.setSubmittedTime(submittedTime.getTime());
        }
        if (completedTime != null) {
            xmlTaskGraph.setCompletedTime(completedTime.getTime());
        }

    }

    private static String getStatusString(int statusCode) {
        switch (statusCode) {
            case Status.ACTIVE :
                return "Active";

            case Status.CANCELED :
                return "Canceled";

            case Status.COMPLETED :
                return "Completed";

            case Status.FAILED :
                return "Failed";

            case Status.RESUMED :
                return "Resumed";

            case Status.SUBMITTED :
                return "Submitted";
                
            case Status.SUBMITTING :
                return "Submitting";

            case Status.SUSPENDED :
                return "Suspended";

            case Status.UNSUBMITTED :
                return "Unsubmitted";

            default :
                return "Unknown";
        }
    }

    private static void setTasks(
        org.globus.cog.abstraction.interfaces.TaskGraph taskGraph,
        TaskGraph xmlTaskGraph)
        throws MarshalException {
        Enumeration en = taskGraph.elements();

        while (en.hasMoreElements()) {
            ExecutableObject eo = (ExecutableObject) en.nextElement();
            if (eo.getObjectType() == ExecutableObject.TASK) {
                Task xmlTask = new Task();
                try {
                    TaskMarshaller.marshal(
                        (org.globus.cog.abstraction.interfaces.Task) eo,
                        xmlTask);
                } catch (MarshalException me) {
                    throw new MarshalException(
                        "Cannot marshal task " + eo.getIdentity().toString());
                }
                xmlTaskGraph.addTask(xmlTask);
            }
        }
    }

    private static void setTaskGraphs(
        org.globus.cog.abstraction.interfaces.TaskGraph taskGraph,
        TaskGraph xmlTaskGraph)
        throws MarshalException {
        Enumeration en = taskGraph.elements();

        while (en.hasMoreElements()) {
            ExecutableObject eo = (ExecutableObject) en.nextElement();
            if (eo.getObjectType() == ExecutableObject.TASKGRAPH) {
                TaskGraph xmlTG = new TaskGraph();
                try {
                    TaskGraphMarshaller.marshal(
                        (org.globus.cog.abstraction.interfaces.TaskGraph) eo,
                        xmlTG);
                } catch (MarshalException me) {
                    throw new MarshalException(
                        "Cannot marshal task graph "
                            + eo.getIdentity().toString());
                }
                xmlTaskGraph.addTaskGraph(xmlTG);
            }
        }
    }

    private static void setDependency(
        org.globus.cog.abstraction.interfaces.TaskGraph taskGraph,
        TaskGraph xmlTaskGraph) {
        org.globus.cog.abstraction.interfaces.Dependency dependency =
            taskGraph.getDependency();
        if (dependency != null) {
            DependencyList xmlDependencyList = new DependencyList();

            Enumeration en = dependency.elements();
            while (en.hasMoreElements()) {
                DependencyPair pair = (DependencyPair) en.nextElement();
                ExecutableObject from = pair.getFrom();
                ExecutableObject to = pair.getTo();
                Dependency xmlDependency = new Dependency();
                xmlDependency.setFrom(from.getIdentity().getValue());
                xmlDependency.setTo(to.getIdentity().getValue());
                xmlDependencyList.addDependency(xmlDependency);
            }

            if (xmlDependencyList.getDependencyCount() > 0) {
                xmlTaskGraph.setDependencyList(xmlDependencyList);
            }
        }
    }

    private static String getPolicyString(int policyCode) {
        switch (policyCode) {
            case org.globus.cog.abstraction.interfaces.TaskGraph.AbortOnFailure :
                return "AbortOnFailure";

            case org.globus.cog.abstraction.interfaces.TaskGraph.ContinueOnFailure :
                return "ContinueOnFailure";

            default :
                return "Unknown";
        }
    }

}
