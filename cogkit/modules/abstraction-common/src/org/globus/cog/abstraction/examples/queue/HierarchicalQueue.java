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

package org.globus.cog.abstraction.examples.queue;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.queue.QueueHandlerImpl;
import org.globus.cog.abstraction.impl.common.queue.QueueImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Queue;
import org.globus.cog.abstraction.interfaces.QueueHandler;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.tools.transfer.FileTransfer;

/*
 * This class serves as an example to demonstrate the execution of hierarchical
 * queue. It simply shows the semantics of how a user can create a hierarchy of
 * queue. The parameters to the task specification MUST be changed to suite your
 * requirements and environment.
 */
public class HierarchicalQueue implements StatusListener {
    static Logger logger = Logger.getLogger(HierarchicalQueue.class.getName());
    protected Queue queue = null;
    protected boolean active = false;
    protected Task task7;

    public HierarchicalQueue() {
        this.queue = new QueueImpl();
    }

    public void createQueue() throws Exception {
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

        Queue innerQueue = new QueueImpl();
        innerQueue.setName("InnerQueue");
        try {
            innerQueue.add(task6);
            innerQueue.add(task7);

            this.queue.add(task1);
            this.queue.add(task2);
            this.queue.add(task3);
            this.queue.add(task4);
            this.queue.add(task5);
            this.queue.add(innerQueue);
        } catch (Exception e) {
        }
        this.queue.addStatusListener(this);
    }

    public void submitQueue() {
        QueueHandler handler = new QueueHandlerImpl();
        try {
            handler.submit(this.queue);
            logger.debug("Queue submitted");
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
            logger.info("Queue  Done");
            System.exit(1);
        }
    }

    public static void main(String arg[]) {
        try {
            HierarchicalQueue q = new HierarchicalQueue();
            q.createQueue();
            q.submitQueue();
        } catch (Exception e) {
            logger.error("Exception caught:", e);
        }
    }
}