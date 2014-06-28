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

import static org.griphyn.vdl.engine.CompilerUtils.getLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlFloat;
import org.apache.xmlbeans.XmlInt;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;
import org.globus.swift.language.*;
import org.globus.swift.language.If.Else;
import org.globus.swift.language.If.Then;
import org.globus.swift.language.ImportsDocument.Imports;
import org.globus.swift.language.ProgramDocument.Program;
import org.globus.swift.language.Switch.Case;
import org.globus.swift.language.Switch.Default;
import org.globus.swift.language.TypesDocument.Types;
import org.globus.swift.language.TypesDocument.Types.Type;
import org.globus.swift.language.Variable.Mapping;
import org.globus.swift.language.Variable.Mapping.Param;
import org.griphyn.vdl.engine.VariableScope.EnclosureType;
import org.griphyn.vdl.engine.VariableScope.WriteType;
import org.griphyn.vdl.karajan.CompilationException;
import org.griphyn.vdl.karajan.Loader;
import org.griphyn.vdl.mapping.MapperFactory;
import org.griphyn.vdl.toolkit.VDLt2VDLx;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.safehaus.uuid.UUIDGenerator;
import org.w3c.dom.Node;

public class Karajan {
	public static final Logger logger = Logger.getLogger(Karajan.class);

	Map<String,String> stringInternMap = new HashMap<String,String>();
	Map<Integer,String> intInternMap = new HashMap<Integer,String>();
	Map<Float,String> floatInternMap = new HashMap<Float,String>();
	Map<String,ProcedureSignature> proceduresMap = 
	    new HashMap<String,ProcedureSignature>();
	Map<String,ProcedureSignature> functionsMap = 
	    new HashMap<String,ProcedureSignature>();
	Map<String, Type> typesMap = new HashMap<String, Type>();
	Set<String> nonMappedTypes = new HashSet<String>();
	
	private class InternedField {
	    public final String name, type;
	    
	    public InternedField(String name, String type) {
	        this.name = name;
	        this.type = type;
	    }

        @Override
        public int hashCode() {
            return name.hashCode() + type.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InternedField) {
                InternedField o = (InternedField) obj;
                return name.equals(o.name) && type.equals(o.type);
            }
            else {
                return false;
            }
        }
	}
	
	Map<InternedField, String> usedFields = new HashMap<InternedField, String>();
	
	List<StringTemplate> variables = new ArrayList<StringTemplate>();

	public static final String TEMPLATE_FILE_NAME = "Karajan.stg";
	public static final String TEMPLATE_FILE_NAME_NO_PROVENANCE = "Karajan-no-provenance.stg";

	LinkedList<Program> importList = new LinkedList<Program>();
	Set<String> importedNames = new HashSet<String>();

	int internedIDCounter = 17000;

	/** an arbitrary statement identifier. Start at some high number to
	    aid visual distinction in logs, but the actual value doesn't
		matter. */

	StringTemplateGroup m_templates;

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Please provide a SwiftScript program file.");
			System.exit(1);
		}
		compile(args[0], System.out, false);
	}

	public static void compile(Object in, PrintStream out, boolean provenanceEnabled) throws CompilationException {
		Karajan karajan = new Karajan();
		StringTemplateGroup templates;
		try {
		    StringTemplateGroup main = new StringTemplateGroup(new InputStreamReader(
                    Karajan.class.getClassLoader().getResource(TEMPLATE_FILE_NAME).openStream()));
		    if (provenanceEnabled) {
		        templates = main;
		    }
		    else {
		        StringTemplateGroup override = new StringTemplateGroup(new InputStreamReader(
                    Karajan.class.getClassLoader().getResource(TEMPLATE_FILE_NAME_NO_PROVENANCE).openStream()));
		        override.setSuperGroup(main);
		        templates = override;
		    }
		} catch(IOException ioe) {
			throw new CompilationException("Unable to load karajan source templates",ioe);
		}

		ProgramDocument programDoc;
		try {
			programDoc = parseProgramXML(in);
		} 
		catch(Exception e) {
			throw new CompilationException("Unable to parse intermediate XML", e);
		}

		Program prog = programDoc.getProgram();
		karajan.setTemplateGroup(templates);
		StringTemplate code = karajan.program(prog);
		out.println(code.toString());
	}

	public static ProgramDocument parseProgramXML(Object in)
		throws XmlException, IOException {

		XmlOptions options = new XmlOptions();
		Collection<XmlError> errors = new ArrayList<XmlError>();
		options.setErrorListener(errors);
		options.setValidateOnSet();
		options.setLoadLineNumbers();

		ProgramDocument programDoc;
		if (in instanceof File) {
		    programDoc  = ProgramDocument.Factory.parse((File) in, options);
		}
		else if (in instanceof String) {
		    programDoc  = ProgramDocument.Factory.parse((String) in, options);
		}
		else {
		    throw new IllegalArgumentException("Don't know how to parse a " + in.getClass().getName());
		}

		if(programDoc.validate(options)) {
			logger.debug("Validation of XML intermediate file was successful");
		} else {
			logger.warn("Validation of XML intermediate file failed.");
			logger.warn("Validation errors:");
			for (XmlError error : errors)
				logger.warn(error.toString());
			System.exit(3);
		}
		return programDoc;
	}

	public Karajan() {
		// Built-in procedures
		proceduresMap = ProcedureSignature.makeProcedureSignatures();
		// Built-in functions
		functionsMap = ProcedureSignature.makeFunctionSignatures();
		
		// used by some templates
		addInternedField("temp", "int");
		addInternedField("const", "int");
		addInternedField("const", "float");
		addInternedField("const", "string");
		addInternedField("const", "boolean");
	}

	void setTemplateGroup(StringTemplateGroup tempGroup) {
		m_templates = tempGroup;
	}

	protected StringTemplate template(String name) {
		return m_templates.getInstanceOf(name);
	}

    private void processImports(Program prog) throws CompilationException {

		Imports imports = prog.getImports();
		if(imports!=null) {
			logger.debug("Processing SwiftScript imports");
            // process imports in reverse order
			for(int i = imports.sizeOfImportArray() - 1 ;  i >=0 ; i--) {
				String moduleToImport = imports.getImportArray(i);
				logger.debug("Importing module "+moduleToImport);
				if(!importedNames.contains(moduleToImport)) {

					// TODO PATH/PERL5LIB-style path handling
					//String swiftfilename = "./"+moduleToImport+".swift";
					//String xmlfilename = "./"+moduleToImport+".xml";
				    String lib_path = System.getenv("SWIFT_LIB");
					String swiftfilename = moduleToImport+".swift";
					String xmlfilename = moduleToImport+".swiftx";

					File local = new File(swiftfilename);
					if( !( lib_path == null || local.exists() ) )
					{
					    String[] path = lib_path.split(":");
					    for(String entry : path)
					    {
					        String lib_script_location = entry + "/" + swiftfilename;
					        File file = new File(lib_script_location);
					        
					        if(file.exists())
					        {
					            swiftfilename = entry + "/" + swiftfilename;

					            moduleToImport = entry + "/" + moduleToImport;
					            break;
					        }
					    }
					}

					try {
        	    		VDLt2VDLx.compile(new FileInputStream(swiftfilename),new PrintStream(new FileOutputStream(xmlfilename)));
						logger.debug("Compiled. Now reading in compiled XML for "+moduleToImport);
						Program importedProgram = parseProgramXML(new File(xmlfilename)).getProgram();
						logger.debug("Read in compiled XML for "+moduleToImport);
						importList.addFirst(importedProgram);
						importedNames.add(moduleToImport);
						logger.debug("Added "+moduleToImport+" to import list. Processing imports from that.");
						processImports(importedProgram);
					} catch(Exception e) {
						throw new CompilationException("When processing import "+moduleToImport, e);
					}
				} else {
					logger.debug("Skipping repeated import of "+moduleToImport);
				}
			}
		}
	}

	private void processTypes(Program prog, VariableScope scope) throws CompilationException {
	    Types types = prog.getTypes();
		if (types != null) {
			for (int i = 0; i < types.sizeOfTypeArray(); i++) {
				Type theType = types.getTypeArray(i);
				String typeName = theType.getTypename();
				String typeAlias = theType.getTypealias();

				logger.debug("Processing type "+typeName);

				typesMap.put(typeName, theType);

				StringTemplate st = template("typeDef");
				st.setAttribute("name", typeName);
				if (typeAlias != null && !typeAlias.equals("") && !typeAlias.equals("string")) {
					checkIsTypeDefined(typeAlias);
					st.setAttribute("type", typeAlias);
				}

				TypeStructure ts = theType.getTypestructure();
				boolean allPrimitive = ts.sizeOfMemberArray() > 0;
				for (int j = 0; j < ts.sizeOfMemberArray(); j++) {
					TypeRow tr = ts.getMemberArray(j);

					StringTemplate stMember = template("memberdefinition");
					stMember.setAttribute("name", tr.getMembername());
					stMember.setAttribute("type", tr.getMembertype());
					if (!isPrimitiveOrArrayOfPrimitive(tr.getMembertype())) {
						allPrimitive = false;
					}

					st.setAttribute("members", stMember);
				}
				if (allPrimitive) {
					nonMappedTypes.add(typeName);
				}
				scope.bodyTemplate.setAttribute("types", st);
			}
		}
	}

	private void processProcedures(Program prog, VariableScope scope) throws CompilationException {
		Map<String, Procedure> names = new HashMap<String, Procedure>();
		// Keep track of declared procedures
	    // Check for redefinitions of existing procedures
	    Set<String> procsDefined = new HashSet<String>() ;	    
		for (int i = 0; i < prog.sizeOfProcedureArray(); i++) {
			Procedure proc = prog.getProcedureArray(i);
			String procName = proc.getName();
			if (procsDefined.contains(procName)){
			    // We have a redefinition error
			    throw new CompilationException("Illegal redefinition of procedure attempted for " + procName );
			}
			procsDefined.add(procName);
			ProcedureSignature ps = new ProcedureSignature(procName);
			ps.setInputArgs(proc.getInputArray());
			ps.setOutputArgs(proc.getOutputArray());
			proceduresMap.put(procName, ps);
			names.put(procName, proc);
		}
		
		List<Procedure> sorted = new ArrayList<Procedure>();
        Set<Procedure> unmarked = new HashSet<Procedure>(Arrays.asList(prog.getProcedureArray()));
        
        while (!unmarked.isEmpty()) {
            Set<Procedure> tmp = new HashSet<Procedure>();
            visit(null, unmarked.iterator().next(), unmarked, sorted, tmp, names);
        }

        for (Procedure proc : sorted) {
			procedure(proc, scope);
		}
	}


    private void visit(Procedure self, Procedure proc, Set<Procedure> unmarked,
            List<Procedure> sorted, Set<Procedure> tmp, Map<String, Procedure> names) throws CompilationException {
    	if (tmp.contains(proc)) {
    	    if (proc == self) {
    	        // immediate recursion allowed
    	        return;
    	    }
    	    else {
    	        throw new CompilationException("Circular procedure dependency detected");
    	    }
    	}
    	if (unmarked.contains(proc)) {
    		tmp.add(proc);
    		Set<String> dupes = new HashSet<String>();
    		for (Call c : getCalls(proc, new ArrayList<Call>())) {
    			String name = c.getProc().getLocalPart();
    			if (dupes.contains(name)) {
    				continue;
    			}
    			dupes.add(name);
    			if (names.containsKey(name)) {
    				visit(proc, names.get(name), unmarked, sorted, tmp, names);
    			}
    			else {
    				// handled later
    			}
    		}
    		unmarked.remove(proc);
    		sorted.add(proc);
    	}
    }

    private List<Call> getCalls(XmlObject o, List<Call> l) {
        XmlCursor cursor = o.newCursor();
        cursor.selectPath("*");
        while (cursor.toNextSelection()) {
            XmlObject child = cursor.getObject();
            if (child instanceof Call) {
            	l.add((Call) child);
            }
            getCalls(child, l);
        }
        return l;
    }

    public StringTemplate program(Program prog) throws CompilationException {
		VariableScope scope = new VariableScope(this, null, prog);
		scope.bodyTemplate = template("program");

		scope.bodyTemplate.setAttribute("buildversion",Loader.buildVersion);

		importList.addFirst(prog);
		processImports(prog);

		for (Program program : importList)
		    processTypes(program, scope);
		for (Program program : importList)
            statementsForSymbols(program, scope);
        for (Program program : importList)
            processProcedures(program, scope);
        for (Program program : importList)
            statements(program, scope);
		
        generateInternedFields(scope.bodyTemplate);
		generateInternedConstants(scope.bodyTemplate);
		scope.analyzeWriters();
		
		scope.bodyTemplate.setAttribute("cleanups", scope.getCleanups());

		return scope.bodyTemplate;
	}
	
    public void procedure(Procedure proc, VariableScope containingScope) throws CompilationException {
		VariableScope outerScope = new VariableScope(this, containingScope, EnclosureType.PROCEDURE, proc);
		VariableScope innerScope = new VariableScope(this, outerScope, EnclosureType.NONE, proc);
		StringTemplate procST = template("procedure");
		containingScope.bodyTemplate.setAttribute("procedures", procST);
		procST.setAttribute("line", getLine(proc));
		procST.setAttribute("name", proc.getName());
		for (int i = 0; i < proc.sizeOfOutputArray(); i++) {
			FormalParameter param = proc.getOutputArray(i);
			StringTemplate paramST = parameter(param, innerScope);
			procST.setAttribute("outputs", paramST);
			if (!this.isPrimitiveOrArrayOfPrimitive(param.getType().getLocalPart())) {
                procST.setAttribute("stageouts", paramST);
            }
			addArg(procST, param, paramST, true, innerScope);		
		}
		for (int i = 0; i < proc.sizeOfInputArray(); i++) {
			FormalParameter param = proc.getInputArray(i);
			StringTemplate paramST = parameter(param, outerScope);
			procST.setAttribute("inputs", paramST);
			if (!this.isPrimitiveOrArrayOfPrimitive(param.getType().getLocalPart())) {
			    procST.setAttribute("stageins", paramST);
			}
			addArg(procST, param, paramST, false, outerScope);
			outerScope.addWriter(param.getName(), WriteType.FULL, proc, procST);
		}
		
		Binding bind;
		if ((bind = proc.getBinding()) != null) {
			binding(bind, procST, innerScope);
		}
		else {
			VariableScope compoundScope = new VariableScope(this, innerScope, proc);
			compoundScope.bodyTemplate = procST;
			statementsForSymbols(proc, compoundScope);
			statements(proc, compoundScope);
			procST.setAttribute("cleanups", compoundScope.getCleanups());
		}
	}

    private void addArg(StringTemplate procST, FormalParameter param,
            StringTemplate paramST, boolean returnArg, VariableScope scope) throws CompilationException {
        if (!param.isNil()) {
            procST.setAttribute("optargs", paramST);
        }
        else {
            procST.setAttribute("arguments", paramST);
        }
        String type = normalize(param.getType().getLocalPart());
        checkIsTypeDefined(type);
        scope.addVariable(param.getName(), type, returnArg ? "Return value" : "Parameter", false, param);
        
        if (returnArg) {
            StringTemplate initWaitCountST = template("setWaitCount");
            initWaitCountST.setAttribute("name", param.getName());
            procST.setAttribute("initWaitCounts", initWaitCountST);
        }
    }
    
  
    public StringTemplate parameter(FormalParameter param, VariableScope scope) throws CompilationException {
		StringTemplate paramST = new StringTemplate("parameter");
		StringTemplate typeST = new StringTemplate("type");
		paramST.setAttribute("name", param.getName());
		typeST.setAttribute("name", normalize(param.getType().getLocalPart()));
		typeST.setAttribute("namespace", param.getType().getNamespaceURI());
		paramST.setAttribute("type", typeST);
		if(!param.isNil()) {
			paramST.setAttribute("default",expressionToKarajan(param.getAbstractExpression(), scope));
		}
		return paramST;
	}

	public void variableForSymbol(Variable var, VariableScope scope) throws CompilationException {
		checkIsTypeDefined(var.getType().getLocalPart());
		scope.addVariable(var.getName(), var.getType().getLocalPart(), "Variable", var.getIsGlobal(), var);
	}

	public void variable(Variable var, VariableScope scope) throws CompilationException {
		StringTemplate variableST = template("variable");
		variableST.setAttribute("name", var.getName());
		variableST.setAttribute("type", var.getType().getLocalPart());
		variableST.setAttribute("field", addInternedField(var.getName(), var.getType().getLocalPart()));
		variableST.setAttribute("isGlobal", Boolean.valueOf(var.getIsGlobal()));
		variableST.setAttribute("line", getLine(var));
		variables.add(variableST);
		
		
		/*
		 *  possibly an input; mark it as such here, and if
		 *  writers are detected, remove the input attribute (that is
		 *  done in VariableScope).
		 */
		variableST.setAttribute("input", "true");

		if (!var.isNil()) {
			if (var.getFile() != null) {
				StringTemplate fileST = new StringTemplate("file");
				fileST.setAttribute("name", escape(var.getFile().getName()));
				fileST.defineFormalArgument("params");
				variableST.setAttribute("file", fileST);
			}

			Mapping mapping = var.getMapping();

			if (mapping != null) {
				StringTemplate mappingST = new StringTemplate("mapping");
				String mapperType = mapping.getDescriptor();
				mappingST.setAttribute("descriptor", mapperType);
				checkMapperParams(mapperType, mapping);
				for (int i = 0; i < mapping.sizeOfParamArray(); i++) {
					Param param = mapping.getParamArray(i);
					mappingST.setAttribute("params", mappingParameter(param, scope));
				}
				variableST.setAttribute("mapping", mappingST);
			}
   		}
		else {
			// add temporary mapping info in not primitive or array of primitive	    
			if (!isPrimitiveOrArrayOfPrimitive(var.getType().getLocalPart())) {
    			StringTemplate mappingST = new StringTemplate("mapping");
    			mappingST.setAttribute("descriptor", "ConcurrentMapper");
    			StringTemplate paramST = template("swift_parameter");
    			paramST.setAttribute("name", "prefix");
    			paramST.setAttribute("expr", "\"" + var.getName() + "-" + lineNumber(var.getSrc()) + "\"");
    			mappingST.setAttribute("params", paramST);
    			variableST.setAttribute("mapping", mappingST);
    			variableST.setAttribute("nil", Boolean.TRUE);
			}
   		}

		scope.bodyTemplate.setAttribute("declarations", variableST);
	}

    private void checkMapperParams(String mapperType, Mapping mapping) throws CompilationException {
        if (!MapperFactory.isValidMapperType(mapperType)) {
            throw new CompilationException("Unknown mapper type: '" + mapperType + "'");
        }
        
        Set<String> validParams = MapperFactory.getValidParams(mapperType);
        if (validParams == null && mapping.sizeOfParamArray() > 0) {
            throw new CompilationException(mapperType + " does not support any parameters");
        }
        if (validParams.contains("*")) {
            // mapper accepts any parameter (e.g. external_mapper)
            return;
        }
        for (int i = 0; i < mapping.sizeOfParamArray(); i++) {
            Param param = mapping.getParamArray(i);
            if (!validParams.contains(param.getName())) {
                throw new CompilationException(mapperType + " does not support a '" + param.getName() + "' parameter");
            }
        }
    }

    private StringTemplate mappingParameter(Param param, VariableScope scope) throws CompilationException {
        StringTemplate paramST = template("swift_parameter");
        paramST.setAttribute("name", param.getName());
        Node expressionDOM = param.getAbstractExpression().getDomNode();
        String namespaceURI = expressionDOM.getNamespaceURI();
        String localName = expressionDOM.getLocalName();
        QName expressionQName = new QName(namespaceURI, localName);
        if (expressionQName.equals(VARIABLE_REFERENCE_EXPR))     {
            paramST.setAttribute("expr", expressionToKarajan(param.getAbstractExpression(), scope));
        } 
        else {
            String parameterVariableName="swift.mapper." + (internedIDCounter++);
            // make template for variable declaration (need to compute type of this variable too?)
            StringTemplate variableDeclarationST = template("variable");
            // TODO factorise this and other code in variable()?
            StringTemplate pmappingST = new StringTemplate("mapping");
            pmappingST.setAttribute("descriptor", "ConcurrentMapper");
            StringTemplate pparamST = template("swift_parameter");
            pparamST.setAttribute("name", "prefix");
            pparamST.setAttribute("expr", parameterVariableName + "-" + 
                UUIDGenerator.getInstance().generateRandomBasedUUID().toString());
            pmappingST.setAttribute("params", pparamST);
            variableDeclarationST.setAttribute("nil", Boolean.TRUE);
            variableDeclarationST.setAttribute("name", parameterVariableName);
            scope.bodyTemplate.setAttribute("declarations", variableDeclarationST);
            StringTemplate paramValueST=expressionToKarajan(param.getAbstractExpression(),scope);
            String paramValueType = datatype(paramValueST);
            scope.addVariable(parameterVariableName, paramValueType, "Variable", param);
            variableDeclarationST.setAttribute("type", paramValueType);
            variableDeclarationST.setAttribute("field", addInternedField(parameterVariableName, paramValueType));
            
            StringTemplate variableReferenceST = template("id");
            variableReferenceST.setAttribute("var",parameterVariableName);
            StringTemplate variableAssignmentST = template("assign");
            variableAssignmentST.setAttribute("var",variableReferenceST);
            variableAssignmentST.setAttribute("value",paramValueST);
            scope.appendStatement(variableAssignmentST);
            if (param.getAbstractExpression().getDomNode().getNodeName().equals("stringConstant")) {
                StringTemplate valueST = template("sConst");
                valueST.setAttribute("value", param.getAbstractExpression().getDomNode().getFirstChild().getNodeValue());
                paramST.setAttribute("expr", valueST);
            }
            else {
                paramST.setAttribute("expr",variableReferenceST);
            }
        }
        return paramST;
    }

    void checkIsTypeDefined(String type) throws CompilationException {
	    if (!org.griphyn.vdl.type.Types.isValidType(type, typesMap.keySet())) {
	        throw new CompilationException("Type " + type + " is not defined.");
	    }
	}
    
    private boolean isPrimitiveOrArrayOfPrimitive(String type) {
        org.griphyn.vdl.type.Type t;
        try {
            t = org.griphyn.vdl.type.Types.getType(type);
            return t.isPrimitive() || (t.isArray() && t.itemType().isPrimitive());
        }
        catch (NoSuchTypeException e) {
            if (nonMappedTypes.contains(type)) {
            	return true;
            }
            else {
            	return false;
            }
        }
    }
    
	public void assign(Assign assign, VariableScope scope) throws CompilationException {
		try {
		    XmlObject value = assign.getAbstractExpressionArray(1);
            if (isProcedureCall(value)) {
                Call call = (Call) value;
                ActualParameter out = call.addNewOutput();
                XmlObject src = assign.getAbstractExpressionArray(0);
                out.getDomNode().appendChild(src.getDomNode());
                call(call, scope, false);
            }
            else {
    			StringTemplate assignST = template("assign");
    			StringTemplate varST = expressionToKarajan(assign.getAbstractExpressionArray(0), scope, true);
    			String lValueType = datatype(varST);
    			
    			
    			StringTemplate valueST = expressionToKarajan(assign.getAbstractExpressionArray(1), scope, false, lValueType);
    			
    			checkOrInferReturnedType(varST, valueST);
    			
    			assignST.setAttribute("var", varST);
    			assignST.setAttribute("value", valueST);
    			assignST.setAttribute("line", getLine(assign));
    			String rootvar = abstractExpressionToRootVariable(assign.getAbstractExpressionArray(0));
    			scope.addWriter(rootvar, getRootVariableWriteType(assign.getAbstractExpressionArray(0)), assign, assignST);
    			scope.appendStatement(assignST);
            }
		} catch(CompilationException re) {
			throw new CompilationException("Compile error in assignment at "+assign.getSrc()+": "+re.getMessage(),re);
		}
	}

    private void checkOrInferReturnedType(StringTemplate varST, StringTemplate valueST) throws CompilationException {
        String lValueType = datatype(varST);
        String rValueType = datatype(valueST);
        if (isAnyType(lValueType)) {
            if (isAnyType(rValueType)) {
                // any <- any
            }
            else {
                // any <- someType, so infer lValueType as rValueType
                setDatatype(varST, rValueType);
            }
        }
        else {
            if (isAnyType(rValueType)) {
                // someType <- any
                // only expressions that are allowed to return 'any' are procedures
                // for example readData(ret, file). These are special procedures that
                // need to look at the return type at run-time.
            }
            else if (!lValueType.equals(rValueType)){
                throw new CompilationException("You cannot assign value of type " + rValueType +
                    " to a variable of type " + lValueType);
            }
        }
    }

    private boolean isProcedureCall(XmlObject value) {
        if (value instanceof Call) {
            Call call = (Call) value;
            return proceduresMap.get(call.getProc().getLocalPart()) != null;
        }
        else {
            return false;
        }
    }

    private boolean isAnyType(String type) {
        return ProcedureSignature.ANY.equals(type);
    }
    
    public void append(Append append, VariableScope scope) throws CompilationException {
        try {
            StringTemplate appendST = template("append");
            StringTemplate array = expressionToKarajan(append.getAbstractExpressionArray(0),scope);
            StringTemplate value = expressionToKarajan(append.getAbstractExpressionArray(1),scope);
            String indexType = org.griphyn.vdl.type.Types.getArrayInnerIndexTypeName(datatype(array));
            if (!"auto".equals(indexType)) {
                throw new CompilationException("You can only append to an array with " +
                		"'auto' index type. Current index type: " + indexType);
            }
            if (!datatype(value).equals(org.griphyn.vdl.type.Types.getArrayInnerItemTypeName(datatype(array)))) {
                throw new CompilationException("You cannot append value of type " + datatype(value) +
                        " to an array of type " + datatype(array));
            }
            appendST.setAttribute("array", array);
            appendST.setAttribute("value", value);
            String rootvar = abstractExpressionToRootVariable(append.getAbstractExpressionArray(0));
            // an append is always a partial write
            scope.addWriter(rootvar, WriteType.PARTIAL, append, appendST);
            scope.appendStatement(appendST);
        } catch(CompilationException re) {
            throw new CompilationException("Compile error in assignment at "+append.getSrc()+": "+re.getMessage(),re);
        }
    }

	public void statementsForSymbols(XmlObject prog, VariableScope scope) throws CompilationException {
		XmlCursor cursor = prog.newCursor();
		cursor.selectPath("*");
		while (cursor.toNextSelection()) {
			XmlObject child = cursor.getObject();
			statementForSymbol(child, scope);
		}
	}

	public void statements(XmlObject prog, VariableScope scope) throws CompilationException {
		XmlCursor cursor = prog.newCursor();
		cursor.selectPath("*");
		while (cursor.toNextSelection()) {
			XmlObject child = cursor.getObject();
			statement(child, scope);
		}
	}

	public void statementForSymbol(XmlObject child, VariableScope scope) throws CompilationException {
		if (child instanceof Variable) {
			variableForSymbol((Variable) child, scope);
		}
		else if (child instanceof Assign
		    || child instanceof Append
			|| child instanceof Call
			|| child instanceof Foreach
			|| child instanceof Iterate
			|| child instanceof If
			|| child instanceof Switch
			|| child instanceof Procedure
			|| child instanceof Types
			|| child instanceof FormalParameter
			|| child instanceof Imports) {
			// ignore these - they're expected but we don't need to
			// do anything for them here
		} else {
			throw new CompilationException("Unexpected element in XML. Implementing class "+child.getClass()+", content "+child);
		}
	}

	public void statement(XmlObject child, VariableScope scope) throws CompilationException {
		if (child instanceof Variable) {
			variable((Variable) child, scope);
		}
		else if (child instanceof Assign) {
			assign((Assign) child, scope);
		}
		else if (child instanceof Append) {
		    append((Append) child, scope);
		}
		else if (child instanceof Call) {
			call((Call) child, scope, false);
		}
		else if (child instanceof Foreach) {
			foreachStat((Foreach)child, scope);
		}
		else if (child instanceof Iterate) {
			iterateStat((Iterate)child, scope);
		}
		else if (child instanceof If) {
			ifStat((If)child, scope);
		} else if (child instanceof Switch) {
			switchStat((Switch) child, scope);
		} else if (child instanceof Procedure
			|| child instanceof Types
			|| child instanceof FormalParameter
			|| child instanceof Imports) {
			// ignore these - they're expected but we don't need to
			// do anything for them here
		} else {
			throw new CompilationException("Unexpected element in XML. Implementing class "+child.getClass()+", content "+child);
		}
	}


	public StringTemplate call(Call call, VariableScope scope, boolean inhibitOutput) 
	throws CompilationException {
		try {
			// Check is called procedure declared previously
			String procName = call.getProc().getLocalPart();
			if (proceduresMap.get(procName) == null) {
			    if (functionsMap.containsKey(procName)) {
                    StringTemplate st = functionAsCall(call, scope);
                    if (!inhibitOutput) {
                        scope.appendStatement(st);
                    }
                    return st;
                }
				throw new CompilationException("No function or procedure '" + procName + "' found.");
			}

			// Check procedure arguments
			int noOfOptInArgs = 0;
			Map<String, FormalArgumentSignature> inArgs = 
			    new HashMap<String, FormalArgumentSignature>();
			Map<String, FormalArgumentSignature> outArgs = 
			    new HashMap<String, FormalArgumentSignature>();

			ProcedureSignature proc = proceduresMap.get(procName);
			
			if (proc.isDeprecated()) {
			    Warnings.warn(Warnings.Type.DEPRECATION, 
			        call, "Procedure " + procName + " is deprecated");
			}
			
			StringTemplate callST;
			if(proc.getInvocationMode() == ProcedureSignature.INVOCATION_USERDEFINED) {
				callST = template("callUserDefined");
			} else if(proc.getInvocationMode() == ProcedureSignature.INVOCATION_INTERNAL) {
				callST = template("callInternal");
			} else {
				throw new CompilationException
				("Unknown procedure invocation mode "+proc.getInvocationMode());
			}
			callST.setAttribute("func", procName);
			callST.setAttribute("line", getLine(call));
			/* Does number of input arguments match */
			for (int i = 0; i < proc.sizeOfInputArray(); i++) {
				if (proc.getInputArray(i).isOptional())
					noOfOptInArgs++;
				inArgs.put(proc.getInputArray(i).getName(), proc.getInputArray(i));
			}
			if (!proc.getAnyNumOfInputArgs() && (call.sizeOfInputArray() < proc.sizeOfInputArray() - noOfOptInArgs ||

				                                 call.sizeOfInputArray() > proc.sizeOfInputArray()))
				throw new CompilationException("Wrong number of procedure input arguments: specified " + call.sizeOfInputArray() +
						" and should be " + proc.sizeOfInputArray());

			/* Does number of output arguments match - no optional output args */
			for (int i = 0; i < proc.sizeOfOutputArray(); i++) {
				outArgs.put(proc.getOutputArray(i).getName(), proc.getOutputArray(i));
			}
			if (!proc.getAnyNumOfOutputArgs() && (call.sizeOfOutputArray() != proc.sizeOfOutputArray()))
				throw new CompilationException("Wrong number of procedure output arguments: specified " + call.sizeOfOutputArray() +
						" and should be " + proc.sizeOfOutputArray());


			boolean keywordArgsInput = true;
			for (int i = 0; i < call.sizeOfInputArray(); i++) {
				if (!call.getInputArray(i).isSetBind()) {
					keywordArgsInput = false;
					break;
				}
			}
			if (proc.getAnyNumOfInputArgs()) {
				/* If procedure can have any number of input args, we don't do typechecking */
				for (int i = 0; i < call.sizeOfInputArray(); i++) {
					ActualParameter input = call.getInputArray(i);
					StringTemplate argST = actualParameter(input, scope);
					callST.setAttribute("inputs", argST);
				}
			} else if (keywordArgsInput) {
			    /* if ALL arguments are specified by name=value */
                /* Re-order all (which re-orders positionals), then pass optionals by keyword */
                ActualParameter[] actuals = new ActualParameter[proc.sizeOfInputArray()];
                for (int i = 0; i < call.sizeOfInputArray(); i++) {
                    ActualParameter actual = call.getInputArray(i);
                    boolean found = false;
                    for (int j = 0; j < proc.sizeOfInputArray(); j++) {
                        FormalArgumentSignature formal = proc.getInputArray(j);
                        if (actual.getBind().equals(formal.getName())) {
                            actuals[j] = actual;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        throw new CompilationException("Formal argument " + actual.getBind() + " doesn't exist");
                    }
                }
                
                int noOfMandArgs = 0;
                for (ActualParameter actual : actuals) {
                    if (actual == null) {
                        // an optional formal parameter with no actual parameter
                        continue;
                    }
					FormalArgumentSignature formal = inArgs.get(actual.getBind());
					String formalType = formal.getType();
					
					StringTemplate argST;
					if (formal.isOptional()) {
                        argST = actualParameter(actual, formal.getName(), scope, false, formalType);
                    }
                    else {
                        argST = actualParameter(actual, null, scope, false, formalType);
                    }
					callST.setAttribute("inputs", argST);

					String actualType = datatype(argST);
					if (!formal.isAnyType() && !actualType.equals(formalType))
						throw new CompilationException("Wrong type for '" + formal.getName() + "' parameter;" +
								" expected " + formalType + ", got " + actualType);

					if (!formal.isOptional()) {
						noOfMandArgs++;
					}
				}
				if (!proc.getAnyNumOfInputArgs() && noOfMandArgs < proc.sizeOfInputArray() - noOfOptInArgs) {
					throw new CompilationException("Mandatory argument missing");
				}
			} else { /* Positional arguments */
				/* Checking types of mandatory arguments */
				for (int i = 0; i < proc.sizeOfInputArray() - noOfOptInArgs; i++) {
					ActualParameter input = call.getInputArray(i);
					FormalArgumentSignature formalArg = proceduresMap.get(procName).getInputArray(i);
					StringTemplate argST = actualParameter(input, null, scope, false, formalArg.getType());
					callST.setAttribute("inputs", argST);

					String formalType = formalArg.getType();
					String actualType = datatype(argST);
					if (!formalArg.isAnyType() && !actualType.equals(formalType))
						throw new CompilationException("Wrong type for parameter number " + i +
								", expected " + formalType + ", got " + actualType);
				}
				/* Checking types of optional arguments */
				for (int i = proc.sizeOfInputArray() - noOfOptInArgs; i < call.sizeOfInputArray(); i++) {
					ActualParameter input = call.getInputArray(i);

					String formalName = input.getBind();
					if (!inArgs.containsKey(formalName))
						throw new CompilationException("Formal argument " + formalName + " doesn't exist");
					FormalArgumentSignature formalArg = inArgs.get(formalName);
					String formalType = formalArg.getType();
					
					StringTemplate argST = actualParameter(input, formalArg.getName(), scope, false, formalType);
                    callST.setAttribute("inputs", argST);
					
					String actualType = datatype(argST);
					if (!formalArg.isAnyType() && !actualType.equals(formalType))
						throw new CompilationException("Wrong type for parameter " + formalName +
								", expected " + formalType + ", got " + actualType);
				}
			}

			boolean keywordArgsOutput = true;
			for (int i = 0; i < call.sizeOfOutputArray(); i++) {
				if (!call.getOutputArray(i).isSetBind()) {
					keywordArgsOutput = false;
					break;
				}
			}
			if (proc.getAnyNumOfOutputArgs()) {
				/* If procedure can have any number of output args, we don't do typechecking */
				for (int i = 0; i < call.sizeOfOutputArray(); i++) {
					ActualParameter output = call.getOutputArray(i);
					StringTemplate argST = actualParameter(output, scope);
					callST.setAttribute("outputs", argST);
					addWriterToScope(scope, call.getOutputArray(i).getAbstractExpression(), call, callST);
				}
			}
			if (keywordArgsOutput) {
				/* if ALL arguments are specified by name=value */
			    /* Re-order to match the formal output args */
			    ActualParameter[] actuals = new ActualParameter[proc.sizeOfOutputArray()];
			    for (int i = 0; i < call.sizeOfOutputArray(); i++) {
			        ActualParameter actual = call.getOutputArray(i);
			        boolean found = false;
			        for (int j = 0; j < proc.sizeOfOutputArray(); j++) {
			            FormalArgumentSignature formal = proc.getOutputArray(j);
			            if (actual.getBind().equals(formal.getName())) {
			                actuals[j] = actual;
			                found = true;
			                break;
			            }
			        }
			        if (!found) {
			            throw new CompilationException("Formal argument " + actual.getBind() + " doesn't exist");
			        }
			    }
				for (ActualParameter actual : actuals) {
                    if (!outArgs.containsKey(actual.getBind()))
                        throw new CompilationException("Formal argument " + actual.getBind() + " doesn't exist");
                    FormalArgumentSignature formalArg = outArgs.get(actual.getBind());
                    String formalType = formalArg.getType();

					StringTemplate argST = actualParameter(actual, null, scope, false, formalType);
					callST.setAttribute("outputs", argST);

					String actualType = datatype(argST);
					if (!formalArg.isAnyType() && !actualType.equals(formalType))
						throw new CompilationException("Wrong type for output parameter '" + actual.getBind() +
								"', expected " + formalType + ", got " + actualType);

					addWriterToScope(scope, actual.getAbstractExpression(), call, callST);
				}
			} else { /* Positional arguments */
				for (int i = 0; i < call.sizeOfOutputArray(); i++) {
					ActualParameter output = call.getOutputArray(i);
					FormalArgumentSignature formalArg = proceduresMap.get(procName).getOutputArray(i);
					String formalType = formalArg.getType();
					
					StringTemplate argST = actualParameter(output, null, scope, true, formalType);
					callST.setAttribute("outputs", argST);

					/* type check positional output args */
					String actualType = datatype(argST);
					if (!formalArg.isAnyType() && !actualType.equals(formalType))
						throw new CompilationException("Wrong type for parameter number " + i +
								", expected " + formalType + ", got " + actualType);

					addWriterToScope(scope, call.getOutputArray(i).getAbstractExpression(), call, callST);
				}
			}

			if (!inhibitOutput) {
			    scope.appendStatement(callST);
			}
			if (allVariables(callST.getAttribute("outputs")) && allVariables(callST.getAttribute("inputs"))) {
			    callST.setAttribute("serialize", Boolean.TRUE);
			}
			return callST;
		} 
		catch (CompilationException ce) {
			throw new CompilationException("Compile error in procedure invocation at " + call.getSrc(), ce);
		}
	}

	private static final Set<String> VAR_TYPES;
	
	static {
	    VAR_TYPES = new HashSet<String>();
	    VAR_TYPES.add("id");
	    VAR_TYPES.add("extractarrayelement");
	    VAR_TYPES.add("slicearray");
	    VAR_TYPES.add("extractstructureelement");
	}
	
    @SuppressWarnings("unchecked")
    private boolean allVariables(Object st) {
        List<StringTemplate> l;
        if (st == null) {
            return true;
        }
        else if (st instanceof List) {
            l = (List<StringTemplate>) st;
        }
        else {
            l = Collections.singletonList((StringTemplate) st);
        }
        for (StringTemplate pst : l) {
            if (!pst.getName().equals("call_arg")) {
                return false;
            }
            StringTemplate expr = (StringTemplate) pst.getAttribute("expr");
            if (!VAR_TYPES.contains(expr.getName())) {
                return false;
            }
        }
        return true;
    }

    private void addWriterToScope(VariableScope scope, XmlObject var, XmlObject src, StringTemplate out) throws CompilationException {
        String rootvar = abstractExpressionToRootVariable(var);
        WriteType writeType = getRootVariableWriteType(var);
        if (writeType == WriteType.FULL) {
            // don't close variables that are already closed by the function itself
            scope.inhibitClosing(rootvar);
        }
        scope.addWriter(rootvar, writeType, src, out);
    }

    public void iterateStat(Iterate iterate, VariableScope scope) throws CompilationException {
		VariableScope loopScope = new VariableScope(this, scope, EnclosureType.ALL, iterate);
		VariableScope innerScope = new VariableScope(this, loopScope, EnclosureType.LOOP, iterate);

		loopScope.addVariable(iterate.getVar(), "int", "Iteration variable", iterate);

		StringTemplate iterateST = template("iterate");
		iterateST.setAttribute("line", getLine(iterate));

		iterateST.setAttribute("var", iterate.getVar());
		innerScope.bodyTemplate = iterateST;
		
		loopScope.addWriter(iterate.getVar(), WriteType.FULL, iterate, iterateST);

		statementsForSymbols(iterate.getBody(), innerScope);
		statements(iterate.getBody(), innerScope);

		XmlObject cond = iterate.getAbstractExpression();
		StringTemplate condST = expressionToKarajan(cond, innerScope);
		iterateST.setAttribute("cond", condST);

		scope.appendStatement(iterateST);
		iterateST.setAttribute("cleanups", innerScope.getCleanups());
	}

	public void foreachStat(Foreach foreach, VariableScope scope) throws CompilationException {
		try {
			VariableScope innerScope = new VariableScope(this, scope, EnclosureType.LOOP, foreach);

			StringTemplate foreachST = template("foreach");
			foreachST.setAttribute("var", foreach.getVar());
			foreachST.setAttribute("line", lineNumber(foreach.getSrc()));

			XmlObject in = foreach.getIn().getAbstractExpression();
			StringTemplate inST = expressionToKarajan(in, scope);
			foreachST.setAttribute("in", inST);
			
			if ("id".equals(inST.getName())) {
                innerScope.setForeachSourceVar((String) inST.getAttribute("var"), foreach);
            }

			String inType = datatype(inST);
			String itemType = org.griphyn.vdl.type.Types.getArrayInnerItemTypeName(inType);
			String keyType = org.griphyn.vdl.type.Types.getArrayInnerIndexTypeName(inType);
			if (itemType == null) {
			    throw new CompilationException("You can iterate through an array structure only");
			}
			innerScope.addVariable(foreach.getVar(), itemType, "Iteration variable", foreach);
			innerScope.addWriter(foreach.getVar(), WriteType.FULL, foreach, foreachST);
			foreachST.setAttribute("indexVar", foreach.getIndexVar());
			if (foreach.getIndexVar() != null) {
			    foreachST.setAttribute("indexVarField", addInternedField(foreach.getIndexVar(), keyType));
				innerScope.addVariable(foreach.getIndexVar(), keyType, "Iteration variable", foreach);
				innerScope.addWriter(foreach.getIndexVar(), WriteType.FULL, foreach, foreachST);
			}

			innerScope.bodyTemplate = foreachST;

			statementsForSymbols(foreach.getBody(), innerScope);
			statements(foreach.getBody(), innerScope);
			
			scope.appendStatement(foreachST);
			Collection<String> cleanups = innerScope.getCleanups();
			cleanups.remove(foreach.getVar());
			foreachST.setAttribute("cleanups", cleanups);
		} 
		catch (CompilationException re) {
			throw new CompilationException("Compile error in foreach statement at " + foreach.getSrc(), re);
		}

	}

	public void ifStat(If ifstat, VariableScope scope) throws CompilationException {
		StringTemplate ifST = template("if");
		StringTemplate conditionST = expressionToKarajan(ifstat.getAbstractExpression(), scope);
		ifST.setAttribute("condition", conditionST.toString());
		ifST.setAttribute("line", getLine(ifstat));
		if (!datatype(conditionST).equals("boolean"))
			throw new CompilationException ("Condition in if statement has to be of boolean type.");

		Then thenstat = ifstat.getThen();
		Else elsestat = ifstat.getElse();

		VariableScope innerThenScope = new VariableScope(this, scope, 
		    EnclosureType.CONDITION, thenstat);
		innerThenScope.bodyTemplate = template("sub_comp");
		ifST.setAttribute("vthen", innerThenScope.bodyTemplate);

		statementsForSymbols(thenstat, innerThenScope);
		statements(thenstat, innerThenScope);
		innerThenScope.bodyTemplate.setAttribute("cleanups", innerThenScope.getCleanups());

		if (elsestat != null) {
			VariableScope innerElseScope = new VariableScope(this, scope, 
			    EnclosureType.CONDITION, elsestat);
			innerElseScope.bodyTemplate = template("sub_comp");
			innerElseScope.setThen(innerThenScope);
			ifST.setAttribute("velse", innerElseScope.bodyTemplate);

			statementsForSymbols(elsestat, innerElseScope);
			statements(elsestat, innerElseScope);
			
			innerElseScope.bodyTemplate.setAttribute("cleanups", innerElseScope.getCleanups());
		}
		else if (innerThenScope.hasPartialClosing()) {
		    // must do matching partial closing somewhere, so need 
		    // else branch even though not specified in the swift source
		    VariableScope innerElseScope = new VariableScope(this, scope, 
                EnclosureType.CONDITION, elsestat);
            innerElseScope.bodyTemplate = template("sub_comp");
            innerElseScope.setThen(innerThenScope);
            ifST.setAttribute("velse", innerElseScope.bodyTemplate);
		}
		scope.appendStatement(ifST);
	}

	public void switchStat(Switch switchstat, VariableScope scope) throws CompilationException {
		StringTemplate switchST = template("switch");
		scope.bodyTemplate.setAttribute("statements", switchST);
		StringTemplate conditionST = expressionToKarajan(switchstat.getAbstractExpression(), scope);
		switchST.setAttribute("condition", conditionST.toString());

		/* TODO can switch statement can be anything apart from int and float ? */
		if (!datatype(conditionST).equals("int") && !datatype(conditionST).equals("float"))
			throw new CompilationException("Condition in switch statements has to be of numeric type.");

		for (int i=0; i< switchstat.sizeOfCaseArray(); i++) {
			Case casestat = switchstat.getCaseArray(i);
			VariableScope caseScope = new VariableScope(this, scope, casestat);
			caseScope.bodyTemplate = new StringTemplate("case");
			switchST.setAttribute("cases", caseScope.bodyTemplate);

			caseStat(casestat, caseScope);
		}
		Default defaultstat = switchstat.getDefault();
		if (defaultstat != null) {
			VariableScope defaultScope = new VariableScope(this, scope, defaultstat);
			defaultScope.bodyTemplate = template("sub_comp");
			switchST.setAttribute("sdefault", defaultScope.bodyTemplate);
			statementsForSymbols(defaultstat, defaultScope);
			statements(defaultstat, defaultScope);
		}
	}

	public void caseStat(Case casestat, VariableScope scope) throws CompilationException {
		StringTemplate valueST = expressionToKarajan(casestat.getAbstractExpression(), scope);
		scope.bodyTemplate.setAttribute("value", valueST.toString());
		statementsForSymbols(casestat.getStatements(), scope);
		statements(casestat.getStatements(), scope);
	}
	
	public StringTemplate actualParameter(ActualParameter arg, VariableScope scope) throws CompilationException {
        return actualParameter(arg, null, scope, false, null);
    }
	
	public StringTemplate actualParameter(ActualParameter arg, String bind, VariableScope scope) throws CompilationException {
	    return actualParameter(arg, bind, scope, false, null);
	}
	
	public StringTemplate actualParameter(ActualParameter arg, VariableScope scope, boolean lvalue) throws CompilationException {
        return actualParameter(arg, null, scope, lvalue, null);
    }

	public StringTemplate actualParameter(ActualParameter arg, String bind, VariableScope scope, 
	        boolean lvalue, String expectedType) throws CompilationException {
		StringTemplate argST = template("call_arg");
		StringTemplate expST = expressionToKarajan(arg.getAbstractExpression(), scope, lvalue, expectedType);
		if (bind != null) {
		    argST.setAttribute("bind", arg.getBind());
		}
		argST.setAttribute("expr", expST);
		argST.setAttribute("datatype", datatype(expST));
		return argST;
	}

	public void binding(Binding bind, StringTemplate procST, VariableScope scope) throws CompilationException {
		StringTemplate bindST = new StringTemplate("binding");
		ApplicationBinding app;
		if ((app = bind.getApplication()) != null) {
			bindST.setAttribute("application", application(app, scope));
			procST.setAttribute("binding", bindST);
		} else throw new CompilationException("Unknown binding: "+bind);
	}

	public StringTemplate application(ApplicationBinding app, VariableScope scope) throws CompilationException {
		try {
			StringTemplate appST = new StringTemplate("application");
			appST.setAttribute("exec", app.getExecutable());
			for (int i = 0; i < app.sizeOfAbstractExpressionArray(); i++) {
				XmlObject argument = app.getAbstractExpressionArray(i);
				StringTemplate argumentST = expressionToKarajan(argument, scope);
				String type = datatype(argumentST);
				String base = org.griphyn.vdl.type.Types.getArrayInnerItemTypeName(type);
				String testType;
				// if array then use the array item type for testing
				if (base != null) {
				    testType = base;
				}
				else {
				    testType = type;
				}
				if (org.griphyn.vdl.type.Types.isPrimitive(testType)) {
				    appST.setAttribute("arguments", argumentST);
				} 
				else {
					throw new CompilationException("Cannot pass type '"+type+"' as a parameter to application '"+app.getExecutable()+"'");
				}
			}
			if(app.getStdin()!=null)
				appST.setAttribute("stdin", expressionToKarajan(app.getStdin().getAbstractExpression(), scope));
			if(app.getStdout()!=null)
				appST.setAttribute("stdout", expressionToKarajan(app.getStdout().getAbstractExpression(), scope));
			if(app.getStderr()!=null)
				appST.setAttribute("stderr", expressionToKarajan(app.getStderr().getAbstractExpression(), scope));
			addProfiles(app, scope, appST);
			return appST;
		} catch(CompilationException e) {
			throw new CompilationException(e.getMessage()+" in application "+app.getExecutable()+" at "+app.getSrc(),e);
		}
	}

	private void addProfiles(ApplicationBinding app, 
	                         VariableScope scope,
	                         StringTemplate appST) 
	throws CompilationException {
		Profile[] profiles = app.getProfileArray();
		if (profiles.length == 0) 
			return;
		StringTemplate attributes = template("swift_attributes");
		for (Profile profile : profiles) { 
			XmlObject xmlKey   = profile.getAbstractExpressionArray(0);
			XmlObject xmlValue = profile.getAbstractExpressionArray(1);
			StringTemplate key   = expressionToKarajan(xmlKey, scope);
			StringTemplate value = expressionToKarajan(xmlValue, scope);
			StringTemplate entry = template("map_entry");
			entry.setAttribute("key", key);
			entry.setAttribute("value", value);
			attributes.setAttribute("entries", entry);
		}
		appST.setAttribute("attributes", attributes);
	}

	/** Produces a Karajan function invocation from a SwiftScript invocation.
	  * The Karajan invocation will have the same name as the SwiftScript
	  * function, in the 'vdl' Karajan namespace. Parameters to the
	  * Karajan function will differ from the SwiftScript parameters in
	  * a number of ways - read the source for the exact ways in which
	  * that happens.
	  */

	public StringTemplate function(XmlObject func, String name, XmlObject[] arguments, VariableScope scope) 
	        throws CompilationException {
		StringTemplate funcST = template("function");
		funcST.setAttribute("name", name);
		funcST.setAttribute("line", getLine(func));
		ProcedureSignature funcSignature = functionsMap.get(name);
		if (funcSignature == null) {
			throw new CompilationException("Unknown function: @" + name);
		}
		int noOfOptInArgs = 0;
		for (int i = 0; i < funcSignature.sizeOfInputArray(); i++) {
			if (funcSignature.getInputArray(i).isOptional())
				noOfOptInArgs++;
		}
		if (!funcSignature.getAnyNumOfInputArgs() &&
			(arguments.length < funcSignature.sizeOfInputArray() - noOfOptInArgs ||
			 arguments.length > funcSignature.sizeOfInputArray()))
			throw new CompilationException("Wrong number of function input arguments: specified " +
					arguments.length + " and should be " + funcSignature.sizeOfInputArray());

		for(int i = 0; i < arguments.length; i++ ) {
		    String type = null;
		    if (!funcSignature.getAnyNumOfInputArgs()) {
		        funcSignature.getInputArray(i).getType();
		    }
			StringTemplate exprST = expressionToKarajan(arguments[i], scope, false, type);
			funcST.setAttribute("args", exprST);

			/* Type check of function arguments */
			if (!funcSignature.getAnyNumOfInputArgs()) {
				String actualType = datatype(exprST);
				FormalArgumentSignature fas = funcSignature.getInputArray(i);
				if (!fas.isAnyType() && !fas.getType().equals(actualType))
					throw new CompilationException("Wrong type for parameter " + i +
					    (fas.getName() == null ? "" : "('" + fas.getName() + "')") + 
							"; expected " + fas.getType() + ", got " + actualType);
				}
		}

		return funcST;
	}

	static final String SWIFTSCRIPT_NS = "http://ci.uchicago.edu/swift/2009/02/swiftscript";

	static final QName OR_EXPR = new QName(SWIFTSCRIPT_NS, "or");
	static final QName AND_EXPR = new QName(SWIFTSCRIPT_NS, "and");
	static final QName BOOL_EXPR = new QName(SWIFTSCRIPT_NS, "booleanConstant");
	static final QName INT_EXPR = new QName(SWIFTSCRIPT_NS, "integerConstant");
	static final QName FLOAT_EXPR = new QName(SWIFTSCRIPT_NS, "floatConstant");
	static final QName STRING_EXPR = new QName(SWIFTSCRIPT_NS, "stringConstant");
	static final QName COND_EXPR = new QName(SWIFTSCRIPT_NS, "cond");
	static final QName ARITH_EXPR = new QName(SWIFTSCRIPT_NS, "arith");
	static final QName UNARY_NEGATION_EXPR = new QName(SWIFTSCRIPT_NS, "unaryNegation");
	static final QName NOT_EXPR = new QName(SWIFTSCRIPT_NS, "not");
	static final QName VARIABLE_REFERENCE_EXPR = new QName(SWIFTSCRIPT_NS, "variableReference");
	static final QName ARRAY_SUBSCRIPT_EXPR = new QName(SWIFTSCRIPT_NS, "arraySubscript");
	static final QName STRUCTURE_MEMBER_EXPR = new QName(SWIFTSCRIPT_NS, "structureMember");
	static final QName ARRAY_EXPR = new QName(SWIFTSCRIPT_NS, "array");
	static final QName RANGE_EXPR = new QName(SWIFTSCRIPT_NS, "range");
	static final QName FUNCTION_EXPR = new QName(SWIFTSCRIPT_NS, "function");
	static final QName CALL_EXPR = new QName(SWIFTSCRIPT_NS, "call");
	
	public StringTemplate expressionToKarajan(XmlObject expression, VariableScope scope) throws CompilationException {
	    return expressionToKarajan(expression, scope, false, null);
    }
	
	public StringTemplate expressionToKarajan(XmlObject expression, VariableScope scope, 
            boolean lvalue) throws CompilationException {
	    return expressionToKarajan(expression, scope, lvalue, null);
	}

	/** converts an XML intermediate form expression into a
	 *  Karajan expression.
	 */
	public StringTemplate expressionToKarajan(XmlObject expression, VariableScope scope, 
	        boolean lvalue, String expectedType) throws CompilationException {
	    
		Node expressionDOM = expression.getDomNode();
		String namespaceURI = expressionDOM.getNamespaceURI();
		String localName = expressionDOM.getLocalName();
		QName expressionQName = new QName(namespaceURI, localName);

		if(expressionQName.equals(OR_EXPR))	{
			StringTemplate st = template("binaryop");
			BinaryOperator o = (BinaryOperator)expression;
			StringTemplate leftST = expressionToKarajan(o.getAbstractExpressionArray(0), scope);
			StringTemplate rightST = expressionToKarajan(o.getAbstractExpressionArray(1), scope);
			st.setAttribute("op","||");
			st.setAttribute("left", leftST);
			st.setAttribute("right", rightST);
			if (datatype(leftST).equals("boolean") && datatype(rightST).equals("boolean"))
				st.setAttribute("datatype", "boolean");
			else
				throw new CompilationException("Or operation can only be applied to parameters of type boolean.");
			return st;
		} else if (expressionQName.equals(AND_EXPR)) {
			StringTemplate st = template("binaryop");
			BinaryOperator o = (BinaryOperator)expression;
			StringTemplate leftST = expressionToKarajan(o.getAbstractExpressionArray(0), scope);
			StringTemplate rightST = expressionToKarajan(o.getAbstractExpressionArray(1), scope);
			st.setAttribute("op","&amp;&amp;");
			st.setAttribute("left", leftST);
			st.setAttribute("right", rightST);
			if (datatype(leftST).equals("boolean") && datatype(rightST).equals("boolean"))
				st.setAttribute("datatype", "boolean");
			else
				throw new CompilationException("And operation can only be applied to parameters of type boolean.");
			return st;
		} else if (expressionQName.equals(BOOL_EXPR)) {
			XmlBoolean xmlBoolean = (XmlBoolean) expression;
			boolean b = xmlBoolean.getBooleanValue();
			StringTemplate st = template("bConst");
			st.setAttribute("value",""+b);
			st.setAttribute("datatype", "boolean");
			return st;
		} else if (expressionQName.equals(INT_EXPR)) {
			XmlInt xmlInt = (XmlInt) expression;
			int i = xmlInt.getIntValue();
			Integer iobj = Integer.valueOf(i);
			String internedID;
			if(intInternMap.get(iobj) == null) {
				internedID = "swift.int." + i;
				intInternMap.put(iobj, internedID);
			} else {
				internedID = intInternMap.get(iobj);
			}
			StringTemplate st = template("id");
			st.setAttribute("var", internedID);
			st.setAttribute("datatype", "int");
			return st;
		} else if (expressionQName.equals(FLOAT_EXPR)) {
			XmlFloat xmlFloat = (XmlFloat) expression;
			float f = xmlFloat.getFloatValue();
			Float fobj = new Float(f);
			String internedID;
			if(floatInternMap.get(fobj) == null) {
				internedID = "swift.float." + (internedIDCounter++);
				floatInternMap.put(fobj, internedID);
			} else {
				internedID = floatInternMap.get(fobj);
			}
			StringTemplate st = template("id");
			st.setAttribute("var",internedID);
			st.setAttribute("datatype", "float");
			return st;
		} else if (expressionQName.equals(STRING_EXPR)) {
			XmlString xmlString = (XmlString) expression;
			String s = xmlString.getStringValue();
			String internedID;
			if (stringInternMap.get(s) == null) {
				internedID = "swift.string." + (internedIDCounter++);
				stringInternMap.put(s, internedID);
			} else {
				internedID = stringInternMap.get(s);
			}
			StringTemplate st = template("id");
			st.setAttribute("var", internedID);
			st.setAttribute("datatype", "string");
			return st;
		} else if (expressionQName.equals(COND_EXPR)) {
			StringTemplate st = template("binaryop");
			LabelledBinaryOperator o = (LabelledBinaryOperator) expression;
			StringTemplate leftST = expressionToKarajan(o.getAbstractExpressionArray(0), scope);
			StringTemplate rightST = expressionToKarajan(o.getAbstractExpressionArray(1), scope);
			st.setAttribute("op", o.getOp());
			st.setAttribute("left", leftST);
			st.setAttribute("right", rightST);

			checkTypesInCondExpr(o.getOp(), datatype(leftST), datatype(rightST), st);
			return st;
		} else if (expressionQName.equals(ARITH_EXPR)) {
			LabelledBinaryOperator o = (LabelledBinaryOperator) expression;
			StringTemplate st = template("binaryop");
			st.setAttribute("op", o.getOp());
			StringTemplate leftST = expressionToKarajan(o.getAbstractExpressionArray(0), scope);
			StringTemplate rightST = expressionToKarajan(o.getAbstractExpressionArray(1), scope);
			st.setAttribute("left", leftST);
			st.setAttribute("right", rightST);

			checkTypesInArithmExpr(o.getOp(), datatype(leftST), datatype(rightST), st);
			return st;
		} else if (expressionQName.equals(UNARY_NEGATION_EXPR)) {
			UnlabelledUnaryOperator e = (UnlabelledUnaryOperator) expression;
			StringTemplate st = template("unaryNegation");
			StringTemplate expST = expressionToKarajan(e.getAbstractExpression(), scope);
			st.setAttribute("exp", expST);
			if (!(datatype(expST).equals("float")) && !(datatype(expST).equals("int")))
				throw new CompilationException("Negation operation can only be applied to parameter of numeric types.");
			st.setAttribute("datatype", datatype(expST));
			return st;
		} else if (expressionQName.equals(NOT_EXPR)) {
			// TODO not can probably merge with 'unary'
			UnlabelledUnaryOperator e = (UnlabelledUnaryOperator)expression;
			StringTemplate st = template("not");
			StringTemplate expST = expressionToKarajan(e.getAbstractExpression(), scope);
			st.setAttribute("exp", expST);
			if (datatype(expST).equals("boolean"))
				st.setAttribute("datatype", "boolean");
			else
				throw new CompilationException("Not operation can only be applied to parameter of type boolean.");
			return st;
		} else if (expressionQName.equals(VARIABLE_REFERENCE_EXPR)) {
			XmlString xmlString = (XmlString) expression;
			String s = xmlString.getStringValue();
			if(!scope.isVariableDefined(s)) {
				throw new CompilationException("Variable " + s + " was not declared in this scope.");
			}
									
			if (!lvalue) {
			    scope.addReader(s, false, expression);
			}
			StringTemplate st = template("id");
			st.setAttribute("var", s);
			String actualType;

			actualType = scope.getVariableType(s);
			st.setAttribute("datatype", actualType);
			return st;
		} else if (expressionQName.equals(ARRAY_SUBSCRIPT_EXPR)) {
			BinaryOperator op = (BinaryOperator) expression;
			StringTemplate arrayST = expressionToKarajan(op.getAbstractExpressionArray(1), scope);
			StringTemplate parentST = expressionToKarajan(op.getAbstractExpressionArray(0), scope, true);

			String indexType = datatype(arrayST);
			String declaredIndexType = org.griphyn.vdl.type.Types.getArrayOuterIndexTypeName(datatype(parentST));
			// the index type must match the declared index type,
			// unless the declared index type is *
			
			// and really, at this point type checking should be delegated to the type system
			// instead of the ad-hoc string comparisons
			if (datatype(arrayST).equals("string")) {
			    XmlObject var = op.getAbstractExpressionArray(1);
			    // make sure this is array["somestring"] rather than array[otherExpressionOfTypeString]
			    if (var instanceof XmlString) {
                    XmlString vars = (XmlString) var;
                    if (vars.getStringValue().equals("*")) {
                        return parentST;
                    }
			    }
            }
			
			if (!indexType.equals(declaredIndexType) 
			        && !"".equals(declaredIndexType) 
			        && !"any".equals(declaredIndexType)) {
			    throw new CompilationException("Supplied array index type (" 
			        + indexType + ") does not match the declared index type (" + declaredIndexType + ")");
			}
			
			scope.addReader((String) parentST.getAttribute("var"), true, expression);
			
			StringTemplate newst = template("extractarrayelement");
			newst.setAttribute("arraychild", arrayST);
			newst.setAttribute("parent", parentST);
			newst.setAttribute("datatype", org.griphyn.vdl.type.Types.getArrayOuterItemTypeName(datatype(parentST)));

			return newst;
		} else if (expressionQName.equals(STRUCTURE_MEMBER_EXPR)) {
			StructureMember sm = (StructureMember) expression;
			StringTemplate parentST = expressionToKarajan(sm.getAbstractExpression(), scope, true);

			String parentType = datatype(parentST);

			// if the parent is an array, then check against
			// the base type of the array
			
			String baseType = org.griphyn.vdl.type.Types.getArrayInnerItemTypeName(parentType);
			String indexType = org.griphyn.vdl.type.Types.getArrayInnerIndexTypeName(parentType);
			String arrayType = parentType;

			boolean arrayMode = false;
			if (baseType != null) {
				arrayMode = true;
				parentType = baseType;
			}

			// TODO this should be a map lookup of some kind?

			Type t = typesMap.get(parentType);
			
			if (t == null) {
			    // this happens when trying to access a field of a built-in type
			    // which cannot currently be a structure
			    throw new CompilationException("Type " + parentType + " is not a structure");
			}
			
			TypeStructure ts = t.getTypestructure();
			
			String actualType = null;
			for (int j = 0; j < ts.sizeOfMemberArray(); j++) {
				if (ts.getMemberArray(j).getMembername().equals(sm.getMemberName())) 	{
					actualType = ts.getMemberArray(j).getMembertype();
					break;
				}
			}
			if (actualType == null) {
                throw new CompilationException("No member " + sm.getMemberName() + " in type " + parentType);
			}
			StringTemplate newst;
			if(arrayMode) {
			    actualType = actualType + "[" + indexType + "]";
				newst = template("slicearray");
			}
			else {
				newst = template("extractstructelement");
			}
			
			scope.addReader((String) parentST.getAttribute("var"), true, expression);
			newst.setAttribute("parent", parentST);
			newst.setAttribute("memberchild", sm.getMemberName());
            newst.setAttribute("datatype", actualType);
            return newst;
			// TODO the template layout for this and ARRAY_SUBSCRIPT are
			// both a bit convoluted for historical reasons.
			// should be straightforward to tidy up.
		} else if (expressionQName.equals(ARRAY_EXPR)) {
			Array array = (Array)expression;
			StringTemplate st = template("array");
			String elemType = "";
			for (int i = 0; i < array.sizeOfAbstractExpressionArray(); i++) {
				XmlObject expr = array.getAbstractExpressionArray(i);
				StringTemplate elemST = expressionToKarajan(expr, scope);
				if (i == 0)
					elemType = datatype(elemST);
				else if (!elemType.equals(datatype(elemST)))
					throw new CompilationException("Wrong array element type.");
				st.setAttribute("elements", elemST);
			}
			if (elemType.equals(""))
				logger.warn("WARNING: Empty array constant");
			st.setAttribute("datatype", elemType + "[int]");
			return st;
		} else if (expressionQName.equals(RANGE_EXPR)) {
			Range range = (Range)expression;
			StringTemplate st = template("range");
			StringTemplate fromST = expressionToKarajan(range.getAbstractExpressionArray(0), scope);
			StringTemplate toST = expressionToKarajan(range.getAbstractExpressionArray(1), scope);
			st.setAttribute("from", fromST);
			st.setAttribute("to", toST);
			StringTemplate stepST = null;
			if(range.sizeOfAbstractExpressionArray() == 3) {// step is optional
				stepST = expressionToKarajan(range.getAbstractExpressionArray(2), scope);
				st.setAttribute("step", stepST);
			}

			String fromType = datatype(fromST);
			String toType = datatype(toST);
			if (!fromType.equals(toType)) {
			    throw new CompilationException("To and from range values must have the same type");
            }
			if (stepST != null && !datatype(stepST).equals(fromType)) {
			    throw new CompilationException("Step (" + datatype(stepST) + 
			            ") must be of the same type as from and to (" + toType + ")");
			}
			if (stepST == null && (!fromType.equals("int") || !toType.equals("int"))) {
				throw new CompilationException("Step in range specification can be omitted only when from and to types are int");
			}
			else if (fromType.equals("int") && toType.equals("int")) {
				st.setAttribute("datatype", "int[int]");
			}
			else if (fromType.equals("float") && toType.equals("float") &&
					datatype(stepST).equals("float")) {
				st.setAttribute("datatype", "float[int]");
			}
			else {
				throw new CompilationException("Range can only be specified with numeric types");
			}
			return st;
		} else if (expressionQName.equals(FUNCTION_EXPR)) {
			Function f = (Function) expression;
			return functionExpr(f, scope);
		} else if (expressionQName.equals(CALL_EXPR)) {
		    Call c = (Call) expression;
		    String name = c.getProc().getLocalPart();
		    
		    if (proceduresMap.containsKey(name)) {
		        return callExpr(c, scope, expectedType);
		    }
		    else {
		        if (functionsMap.containsKey(name)) {
		            return functionExpr(c, scope);
		        }
		        else {
		            throw new CompilationException("No function or procedure '" + name + "' found.");
		        }
		    }
		} else {
			throw new CompilationException("unknown expression implemented by class "+expression.getClass()+" with node name "+expressionQName +" and with content "+expression);
		}
		// perhaps one big throw catch block surrounding body of this method
		// which shows Compiler Exception and line number of error
	}

    private StringTemplate callExpr(Call c, VariableScope scope, String expectedType) throws CompilationException {
        c.addNewOutput();
        VariableScope subscope = new VariableScope(this, scope, c);
        VariableReferenceDocument ref = VariableReferenceDocument.Factory.newInstance();
        ref.setVariableReference("swift.callintermediate");
        c.getOutputArray(0).set(ref);
        String name = c.getProc().getLocalPart();
        ProcedureSignature funcSignature = proceduresMap.get(name);

        if (funcSignature.sizeOfOutputArray() != 1) {
            throw new CompilationException("Procedure " + name + " must have exactly one " +
                    "return value to be used in an expression.");
        }
        
        StringTemplate call = template("callexpr");

        String type = funcSignature.getOutputArray(0).getType();
        
        if (isAnyType(type)) {
            if (expectedType != null) {
                type = expectedType;
            }
            else {
                throw new CompilationException("Cannot infer return type of procedure call");
            }
        }
        
        if (!isPrimitiveOrArrayOfPrimitive(type)) {
        	call.setAttribute("mapping", true);
        }
        
        subscope.addInternalVariable("swift.callintermediate", type, null);

        call.setAttribute("datatype", type);
        call.setAttribute("field", addInternedField("swift.callintermediate", type));
        call.setAttribute("call", call(c, subscope, true));
        if (!isPrimitiveOrArrayOfPrimitive(type)) {
            call.setAttribute("prefix", UUIDGenerator.getInstance().generateRandomBasedUUID().toString());
        }
        return call;
    }
    
    private StringTemplate functionExpr(Call c, VariableScope scope) throws CompilationException {
        String name = c.getProc().getLocalPart();
        ProcedureSignature funcSignature = functionsMap.get(name);
        if (funcSignature == null) {
            throw new CompilationException("Function " + name + " is not defined.");
        }
        
        StringTemplate st = function(c, name, getCallParams(c), scope);
        /* Set function output type */
        /* Functions have only one output parameter */
        st.setAttribute("datatype", funcSignature.getOutputArray(0).getType());
        if (funcSignature.isDeprecated()) {
            Warnings.warn(Warnings.Type.DEPRECATION, 
                c, "Function " + name + " is deprecated");
        }
        
        return st;
    }

    private StringTemplate functionExpr(Function f, VariableScope scope) throws CompilationException {
        String name = f.getName();
        if (name.equals("")) {
            name = "filename";
        }
        else {
            // the @var shortcut for filename(var) is not deprecated
            Warnings.warn(Warnings.Type.DEPRECATION, 
                "The @ syntax for function invocation is deprecated");
        }
        ProcedureSignature funcSignature = functionsMap.get(name);
        if (funcSignature == null) {
            throw new CompilationException("Function " + name + " is not defined.");
        }
        StringTemplate st = function(f, name, f.getAbstractExpressionArray(), scope);
        /* Set function output type */    
        /* Functions have only one output parameter */
        st.setAttribute("datatype", funcSignature.getOutputArray(0).getType());
        
        if (funcSignature.isDeprecated()) {
            Warnings.warn(Warnings.Type.DEPRECATION, 
                f, "Function " + name + " is deprecated");
        }
    
        return st;
    }
    
    /**
     * Translates a call(output, params) into a output = function(params) form.
     */
    private StringTemplate functionAsCall(Call call, VariableScope scope) throws CompilationException {
        String name = call.getProc().getLocalPart();
        ProcedureSignature sig = functionsMap.get(name);
        if (sig == null) {
            throw new CompilationException("Function " + name + " is not defined.");
        }
        if (call.getOutputArray().length == 0) {
            throw new CompilationException("Call to a function that does not return a value");
        }
        if (call.getOutputArray().length > 1) {
            throw new CompilationException("Cannot assign multiple values with a function invocation");
        }
        
        StringTemplate value = function(call, name, getCallParams(call), scope);
        value.setAttribute("datatype", sig.getOutputArray(0).getType());
        StringTemplate assign = assignFromCallReturn(call, value, scope);
        
        if (sig.isDeprecated()) {
            Warnings.warn(Warnings.Type.DEPRECATION, call, "Function " + name + " is deprecated");
        }
    
        return assign;
    }

    private XmlObject[] getCallParams(Call call) {
        XmlObject[] params = new XmlObject[call.getInputArray().length];
        for (int i = 0; i < params.length; i++) {
            params[i] = call.getInputArray(i).getAbstractExpression();
        }
        return params;
    }

    private StringTemplate assignFromCallReturn(Call call, StringTemplate valueST, VariableScope scope) 
            throws CompilationException {
        StringTemplate assignST = template("assign");
        XmlObject var = call.getOutputArray(0).getAbstractExpression();
        StringTemplate varST = expressionToKarajan(var, scope, true);
        if (! (datatype(varST).equals(datatype(valueST)) || datatype(valueST).equals("java"))) {
            throw new CompilationException("You cannot assign value of type " + datatype(valueST) +
                    " to a variable of type " + datatype(varST));
        }
        assignST.setAttribute("var", varST);
        assignST.setAttribute("value", valueST);
        assignST.setAttribute("line", getLine(call));
        String rootvar = abstractExpressionToRootVariable(var);
        scope.addWriter(rootvar, getRootVariableWriteType(var), call, assignST);
        return assignST;
    }


    void checkTypesInCondExpr(String op, String left, String right, StringTemplate st)
			throws CompilationException {
		if (left.equals(right))
			st.setAttribute("datatype", "boolean");
		else
			throw new CompilationException("Conditional operator can only be applied to parameters of same type.");

		if ((op.equals("==") || op.equals("!=")) && !left.equals("int") && !left.equals("float")
				                                 && !left.equals("string") && !left.equals("boolean"))
			throw new CompilationException("Conditional operator " + op +
					" can only be applied to parameters of type int, float, string and boolean.");

		if ((op.equals("<=") || op.equals(">=") || op.equals(">") || op.equals("<"))
				&& !left.equals("int") && !left.equals("float") && !left.equals("string"))
			throw new CompilationException("Conditional operator " + op +
			" can only be applied to parameters of type int, float and string.");
	}

	void checkTypesInArithmExpr(String op, String left, String right, StringTemplate st)
			throws CompilationException {
	    /* 
	     * +, -, /, * : int, int -> int
	     * +, -, /, *: float, float -> float
	     * +, -, /, * : int, float -> float
	     * +, -, /, * : float, int -> float
	     * 
	     * + : string, string -> string
         * + : string, int -> string
         * + : int, string -> string
         * + : string, float -> string
         * + : float, string -> string
         * 
         * %/ : int, int -> int
         * %% : int, int -> int
	     */
	    
	    switch (op.charAt(0)) {
	        case '+':
	            if (left.equals("string")) {
	                if (!right.equals("string") && !right.equals("int") && !right.equals("float")) {
	                    throw new CompilationException("Operator '+' cannot be applied to 'string'  and '" + right + "'");
	                }
	                st.setAttribute("datatype", "string");
	            }
	            else if (right.equals("string")) {
	                if (!left.equals("string") && !left.equals("int") && !left.equals("float")) {
                        throw new CompilationException("Operator '+' cannot be applied to '" + left + "'  and 'string'");
                    }
	                st.setAttribute("datatype", "string");
	            }
	            else {
	                checkTypesInType1ArithmExpr(op, left, right, st);
	            }
	            break;
	        case '-':
	        case '/':
	        case '*':
	            checkTypesInType1ArithmExpr(op, left, right, st);
	            break;
	        case '%':
	            checkTypesInType2ArithmExpr(op, left, right, st);
	            break;
	        default:
	            throw new CompilationException("Unknown operator '" + op + "'");
	    }
	}

	private void checkTypesInType1ArithmExpr(String op, String left, String right, StringTemplate st) 
	        throws CompilationException {
	    /* 
         * int, int -> int
         * float, float -> float
         * int, float -> float
         * float, int -> float
         */
	    if (left.equals("int")) {
	        if (right.equals("int")) {
	            st.setAttribute("datatype", "int");
	            return;
	        }
	        else if (right.equals("float")) {
	            st.setAttribute("datatype", "float");
	            return;
	        }
	    }
	    else if (left.equals("float")) {
	        if (right.equals("int") || right.equals("float")) {
	            st.setAttribute("datatype", "float");
	            return;
            }
	    }
	    throw new CompilationException("Operator '" + op + "' cannot be applied to '" + left + "' and '" + right + "'");
    }

    private void checkTypesInType2ArithmExpr(String op, String left, String right, StringTemplate st) 
            throws CompilationException {
        if (!left.equals("int") || !right.equals("int")) {
            throw new CompilationException("Operator '" + op + "' can only be applied to 'int' and 'int'");
        }
        st.setAttribute("datatype", "int");
    }

    public String abstractExpressionToRootVariable(XmlObject expression) throws CompilationException {
		Node expressionDOM = expression.getDomNode();
		String namespaceURI = expressionDOM.getNamespaceURI();
		String localName = expressionDOM.getLocalName();
		QName expressionQName = new QName(namespaceURI, localName);
		if (expressionQName.equals(VARIABLE_REFERENCE_EXPR)) {
			XmlString xmlString = (XmlString) expression;
			String s = xmlString.getStringValue();
			return s;
		} else if (expressionQName.equals(ARRAY_SUBSCRIPT_EXPR)) {
			BinaryOperator op = (BinaryOperator) expression;
			return abstractExpressionToRootVariable(op.getAbstractExpressionArray(0));
		} else if (expressionQName.equals(STRUCTURE_MEMBER_EXPR)) {
			StructureMember sm = (StructureMember) expression;
			return abstractExpressionToRootVariable(sm.getAbstractExpression());
		} else {
			throw new CompilationException("Could not find root for abstract expression.");
		}
	}

	public WriteType getRootVariableWriteType(XmlObject expression) {
		Node expressionDOM = expression.getDomNode();
		String namespaceURI = expressionDOM.getNamespaceURI();
		String localName = expressionDOM.getLocalName();
		QName expressionQName = new QName(namespaceURI, localName);
		if (expressionQName.equals(VARIABLE_REFERENCE_EXPR)) {
			return WriteType.FULL;
		} else if (expressionQName.equals(ARRAY_SUBSCRIPT_EXPR)) {
			return WriteType.PARTIAL;
		} else if (expressionQName.equals(STRUCTURE_MEMBER_EXPR)) {
			return WriteType.PARTIAL;
		} else {
			throw new RuntimeException("Could not find root for abstract expression.");
		}
	}
	
	public void generateInternedFields(StringTemplate programTemplate) {
	    for (Map.Entry<InternedField, String> e : usedFields.entrySet()) {
            StringTemplate st = template("fieldConst");
            st.setAttribute("name", e.getValue());
            st.setAttribute("id", e.getKey().name);
            st.setAttribute("type", e.getKey().type);
            programTemplate.setAttribute("constants", st);
        }
	}

	private String internedFieldName(InternedField f) {
	    String v = usedFields.get(f);
	    if (v == null) {
	        throw new IllegalArgumentException("No such interned field: " + f);
	    }
	    return v;
    }
	
	private int fieldCounter = 1;
	private String addInternedField(String name, String type) {
	    String v = internedFieldName(name, type);
	    usedFields.put(new InternedField(name, type), v);
	    return v;
    }
	
	public static String internedFieldName(String name, String type) {
	    return "swift.field." + name + "." + type.replace("[", ".array.").replace("]", "");
	}

    public void generateInternedConstants(StringTemplate programTemplate) {
	    generateInternedConstants(programTemplate, stringInternMap, "sConst");
	    generateInternedConstants(programTemplate, intInternMap, "iConst");
	    generateInternedConstants(programTemplate, floatInternMap, "fConst");
	}

	
	private void generateInternedConstants(StringTemplate programTemplate, Map<?, String> map,
            String cTemplate) {
	    for (Object key : map.keySet()) {
            String variableName = map.get(key);
            StringTemplate st = template(cTemplate);
            st.setAttribute("value", toKarajanValue(key));
            StringTemplate vt = template("globalConstant");
            vt.setAttribute("name", variableName);
            vt.setAttribute("expr", st);
            programTemplate.setAttribute("constants", vt);
        }
    }
	
	private String toKarajanValue(Object o) {
	    if (o instanceof String) {
	        return escape((String) o);
	    }
	    else {
	        return String.valueOf(o);
	    }
	}

    private String escape(String in) {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < in.length(); i++) {
	        char c = in.charAt(i);
	        switch (c) {
	            case '"':
	                sb.append("\\\"");
	                break;
	            case '{':
	            	sb.append("\\{");
	            	break;
	            default:
	                sb.append(c);
	        }
	    }
        return sb.toString();
    }
	
	public static String normalize(String type) {
	    return org.griphyn.vdl.type.Types.normalize(type);
	}
	
	public static String lineNumber(String src) {
	    return src.substring(src.indexOf(' ') + 1);
	}

	String datatype(StringTemplate st) {
	    try {
	        return normalize(st.getAttribute("datatype").toString());
	    }
	    catch (Exception e) {
	        throw new RuntimeException("Not typed properly: " + st);
	    }
	}
	
	protected void setDatatype(StringTemplate st, String type) {
	    st.removeAttribute("datatype");
	    st.setAttribute("datatype", type);
	}
}
