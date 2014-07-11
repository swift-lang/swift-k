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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

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
        
        public Object get() {
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
        
        private void toString(StringBuilder sb, int level, String k) {
            for (int i = 0; i < level; i++) {
                sb.append('\t');
            }
            if (nodes == null || nodes.isEmpty()) {
                if (value != null) {
                    sb.append(k);
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
            else if (nodes.size() == 1) {
                String key = nodes.keySet().iterator().next();
                if (k == null) {
                    nodes.values().iterator().next().toString(sb, 0, key);
                }
                else {
                    nodes.values().iterator().next().toString(sb, 0, k + "." + key);
                }
            }
            else {
                if (k != null) {
                    sb.append(k);
                    sb.append(' ');
                }
                sb.append("{\n");
                for (Map.Entry<String, Node<T>> e : nodes.entrySet()) {
                    e.getValue().toString(sb, level + 1, e.getKey());
                }
                for (int i = 0; i < level; i++) {
                    sb.append('\t');
                }
                sb.append("}\n");
            }
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
        List<String> l = new ArrayList<String>();
        root.getLeafPaths(l, null);
        return l;
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
        StringBuilder sb = new StringBuilder();
        root.toString(sb, 0, null);
        return sb.toString();
    }
}
