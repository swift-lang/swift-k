//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 1, 2014
 */
package org.griphyn.vdl.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.util.Pair;

public class GenericMappingParamSet {
    private final String descriptor;
    private List<Pair<String, Object>> params;
    
    public GenericMappingParamSet(String descriptor) {
        this.descriptor = descriptor;
    }

    public void addParam(Pair<String, Object> param) {
        if (params == null) {
            params = new ArrayList<Pair<String, Object>>(4);
        }
        params.add(param);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('<');
        sb.append(descriptor);
        
        if (params != null) {
            sb.append("; ");
            Iterator<Pair<String, Object>> it = params.iterator();
            while (it.hasNext()) {
                Pair<String, Object> p = it.next();
                sb.append(p.s);
                sb.append(" = ");
                sb.append(p.t);
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        sb.append(">");
        
        return sb.toString();
    }

    public String getDescriptor() {
        return descriptor;
    }

    public Collection<Pair<String, Object>> getParams() {
        if (params == null) {
            return Collections.emptyList();
        }
        else {
            return params;
        }
    }

    public void put(String name, Object value) {
        addParam(new Pair<String, Object>(name, value));
    }
}
