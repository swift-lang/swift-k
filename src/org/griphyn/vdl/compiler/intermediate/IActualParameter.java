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
import org.griphyn.vdl.type.Type;

public class IActualParameter extends AbstractINode {
    private String binding;
    private IExpression value;
    private Type type;

    public IActualParameter() {
        super();
    }
    
    public IActualParameter(IExpression value, Type type) {
        super();
        this.value = value;
        this.type = type;
    }
     

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }


    public IExpression getValue() {
        return value;
    }

    public void setValue(IExpression value) {
        this.value = value;
    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }


    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        if (binding != null) {
            st.setAttribute("bind", binding);
        }
        st.setAttribute("expr", value.getTemplate(oc));
    }


    @Override
    protected String getTemplateName() {
        return "call_arg";
    }
}
