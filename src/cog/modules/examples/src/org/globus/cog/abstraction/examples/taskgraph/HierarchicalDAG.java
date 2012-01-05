// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.examples.taskgraph;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphHandlerImpl;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphImpl;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskGraph;
import org.globus.cog.abstraction.interfaces.TaskGraphHandler;
import org.globus.cog.abstraction.tools.transfer.FileTransfer;

/*
 * This class serves as an example to demonstrate the execution of hierarchical
 * taskgraphs (DAGs). It simply shows the semantics of how a user can create a
 * hierarchy of task graph and submit additional tasks to an already executing
 * task graph (a.k.a adding tasks to a "live taskgraph"). The parameters to the
 * task specification MUST be changed to suite your requirements and
 * environment.
 */
public class HierarchicalDAG implements StatusListener {
    static Logger logger = Logger.getLogger(HierarchicalDAG.class.getName());
    protected org.globus.cog.abstraction.interfaces.TaskGraph taskGraph;
    protected boolean active = false;
    private boolean submitted = false;
    private Task task6 = null;
    private Task task9 = null;
    private TaskGraph tg5 = null;

    public void createDAG() throws Exception {
        TaskGraph tg1 = new TaskGraphImpl();
        try {
            tg1.setName("TG1");
            Task task1 = prepareTask("Task1",
                    "gridftp://hot.mcs.anl.gov:2811//home/cog/gridfile1",
                    "gridftp://cold.mcs.anl.gov:2811//home/cog/gridfile1");
            Task task2 = prepareTask("Task2",
                    "gridftp://cold.mcs.anl.gov:2811//home/cog/gridfile2",
                    "gridftp://hot.mcs.anl.gov:2811//home/cog/gridfile2");
            tg1.add(task1);
            tg1.add(task2);
            tg1.addDependency(task1, task2);

            TaskGraph tg2 = new TaskGraphImpl();
            tg2.setName("TG2");
            Task task3 = prepareTask("Task3",
                    "gridftp://hot.mcs.anl.gov:2811//home/cog/gridfile3",
                    "gridftp://cold.mcs.anl.gov:2811//home/cog/gridfile3");
            Task task4 = prepareTask("Task4",
                    "gridftp://cold.mcs.anl.gov:2811//home/cog/gridfile4",
                    "gridftp://hot.mcs.anl.gov:2811//home/cog/gridfile4");
            tg2.add(task3);
            tg2.add(task4);
            tg2.addDependency(task3, task4);

            TaskGraph tg3 = new TaskGraphImpl();
            tg3.setName("TG3");
            tg3.add(tg1);
            tg3.add(tg2);
            tg3.addDependency(tg1, tg2);

            TaskGraph tg4 = new TaskGraphImpl();
            tg4.setName("TG4");
            Task task5 = prepareTask("Task5",
                    "gridftp://new.mcs.anl.gov:2811//home/cog/gridfile6",
                    "gridftp://old.mcs.anl.gov:2811//home/cog/gridfile7");
            tg4.add(tg3);
            tg4.add(task5);
            tg4.addDependency(tg3, task5);

            tg5 = new TaskGraphImpl();

            Task task7 = prepareTask("Task7",
                    "gridftp://old.mcs.anl.gov:2811//home/cog/gridfile8",
                    "gridftp://new.mcs.anl.gov:2811//home/cog/gridfile9");
            Task task8 = prepareTask("Task8",
                    "gridftp://here.mcs.anl.gov:2811//home/cog/gridfile10",
                    "gridftp://there.mcs.anl.gov:2811//home/cog/gridfile10");
            task9 = prepareTask("Task9",
                    "gridftp://here.mcs.anl.gov:2811//home/cog/gridfile11",
                    "gridftp://there.mcs.anl.gov:2811//home/cog/gridfile11");

            tg5.add(task7);
            tg5.add(task8);
            tg5.add(task9);
            tg5.add(tg4);

            tg5.addDependency(task7, task8);
            tg5.addDependency(task8, task9);
            tg5.addDependency(task8, tg4);
            tg5.addDependency(task9, tg4);

            this.taskGraph = tg5;
            this.taskGraph.setName("Main Graph");
            this.taskGraph.addStatusListener(this);
        } catch (Exception e) {
            logger.error("Unable to create DAG", e);
        }
    }

    public void submitDAG() {
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

        // demonstrating "live" task graphs
        if (status.getStatusCode() == Status.SUBMITTED && !submitted) {
            submitted = true;
            try {
                task6 = prepareTask("Task6",
                        "gridftp://this.mcs.anl.gov:2811//home/cog/gridfile12",
                        "gridftp://that.mcs.anl.gov:2811//home/cog/gridfile12");
                tg5.addDependency(task6, task9);
                tg5.add(task6);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (status.getStatusCode() == Status.COMPLETED
                || status.getStatusCode() == Status.FAILED) {
            logger.info("Task Graph Done");
            // System.exit(1);
        }
    }

    public static void main(String arg[]) {
        try {
            HierarchicalDAG dag = new HierarchicalDAG();
            dag.createDAG();
            dag.submitDAG();
        } catch (Exception e) {
            logger.error("Exception caught: ", e);
        }
    }
}