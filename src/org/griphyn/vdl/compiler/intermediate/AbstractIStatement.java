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
 * Created on Sep 4, 2015
 */
package org.griphyn.vdl.compiler.intermediate;

import java.util.HashMap;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;


public abstract class AbstractIStatement extends AbstractINode implements IStatement {
    private Map<String, MutableInteger> partialClose;
    
    @Override
    public StringTemplate getTemplate(OutputContext oc) {
        return maybeWrapPartialClose(oc, super.getTemplate(oc), partialClose);
    }

    public static StringTemplate maybeWrapPartialClose(OutputContext oc, StringTemplate st, Map<String, MutableInteger> m) {
        if (m == null) {
            return st;
        }
        else {
            boolean any = false;
            StringTemplate pcst = oc.template("partialclose");
            for (Map.Entry<String, MutableInteger> e : m.entrySet()) {
                if (e.getValue().getValue() > 0) {
                    pcst.setAttribute("items", e.getKey());
                    pcst.setAttribute("items", e.getValue().getValue());
                    any = true;
                }
            }
            if (any) {
                StringTemplate seq = oc.template("sequential");
                seq.setAttribute("statements", st);
                seq.setAttribute("statements", pcst);
                return seq;
            }
            else {
                return st;
            }
        }
    }
    
    public static Map<String, MutableInteger> lazyPutPartial(Map<String, MutableInteger> map, String var, int delta) {
        if (map == null) {
            map = new HashMap<String, MutableInteger>();
        }
        MutableInteger mi = map.get(var);
        if (mi == null) {
            mi = new MutableInteger();
            map.put(var, mi);
        }
        mi.inc(delta);
        
        return map;
    }
    
    @Override
    public void addPartialClose(String var) {
        addPartialClose(var, 1);
    }
            
    public void addPartialClose(String var, int delta) {
        partialClose = lazyPutPartial(partialClose, var, delta);
    }
}
