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

public class IIfStatement extends AbstractINode implements IStatement {
    private IExpression condition;
    private IStatementBlock thenBlock, elseBlock;
    
    public IIfStatement() {
        super();
    }
    

    public IExpression getCondition() {
        return condition;
    }

    public void setCondition(IExpression condition) {
        this.condition = condition;
    }
    

    public IStatementBlock getThenBlock() {
        return thenBlock;
    }

    public void setThenBlock(IStatementBlock thenBlock) {
        this.thenBlock = thenBlock;
    }


    public IStatementBlock getElseBlock() {
        return elseBlock;
    }

    public void setElseBlock(IStatementBlock elseBlock) {
        this.elseBlock = elseBlock;
    }

    
    @Override
    public void addPartialClose(String name) {
        // ignore; these are handled by the branches
    }


    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("condition", condition.getTemplate(oc));
        st.setAttribute("vthen", thenBlock.getTemplate(oc));
        if (elseBlock != null) {
            st.setAttribute("velse", elseBlock.getTemplate(oc));
        }
    }

    @Override
    protected String getTemplateName() {
        return "if";
    }
}
