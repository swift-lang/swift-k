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
import org.griphyn.vdl.engine.VariableScope;
import org.griphyn.vdl.type.Type;

public class IVariableReference extends IAbstractExpression implements ILValue {
    private String name;
    private boolean isLValue;
    private boolean isFullWrite;
    private IRefCounted declaration;
    
    public IVariableReference() {
        super();
    }
    
    public IVariableReference(String name) {
        super();
        this.name = name;
    }
    
    public IVariableReference(Type type, String name) {
        super();
        this.name = name;
        setType(type);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsLValue() {
        return isLValue;
    }

    public void setIsLValue(boolean isLValue) {
        this.isLValue = isLValue;
    }
     
    public boolean isFullWrite() {
        return isFullWrite;
    }

    public void setIsFullWrite(boolean isFullWrite) {
        this.isFullWrite = isFullWrite;
    }

    public IRefCounted getDeclaration() {
        return declaration;
    }

    public void setDeclaration(IRefCounted declaration) {
        this.declaration = declaration;
    }
    

    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("var", name);
    }

    @Override
    protected String getTemplateName() {
        if (declaration == null) {
            // internal stuff
            return "id";
        }
        if (declaration.isWrapped()) {
            if (isLValue && !isFullWrite) {
                return "unwrapVar";
            }
            else {
                return "readVar";
            }
        }
        else {
            // not read from, so if there is only one full write,
            // we can delete it on access
            if (declaration.getWriteCount() == VariableScope.FULL_WRITE_COUNT) {
               return "readAndDeleteVar";
            }
            else {
                return "id";
            }
        }
    }
}