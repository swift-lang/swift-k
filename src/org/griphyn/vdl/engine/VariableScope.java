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


package org.griphyn.vdl.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.griphyn.vdl.karajan.CompilationException;


public class VariableScope {
    public enum EnclosureType {
        /** permit array up-assignment, but not entire variables */
        LOOP, 
        /** permit all upwards assignments */
        ALL, 
        /** permit no upwards assignments */
        NONE, 
        /** permit no access to the containing scope except for finding
        global variables */
        PROCEDURE,
        /** Override ENCLOSURE_LOOP to allow assignment inside a loop
         * based on some condition
         */
        CONDITION;
    }
    
    public enum WriteType {
        FULL, PARTIAL
    }
	
	public static final Logger logger = Logger.getLogger(VariableScope.class);

	/** need this for the program as a whole. probably should factor
	    that into separate pieces. */
	Karajan compiler;

	/** the scope that directly contains this scope. */
	VariableScope parentScope;

	/** the root level scope for the entire SwiftScript program. Sometimes
	    this will be the same as parentScope or this. */
	VariableScope rootScope;

	private EnclosureType enclosureType;

	/** The string template in which we will store statements
	    outputted into this scope. */
	public StringTemplate bodyTemplate;
	
	private List<String> outputs = new ArrayList<String>();
	private Set<String> inhibitClosing;
	private List<VariableScope> children = new LinkedList<VariableScope>();
	private XmlObject src;
	
	/** List of templates to be executed in sequence after the present
        in-preparation statement is outputted. */
    List<StringTemplate> presentStatementPostStatements =  new ArrayList<StringTemplate>();
    
    /** Set of variables (String token names) that are declared in
        this scope - not variables in parent scopes, though. */
    Map<String, Variable> variables;
    
    /**
     * Usage (such as writing or reading) in this scope
     */
    Map<String, VariableUsage> variableUsage;
    
    /**
     * If this is the scope of an 'else', then store a reference
     * to the corresponding 'then'
     */
    private VariableScope thenScope;
    
    private static class Variable {
        public final String name, type;
        public final XmlObject src;
        public final boolean isGlobal;
        private XmlObject foreachSrc;
        
        public Variable(String name, String type, boolean isGlobal, XmlObject src) {
            this.name = name;
            this.type = type;
            this.src = src;
            this.isGlobal = isGlobal;
        }
    }
    
    /** Stores information about a variable that is referred to in this
        scope. Should probably get used for dataset marking eventually. */
    private static class VariableUsage {
        private final String name;
        private XmlObject fullWritingLoc, foreachSourceVar;
        private List<XmlObject> partialWritingLoc;
        private List<XmlObject> fullReadingLoc;
        private int referenceCount;
        
        public VariableUsage(String name) {
            this.name = name;
        }
        
        public boolean addWritingStatement(XmlObject loc, WriteType writeType) throws CompilationException {
            if (writeType == WriteType.PARTIAL) {
                if (partialWritingLoc == null) {
                    partialWritingLoc = new LinkedList<XmlObject>();
                }
                if (!partialWritingLoc.contains(loc)) {
                    partialWritingLoc.add(loc);
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                if (fullWritingLoc != null) {
                    throw new CompilationException("Variable " + name + " is written to in two different locations:\n\t1. " 
                        + CompilerUtils.info(fullWritingLoc) + "\n\t2. " + CompilerUtils.info(loc));
                }
                else {
                    fullWritingLoc = loc;
                    return true;
                }
            }
        }

        public boolean hasWriters() {
            return fullWritingLoc != null || (partialWritingLoc != null && partialWritingLoc.size() > 0);
        }

        public void addFullReadingStatement(XmlObject where) {
            if (fullReadingLoc == null) {
                fullReadingLoc = new LinkedList<XmlObject>();
            }
            fullReadingLoc.add(where);
        }

        public boolean hasReaders() {
            return fullReadingLoc != null && fullReadingLoc.size() > 0;
        }

        public XmlObject getForeachSourceVar() {
            return foreachSourceVar;
        }

        public void setForeachSourceVar(XmlObject foreachSourceVar) {
            this.foreachSourceVar = foreachSourceVar;
        }
    }

	public VariableScope(Karajan c, VariableScope parent, XmlObject src) {
		this(c, parent, EnclosureType.ALL, src);
	}

	/** Creates a new variable scope.
		@param c the compiler scope in which this variable lives
		@param parent the enclosing scope, or null if this is a
			top level scope
		@param a specifies how assignments to variables made in enclosing
			scopes will be handled.
	*/
	public VariableScope(Karajan c, VariableScope parent, EnclosureType a, XmlObject src) {
	    if (logger.isDebugEnabled()) {
    		if (parentScope != null) {
    			logger.debug("New scope " + hashCode() + " with parent scope " + parentScope.hashCode());
    		} 
    		else {
    			logger.debug("New scope " + hashCode() + " with no parent.");
    		}
	    }
		compiler = c;
		parentScope = parent;
		enclosureType = a;
		if (parentScope == null) {
			rootScope = this;
		} 
		else {
			rootScope = parentScope.getRootScope();
			parentScope.addChild(this);
		}
		this.src = src;
	}


	public void addChild(VariableScope scope) {
	    children.add(scope);
    }
	
	public List<VariableScope> getChildren() {
	    return children;
	}
	
	public void setThen(VariableScope thenScope) {
	    this.thenScope = thenScope;
	    updateBranchReferenceCounts();
	}

	/** Asserts that a named variable is declared in this scope.
	    Might also eventually contain more information about the
	    variable. Need to define behaviour here when the
	    declaration already exists. Perhaps error in same scope and
	    warning if it shadows an outer scope? */

	public void addVariable(String name, String type, String context, XmlObject src) throws CompilationException {
		addVariable(name, type, context, false, src);
	}
	
	private Collection<VariableUsage> getVariableUsageValues() {
	    if (variableUsage == null) {
	        return Collections.emptyList();
	    }
	    else {
	        return variableUsage.values();
	    }
	}
	
	private Map<String, VariableUsage> getVariableUsage() {
	    if (variableUsage == null) {
	        variableUsage = new HashMap<String, VariableUsage>();
	    }
	    return variableUsage;
	}
	
	/**
	 * Get variable usage if it exists, otherwise return null
	 */
	protected VariableUsage getExistingUsage(String name) {
	    if (variableUsage == null) {
	        return null;
	    }
	    else {
	        return variableUsage.get(name);
	    }
	}
	
	private Collection<String> getUsedVariableNames() {
	    if (variableUsage == null) {
	        return Collections.emptyList();
	    }
	    else {
	        return variableUsage.keySet();
	    }
    }
	
	private Collection<String> getLocallyDeclaredVariableNames() {
        if (variables == null) {
            return Collections.emptyList();
        }
        else {
            return variables.keySet();
        }
    }

	
	/**
	 * Returns variable usage in this scope for the given name.
	 * If no previous usage exists, create it. This method
	 * guarantees a non-null return
	 */
	protected VariableUsage getUsageForUpdate(String name) {
	    Map<String, VariableUsage> usage = getVariableUsage();
	    VariableUsage u = usage.get(name);
	    if (u == null) {
	        u = new VariableUsage(name);
	        usage.put(name, u);
	    }
	    return u;
	}
	
	public void setForeachSourceVar(String name, XmlObject src) {
	    getUsageForUpdate(name).setForeachSourceVar(src);
    }
	
	public boolean isForeachSourceVar(String name) {
	    VariableUsage u = getExistingUsage(name);
	    return u != null && u.getForeachSourceVar() != null;
    }
	
	public void inhibitClosing(String name) {
	    if (inhibitClosing == null) {
	        inhibitClosing = new HashSet<String>();
	    }
		inhibitClosing.add(name);
		if (thenScope != null) {
		    setPreClose(name, 0);
		}
	}

	public void addVariable(String name, String type, String context, boolean global, XmlObject src) throws CompilationException {
	    if (logger.isDebugEnabled()) {
	        logger.debug("Adding variable " + name + " of type " + type + " to scope " + hashCode());
	    }
		
		if(isVariableDefined(name)) {
			throw new CompilationException("Variable " + name + ", on line " 
			    + CompilerUtils.getLine(src) + ", was already defined on line " + getDeclarationLine(name));
		}

		// TODO does this if() ever fire? or is it always taken
		// by the above if? in which case isVariableDefined should
		// be replaced by is locally defined test.
		if(parentScope != null && parentScope.isVariableDefined(name)) {
		    Warnings.warn(Warnings.Type.SHADOWING,  
		        context + " " + name + ", on line " + CompilerUtils.getLine(src)
		        + ", shadows variable of same name on line " + parentScope.getDeclarationLine(name));
		}

		if (global && this != rootScope) {
			throw new CompilationException("Global " + name + " can only be declared in the root scope of a program.");
		}
		
		getVariablesMap().put(name, new Variable(name, type, global, src));
	}
	
	private Map<String, Variable> getVariablesMap() {
	    if (variables == null) {
	        variables = new HashMap<String, Variable>();
	    }
	    return variables;
	}

    /**
	 * Does pretty much the same as addVariable() except it doesn't throw
	 * an exception if the variable is defined in a parent scope
	 */
	public void addInternalVariable(String name, String type, XmlObject src) throws CompilationException {
	    if (logger.isDebugEnabled()) {
	        logger.debug("Adding internal variable " + name + " of type " + type + " to scope " + hashCode());
	    }
		
		if(isVariableLocallyDefined(name)) {
			throw new CompilationException("Variable " + name + ", on line " 
			    + CompilerUtils.getLine(src) + ", was already defined on line " + getDeclarationLine(name));
		}

		getVariablesMap().put(name, new Variable(name, type, false, src));
	}
	
	public String getDeclarationLine(String name) {
	    XmlObject src;
	    if (rootScope.isGlobalDefined(name)) {
	        src = rootScope.getGlobalSrc(name);
	    }
	    else {
	        src = getSrcRecursive(name);
	    }
	    if (src != null) {
	        return CompilerUtils.getLine(src);
	    }
	    else {
	        return "unknown";
	    }
	}

	public boolean isVariableDefined(String name) {
		if(rootScope.isGlobalDefined(name)) return true;
		return isVariableVisible(name);
	}
	
	/**
	 * Recursively find information about a variable visible in this scope.
	 * Also, look for globals.
	 */
	public Variable lookup(String name) {
	    if (variables != null) {
	        Variable var = variables.get(name);
	        if (var != null) {
	            return var;
	        }
	    }
	    if (enclosureType != EnclosureType.PROCEDURE && parentScope != null) {
	        return parentScope.lookup(name);
        }
	    else {
	        // if the search fails, see if there is a global with this name
	        if (rootScope.variables != null) {
	            Variable v = rootScope.variables.get(name);
	            if (v != null && v.isGlobal) {
	                return v;
	            }
	            else {
	                return null;
	            }
	        }
	        else {
	            return null;
	        }
	    }
	}
	
	private XmlObject getSrcRecursive(String name) {
	    Variable var = lookup(name);
	    if (var == null) {
	        throw new IllegalArgumentException("Variable " + name + " is not visible in this scope");
	    }
	    return var.src;
	}

	private boolean isVariableVisible(String name) {
		if (isVariableLocallyDefined(name)) { 
		    return true;
		}
		if (enclosureType != EnclosureType.PROCEDURE && parentScope != null) {
		    return parentScope.isVariableVisible(name);
		}
		else {
		    return false;
		}
	}
		
	private StringTemplate getExistingDeclaration(String name) {
        if (isVariableLocallyDefined(name)) {
            return getLocalDeclaration(name);
        }
        else if (parentScope != null) {
            return parentScope.getExistingDeclaration(name);
        }
        else {
            return null;
        }
    }

	public boolean isGlobalDefined(String name) {
	    Variable var = rootScope.lookup(name);
	    return var != null && var.isGlobal;
	}
	
	public XmlObject getGlobalSrc(String name) {
	    Variable var = rootScope.lookup(name);
        if (var != null && var.isGlobal) {
            return var.src;
        }
        else {
            throw new IllegalArgumentException("'" + name + "' is not a global variable");
        }
	}

	public String getVariableType(String name) {
	    Variable var = lookup(name);
	    if (var != null) {
	        return var.type;
	    }
	    else {
	        throw new IllegalArgumentException("Variable " + name + " is not visible in this scope");
	    }
	}

	public boolean isVariableWriteable(String name, WriteType writeType) {
		if (isVariableLocallyDefined(name)) {
		    return true;
		}
		if (parentScope == null) {
		    // if we are the root scope and the variable is not declared here,
		    // then there is no other place to look
		    return false;
		}
		switch (enclosureType) {
		    case CONDITION:
		        if (parentScope.isVariableWriteable(name, WriteType.PARTIAL)) {
		            if (getTopmostLoopToDeclaration(name) != null) {
		                Warnings.warn(Warnings.Type.DATAFLOW, "Variable " + name + ", defined on line " + 
		                    getDeclarationLine(name) + ", might have multiple conflicting writers");
		            }
		            return true;
		        }
		        else {
		            return false;
		        }
		    case ALL:
		        return parentScope.isVariableWriteable(name, writeType);
		    case LOOP:
		        return parentScope.isVariableWriteable(name, writeType) && writeType == WriteType.PARTIAL;
		    default:
		        return false;
		}
	}


	/**
	 * Assumes that the variable is defined in some parent scope.
	 * Returns the top-most loop between this scope and the declaration
	 * or null if no such loop is found
	 */
	private VariableScope getTopmostLoopToDeclaration(String name) {
        if (isVariableLocallyDefined(name)) {
            return null;
        }
        else if (enclosureType == EnclosureType.LOOP) {
            VariableScope parentLoop = parentScope.getTopmostLoopToDeclaration(name);
            if (parentLoop != null) {
                return parentLoop;
            }
            else {
                return this;
            }
        }
        else {
            return parentScope.getTopmostLoopToDeclaration(name); 
        }
	}
	
	/**
	 * This deals with cases like this:
	 * a[]; a[0] = 1;
	 * foreach v, k in a {
	 *   if (c) {
	 *     a[k + 1] = 1;
	 *   }
	 * }
	 * ...
	 * foreach v, k in a {
	 *   if (c) {
     *     a[k + 100] = 1;
     *   }
     * }
	 * 
	 * a[] can be considered closed when all of the following are true:
	 *   1. the write reference count is equal to the number of loops
	 *      that both iterate on and write to a
	 *   2. none of those loops have any iterations running 
	 *   3. there are no elements in a[] left to iterate on
	 * These three conditions guarantee that no more writing
	 * can happen to a[].
	 * 
	 *  Unfortunately, it seems horribly complicated to deal with multiple
	 *  loops that do this. Simple reference counting doesn't work, since
	 *  any of the conditions above can become false at any point and it is
	 *  hard to have an absolute time for all of the loops.
	 *  
	 *  So engineering compromise: this is allowed for a single loop (per
	 *  array), and that's been shown to work (the self close scheme).
	 *  If used in more than one array, throw an error.
	 * @throws CompilationException 
	 */
	private void markInVariableUsedInsideLoop(String name) throws CompilationException {
	    if (isVariableLocallyDefined(name)) {
	        return;
	    }
	    switch (enclosureType) {
	        case PROCEDURE:
	        case NONE:
	            return;
	        case LOOP:
	            if (isForeachSourceVar(name)) {
	                // mark as selfClosing
	                Variable var = lookup(name);
	                if (var.foreachSrc != null) {
	                    throw new CompilationException("Updating of an array (" + 
	                        name + ", line " + getDeclarationLine(name) + ") inside a " + 
	                        "foreach loop that iterates over it is limited to a single " + 
	                        "loop, but it is used in both " + CompilerUtils.info(var.foreachSrc) + 
	                        " and " + CompilerUtils.info(src));
	                }
	                else {
	                    setSelfClose();
	                }
	            }
	            // also add partial close
	            
	        default:
	            if (parentScope != null) {
	                parentScope.markInVariableUsedInsideLoop(name);
	            }
	    }
    }

    private void setSelfClose() {
        bodyTemplate.setAttribute("selfClose", "true");
    }

    public boolean isVariableLocallyDefined(String name) {
		return variables != null && variables.containsKey(name);
	}

	/** indicates that the present in-preparation statement writes to the
	    named variable. If the variable is declared in the local scope,
	    register a closing statement; otherwise, record that this scope
	    writes to the variable so that the scope-embedding code (such as
	    the foreach compiler) can handle appropriately. */
	public void addWriter(String variableName, WriteType writeType, 
	        XmlObject where, StringTemplate out) throws CompilationException {
		if (!isVariableDefined(variableName)) {
			throw new CompilationException("Variable " + variableName + " is not defined");
		}
		
		if (logger.isDebugEnabled()) {
		    logger.debug("Writer " + variableName + " " + CompilerUtils.info(where));
		}

		if (isVariableWriteable(variableName, writeType)) {
	        setVariableWrittenToInThisScope(variableName, writeType, where, out);
		} 
		else {
		    isVariableWriteable(variableName, writeType);
			throw new CompilationException("variable " + variableName + " is not writeable in this scope");
		}
	}
	
	public void partialClose(String name, XmlObject src, StringTemplate out) {
	    if (inhibitClosing == null || !inhibitClosing.contains(name)) {
	        StringTemplate decl = getExistingDeclaration(name);
	        if (decl != null) {
	            // a function return value will result
	            // in a missing declaration
	            // TODO fix that
	            incWaitCount(name);
	        
	            StringTemplate postST = compiler.template("partialclose");
	            postST.setAttribute("var", name);
	            
	            out.setAttribute("partialClose", postST);
	        }
        }
	}
	
	public boolean hasPartialClosing() {
	    if (enclosureType != EnclosureType.CONDITION || thenScope != null) {
	        throw new IllegalStateException("hasPartialClosing should only be called in a 'then' condition scope");
	    }
	    for (String name : getUsedVariableNames()) {
	        VariableUsage v = getExistingUsage(name);
	        if (v != null && v.referenceCount > 0) {
	            return true;
	        }
	    }
	    return false;
	}
			
	private void setPreClose(String name, int count) {
	    if (inhibitClosing != null && inhibitClosing.contains(name) && count != 0) {
	        return;
	    }
	    setCount("preClose", name, count, 
	        new RefCountSetter<StringTemplate>() {
	            @Override
                public StringTemplate create(String name, int count) {
	                StringTemplate t = compiler.template("partialclose");
	                t.setAttribute("var", name);
	                if (count != 1) {
	                    t.setAttribute("count", count);
	                }
	                return t;
                }
	            
                @Override
                public void set(StringTemplate t, int count) {
                    t.removeAttribute("count");
                    if (count != 1) {
                        // 1 is the default count, so no need to have it explicitly
                        t.setAttribute("count", count);
                    }
                }

                @Override
                public boolean matches(StringTemplate t, String name) {
                    return t.getAttribute("var").equals(name);
                }
	    });
	}
	
	private void setLoopReferenceCount(String name, int count) {
        setCount("refs", name, count, new RefCountSetter<SimpleRefCountRep>() {
            @Override
            public SimpleRefCountRep create(String name, int count) {
                return new SimpleRefCountRep(name, count);
            }

            @Override
            public boolean matches(SimpleRefCountRep t, String name) {
                return t.name.equals(name);
            }

            @Override
            public void set(SimpleRefCountRep t, int count) {
                t.count = count;
            }
        });
    }
	
	private static class SimpleRefCountRep {
	    public final String name;
	    public int count;
	    
	    public SimpleRefCountRep(String name, int count) {
	        this.name = name;
	        this.count = count;
	    }
	    
	    public String toString() {
	        return name + " " + count;
	    }
	}
	
	private static interface RefCountSetter<T> {
	    T create(String name, int count);
	    void set(T o, int count);
	    boolean matches(T t, String name);
	}
	
	@SuppressWarnings("unchecked")
    private <T> void setCount(String attrName, String name, int count, RefCountSetter<T> r) {
   	    Object o = bodyTemplate.getAttribute(attrName);
   	    List<T> pcs;
   	    T matching = null;
   	    if (o instanceof List) {
   	        pcs = (List<T>) o;
   	        Iterator<T> i = pcs.iterator();
   	        while (i.hasNext()) {
   	            T st = i.next();
   	            if (r.matches(st, name)) {
   	                if (count == 0) {
   	                    i.remove();
   	                }
   	                else {
   	                    matching = st;
   	                    break;
   	                }
   	            }
   	        }
   	    }
   	    else if (o != null) {
   	        T st = (T) o;
   	        if (r.matches(st, name)) {
   	            if (count == 0) {
   	                bodyTemplate.removeAttribute("preClose");
   	            }
   	            else {
   	                matching = st;
   	            }
   	        }
   	    }
   	    if (count != 0 ) {
   	        // add
   	        if (matching == null) {
   	            bodyTemplate.setAttribute(attrName, r.create(name, count));
   	        }
   	        else {
	            r.set(matching, count);
   	        }
   	    }
	}
	
	private void incWaitCountLocal(String name) {
	    StringTemplate decl = getLocalDeclaration(name);
	    Integer i = (Integer) decl.getAttribute("waitCount");
	    if (i == null) {
	        i = 1;
	    }
	    else {
	        i = i + 1;
	        decl.removeAttribute("waitCount");
	    }
	    decl.setAttribute("waitCount", i);
    }

    private void incWaitCount(String name) {
	    if (this.isVariableLocallyDefined(name)) {
	        incWaitCountLocal(name);
	    }
	    else {
	        boolean propagateToParent = true;
	        if (enclosureType == EnclosureType.CONDITION) {
	            // due to the parsing order, the
	            // then scopes are done before the else scopes, 
	            // so no need to update counts in subling else scopes
	            // if this is a then scope
	            if (thenScope == null) {
	                // we are then 'then' scope
	                incVariableReferenceCount(name);
	            }
	            else {
	                // else scope
	                int myCount = incVariableReferenceCount(name);
	                int thenCount = thenScope.getVariableReferenceCount(name);
	                if (myCount > thenCount) {
	                    thenScope.setPreClose(name, myCount - thenCount);
	                    this.setPreClose(name, 0);
	                }
	                else {
	                    this.setPreClose(name, thenCount - myCount);
	                    thenScope.setPreClose(name, 0);
	                    // this has already been accounted for in the then branch
	                    // so skip propagating it up
	                    propagateToParent = false;
	                }
	            }
	        }
	        else if (enclosureType == EnclosureType.LOOP) {
	            // issue statements to dynamically increment wait
	            // count on each iteration
	            addLoopReferenceCount(name);
	        }
	        // keep going until we hit the declaration
	        if (propagateToParent && parentScope != null) {
	            parentScope.incWaitCount(name);
	        }
	    }
    }
    
    /**
     * This is only called by setThen(scope), so guaranteed to
     * be an 'else' scope
     */
    private void updateBranchReferenceCounts() {
        for (VariableUsage v : thenScope.getVariableUsageValues()) {
            if (v.referenceCount != 0) {
                setPreClose(v.name, v.referenceCount);
            }
        }
    }

    public void addReader(String name, boolean partial, XmlObject where) throws CompilationException {
	    setVariableReadInThisScope(name, partial, where);
	    if (!partial) {
	        if (logger.isDebugEnabled()) {
	            logger.debug(name + " fully read in " + CompilerUtils.info(where));
	        }
	    }
	}
	
	private void setVariableWrittenToInThisScope(String name, WriteType writeType, XmlObject where, StringTemplate out)
	throws CompilationException {
	    VariableUsage variable = getUsageForUpdate(name);
	    boolean added = variable.addWritingStatement(where, writeType);
	    if (added) {
	        partialClose(name, where, out);
	        setNonInput(name);
	    }
	    markInVariableUsedInsideLoop(name);
    }
	
	private void setNonInput(String name) {
	    StringTemplate decl = getExistingDeclaration(name);
	    if (decl != null) {
	        decl.removeAttribute("input");
	    }
    }

    private void setVariableReadInThisScope(String name, boolean partial, XmlObject where)
    throws CompilationException {
	    // only care about full reads, since they can only happen in
	    // a scope higher than the closing
	    if (partial) {
	        return;
	    }
        getUsageForUpdate(name).addFullReadingStatement(where);
    }
	
	private boolean isVariableWrittenToInThisScope(String name) {
	    VariableUsage v = getExistingUsage(name);
	    return v != null && v.hasWriters();
	}
	
	private int incVariableReferenceCount(String name) {
	    return ++getUsageForUpdate(name).referenceCount;
    }
	
	private void addLoopReferenceCount(String name) {
        int count = incVariableReferenceCount(name);
        setLoopReferenceCount(name, count);
    }
	
	private int getVariableReferenceCount(String name) {
        VariableUsage v = getExistingUsage(name);
        if (v == null) {
            return 0;
        }
        else {
            return v.referenceCount;
        }
    }
	
	private boolean isVariableFullyReadInThisScope(String name) {
        VariableUsage v = getExistingUsage(name);
        return v != null && v.hasReaders();
    }
	
	private XmlObject getFirstFullRead(String name) {
        VariableUsage v = getExistingUsage(name);
        if (v == null) {
            return null;
        }
        if (v.fullReadingLoc == null) {
            return null;
        }
        if (v.fullReadingLoc.isEmpty()) {
            return null;
        }
        return v.fullReadingLoc.get(0);
    }
	
	private XmlObject getFirstWrite(String name) {
        VariableUsage v = getExistingUsage(name);
        if (v == null) {
            return null;
        }
        if (v.fullWritingLoc != null) {
            return v.fullWritingLoc;
        }
        if (v.partialWritingLoc == null) {
            return null;
        }
        if (v.partialWritingLoc.isEmpty()) {
            return null;
        }
        return v.partialWritingLoc.get(0);
    }
	
	private List<XmlObject> getAllWriters(String name) {
	    VariableUsage v = getExistingUsage(name);
	    if (v == null) {
	        throw new IllegalArgumentException("Variable " + name + " is not written to in this scope");
	    }
	    return v.partialWritingLoc;
	}
	
	StringTemplate getLocalDeclaration(String name) {
	    StringTemplate st = getLocalDeclaration(name, "declarations");
	    /* 
	     * procedure scopes go like this:
         * outerScope - contains all arguments - PROCEDURE
         * innerScope - contains all returns - NONE
         * compoundScope - this is where the proc template lives - ALL
         * this prevents writing to arguments, since writing upwards of NONE is prohibited,
         * but allows writing to return values
         */

	    if (st == null && parentScope != null && parentScope.enclosureType == EnclosureType.PROCEDURE) {
	        if (children.size() != 1) {
	            throw new IllegalStateException("Procedure scope with more than one child found");
	        }
	        return children.get(0).getLocalDeclaration(name, "initWaitCounts");
	    }
	    else {
	        return st;
	    }
	}

    /** looks up the template that declared the named variable in the
	    present scope. If such a template does not exist, returns null.
	    TODO this should probably merge into general variable structure
	    rather then walking the template.
	*/
	StringTemplate getLocalDeclaration(String name, String type) {
	    if (bodyTemplate == null) {
	        return null;
	    }
		Object decls = bodyTemplate.getAttribute(type);
		if (decls == null) {
		    return null;
		}

		if (decls instanceof StringTemplate) {
			StringTemplate declST = (StringTemplate) decls;
			if (declST.getAttribute("name").equals(name)) {
			    if (logger.isDebugEnabled()) {
			        logger.debug("Found declaration for " + name);
			    }
				return declST;
			}
		} 
		else { // assume its a List of StringTemplate
		     @SuppressWarnings("unchecked")
		     List<StringTemplate> list = (List<StringTemplate>) decls;
		     for (StringTemplate declST : list) { 
		         if (logger.isDebugEnabled()) {
		             logger.debug("looking at declaration " + declST);
		         }
		         try {
		             if (declST.getAttribute("name").equals(name)) {
		                 if (logger.isDebugEnabled()) {
		                     logger.debug("Found declaration for " + name);
		                 }
		                 return declST;
		             }
		         } 
		         catch (java.util.NoSuchElementException nse) {
		             logger.debug("it so definitely wasn't in that one, we got an exception.");
		             // TODO this is not a nice use of exceptions...
		         }
		     }
		}
		
		if (logger.isInfoEnabled()) {
		    logger.info("Couldn't find local definition for " + name);
		}
		return null;
	}

	/** appends statement to the present scope, wrapping as appropriate
	    with code to partially-close registered uses. perhaps should move
	    into own Statement class? */

	void appendStatement(StringTemplate statementST) {
		StringTemplate wrapperST = compiler.template("sequential");
		bodyTemplate.setAttribute("statements", wrapperST);
		if (!outputs.isEmpty()) {
            StringTemplate unitStart = compiler.template("unitStart");
            unitStart.setAttribute("type", "SCOPE");
            unitStart.setAttribute("outputs", join(outputs));
            wrapperST.setAttribute("statements", unitStart);
            StringTemplate unitEnd = compiler.template("unitEnd");
            unitEnd.setAttribute("type", "SCOPE");
            presentStatementPostStatements.add(unitEnd);
            if ("foreach".equals(statementST.getName()) ||
                    "if".equals(statementST.getName()) ||
                    "iterate".equals(statementST.getName())) {
                statementST.setAttribute("trace", Boolean.TRUE);
            }
        }
		wrapperST.setAttribute("statements",statementST);
		Iterator<StringTemplate> it = presentStatementPostStatements.iterator();
		while(it.hasNext()) {
			wrapperST.setAttribute("statements", it.next());
		}
		presentStatementPostStatements.clear();
		outputs.clear();
		// Cannot set inhibitClosing to null here, since
		// it may break then/else linked closing. See bug 927
	}

	private String join(List<String> l) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> i = l.iterator();
        while (i.hasNext()) {
            sb.append(i.next());
            if (i.hasNext()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
	
	public VariableScope getRootScope() {
		return rootScope;
	}

    public List<String> getCleanups() {
        List<String> cleanups = new ArrayList<String>();
        for (String var : getLocallyDeclaredVariableNames()) {
            String type = getVariableType(var);
            if (!org.griphyn.vdl.type.Types.isPrimitive(type)) {
                cleanups.add(var);
            }
        }
        return cleanups;
    }
    
    public String toString() {
        return CompilerUtils.info(src);
    }
    
    /**
     * @throws CompilationException 
     */
    public void analyzeWriters() throws CompilationException {
        // find variables that are read but not written to
        
        // keep a stack to account for shadowing
        Map<String, Stack<Usage>> access = new HashMap<String, Stack<Usage>>();
        
        analyzeWriters(access);
    }
    
    private static class Usage {
        public VariableScope lastWriter, lastReader;
    }

    private void analyzeWriters(Map<String, Stack<Usage>> access) throws CompilationException {
        for (String name : getLocallyDeclaredVariableNames()) {
            declare(access, name);
        }
        
        // parent first, which ensures that the topmost writer
        // is always first
        for (Map.Entry<String, Stack<Usage>> e : access.entrySet()) {
            String name = e.getKey();
            Stack<Usage> stack = e.getValue();
            
            boolean fullyRead = isVariableFullyReadInThisScope(name);
                        
            if (isVariableWrittenToInThisScope(name)) {
                stack.peek().lastWriter = this;
            }
            
            if (fullyRead && org.griphyn.vdl.type.Types.isPrimitive(getVariableType(name))) {
                stack.peek().lastReader = this;
            }
        }
        
        for (VariableScope child : children) {
            child.analyzeWriters(access);
        }

        for (String name : getLocallyDeclaredVariableNames()) {
            undeclare(access, name);
        }
    }

    private void declare(Map<String, Stack<Usage>> access, String name) {
        Stack<Usage> s = access.get(name);
        if (s == null) {
            s = new Stack<Usage>();
            access.put(name, s);
        }
        Usage u = new Usage();
        s.push(u);
    }
    
    private void undeclare(Map<String, Stack<Usage>> access, String name) throws CompilationException {
        Stack<Usage> s = access.get(name);
        if (s == null || s.isEmpty()) {
            throw new RuntimeException("Mistmatched undeclare for " + name + " in scope " + this);
        }
        Usage u = s.pop();
        if (s.isEmpty()) {
            access.remove(name);
        }
        if (u.lastReader != null && u.lastWriter == null) {
            throw new CompilationException("Uninitalized variable: " + name);
        }
    }
}

