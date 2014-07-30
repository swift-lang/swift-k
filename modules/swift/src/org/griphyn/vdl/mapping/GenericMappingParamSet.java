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
