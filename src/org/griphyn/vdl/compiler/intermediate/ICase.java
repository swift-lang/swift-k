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

import org.antlr.stringtemplate.StringTemplate;
import org.griphyn.vdl.engine.VariableScope;

public class ICase extends IConditionBranch {    
    private IExpression value;
    
    public ICase(VariableScope scope) {
        super(scope);
    }
    

    public IExpression getValue() {
        return value;
    }

    public void setValue(IExpression value) {
        this.value = value;
    }

    
    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("value", value.getTemplate(oc));
        st.setAttribute("cleanups", getScope().getCleanups());
    }
    
    @Override
    protected StringTemplate makeStringTemplate(OutputContext oc) {
        return new StringTemplate("case");
    }
}
