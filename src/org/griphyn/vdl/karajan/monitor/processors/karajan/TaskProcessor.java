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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Level;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.util.StringCache;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.items.ApplicationState;
import org.griphyn.vdl.karajan.monitor.items.Bridge;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
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
                else if (p.matchAndSkip("JOB_TASK ")) {
                    p.skip("jobid=");
                    String jobid = p.word();
                    p.skip("taskid=");
                    id = p.word();
                    p.skip("exec=");
                    String exec = p.word();
                    p.skip("dir=");
                    String dir = p.word();
                    p.skip("args=");
                    String args = p.remaining();
                    
                    ti = new TaskItem(id, Task.JOB_SUBMISSION);
                    Task t = new TaskImpl();
                    t.setType(Task.JOB_SUBMISSION);
                    JobSpecification spec = new JobSpecificationImpl();
                    spec.setExecutable(StringCache.intern(exec));
                    
                    List<String> argsl = new ArrayList<String>();
                    StringTokenizer st = new StringTokenizer(args);
                    while (st.hasMoreTokens()) {
                        argsl.add(StringCache.intern(st.nextToken()));
                    }
                    spec.setArguments(argsl);
                    spec.setDirectory(StringCache.intern(dir));
                    t.setSpecification(spec);
                    ti.setTask(t);
                    updateParent(state, jobid, id, ti);
                    state.addItem(ti);
                }
                else if (p.matchAndSkip("TASK_STATUS_CHANGE ")) {
                    p.skip("taskid=");
                    id = p.word();
                    p.skip("status=");
                    taskState = Integer.parseInt(p.word());
                    ti = (TaskItem) state.getItemByID(id, StatefulItemClass.TASK);
                    if (p.matchAndSkip("workerid=")) {
                        ti.setWorkerId(p.word());
                    }
                    ti.setStatus(taskState);
                    state.itemUpdated(ti);
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
                        updateParent(state, null, id, ti);
                        state.addItem(ti);
                    }
                }
            }
            catch (ParsingException e) {
                e.printStackTrace();
            }
        }
        if (ti != null && ti.getParent() != null && taskState != 0) {
            ApplicationItem app = (ApplicationItem) ti.getParent();
            switch (taskState) {
                //handled in AppStartProcessor
                //case Status.SUBMITTING:
                case Status.SUBMITTED:
                case Status.ACTIVE:
                case Status.STAGE_IN:
                case Status.STAGE_OUT:
                    app.setState(getAppStateFromTaskState(taskState), state.getCurrentTime());
                    app.setWorkerId(ti.getWorkerId());
                    state.itemUpdated(app);
                    break;
            }
        }
    }

    private void updateParent(SystemState state, String jobid, String id, TaskItem ti) {
        if (jobid != null) {
            ApplicationItem.QualifiedID qid = ApplicationItem.parseId(jobid);
            StatefulItem app = state.getItemByID(qid.id, StatefulItemClass.APPLICATION);
            Bridge bridge = new Bridge(id);
            bridge.setParent(app);
            ti.setParent(bridge);
            state.addItem(bridge);
            app.addChild(bridge);
            return;
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
    }

    private ApplicationState getAppStateFromTaskState(int taskState) {
        switch (taskState) {
            case Status.SUBMITTING:
                return ApplicationState.SUBMITTING;
            case Status.SUBMITTED:
                return ApplicationState.SUBMITTED;
            case Status.ACTIVE:
                return ApplicationState.ACTIVE;
            case Status.STAGE_IN:
                return ApplicationState.STAGE_IN;
            case Status.STAGE_OUT:
                return ApplicationState.STAGE_OUT;
            default:
                return ApplicationState.INITIALIZING;
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
