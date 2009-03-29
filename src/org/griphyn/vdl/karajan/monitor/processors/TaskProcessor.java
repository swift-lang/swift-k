/*
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.processors;

import org.apache.log4j.Level;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItem;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.TaskItem;

public class TaskProcessor implements LogMessageProcessor {

    public Object getSupportedCategory() {
        return Level.DEBUG;
    }

    public String getSupportedSource() {
        return "org.globus.cog.karajan.workflow.nodes.grid.AbstractGridNode";
    }

    public void processMessage(SystemState state, Object message, Object details) {
        String id = null;
        TaskItem ti = null;
        if (message instanceof Task) {
            Task task = (Task) message;
            id = task.getIdentity().toString();
            ti = new TaskItem(id, task);
            state.addItem(ti);
            switch (task.getType()) {
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
                    int status;
                    try {
                        status = Integer.parseInt(p.word());
                    }
                    catch (Exception e) {
                        return;
                    }
                    TaskItem si = (TaskItem) state.getItemByID(id,
                        StatefulItemClass.TASK);
                    if (si != null) {
                        if (status == Status.COMPLETED
                                || status == Status.FAILED) {
                            switch (si.getTask().getType()) {
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
                            state.removeItem(si);
                        }
                        else {
                            ((TaskItem) si).setStatus(status);
                            state.itemUpdated(si);
                        }
                    }
                }
                else if (p.matchAndSkip("Task(")) {
                    p.skip("type=");
                    p.beginToken();
                    p.markTo(",");
                    p.endToken();
                    String type = p.getToken();
                    p.skip("identity=");
                    p.beginToken();
                    p.markTo(")");
                    p.endToken();
                    id = p.getToken();
                    if (state.getItemByID(id, StatefulItemClass.TASK) != null) {
                        return;
                    }
                    else {
                        ti = new TaskItem(id);
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
            StatefulItem thread = state
                .find(threadid, StatefulItemClass.BRIDGE);
            if (thread != null) {
                ti.setParent(thread);
                thread.addChild(ti);
            }
        }
    }
}
