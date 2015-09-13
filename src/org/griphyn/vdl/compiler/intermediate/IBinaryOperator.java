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

public class IBinaryOperator extends IAbstractExpression {
    private String op;
    private IExpression left, right;
    
    public IBinaryOperator() {
        super();
    }
    
    public IBinaryOperator(String op, IExpression left, IExpression right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }


    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    
    public IExpression getLeft() {
        return left;
    }

    public void setLeft(IExpression left) {
        this.left = left;
    }


    public IExpression getRight() {
        return right;
    }


    public void setRight(IExpression right) {
        this.right = right;
    }




    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("op", op);
        st.setAttribute("left", left.getTemplate(oc));
        st.setAttribute("right", right.getTemplate(oc));
    }

    @Override
    protected String getTemplateName() {
        return "binaryop";
    }
}
