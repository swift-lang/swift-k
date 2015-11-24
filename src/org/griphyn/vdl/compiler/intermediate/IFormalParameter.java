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

public class IFormalParameter extends AbstractINode implements IRefCounted {
    private boolean isReturn;
    private String name;
    private int writeCount;
    private int readCount;
    private ITypeReference type;
    private INode defaultValue;
    private final IProcedureDeclaration procedure;

    public IFormalParameter(IProcedureDeclaration procedure) {
        super();
        this.procedure = procedure;
    }
 
    
    public IProcedureDeclaration getProcedure() {
        return procedure;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void incReadCount() {
        incReadCount(1);
    }

    public void incReadCount(int amount) {
        readCount += amount;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public void incWriteCount(int amount) {
        writeCount += amount;
    }
    
    public int getWriteCount() {
        return writeCount;
    }

    public void setWriteCount(int writeCount) {
        this.writeCount = writeCount;
    }


    public ITypeReference getType() {
        return type;
    }

    public void setType(ITypeReference type) {
        this.type = type;
    }


    public INode getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(INode defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public void setIsReturn(boolean isReturn) {
        this.isReturn = isReturn;
    }

    @Override
    public boolean isWrapped() {
        return !isReturn && readCount > 0 && !procedure.isApplication();
    }

    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("name", name);
        //st.setAttribute("cleanCount", 0);
        StringTemplate typest = new StringTemplate("type");
        typest.setAttribute("name", type.getType().toString());
        st.setAttribute("type", typest);
        if (defaultValue != null) {
            st.setAttribute("default", defaultValue.getTemplate(oc));
        }
    }

    @Override
    protected StringTemplate makeStringTemplate(OutputContext oc) {
        return new StringTemplate("parameter");
    }

    @Override
    protected String getTemplateName() {
        return "parameter";
    }
    
    public String toString() {
        return name;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }
}
