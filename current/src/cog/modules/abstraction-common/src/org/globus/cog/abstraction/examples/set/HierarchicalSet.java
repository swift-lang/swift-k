// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.examples.set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.set.SetHandlerImpl;
import org.globus.cog.abstraction.impl.common.set.SetImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphImpl;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Set;
import org.globus.cog.abstraction.interfaces.SetHandler;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskGraph;
import org.globus.cog.abstraction.tools.transfer.FileTransfer;

/*
 * This class serves as an example to demonstrate the execution of hierarchical
 * set. It simply shows the semantics of how a user can create a hierarchy of
 * sets. The parameters to the task specification MUST be changed to suite your
 * requirements and environment.
 */
public class HierarchicalSet implements StatusListener {
    static Logger logger = Logger.getLogger(HierarchicalSet.class.getName());
    protected Set set = null;
    protected boolean active = false;
    protected Task task7;

    public HierarchicalSet() {
        this.set = new SetImpl();
    }

    public void createSet() throws Exception {
        Task task1 = prepareTask("Task1",
                "gridftp://hot.anl.gov:2811//home/cog/gridfile1",
                "gridftp://co;d.anl.gov:2811//home/cog/gridfile1");

        Task task2 = prepareTask("Task2",
                "gridftp://hot.anl.gov:2811//home/cog/gridfile2",
                "gridftp://co;d.anl.gov:2811//home/cog/gridfile2");

        Task task3 = prepareTask("Task3",
                "gridftp://hot.anl.gov:2811//home/cog/gridfile3",
                "gridftp://co;d.anl.gov:2811//home/cog/gridfile3");
        Task task4 = prepareTask("Task4",
                "gridftp://hot.anl.gov:2811//home/cog/gridfile4",
                "gridftp://co;d.anl.gov:2811//home/cog/gridfile4");

        Task task5 = prepareTask("Task5",
                "gridftp://hot.anl.gov:2811//home/cog/gridfile5",
                "gridftp://co;d.anl.gov:2811//home/cog/gridfile5");

        Task task6 = prepareTask("Task6",
                "gridftp://hot.anl.gov:2811//home/cog/gridfile6",
                "gridftp://co;d.anl.gov:2811//home/cog/gridfile6");

        Task task7 = prepareTask("Task7",
                "gridftp://hot.anl.gov:2811//home/cog/gridfile7",
                "gridftp://co;d.anl.gov:2811//home/cog/gridfile7");

        TaskGraph tg = new TaskGraphImpl();
        tg.setName("TaskGraph");
        try {
            tg.add(task6);
            tg.add(task7);

            this.set.add(task1);
            this.set.add(task2);
            this.set.add(task3);
            this.set.add(task4);
            this.set.add(task5);
            this.set.add(tg);
        } catch (Exception e) {
        }

        this.set.addStatusListener(this);
    }

    public void submitSet() {
        SetHandler handler = new SetHandlerImpl();
        try {
            handler.submit(this.set);
            logger.debug("Set submitted");
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

    private Task prepareTask(String name, String source, String destination)
            throws Exception {
        FileTransfer fileTransfer = new FileTransfer(name, source, destination);
        fileTransfer.prepareTask();
        Task task = fileTransfer.getFileTransferTask();
        task.removeStatusListener(fileTransfer);
        return task;
    }

    public void statusChanged(StatusEvent event) {
        ExecutableObject eo = event.getSource();
        logger.debug(eo.getName());
        Status status = event.getStatus();
        logger.debug("Status changed to: " + status.getStatusString());

        if (status.getStatusCode() == Status.COMPLETED
                || status.getStatusCode() == Status.FAILED) {
            logger.info("Set  Done");
            System.exit(1);
        }
    }

    public static void main(String arg[]) {
        try {
            HierarchicalSet q = new HierarchicalSet();
            q.createSet();
            q.submitSet();
        } catch (Exception e) {
            logger.error("Exception caught: ", e);
        }
    }
}