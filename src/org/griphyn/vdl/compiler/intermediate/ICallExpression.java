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

import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.griphyn.vdl.engine.VariableScope;

public class ICallExpression extends IFunctionCall implements IStatementContainer {
    private boolean mapping;
    private String fieldName;
    private ICall call;
    private String prefix;
    private VariableScope scope;
    
    public ICallExpression(VariableScope scope) {
        this.scope = scope;
        scope.setOwner(this);
    }
   
    public VariableScope getScope() {
        return scope;
    }

    public void setScope(VariableScope scope) {
        this.scope = scope;
    }

    @Override
    public void addVariableDeclaration(IVariableDeclaration var) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IVariableDeclaration> getVariableDeclarations() {
        return null;
    }

    @Override
    public void addStatement(IStatement stat) {
        throw new UnsupportedOperationException();
    }

    public boolean getMapping() {
        return mapping;
    }

    public void setMapping(boolean mapping) {
        this.mapping = mapping;
    }
    

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    
    public ICall getCall() {
        return call;
    }

    public void setCall(ICall call) {
        this.call = call;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    
    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        if (mapping) {
            st.setAttribute("mapping", true);
        }
        st.setAttribute("field", fieldName);
        st.setAttribute("call", call.getTemplate(oc));
        if (prefix != null) {
            st.setAttribute("prefix", prefix);
        }
    }

    @Override
    protected String getTemplateName() {
        return "callexpr";
    }
}
