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

package org.globus.cog.abstraction.examples.xml;

import java.io.File;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphImpl;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskGraph;
import org.globus.cog.abstraction.tools.transfer.FileTransfer;
import org.globus.cog.abstraction.xml.TaskGraphMarshaller;

public class TaskGraph2XML {
    static Logger logger = Logger.getLogger(TaskGraph2XML.class.getName());
    protected org.globus.cog.abstraction.interfaces.TaskGraph taskGraph;
    protected boolean active = false;

    public TaskGraph2XML() throws Exception {
        createDAG();
        marshalDAG();
    }

    public void createDAG() throws Exception {
        try {
            TaskGraph tg1 = new TaskGraphImpl();
            tg1.setName("TG1");
            Task task1 = prepareTask("Task1",
                    "gridftp://hot.anl.gov:2811//home/cog/gridfile1",
                    "gridftp://co;d.anl.gov:2811//home/cog/gridfile1");
            Task task2 = prepareTask("Task2",
                    "gridftp://hot.anl.gov:2811//home/cog/gridfile2",
                    "gridftp://co;d.anl.gov:2811//home/cog/gridfile2");
            tg1.add(task1);
            tg1.add(task2);
            tg1.addDependency(task1, task2);

            TaskGraph tg2 = new TaskGraphImpl();
            tg2.setName("TG2");
            Task task3 = prepareTask("Task3",
                    "gridftp://hot.anl.gov:2811//home/cog/gridfile3",
                    "gridftp://co;d.anl.gov:2811//home/cog/gridfile3");
            Task task4 = prepareTask("Task4",
                    "gridftp://hot.anl.gov:2811//home/cog/gridfile4",
                    "gridftp://co;d.anl.gov:2811//home/cog/gridfile4");
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
                    "gridftp://hot.anl.gov:2811//home/cog/gridfile5",
                    "gridftp://co;d.anl.gov:2811//home/cog/gridfile5");
            tg4.add(tg3);
            tg4.add(task5);
            tg4.addDependency(tg3, task5);

            TaskGraph tg5 = new TaskGraphImpl();
            Task task6 = prepareTask("Task6",
                    "gridftp://hot.anl.gov:2811//home/cog/gridfile6",
                    "gridftp://co;d.anl.gov:2811//home/cog/gridfile6");
            Task task7 = prepareTask("Task7",
                    "gridftp://hot.anl.gov:2811//home/cog/gridfile7",
                    "gridftp://co;d.anl.gov:2811//home/cog/gridfile7");
            Task task8 = prepareTask("Task8",
                    "gridftp://hot.anl.gov:2811//home/cog/gridfile8",
                    "gridftp://co;d.anl.gov:2811//home/cog/gridfile8");
            Task task9 = prepareTask("Task9",
                    "gridftp://hot.anl.gov:2811//home/cog/gridfile10",
                    "gridftp://co;d.anl.gov:2811//home/cog/gridfile10");
            tg5.add(task6);
            tg5.add(task7);
            tg5.add(task8);
            tg5.add(task9);
            tg5.add(tg4);
            tg5.addDependency(task6, task7);
            tg5.addDependency(task7, task8);
            tg5.addDependency(task7, task9);
            tg5.addDependency(task8, tg4);
            tg5.addDependency(task9, tg4);

            this.taskGraph = tg5;
            this.taskGraph.setName("Main Graph");
        } catch (Exception e) {
            logger.error("Cannot create DAG", e);
        }
    }

    public void marshalDAG() {
        try {
            //		Create a File to marshal to
            File xmlFile = new File("TaskGraph.xml");

            //		Marshal the task graph object
            TaskGraphMarshaller.marshal(this.taskGraph, xmlFile);
        } catch (Exception e) {
            logger.error("Cannot masrshal the task graph", e);
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

    public static void main(String arg[]) {
        try {
            new TaskGraph2XML();
        } catch (Exception e) {
            logger.error("Exception caught: ", e);
        }
    }
}