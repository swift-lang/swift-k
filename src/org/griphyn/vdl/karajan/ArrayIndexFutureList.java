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

import k.rt.FutureListener;

import org.globus.cog.karajan.futures.FutureEvaluationException;
import org.globus.cog.karajan.futures.FutureIterator;
import org.globus.cog.karajan.futures.FutureList;
import org.globus.cog.karajan.futures.FutureNotYetAvailable;
import org.griphyn.vdl.mapping.ArrayDataNode;
import org.griphyn.vdl.mapping.DSHandle;

public class ArrayIndexFutureList implements FutureList, FutureWrapper {    
    private ArrayList<Object> keys;
    private Map<?, ?> values;
    private LinkedList<FutureListener> listeners;
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

    @Override
    public void addListener(FutureListener l) {
        boolean closed;
        synchronized(this) {
            if (listeners == null) {
                listeners = new LinkedList<FutureListener>();
            }
            listeners.add(l);
            closed = isClosed();
        }
        if (closed) {
            notifyListeners();
        }
    }
    
    public void notifyListeners() {
        List<FutureListener> ls;
        synchronized(this) {
            if (listeners == null) {
                return;
            }
            ls = listeners;
            listeners = null;
        }
        for (FutureListener l : ls) {
            l.futureUpdated(this);
        }
    }
    
    @Override
    public synchronized List<FutureListener> getListeners() {
        return new LinkedList<FutureListener>(listeners);
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
