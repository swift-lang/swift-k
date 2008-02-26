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

	public static final Logger logger = Logger.getLogger(VariableScope.class);

	/** need this for the program as a whole. probably should factor
	    that into separate pieces. */
	Karajan compiler;

	VariableScope parentScope;

	/** The string template in which we will store statements
	    outputted into this scope. */
	public StringTemplate bodyTemplate;

	public VariableScope(Karajan c, VariableScope parent) {
		if(parentScope!=null) {
			logger.info("New scope "+hashCode()+" with parent scope "+parentScope.hashCode());
		} else {
			logger.info("New scope "+hashCode()+" with no parent.");
		}
		compiler = c;
		parentScope = parent;
	}

	/** Set of variables (String token names) that are declared in
	    this scope - not variables in parent scopes, though. */
	Set variables = new HashSet();

	/** Asserts that a named variable is declared in this scope.
	    Might also eventually contain more information about the
	    variable. Need to define behaviour here when the
	    declaration already exists. Perhaps error in same scope and
	    warning if it shadows an outer scope? */
	public void addVariable(String name) throws CompilationException {
		logger.info("Adding variable "+name+" to scope "+hashCode());

		if(isVariableDefined(name)) {
			throw new CompilationException("Variable "+name+" is already defined.");
		}

		if(parentScope != null && parentScope.isVariableDefined(name)) {
			logger.warn("Variable "+name+" defined in scope "+hashCode()
			+ " shadows variable of same name in scope "
			+ parentScope.hashCode());
		}

		boolean added = variables.add(name);
		if(!added) throw new CompilationException("Could not add variable "+name+" to scope.");
	}


	public boolean isVariableDefined(String name) {
		if(isVariableLocallyDefined(name)) return true;
		if(parentScope != null && parentScope.isVariableDefined(name)) return true;
		return false;
	}

	public boolean isVariableLocallyDefined(String name) {
		return variables.contains(name);
	}

	/** List of templates to be executed in sequence after the present
	    in-preparation statement is outputted. */
	List presentStatementPostStatements =  Collections.synchronizedList(new ArrayList());

	Map variableUsage = Collections.synchronizedMap(new HashMap());

	/** indicates that the present in-preparation statement writes to the
	    named variable. If the variable is declared in the local scope,
	    register a closing statement; otherwise, record that this scope
	    writes to the variable so that the scope-embedding code (such as
	    the foreach compiler) can handle appropriately. */
	public void addWriter(String variableName, Object closeID) throws CompilationException
	{
		if(!isVariableDefined(variableName)) {
			throw new CompilationException("Variable "+variableName+" is not defined");
		}

		if(isVariableLocallyDefined(variableName)) {
			StringTemplate ld = getLocalDeclaration(variableName);
			if(ld != null) {
				ld.setAttribute("waitfor", closeID);
			} else {
				logger.info("Variable "+variableName+" is local but has no template.");
			}
			StringTemplate postST = compiler.template("partialclose");
			postST.setAttribute("var", variableName);
			postST.setAttribute("closeID", closeID);
			presentStatementPostStatements.add(postST);
		} else {
			Variable variable = (Variable) variableUsage.get(variableName);
			if(variable == null) {
				variable = new Variable();
				variableUsage.put(variableName, variable);
			}

			List statementList = variable.writingStatements;
			if(!statementList.contains(closeID)) {
				statementList.add(closeID);
			}
			logger.info("added "+closeID+" to variable "+variableName+" in scope "+hashCode());
		}
	}

	/** looks up the template that declared the named variable in the
	    present scope. If such a template does not exist, returns null.
	    TODO this should probably merge into general variable structure
	    rather then walking the template.
	*/
	StringTemplate getLocalDeclaration(String name) {
		Object decls = bodyTemplate.getAttribute("declarations");
		if(decls == null) return null;

		if(decls instanceof StringTemplate) {
			StringTemplate declST = (StringTemplate) decls;
			if(declST.getAttribute("name").equals(name)) {
logger.info("thats the declaration for "+name);
				return declST;
			}
		} else { // assume its a List
			Iterator it = ((List) decls).iterator();
			while(it.hasNext()) {
				StringTemplate declST = (StringTemplate) it.next();
logger.info("looking at declaration "+declST);
try {
				if(declST.getAttribute("name").equals(name)) {
logger.info("thats the declaration for "+name);
					return declST;
				}
} catch(java.util.NoSuchElementException nse) {
logger.info("it so definitely wasn't in that one, we got an exception.");
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
		Iterator it = presentStatementPostStatements.iterator();
		while(it.hasNext()) {
			wrapperST.setAttribute("statements", it.next());
		}
		presentStatementPostStatements =  Collections.synchronizedList(new ArrayList());
	}

	/** Stores information about a variable that is referred to in this
	    scope. Should probably get used for dataset marking eventually. */
	class Variable {
		List writingStatements =  Collections.synchronizedList(new ArrayList());
	}

	Iterator getVariableIterator() {
		return variableUsage.keySet().iterator();
	}

}

