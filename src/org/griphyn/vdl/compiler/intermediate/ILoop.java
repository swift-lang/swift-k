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
 * Created on Sep 6, 2015
 */
package org.griphyn.vdl.compiler.intermediate;

import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.griphyn.vdl.engine.VariableScope;

public abstract class ILoop extends AbstractIStatementContainer {
    protected Map<String, MutableInteger> wrefs;
    protected Map<String, MutableInteger> rrefs;
    
    protected ILoop(VariableScope scope) {
        super(scope);
    }
    
    public void addWRef(String var, int delta) {
        wrefs = AbstractIStatement.lazyPutPartial(wrefs, var, delta);
    }
    
    public void addRRef(String var, int delta) {
        rrefs = AbstractIStatement.lazyPutPartial(rrefs, var, delta);
    }

    protected void mergeRefs(Map<String, MutableInteger> rrefs, Map<String, MutableInteger> wrefs) {
        /*
         * Consider writes as read access, too. See comment at the end of setTemplateAttributes()
         * in IVariableDeclaration.
         */
        if (rrefs == null) {
            return;
        }
        if (wrefs == null) {
            // nothing to merge
            return;
        }
        for (Map.Entry<String, MutableInteger> e : rrefs.entrySet()) {
            if (e.getValue().getValue() > 0) {
                MutableInteger wref = wrefs.get(e.getKey());
                if (wref != null) {
                    if (wref.getValue() == VariableScope.FULL_WRITE_COUNT) {
                        e.getValue().inc(1);
                    }
                    else {
                        e.getValue().inc(wref.getValue());
                    }
                }
            }
        }
    }


    protected void setPartials(StringTemplate st, Map<String, MutableInteger> refs, String attrName) {
        if (refs == null) {
            return;
        }
        for (Map.Entry<String, MutableInteger> e : refs.entrySet()) {
            if (e.getValue().getValue() > 0) {
                st.setAttribute(attrName, e.getKey());
                st.setAttribute(attrName, e.getValue().getValue());
            }
        }
    }
}
