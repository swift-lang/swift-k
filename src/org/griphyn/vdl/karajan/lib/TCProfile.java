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
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.interfaces.EnvironmentVariable;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.swift.catalog.site.Application;
import org.globus.swift.catalog.site.SwiftContact;
import org.griphyn.vdl.engine.Warnings;
import org.griphyn.vdl.karajan.Command;

public class TCProfile extends SwiftFunction {
    public static final Logger logger = Logger.getLogger(TCProfile.class);
    
    private ArgRef<SwiftContact> host;
    /**
       Allows for dynamic attributes from the SwiftScript 
       profile statements. 
       These override any other attributes. 
     */
    private ArgRef<Map<String, Object>> attributes;
    private ArgRef<Command[]> commands;
    private ArgRef<String> tr;
    
    private VarRef<Object> r_count;
    private VarRef<Object> r_jobType;
    private VarRef<Object> r_attributes;
    private VarRef<List<EnvironmentVariable>> r_environment;
    
    private static class CacheEntry {
        private ReentrantLock lock = new ReentrantLock();
        private List<EnvironmentVariable> env = null;
        private Map<String, Object> attrs = null;
        private int count;
        private String jobType;
    }
    
    private static ReentrantLock cacheLock = new ReentrantLock();
    private static Map<String, CacheEntry> cache;
    
    private enum Attr {
        COUNT, JOB_TYPE;
    }
    
    private static final Map<String, Attr> ATTR_TYPES;
    
    static {
        ATTR_TYPES = new HashMap<String, Attr>();
        ATTR_TYPES.put("count", Attr.COUNT);
        ATTR_TYPES.put("jobType", Attr.JOB_TYPE);
        
        cache = new HashMap<String, CacheEntry>();
    }

	@Override
    protected Signature getSignature() {
        return new Signature(
            params("host", optional("attributes", null), optional("commands", null), optional("tr", null)),
            returns("count", "jobType",
                "attributes", "environment")
        );
    }

    public Object function(Stack stack) {
        String tr = this.tr.getValue(stack);
		Command[] cmds = this.commands.getValue(stack);
		SwiftContact bc = this.host.getValue(stack);
		
		Map<String, Object> dynamicAttributes = this.attributes.getValue(stack);
		// The combination of a tr and site has static dynamicAttribute names.
		// In other words, all the keys are known, but not necessarily the values
		CacheEntry e = getCachedEntry(tr, cmds, bc, dynamicAttributes);
		
		combineAndSetAttributes(stack, e, dynamicAttributes);
		addEnvironment(stack, e.env);
		return null;
	}

    private CacheEntry getCachedEntry(String tr, Command[] cmds, SwiftContact bc, Map<String, Object> dynamicAttributes) {
        String key = bc.getName() + ":" + tr;
        boolean build = false;
        cacheLock.lock();
        CacheEntry e = cache.get(key);
        if (e == null) {
            e = new CacheEntry();
            cache.put(key, e);
            build = true;
        }
        e.lock.lock();
        cacheLock.unlock();
        try {
            if (build) {
                build(e, tr, cmds, bc, dynamicAttributes);
            }
        }
        finally {
            e.lock.unlock();
        }
        return e;
    }

    private void build(CacheEntry e, String tr, Command[] cmds, SwiftContact bc, Map<String, Object> dynamicAttributes) {
        Application[] apps = new Application[cmds.length];
        Set<Integer> addedEnvs = new HashSet<Integer>();
        for (int i = 0; i < cmds.length; i++) {
            Command cmd = cmds[i];
            String exec = cmd.getExecutable();
            Application app = bc.findApplication(exec);
            if (app == null) {
                if ("*".equals(tr)) {
                    continue;
                }
                else {
                    throw new RuntimeException("Application '" + exec + "' not found on site '" + bc.getName() + "'");
                }
            }
            
            List<EnvironmentVariable> appEnv = app.getEnv();
            if (appEnv != null && !appEnv.isEmpty()) {
                combineEnv(e, appEnv, addedEnvs, cmd, cmds, bc);
            }
            
            Map<String, Object> appAttrs = app.getProperties();
            if (appAttrs != null && !appAttrs.isEmpty()) {
                combineAttributes(e, app.getProperties(), cmd, cmds, bc, dynamicAttributes);
            }
            
            apps[i] = app;
        }

        
        if (e.attrs != null) {
            // convert max wall time to string, which is what's generally expected by the rest of the API
            WallTime wt = (WallTime) e.attrs.get(MAX_WALL_TIME);
            if (wt != null) {
                e.attrs.put(MAX_WALL_TIME, wt.toString());
            }
        }        
    }
    
    private static final String MAX_WALL_TIME = "maxwalltime";

    private void combineAttributes(CacheEntry cv, Map<String, Object> appAttrs, Command cmd, Command[] cmds, SwiftContact bc, 
            Map<String, Object> dynamicAttributes) {
        
        for (Map.Entry<String, Object> e : appAttrs.entrySet()) {
            if (cv.attrs == null) {
                cv.attrs = new HashMap<String, Object>();
            }
            if (e.getKey().equals(MAX_WALL_TIME)) {
                // add walltimes
                if (cv.attrs.containsKey(MAX_WALL_TIME)) {
                    cv.attrs.put(MAX_WALL_TIME, addWallTimes((WallTime) cv.attrs.get(MAX_WALL_TIME), wallTime(e.getValue(), cmd.getExecutable())));
                }
                else {
                    cv.attrs.put(MAX_WALL_TIME, wallTime(e.getValue(), cmd.getExecutable()));
                }
            }
            else if (e.getKey().equals("count")) {
                cv.count = Math.max(cv.count, toInt(e.getValue()));
            }
            else if (e.getKey().equals("jobType")) {
                cv.jobType = combineJobType(cv.jobType, e.getValue().toString());
            }
            else {
                Object old = cv.attrs.put(e.getKey(), e.getValue());
                if (old != null && !old.equals(e.getValue()) ) {
                    Warnings.warn(Warnings.Type.SITE, "Conflicting attribute \"" + e.getKey()
                        + "\" for " + getCmdWithAttr(e.getKey(), cmds, bc, 1) + " and " + cmd.getExecutable() + " on site " + bc);
                }
            }
        }
        if (dynamicAttributes != null) {
            for (String name : dynamicAttributes.keySet()) {
                if (name.equals("jobType") || name.equals("count")) {
                    continue;
                }
                if (cv.attrs == null) {
                    cv.attrs = new HashMap<String, Object>();
                }
                cv.attrs.put(name, null);
            }
        }
    }

    private String combineJobType(String jt1, String jt2) {
        if (jt2 == null) {
            return jt1;
        }
        if (jt1 == null) {
            return jt2;
        }
        if (jt1.equals("MPI") || jt1.equals("multiple")) {
            return jt1;
        }
        if (jt2.equals("MPI") || jt2.equals("multiple")) {
            return jt1;
        }
        return "single";
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        else if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        else {
            return Integer.parseInt(value.toString());
        }
    }

    private WallTime addWallTimes(WallTime a, WallTime b) {
        return new WallTime(a.getSeconds() + b.getSeconds());
    }

    private WallTime wallTime(Object walltime, String exec) {
        try {
            //validate walltime
            return new WallTime(walltime.toString());
        }
        catch (ExecutionException e) {
            Warnings.warn(Warnings.Type.SITE, "Invalid walltime specification for \"" + exec
                    + "\" (" + walltime + ").");
            return new WallTime(600);
        }
    }

    private void combineEnv(CacheEntry cv, List<EnvironmentVariable> appEnv, Set<Integer> addedEnvs, Command cmd, Command[] cmds, SwiftContact bc) {
        for (EnvironmentVariable e : appEnv) {
            if (cv.env == null) {
                cv.env = new ArrayList<EnvironmentVariable>();
            }
            Integer id = System.identityHashCode(e);
            if (!addedEnvs.contains(id)) {
                boolean added = cv.env.add(e);
                addedEnvs.add(id);
            }
        }
    }
    
    private String getCmdWithAttr(String name, Command[] cmds, SwiftContact bc, int n) {
        for (Command cmd : cmds) {
            Application app = bc.findApplication(cmd.getExecutable());
            if (app.getProperties() != null && app.getProperties().containsKey(name)) {
                if (--n == 0) {
                    return app.getExecutable() + " (value: " + app.getProperties().get(name) + ")";
                }
            }
        }
        throw new RuntimeException("Internal error in attribute lookup");
    }

    private void addEnvironment(Stack stack, List<EnvironmentVariable> env) {
	    r_environment.setValue(stack, env);
    }

    /**
	   Bring in the dynamic attributes from the Karajan stack 
	   @return Map, may be null
	 */
	private Map<String, Object> readDynamicAttributes(Stack stack) {
		return this.attributes.getValue(stack);
	}

	/**
	 * Combine attributes creating result as necessary
	 */
	private void combineAndSetAttributes(Stack stack, CacheEntry e, Map<String, Object> dynamicAttrs) {
	    Object jobType = null;
	    Object count = null;
	    int nDynAttrs = 0;
	    if (dynamicAttrs != null) {
	        nDynAttrs = dynamicAttrs.size();
	        jobType = dynamicAttrs.get("jobType");
	        if (jobType != null) {
	            nDynAttrs--;
	        }
	        count = dynamicAttrs.get("count");
	        if (count != null) {
	            nDynAttrs--;
	        }
	    }
	    if (jobType == null) {
	        jobType = e.jobType;
	    }
	    if (count == null && e.count > 0) {
	        count = e.count;
	    }
	    if (jobType != null) {
	        setAttr(Attr.JOB_TYPE, stack, jobType);
	    }
	    if (count != null) {
	        setAttr(Attr.COUNT, stack, count);
	    }
	    if (nDynAttrs == 0) {
	        this.r_attributes.setValue(stack, e.attrs);
	    }
	    else if (e.attrs != null) {
	        this.r_attributes.setValue(stack, new FallBackMap<String, Object>(dynamicAttrs, e.attrs));
	    }
	    else {
	        this.r_attributes.setValue(stack, null);
	    }
	}
			
	private void setAttributes(Stack stack, Map<String, Object> attrs) {
	    this.r_attributes.setValue(stack, attrs);
	}

    private void setAttr(Attr a, Stack stack, Object value) {
        switch (a) {
            case COUNT:
                r_count.setValue(stack, value);
                break;
            case JOB_TYPE:
                r_jobType.setValue(stack, value);
                break;
        }
    }
    
    private static class FallBackMap<K, V> implements Map<K, V> {
        private Map<K, V> m;
        private Map<K, V> fallback;
        
        public FallBackMap(Map<K, V> m, Map<K, V> fallback) {
            this.m = m;
            this.fallback = fallback;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
            return fallback.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return new Set<Entry<K, V>>() {

                @Override
                public boolean add(Entry<K, V> e) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean addAll(Collection<? extends Entry<K, V>> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean containsAll(Collection<?> arg0) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean isEmpty() {
                    return fallback.isEmpty();
                }

                @Override
                public Iterator<Entry<K, V>> iterator() {
                    final Iterator<Entry<K, V>> it = fallback.entrySet().iterator();
                    return new Iterator<Entry<K, V>>() {
                        @Override
                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        @Override
                        public Entry<K, V> next() {
                            final Entry<K, V> e = it.next();
                            return new Entry<K, V>() {
                                @Override
                                public K getKey() {
                                    return e.getKey();
                                }

                                @Override
                                public V getValue() {
                                    V v = m.get(e.getKey());
                                    if (v == null) {
                                        return e.getValue();
                                    }
                                    else {
                                        return v;
                                    }
                                }

                                @Override
                                public V setValue(V value) {
                                    throw new UnsupportedOperationException();
                                }
                            };
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                @Override
                public boolean remove(Object k) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public int size() {
                    return fallback.size();
                }

                @Override
                public Object[] toArray() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public <T> T[] toArray(T[] arg0) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public V get(Object key) {
            V v = m.get(key);
            if (v == null) {
                return fallback.get(key);
            }
            else {
                return v;
            }
        }

        @Override
        public boolean isEmpty() {
            return fallback.isEmpty();
        }

        @Override
        public Set<K> keySet() {
            return fallback.keySet();
        }

        @Override
        public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return fallback.size();
        }

        @Override
        public Collection<V> values() {
            throw new UnsupportedOperationException();
        }
    }
}
