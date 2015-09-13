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

import org.antlr.stringtemplate.StringTemplate;

public class ICall extends AbstractIStatement {
    private boolean internal;
    private String name;
    private List<IActualParameter> returns;
    private List<IActualParameter> parameters;
    private boolean serialize;
    
    public ICall() {
        super();
    }

    public boolean getInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public boolean getSerialize() {
        return serialize;
    }

    public void setSerialize(boolean serialize) {
        this.serialize = serialize;
    }

    
    public void addReturn(IActualParameter iRet) {
        returns = lazyAdd(returns, iRet);
    }
    
    public void setReturn(int index, IActualParameter iRet) {
        returns.set(0, iRet);
    }
    
    public void addParameter(IActualParameter iParam) {
        parameters = lazyAdd(parameters, iParam);
    }

    
    public List<IActualParameter> getReturns() {
        return returns;
    }

    public List<IActualParameter> getParameters() {
        return parameters;
    }

    @Override
    protected void setTemplateAttributes(OutputContext oc, StringTemplate st) {
        super.setTemplateAttributes(oc, st);
        st.setAttribute("func", name);
        if (returns != null) {
            for (IActualParameter n : returns) {
                st.setAttribute("outputs", n.getTemplate(oc));
                if (internal) {
                	String outputName = getOutputName(n.getValue());
                	if (outputName != null) {
                	    st.setAttribute("outputNames", outputName);
                	}
                }
            }
        }
        setAll(oc, st, parameters, "inputs");
        if (serialize) {
            st.setAttribute("serialize", true);
        }
    }

    private String getOutputName(IExpression exp) {
        if (exp instanceof ILValue) {
        	return getVarName((ILValue) exp);
        }
        else {
        	return null;
        }
    }

    private String getVarName(ILValue exp) {
        if (exp instanceof IVariableReference) {
        	return ((IVariableReference) exp).getName();
        }
        else if (exp instanceof IArrayReference) {
        	return getOutputName(((IArrayReference) exp).getArray());
        }
        else if (exp instanceof IStructReference) {
        	return getOutputName(((IStructReference) exp).getStruct());
        }
        else {
        	return null;
        }
    }

    @Override
    protected String getTemplateName() {
        if (internal) {
            return "callInternal";
        }
        else {
            return "callUserDefined";
        }
    }

    
}
