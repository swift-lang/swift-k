// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.examples.xml;

import java.io.File;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphHandlerImpl;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.TaskGraphHandler;
import org.globus.cog.abstraction.xml.TaskGraphMarshaller;
import org.globus.cog.abstraction.xml.TaskGraphUnmarshaller;

public class XML2TaskGraph implements StatusListener {
    static Logger logger = Logger.getLogger(XML2TaskGraph.class.getName());
    protected org.globus.cog.abstraction.interfaces.TaskGraph taskGraph;

    public XML2TaskGraph() {
        unmarshalTaskGraph();
        submitTaskGraph();
    }

    private void unmarshalTaskGraph() {
        try {
            File xmlFile = new File("TaskGraph.xml");
            this.taskGraph = TaskGraphUnmarshaller.unmarshal(xmlFile);

            this.taskGraph.addStatusListener(this);
        } catch (Exception e) {
            logger.error("Cannot unmarshal task graph", e);
        }
    }

    public void submitTaskGraph() {
        TaskGraphHandler handler = new TaskGraphHandlerImpl();
        try {
            handler.submit(this.taskGraph);
            logger.debug("TaskGraph submitted");
        } catch (InvalidSecurityContextException ise) {
            logger.error("Security Exception");
            ise.printStackTrace();
            System.exit(1);
        } catch (TaskSubmissionException tse) {
            logger.error("TaskSubmission Exception");
            tse.printStackTrace();
            System.exit(1);
        } catch (IllegalSpecException ispe) {
            logger.error("Specification Exception");
            ispe.printStackTrace();
            System.exit(1);
        } catch (InvalidServiceContactException isce) {
            logger.error("Service Contact Exception");
            isce.printStackTrace();
            System.exit(1);
        }
    }

    public void marshalTaskGraph() {
        try {
            File xmlFile = new File("TaskGraphDone.xml");
            TaskGraphMarshaller.marshal(this.taskGraph, xmlFile);
        } catch (Exception e) {
            logger.error("Cannot marshal task graph", e);
        }
    }

    public void statusChanged(StatusEvent event) {
        ExecutableObject eo = event.getSource();
        logger.debug(eo.getName());
        Status status = event.getStatus();
        logger.debug("Status changed to: " + status.getStatusString());

        if (status.getStatusCode() == Status.COMPLETED
            || status.getStatusCode() == Status.FAILED) {
            logger.info("Task Graph Done");
            marshalTaskGraph();
            System.exit(0);
        }
    }

    public static void main(String arg[]) {
        new XML2TaskGraph();
    }
}
