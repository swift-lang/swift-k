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
import org.apache.xmlbeans.XmlDouble;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
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
import org.globus.swift.language.Variable.Mapping;
import org.globus.swift.language.Variable.Mapping.Param;
import org.griphyn.vdl.engine.VariableScope.AccessType;
import org.griphyn.vdl.engine.VariableScope.EnclosureType;
import org.griphyn.vdl.engine.VariableScope.VariableOrigin;
import org.griphyn.vdl.engine.VariableScope.WriteType;
import org.griphyn.vdl.karajan.CompilationException;
import org.griphyn.vdl.karajan.Loader;
import org.griphyn.vdl.mapping.MapperFactory;
import org.griphyn.vdl.toolkit.VDLt2VDLx;
import org.griphyn.vdl.type.DuplicateFieldException;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;
import org.safehaus.uuid.UUIDGenerator;
import org.w3c.dom.Node;

public class Karajan {
	public static final Logger logger = Logger.getLogger(Karajan.class);
	
	public static final String STDLIB_V2 = "stdlib.v2";
	public static final String STDLIB_V1 = "stdlib.v1";

	Map<String, String> stringInternMap = new HashMap<String, String>();
	Map<Integer, String> intInternMap = new HashMap<Integer, String>();
	Map<Double, String> floatInternMap = new HashMap<Double, String>();
	private FunctionsMap functionsMap;
	Types types = new Types(Types.BUILT_IN_TYPES);
	Set<String> nonMappedTypes = new HashSet<String>();
	
	private class InternedField {
	    public final String name;
	    public final Type type;
	    
	    public InternedField(String name, Type type) {
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
	private boolean newStdLib;

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
		} 
		catch(IOException ioe) {
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

		if (programDoc.validate(options)) {
			logger.debug("Validation of XML intermediate file was successful");
		} 
		else {
			logger.warn("Validation of XML intermediate file failed.");
			logger.warn("Validation errors:");
			for (XmlError error : errors) {
				logger.warn(error.toString());
			}
			System.exit(3);
		}
		return programDoc;
	}

	public Karajan() {
		// used by some templates
		addInternedField("temp", Types.INT);
		addInternedField("const", Types.INT);
		addInternedField("const", Types.FLOAT);
		addInternedField("const", Types.STRING);
		addInternedField("const", Types.BOOLEAN);
	}

	void setTemplateGroup(StringTemplateGroup tempGroup) {
		m_templates = tempGroup;
	}

	protected StringTemplate template(String name) {
		return m_templates.getInstanceOf(name);
	}

    private void processImports(Program prog) throws CompilationException {
		Imports imports = prog.getImports();
		if (imports!=null) {
			logger.debug("Processing SwiftScript imports");
            // process imports in reverse order
			for (int i = imports.sizeOfImportArray() - 1 ;  i >=0 ; i--) {
				String moduleToImport = imports.getImportArray(i);
				logger.debug("Importing module "+moduleToImport);
				if (!importedNames.contains(moduleToImport)) {
				    if (moduleToImport.equals(STDLIB_V2)) {
				        newStdLib = true;
				    }
				    else if (moduleToImport.equals(STDLIB_V1)) {
                        newStdLib = false;
                    }
				    else {
				        processImport(moduleToImport);
				    }
				} 
				else {
					logger.debug("Skipping repeated import of "+moduleToImport);
				}
			}
		}
	}

	private void processImport(String moduleToImport) throws CompilationException {
	    // TODO PATH/PERL5LIB-style path handling
        //String swiftfilename = "./"+moduleToImport+".swift";
        //String xmlfilename = "./"+moduleToImport+".xml";
        String lib_path = System.getenv("SWIFT_LIB");
        String swiftfilename = moduleToImport + ".swift";
        String xmlfilename = moduleToImport + ".swiftx";

        File local = new File(swiftfilename);
        if (!(lib_path == null || local.exists())) {
            String[] path = lib_path.split(":");
            for(String entry : path) {
                String lib_script_location = entry + "/" + swiftfilename;
                File file = new File(lib_script_location);
                
                if(file.exists()) {
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
        } 
        catch(Exception e) {
            throw new CompilationException("When processing import "+moduleToImport, e);
        }
    }

    private void processTypes(Program prog, VariableScope scope) throws CompilationException {
	    TypesDocument.Types progTypes = prog.getTypes();
		if (progTypes != null) {
			for (int i = 0; i < progTypes.sizeOfTypeArray(); i++) {
				TypesDocument.Types.Type theType = progTypes.getTypeArray(i);
				String typeName = theType.getTypename();
				String typeAlias = theType.getTypealias();

				logger.debug("Processing type " + typeName);

				StringTemplate st = template("typeDef");
				st.setAttribute("name", typeName);
				if (typeAlias != null && !typeAlias.equals("") && !typeAlias.equals("string")) {
				    addTypeAlias(typeName, typeAlias, st);
				}
				else {
				    addStructType(typeName, theType, st);
				}
				scope.bodyTemplate.setAttribute("types", st);
			}
		}
		try {
            types.resolveTypes();
        }
        catch (NoSuchTypeException e) {
            throw new CompilationException("Cannot resolve types", e);
        }
	}

	private void addStructType(String typeName, TypesDocument.Types.Type theType, StringTemplate st) 
	        throws CompilationException {
	    
	    Type type = Type.Factory.createType(typeName, false);
	    TypeStructure ts = theType.getTypestructure();
        boolean allPrimitive = ts.sizeOfMemberArray() > 0;
        for (int j = 0; j < ts.sizeOfMemberArray(); j++) {
            TypeRow tr = ts.getMemberArray(j);

            StringTemplate stMember = template("memberdefinition");
            String fieldName = tr.getMembername();
            org.griphyn.vdl.type.Type fieldType;
            try {
                fieldType = findType(tr.getMembertype());
                type.addField(fieldName, fieldType);
                if (!isPrimitiveOrArrayOfPrimitives(fieldType)) {
                    allPrimitive = false;
                }
            }
            catch (DuplicateFieldException e) {
                throw new CompilationException("Re-definition of field '" + 
                    fieldName + "' in type '" + typeName + "'");
            }
            stMember.setAttribute("name", tr.getMembername());
            stMember.setAttribute("type", tr.getMembertype());

            st.setAttribute("members", stMember);
        }
        if (allPrimitive) {
            nonMappedTypes.add(typeName);
        }
        types.addType(type);
    }

    private void addTypeAlias(String typeName, String typeAlias, StringTemplate st) 
            throws CompilationException {
        
	    Type type = Type.Factory.createType(typeName, false);
        type.setBaseType(findType(typeAlias));
        types.addType(type);
        st.setAttribute("type", typeAlias);
    }

    private void processProcedures(Program prog, VariableScope scope) throws CompilationException {
		Map<String, Procedure> names = new HashMap<String, Procedure>();
		// Keep track of declared procedures
	    // Check for redefinitions of existing procedures
		for (int i = 0; i < prog.sizeOfProcedureArray(); i++) {
			Procedure proc = prog.getProcedureArray(i);
			String procName = proc.getName();
			if (functionsMap.find(procName, proc.getInputArray()) != null) {
			    // We have a redefinition error
			    throw new CompilationException("Illegal redefinition of procedure attempted for " + procName);
			}
			Signature ps = new Signature(procName);
			try {
                ps.setInputArgs(proc.getInputArray(), types);
                ps.setOutputArgs(proc.getOutputArray(), types);
            }
            catch (NoSuchTypeException e) {
                throw new CompilationException("Type not found", e);
            }
			functionsMap.addUserProcedure(procName, ps);
			proc.setName(ps.getMangledName());
			names.put(ps.getMangledName(), proc);
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
		Map<String, Object> constants;
		if (newStdLib) {
		    functionsMap = StandardLibrary.NEW.getFunctionSignatures();
		    constants = StandardLibrary.NEW.getConstants();
		    scope.bodyTemplate.setAttribute("stdlibversion", "2");
		}
		else {
            functionsMap = StandardLibrary.LEGACY.getFunctionSignatures();
            constants = StandardLibrary.LEGACY.getConstants();
            scope.bodyTemplate.setAttribute("stdlibversion", "1");
		}

		addConstants(prog, scope, constants);
		for (Program program : importList) {
		    processTypes(program, scope);
		}
		for (Program program : importList) {
            statementsForSymbols(program, scope);
		}
        for (Program program : importList) {
            processProcedures(program, scope);
        }
        for (Program program : importList) {
            statements(program, scope);
        }
		
        generateInternedFields(scope.bodyTemplate);
		generateInternedConstants(scope.bodyTemplate);
		scope.analyzeWriters();
		scope.checkUnused();
		
		scope.bodyTemplate.setAttribute("cleanups", scope.getCleanups());

		return scope.bodyTemplate;
	}
	
    private void addConstants(Program prog, VariableScope scope, Map<String, Object> constants) 
            throws CompilationException {
        
        for (Map.Entry<String, Object> e : constants.entrySet()) {
            org.griphyn.vdl.type.Type type = inferTypeFromValue(e.getValue());
            scope.addVariable(e.getKey(), type, "Variable", AccessType.GLOBAL, 
                VariableOrigin.INTERNAL, prog);
            scope.addWriter(e.getKey(), WriteType.FULL, prog, null);
            // actual value will be defined by importStdlib
        }
    }

    private org.griphyn.vdl.type.Type inferTypeFromValue(Object value) throws CompilationException {
        if (value instanceof Double) {
            return Types.FLOAT;
        }
        if (value instanceof Integer) {
            return Types.INT;
        }
        if (value instanceof String) {
            return Types.STRING;
        }
        throw new CompilationException("Could not infer constant type for value '" + value + "'");
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
			Type type = findType(param.getType().getLocalPart());
			procST.setAttribute("outputs", paramST);
			if (!isPrimitiveOrArrayOfPrimitives(type)) {
                procST.setAttribute("stageouts", paramST);
            }
			addArg(procST, param, type, paramST, true, innerScope);		
		}
		for (int i = 0; i < proc.sizeOfInputArray(); i++) {
			FormalParameter param = proc.getInputArray(i);
			StringTemplate paramST = parameter(param, outerScope);
			Type type = findType(param.getType().getLocalPart());
			procST.setAttribute("inputs", paramST);
			if (!this.isPrimitiveOrArrayOfPrimitives(type)) {
			    procST.setAttribute("stageins", paramST);
			}
			addArg(procST, param, type, paramST, false, outerScope);
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
            Type type, StringTemplate paramST, boolean returnArg, VariableScope scope) 
                throws CompilationException {
        
        if (!param.isNil()) {
            procST.setAttribute("optargs", paramST);
        }
        else {
            procST.setAttribute("arguments", paramST);
        }
        scope.addVariable(param.getName(), type, returnArg ? "Return value" : "Parameter", 
                AccessType.LOCAL, VariableOrigin.INTERNAL, param);
        
        if (returnArg) {
            StringTemplate initWaitCountST = template("setWaitCount");
            initWaitCountST.setAttribute("name", param.getName());
            procST.setAttribute("initWaitCounts", initWaitCountST);
        }
    }
    
    public StringTemplate parameter(FormalParameter param, VariableScope scope) throws CompilationException {
		StringTemplate paramST = new StringTemplate("parameter");
		StringTemplate typeST = new StringTemplate("type");
		Type type = findType(param.getType().getLocalPart());
		paramST.setAttribute("name", param.getName());
		typeST.setAttribute("name", type.toString());
		typeST.setAttribute("namespace", param.getType().getNamespaceURI());
		paramST.setAttribute("type", typeST);
		if(!param.isNil()) {
			paramST.setAttribute("default", expressionToKarajan(param.getAbstractExpression(), scope));
		}
		return paramST;
	}

	public void variableForSymbol(Variable var, VariableScope scope) throws CompilationException {
		Type type = findType(var.getType().getLocalPart());
		scope.addVariable(var.getName(), type, "Variable", 
		    var.getIsGlobal() ? AccessType.GLOBAL : AccessType.LOCAL, VariableOrigin.USER, var);
	}

	public void variable(Variable var, VariableScope scope) throws CompilationException {
		StringTemplate variableST = template("variable");
		variableST.setAttribute("name", var.getName());
		Type type = findType(var.getType().getLocalPart());
		variableST.setAttribute("type", type.toString());
		variableST.setAttribute("field", addInternedField(var.getName(), type));
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
			// add temporary mapping info if not primitive or array of primitive
		   
			if (!isPrimitiveOrArrayOfPrimitives(type)) {
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
        QName expressionQName = getQName(param.getAbstractExpression());
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
            Type paramValueType = datatype(paramValueST);
            scope.addVariable(parameterVariableName, paramValueType, "Variable", VariableOrigin.INTERNAL, param);
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
    
    private boolean isPrimitiveOrArrayOfPrimitives(Type t) {
        return t.isPrimitive() || (t.isArray() && t.itemType().isPrimitive());
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
    			StringTemplate varST = expressionToKarajan(assign.getAbstractExpressionArray(0), scope, true, null);
    			Type lValueType = datatype(varST);
    			
    			String rootvar = abstractExpressionToRootVariable(assign.getAbstractExpressionArray(0));
                scope.addWriter(rootvar, getRootVariableWriteType(assign.getAbstractExpressionArray(0)), assign, assignST);
                
    			StringTemplate valueST = expressionToKarajan(assign.getAbstractExpressionArray(1), scope, false, varST, lValueType);
    			if (valueST == null) {
    			    // the expression will handle the assignment
    			    return;
    			}
    			
    			checkOrInferReturnedType(varST, valueST);
    			
    			assignST.setAttribute("var", varST);
    			assignST.setAttribute("value", valueST);
    			assignST.setAttribute("line", getLine(assign));
    			scope.appendStatement(assignST);
            }
		} catch(CompilationException re) {
			throw new CompilationException("Compile error in assignment at "+assign.getSrc()+": "+re.getMessage(),re);
		}
	}

    private void checkOrInferReturnedType(StringTemplate varST, StringTemplate valueST) throws CompilationException {
        Type lValueType = datatype(varST);
        Type rValueType = datatype(valueST);
        if (lValueType.equals(Types.ANY)) {
            if (rValueType.equals(Types.ANY)) {
                // any <- any
            }
            else {
                // any <- someType, so infer lValueType as rValueType
                setDatatype(varST, rValueType);
            }
        }
        else {
            if (rValueType.equals(Types.ANY)) {
                // someType <- any
                // only expressions that are allowed to return 'any' are procedures
                // for example readData(ret, file). These are special procedures that
                // need to look at the return type at run-time.
            }
            else if (lValueType.equals(Types.FLOAT) && rValueType.equals(Types.INT)) {
                // widening
            }
            else if (!lValueType.equals(rValueType)){
                throw new CompilationException("Cannot assign a value of type " + rValueType +
                    " to a variable of type " + lValueType);
            }
        }
    }

    private boolean isProcedureCall(XmlObject value) {
        if (value instanceof Call) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public void append(Append append, VariableScope scope) throws CompilationException {
        try {
            StringTemplate appendST = template("append");
            StringTemplate array = expressionToKarajan(append.getAbstractExpressionArray(0),scope);
            StringTemplate value = expressionToKarajan(append.getAbstractExpressionArray(1),scope);
            Type arrayType = datatype(array);
            if (!arrayType.keyType().equals(Types.AUTO)) {
                throw new CompilationException("Illegal append to an array of type '" + arrayType + 
                    "'. Array must have 'auto' key type.");
            }
            if (!datatype(value).equals(arrayType.itemType())) {
                throw new CompilationException("Cannot append value of type " + datatype(value) +
                        " to an array of type " + datatype(array));
            }
            appendST.setAttribute("array", array);
            appendST.setAttribute("value", value);
            String rootvar = abstractExpressionToRootVariable(append.getAbstractExpressionArray(0));
            // an append is always a partial write
            scope.addWriter(rootvar, WriteType.PARTIAL, append, appendST);
            scope.appendStatement(appendST);
        } 
        catch(CompilationException re) {
            throw new CompilationException("Compile error in assignment at " + 
                append.getSrc() + ": " + re.getMessage(), re);
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
			|| child instanceof TypesDocument.Types
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
			|| child instanceof TypesDocument.Types
			|| child instanceof FormalParameter
			|| child instanceof Imports) {
			// ignore these - they're expected but we don't need to
			// do anything for them here
		} else {
			throw new CompilationException("Unexpected element in XML. Implementing class "+child.getClass()+", content "+child);
		}
	}
	
	protected ActualParameters getActualParameters(Call call, VariableScope scope) throws CompilationException {
	    ActualParameters actuals = new ActualParameters();
	    Set<String> seen = new HashSet<String>();
	    for (ActualParameter ap : call.getInputArray()) {
	        checkDuplicate(seen, ap.getBind(), "parameter");
	        // TODO do partial matching to infer type
	        StringTemplate argST = actualParameter(ap, scope);
	        actuals.addParameter(ap, argST, datatype(argST));
	    }
	    seen.clear();
	    for (ActualParameter ar : call.getOutputArray()) {
	        checkDuplicate(seen, ar.getBind(), "return");
            StringTemplate retST = actualParameter(ar, scope);
            actuals.addReturn(ar, retST, datatype(retST));
        }
	    return actuals;
	}
	
	private void checkDuplicate(Set<String> seen, String name, String what) throws CompilationException {
	    if (name == null) {
	        return;
	    }
	    if (seen.contains(name)) {
	        throw new CompilationException("Duplicate " + what + ": '" + name + "'");
	    }
	    seen.add(name);
    }

    protected ActualParameters getActualParameters(Function f, VariableScope scope, Type expectedType) 
            throws CompilationException {
        return getActualParameters(f.getAbstractExpressionArray(), scope, expectedType);
    }
	
	protected ActualParameters getActualParameters(XmlObject[] params, VariableScope scope, Type expectedType) throws CompilationException {
        ActualParameters actuals = new ActualParameters();
        for (XmlObject param : params) {
            StringTemplate exprST = expressionToKarajan(param, scope, false, null);
            actuals.addParameter(param, exprST, datatype(exprST));
        }
        if (expectedType != null) {
            actuals.addReturn(null, null, expectedType);
        }
        else {
            actuals.addReturn(null, null, Types.ANY);
        }
        return actuals;
    }


	public StringTemplate call(Call call, VariableScope scope, boolean inhibitOutput) 
	        throws CompilationException {
		try {
		    // first we figure out actual parameter types
		    ActualParameters actualParams = getActualParameters(call, scope);
		    
			// Check is called procedure declared previously
			String procName = call.getProc().getLocalPart();
			Signature proc = functionsMap.find(procName, actualParams, false);
			if (proc == null) {
			    throw noFunctionOrProcedure(procName, actualParams, false);
			}
			
			if (proc.isProcedure()) {
			    return call(call, scope, proc, actualParams, inhibitOutput);
			}
			else {
			    StringTemplate st = functionAsCall(call, scope, proc, actualParams, 
			        actualParams.getReturn(0).getType());
                if (!inhibitOutput) {
                    scope.appendStatement(st);
                }
                return st;
			}
		} 
		catch (CompilationException ce) {
			throw new CompilationException("Compile error in procedure invocation at " + call.getSrc(), ce);
		}
	}

	private StringTemplate call(Call call, VariableScope scope, Signature sig,
            ActualParameters actualParams, boolean inhibitOutput) throws CompilationException {

	    if (sig.isDeprecated()) {
            Warnings.warn(Warnings.Type.DEPRECATION, 
                call, "Procedure " + sig.getName() + " is deprecated");
        }
                    
        StringTemplate callST;
        if (sig.getType() == Signature.InvocationType.USER_DEFINED) {
            callST = template("callUserDefined");
        } 
        else if (sig.getType() == Signature.InvocationType.INTERNAL) {
            callST = template("callInternal");
        } 
        else {
            throw new CompilationException("Unhandled procedure invocation mode " +
                sig.getType());
        }
        callST.setAttribute("func", sig.getMangledName());
        callST.setAttribute("line", getLine(call));
        
        // type checking is done during the search, so at this
        // point we can assume that the actual parameters are legitimate
        // the binding process goes as follows:
        // - go over outputs and re-order to match signature
        // - iterate over arguments and assign to positionals in order until
        //   we either run out of positionals or we hit a keyword actual
        // - all actuals must be keyword from now on (except for varargs)
        // - assign to positionals and optionals as needed
        // - if there are varargs and there are remaining positionals,
        //   assign to varargs
    
        
        // generate code for returns
        // should have been re-ordered during type checking
        for (int i = 0; i < actualParams.returnCount(); i++) {
            StringTemplate compiled = actualParams.getReturn(i).getParamST();
            compiled.removeAttribute("bind");
            callST.setAttribute("outputs", compiled);
            addWriterToScope(scope, call.getOutputArray(i).getAbstractExpression(), call, callST);
        }
                    
        // parameters
        setParameters(callST, "inputs", actualParams);
        
        if (!inhibitOutput) {
            scope.appendStatement(callST);
        }
        if (allVariables(callST.getAttribute("outputs")) && allVariables(callST.getAttribute("inputs"))) {
            callST.setAttribute("serialize", Boolean.TRUE);
        }

        return callST;
    }

    private CompilationException noFunctionOrProcedure(String name, ActualParameters actualParams, boolean fnContext) {
        List<Signature> all = functionsMap.findAll(name);
        if (all.size() == 0) {
            return new CompilationException("No function or procedure '" + name + "' found");
        }
        try {
            // for debugging purposes, if I forget to remove it
            functionsMap.find(name, actualParams, fnContext);
        }
        catch (CompilationException e) {
        }
        if (all.size() == 1) {
            return new CompilationException("Parameter type mismatch: " + 
                actualParams.toString(name) + "\n\tExpected: " + all.get(0));
        }
        StringBuilder sb = new StringBuilder();
        for (Signature sig : all) {
            sb.append("\t");
            sb.append(sig);
            sb.append("\n\n");
        }
        return new CompilationException("No function or procedure found matching signature:\n\t" + 
            actualParams.toString(name) + "\n\nPossible candidates:\n" + sb.toString());
    }

    private void setParameters(StringTemplate st, String attrName, ActualParameters actualParams) {
	    for (int i = 0; i < actualParams.positionalCount(); i++) {
            StringTemplate compiled = actualParams.getPositionalST(i);
            compiled.removeAttribute("bind");
            st.setAttribute(attrName, compiled);
        }
        for (int i = 0; i < actualParams.optionalCount(); i++) {
            StringTemplate compiled = actualParams.getOptionalST(i);
            st.setAttribute(attrName, compiled);
        }
        for (int i = 0; i < actualParams.varargCount(); i++) {
            StringTemplate compiled = actualParams.getVarargST(i);
            st.setAttribute(attrName, compiled);
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

		loopScope.addVariable(iterate.getVar(), Types.INT, "Iteration variable", 
		    VariableOrigin.USER, iterate);

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

			Type inType = datatype(inST);
			if (!inType.isArray()) {
			    throw new CompilationException("You can iterate through an array structure only");
			}
			innerScope.addVariable(foreach.getVar(), inType.itemType(), "Iteration variable", 
			    VariableOrigin.USER, foreach);
			innerScope.addWriter(foreach.getVar(), WriteType.FULL, foreach, foreachST);
			foreachST.setAttribute("indexVar", foreach.getIndexVar());
			if (foreach.getIndexVar() != null) {
			    foreachST.setAttribute("indexVarField", addInternedField(foreach.getIndexVar(), inType.keyType()));
				innerScope.addVariable(foreach.getIndexVar(), inType.keyType(), "Iteration variable", 
				    VariableOrigin.USER, foreach);
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
		if (!datatype(conditionST).equals(Types.BOOLEAN)) {
			throw new CompilationException ("Condition in if statement has to be of boolean type.");
		}

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
		if (!datatype(conditionST).equals(Types.INT) && !datatype(conditionST).equals(Types.FLOAT))
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
        return actualParameter(arg, arg.getBind(), scope, false, null);
    }
	
	public StringTemplate actualParameter(ActualParameter arg, String bind, VariableScope scope) throws CompilationException {
	    return actualParameter(arg, bind, scope, false, null);
	}
	
	public StringTemplate actualParameter(ActualParameter arg, VariableScope scope, boolean lvalue) throws CompilationException {
        return actualParameter(arg, null, scope, lvalue, null);
    }

	public StringTemplate actualParameter(ActualParameter arg, String bind, VariableScope scope, 
	        boolean lvalue, Type expectedType) throws CompilationException {
		StringTemplate argST = template("call_arg");
		StringTemplate expST = expressionToKarajan(arg.getAbstractExpression(), scope, lvalue, null, expectedType);
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
		} 
		else {
		    throw new CompilationException("Unknown binding: " + bind);
		}
	}

	public StringTemplate application(ApplicationBinding app, VariableScope scope) throws CompilationException {
		try {
			StringTemplate appST = new StringTemplate("application");
			appST.setAttribute("exec", app.getExecutable());
			for (int i = 0; i < app.sizeOfAbstractExpressionArray(); i++) {
				XmlObject argument = app.getAbstractExpressionArray(i);
				StringTemplate argumentST = expressionToKarajan(argument, scope, false, null, Types.ANY);
				Type type = datatype(argumentST);
				Type base = type.itemType();
				Type testType;
				// if array then use the array item type for testing
				if (base != null) {
				    testType = base;
				}
				else {
				    testType = type;
				}
				if (testType.isPrimitive()) {
				    appST.setAttribute("arguments", argumentST);
				} 
				else {
					throw new CompilationException("Cannot pass type '" + type + 
					    "' as a parameter to application '" + app.getExecutable() + "'");
				}
			}
			if (app.getStdin() != null) {
				appST.setAttribute("stdin", 
				    expressionToKarajan(app.getStdin().getAbstractExpression(), scope));
			}
			if (app.getStdout() != null) {
				appST.setAttribute("stdout", 
				    expressionToKarajan(app.getStdout().getAbstractExpression(), scope));
			}
			if (app.getStderr() != null) {
				appST.setAttribute("stderr", 
				    expressionToKarajan(app.getStderr().getAbstractExpression(), scope));
			}
			addProfiles(app, scope, appST);
			return appST;
		} 
		catch (CompilationException e) {
			throw new CompilationException(e.getMessage() + 
			    " in application " + app.getExecutable() + " at " + app.getSrc(), e);
		}
	}

	private void addProfiles(ApplicationBinding app, VariableScope scope, StringTemplate appST) 
	        throws CompilationException {
		Profile[] profiles = app.getProfileArray();
		if (profiles.length == 0) { 
			return;
		}
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

	public StringTemplate function(XmlObject func, String name, XmlObject[] arguments, VariableScope scope, Type expectedType) 
	        throws CompilationException {
	    ActualParameters actual = getActualParameters(arguments, scope, expectedType);
	    Signature funcSignature = functionsMap.find(name, actual, true);
        if (funcSignature == null) {
            throw new CompilationException("Unknown function: '" + name + "'");
        }
	    
        return function(func, scope, funcSignature, actual);
	}

	private StringTemplate function(XmlObject func, VariableScope scope, Signature funcSignature,
            ActualParameters actual) {
	    
        StringTemplate funcST = template("function");
        funcST.setAttribute("line", getLine(func));
        
        funcST.setAttribute("name", funcSignature.getMangledName());
        setParameters(funcST, "args", actual);

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
	static final QName STRUCT_EXPR = new QName(SWIFTSCRIPT_NS, "struct");
	
	public StringTemplate expressionToKarajan(XmlObject expression, VariableScope scope) 
            throws CompilationException {
        return expressionToKarajan(expression, scope, false, null, null);
    }
	
	public StringTemplate expressionToKarajan(XmlObject expression, VariableScope scope, StringTemplate lvalue) 
	        throws CompilationException {
	    return expressionToKarajan(expression, scope, false, lvalue, null);
    }
	
	public StringTemplate expressionToKarajan(XmlObject expression, VariableScope scope, 
            boolean isLvalue, StringTemplate lvalue) throws CompilationException {
	    return expressionToKarajan(expression, scope, isLvalue, lvalue, null);
	}

	/** converts an XML intermediate form expression into a
	 *  Karajan expression.
	 */
	public StringTemplate expressionToKarajan(XmlObject expression, VariableScope scope, 
	        boolean isLvalue, StringTemplate lvalue, Type expectedType) throws CompilationException {
	    
		Node expressionDOM = expression.getDomNode();
		QName expressionQName = getQName(expressionDOM);

		if (expressionQName.equals(OR_EXPR))	{
			StringTemplate st = template("binaryop");
			BinaryOperator o = (BinaryOperator)expression;
			StringTemplate leftST = expressionToKarajan(o.getAbstractExpressionArray(0), scope);
			StringTemplate rightST = expressionToKarajan(o.getAbstractExpressionArray(1), scope);
			st.setAttribute("op","||");
			st.setAttribute("left", leftST);
			st.setAttribute("right", rightST);
			if (datatype(leftST).equals(Types.BOOLEAN) && datatype(rightST).equals(Types.BOOLEAN)) {
				st.setAttribute("datatype", "boolean");
			}
			else {
				throw new CompilationException("Or operation can only be applied to parameters of type boolean.");
			}
			return st;
		} 
		else if (expressionQName.equals(AND_EXPR)) {
			StringTemplate st = template("binaryop");
			BinaryOperator o = (BinaryOperator)expression;
			StringTemplate leftST = expressionToKarajan(o.getAbstractExpressionArray(0), scope);
			StringTemplate rightST = expressionToKarajan(o.getAbstractExpressionArray(1), scope);
			st.setAttribute("op","&amp;&amp;");
			st.setAttribute("left", leftST);
			st.setAttribute("right", rightST);
			if (datatype(leftST).equals(Types.BOOLEAN) && datatype(rightST).equals(Types.BOOLEAN)) {
				st.setAttribute("datatype", "boolean");
			}
			else {
				throw new CompilationException("And operation can only be applied to parameters of type boolean.");
			}
			return st;
		} 
		else if (expressionQName.equals(BOOL_EXPR)) {
			XmlBoolean xmlBoolean = (XmlBoolean) expression;
			boolean b = xmlBoolean.getBooleanValue();
			StringTemplate st = template("bConst");
			st.setAttribute("value",""+b);
			st.setAttribute("datatype", "boolean");
			return st;
		} 
		else if (expressionQName.equals(INT_EXPR)) {
			XmlInt xmlInt = (XmlInt) expression;
			int i = xmlInt.getIntValue();
			Integer iobj = Integer.valueOf(i);
			String internedID;
			if (intInternMap.get(iobj) == null) {
				internedID = "swift.int." + i;
				intInternMap.put(iobj, internedID);
			} 
			else {
				internedID = intInternMap.get(iobj);
			}
			StringTemplate st = template("id");
			st.setAttribute("var", internedID);
			st.setAttribute("datatype", "int");
			return st;
		} 
		else if (expressionQName.equals(FLOAT_EXPR)) {
			XmlDouble xmlFloat = (XmlDouble) expression;
			double f = xmlFloat.getDoubleValue();
			Double fobj = new Double(f);
			String internedID;
			if (floatInternMap.get(fobj) == null) {
				internedID = "swift.float." + (internedIDCounter++);
				floatInternMap.put(fobj, internedID);
			} 
			else {
				internedID = floatInternMap.get(fobj);
			}
			StringTemplate st = template("id");
			st.setAttribute("var",internedID);
			st.setAttribute("datatype", "float");
			return st;
		} 
		else if (expressionQName.equals(STRING_EXPR)) {
			XmlString xmlString = (XmlString) expression;
			String s = xmlString.getStringValue();
			String internedID;
			if (stringInternMap.get(s) == null) {
				internedID = "swift.string." + (internedIDCounter++);
				stringInternMap.put(s, internedID);
			} 
			else {
				internedID = stringInternMap.get(s);
			}
			StringTemplate st = template("id");
			st.setAttribute("var", internedID);
			st.setAttribute("datatype", "string");
			return st;
		} 
		else if (expressionQName.equals(COND_EXPR)) {
			StringTemplate st = template("binaryop");
			LabelledBinaryOperator o = (LabelledBinaryOperator) expression;
			StringTemplate leftST = expressionToKarajan(o.getAbstractExpressionArray(0), scope);
			StringTemplate rightST = expressionToKarajan(o.getAbstractExpressionArray(1), scope);
			st.setAttribute("op", o.getOp());
			st.setAttribute("left", leftST);
			st.setAttribute("right", rightST);

			checkTypesInCondExpr(o.getOp(), datatype(leftST), datatype(rightST), st);
			return st;
		} 
		else if (expressionQName.equals(ARITH_EXPR)) {
			LabelledBinaryOperator o = (LabelledBinaryOperator) expression;
			StringTemplate st = template("binaryop");
			st.setAttribute("op", o.getOp());
			StringTemplate leftST = expressionToKarajan(o.getAbstractExpressionArray(0), scope);
			StringTemplate rightST = expressionToKarajan(o.getAbstractExpressionArray(1), scope);
			st.setAttribute("left", leftST);
			st.setAttribute("right", rightST);

			checkTypesInArithmExpr(o.getOp(), datatype(leftST), datatype(rightST), st);
			return st;
		} 
		else if (expressionQName.equals(UNARY_NEGATION_EXPR)) {
			UnlabelledUnaryOperator e = (UnlabelledUnaryOperator) expression;
			StringTemplate st = template("unaryNegation");
			StringTemplate expST = expressionToKarajan(e.getAbstractExpression(), scope);
			st.setAttribute("exp", expST);
			if (!(datatype(expST).equals(Types.FLOAT)) && !(datatype(expST).equals(Types.INT))) {
				throw new CompilationException("Negation operation can only be applied to parameter of numeric types.");
			}
			st.setAttribute("datatype", datatype(expST));
			return st;
		} 
		else if (expressionQName.equals(NOT_EXPR)) {
			// TODO not can probably merge with 'unary'
			UnlabelledUnaryOperator e = (UnlabelledUnaryOperator)expression;
			StringTemplate st = template("not");
			StringTemplate expST = expressionToKarajan(e.getAbstractExpression(), scope);
			st.setAttribute("exp", expST);
			if (datatype(expST).equals(Types.BOOLEAN)) {
				st.setAttribute("datatype", "boolean");
			}
			else {
				throw new CompilationException("Not operation can only be applied to parameter of type boolean.");
			}
			return st;
		} 
		else if (expressionQName.equals(VARIABLE_REFERENCE_EXPR)) {
			XmlString xmlString = (XmlString) expression;
			String s = xmlString.getStringValue();
			if (!scope.isVariableDefined(s)) {
				throw new CompilationException("Variable " + s + " was not declared in this scope.");
			}
									
			if (!isLvalue) {
			    scope.addReader(s, false, expression);
			}
			StringTemplate st = template("id");
			st.setAttribute("var", s);

			Type actualType = scope.getVariableType(s);
			st.setAttribute("datatype", actualType.toString());
			return st;
		} 
		else if (expressionQName.equals(ARRAY_SUBSCRIPT_EXPR)) {
			BinaryOperator op = (BinaryOperator) expression;
			StringTemplate arrayST = expressionToKarajan(op.getAbstractExpressionArray(1), scope);
			StringTemplate parentST = expressionToKarajan(op.getAbstractExpressionArray(0), scope, true, null);

			Type indexType = datatype(arrayST);
			Type declaredArrayType = datatype(parentST);
			Type declaredIndexType = declaredArrayType.keyType();
			// the index type must match the declared index type,
			// unless the declared index type is *
			
			// and really, at this point type checking should be delegated to the type system
			// instead of the ad-hoc string comparisons
			if (indexType.equals(Types.STRING)) {
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
			
			scope.addReader(getRootVar(parentST), true, expression);
			
			StringTemplate newst = template("extractarrayelement");
			newst.setAttribute("arraychild", arrayST);
			newst.setAttribute("parent", parentST);
			newst.setAttribute("datatype", declaredArrayType.itemType().toString());

			return newst;
		}
		else if (expressionQName.equals(STRUCTURE_MEMBER_EXPR)) {
			StructureMember sm = (StructureMember) expression;
			StringTemplate parentST = expressionToKarajan(sm.getAbstractExpression(), scope, true, null);

			Type parentType = datatype(parentST);
			Type arrayType = parentType;
			boolean arrayMode = false;

			// if the parent is an array, then check against
			// the base type of the array
			if (parentType.isArray()) {
			    parentType = parentType.itemType();
			    arrayMode = true;
			}

			if (!parentType.isComposite() || parentType.isArray()) {
			    // this happens when trying to access a field of a built-in type
			    // which cannot currently be a structure
			    throw new CompilationException("Type " + parentType + " is not a structure");
			}

			Type actualType;
			try {
                actualType = parentType.getField(sm.getMemberName()).getType();
            }
            catch (NoSuchFieldException e) {
                throw new CompilationException("No member " + sm.getMemberName() + " in type " + parentType);
            }
			StringTemplate newst;
			if (arrayMode) {
			    /*
			     *  this is when we have a situation like this:
			     *    type s {int a, b;};
			     *    s[] foo;
			     *    ...
			     *    foo.a
			     *  This basically means foo[*].a
			     *  With parametrized types:
			     *  (S[K]).(F) -> (F[K]) 
			     */
			    
			    actualType = actualType.arrayType(arrayType.keyType());
				newst = template("slicearray");
			}
			else {
				newst = template("extractstructelement");
			}
			
			scope.addReader(getRootVar(parentST), true, expression);
			newst.setAttribute("parent", parentST);
			newst.setAttribute("memberchild", sm.getMemberName());
            newst.setAttribute("datatype", actualType.toString());
            return newst;
			// TODO the template layout for this and ARRAY_SUBSCRIPT are
			// both a bit convoluted for historical reasons.
			// should be straightforward to tidy up.
		}
		else if (expressionQName.equals(ARRAY_EXPR)) {
			Array array = (Array)expression;
			StringTemplate st = template("array");
			Type elemType = null;
			for (int i = 0; i < array.sizeOfAbstractExpressionArray(); i++) {
				XmlObject expr = array.getAbstractExpressionArray(i);
				StringTemplate elemST = expressionToKarajan(expr, scope, false, null, 
				    expectedType == null ? null : expectedType.itemType());
				Type newType = datatype(elemST);
				if (i == 0) {
					elemType = newType;
				}
				else if (!elemType.equals(newType)) {
					throw new CompilationException("Heterogeneous arrays are not supported");
				}
				st.setAttribute("elements", elemST);
			}
			if (expectedType != null) {
    			if (!expectedType.isArray()) {
    			    throw new CompilationException("Type error. Array used where non-array " +
    			    		"type is expected (" + expectedType + ")");
    			}
    			if (!expectedType.keyType().equals(Types.INT)) {
    			    throw new CompilationException("Type error. Array expressions have " +
    			    		"integer key types, but expected type has non-integer keys (" + 
    			    		expectedType + ")");
    			}
    			if (elemType == null) {
    			    elemType = expectedType.itemType();
    			}
			}
			else {
			    if (elemType == null) {
			        throw new CompilationException("Cannot infer type of empty array");
			    }
			}
		    st.setAttribute("datatype", elemType + "[int]");
		    st.setAttribute("field", addInternedField("$arrayexpr", elemType.arrayType()));
			return st;
		}
		else if (expressionQName.equals(RANGE_EXPR)) {
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

			Type fromType = datatype(fromST);
			Type toType = datatype(toST);
			if (!fromType.equals(toType)) {
			    throw new CompilationException("To and from range values must have the same type");
            }
			if (stepST != null && !datatype(stepST).equals(fromType)) {
			    throw new CompilationException("Step (" + datatype(stepST) + 
			            ") must be of the same type as from and to (" + toType + ")");
			}
			if (stepST == null && (!fromType.equals(Types.INT) || !toType.equals(Types.INT))) {
				throw new CompilationException("Step in range specification can be omitted only when from and to types are int");
			}
			else if (fromType.equals(Types.INT) && toType.equals(Types.INT)) {
				st.setAttribute("datatype", "int[int]");
			}
			else if (fromType.equals(Types.FLOAT) && toType.equals(Types.FLOAT) &&
					datatype(stepST).equals(Types.FLOAT)) {
				st.setAttribute("datatype", "float[int]");
			}
			else {
				throw new CompilationException("Range can only be specified with numeric types");
			}
			return st;
		} 
		else if (expressionQName.equals(FUNCTION_EXPR)) {
			Function f = (Function) expression;
			return functionExpr(f, scope, expectedType);
		}
		else if (expressionQName.equals(CALL_EXPR)) {
		    Call c = (Call) expression;
		    String name = c.getProc().getLocalPart();
		    ActualParameters actual = getActualParameters(c, scope);
		    if (expectedType == null) {
		        actual.addReturn(null, null, Types.ANY);
		    }
		    else {
		        actual.addReturn(null, null, expectedType);
		    }
		    if (functionsMap.isDefined(name)) {
		        Signature sig = functionsMap.find(name, actual, true);
		        if (sig != null) {
    		        if (sig.isProcedure()) {
    		            return callExpr(c, scope, expectedType, sig, actual, lvalue);
    		        }
    		        else {
    		            return functionExpr(c, scope, sig, actual);
    		        }
		        }
		    }
            throw noFunctionOrProcedure(name, actual, true);
		}
		else if (expressionQName.equals(STRUCT_EXPR)) {
		    Struct s = (Struct) expression;
		    // distringuish between a struct or sparse array
		    // expr by looking at the keys. If they are identifiers,
		    // then struct, if expressions
		    // if there are no kv pairs, then this is a sparse array expression
		    boolean sparseArray;
		    if (s.sizeOfFieldArray() == 0) {
		        sparseArray = true;
		    }
		    else {
    		    StructField f = s.getFieldArray(0);
    		    QName keyType = getQName(f.getAbstractExpressionArray(0).getDomNode());
    		    if (keyType.equals(VARIABLE_REFERENCE_EXPR)) {
    		        sparseArray = false;
    		    }
    		    else {
    		        sparseArray = true;
    		    }
		    }
		    if (isLvalue) {
		        if (sparseArray) {
		            throw new CompilationException("Array initializer cannot be an lvalue");
		        }
		        else {
		            throw new CompilationException("Structure initializer cannot be an lvalue");
		        }
		    }
		    if (sparseArray) {
		        return sparseArrayInitializer(s, scope, lvalue, expectedType);
		    }
		    else {
		        return structInitializer(s, scope, lvalue, expectedType);
		    }
		}
		else {
			throw new CompilationException("unknown expression implemented by class " + 
			    expression.getClass() + " with node name " + expressionQName + 
			    " and with content " + expression);
		}
		// perhaps one big throw catch block surrounding body of this method
		// which shows Compiler Exception and line number of error
	}

    private QName getQName(Node node) {
        String namespaceURI = node.getNamespaceURI();
        String localName = node.getLocalName();
        return new QName(namespaceURI, localName);
    }
    
    private QName getQName(XmlObject obj) {
        return getQName(obj.getDomNode());
    }

    private StringTemplate structInitializer(Struct s, VariableScope scope, StringTemplate lvalue, Type expectedType) 
            throws CompilationException {
        StringTemplate st = template("newStruct");
        if (expectedType == null) {
            throw new CompilationException("Cannot infer destination type in structure initializer");
        }
        if (lvalue != null) {
            st.setAttribute("var", lvalue);
        }
        else {
            st.setAttribute("field", addInternedField("$structexpr", expectedType));
        }
        if (!expectedType.isComposite() || expectedType.isArray()) {
            throw new CompilationException("Cannot assign a structure to a non-structure");
        }
        Set<String> seen = new HashSet<String>();
        for (int i = 0; i < s.sizeOfFieldArray(); i++) {
            st.setAttribute("fields", structField(s.getFieldArray(i), scope, expectedType, seen));
        }
        if (expectedType.getFields().size() != seen.size()) {
            throw new CompilationException("Missing fields in structure initializer: " + getMissingFields(seen, expectedType));
        }
        st.setAttribute("datatype", expectedType.toString());
        if (lvalue != null) {
            scope.appendStatement(st);
            return null;
        }
        else {
            return st;
        }
    }

    private StringTemplate structField(StructField field, VariableScope scope, Type expectedType, Set<String> seen) 
            throws CompilationException {
        XmlObject xkey = field.getAbstractExpressionArray(0);
        Node key = xkey.getDomNode();
        if (!getQName(key).equals(VARIABLE_REFERENCE_EXPR)) {
            // TODO better error message
            throw new CompilationException("Invalid field name " + key.getLocalName());
        }
        XmlString var = (XmlString) xkey;
        String name = var.getStringValue();
        if (seen.contains(name)) {
            throw new CompilationException("Duplicate field: '" + name + "'");
        }
        seen.add(name);
        
        StringTemplate st = template("makeField");
        st.setAttribute("key", "\"" + name + "\"");
        try {
            st.setAttribute("value", expressionToKarajan(field.getAbstractExpressionArray(1), 
                scope, false, null, expectedType.getField(name).getType()));
        }
        catch (NoSuchFieldException e) {
            throw new CompilationException("Invalid field '" + name + "' for type '" + expectedType + "'");
        }
        return st;
    }

    private String getMissingFields(Set<String> seen, Type t) {
        List<String> missing = new ArrayList<String>();
        for (String f : t.getFieldNames()) {
            if (!seen.contains(f)) {
                missing.add(f);
            }
        }
        return missing.toString();
    }

    private StringTemplate sparseArrayInitializer(Struct s, VariableScope scope, StringTemplate lvalue, Type expectedType) 
            throws CompilationException {
        StringTemplate st = template("newSparseArray");
        if (lvalue != null) {
            st.setAttribute("var", lvalue);
        }
        else {
            if (expectedType == null) {
                throw new CompilationException("Could not infer type in sparse array initializer");
            }
            st.setAttribute("field", addInternedField("$arrayexpr", expectedType));
        }
        if (!expectedType.isArray()) {
            throw new CompilationException("Cannot assign an array to a non-array");
        }
        for (int i = 0; i < s.sizeOfFieldArray(); i++) {
            st.setAttribute("fields", sparseArrayField(s.getFieldArray(i), scope, expectedType));
        }
        st.setAttribute("datatype", expectedType.toString());
        if (lvalue != null) {
            scope.appendStatement(st);
            return null;
        }
        else {
            return st;
        }
    }
    
    private StringTemplate sparseArrayField(StructField field, VariableScope scope, Type expectedType) 
            throws CompilationException {
        Type keyType = expectedType.keyType();
        Type itemType = expectedType.itemType();
        
        StringTemplate st = template("makeField");
        st.setAttribute("key", expressionToKarajan(field.getAbstractExpressionArray(0), 
            scope, false, null, keyType));
        st.setAttribute("value", expressionToKarajan(field.getAbstractExpressionArray(1), 
                scope, false, null, itemType));
        return st;
    }

    private String getRootVar(StringTemplate st) throws CompilationException {
        StringTemplate parent = (StringTemplate) st.getAttribute("parent");
        if (parent == null || st == parent) {
            String name = (String) st.getAttribute("var");
            if (name == null) {
                throw new CompilationException("Could not get variable name " + st);
            }
            return name;
        }
        else {
            return getRootVar(parent);
        }
    }

    private StringTemplate callExpr(Call c, VariableScope scope, Type expectedType, Signature sig, 
            ActualParameters actual, StringTemplate lvalue) 
            throws CompilationException {
        c.addNewOutput();
        VariableScope subscope = new VariableScope(this, scope, c);
        VariableReferenceDocument ref = VariableReferenceDocument.Factory.newInstance();
        ref.setVariableReference("swift.callintermediate");
        c.getOutputArray(0).set(ref);

        if (sig.getReturns().size() != 1) {
            throw new CompilationException("Procedure '" + sig.getName() + "' must have exactly one " +
                    "return value to be used in an expression.");
        }
        
        StringTemplate call = template("callexpr");
        if (lvalue != null) {
            call.setAttribute("outputs", lvalue.getAttribute("var"));
        }

        Type type = sig.getReturnType();
        
        if (type.equals(Types.ANY)) {
            if (expectedType != null) {
                type = expectedType;
            }
            else {
                throw new CompilationException("Cannot infer return type of procedure call");
            }
        }
        
        if (!isPrimitiveOrArrayOfPrimitives(type)) {
        	call.setAttribute("mapping", true);
        }
        
        subscope.addInternalVariable("swift.callintermediate", type, null);
        
        actual.getReturn(0).setParamST(actualParameter(c.getOutputArray(0), subscope));
        call.setAttribute("datatype", type.toString());
        call.setAttribute("field", addInternedField("swift.callintermediate", type));
        call.setAttribute("call", call(c, subscope, sig, actual, true));
        if (!isPrimitiveOrArrayOfPrimitives(type)) {
            call.setAttribute("prefix", UUIDGenerator.getInstance().generateRandomBasedUUID().toString());
        }
        return call;
    }
    
    private StringTemplate functionExpr(Call c, VariableScope scope, Signature sig, ActualParameters actual) 
            throws CompilationException {
        StringTemplate st = function(c, scope, sig, actual);
        /* Set function output type */
        /* Functions have only one output parameter */
        st.setAttribute("datatype", sig.getReturnType().toString());
        if (sig.isDeprecated()) {
            Warnings.warn(Warnings.Type.DEPRECATION, 
                c, "Function " + sig.getName() + " is deprecated");
        }
        
        return st;
    }

    private StringTemplate functionExpr(Function f, VariableScope scope, Type expectedType) throws CompilationException {
        String name = f.getName();
        if (name.equals("")) {
            name = "filename";
        }
        else {
            // the @var shortcut for filename(var) is not deprecated
            Warnings.warn(Warnings.Type.DEPRECATION, 
                "The @ syntax for function invocation is deprecated");
        }
        
        ActualParameters actual = getActualParameters(f, scope, expectedType);
        Signature funcSignature = functionsMap.find(name, actual, true);
        if (funcSignature == null) {
            throw noFunctionOrProcedure(name, actual, true);
        }
        StringTemplate st = function(f, scope, funcSignature, actual);
        /* Set function output type */    
        /* Functions have only one output parameter */
        st.setAttribute("datatype", funcSignature.getReturnType().toString());
        
        if (funcSignature.isDeprecated()) {
            Warnings.warn(Warnings.Type.DEPRECATION, 
                f, "Function " + name + " is deprecated");
        }
    
        return st;
    }
    
    /**
     * Translates a call(output, params) into a output = function(params) form.
     * @param actualParams 
     * @param proc 
     */
    private StringTemplate functionAsCall(Call call, VariableScope scope, Signature proc, 
            ActualParameters actualParams, Type expectedType) throws CompilationException {
        
        String name = call.getProc().getLocalPart();
        if (call.getOutputArray().length == 0) {
            throw new CompilationException("Call to a function that does not return a value");
        }
        if (call.getOutputArray().length > 1) {
            throw new CompilationException("Cannot assign multiple values with a function invocation");
        }
        
        StringTemplate value = function(call, name, getCallParams(call), scope, expectedType);
        value.setAttribute("datatype", proc.getReturnType().toString());
        StringTemplate assign = assignFromCallReturn(call, value, scope);
        
        if (proc.isDeprecated()) {
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
        StringTemplate varST = expressionToKarajan(var, scope, true, null);
        if (!datatype(varST).equals(datatype(valueST))) {
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


    // TODO: this ad-hoc type checking bothers me
    void checkTypesInCondExpr(String op, Type left, Type right, StringTemplate st)
			throws CompilationException {
        if (left.equals(Types.FLOAT) && right.equals(Types.INT)) {
            // widening
            right = Types.FLOAT;
        }
        if (right.equals(Types.FLOAT) && left.equals(Types.INT)) {
            // widening
            left = Types.FLOAT;
        }
		if (left.equals(right)) {
			st.setAttribute("datatype", "boolean");
		}
		else {
			throw new CompilationException("Conditional operator can only be applied to parameters of same type.");
		}

		if ((op.equals("==") || op.equals("!=")) && !left.equals(Types.INT) && !left.equals(Types.FLOAT)
				                                 && !left.equals(Types.STRING) && !left.equals(Types.BOOLEAN)) {
			throw new CompilationException("Conditional operator " + op +
					" can only be applied to parameters of type int, float, string and boolean.");
		}

		if ((op.equals("<=") || op.equals(">=") || op.equals(">") || op.equals("<"))
				&& !left.equals(Types.INT) && !left.equals(Types.FLOAT) && !left.equals(Types.STRING)) {
			throw new CompilationException("Conditional operator " + op +
			        " can only be applied to parameters of type int, float and string.");
		}
	}

	void checkTypesInArithmExpr(String op, Type left, Type right, StringTemplate st)
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
	            if (left.equals(Types.STRING)) {
	                if (!right.equals(Types.STRING) && !right.equals(Types.INT) && !right.equals(Types.FLOAT)) {
	                    throw new CompilationException("Operator '+' cannot be applied to 'string'  and '" + right + "'");
	                }
	                st.setAttribute("datatype", "string");
	            }
	            else if (right.equals(Types.STRING)) {
	                if (!left.equals(Types.STRING) && !left.equals(Types.INT) && !left.equals(Types.FLOAT)) {
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

	private void checkTypesInType1ArithmExpr(String op, Type left, Type right, StringTemplate st) 
	        throws CompilationException {
	    /* 
         * int, int -> int
         * float, float -> float
         * int, float -> float
         * float, int -> float
         */
	    if (left.equals(Types.INT)) {
	        if (right.equals(Types.INT)) {
	            st.setAttribute("datatype", "int");
	            return;
	        }
	        else if (right.equals(Types.FLOAT)) {
	            st.setAttribute("datatype", "float");
	            return;
	        }
	    }
	    else if (left.equals(Types.FLOAT)) {
	        if (right.equals(Types.INT) || right.equals(Types.FLOAT)) {
	            st.setAttribute("datatype", "float");
	            return;
            }
	    }
	    throw new CompilationException("Operator '" + op + "' cannot be applied to '" + left + "' and '" + right + "'");
    }

    private void checkTypesInType2ArithmExpr(String op, Type left, Type right, StringTemplate st) 
            throws CompilationException {
        if (!left.equals(Types.INT) || !right.equals(Types.INT)) {
            throw new CompilationException("Operator '" + op + "' can only be applied to 'int' and 'int'");
        }
        st.setAttribute("datatype", "int");
    }

    public String abstractExpressionToRootVariable(XmlObject expression) throws CompilationException {
		QName expressionQName = getQName(expression);
		if (expressionQName.equals(VARIABLE_REFERENCE_EXPR)) {
			XmlString xmlString = (XmlString) expression;
			String s = xmlString.getStringValue();
			return s;
		} 
		else if (expressionQName.equals(ARRAY_SUBSCRIPT_EXPR)) {
			BinaryOperator op = (BinaryOperator) expression;
			return abstractExpressionToRootVariable(op.getAbstractExpressionArray(0));
		} 
		else if (expressionQName.equals(STRUCTURE_MEMBER_EXPR)) {
			StructureMember sm = (StructureMember) expression;
			return abstractExpressionToRootVariable(sm.getAbstractExpression());
		} 
		else {
			throw new CompilationException("Could not find root for abstract expression.");
		}
	}

	public WriteType getRootVariableWriteType(XmlObject expression) {
		QName expressionQName = getQName(expression);
		if (expressionQName.equals(VARIABLE_REFERENCE_EXPR)) {
			return WriteType.FULL;
		} 
		else if (expressionQName.equals(ARRAY_SUBSCRIPT_EXPR)) {
			return WriteType.PARTIAL;
		} 
		else if (expressionQName.equals(STRUCTURE_MEMBER_EXPR)) {
			return WriteType.PARTIAL;
		} 
		else {
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
	private String addInternedField(String name, Type type) {
	    String v = internedFieldName(name, type);
	    usedFields.put(new InternedField(name, type), v);
	    return v;
    }
	
	public static String internedFieldName(String name, Type type) {
	    return "swift.field." + name + "." + type.toString().replace("[", ".array.").replace("]", "");
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
		
	public static String lineNumber(String src) {
	    return src.substring(src.indexOf(' ') + 1);
	}

	private Type datatype(StringTemplate st) throws CompilationException {
	    return findType(st.getAttribute("datatype").toString());
	}
	
	protected void setDatatype(StringTemplate st, org.griphyn.vdl.type.Type type) {
	    st.removeAttribute("datatype");
	    st.setAttribute("datatype", type.toString());
	}
	
	private Type findType(String typeName) throws CompilationException {
	    try {
            return types.getType(typeName);
        }
        catch (NoSuchTypeException e) {
            throw new CompilationException("Invalid type '" + typeName + "'", e);
        }
	}
	
	private void assertTrue(boolean condition, String message) throws CompilationException {
	    if (!condition) {
	        throw new CompilationException(message);
	    }
	}
}
