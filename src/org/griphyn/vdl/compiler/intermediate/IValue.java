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
import org.griphyn.vdl.type.Types;

public class IValue extends IAbstractExpression {
    private Type type;
    private String valueRepr;

    public IValue() {
        super();
    }
    
    public IValue(Type type, String valueRepr) {
        super();
        this.type = type;
        this.valueRepr = valueRepr;
    }
   

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    

    public String getValueRepr() {
        return valueRepr;
    }

    public void setValueRepr(String valueRepr) {
        this.valueRepr = valueRepr;
    }


    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("value", valueRepr);
    }

    @Override
    protected String getTemplateName() {
        if (type == Types.STRING) {
            return "sConst";
        }
        else if (type == Types.INT) {
            return "iConst";
        }
        else if (type == Types.FLOAT) {
            return "fConst";
        }
        else if (type == Types.BOOLEAN) {
            return "bConst";
        }
        else {
            throw new IllegalArgumentException("Cannot handle constant type " + type);
        }
    }
}
