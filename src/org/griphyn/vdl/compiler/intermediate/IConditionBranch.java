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
 * Created on Sep 5, 2015
 */
package org.griphyn.vdl.compiler.intermediate;

import java.util.HashMap;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.griphyn.vdl.engine.VariableScope;

public class IConditionBranch extends IStatementBlock {
    private Map<String, Integer> preClean, preClose, postClose;

    public IConditionBranch(VariableScope scope) {
        super(scope);
    }
    
    public void setPreClean(String name, int count) {
        preClean = setPre(preClean, name, count);
    }
    
    public void setPreClose(String name, int count) {
        preClose = setPre(preClose, name, count);
    }
    
    public void setPostFullClose(String name) {
        postClose = setPre(postClose, name, 1);
    } 

    private Map<String, Integer> setPre(Map<String, Integer> m, String name, int count) {
        if (m == null) {
            m = new HashMap<String, Integer>();
        }
        m.put(name, count);
        return m;
    }

    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        addPre(oc, st, preClean, "partialclean");
        if (postClose != null && preClose != null) {
            for (String name : postClose.keySet()) {
                preClose.remove(name);
            }
        }
        addPre(oc, st, preClose, "partialclose");
        super.setTemplateAttributes(oc, st);
        if (postClose != null) {
            for (String name : postClose.keySet()) {
                StringTemplate close = oc.template("fullclose");
                close.setAttribute("var", name);
                st.setAttribute("statements", close);
            }
        }
    }

    private void addPre(OutputContext oc, StringTemplate st, Map<String, Integer> counts, String templateName) {
        if (counts != null) {
            boolean any = false;
            StringTemplate pre = oc.template(templateName);
            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                if (e.getValue() > 0) {
                    pre.setAttribute("items", e.getKey());
                    pre.setAttribute("items", e.getValue());
                    any = true;
                }
            }
            if (any) {
                st.setAttribute("statements", pre);
            }
        }
    }
}
