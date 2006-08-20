// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.broker.impl;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.broker.interfaces.Dispatcher;
import org.globus.cog.broker.interfaces.Queue;

public class FCFSDispatcher implements Dispatcher {
    static Logger logger = Logger.getLogger(FCFSDispatcher.class.getName());
    private Queue queue = null;
    private Hashtable attributes = null;
    private boolean reset = true;
    private int index = 0;

    // FCFS Dispatcher

    public FCFSDispatcher(Queue queue) {
        this.queue = queue;
        this.attributes = new Hashtable();
    }

    private Task getFirst() {
        synchronized (this.queue) {
            if (this.queue == null) {
                return null;
            } else {
                // reset the index to 0
                this.index = 0;
                Task task = null;
                while (true) {
                    try {
                        task = (Task) this.queue.get(index);
                    } catch (Exception e) {
                        //	IndexOutOfBoundException
                        this.index = 0;
                        return null;
                    }
                    String isBrokered = (String) task
                            .getAttribute("isBrokered");
                    if (isBrokered == null
                            || !isBrokered.equalsIgnoreCase("true")) {
                        index++;
                        return task;
                    }
                    index++;
                }
            }
        }
    }

    public Task getNext() {
        if (reset) {
            reset = false;
            return getFirst();
        } else {
            return next();
        }
    }

    private Task next() {
        synchronized (this.queue) {
            if (this.queue == null) {
                return null;
            } else {
                Task task = null;
                while (true) {
                    try {
                        task = (Task) this.queue.get(index);
                    } catch (Exception e) {
                        //IndexOutOfBoundException
                        this.index = 0;
                        return null;
                    }
                    String isBrokered = (String) task
                            .getAttribute("isBrokered");
                    if (isBrokered == null
                            || !isBrokered.equalsIgnoreCase("true")) {
                        index++;
                        return task;
                    }
                    index++;
                }
            }
        }
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public Enumeration getAllAttributes() {
        return this.attributes.keys();
    }

    public void reset() {
        this.reset = true;
        logger.debug("Dispatcher reset");
    }

}