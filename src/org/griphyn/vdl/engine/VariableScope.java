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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.log4j.Logger;
import org.globus.swift.parsetree.Node;
import org.griphyn.vdl.compiler.intermediate.IConditionBranch;
import org.griphyn.vdl.compiler.intermediate.IForeach;
import org.griphyn.vdl.compiler.intermediate.IFormalParameter;
import org.griphyn.vdl.compiler.intermediate.ILoop;
import org.griphyn.vdl.compiler.intermediate.INode;
import org.griphyn.vdl.compiler.intermediate.IProcedureDeclaration;
import org.griphyn.vdl.compiler.intermediate.IRefCounted;
import org.griphyn.vdl.compiler.intermediate.IStatement;
import org.griphyn.vdl.compiler.intermediate.IStatementContainer;
import org.griphyn.vdl.compiler.intermediate.IVariableDeclaration;
import org.griphyn.vdl.karajan.CompilationException;
import org.griphyn.vdl.type.Type;


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
    
    public enum AccessType {
        LOCAL, GLOBAL
    }
    
    public enum VariableOrigin {
        // auto is added as part of a foreach or iterate
        USER, INTERNAL, AUTO
    }
    
    public enum CountType {
        READ, WRITE;
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
	
	private IStatementContainer owner;
	
	private List<VariableScope> children = new LinkedList<VariableScope>();
	private Node src;
	
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
        
    private static class Variable {
        public final String name;
        public final Type type;
        public final Node src;
        public final AccessType accessType;
        public final VariableOrigin origin;
        private Node foreachSrc;
        public boolean unused;
        public IRefCounted declaration;
        
        public Variable(String name, Type type, AccessType accessType, VariableOrigin origin, Node src, IRefCounted declaration) {
            this.name = name;
            this.type = type;
            this.src = src;
            this.accessType = accessType;
            this.origin = origin;
            this.declaration = declaration;
        }

        public boolean isGlobal() {
            return accessType == AccessType.GLOBAL;
        }
    }
    
    public static final int FULL_WRITE_COUNT = Integer.MAX_VALUE;
    
    /** Stores information about a variable that is referred to in this
        scope. Should probably get used for dataset marking eventually. */
    public static class VariableUsage {
        private final String name;
        private Node fullWritingLoc, foreachSourceVar;
        private List<Node> partialWritingLoc;
        private List<Node> fullReadingLoc;
        private boolean partiallyRead;
        private int writeReferenceCount, readReferenceCount;
        
        public VariableUsage(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }

        public boolean addWritingStatement(Node loc, WriteType writeType) throws CompilationException {
            if (writeType == WriteType.PARTIAL) {
                if (partialWritingLoc == null) {
                    partialWritingLoc = new LinkedList<Node>();
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
                writeReferenceCount = FULL_WRITE_COUNT;
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

        public void addFullReadingStatement(Node where) {
            if (fullReadingLoc == null) {
                fullReadingLoc = new LinkedList<Node>();
            }
            fullReadingLoc.add(where);
        }

        public boolean hasFullReaders() {
            return fullReadingLoc != null && fullReadingLoc.size() > 0;
        }
        
        public boolean hasReaders() {
            return partiallyRead || (fullReadingLoc != null && fullReadingLoc.size() > 0);
        }

        public Node getForeachSourceVar() {
            return foreachSourceVar;
        }

        public void setForeachSourceVar(Node foreachSourceVar) {
            this.foreachSourceVar = foreachSourceVar;
        }

        public void addPartialReadingStatement(Node where) {
            this.partiallyRead = true;
        }

        public int getReferenceCount(CountType countType) {
            switch (countType) {
                case READ:
                    return this.readReferenceCount;
                case WRITE:
                    return this.writeReferenceCount;
            }
            return 0;
        }

        public int incReferenceCount(CountType countType, int amount) {
            switch (countType) {
                case READ:
                    return this.readReferenceCount += amount;
                case WRITE:
                    if (this.writeReferenceCount != FULL_WRITE_COUNT) {
                        return this.writeReferenceCount += amount;
                    }
                    else {
                        return FULL_WRITE_COUNT;
                    }
            }
            return 0;
        }
    }

	public VariableScope(Karajan c, VariableScope parent, Node src) {
		this(c, parent, EnclosureType.ALL, src);
	}

	/** Creates a new variable scope.
		@param c the compiler scope in which this variable lives
		@param parent the enclosing scope, or null if this is a
			top level scope
		@param a specifies how assignments to variables made in enclosing
			scopes will be handled.
	*/
	public VariableScope(Karajan c, VariableScope parent, EnclosureType a, Node src) {
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
	
	/** Asserts that a named variable is declared in this scope.
	    Might also eventually contain more information about the
	    variable. Need to define behaviour here when the
	    declaration already exists. Perhaps error in same scope and
	    warning if it shadows an outer scope? */

	public void addVariable(String name, Type type, String context, 
	        VariableOrigin origin, Node src, IRefCounted declaration) throws CompilationException {
		addVariable(name, type, context, AccessType.LOCAL, origin, src, declaration);
	}
	
	public Collection<VariableUsage> getVariableUsageValues() {
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
	    if (name == null) {
	        throw new NullPointerException();
	    }
	    Map<String, VariableUsage> usage = getVariableUsage();
	    VariableUsage u = usage.get(name);
	    if (u == null) {
	        u = new VariableUsage(name);
	        usage.put(name, u);
	    }
	    return u;
	}
	
	public void setForeachSourceVar(String name, Node src) {
	    getUsageForUpdate(name).setForeachSourceVar(src);
    }
	
	public boolean isForeachSourceVar(String name) {
	    VariableUsage u = getExistingUsage(name);
	    return u != null && u.getForeachSourceVar() != null;
    }
	
	public void addVariable(String name, Type type, String context, 
	        AccessType accessType, VariableOrigin origin, Node src, IRefCounted declaration) throws CompilationException {
	    if (logger.isDebugEnabled()) {
	        logger.debug("Adding variable " + name + " of type " + type + " to scope " + hashCode());
	    }
		
		if (isVariableDefined(name)) {
			throw new CompilationException("Variable " + name + ", on line " 
			    + src.getLine() + ", was already defined on line " + getDeclarationLine(name));
		}

		// TODO does this if() ever fire? or is it always taken
		// by the above if? in which case isVariableDefined should
		// be replaced by is locally defined test.
		if (parentScope != null && parentScope.isVariableDefined(name)) {
		    Warnings.warn(Warnings.Type.SHADOWING,  
		        context + " " + name + ", on line " + src.getLine()
		        + ", shadows variable of same name on line " + parentScope.getDeclarationLine(name));
		}

		if (accessType == AccessType.GLOBAL && this != rootScope) {
			throw new CompilationException("Global " + name + " can only be declared in the root scope of a program.");
		}
		
		getVariablesMap().put(name, new Variable(name, type, accessType, origin, src, declaration));
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
	public void addInternalVariable(String name, Type type, Node src) throws CompilationException {
	    if (logger.isDebugEnabled()) {
	        logger.debug("Adding internal variable " + name + " of type " + type + " to scope " + hashCode());
	    }
		
		if(isVariableLocallyDefined(name)) {
			throw new CompilationException("Variable " + name + ", on line " 
			    + src.getLine() + ", was already defined on line " + getDeclarationLine(name));
		}

		getVariablesMap().put(name, new Variable(name, type, AccessType.LOCAL, VariableOrigin.INTERNAL, src, null));
	}
	
	public int getDeclarationLine(String name) {
	    Node src;
	    if (rootScope.isGlobalDefined(name)) {
	        src = rootScope.getGlobalSrc(name);
	    }
	    else {
	        src = getSrcRecursive(name);
	    }
	    if (src != null) {
	        return src.getLine();
	    }
	    else {
	        return -1;
	    }
	}

	public boolean isVariableDefined(String name) {
		if(rootScope.isGlobalDefined(name)) return true;
		return isVariableVisible(name);
	}
	
	public VariableOrigin getOrigin(String name) {
	    return lookup(name).origin;
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
	            if (v != null && v.isGlobal()) {
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
	
	public IRefCounted getDeclaration(String name) {
        Variable var = lookup(name);
        if (var == null) {
            return null;
        }
        else {
            return var.declaration;
        }
    }
	
	public boolean isGlobal(String name) {
	    Variable var = lookup(name);
	    if (var == null) {
            throw new IllegalArgumentException("Variable " + name + " is not visible in this scope");
        }
        else {
            return var.isGlobal();
        }
	}
	
	public void setDeclaration(String name, IRefCounted declaration) {
        Variable var = lookup(name);
        if (var == null) {
            throw new IllegalArgumentException("Variable " + name + " is not visible in this scope");
        }
        else {
            var.declaration = declaration;
        }
    }
		
	private Node getSrcRecursive(String name) {
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
		
	private IRefCounted getExistingDeclaration(String name) {
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
	    return var != null && var.isGlobal();
	}
	
	public Node getGlobalSrc(String name) {
	    Variable var = rootScope.lookup(name);
        if (var != null && var.isGlobal()) {
            return var.src;
        }
        else {
            throw new IllegalArgumentException("'" + name + "' is not a global variable");
        }
	}

	public Type getVariableType(String name) {
	    Variable var = lookup(name);
	    if (var != null) {
	        return var.type;
	    }
	    else {
	        throw new IllegalArgumentException("Variable " + name + " is not visible in this scope");
	    }
	}
	
	public VariableOrigin getVariableOrigin(String name) {
        Variable var = lookup(name);
        if (var != null) {
            return var.origin;
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
	                    ((IForeach) owner).setSelfClose(true);
	                }
	            }
	            // also add partial close
	        default:
	            if (parentScope != null) {
	                parentScope.markInVariableUsedInsideLoop(name);
	            }
	    }
    }
	
	private void propagateReads(String name) throws CompilationException {
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
                        ((IForeach) owner).setSelfClose(true);
                    }
                }
                // also add partial close
            default:
                if (parentScope != null) {
                    parentScope.markInVariableUsedInsideLoop(name);
                }
        }
    }

  
    public boolean isVariableLocallyDefined(String name) {
		return variables != null && variables.containsKey(name);
	}
    
    public void addWriter(String variableName, WriteType writeType, 
            Node where, INode out) throws CompilationException {
        addWriters(variableName, writeType, where, out, 1);
    }

	/** indicates that the present in-preparation statement writes to the
	    named variable. If the variable is declared in the local scope,
	    register a closing statement; otherwise, record that this scope
	    writes to the variable so that the scope-embedding code (such as
	    the foreach compiler) can handle appropriately. */
	public void addWriters(String variableName, WriteType writeType, 
	        Node where, INode out, int count) throws CompilationException {
		if (!isVariableDefined(variableName)) {
			throw new CompilationException("Variable " + variableName + " is not defined");
		}
		
		if (logger.isDebugEnabled()) {
		    logger.debug("Writer " + variableName + " " + CompilerUtils.info(where));
		}

		if (isVariableWriteable(variableName, writeType)) {
	        setVariableWrittenToInThisScope(variableName, writeType, where, out, count);
		} 
		else {
		    isVariableWriteable(variableName, writeType);
			throw new CompilationException("variable " + variableName + " is not writeable in this scope");
		}
	}
	
	public void markInitialized(String name, Node where) throws CompilationException {
        if (!isVariableDefined(name)) {
            throw new CompilationException("Variable " + name + " is not defined");
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("markInitialized " + name + " " + CompilerUtils.info(where));
        }
        
        VariableUsage variable = getUsageForUpdate(name);
        variable.addWritingStatement(where, WriteType.FULL);
    }

	
	public void partialCount(String name, Node src, INode out, CountType countType, int amount) {
        incCount(name, countType, amount);
        switch (countType) {
        	case READ:
        		break;
        	case WRITE:
        	    if (amount != FULL_WRITE_COUNT) {
        	        ((IStatement) out).addPartialClose(name);
        	    }
                break;
        }
	}
	
	public boolean hasPartialCount() {
	    return hasPartialCount(CountType.READ) || hasPartialCount(CountType.WRITE); 
	}
	
	public boolean hasPartialCount(CountType countType) {
	    for (String name : getUsedVariableNames()) {
	        VariableUsage v = getExistingUsage(name);
	        if (v != null) {
	            int refCount = v.getReferenceCount(countType);
	            if (refCount > 0 && refCount != FULL_WRITE_COUNT) {
	                return true;
	            }
	            else {
	                return false;
	            }
	        }
	    }
	    return false;
	}
				
	private void setPreCount(String name, int count, CountType countType) {
	    String attrName = null;
	    String templateName = null;
	    switch (countType) {
	        case READ:
	            ((IConditionBranch) owner).setPreClean(name, count);
                break;
	        case WRITE:
	            ((IConditionBranch) owner).setPreClose(name, count);
	            break;
	    }
	}
	
	
	private void addLoopWriteReferenceCount(String name, int delta) {
	    ((ILoop) owner).addWRef(name, delta);
    }
	
	private void addLoopReadReferenceCount(String name, int delta) {
        ((ILoop) owner).addRRef(name, delta);
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
	
	
	private void incReadCountLocal(String name, int amount) {
	    IRefCounted decl = getLocalDeclaration(name);
        if (decl != null) {
            decl.incReadCount(amount);
        }
        else if (enclosureType == EnclosureType.NONE) {
            IFormalParameter param = ((IProcedureDeclaration) parentScope.owner).getReturn(name);
            if (param != null) {
                param.incWriteCount(amount);
            }
        }
	}
	
	
	private void incWaitCountLocal(String name, int amount) {
	    /* 
         * procedure scopes go like this:
         * outerScope - contains all arguments - PROCEDURE
         * innerScope - contains all returns - NONE
         * compoundScope - this is where the proc template lives - ALL
         * this prevents writing to arguments, since writing upwards of NONE is prohibited,
         * but allows writing to return values
         */
	    
	    // wait count can be set either on a declaration or a function return
	    IRefCounted decl = getLocalDeclaration(name);
	    if (decl != null) {
	        decl.incWriteCount(amount);
	    }
	    else if (enclosureType == EnclosureType.NONE) {
	        IFormalParameter param = ((IProcedureDeclaration) parentScope.owner).getReturn(name);
	        if (param != null) {
	            param.incWriteCount(amount);
	        }
	    } 
    }
	
	private void addParameterReadReferenceCount(String name) {
        int count = incVariableReferenceCount(name, CountType.READ, 1);
    }
	
	public void incReadCount(String name, int amount) {
	    incCount(name, CountType.READ, amount);
	}

    private void incCount(String name, CountType countType, int amount) {
	    if (this.isVariableLocallyDefined(name)) {
	        incCountLocal(name, countType, amount);
	    }
	    else {
	        boolean propagateToParent = true;
	        switch (enclosureType) {
	            case PROCEDURE:
	                break;
	            case NONE:
	                // only appears as first and only descendant of a PROCEDURE scope
	                if (countType == CountType.READ) {
	                    // inner scope has a reference to the IProcedureDeclaration; see Karajan.procedure()
	                    IProcedureDeclaration iProc = (IProcedureDeclaration) owner;
	                    if (iProc.hasParameterOrReturn(name)) {
                            addParameterReadReferenceCount(name);
                            iProc.addParameterRead(name, amount);
	                    }
	                    propagateToParent = false;
                    }
	                break;
	            case CONDITION:
    	            // will propagate when balancing writers
	                propagateToParent = false;
	                addConditionReferenceCount(name, countType, amount);
    	            break;
	            case LOOP:
    	            // issue statements to dynamically increment wait
    	            // count on each iteration
	                Variable var = lookup(name);
	                if (var != null && var.origin == VariableOrigin.AUTO) {
	                    // ignore iteration variables
	                    return;
	                }
    	            addLoopReferenceCount(name, countType, amount);
    	            break;
	        }
	        // keep going until we hit the declaration
	        if (propagateToParent && parentScope != null) {
	            parentScope.incCount(name, countType, amount);
	        }
	    }
    }
    
    private void incCountLocal(String name, CountType countType, int amount) {
        switch (countType) {
            case READ:
                incReadCountLocal(name, amount);
                break;
            case WRITE:
                incWaitCountLocal(name, amount);
                break;
        }
    }
    
    public void addReader(String name, boolean partial, Node where, INode who) throws CompilationException {
        addReaders(name, partial, where, who, 1);
    }

    public void addReaders(String name, boolean partial, Node where, INode who, int count) 
            throws CompilationException {
        
	    setVariableReadInThisScope(name, partial, where, who, count);
	    if (!partial) {
	        if (logger.isDebugEnabled()) {
	            logger.debug(name + " fully read in " + CompilerUtils.info(where));
	        }
	    }
	}
	
	private void setVariableWrittenToInThisScope(String name, WriteType writeType, Node where, INode out, int count)
	        throws CompilationException {
	    
	    VariableUsage variable = getUsageForUpdate(name);
	    boolean added = variable.addWritingStatement(where, writeType);
	    if (added) {
	        if (writeType != WriteType.FULL) {
	            partialCount(name, where, out, CountType.WRITE, count);
	        }
	        else {
	            partialCount(name, where, out, CountType.WRITE, FULL_WRITE_COUNT);
	        }
	        setNonInput(name);
	    }
	    markInVariableUsedInsideLoop(name);
    }
	
	private void setNonInput(String name) {
	    IRefCounted decl = getExistingDeclaration(name);
	    if (decl instanceof IVariableDeclaration) {
	        ((IVariableDeclaration) decl).setInput(false);
	    }
    }

    private void setVariableReadInThisScope(String name, boolean partial, Node where, INode who, int count)
            throws CompilationException {
        
        VariableUsage var = getUsageForUpdate(name);
	    if (partial) {
	        var.addPartialReadingStatement(where);
	    }
	    else {
	        var.addFullReadingStatement(where);
	    }
	    partialCount(name, where, who, CountType.READ, count);
    }
	
    private boolean isVariableWrittenToInThisScope(String name) {
	    VariableUsage v = getExistingUsage(name);
	    return v != null && v.hasWriters();
	}
	
	private int incVariableReferenceCount(String name, CountType countType, int amount) {
	    return getUsageForUpdate(name).incReferenceCount(countType, amount);
    }
	
	private void addConditionReferenceCount(String name, CountType countType, int amount) {
	    int count = incVariableReferenceCount(name, countType, amount);
	}
	
	private void addLoopReferenceCount(String name, CountType countType, int amount) {
        int count = incVariableReferenceCount(name, countType, amount);
        switch (countType) {
        	case READ:
        		addLoopReadReferenceCount(name, amount);
        		break;
        	case WRITE:
                addLoopWriteReferenceCount(name, amount);
                break;
        }
    }
	
	private int getVariableReferenceCount(String name, CountType countType) {
        VariableUsage v = getExistingUsage(name);
        if (v == null) {
            return 0;
        }
        else {
            switch (countType) {
                case READ:
                    return v.readReferenceCount;
                case WRITE:
                    return v.writeReferenceCount;
            }
            return 0;
        }
    }
	
	private boolean isVariableFullyReadInThisScope(String name) {
        VariableUsage v = getExistingUsage(name);
        return v != null && v.hasFullReaders();
    }
	
	private Node getFirstWrite(String name) {
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
	
	private List<Node> getAllWriters(String name) {
	    VariableUsage v = getExistingUsage(name);
	    if (v == null) {
	        throw new IllegalArgumentException("Variable " + name + " is not written to in this scope");
	    }
	    return v.partialWritingLoc;
	}
	
	
    /** looks up the template that declared the named variable in the
	    present scope. If such a template does not exist, returns null.
	    TODO this should probably merge into general variable structure
	    rather then walking the template.
	*/
	private IRefCounted getLocalDeclaration(String name) {
	    if (owner == null) {
	        return null;
	    }
	    
		List<IVariableDeclaration> decls = owner.getVariableDeclarations();
		if (decls == null) {
		    switch (enclosureType) {
		        case PROCEDURE:
		            return ((IProcedureDeclaration) owner).getParameter(name);
		        case NONE:
		            return ((IProcedureDeclaration) owner).getReturn(name);
		    }
		    return null;
		}

		for (IVariableDeclaration decl : decls) { 
		    if (logger.isDebugEnabled()) {
		        logger.debug("looking at declaration " + decl);
		    }
	        if (decl.getName().equals(name)) {
	            if (logger.isDebugEnabled()) {
	                logger.debug("Found declaration for " + name);
	            }
	            return decl;
	        }
		}
		
		if (logger.isInfoEnabled()) {
		    logger.info("Couldn't find local definition for " + name);
		}
		return null;
	}
	
	private void setUnused(String name) {
        IRefCounted decl = this.getLocalDeclaration(name);
        if (decl instanceof IVariableDeclaration) {
            ((IVariableDeclaration) decl).setUnused(true);
        }
        lookup(name).unused = true;
    }
	
	public VariableScope getRootScope() {
		return rootScope;
	}

    public List<String> getCleanups() {
        List<String> cleanups = new ArrayList<String>();
        for (String var : getLocallyDeclaredVariableNames()) {
            Variable v = lookup(var);
            Type type = v.type;
            if (!type.isPrimitive() && !v.unused && v.origin != VariableOrigin.AUTO) {
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
            
            if (fullyRead && getVariableType(name).isPrimitive()) {
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
    
    public void checkUnused() {
        checkUnusedRecursive();
        removeUnused();
    }

    private void removeUnused() {
        for (String name : getLocallyDeclaredVariableNames()) {
            Variable var = lookup(name);
            if (var.origin == VariableOrigin.USER && var.unused) {
                setUnused(name);
            }
        }
        
        for (VariableScope child : children) {
            child.removeUnused();
        }
    }

    private void checkUnusedRecursive() {
        for (String name : getLocallyDeclaredVariableNames()) {
            Variable var = lookup(name);
            if (var.origin == VariableOrigin.USER) {
                var.unused = true;
            }
        }
        
        for (String name : getUsedVariableNames()) {
            VariableUsage usage = getExistingUsage(name);
            if (usage != null && (usage.hasWriters() || usage.hasReaders())) {
                lookup(name).unused = false;
            }
        }
        
        for (VariableScope child : children) {
            child.checkUnusedRecursive();
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
            throw new CompilationException("Uninitialized variable: " + name);
        }
    }

    public IStatementContainer getOwner() {
        return owner;
    }

    public void setOwner(IStatementContainer owner) {
        this.owner = owner;
    }
}

