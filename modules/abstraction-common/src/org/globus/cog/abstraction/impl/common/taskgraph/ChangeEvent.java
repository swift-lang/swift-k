// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.taskgraph;

import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.TaskGraph;

public class ChangeEvent {
    public static final int ADD = 1;
    public static final int REMOVE = 2;

    private TaskGraph source = null;
    private ExecutableObject node = null;
    private int type = 0;

    public ChangeEvent(TaskGraph source, ExecutableObject node, int type) {
        this.source = source;
        this.node = node;
        this.type = type;
    }

    public void setSource(TaskGraph source) {
        this.source = source;
    }

    public TaskGraph getSource() {
        return this.source;
    }

    public void setNode(ExecutableObject node) {
        this.node = node;
    }

    public ExecutableObject getNode() {
        return this.node;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
}
