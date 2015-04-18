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
 * Created on Mar 6, 2015
 */
package org.griphyn.vdl.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.globus.swift.language.FormalParameter;
import org.griphyn.vdl.engine.ActualParameters.Entry;
import org.griphyn.vdl.engine.Signature.Parameter;
import org.griphyn.vdl.karajan.CompilationException;
import org.griphyn.vdl.type.Type;

public class FunctionsMap {
    private Map<String, List<Signature>> map;
    
    public FunctionsMap() {
        map = new HashMap<String, List<Signature>>();
    }
    
    public void addUserFunction(String name, Signature ps) throws CompilationException {
        ps.setIsProcedure(false);
        addFunction(name, ps, true);
    }
    
    public void addUserProcedure(String name, Signature ps) throws CompilationException {
        ps.setIsProcedure(true);
        addFunction(name, ps, true);
    }

    public void addFunction(String name, Signature ps, boolean overloadingOk) throws CompilationException {
        List<Signature> l = map.get(name);
        if (l == null) {
            l = new LinkedList<Signature>();
            map.put(name, l);
        }
        if (l.size() == 0) {
            ps.usePlainName();
        }
        else {
            if (!overloadingOk) {
                throw new CompilationException("Illegal redefinition of procedure attempted for " + name);
            }
            ps.useMangledName();
            checkAmbiguous(l, getPositionals(ps), ps);
        }
        l.add(ps);
    }

    private List<Signature.Parameter> getPositionals(Signature ps) {
        List<Signature.Parameter> l = new ArrayList<Signature.Parameter>();
        for (Signature.Parameter p : ps.getParameters()) {
            if (!p.isOptional()) {
                l.add(p);
            }
        }
        return l;
    }

    private void checkAmbiguous(List<Signature> l, List<Parameter> lps, Signature ps) 
            throws CompilationException {
        for (Signature s : l) {
            List<Signature.Parameter> ls = getPositionals(s);
            if (ls.size() != lps.size()) {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < ls.size(); i++) {
                Type t1 = ls.get(i).getType();
                Type t2 = lps.get(i).getType();
                if (!t1.equals(t2)) {
                    match = false;
                    break;
                }
            }
            if (!match) {
                continue;
            }
            throw new CompilationException("Ambiguous declaration: " + ps + 
                "\n\tConflicting previous declaration: " + s);
        }
    }

    

    public void addInternalFunction(String name, Signature ps) {
        try {
            addFunction(name, ps, true);
        }
        catch (CompilationException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    public boolean isDefined(String name) {
        return map.containsKey(name);
    }
    
    protected Signature find(String name, ActualParameters actual, boolean functionContext) throws CompilationException {
    	List<Signature> procs = map.get(name);
        if (procs == null) {
            return null;
        }
        for (Signature ps : procs) {
            // the parametrized type matching relies on
            // Type.isAssignableFrom() or Type.canBeAssignedTo()
            // always being called on types inside the formal signature
            ps.clearTypeParameterBindings();
        	if (typesMatch(ps, actual, functionContext)) {
        	    actual.reOrder(ps);
        		return ps;
        	}
        }
        return null;
    }

    private boolean typesMatch(Signature ps, ActualParameters actual, boolean functionContext) throws CompilationException {
        // the keyword actuals (returns and params) are at this point
        // guaranteed to be unique
        if (!returnsMatch(ps, actual, functionContext)) {
            return false;
        }
        else if (!parametersMatch(ps, actual)) {
            return false;
        }
        else {
            return true;
        }        
    }

    private boolean parametersMatch(Signature ps, ActualParameters actual) throws CompilationException {
        // set here and override if necessary
        actual.setVarargPositionStart(actual.parameterCount());
                
        int positionalCount = ps.getPositionalCount();
        int actualPositionals = 0;
        for (int i = 0; i < actual.parameterCount(); i++) {
            ActualParameters.Entry e = actual.getParameterEntry(i);
            if (e.getBinding() != null) {
                if (i == 0) {
                    if (actual.allPassedByKeyword()) {
                        actual.setPassedPositionallyCount(0);
                        return allKeywordMatch(ps, actual);
                    }
                }
                actual.setPassedPositionallyCount(i);
                return keywordMatch(ps, actual, i);
            }
            else if (i >= positionalCount) {
                actual.setPassedPositionallyCount(positionalCount);
                return varargsMatch(ps, actual, i);
            }
            else {
                actualPositionals++;
                if (!ps.getParameter(i).getType().isAssignableFrom(e.getType())) {
                    return false;
                }
            }
        }
        if (actualPositionals < positionalCount) {
            return false;
        }
        actual.setPassedPositionallyCount(positionalCount);
        return true;
    }

    private boolean allKeywordMatch(Signature ps, ActualParameters actual) {
        int left = ps.getPositionalCount();
        for (ActualParameters.Entry e : actual.getParameters()) {
            if (e.getBinding() == null) {
                return false;
            }
            Signature.Parameter p = ps.getParameter(e.getBinding());
            if (p == null) {
                return false;
            }
            if (!p.getType().isAssignableFrom(p.getType())) {
                return false;
            }
            if (!p.isOptional()) {
                left--;
            }
        }
        return left == 0;
    }

    private boolean keywordMatch(Signature ps, ActualParameters actual, int start) throws CompilationException {
        // this is a mighty mess
        // all keyword arguments from start have to:
        //    cover all remaining positionals
        //    maybe cover optionals
        // hand over to varargs match when hitting the first non-keyword arg
        int seenPositionalCount = start;
        for (int i = start; i < actual.parameterCount(); i++) {
            ActualParameters.Entry e = actual.getParameterEntry(i);
            String name = e.getBinding();
            if (name == null) {
                return allPositionalsCovered(ps, i) && varargsMatch(ps, actual, i);
            }
            if (!ps.hasPositional(name, e.getType())) {
                return allPositionalsCovered(ps, i) && optionalMatch(ps, actual, i);
            }
            seenPositionalCount++;
        }
        return allPositionalsCovered(ps, actual.parameterCount()); 
    }

    private boolean optionalMatch(Signature ps, ActualParameters actual, int start) throws CompilationException {
        // only optionals and possibly varargs left
        for (int i = start; i < actual.parameterCount(); i++) {
            ActualParameters.Entry e = actual.getParameterEntry(i);
            String name = e.getBinding();
            if (name == null) {
                return varargsMatch(ps, actual, i);
            }
            if (!ps.hasOptional(name, e.getType())) {
                return false;
            }
        }
        return true;
    }

    private boolean allPositionalsCovered(Signature ps, int count) {
        return ps.getPositionalCount() == count;
    }

    private boolean varargsMatch(Signature ps, ActualParameters actual, int start) throws CompilationException {
        // at this point no more keyword args are allowed
        if (!ps.hasVarArgs()) {
            return false;
        }
        actual.setVarargPositionStart(start);
        for (int i = start; i < actual.parameterCount(); i++) {
            ActualParameters.Entry e = actual.getParameterEntry(i);
            String name = e.getBinding();
            if (name != null) {
                throw new CompilationException("Illegal keyword argument: '" + name + "'");
            }
            if (!varargMatch(ps, e)) {
                return false;
            }
        }
        return true;
    }

    private boolean varargMatch(Signature ps, Entry e) {
        if (ps.hasVarArgs()) {
            if (e.getBinding() != null) {
                return false;
            }
            else if (!ps.getVarArgsType().isAssignableFrom(e.getType())) {
                return false;
            }
            else {
                return true;
            }
        }
        else {
            return false;
        }
    }

    private boolean returnsMatch(Signature ps, ActualParameters actuals, boolean functionContext) {
        if (ps.getReturns().size() != actuals.returnCount()) {
            return false;
        }
        // we either have library functions which don't name returns,
        // or user-defined functions/procs which do
        if (actuals.someReturnsHaveNames()) {
            if (!ps.returnsHaveNames()) {
                return false;
            }
            else {
                // all actual returns must have names and they must all match
                for (ActualParameters.Entry e : actuals.getReturns()) {
                    String name = e.getBinding();
                    if (name == null) {
                        return false;
                    }
                    else if (!ps.hasReturn(name, e.getType())) {
                        return false;
                    }
                }
            }
        }
        else {
            for (int i = 0; i < actuals.returnCount(); i++) {
                Signature.Parameter formal = ps.getReturns().get(i);
                ActualParameters.Entry actual = actuals.getReturn(i);
                // For functions, we need to know if
                // the formal type can be assigned to the actual type
                //
                // For procedures, returns are just arguments passed by
                // value, so we need to check if the actuals can be assigned to the formals
                //
                // This seems a bit paradoxical
                if (!functionContext) {
                    if (!formal.getType().isAssignableFrom(actual.getType())) {
                        return false;
                    }
                }
                else {
                    if (!formal.getType().canBeAssignedTo(actual.getType())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public Object find(String procName, FormalParameter[] inputArray) {
        return null;
    }

    public List<Signature> findAll(String name) {
        List<Signature> l = new ArrayList<Signature>();
        if (map.containsKey(name)) {
            l.addAll(map.get(name));
        }
        return l;
    }
}
