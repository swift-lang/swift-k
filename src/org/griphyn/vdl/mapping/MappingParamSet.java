/*
 * Created on Sep 14, 2012
 */
package org.griphyn.vdl.mapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.karajan.lib.Tracer;

public class MappingParamSet {
    private Map<String, Object> params;
    
    public MappingParamSet() {
        params = new HashMap<String, Object>();
    }

    public void set(MappingParam p, Object value) {
        params.put(p.getName(), value);
    }

    public void setAll(Map<String, Object> m) {
        if (m != null) {
            for (Map.Entry<String, Object> e : m.entrySet()) {
                params.put(e.getKey(), e.getValue());
            }
        }
    }

    public Object get(MappingParam p) {
        return params.get(p.getName());
    }
    
    public boolean isPresent(MappingParam p) {
        return params.containsKey(p.getName());
    }

    public AbstractDataNode getFirstOpenParamValue() {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object v = entry.getValue();
            if (v instanceof AbstractDataNode && !((AbstractDataNode) v).isClosed()) {
                return (AbstractDataNode) v;        
            }
        }
        return null;
    }

    public Collection<String> names() {
        return params.keySet();
    }

    @Override
    public String toString() {
        Object desc = get(MappingParam.SWIFT_DESCRIPTOR);
        if (desc == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(desc);
        sb.append("; ");
        sb.append("input = " + get(MappingParam.SWIFT_INPUT));
        for (String name : names()) {
            if (name.indexOf('#') >= 0) {
                // skip internal parameters
                continue;
            }
            sb.append(", ");
            sb.append(name);
            sb.append(" = \"");
            sb.append(Tracer.unwrapHandle(params.get(name)));
            sb.append("\"");
        }
        sb.append('>');
        return sb.toString();
    }
}
