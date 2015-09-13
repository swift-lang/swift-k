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

public class IUnaryOperator extends IAbstractExpression {
    public enum OperatorType {
        NEGATION, NOT
    }
    private OperatorType type;
    private IExpression operand;
    
    public IUnaryOperator() {
        super();
    }
    
    public IUnaryOperator(OperatorType type, IExpression operand) {
        this.type = type;
        this.operand = operand;
    }

    
    public OperatorType getOperatorType() {
        return type;
    }

    public void setOperatorType(OperatorType type) {
        this.type = type;
    }

    
    public IExpression getOperand() {
        return operand;
    }

    public void setOperand(IExpression operand) {
        this.operand = operand;
    }


    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("exp", operand.getTemplate(oc));
    }

    @Override
    protected String getTemplateName() {
        switch (type) {
            case NEGATION:
                return "unaryNegation";
            case NOT:
                return "not";
        }
        return "binaryop";
    }
}
