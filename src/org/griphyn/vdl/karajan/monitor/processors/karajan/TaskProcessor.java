/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.processors.karajan;

import org.apache.log4j.Level;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.ApplicationState;
import org.griphyn.vdl.karajan.monitor.items.Bridge;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.TaskItem;
import org.griphyn.vdl.karajan.monitor.processors.AbstractMessageProcessor;
import org.griphyn.vdl.karajan.monitor.processors.ParsingException;
import org.griphyn.vdl.karajan.monitor.processors.SimpleParser;

public class TaskProcessor extends AbstractMessageProcessor {

    public Level getSupportedLevel() {
        return Level.DEBUG;
    }

    public Class<?> getSupportedSource() {
        return org.globus.cog.karajan.compiled.nodes.grid.AbstractGridNode.class;
    }

    public void processMessage(SystemState state, Object message, Object details) {
        String id = null;
        TaskItem ti = null;
        int taskType;
        int taskState = 0;
        if (message instanceof Task) {
            Task task = (Task) message;
            id = task.getIdentity().toString();
            ti = new TaskItem(id, task);
            state.addItem(ti);
            taskType = task.getType();
            switch (taskType) {
                case Task.JOB_SUBMISSION:
                    state.getStats("jobs").add();
                    break;
                case Task.FILE_OPERATION:
                    state.getStats("fops").add();
                    break;
                case Task.FILE_TRANSFER:
                    state.getStats("transfers").add();
                    break;
            }
        }
        else {
            SimpleParser p = new SimpleParser(String.valueOf(message));
            try {
                if (p.matchAndSkip("Task status changed ")) {
                    id = p.word();
                    try {
                        taskState = Integer.parseInt(p.word());
                    }
                    catch (Exception e) {
                        return;
                    }
                    ti = (TaskItem) state.getItemByID(id, StatefulItemClass.TASK);
                    if (ti != null) {
                        if (taskState == Status.COMPLETED
                                || taskState == Status.FAILED) {
                            switch (ti.getType()) {
                                case Task.JOB_SUBMISSION:
                                    state.getStats("jobs").remove();
                                    break;
                                case Task.FILE_OPERATION:
                                    state.getStats("fops").remove();
                                    break;
                                case Task.FILE_TRANSFER:
                                    state.getStats("transfers").remove();
                                    break;
                            }
                            state.removeItem(ti);
                        }
                        else {
                            ti.setStatus(taskState);
                            state.itemUpdated(ti);
                        }
                    }
                }
                else if (p.matchAndSkip("Task(")) {
                    p.skip("type=");
                    p.beginToken();
                    p.markTo(",");
                    p.endToken();
                    String type = p.getToken();
                    taskType = getTypeFromString(type);
                    p.skip("identity=");
                    p.beginToken();
                    p.markTo(")");
                    p.endToken();
                    id = p.getToken();
                    if (state.getItemByID(id, StatefulItemClass.TASK) != null) {
                        return;
                    }
                    else {
                        ti = new TaskItem(id, taskType);
                        state.addItem(ti);
                    }
                }
            }
            catch (ParsingException e) {
                e.printStackTrace();
            }
        }
        if (ti != null && id != null && ti.getParent() == null) {
            int bi = id.indexOf(':');
            int li = id.lastIndexOf('-');
            if (li == -1 || bi == -1 || bi > li) {
                return;
            }
            String threadid = id.substring(bi + 1, li);
            Bridge bridge = (Bridge) state.find(threadid, StatefulItemClass.BRIDGE);
            if (bridge != null) {
                ti.setParent(bridge);
                bridge.addChild(ti);
            }
        }
        if (ti != null && ti.getParent() != null && taskState != 0) {
            ApplicationItem app = (ApplicationItem) ti.getParent();
            if (taskState == Status.SUBMITTING) {
                app.setState(ApplicationState.SUBMITTING, state.getCurrentTime());
                state.itemUpdated(app);
            }
            else if (taskState == Status.SUBMITTED) {
                app.setState(ApplicationState.SUBMITTED, state.getCurrentTime());
                state.itemUpdated(app);
            }
            else if (taskState == Status.ACTIVE) {
                app.setState(ApplicationState.ACTIVE, state.getCurrentTime());
                state.itemUpdated(app);
            }
        }
    }

    private int getTypeFromString(String type) {
        if ("JOB_SUBMISSION".equals(type)) {
            return Task.JOB_SUBMISSION;
        }
        if ("FILE_TRANSFER".equals(type)) {
            return Task.FILE_TRANSFER;
        }
        if ("FILE_OPERATION".equals(type)) {
            return Task.FILE_OPERATION;
        }
        return -1;
    }
}
