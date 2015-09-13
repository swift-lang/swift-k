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

public class IAppend extends AbstractIStatement {
    private ILValue lvalue;
    private IExpression rvalue;
    
    public IAppend() {
        super();
    }
    
    public IAppend(ILValue lvalue, IExpression rvalue) {
        super();
        this.lvalue = lvalue;
        this.rvalue = rvalue;
    }
    

    public ILValue getLValue() {
        return lvalue;
    }

    public void setLValue(ILValue lvalue) {
        this.lvalue = lvalue;
    }


    public IExpression getRValue() {
        return rvalue;
    }

    public void setRValue(IExpression rvalue) {
        this.rvalue = rvalue;
    }

    
    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("array", lvalue.getTemplate(oc));
        st.setAttribute("value", rvalue.getTemplate(oc));
    }

    @Override
    protected String getTemplateName() {
        return "append";
    }
}
