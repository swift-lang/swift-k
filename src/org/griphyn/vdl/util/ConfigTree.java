/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 6, 2014
 */
package org.griphyn.vdl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

public class ConfigTree<T> {
    
    public static class Node<T> {
        private Map<String, Node<T>> nodes;
        private T value;
        
        protected void checkEmpty(String k) {
            if (k.isEmpty()) {
                throw new IllegalArgumentException();
            }
        }
        
        protected String first(String k) {
            int ix = k.indexOf('.');
            if (ix == -1) {
                return k;
            }
            else {
                return k.substring(0, ix);
            }
        }
        
        protected String rest(String k) {
            int ix = k.indexOf('.');
            if (ix == -1) {
                return "";
            }
            else {
                return k.substring(ix + 1);
            }
        }
        
        public T get() {
            return value;
        }
        
        public T get(String k) {
            return get(k, null);
        }
        
        public T get(String k, String wildcard) {
            if (k.isEmpty()) {
                return value;
            }
            if (nodes == null || nodes.isEmpty()) {
                throw new NoSuchElementException();
            }
            Node<T> t = nodes.get(first(k));
            if (t == null && wildcard != null) {
                t = nodes.get(wildcard);
            }
            if (t == null) {
                throw new NoSuchElementException();
            }
            return t.get(rest(k), wildcard);
        }
        
        public boolean hasKey(String k) {
            if (k.isEmpty()) {
                return value != null;
            }
            if (nodes == null || nodes.isEmpty()) {
                return false;
            }
            Node<T> t = nodes.get(first(k));
            if (t == null) {
                return false;
            }
            return t.hasKey(rest(k));
        }

        public T put(String k, T v) {
            if (k.isEmpty()) {
                return set(v);
            }
            else {
                if (nodes == null) {
                    nodes = new HashMap<String, Node<T>>();
                }
                String first = first(k);
                if (k == first) {
                    return getOrCreateTree(k).set(v);
                }
                else {
                    return getOrCreateTree(first).put(rest(k), v);
                }
            }
        }

        private Node<T> getOrCreateTree(String k) {
            Node<T> t = nodes.get(k);
            if (t == null) {
                t = new Node<T>();
                nodes.put(k, t);
                return t;
            }
            else {
                return t;
            }
        }

        public void getLeafPaths(List<String> l, String partial) {
            if (nodes != null) {
                for (Map.Entry<String, Node<T>> e : nodes.entrySet()) {
                    if (partial == null) {
                        e.getValue().getLeafPaths(l, e.getKey());
                    }
                    else {
                        e.getValue().getLeafPaths(l, partial + "." + e.getKey());
                    }
                }
            }
            else {
                l.add(partial);
            }
        }
        
        public void expandWildcards(List<String> l, String k, String wildcard, String partial) {
            if (nodes == null || nodes.isEmpty()) {
                if (k.isEmpty()) {
                    l.add(partial);
                }
                else {
                    throw new IllegalArgumentException("No such path: " + partial + "." + k);
                }
                return;
            }
            String mk = first(k);
            if (mk.equals(wildcard)) {
                for (Map.Entry<String, Node<T>> e : nodes.entrySet()) {
                    Node<T> n = e.getValue();
                    String rest = rest(k);
                    String p;
                    if (partial == null) {
                        p = e.getKey();
                    }
                    else {
                        p = partial + "." + e.getKey();
                    }
                    n.expandWildcards(l, rest, wildcard, p);
                }
            }
            else {
                Node<T> t = nodes.get(mk);
                if (t == null) {
                    if (first(rest(k)).equals(wildcard)) {
                        // x.* is allowed to not be there
                        return;
                    }
                    if (partial == null || k.equals("")) {
                        l.add(k);
                    }
                    else {
                        l.add(partial + "." + k);
                    }
                    return;
                }
                String rest = rest(k);
                String p;
                if (partial == null) {
                    p = mk;
                }
                else {
                    p = partial + "." + mk;
                }   
                t.expandWildcards(l, rest, wildcard, p);
            }
        }
        
        public T set(T v) {
            T old = value;
            value = v;
            return old;
        }

        public Set<Map.Entry<String, Node<T>>> entrySet() {
            if (nodes == null) {
                Map<String, Node<T>> empty = Collections.emptyMap();
                return empty.entrySet();
            }
            else {
                return nodes.entrySet();
            }
        }
        
        private void toString(StringBuilder sb, int level, String k, String full, boolean sort, ValueFormatter f) {
            if (nodes == null || nodes.isEmpty()) {
                f.format(k, full, value, level, sb);
            }
            else if (nodes.size() == 1) {
                String key = nodes.keySet().iterator().next();
                if (k == null) {
                    nodes.values().iterator().next().toString(sb, level, key, cat(full, key), sort, f);
                }
                else {
                    String nkey = cat(k, key);
                    nodes.values().iterator().next().toString(sb, level, nkey, cat(full, key), sort, f);
                }
            }
            else {
                for (int i = 0; i < level; i++) {
                    sb.append('\t');
                }
                if (k != null) {
                    sb.append(k);
                    sb.append(' ');
                }
                sb.append("{\n");
                Collection<String> keys;
                if (sort) {
                    keys = new TreeSet<String>(nodes.keySet());
                }
                else {
                    keys = nodes.keySet();
                }
                for (String key : keys) {
                    nodes.get(key).toString(sb, level + 1, key, cat(full, key), sort, f);
                }
                for (int i = 0; i < level; i++) {
                    sb.append('\t');
                }
                sb.append("}\n");
            }
        }

        private String cat(String full, String key) {
            if (full == null) {
                return key;
            }
            else {
                return full + "." + key;
            }
        }

        public List<String> getLeafPaths() {
            List<String> l = new ArrayList<String>();
            getLeafPaths(l, null);
            return l;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb, 0, null, null, false, DEFAULT_VALUE_FORMATTER);
            return sb.toString();
        }
    }
    
    private Node<T> root;
    
    public ConfigTree() {
        root = new Node<T>();
    }

    public T get(String k) {
        return get(k, null);
    }

    public T put(String k, T v) {
        return root.put(k, v);
    }

    public T get(String k, String wildcard) {
        try {
            return root.get(k, wildcard);
        }
        catch (IllegalArgumentException e) {
            throw new NoSuchElementException("Not a leaf: " + k);
        }
        catch (NoSuchElementException e) {
            return null;
        }
    }

    public List<String> getLeafPaths() {
        return root.getLeafPaths();
    }

    /**
     * Find all paths matching the given path. Wildcards are expanded based
     * on what's in the tree, but the full paths do not need to exist in the tree.
     * 
     * So if a.1.b.2 and a.1.b.3 were in the tree, a.*.b.*.c would generate
     * a.1.b.2.c and a.1.b.3.c
     * 
     */
    public List<String> expandWildcards(String key, String wildcard) {
        List<String> l = new ArrayList<String>();
        root.expandWildcards(l, key, wildcard, null);
        return l;
    }

    public boolean hasKey(String k) {
        return root.hasKey(k);
    }

    public Set<Map.Entry<String, Node<T>>> entrySet() {
        return root.entrySet();
    }
    
    public String toString() {
        return toString(false, DEFAULT_VALUE_FORMATTER);
    }
    
    public String toString(boolean sort, ValueFormatter f) {
        StringBuilder sb = new StringBuilder();
        root.toString(sb, 0, null, null, sort, f);
        return sb.toString();
    }
    
    public interface ValueFormatter {
        void format(String key, String full, Object value, int indentationLevel, StringBuilder sb);
    }
    
    public static class DefaultValueFormatter implements ValueFormatter {
        @Override
        public void format(String key, String full, Object value, int indentationLevel, StringBuilder sb) {
            for (int i = 0; i < indentationLevel; i++) {
                sb.append('\t');
            }
            if (value != null) {
                sb.append(key);
                sb.append(": ");
                if (value instanceof String) {
                    sb.append('\"');
                    sb.append(value);
                    sb.append('\"');
                }
                else {
                    sb.append(value);
                }
                sb.append('\n');
            }
        }
    }
    
    public static final ValueFormatter DEFAULT_VALUE_FORMATTER = new DefaultValueFormatter();
}
