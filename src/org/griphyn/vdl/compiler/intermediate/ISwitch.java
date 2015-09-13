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

import java.util.List;

import org.antlr.stringtemplate.StringTemplate;

public class ISwitch extends AbstractINode implements IStatement {
    private IExpression condition;
    private List<ICase> cases;
    private IStatementBlock defaultCase;
    
    public ISwitch() {
        super();
    }
    
    
    public IExpression getCondition() {
        return condition;
    }

    public void setCondition(IExpression condition) {
        this.condition = condition;
    }
    
    public IStatementBlock getDefaultCase() {
        return defaultCase;
    }

    public void setDefaultCase(IStatementBlock defaultCase) {
        this.defaultCase = defaultCase;
    }
    
    public void addCase(ICase iCase) {
        cases = lazyAdd(cases, iCase);
    }
    
   
    @Override
    public void addPartialClose(String var) {
        throw new UnsupportedOperationException();
    }


    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("condition", condition.getTemplate(oc));
        setAll(oc, st, cases, "cases");
        if (defaultCase != null) {
            st.setAttribute("sdefault", defaultCase.getTemplate(oc));
        }
    }

    @Override
    protected String getTemplateName() {
        return "switch";
    }
}
