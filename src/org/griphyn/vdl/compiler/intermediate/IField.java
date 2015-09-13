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

public class IField extends AbstractINode {
    private Object key;
    private IExpression value;

    public IField() {
    }
    
    public IField(INode key, IExpression value) {
        this.key = key;
        this.value = value;
    }

    
    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }


    public IExpression getValue() {
        return value;
    }

    public void setValue(IExpression value) {
        this.value = value;
    }
    
    
    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        if (key instanceof INode) {
            st.setAttribute("key", ((INode) key).getTemplate(oc));
        }
        else {
            st.setAttribute("key", "\"" + key + "\"");
        }
        st.setAttribute("value", value.getTemplate(oc));
    }

    @Override
    protected String getTemplateName() {
        return "makeField";
    }
}
