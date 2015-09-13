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

public class IForeach extends ILoop implements IStatement {
    private String varName;
    private String indexVarName;
    private String indexVarFieldName;
    private IExpression in;
    private boolean selfClose;
    
    public IForeach(VariableScope scope) {
        super(scope);
    }
    
    
    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }


    public String getIndexVarName() {
        return indexVarName;
    }

    public void setIndexVarName(String indexVarName) {
        this.indexVarName = indexVarName;
    }


    public String getIndexVarFieldName() {
        return indexVarFieldName;
    }

    public void setIndexVarFieldName(String indexVarFieldName) {
        this.indexVarFieldName = indexVarFieldName;
    }


    public IExpression getIn() {
        return in;
    }

    public void setIn(IExpression in) {
        this.in = in;
    }

    
    public boolean getSelfClose() {
        return selfClose;
    }

    public void setSelfClose(boolean selfClose) {
        this.selfClose = selfClose;
    }

    @Override
    public void addPartialClose(String name) {
        // ignore; there is special handling for loops (see addWRef, addRRef)
    }


    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        Collection<String> cleanups = getScope().getCleanups();
        if (cleanups != null) {
            cleanups.remove(varName);
            if (indexVarName != null) {
                cleanups.remove(indexVarName);
            }
        }
        super.setTemplateAttributes(oc, st);
        st.setAttribute("var", varName);
        st.setAttribute("in", in.getTemplate(oc));
        if (indexVarName != null) {
            st.setAttribute("indexVar", indexVarName);
            st.setAttribute("indexVarField", indexVarFieldName);
        }
        if (selfClose) {
            st.setAttribute("selfClose", "true");
        }
        setPartials(st, wrefs, "wrefs");
        mergeRefs(rrefs, wrefs);
        setPartials(st, rrefs, "rrefs");
    }


    @Override
    protected String getTemplateName() {
        return "foreach";
    }
}
