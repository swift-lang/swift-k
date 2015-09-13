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
 * Created on Mar 8, 2015
 */
package org.griphyn.vdl.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.globus.swift.parsetree.Node;
import org.globus.swift.parsetree.ReturnParameter;
import org.griphyn.vdl.compiler.intermediate.IActualParameter;
import org.griphyn.vdl.type.Type;

public class ActualParameters {
    public static class Entry {
        private final Node param;
        private IActualParameter iParam;
        
        public Entry(Node param, IActualParameter iParam) {
            this.param = param;
            this.iParam = iParam;
        }

        public Node getParam() {
            return param;
        }

        public IActualParameter getIParam() {
            return iParam;
        }

        public void setIParam(IActualParameter iParam) {
            this.iParam = iParam;
        }
        
        public String getBinding() {
            return iParam.getBinding();
        }
        
        public Type getType() {
            return iParam.getType();
        }
    }
    
    private List<Entry> params;
    private List<Entry> returns;
    
    // set by reOrder with the actual positionals
    private List<Entry> positionals;
    
    // set by reOrder with the actual optionals
    private List<Entry> optionals;
    
    // set by reOrder with the varargs
    private List<Entry> varargs;
    
    private boolean someReturnsHaveNames;
    // set by the type checking mechanism to indicate
    // the position of the first vararg
    private int varargPosStart;
    // set by the type checking mechanism to indicate
    // how many positionals are passed by position
    private int passedPositionallyCount;
    
    public void addParameter(Node param, IActualParameter iParam) {
        if (params == null) {
            params = new ArrayList<Entry>();
        }
        params.add(new Entry(param, iParam));
    }
        
    public void addReturn(ReturnParameter ret, IActualParameter iRet) {
        if (returns == null) {
            returns = new ArrayList<Entry>();
        }
        if (ret != null && ret.getBinding() != null) {
            someReturnsHaveNames = true;
        }
        returns.add(new Entry(ret, iRet));
    }
    
    public void setReturn(int index, IActualParameter iRet) {
        returns.get(index).setIParam(iRet);
    }

    public List<Entry> getReturnEntries() {
        return returns;
    }

    public int returnCount() {
        if (returns == null) {
            return 0;
        }
        else {
            return returns.size();
        }
    }

    public IActualParameter getReturn(int i) {
        return returns.get(i).getIParam();
    }
    
    public List<Entry> getReturns() {
        return returns;
    }

    public boolean someReturnsHaveNames() {
        return someReturnsHaveNames;
    }

    public List<Entry> getParameters() {
        return params;
    }
    
    public IActualParameter getParameter(int i) {
        return params.get(i).getIParam();
    }

    public int parameterCount() {
        if (params == null) {
            return 0;
        }
        else {
            return params.size();
        }
    }

    public Entry getParameterEntry(int i) {
        return params.get(i);
    }

    public void reOrder(Signature sig) {
        if (someReturnsHaveNames()) {
            List<Entry> newReturns = new ArrayList<Entry>();
            for (Signature.Parameter r : sig.getReturns()) {
                newReturns.add(getReturn(r.getName()));
            }
            returns = newReturns;
        }
        positionals = new ArrayList<Entry>();
        for (int i = 0; i < passedPositionallyCount; i++) {
            positionals.add(params.get(i));
        }
        for (int i = passedPositionallyCount; i < sig.getPositionalCount(); i++) {
            Signature.Parameter p = sig.getParameter(i);
            positionals.add(getPositionalParam(p.getName()));
        }
        optionals = new ArrayList<Entry>();
        for (int i = sig.getPositionalCount(); i < varargPosStart; i++) {
            Signature.Parameter p = sig.getParameter(i);
            Entry o = getOptionalParam(p.getName());
            if (o != null) {
                optionals.add(o);
            }
        }
        varargs = new ArrayList<Entry>();
        if (params != null) {
            for (int i = varargPosStart; i < params.size(); i++) {
                varargs.add(params.get(i));
            }
        }
    }
    
    private Entry getOptionalParam(String name) {
        for (Entry p : params) {
            if (name.equals(p.getIParam().getBinding())) {
                return p;
            }
        }
        return null;
    }

    
    private Entry getPositionalParam(String name) {
        for (Entry p : params) {
            if (name.equals(p.getIParam().getBinding())) {
                return p;
            }
        }
        throw new RuntimeException("Internal error. No parameter with name '" + 
            name + "' found for " + this);
    }

    private Entry getReturn(String name) {
        for (Entry r : returns) {
            if (r.getIParam().getBinding().equals(name)) {
                return r;
            }
        }
        throw new RuntimeException("Internal error. No return with name '" + 
            name + "' found for " + this);
    }

    public void setVarargPositionStart(int index) {
        this.varargPosStart = index;
    }

    public void setPassedPositionallyCount(int index) {
        this.passedPositionallyCount = index;
    }

    public int positionalCount() {
        if (positionals == null) {
            return 0;
        }
        else {
            return positionals.size();
        }
    }

    public IActualParameter getPositional(int i) {
        return positionals.get(i).getIParam();
    }

    public int optionalCount() {
        return optionals.size();
    }

    public IActualParameter getOptional(int i) {
        return optionals.get(i).getIParam();
    }

    public int varargCount() {
        return varargs.size();
    }

    public IActualParameter getVararg(int i) {
        return varargs.get(i).getIParam();
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        appendList(sb, params);
        sb.append(") -> (");
        appendList(sb, returns);
        sb.append(')');
        return sb.toString();
    }

    private void appendList(StringBuilder sb, List<Entry> l) {
        if (l == null) {
            return;
        }
        Iterator<Entry> i = l.iterator();
        while (i.hasNext()) {
            Entry e = i.next();
            IActualParameter iParam = e.getIParam();
            sb.append(iParam.getType());
            if (iParam.getBinding() != null) {
                sb.append(' ');
                sb.append(iParam.getBinding());
            }
            if (i.hasNext()) {
                sb.append(", ");
            }
        }
    }

    public String toString(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        appendList(sb, returns);
        sb.append(") ");
        sb.append(name);
        sb.append("(");
        appendList(sb, params);
        sb.append(')');
        return sb.toString();

    }

    public boolean allPassedByKeyword() {
        for (Entry e : params) {
            if (e.getIParam().getBinding() == null) {
                return false;
            }
        }
        return true;
    }
}
