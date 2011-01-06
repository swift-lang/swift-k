// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.queue;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphImpl;
import org.globus.cog.abstraction.interfaces.Dependency;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Queue;

public class QueueImpl extends TaskGraphImpl implements Queue {
    private LinkedList queue = null;

    public QueueImpl() {
        super();

        // Maintains a mapping of queue dependencies
        this.queue = new LinkedList();
    }

    public QueueImpl(Identity id) {
        super(id);

        // Maintains a mapping of queue dependencies
        this.queue = new LinkedList();
    }

    public void add(ExecutableObject node) throws Exception {
        try {
            ExecutableObject prev = (ExecutableObject) this.queue.getLast();
            super.addDependency(prev, node);
        } catch (NoSuchElementException nsee) {
            // dont add any dependency since this is the first node
        }

        this.queue.addLast(node);
        super.add(node);
    }

    public ExecutableObject remove(Identity id) {
        ExecutableObject givenNode = this.get(id);
        ListIterator iterator = this.queue.listIterator();
        while (iterator.hasNext()) {
            ExecutableObject eo = (ExecutableObject) iterator.next();
            if (givenNode.equals(eo)) {
                this.queue.remove(eo);
                break;
            }
        }

        ExecutableObject node = super.remove(id);
        return node;
    }

    public void setDependency(Dependency dependency) {
        // N/A
    }

    public Dependency getDependency() {
        return super.getDependency();
    }

    public void addDependency(ExecutableObject from, ExecutableObject to) {
        // N/A
    }

    public boolean removeDependency(ExecutableObject from, ExecutableObject to) {
        // N/A
        return false;
    }

}