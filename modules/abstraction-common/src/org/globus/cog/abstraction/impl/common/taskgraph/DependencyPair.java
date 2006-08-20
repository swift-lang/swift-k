// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------



package org.globus.cog.abstraction.impl.common.taskgraph;

import org.globus.cog.abstraction.interfaces.ExecutableObject;

public class DependencyPair {
    private ExecutableObject from = null;
    private ExecutableObject to = null;

    public DependencyPair(ExecutableObject from, ExecutableObject to) {
        this.from = from;
        this.to = to;
    }

    public void setFrom(ExecutableObject from) {
        this.from = from;
    }

    public ExecutableObject getFrom() {
        return this.from;
    }

    public void setTo(ExecutableObject to) {
        this.to = to;
    }

    public ExecutableObject getTo() {
        return this.to;
    }

    public boolean equals(DependencyPair pair) {
        return (
            this.from.equals(pair.getFrom()) && this.to.equals(pair.getTo()));
    }
}
