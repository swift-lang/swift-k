//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 8, 2015
 */
package org.griphyn.vdl.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.xmlbeans.XmlObject;
import org.globus.swift.language.ActualParameter;
import org.griphyn.vdl.type.Type;

public class ActualParameters {
    public static class Entry {
        private final XmlObject param;
        private final String binding;
        private StringTemplate paramST;
        private final Type type;
        
        public Entry(XmlObject param, String binding, StringTemplate paramST, Type type) {
            this.param = param;
            this.binding = binding;
            this.paramST = paramST;
            this.type = type;
        }

        public XmlObject getParam() {
            return param;
        }

        public String getBinding() {
            return binding;
        }

        public StringTemplate getParamST() {
            return paramST;
        }

        public Type getType() {
            return type;
        }

        public void setParamST(StringTemplate st) {
            paramST = st;
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
    
    private void addParameter(XmlObject param, String binding, StringTemplate exprST, Type type) {
        if (params == null) {
            params = new ArrayList<Entry>();
        }
        params.add(new Entry(param, binding, exprST, type));
    }
    
    public void addParameter(ActualParameter arg, StringTemplate argST, Type type) {
        addParameter(arg, arg.getBind(), argST, type);
    }
    
    public void addParameter(XmlObject arg, StringTemplate argST, Type type) {
        addParameter(arg, null, argST, type);
    }
    
    public void addReturn(ActualParameter ret, StringTemplate retST, Type type) {
        if (returns == null) {
            returns = new ArrayList<Entry>();
        }
        if (ret != null && ret.getBind() != null) {
            someReturnsHaveNames = true;
        }
        returns.add(new Entry(ret, ret == null ? null : ret.getBind(), retST, type));
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

    public Entry getReturn(int i) {
        return returns.get(i);
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
    
    public Entry getParameter(int i) {
        return params.get(i);
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
            if (name.equals(p.getBinding())) {
                return p;
            }
        }
        return null;
    }

    
    private Entry getPositionalParam(String name) {
        for (Entry p : params) {
            if (name.equals(p.getBinding())) {
                return p;
            }
        }
        throw new RuntimeException("Internal error. No parameter with name '" + 
            name + "' found for " + this);
    }

    private Entry getReturn(String name) {
        for (Entry r : returns) {
            if (r.getBinding().equals(name)) {
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

    public StringTemplate getPositionalST(int i) {
        return positionals.get(i).getParamST();
    }

    public int optionalCount() {
        return optionals.size();
    }

    public StringTemplate getOptionalST(int i) {
        return optionals.get(i).getParamST();
    }

    public int varargCount() {
        return varargs.size();
    }

    public StringTemplate getVarargST(int i) {
        return varargs.get(i).getParamST();
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
            sb.append(e.getType());
            if (e.getBinding() != null) {
                sb.append(' ');
                sb.append(e.getBinding());
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
            if (e.getBinding() == null) {
                return false;
            }
        }
        return true;
    }
}
