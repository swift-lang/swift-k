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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplate;
import org.griphyn.vdl.engine.VariableScope;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class IProcedureDeclaration extends AbstractIStatementContainer {
    private String name;
    private List<IFormalParameter> returns;
    private List<IFormalParameter> parameters;
    private Set<String> allNames;
    private IApplication application;
    private boolean isApplication;

    public IProcedureDeclaration(VariableScope outerScope) {
        super(outerScope);
        allNames = new HashSet<String>();
    }
 

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void addReturn(IFormalParameter r) {
        returns = lazyAdd(returns, r);
        allNames.add(r.getName());
    }
    
    public void addParameter(IFormalParameter r) {
        parameters = lazyAdd(parameters, r);
        allNames.add(r.getName());
    }

    
    public IApplication getApplication() {
        return application;
    }
    
    public boolean isApplication() {
        return isApplication;
    }
    
    public void setIsApplication(boolean isApplication) {
        this.isApplication = isApplication;
    }

    public void setApplication(IApplication application) {
        this.application = application;
    }
    
    private boolean isPrimitiveOrArrayOfPrimitives(Type t) {
        return (t.isPrimitive() && !Types.EXTERNAL.equals(t)) || (t.isArray() && t.itemType().isPrimitive());
    }
    
    public IFormalParameter getReturn(String name) {
        return getReturnOrParam(name, returns);
    }
    
    public IFormalParameter getParameter(String name) {
        return getReturnOrParam(name, parameters);
    }
    
    public void addParameterRead(String name, int amount) {
        getParameter(name).incReadCount(amount);
    }
    
    public boolean hasParameterOrReturn(String name) {
        return allNames.contains(name);
    }
    
    private IFormalParameter getReturnOrParam(String name, List<IFormalParameter> l) {
        if (l == null) {
            return null;
        }
        for (IFormalParameter r : l) {
            if (r.getName().equals(name)) {
                return r;
            }
        }
        return null;
    }
    
    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        if (parameters != null) {
            StringTemplate readInitST = oc.template("initProcReadRefs");
            for (IFormalParameter p : parameters) {
                if (p.getReadCount() > 0) {
                    readInitST.setAttribute("items", p.getName());
                    readInitST.setAttribute("items", p.getReadCount());
                }
            }
            if (!parameters.isEmpty()) {
                st.setAttribute("initReadCounts", readInitST);
            }
        }
        super.setTemplateAttributes(oc, st);
        st.setAttribute("name", name);
        
        if (returns != null) {
            StringTemplate initWaitCountST = null;
            for (IFormalParameter r : returns) {
                StringTemplate rst = r.getTemplate(oc);
                st.setAttribute("outputs", rst);
                if (r.getDefaultValue() != null) {
                    st.setAttribute("optargs", rst);
                }
                else {
                    st.setAttribute("arguments", rst);
                }
                if (r.getWriteCount() > 0 && r.getWriteCount() != VariableScope.FULL_WRITE_COUNT) {
                    if (initWaitCountST == null) {
                        initWaitCountST = oc.template("setWaitCount");
                        st.setAttribute("initWaitCounts", initWaitCountST);
                    }
                    initWaitCountST.setAttribute("items", r.getName());
                    initWaitCountST.setAttribute("items", r.getWriteCount());
                }
                
                if (application != null && !this.isPrimitiveOrArrayOfPrimitives(r.getType().getType())) {
                    st.setAttribute("stageouts", r.getName());
                }
            }
        }
        if (parameters != null) {
            for (IFormalParameter p : parameters) {
                StringTemplate pst = p.getTemplate(oc);
                st.setAttribute("inputs", pst);
                if (p.getDefaultValue() != null) {
                    st.setAttribute("optargs", pst);
                }
                else {
                    st.setAttribute("arguments", pst);
                }
                if (application != null && !this.isPrimitiveOrArrayOfPrimitives(p.getType().getType())) {
                    st.setAttribute("stageins", p.getName());
                }
            }
        }
        if (application != null) {
            StringTemplate bindST = new StringTemplate("binding");
            bindST.setAttribute("application", application.getTemplate(oc));
            st.setAttribute("binding", bindST);
        }
    }

    @Override
    protected String getTemplateName() {
        return "procedure";
    }
}
