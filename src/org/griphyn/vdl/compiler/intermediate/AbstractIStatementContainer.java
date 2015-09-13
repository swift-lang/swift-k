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

import java.util.Iterator;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.griphyn.vdl.engine.VariableScope;

public abstract class AbstractIStatementContainer extends AbstractINode implements IStatementContainer {
    private final VariableScope scope;
    private List<IVariableDeclaration> variables;
    private List<IStatement> statements;
    private List<String> outputs;
    
    public AbstractIStatementContainer(VariableScope scope) {
        super();
        this.scope = scope;
        scope.setOwner(this);
    }

    @Override
    public VariableScope getScope() {
        return scope;
    }
    
    @Override
    public void addVariableDeclaration(IVariableDeclaration var) {
        variables = lazyAdd(variables, var);
    }
    
    public List<IVariableDeclaration> getVariableDeclarations(){
        return variables;
    }
    
    @Override
    public void addStatement(IStatement stat) {
        statements = lazyAdd(statements, stat);
    }

    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        setAll(oc, st, variables, "declarations");
        setAll(oc, st, statements, "statements");
        st.setAttribute("cleanups", getScope().getCleanups());
    }
    
    private String join(List<String> l) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> i = l.iterator();
        while (i.hasNext()) {
            sb.append(i.next());
            if (i.hasNext()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
    
}
