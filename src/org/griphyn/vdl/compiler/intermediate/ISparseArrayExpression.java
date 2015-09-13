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

import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;

public class ISparseArrayExpression extends IAbstractExpression implements IStatement {
    private String fieldName;
    private ILValue var;
    private List<IField> items;
    private Map<String, MutableInteger> partialClose;
    
    public ISparseArrayExpression() {
        super();
    }


    public void addItem(IField item) {
        items = lazyAdd(items, item);
    }


    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }


    public ILValue getVar() {
        return var;
    }

    public void setVar(ILValue var) {
        this.var = var;
    }

    @Override
    public void addPartialClose(String var) {
        partialClose = AbstractIStatement.lazyPutPartial(partialClose, var, 1);
    }

    @Override
    public StringTemplate getTemplate(OutputContext oc) {
        return AbstractIStatement.maybeWrapPartialClose(oc, super.getTemplate(oc), partialClose);
    }

    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("field", fieldName);
        if (var != null) {
            st.setAttribute("var", var.getTemplate(oc));
        }
        setAll(oc, st, items, "fields");
    }

    @Override
    protected String getTemplateName() {
        return "newSparseArray";
    }
}
