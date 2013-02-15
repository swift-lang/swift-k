// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



package org.globus.cog.abstraction.impl.common.task;

import org.globus.cog.abstraction.interfaces.Task;

public class OutputEvent {

    private Task source = null;
    private String output = null;

    public OutputEvent(Task source, String output) {
        this.source = source;
        this.output = output;
    }

    public void setSource(Task source) {
        this.source = source;
    }

    public Task getSource() {
        return this.source;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getOutput() {
        return this.output;
    }
}
