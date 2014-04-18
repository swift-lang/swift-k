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
 * Created on Jun 9, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.globus.cog.karajan.workflow.futures.FutureIterator;
import org.globus.cog.karajan.workflow.futures.FutureList;
import org.globus.cog.karajan.workflow.futures.FutureListener;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.globus.cog.karajan.workflow.futures.ListenerStackPair;
import org.griphyn.vdl.mapping.ArrayDataNode;
import org.griphyn.vdl.mapping.DSHandle;

public class ArrayIndexFutureList implements FutureList, FutureWrapper {    
    private ArrayList<Object> keys;
    private Map<?, ?> values;
    private List<ListenerStackPair> listeners;
    private ArrayDataNode node;
    private boolean purged;

    public ArrayIndexFutureList(ArrayDataNode node, Map<?, ?> values) {
        this.node = node;
        this.values = values;
        keys = new ArrayList<Object>();
        keys.addAll(values.keySet());
    }

    public Object get(int index) {
        synchronized(node) {
            Object v = node.getValue();
            if (v instanceof RuntimeException) {
                throw (RuntimeException) v;
            }
            if (!node.isClosed() && index >= keys.size()) {
                throw new FutureNotYetAvailable(this);
            }
            else {
                Object key = keys.get(index);
                return new Pair(key, values.get(key));
            }
        }
    }

    public int available() {
        synchronized(node) {
            return keys.size();
        }
    }

    public void addKey(Object key) {
        synchronized(node) {
            keys.add(key);
        }
        notifyListeners();
    }

    public FutureIterator futureIterator() {
        return new FuturePairIterator(this);
    }

    public FutureIterator futureIterator(VariableStack stack) {
        return new FuturePairIterator(this, stack);
    }

    public void close() {
        throw new UnsupportedOperationException("Not used here");
    }

    private void purge() {
        Set<Object> allkeys = new HashSet<Object>(values.keySet());
        allkeys.removeAll(keys);
        // remaining keys must be added
        keys.addAll(allkeys);
        purged = true;
    }

    public boolean isClosed() {
        synchronized(node) {
            boolean closed = node.isClosed();
            if (closed && !purged) {
                // this is done here because no explicit close() is 
                // ever called on this object
                purge();
            }
            return closed;
        }
    }

    public Object getValue() {
        return this;
    }
    
    public DSHandle getHandle() {
        return node;
    }
    
    public void addModificationAction(FutureListener target, VariableStack stack) {
        addModificationAction(target, stack, true);
    }

    public void addModificationAction(FutureListener target, VariableStack stack, boolean partialUpdates) {
        synchronized(node) {
            if (listeners == null) {
                listeners = new LinkedList<ListenerStackPair>();
            }
            listeners.add(new ListenerStackPair(target, stack));
            WaitingThreadsMonitor.addThread(stack, node);
            if (!node.isClosed() && (keys.isEmpty() || !partialUpdates)) {
                return;
            }
        }
        // closed == true;
        notifyListeners();
    }

    public void notifyListeners() {
        List<ListenerStackPair> l;
        synchronized(node) {
            if (listeners == null) {
                return;
            }
            
            l = listeners;
            listeners = null;
        }
        
        for (final ListenerStackPair lsp : l) {
            WaitingThreadsMonitor.removeThread(lsp.stack);
            EventBus.post(new Runnable() {
                @Override
                public void run() {
                    lsp.listener.futureModified(ArrayIndexFutureList.this, lsp.stack);
                }
            });
        }
    }

    public EventTargetPair[] getListenerEvents() {
        synchronized(node) {
            if (listeners != null) {
                return listeners.toArray(new EventTargetPair[0]);
            }
            else {
                return null;
            }
        }
    }

    public int size() {
        synchronized(node) {
            if (node.isClosed()) {
                if (node.getValue() instanceof RuntimeException) {
                    throw (RuntimeException) node.getValue();
                }
                return keys.size();
            }
            else {
                throw new FutureNotYetAvailable(this);
            }
        }
    }

    public String toString() {
        String l;
        if (listeners == null) {
            l = "no listeners";
        }
        else {
            l = listeners.size() + " listeners";
        }
        if (!isClosed()) {
            return "Open, " + keys.size() + " elements, " + l;
        }
        else {
            if (listeners != null) {
                System.out.println("Badness");
            }
            return "Closed, " + keys.size() + " elements, " + l;
        }
    }

    public void fail(FutureEvaluationException e) {
        synchronized(node) {
            node.setValue(e);
        }
    }

    public FutureEvaluationException getException() {
        synchronized(node) {
            Object v = node.getValue();
            if (v instanceof FutureEvaluationException) {
                return (FutureEvaluationException) v;
            }
            else {
                return null;
            }
        }
    }

    public int listenerCount() {
        synchronized(node) {
            if (listeners == null) {
                return 0;
            }
            else {
                return listeners.size();
            }
        }
    }
}
