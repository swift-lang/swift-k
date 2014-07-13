/*
 * Created on Sep 14, 2012
 */
package org.griphyn.vdl.mapping;

import java.util.Collection;

import org.globus.cog.karajan.util.Pair;
import org.griphyn.vdl.karajan.lib.Tracer;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;


public abstract class MappingParamSet {
    
    public void set(String name, Object value) {
        try {
            if (!set0(name, value)) {
                throw new IllegalArgumentException("Unsupported parameter: '" + name + "'");
            }
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid class (" + 
                value.getClass().getName() + ") for parameter '" + name + "'");
        }
    }

    protected boolean set0(String name, Object value) {
        return false;
    }
    
    public AbstractDataNode getFirstOpen() {
        return null;
    }
    
    public abstract Collection<String> getNames();
    
    public void toString(StringBuilder sb) {
    }
    
    protected void addParam(StringBuilder sb, String name, Object value) {
        if (sb.length() > 2 && sb.charAt(sb.length() - 2) == ';') {
            // first param
        }
        else {
            sb.append(", ");
        }
        sb.append(name);
        sb.append("=");
        sb.append(Tracer.unwrapHandle(value));
    }
    
    protected boolean checkOpen(Object v) {
        if (v instanceof AbstractDataNode && !((AbstractDataNode) v).isClosed()) {
            return true;
        }
        else {
            return false;
        }
    }

    public void unwrapPrimitives() {
    }
        
    /** 
     * Unwraps objects stored in AbstractDataNodes 
     */
    public Object unwrap(Object value, Class<?> expected) {
        if (value instanceof AbstractDataNode) {
            AbstractDataNode handle = (AbstractDataNode) value;
            /*
             *  TODO The semantics here (and in the mapper initialization process)
             *  are broken. If an array is passed, the code in RootHandle.innerInit
             *  only waits for the array, but not for each of the elements in the 
             *  array, while at the same time mappers expect all elements of the
             *  array to be closed (e.g. ArrayFileMapper.map(): assert(dn.isClosed()))
             */
            if (!handle.isPrimitive()) {
                throw new IllegalArgumentException("Cannot unwrap non-primitive data");
            }
            Object x = handle.getValue();
            if (expected == Boolean.class && x instanceof String) {
                x = Boolean.parseBoolean((String) x);
            }
            else if (expected == Integer.class && x instanceof String) {
                x = Integer.parseInt((String) x);
            }
            return x;
        }
        else {
            return value;
        }
    }
        
    public void setAll(GenericMappingParamSet m) {
        if (m != null) {
            for (Pair<String, Object> e : m.getParams()) {
                set0(e.s, e.t);
            }
        }
    }

    private void append(StringBuilder sb, String name, Object value) {
        sb.append(name);
        sb.append(" = ");
        sb.append(value);
    }
}
