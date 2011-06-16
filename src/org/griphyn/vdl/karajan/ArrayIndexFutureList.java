/*
 * Created on Jun 9, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.globus.cog.karajan.workflow.futures.FutureIterator;
import org.globus.cog.karajan.workflow.futures.FutureList;
import org.globus.cog.karajan.workflow.futures.FutureListener;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.globus.cog.karajan.workflow.futures.ListenerStackPair;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DSHandleListener;

public class ArrayIndexFutureList implements FutureList, DSHandleListener {
    private ArrayList<Object> keys;
    private Map<?, ?> values;
    private boolean closed;
    private ArrayList<ListenerStackPair> listeners;
    private FutureEvaluationException exception;

    public ArrayIndexFutureList(DSHandle handle, Map<?, ?> values) {
        this.values = values;
        keys = new ArrayList<Object>();
        handle.addListener(this);
    }

    private RuntimeException notYetAvailable() {
        if (exception != null) {
            return exception;
        }
        return new FutureNotYetAvailable(this);
    }

    public Object get(int index) {
        if (exception != null) {
            throw exception;
        }
        if (!closed && index >= keys.size()) {
            throw notYetAvailable();
        }
        else {
            Object key = keys.get(index);
            return new Pair(key, values.get(key));
        }
    }

    public int available() {
        return keys.size();
    }

    public void addKey(Object key) {
        keys.add(key);
        notifyListeners();
    }

    public FutureIterator futureIterator() {
        return new FuturePairIterator(this);
    }

    public FutureIterator futureIterator(VariableStack stack) {
        return new FuturePairIterator(this, stack);
    }

    public void close() {
        synchronized(this) {
            closed = true;
            Set<Object> allkeys = new HashSet<Object>(values.keySet());
            allkeys.removeAll(keys);
            // remaining keys must be added
            keys.addAll(allkeys);
        }
        notifyListeners();
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    public Object getValue() {
        return this;
    }

    public void addModificationAction(FutureListener target,
            VariableStack stack) {
        synchronized(this) {
            if (listeners == null) {
                listeners = new ArrayList<ListenerStackPair>();
            }
    
            listeners.add(new ListenerStackPair(target, stack));
            if (!closed) {
                return;
            }
        }
        // closed
        notifyListeners();
    }

    private void notifyListeners() {
        ArrayList<ListenerStackPair> l;
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            
            l = listeners;
            listeners = null;
        }

        for (ListenerStackPair lsp : l) {
            lsp.listener.futureModified(this, lsp.stack);
        }
    }

    public EventTargetPair[] getListenerEvents() {
        return listeners.toArray(new EventTargetPair[0]);
    }

    public int size() {
        if (closed) {
            return keys.size();
        }
        else {
            throw notYetAvailable();
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
        if (!closed) {
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
        this.exception = e;
        notifyListeners();
    }

    public FutureEvaluationException getException() {
        return exception;
    }

    public void handleClosed(DSHandle handle) {
        close();
    }
}
