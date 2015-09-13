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
 * Created on Sep 3, 2015
 */
package org.griphyn.vdl.compiler.intermediate;

import java.util.Collection;

import org.antlr.stringtemplate.StringTemplate;
import org.griphyn.vdl.engine.VariableScope;

public class IIterate extends ILoop implements IStatement {
    private String varName;
    private IExpression condition;
    
    public IIterate(VariableScope scope) {
        super(scope);
    }
    
    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public IExpression getCondition() {
        return condition;
    }

    public void setCondition(IExpression condition) {
        this.condition = condition;
    }
        
    @Override
    public void addPartialClose(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        Collection<String> cleanups = getScope().getCleanups();
        if (cleanups != null) {
            cleanups.remove(varName);
        }
        super.setTemplateAttributes(oc, st);
        st.setAttribute("var", varName);
        st.setAttribute("cond", condition.getTemplate(oc));
        setPartials(st, wrefs, "wrefs");
        mergeRefs(rrefs, wrefs);
        setPartials(st, rrefs, "rrefs");
    }

    @Override
    protected String getTemplateName() {
        return "iterate";
    }
}
