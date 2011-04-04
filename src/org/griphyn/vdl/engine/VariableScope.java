package org.griphyn.vdl.engine;

import org.antlr.stringtemplate.StringTemplate;

import org.apache.log4j.Logger;

import org.griphyn.vdl.karajan.CompilationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class VariableScope {

	/** permit array up-assignment, but not entire variables */
	public final static int ENCLOSURE_LOOP = 301923;

	/** permit all upwards assignments */
	public static final int ENCLOSURE_ALL = 301924;

	/** permit no upwards assignments */
	public static final int ENCLOSURE_NONE = 301925;

	/** permit no access to the containing scope except for finding
	    global variables */
	public static final int ENCLOSURE_PROCEDURE = 301926;

	public static final Logger logger = Logger.getLogger(VariableScope.class);

	/** need this for the program as a whole. probably should factor
	    that into separate pieces. */
	Karajan compiler;

	/** the scope that directly contains this scope. */
	VariableScope parentScope;

	/** the root level scope for the entire SwiftScript program. Sometimes
	    this will be the same as parentScope or this. */
	VariableScope rootScope;

	int enclosureType;

	/** The string template in which we will store statements
	    outputted into this scope. */
	public StringTemplate bodyTemplate;

	public VariableScope(Karajan c, VariableScope parent) {
		this(c,parent,ENCLOSURE_ALL);
	}

	/** Creates a new variable scope.
		@param c the compiler scope in which this variable lives
		@param parent the enclosing scope, or null if this is a
			top level scope
		@param a specifies how assignments to variables made in enclosing
			scopes will be handled.
	*/
	public VariableScope(Karajan c, VariableScope parent, int a) {
		if(parentScope!=null) {
			logger.debug("New scope "+hashCode()+" with parent scope "+parentScope.hashCode());
		} else {
			logger.debug("New scope "+hashCode()+" with no parent.");
		}
		compiler = c;
		parentScope = parent;
		enclosureType = a;
		if(parentScope == null) {
			rootScope = this;
		} else {
			rootScope = parentScope.getRootScope();
		}
	}


	/** Set of variables (String token names) that are declared in
	    this scope - not variables in parent scopes, though. */
	Set<String> variables = new HashSet<String>();
	Map<String, String> varTypes = new HashMap<String, String>();

	/** Set of variables (String token names) which are global and
	    declared in this scope (which must be a root scope). Variables
	    in this set must also appear in the variables set. */
	Set<String> globals = new HashSet<String>();

	/** Asserts that a named variable is declared in this scope.
	    Might also eventually contain more information about the
	    variable. Need to define behaviour here when the
	    declaration already exists. Perhaps error in same scope and
	    warning if it shadows an outer scope? */

	public void addVariable(String name, String type) throws CompilationException {
		addVariable(name,type,false);
	}

	public void addVariable(String name, String type, boolean global) throws CompilationException {
		logger.debug("Adding variable "+name+" of type "+type+" to scope "+hashCode());
		
		if(isVariableDefined(name)) {
			throw new CompilationException("Variable "+name+" is already defined.");
		}

		// TODO does this if() ever fire? or is it always taken
		// by the above if? in which case isVariableDefined should
		// be replaced by is locally defined test.
		if(parentScope != null && parentScope.isVariableDefined(name)) {
			logger.warn("Variable "+name+" defined in scope "+hashCode()
			+ " shadows variable of same name in scope "
			+ parentScope.hashCode());
		}

		if(global && this != rootScope) {
			throw new CompilationException("Global "+name+" can only be declared in the root scope of a program.");
		}

		boolean added = variables.add(name);
		varTypes.put(name, type);
		if(!added) throw new CompilationException("Could not add variable "+name+" to scope.");

		if(global) {
			added = globals.add(name);
			if(!added) throw new CompilationException("Could not add global variable "+name+" to scope.");
		}
	}
	
	/**
	 * Does pretty much the same as addVariable() except it doesn't throw
	 * an exception if the variable is defined in a parent scope
	 */
	public void addInternalVariable(String name, String type) throws CompilationException {
		logger.debug("Adding internal variable " + name + " of type " + type + " to scope " + hashCode());
		
		if(isVariableLocallyDefined(name)) {
			throw new CompilationException("Variable " + name + " is already defined.");
		}

		boolean added = variables.add(name);
		varTypes.put(name, type);
		if(!added) throw new CompilationException("Could not add variable "+name+" to scope.");
	}

	public boolean isVariableDefined(String name) {
		if(rootScope.isGlobalDefined(name)) return true;
		return isVariableDefinedRecursive(name);
	}

	private boolean isVariableDefinedRecursive(String name) {
		if(isVariableLocallyDefined(name)) return true;
		if(enclosureType != ENCLOSURE_PROCEDURE && parentScope != null && parentScope.isVariableDefined(name)) return true;
		return false;
	}

	public boolean isGlobalDefined(String name) {
		return globals.contains(name);
	}

	/** note that this will return variable types for variables in
	    containing scopes even if they are not accessible. non-null
	    return value from this method should not be treated as an
	    indication that the variable is in any way accessible from
	    this scope. The isVariableDefined and isGlobalDefined methods
	    should be used to check that. */
	public String getVariableType(String name) {
		if(isVariableLocallyDefined(name)) 
			return varTypes.get(name).toString();
		if(parentScope != null && parentScope.isVariableDefined(name)) 
			return parentScope.getVariableType(name);
		return null;
	}

// TODO could also mark variable as written and catch duplicate assignments
// in the same scope here
	public boolean isVariableWriteable(String name, boolean partialWriter) {
		if(isVariableLocallyDefined(name)) return true;
		if(parentScope != null && parentScope.isVariableWriteable(name, partialWriter) && enclosureType == ENCLOSURE_ALL) return true;
		if(parentScope != null && parentScope.isVariableWriteable(name, partialWriter) && enclosureType == ENCLOSURE_LOOP && partialWriter) return true;
		return false;
	}


	public boolean isVariableLocallyDefined(String name) {
		return variables.contains(name);
	}

	/** List of templates to be executed in sequence after the present
	    in-preparation statement is outputted. */
	List<StringTemplate> presentStatementPostStatements =  Collections.synchronizedList(new ArrayList<StringTemplate>());

	Map<String, Variable> variableUsage = Collections.synchronizedMap(new HashMap<String, Variable>());

	/** indicates that the present in-preparation statement writes to the
	    named variable. If the variable is declared in the local scope,
	    register a closing statement; otherwise, record that this scope
	    writes to the variable so that the scope-embedding code (such as
	    the foreach compiler) can handle appropriately. */
	public void addWriter(String variableName, Object closeID, boolean partialWriter) throws CompilationException
	{
		if(!isVariableDefined(variableName)) {
			throw new CompilationException("Variable "+variableName+" is not defined");
		}

		if(isVariableLocallyDefined(variableName)) {
			StringTemplate ld = getLocalDeclaration(variableName);
			if(ld != null) {
				if(!partialWriter && ld.getAttribute("waitfor")!=null) {
					throw new CompilationException("variable "+variableName+" has multiple writers.");
				}
				ld.setAttribute("waitfor", closeID);
			} else {
				logger.debug("Variable "+variableName+" is local but has no template.");
			}
			StringTemplate postST = compiler.template("partialclose");
			postST.setAttribute("var", variableName);
			postST.setAttribute("closeID", closeID);
			presentStatementPostStatements.add(postST);
		} else {

// TODO now we have to walk up the scopes until either we find the
// variable or we find an upwards assignment prohibition or we run
// out of scopes

// TODO so far this should find undeclared variables at compile time
// so perhaps worth making this into a separate patch if it actually
// works

			if(isVariableWriteable(variableName, partialWriter)) {
				Variable variable = variableUsage.get(variableName);
				if(variable == null) {
					variable = new Variable();
					variableUsage.put(variableName, variable);
				}

				List<Object> statementList = variable.writingStatements;
				if(!statementList.contains(closeID)) {
					statementList.add(closeID);
				}
				logger.debug("added "+closeID+" to variable "+variableName+" in scope "+hashCode());
			} else {
				throw new CompilationException("variable "+variableName+" is not writeable in this scope");
			}
		}
	}

	/** looks up the template that declared the named variable in the
	    present scope. If such a template does not exist, returns null.
	    TODO this should probably merge into general variable structure
	    rather then walking the template.
	*/
	StringTemplate getLocalDeclaration(String name) {
	    if (bodyTemplate == null) {
	        return null;
	    }
		Object decls = bodyTemplate.getAttribute("declarations");
		if(decls == null) return null;

		if(decls instanceof StringTemplate) {
			StringTemplate declST = (StringTemplate) decls;
			if(declST.getAttribute("name").equals(name)) {
				logger.debug("thats the declaration for "+name);
				return declST;
			}
		} else { // assume its a List of StringTemplate
		     @SuppressWarnings("unchecked")
		     List<StringTemplate> list = (List<StringTemplate>) decls;
		     for (StringTemplate declST : list) { 
		         logger.debug("looking at declaration "+declST);
		         try {
		             if(declST.getAttribute("name").equals(name)) {
		                 logger.debug("thats the declaration for "+name);
		                 return declST;
		             }
		         } catch(java.util.NoSuchElementException nse) {
		             logger.debug
		             ("it so definitely wasn't in that one, we got an exception.");
		             // TODO this is not a nice use of exceptions...
		         }
		     }
		}
		
		logger.info("UH OH - couldn't find local definition for "+name);
		return null;
	}

	/** appends statement to the present scope, wrapping as appropriate
	    with code to partially-close registered uses. perhaps should move
	    into own Statement class? */

	void appendStatement(StringTemplate statementST) {
		StringTemplate wrapperST = compiler.template("sequential");
		bodyTemplate.setAttribute("statements", wrapperST);
		wrapperST.setAttribute("statements",statementST);
		Iterator<StringTemplate> it = presentStatementPostStatements.iterator();
		while(it.hasNext()) {
			wrapperST.setAttribute("statements", it.next());
		}
		presentStatementPostStatements =  Collections.synchronizedList(new ArrayList<StringTemplate>());
	}

	/** Stores information about a variable that is referred to in this
	    scope. Should probably get used for dataset marking eventually. */
	class Variable {
		List<Object> writingStatements =  Collections.synchronizedList(new ArrayList<Object>());
	}

	Iterator<String> getVariableIterator() {
		return variableUsage.keySet().iterator();
	}

	Set<String> getVariables() {
	    return variableUsage.keySet();
	}
	
	public VariableScope getRootScope() {
		return rootScope;
	}
}

