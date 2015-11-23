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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.log4j.Logger;
import org.globus.swift.parsetree.*;
import org.griphyn.vdl.compiler.intermediate.*;
import org.griphyn.vdl.engine.VariableScope.AccessType;
import org.griphyn.vdl.engine.VariableScope.EnclosureType;
import org.griphyn.vdl.engine.VariableScope.VariableOrigin;
import org.griphyn.vdl.engine.VariableScope.WriteType;
import org.griphyn.vdl.karajan.CompilationException;
import org.griphyn.vdl.karajan.Loader;
import org.griphyn.vdl.mapping.MapperFactory;
import org.griphyn.vdl.toolkit.SwiftParser;
import org.griphyn.vdl.type.DuplicateFieldException;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;
import org.safehaus.uuid.UUIDGenerator;

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
	
	List<IVariableDeclaration> variables = new ArrayList<IVariableDeclaration>();

	public static final String TEMPLATE_FILE_NAME = "Karajan.stg";
	public static final String TEMPLATE_FILE_NAME_NO_PROVENANCE = "Karajan-no-provenance.stg";

	LinkedList<Program> importList = new LinkedList<Program>();
	Set<String> importedNames = new HashSet<String>();

	int internedIDCounter = 17000;

	/** an arbitrary statement identifier. Start at some high number to
	    aid visual distinction in logs, but the actual value doesn't
		matter. */

	private boolean newStdLib;

	public static void compile(Program prog, PrintStream out, boolean provenanceEnabled) throws CompilationException {
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

		OutputContext oc = new OutputContext(templates);
		Karajan karajan = new Karajan();
		IProgram p = karajan.program(prog);
		try {
            p.writeTo(oc, out);
        }
        catch (IOException e) {
            throw new CompilationException("Failed to write output", e);
        }
	}

	public Karajan() {
		// used by some templates
		addInternedField("temp", Types.INT);
		addInternedField("const", Types.INT);
		addInternedField("const", Types.FLOAT);
		addInternedField("const", Types.STRING);
		addInternedField("const", Types.BOOLEAN);
	}

    private void processImports(Program prog) throws CompilationException {
		List<Import> imports = prog.getImports();
		if (imports != null) {
			logger.debug("Processing SwiftScript imports");
            // process imports in reverse order
			for (int i = imports.size() - 1 ;  i >=0 ; i--) {
				Import imp = imports.get(i);
				String moduleToImport = imp.getTarget();
				logger.debug("Importing module " + moduleToImport);
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
					logger.debug("Skipping repeated import of " + moduleToImport);
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
            Program importedProgram = SwiftParser.parse(new FileInputStream(swiftfilename));
            importList.addFirst(importedProgram);
            importedNames.add(moduleToImport);
            logger.debug("Added " + moduleToImport + " to import list. Processing imports from that.");
            processImports(importedProgram);
        } 
        catch(Exception e) {
            throw new CompilationException("When processing import "+moduleToImport, e);
        }
    }

    private void processTypes(Program prog, IProgram iProgram) throws CompilationException {
	    List<TypeDeclaration> progTypes = prog.getTypes();
		if (progTypes != null) {
			for (TypeDeclaration theType : progTypes) {
				String typeName = theType.getName();
				String typeAlias = theType.getTypeAlias();

				logger.debug("Processing type " + typeName);

				ITypeDefinition iType = new ITypeDefinition();
				iType.setName(typeName);
				
				if (typeAlias != null && !typeAlias.equals("") && !typeAlias.equals("string")) {
				    addTypeAlias(typeName, typeAlias, iType);
				}
				else {
				    addStructType(typeName, theType, iType);
				}
				
				iProgram.addType(iType);
			}
		}
		try {
            types.resolveTypes();
        }
        catch (NoSuchTypeException e) {
            throw new CompilationException("Cannot resolve types", e);
        }
	}

	private void addStructType(String typeName, TypeDeclaration theType, ITypeDefinition iType) throws CompilationException {
	    
	    Type type = Type.Factory.createType(typeName, false);
	    List<TypeMemberDeclaration> ts = theType.getMembers();
        boolean allPrimitive = ts.size() > 0;
        for (TypeMemberDeclaration tr : ts) {
            IStructMember iStructMember = new IStructMember();
            String fieldName = tr.getName();
            org.griphyn.vdl.type.Type fieldType;
            try {
                fieldType = findType(tr.getType());
                type.addField(fieldName, fieldType);
                if (!isPrimitiveOrArrayOfPrimitives(fieldType)) {
                    allPrimitive = false;
                }
            }
            catch (DuplicateFieldException e) {
                throw new CompilationException("Re-definition of field '" + 
                    fieldName + "' in type '" + typeName + "'");
            }
            iStructMember.setName(tr.getName());
            iStructMember.setType(tr.getType());
            
            iType.addStructMember(iStructMember);
        }
        if (allPrimitive) {
            nonMappedTypes.add(typeName);
        }
        types.addType(type);
    }

    private void addTypeAlias(String typeName, String typeAlias, ITypeDefinition iType) throws CompilationException {
	    Type type = Type.Factory.createType(typeName, false);
        type.setBaseType(findType(typeAlias));
        types.addType(type);
        iType.setTypeAlias(typeAlias);
    }

    private void processProcedures(Program prog, IProgram iProgram) throws CompilationException {
		final Map<String, FunctionDeclaration> procs = new HashMap<String, FunctionDeclaration>();
		final Map<String, Signature> sigs = new HashMap<String, Signature>();
		// Keep track of declared procedures
	    // Check for redefinitions of existing procedures
		for (FunctionDeclaration proc : prog.getFunctionDeclarations()) {
			String procName = proc.getName();
			Signature ps = new Signature(procName);
			try {
                ps.setInputArgs(proc.getParameters(), types);
                ps.setOutputArgs(proc.getReturns(), types);
            }
            catch (NoSuchTypeException e) {
                throw new CompilationException("Type not found", e);
            }
			functionsMap.addUserProcedure(procName, ps);
			proc.setName(ps.getMangledName());
			procs.put(ps.getMangledName(), proc);
			sigs.put(ps.getMangledName(), ps);
		}
		
		TopologicalSort<String> procSort = new TopologicalSort<String>(procs.keySet(), true, new TopologicalSort.VisitorHelper<String>() {
            @Override
            public Collection<String> getDependencies(String procName) {
                return getCalls(procs.get(procName).getBody());
            }		    
        });
		List<String> sorted = procSort.sort();
        
        for (String procName : sorted) {
			procedure(procs.get(procName), sigs.get(procName), iProgram);
		}
	}
    
    private List<String> getCalls(Node o) {
        List<String> l = new ArrayList<String>();
        getCalls(o, l);
        return l;
    }

    private List<String> getCalls(Node o, List<String> l) {
        for (Object s : o.getSubNodes()) {
            if (s == null) {
                continue;
            }
            if (s instanceof Call) {
            	l.add(((Call) s).getName());
            }
            getCalls((Node) s, l);
        }
        return l;
    }

    public IProgram program(Program prog) throws CompilationException {
		VariableScope scope = new VariableScope(this, null, prog);
		IProgram iProgram = new IProgram(scope);
		
		iProgram.setBuildVersion(Loader.buildVersion);

		importList.addFirst(prog);
		processImports(prog);
		Map<String, Object> constants;
		if (newStdLib) {
		    functionsMap = StandardLibrary.NEW.getFunctionSignatures();
		    constants = StandardLibrary.NEW.getConstants();
		    iProgram.setLibraryVersion("2");
		}
		else {
            functionsMap = StandardLibrary.LEGACY.getFunctionSignatures();
            constants = StandardLibrary.LEGACY.getConstants();
            iProgram.setLibraryVersion("1");
		}

		addConstants(prog, scope, constants);
		for (Program program : importList) {
		    processTypes(program, iProgram);
		}
		for (Program program : importList) {
            statementsForSymbols(program.getBody(), iProgram);
		}
        for (Program program : importList) {
            processProcedures(program, iProgram);
        }
        for (Program program : importList) {
            statements(program.getBody(), iProgram);
        }
		
        generateInternedFields(iProgram);
		generateInternedConstants(iProgram);
		scope.analyzeWriters();
		scope.checkUnused();
		
		return iProgram;
	}
	
    private void addConstants(Program prog, VariableScope scope, Map<String, Object> constants) 
            throws CompilationException {
        
        for (Map.Entry<String, Object> e : constants.entrySet()) {
            org.griphyn.vdl.type.Type type = inferTypeFromValue(e.getValue());
            scope.addVariable(e.getKey(), type, "Variable", AccessType.GLOBAL, 
                VariableOrigin.INTERNAL, prog, null);
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

    public void procedure(FunctionDeclaration proc, Signature sig, IProgram container) throws CompilationException {
		VariableScope outerScope = new VariableScope(this, container.getScope(), EnclosureType.PROCEDURE, proc);
		VariableScope innerScope = new VariableScope(this, outerScope, EnclosureType.NONE, proc);
		IProcedureDeclaration iProcedure;
		if (proc instanceof AppDeclaration) {
            iProcedure = new IProcedureDeclaration(innerScope);
        }
        else {
            VariableScope compoundScope = new VariableScope(this, innerScope, proc);
            iProcedure = new IProcedureDeclaration(compoundScope);
            innerScope.setOwner(iProcedure);
        }
		container.addProcedureDefinition(iProcedure);
		outerScope.setOwner(iProcedure);
		
		iProcedure.setLine(proc.getLine());
		iProcedure.setName(proc.getName());
				
		for (FormalParameter param : proc.getReturns()) {
			IFormalParameter iRet = parameter(param, iProcedure, container, true);
			Type type = findType(param.getType());
			iProcedure.addReturn(iRet);
			innerScope.addVariable(param.getName(), type, "Return value", 
			    AccessType.LOCAL, VariableOrigin.INTERNAL, param, iRet);
		}
		for (FormalParameter param : proc.getParameters()) {
		    IFormalParameter iParam = parameter(param, iProcedure, container, false);
			Type type = findType(param.getType());
			iProcedure.addParameter(iParam);
			outerScope.addVariable(param.getName(), type, "Parameter", 
                AccessType.LOCAL, VariableOrigin.INTERNAL, param, iParam);
			/*
			 * Mark parameters as initialized
			 */
			outerScope.markInitialized(param.getName(), proc);
		}
				
		if (proc instanceof AppDeclaration) {
			binding((AppDeclaration) proc, iProcedure);
		}
		else {
			statementsForSymbols(proc.getBody(), iProcedure);
			statements(proc.getBody(), iProcedure);
		}
	}
    
    public IFormalParameter parameter(FormalParameter param, IProcedureDeclaration iProc, IStatementContainer container, 
            boolean isReturn) throws CompilationException {
        
        IFormalParameter iParam = new IFormalParameter(iProc);
        iParam.setIsReturn(isReturn);
        ITypeReference iTypeRef = new ITypeReference();
    		
		Type type = findType(param.getType());
		iParam.setName(param.getName());
		iParam.setReadCount(0);
		iTypeRef.setType(type);
		iParam.setType(iTypeRef);

		if (param.getDefaultValue() != null) {
		    iParam.setDefaultValue(expressionToKarajan(param.getDefaultValue(), container));
		}
		return iParam;
	}

	public void variableForSymbol(VariableDeclaration var, IStatementContainer container) throws CompilationException {
		Type type = findType(var.getType());
		container.getScope().addVariable(var.getName(), type, "Variable", 
		    var.isGlobal() ? AccessType.GLOBAL : AccessType.LOCAL, VariableOrigin.USER, var, null);
	}

	public void variable(VariableDeclaration var, IStatementContainer container, boolean internal) throws CompilationException {
	    Type type = findType(var.getType());
	    IVariableDeclaration iVar = new IVariableDeclaration();
	    iVar.setName(var.getName());
	    iVar.setType(type);
	    iVar.setField(addInternedField(var.getName(), type));
	    iVar.setGlobal(Boolean.valueOf(var.isGlobal()));
	    iVar.setLine(var.getLine());
	    
	    
	    if (internal) {
	        container.addVariableDeclaration(iVar);
	        return;
	    }
	    else {
	        container.getScope().setDeclaration(var.getName(), iVar);		
	        variables.add(iVar);
	    }
		
		
		/*
		 *  possibly an input; mark it as such here, and if
		 *  writers are detected, remove the input attribute (that is
		 *  done in VariableScope).
		 */
		iVar.setInput(true);

		if (var.getMapping() != null || var.getExpression() != null) {
			if (var.getExpression() != null) {
				IExpression expr = this.expressionToKarajan(var.getExpression(), container);
				IMapping iMapping = new IMapping();
				Type exprType = expr.getType();
				iMapping.setLine(var.getExpression().getLine());
				if (exprType.equals(Types.STRING)) {
					iMapping.setName("SingleFileMapper");
					iMapping.addParameter(new IMappingParameter("file", expr));
				}
				else if (exprType.isArray() && (exprType.itemType().equals(Types.STRING) || exprType.itemType().isMapped())) {
                    iMapping.setName("FixedArrayMapper");
                    iMapping.addParameter(new IMappingParameter("files", expr));
				}
				else {
					throw new CompilationException("Cannot use expression of type " + expr.getType() + " as mapping expression");
				}
				iVar.setMapping(iMapping);
			}
			else {
				MappingDeclaration mapping = var.getMapping();
    			if (mapping != null) {
    			    IMapping iMapping = new IMapping();
    			    String mapperType = mapping.getDescriptor();
                    iMapping.setName(mapperType);
                    
    				checkMapperParams(mapperType, mapping);
    				for (MappingParameter param : mapping.getParameters()) {
    					iMapping.addParameter(mappingParameter(param, container));
    				}
    				iVar.setMapping(iMapping);
    			}
			}
   		}
		else {
			// add temporary mapping info if not primitive or array of primitive
		   
			if (!isPrimitiveOrArrayOfPrimitives(type)) {
			    IMapping iMapping = new IMapping();
			    iMapping.setName("InternalMapper");
			    IMappingParameter iParam = new IMappingParameter("prefix", 
			        new IValue(Types.STRING, var.getName() + "-" + var.getLine()));
			    iMapping.addParameter(iParam);
			    iVar.setMapping(iMapping);
			}
   		}
		// must have this at the end since there might be interned mapping
		// parameters added and they need to be defined before this declaration
		container.addVariableDeclaration(iVar);
	}

    private void checkMapperParams(String mapperType, MappingDeclaration mapping) throws CompilationException {
        if (!MapperFactory.isValidMapperType(mapperType)) {
            throw new CompilationException("Unknown mapper type: '" + mapperType + "'");
        }
        
        Set<String> validParams = MapperFactory.getValidParams(mapperType);
        if (validParams == null && mapping.getParameters().size() > 0) {
            throw new CompilationException(mapperType + " does not support any parameters");
        }
        if (validParams.contains("*")) {
            // mapper accepts any parameter (e.g. external_mapper)
            return;
        }
        for (MappingParameter param : mapping.getParameters()) {
            if (!validParams.contains(param.getName())) {
                throw new CompilationException(mapperType + " does not support a '" + param.getName() + "' parameter");
            }
        }
    }

    private IMappingParameter mappingParameter(MappingParameter param, IStatementContainer container) throws CompilationException {
        IMappingParameter iParam = new IMappingParameter();
        iParam.setName(param.getName());
        Expression.Type type = param.getValue().getExpressionType();
        if (type == Expression.Type.VARIABLE_REFERENCE) {
            iParam.setValue(expressionToKarajan(param.getValue(), container));
        } 
        else {
            /*
             * The declarations are not processed in parallel. In order to avoid issues when
             * parameters depend on other variables, declarations happen first, instantiating
             * futures as placeholders. Then the actual processing happens.
             */
            String parameterVariableName = "swift.mapper." + (internedIDCounter++);
            
            IExpression iParamValue = expressionToKarajan(param.getValue(), container);
            
            // use the abstract syntax tree rather than the intermediate tree
            VariableDeclaration decl = new VariableDeclaration();
            decl.setName(parameterVariableName);
            decl.setType(iParamValue.getType().toString());
            decl.setMapping(new MappingDeclaration("InternalMapper", 
                new MappingParameter("prefix", new StringConstant(parameterVariableName + "-" + 
                        UUIDGenerator.getInstance().generateRandomBasedUUID().toString()))));
            variable(decl, container, true);
            
            IVariableReference iParamVarRef = new IVariableReference(parameterVariableName);
            IAssignment assgn = new IAssignment(iParamVarRef, iParamValue);
            assgn.setDeleteOnAssign(true);
            container.addStatement(assgn);
            
            iParam.setValue(iParamVarRef);
        }
        return iParam;
    }
    
    private boolean isPrimitiveOrArrayOfPrimitives(Type t) {
        return t.isPrimitive() || (t.isArray() && t.itemType().isPrimitive());
    }
    
	public void assign(Assignment assign, IStatementContainer container) throws CompilationException {
		try {
		    Expression value = assign.getRhs();
            if (isProcedureCall(value)) {
                Call call = (Call) value;
                ReturnParameter out = new ReturnParameter();
                call.addReturn(out);
                out.setLValue(assign.getLhs());
                call(call, container, false);
            }
            else {
                IAssignment iAssign = new IAssignment();
                IExpression lValue = expressionToKarajan(assign.getLhs(), container, true, null);
                if (!(lValue instanceof ILValue)) {
                    throw new CompilationException("Expected lvalue");
                }
                ILValue iLValue = (ILValue) lValue;
                iAssign.setLValue(iLValue);
                
                addWriterToScope(container.getScope(), assign.getLhs(), iLValue, assign, iAssign);
                
                IExpression iRValue = expressionToKarajan(assign.getRhs(), container, false, iLValue, iLValue.getType());
                if (iRValue == null) {
                    // the expression will handle the assignment
                    return;
                }
                iAssign.setRValue(iRValue);
                iAssign.setLine(assign.getLine());
                    			
    			checkOrInferReturnedType(iLValue, iRValue);
    			
    			container.addStatement(iAssign);
            }
		} 
		catch (CompilationException re) {
			throw new CompilationException("Compile error in assignment at " + 
			    assign.getLine() + ": " + re.getMessage(), re);
		}
	}

    private void checkOrInferReturnedType(ILValue var, IExpression value) throws CompilationException {
        Type lValueType = var.getType();
        Type rValueType = value.getType();
        if (lValueType.equals(Types.ANY)) {
            if (rValueType.equals(Types.ANY)) {
                // any <- any
            }
            else {
                // any <- someType, so infer lValueType as rValueType
                var.setType(rValueType);
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

    private boolean isProcedureCall(Expression expr) {
        return expr.getExpressionType() == Expression.Type.CALL_EXPRESSION;
    }
    
    public void append(Append append, IStatementContainer container) throws CompilationException {
        try {
            IAppend iAppend = new IAppend();
            IExpression array = expressionToKarajan(append.getLhs(), container, true, null);
            IExpression value = expressionToKarajan(append.getRhs(), container);
            Type arrayType = array.getType();
            if (!arrayType.keyType().equals(Types.AUTO)) {
                throw new CompilationException("Illegal append to an array of type '" + arrayType + 
                    "'. Array must have 'auto' key type.");
            }
            if (!value.getType().equals(arrayType.itemType())) {
                throw new CompilationException("Cannot append value of type " + value.getType() +
                        " to an array of type " + array.getType());
            }
            iAppend.setLValue((ILValue) array);
            iAppend.setRValue(value);
            String rootvar = abstractExpressionToRootVariable(append.getLhs());
            // an append is always a partial write
            container.getScope().addWriter(rootvar, WriteType.PARTIAL, append, iAppend);
            container.addStatement(iAppend);
        } 
        catch(CompilationException re) {
            throw new CompilationException("Compile error in assignment at " + 
                append.getLine() + ": " + re.getMessage(), re);
        }
    }

	public void statementsForSymbols(StatementContainer prog, IStatementContainer container) throws CompilationException {
	    for (VariableDeclaration decl : prog.getVariableDeclarations()) {
	        variableForSymbol(decl, container);
	    }
	}

	public void statements(StatementContainer prog, IStatementContainer container) throws CompilationException {
	    for (VariableDeclaration decl : prog.getVariableDeclarations()) {
            variable(decl, container, false);
        }
	    // sort declarations because some mapper parameter values may depend on other variables
		for (Statement s : prog.getStatements()) {
		    statement(s, container);
		}
	}

	public void statement(Statement child, IStatementContainer container) throws CompilationException {
	    if (child instanceof Assignment) {
			assign((Assignment) child, container);
		}
		else if (child instanceof Append) {
		    append((Append) child, container);
		}
		else if (child instanceof Call) {
			call((Call) child, container, false);
		}
		else if (child instanceof ForeachStatement) {
			foreachStat((ForeachStatement) child, container);
		}
		else if (child instanceof IterateStatement) {
			iterateStat((IterateStatement) child, container);
		}
		else if (child instanceof IfStatement) {
			ifStat((IfStatement) child, container);
		}
		else if (child instanceof SwitchStatement) {
			switchStat((SwitchStatement) child, container);
		}
		else {
			throw new CompilationException("Unexpected element in parse tree. " +
					"Implementing class " + child.getClass() + ", content " + child);
		}
	}
	
	protected ActualParameters getActualParameters(Call call, IStatementContainer container) throws CompilationException {
	    return getActualParameters(call, container, null);
	}
	
	protected ActualParameters getActualParameters(Call call, IStatementContainer container, Signature expected) throws CompilationException {
	    ActualParameters actuals = new ActualParameters();
	    Set<String> seen = new HashSet<String>();
	    for (ActualParameter ap : call.getParameters()) {
	        checkDuplicate(seen, ap.getBinding(), "parameter");
	        // TODO do partial matching to infer type
	        IActualParameter iParam = actualParameter(ap, container);
	        actuals.addParameter(ap, iParam);
	    }
	    seen.clear();
	    for (ReturnParameter ar : call.getReturns()) {
	        checkDuplicate(seen, ar.getBinding(), "return");
            IActualParameter iParam = actualReturn(ar, container);
            actuals.addReturn(ar, iParam);
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

    protected ActualParameters getActualParameters(FunctionInvocation f, IStatementContainer container, Type expectedType) 
            throws CompilationException {
        return getActualParameters(f.getParameters(), container, expectedType);
    }
	
	protected ActualParameters getActualParameters(List<ActualParameter> params, 
	        IStatementContainer container, Type expectedType) throws CompilationException {
        ActualParameters actuals = new ActualParameters();
        for (ActualParameter param : params) {
            IExpression iExpr = expressionToKarajan(param.getValue(), container, false, null);
            actuals.addParameter(param, new IActualParameter(iExpr, iExpr.getType()));
        }
        if (expectedType != null) {
            actuals.addReturn(null, new IActualParameter(null, expectedType));
        }
        else {
            actuals.addReturn(null, new IActualParameter(null, Types.ANY));
        }
        return actuals;
    }


	public INode call(Call call, IStatementContainer container, boolean inhibitOutput) throws CompilationException {
		try {
		    String procName = call.getName();
		    // see if a single function with this name is defined
		    List<Signature> fns = functionsMap.findAll(procName);
		    // first we figure out actual parameter types
		    ActualParameters actualParams = getActualParameters(call, container, fns.size() == 1 ? fns.get(0) : null);
		    
			// Check is called procedure declared previously
			Signature proc = functionsMap.find(procName, actualParams, false);
			if (proc == null) {
			    throw noFunctionOrProcedure(procName, actualParams, false);
			}
			
			if (proc.isProcedure()) {
			    return call(call, container, proc, actualParams, inhibitOutput);
			}
			else {
			    IAssignment iAssign = functionAsCall(call, container, proc, actualParams, 
			        actualParams.getReturn(0).getType());
                if (!inhibitOutput) {
                    container.addStatement(iAssign);
                }
                return iAssign;
			}
		} 
		catch (CompilationException ce) {
			throw new CompilationException("Compile error in procedure invocation at " + call.getLine(), ce);
		}
	}

	private ICall call(Call call, IStatementContainer container, Signature sig, ActualParameters actualParams, 
	        boolean inhibitOutput) throws CompilationException {

	    if (sig.isDeprecated()) {
            Warnings.warn(Warnings.Type.DEPRECATION, 
                call, "Procedure " + sig.getName() + " is deprecated");
        }
            
	    ICall iCall = new ICall();
        if (sig.getType() == Signature.InvocationType.USER_DEFINED) {
            iCall.setInternal(false);
        } 
        else if (sig.getType() == Signature.InvocationType.INTERNAL) {
            iCall.setInternal(true);
        } 
        else {
            throw new CompilationException("Unhandled procedure invocation mode " +
                sig.getType());
        }
        iCall.setLine(call.getLine());
        iCall.setName(sig.getMangledName());
        
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
            IActualParameter iActual = actualParams.getReturn(i);
            iActual.setBinding(null);
            iCall.addReturn(iActual);
            addWriterToScope(container.getScope(), call.getReturns().get(i).getLValue(), iActual.getValue(), call, iCall);
        }
                    
        // parameters
        setParameters(iCall, actualParams, sig, container.getScope());
        
        if (!inhibitOutput) {
            container.addStatement(iCall);
        }
        if (allVariables(iCall.getReturns()) && allVariables(iCall.getParameters())) {
            iCall.setSerialize(true);
        }

        return iCall;
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

    private void setParameters(ICall iCall, ActualParameters actualParams, Signature sig, VariableScope scope) 
            throws CompilationException {
        
	    for (int i = 0; i < actualParams.positionalCount(); i++) {
            IActualParameter compiled = actualParams.getPositional(i);
            compiled.setBinding(null);
            iCall.addParameter(compiled);
        }
        for (int i = 0; i < actualParams.optionalCount(); i++) {
            IActualParameter compiled = actualParams.getOptional(i);
            iCall.addParameter(compiled);
        }
        for (int i = 0; i < actualParams.varargCount(); i++) {
            // TODO: currently only internal functions accept vargs
            IActualParameter compiled = actualParams.getVararg(i);
            iCall.addParameter(compiled);
        }
    }

    private static final Set<Class<?>> VAR_TYPES;
	
	static {
	    VAR_TYPES = new HashSet<Class<?>>();
	    VAR_TYPES.add(IVariableReference.class);
	    VAR_TYPES.add(IArrayReference.class);
	    VAR_TYPES.add(ISliceArray.class);
	    VAR_TYPES.add(IStructReference.class);
	}
	
    private boolean allVariables(List<IActualParameter> l) {
        if (l == null) {
            return true;
        }
        for (IActualParameter pst : l) {
            if (!VAR_TYPES.contains(pst.getValue().getClass())) {
                return false;
            }
        }
        return true;
    }

    private void addWriterToScope(VariableScope scope, LValue var, INode iLValue, Node src, INode out) throws CompilationException {
        String rootvar = abstractExpressionToRootVariable(var);
        WriteType writeType = getRootVariableWriteType(var);
        if (writeType == WriteType.FULL && (iLValue instanceof IVariableReference)) {
            IVariableReference iVar = (IVariableReference) iLValue;
            iVar.setIsFullWrite(true);
        }
        scope.addWriter(rootvar, writeType, src, out);
    }

    private boolean isFunctionReturn(IVariableReference iVar) {
        IRefCounted iDecl = iVar.getDeclaration();
        if (iDecl instanceof IFormalParameter) {
            IFormalParameter iParam = (IFormalParameter) iDecl;
            return iParam.isReturn();
        }
        else {
            return false;
        }
    }

    public void iterateStat(IterateStatement iterate, IStatementContainer container) throws CompilationException {
		VariableScope loopScope = new VariableScope(this, container.getScope(), EnclosureType.ALL, iterate);
		VariableScope innerScope = new VariableScope(this, loopScope, EnclosureType.LOOP, iterate);

		loopScope.addVariable(iterate.getVar(), Types.INT, "Iteration variable",  VariableOrigin.AUTO, iterate, null);
		IIterate iIterate = new IIterate(innerScope);
		iIterate.setLine(iterate.getLine());

		iIterate.setVarName(iterate.getVar());
		
		loopScope.addWriter(iterate.getVar(), WriteType.FULL, iterate, iIterate);

		statementsForSymbols(iterate.getBody(), iIterate);
		statements(iterate.getBody(), iIterate);

		Expression cond = iterate.getCondition();
		IExpression iCondition = expressionToKarajan(cond, iIterate);
		
		iIterate.setCondition(iCondition);
		
		container.addStatement(iIterate);
	}

	public void foreachStat(ForeachStatement foreach, IStatementContainer container) throws CompilationException {
		try {
			VariableScope innerScope = new VariableScope(this, container.getScope(), EnclosureType.LOOP, foreach);

			IForeach iForeach = new IForeach(innerScope);
			iForeach.setVarName(foreach.getVar());
			iForeach.setLine(foreach.getLine());
			
			Expression in = foreach.getInExpression();
			IExpression iExpr = expressionToKarajan(in, container);
			iForeach.setIn(iExpr);
			
			if (iExpr instanceof IVariableReference) {
                innerScope.setForeachSourceVar(((IVariableReference) iExpr).getName(), foreach);
            }

			Type inType = iExpr.getType();
			if (!inType.isArray()) {
			    throw new CompilationException("You can iterate through an array structure only");
			}
			innerScope.addVariable(foreach.getVar(), inType.itemType(), "Iteration variable", 
			    VariableOrigin.AUTO, foreach, null);
			innerScope.addWriter(foreach.getVar(), WriteType.FULL, foreach, iForeach);
			iForeach.setIndexVarName(foreach.getIndexVar());
			
			if (foreach.getIndexVar() != null) {
			    iForeach.setIndexVarFieldName(addInternedField(foreach.getIndexVar(), inType.keyType()));
				innerScope.addVariable(foreach.getIndexVar(), inType.keyType(), "Iteration variable", 
				    VariableOrigin.AUTO, foreach, null);
				innerScope.addWriter(foreach.getIndexVar(), WriteType.FULL, foreach, iForeach);
			}

			statementsForSymbols(foreach.getBody(), iForeach);
			statements(foreach.getBody(), iForeach);
			
			container.addStatement(iForeach);
			
			Collection<String> cleanups = innerScope.getCleanups();
			cleanups.remove(foreach.getVar());
		} 
		catch (CompilationException re) {
			throw new CompilationException("Compile error in foreach statement at " + 
			    foreach.getLine(), re);
		}

	}

	public void ifStat(IfStatement ifstat, IStatementContainer container) throws CompilationException {
	    IIfStatement iIf = new IIfStatement();
	    IExpression iCondition = expressionToKarajan(ifstat.getCondition(), container);
	    iIf.setCondition(iCondition);
	    iIf.setLine(ifstat.getLine());
	    iIf.setSource(ifstat);
		
		if (!iCondition.getType().equals(Types.BOOLEAN)) {
			throw new CompilationException ("Condition in if statement has to be of boolean type.");
		}

		StatementContainer thenstat = ifstat.getThenScope();
		StatementContainer elsestat = ifstat.getElseScope();

		VariableScope innerThenScope = new VariableScope(this, container.getScope(), EnclosureType.CONDITION, thenstat);
		IConditionBranch iThen = new IConditionBranch(innerThenScope);

		statementsForSymbols(thenstat, iThen);
		statements(thenstat, iThen);
		
		iIf.setThenBlock(iThen);
		
		IConditionBranch iElse = null;

		if (elsestat != null) {
			VariableScope innerElseScope = new VariableScope(this, container.getScope(), 
			    EnclosureType.CONDITION, elsestat);
			iElse = new IConditionBranch(innerElseScope);
			
			iIf.setElseBlock(iElse);
			
			statementsForSymbols(elsestat, iElse);
			statements(elsestat, iElse);			
		}
		else if (innerThenScope.hasPartialCount()) {
		    // must do matching partial closing somewhere, so need 
		    // else branch even though not specified in the swift source
		    VariableScope innerElseScope = new VariableScope(this, container.getScope(), 
                EnclosureType.CONDITION, elsestat);
		    iElse = new IConditionBranch(innerElseScope);
            iIf.setElseBlock(iElse);
		}
		
		if (iElse != null) {
		    balanceReadsAndWrites(iIf, container, iThen, iElse);
		}
		container.addStatement(iIf);
	}

	public void switchStat(SwitchStatement switchstat, IStatementContainer container) throws CompilationException {
	    ISwitch iSwitch = new ISwitch();
		IExpression iCondition = expressionToKarajan(switchstat.getCondition(), container);
		iSwitch.setCondition(iCondition);
		iSwitch.setSource(switchstat);

		/* TODO can switch statement can be anything apart from int and float ? */
		/* TODO: yes, it can be a string, too. And why not boolean? */
		if (!iCondition.getType().equals(Types.INT) && !iCondition.getType().equals(Types.FLOAT)) {
			throw new CompilationException("Condition in switch statements has to be of numeric type.");
		}

		IConditionBranch[] iCases = new IConditionBranch[switchstat.getCases().size()];
		boolean anyBranchHasPartialCount = false;
		for (int i = 0; i < iCases.length; i++) {
		    SwitchCase casestat = switchstat.getCases().get(i);
			VariableScope caseScope = new VariableScope(this, container.getScope(), 
			    EnclosureType.CONDITION, casestat);
			
			IConditionBranch iBranch;

			if (casestat.getIsDefault()) {
			    VariableScope defaultScope = new VariableScope(this, container.getScope(), 
                EnclosureType.CONDITION, casestat);
			    IConditionBranch iDefault = new IConditionBranch(defaultScope);
			    iSwitch.setDefaultCase(iDefault);
			    statementsForSymbols(casestat.getBody(), iDefault);
			    statements(casestat.getBody(), iDefault);
			    iBranch = iDefault;
			}
			else {
			    ICase iCase = new ICase(caseScope);
			    iSwitch.addCase(iCase);
			    caseStat(casestat, iCase);
			    iBranch = iCase;
			}
			iCases[i] = iBranch;
			if (!anyBranchHasPartialCount && iBranch.getScope().hasPartialCount()) {
			    anyBranchHasPartialCount = true;
			}
		}
		if (anyBranchHasPartialCount) {
		    balanceReadsAndWrites(iSwitch, container, iCases);
		}
		container.addStatement(iSwitch);
	}

	private void balanceReadsAndWrites(IStatement statement, IStatementContainer container, 
	        IConditionBranch... branches) throws CompilationException {
	    // two pass: compute maximum counts, then compute differences
	    balanceCounts(statement, container, branches, VariableScope.CountType.READ);
	    balanceCounts(statement, container, branches, VariableScope.CountType.WRITE);
    }

    private void balanceCounts(IStatement iStat, IStatementContainer container, 
            IConditionBranch[] branches, VariableScope.CountType countType) 
            throws CompilationException {
        
        Map<String, Integer> max = new HashMap<String, Integer>();
        for (IConditionBranch branch : branches) {
            for (VariableScope.VariableUsage v : branch.getScope().getVariableUsageValues()) {
                if (branch.getScope().isVariableLocallyDefined(v.getName())) {
                    // this is not about variables defined inside the branches
                }
                else {
                    putIfGreater(max, v.getName(), v.getReferenceCount(countType));
                }
            }
        }
        Iterator<Map.Entry<String, Integer>> it = max.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> e = it.next();
            if (container.getScope().getOrigin(e.getKey()) == VariableScope.VariableOrigin.AUTO) {
                it.remove();
            }
        }
        for (Map.Entry<String, Integer> e : max.entrySet()) {
            String name = e.getKey();
            int maxCount = e.getValue();
            if (maxCount == 0) {
                continue;
            }
            // propagate upwards since we inhibited that in VariableScope
            switch (countType) {
                case READ:
                    container.getScope().addReaders(name, true, iStat.getSource(), iStat, maxCount);
                    break;
                case WRITE:
                    if (maxCount == VariableScope.FULL_WRITE_COUNT) {
                        container.getScope().addWriter(name, WriteType.FULL, iStat.getSource(), iStat);
                    }
                    else {
                        container.getScope().addWriters(name, WriteType.PARTIAL, 
                            iStat.getSource(), iStat, maxCount);
                    }
                    break;
            }
        }
        for (IConditionBranch branch : branches) {
            for (Map.Entry<String, Integer> e : max.entrySet()) {
                String name = e.getKey();
                int maxCount = e.getValue();
                VariableScope.VariableUsage v = branch.getScope().getExistingUsage(name);
                int crtCount;
                if (v == null) {
                    crtCount = 0;
                }
                else {
                    crtCount = v.getReferenceCount(countType);
                }
                int diff = maxCount - crtCount;
                // if both are FULL_WRITE_COUNT, nothing needs to be done and, luckily, diff == 0
                if (diff > 0) {
                    switch (countType) {
                        case READ:
                            branch.setPreClean(name, diff);
                            break;
                        case WRITE:
                            if (maxCount == VariableScope.FULL_WRITE_COUNT) {
                                if (crtCount == 0) {
                                    Warnings.warn(Warnings.Type.DATAFLOW, "Variable " + name + 
                                        " is fully written in one branch but not in other(s). "
                                        + "This can lead to unspecified behavior.");
                                }
                                branch.setPostFullClose(name);
                            }
                            else {
                                branch.setPreClose(name, diff);
                            }
                            break;
                    }
                }
            }
        }
    }

    private void putIfGreater(Map<String, Integer> m, String name, int value) {
        Integer x = m.get(name);
        if (x == null || x < value) {
            m.put(name, value);
        }
    }

    public void caseStat(SwitchCase casestat, ICase iCase) throws CompilationException {
		IExpression iValue = expressionToKarajan(casestat.getValue(), iCase);
		iCase.setValue(iValue);
		statementsForSymbols(casestat.getBody(), iCase);
		statements(casestat.getBody(), iCase);
	}
	
	public IActualParameter actualParameter(ActualParameter arg, IStatementContainer container) throws CompilationException {
        return actualParameter(arg, arg.getBinding(), container, false, null);
    }
	
	public IActualParameter actualReturn(ReturnParameter arg, IStatementContainer container) throws CompilationException {
        return actualReturn(arg, arg.getBinding(), container, null);
    }
	
	public IActualParameter actualParameter(ActualParameter arg, String bind, IStatementContainer container) throws CompilationException {
	    return actualParameter(arg, bind, container, false, null);
	}
	
	public IActualParameter actualReturn(ReturnParameter arg, String bind, IStatementContainer container) throws CompilationException {
        return actualReturn(arg, bind, container, null);
    }
	
	public IActualParameter actualParameter(ActualParameter arg, IStatementContainer container, boolean lvalue) throws CompilationException {
        return actualParameter(arg, null, container, lvalue, null);
    }
	

	public IActualParameter actualParameter(ActualParameter arg, String bind, IStatementContainer container, 
	        boolean lvalue, Type expectedType) throws CompilationException {
	    IActualParameter iParam = new IActualParameter();
	    IExpression iExpr = expressionToKarajan(arg.getValue(), container, lvalue, null, expectedType);
	    iParam.setValue(iExpr);
		if (bind != null) {
		    iParam.setBinding(arg.getBinding());
		}
		iParam.setType(iExpr.getType());
		return iParam;
	}
	
	public IActualParameter actualReturn(ReturnParameter arg, String bind, IStatementContainer container, 
            Type expectedType) throws CompilationException {
	    IActualParameter iParam = new IActualParameter();
        IExpression iExpr = expressionToKarajan(arg.getLValue(), container, true, null, expectedType);
        iParam.setValue(iExpr);
        if (bind != null) {
            iParam.setBinding(arg.getBinding());
        }
        iParam.setType(iExpr.getType());
        return iParam;
    }

	public void binding(AppDeclaration bind, IProcedureDeclaration iProcedure) throws CompilationException {
	    // only apps for now
	    iProcedure.setIsApplication(true);
	    iProcedure.setApplication(application(bind, iProcedure));
	}

	public IApplication application(AppDeclaration app, IProcedureDeclaration iProcedure) throws CompilationException {
		try {
		    IApplication iApp = new IApplication();
		    for (AppCommand cmd : app.getCommands()) {
		        IApplicationCommand iCmd = new IApplicationCommand();
    		    iCmd.setExecutable(cmd.getExecutable());
    			for (Expression argument : cmd.getArguments()) {
    				IExpression iArg = expressionToKarajan(argument, iProcedure, false, null, Types.ANY);
    				Type type = iArg.getType();
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
    				    iCmd.addArgument(iArg);
    				} 
    				else {
    					throw new CompilationException("Cannot pass type '" + type + 
    					    "' as a parameter to application '" + cmd.getExecutable() + "'");
    				}
    			}
    			if (cmd.getRedirect("stdin") != null) {
    			    iCmd.setStdin(expressionToKarajan(cmd.getRedirect("stdin"), iProcedure));
    			}
    			if (cmd.getRedirect("stdout") != null) {
    			    iCmd.setStdout(expressionToKarajan(cmd.getRedirect("stdout"), iProcedure));
    			}
    			if (cmd.getRedirect("stderr") != null) {
    			    iCmd.setStderr(expressionToKarajan(cmd.getRedirect("stderr"), iProcedure));
    			}
    			iApp.addCommand(iCmd);
		    }
			addProfiles(app, iProcedure, iApp);
			return iApp;
		} 
		catch (CompilationException e) {
			throw new CompilationException(e.getMessage() + 
			    " in application " + app.getName() + " at " + app.getLine(), e);
		}
	}

	private void addProfiles(AppDeclaration app, IStatementContainer container, IApplication iApp) 
	        throws CompilationException {
		List<AppProfile> profiles = app.getProfiles();
		if (profiles == null || profiles.isEmpty()) { 
			return;
		}
		IAppProfile iProfile = new IAppProfile();
		for (AppProfile profile : profiles) { 
		    iProfile.add(expressionToKarajan(profile.getName(), container), 
		        expressionToKarajan(profile.getValue(), container));
		}
		iApp.setProfile(iProfile);
	}

	/** Produces a Karajan function invocation from a SwiftScript invocation.
	  * The Karajan invocation will have the same name as the SwiftScript
	  * function, in the 'vdl' Karajan namespace. Parameters to the
	  * Karajan function will differ from the SwiftScript parameters in
	  * a number of ways - read the source for the exact ways in which
	  * that happens.
	  */

	public IFunctionCall function(Call func, String name, List<ActualParameter> arguments, 
	        IStatementContainer container, Type expectedType) throws CompilationException {
	    ActualParameters actual = getActualParameters(arguments, container, expectedType);
	    Signature funcSignature = functionsMap.find(name, actual, true);
        if (funcSignature == null) {
            funcSignature = functionsMap.find(name, actual, true);
            throw new CompilationException("Unknown function: '" + name + "'");
        }
	    
        return function(func, container, funcSignature, actual);
	}

	private IFunctionCall function(Call func, IStatementContainer container, Signature funcSignature,
            ActualParameters actual) throws CompilationException {
	    IFunctionCall iFCall = new IFunctionCall();
	    iFCall.setLine(func.getLine());
	    iFCall.setName(funcSignature.getMangledName());
        
        setParameters(iFCall, actual, funcSignature, container.getScope());

        return iFCall;
    }
	
	public IExpression expressionToKarajan(Expression expression, IStatementContainer container) 
            throws CompilationException {
        return expressionToKarajan(expression, container, false, null, null);
    }
	
	public IExpression expressionToKarajan(Expression expression, IStatementContainer container, ILValue lvalue) 
	        throws CompilationException {
	    return expressionToKarajan(expression, container, false, lvalue, null);
    }
	
	public IExpression expressionToKarajan(Expression expression, IStatementContainer container, 
            boolean isLvalue, ILValue lvalue) throws CompilationException {
	    return expressionToKarajan(expression, container, isLvalue, lvalue, null);
	}

	/** converts an XML intermediate form expression into a
	 *  Karajan expression.
	 */
	public IExpression expressionToKarajan(Expression expression, IStatementContainer container, 
	        boolean isLvalue, ILValue lvalue, Type expectedType) throws CompilationException {
	    
	    Expression.Type type = expression.getExpressionType();
	    IExpression iExpr;
	    switch (type) {
	        case OR:
	            return simpleBinaryOp((BinaryOperator) expression, container, 
	                "||", Types.BOOLEAN, Types.BOOLEAN);
	            
	        case AND:
	            return simpleBinaryOp((BinaryOperator) expression, container, 
	                "&amp;&amp;", Types.BOOLEAN, Types.BOOLEAN);
	            
	        case BOOLEAN_CONSTANT: {
    			BooleanConstant bc = (BooleanConstant) expression;
    			boolean b = bc.getValue();
    			return new IValue(Types.BOOLEAN, String.valueOf(b));
	        }
    			
	        case INTEGER_CONSTANT: {
	            IntegerConstant ic = (IntegerConstant) expression;
	            int i = ic.getValue();
	            Integer iobj = Integer.valueOf(i);
	            String internedID;
	            if (intInternMap.get(iobj) == null) {
	                internedID = "swift.int." + i;
	                intInternMap.put(iobj, internedID);
	            } 
	            else {
	                internedID = intInternMap.get(iobj);
	            }
	            return new IVariableReference(Types.INT, internedID);
	        }
	            
	        case FLOAT_CONSTANT: {
	            FloatConstant fc = (FloatConstant) expression;
	            double f = fc.getValue();
	            Double fobj = new Double(f);
	            String internedID;
	            if (floatInternMap.get(fobj) == null) {
	                internedID = "swift.float." + (internedIDCounter++);
	                floatInternMap.put(fobj, internedID);
	            } 
	            else {
	                internedID = floatInternMap.get(fobj);
	            }
	            return new IVariableReference(Types.FLOAT, internedID);
	        }
	            
	        case STRING_CONSTANT: {
	            StringConstant sc = (StringConstant) expression;
	            String s = sc.getValue();
	            String internedID;
	            if (stringInternMap.get(s) == null) {
	                internedID = "swift.string." + (internedIDCounter++);
	                stringInternMap.put(s, internedID);
	            } 
	            else {
	                internedID = stringInternMap.get(s);
	            }
	            return new IVariableReference(Types.STRING, internedID);
	        }
	            
	        case EQ:
	        case NE:
	        case LT:
	        case LE:
	        case GT:
	        case GE: {
	            IBinaryOperator iOp = binaryOperator((BinaryOperator) expression, container);
	            checkTypesInCondExpr(iOp.getOp(), iOp.getLeft().getType(), iOp.getRight().getType(), iOp);
	            return iOp;
	        }
	            
	        case PLUS:
	        case MINUS:
	        case MUL:
	        case FDIV:
	        case IDIV:
	        case MOD: {
	            IBinaryOperator iOp = binaryOperator((BinaryOperator) expression, container);
                checkTypesInArithmExpr(iOp.getOp(), iOp.getLeft().getType(), iOp.getRight().getType(), iOp);
                return iOp;
	        }
	            
	        case NEGATION: {
	            IUnaryOperator iOp = unaryOperator(IUnaryOperator.OperatorType.NEGATION, (UnaryOperator) expression, container);
	            if (!(iOp.getOperand().getType().equals(Types.FLOAT)) && !(iOp.getOperand().getType().equals(Types.INT))) {
	                throw new CompilationException("Negation operation can only be applied to parameter of numeric types.");
	            }
	            iOp.setType(iOp.getOperand().getType());
	            return iOp;
	        }
	            
	        case NOT: {
	            IUnaryOperator iOp = unaryOperator(IUnaryOperator.OperatorType.NOT, (UnaryOperator) expression, container);
                if (iOp.getOperand().getType().equals(Types.BOOLEAN)) {
                    iOp.setType(Types.BOOLEAN);
                }
                else {
                    throw new CompilationException("Not operation can only be applied to parameter of type boolean.");
                }
                return iOp;
	        }
	        case VARIABLE_REFERENCE: {
	            VariableReference ref = (VariableReference) expression;
	            String name = ref.getName();
	            if (!container.getScope().isVariableDefined(name)) {
	                throw new CompilationException("Variable " + name + 
	                    " was not declared in this scope.");
	            }
	            IRefCounted iDecl = container.getScope().getDeclaration(name);
				
	            IVariableReference iVar = new IVariableReference(name);
	            iVar.setDeclaration(iDecl);
	            if (isLvalue) {
	                iVar.setIsLValue(true);
	            }
	            else {
                    container.getScope().addReader(name, false, expression, iVar);
                }
	            Type actualType = container.getScope().getVariableType(name);
	            iVar.setType(actualType);

    			return iVar;
	        }
	        case ARRAY_SUBSCRIPT_EXPRESSION: {
	            ArrayReference ar = (ArrayReference) expression;
	            IArrayReference iArrayRef = new IArrayReference();
	            IExpression iArray = expressionToKarajan(ar.getBase(), container, true, null);
	            Type declaredArrayType = iArray.getType();
	            if (ar.getIndex().getExpressionType() == Expression.Type.STAR_EXPRESSION) {
	                // add full reader
	            	if (!isLvalue) {
	            	    container.getScope().addReader(getRootVar(iArray), false, expression, iArray);
	            	}
	                return iArray;
	            }
	            IExpression iIndex = expressionToKarajan(ar.getIndex(), container);

	            Type indexType = iIndex.getType();
	            Type declaredIndexType = declaredArrayType.keyType();
	            // the index type must match the declared index type,
	            // unless the declared index type is *
	            if (declaredIndexType == null) {
	                declaredIndexType = Types.INT;
	            }
						
	            if (!indexType.equals(declaredIndexType) 
	                    && !declaredIndexType.equals(Types.ANY)) {
	                throw new CompilationException("Supplied array index type (" 
			        + indexType + ") does not match the declared index type (" + declaredIndexType + ")");
	            }
			
	            if (!isLvalue) {
	            	container.getScope().addReader(getRootVar(iArray), true, expression, iArray);
	            }
			
	            iArrayRef.setArray(iArray);
	            iArrayRef.setKey(iIndex);
	            iArrayRef.setType(declaredArrayType.itemType());

	            return iArrayRef;
	        }
	        case STRUCT_MEMBER_REFERENCE: {
	            StructReference sm = (StructReference) expression;
	            
	            IExpression iStruct = expressionToKarajan(sm.getBase(), container, true, null);

	            Type parentType = iStruct.getType();
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
	                actualType = parentType.getField(sm.getField()).getType();
	            }
	            catch (NoSuchFieldException e) {
	                throw new CompilationException("No member " + sm.getField() + " in type " + parentType);
	            }
    			IStructReference iStructRef;
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
    			    
    			    iStructRef = new ISliceArray();
    			}
    			else {
    			    iStructRef = new IStructReference();
    			}
    			
    			container.getScope().addReader(getRootVar(iStruct), true, expression, iStruct);
    			iStructRef.setStruct(iStruct);
    			iStructRef.setFieldName(sm.getField());
    			iStructRef.setType(actualType);
    			
    			return iStructRef;
    			// TODO the template layout for this and ARRAY_SUBSCRIPT are
    			// both a bit convoluted for historical reasons.
    			// should be straightforward to tidy up.
	        }
	        case ARRAY_EXPRESSION: {
	            ArrayInitializer array = (ArrayInitializer) expression;
	            IArrayExpression iArray = new IArrayExpression();
	            Type elemType = null;
    			for (int i = 0; i < array.getItems().size(); i++) {
    				Expression expr = array.getItems().get(i);
    				IExpression iItem = expressionToKarajan(expr, container, false, null, 
    				    expectedType == null ? null : expectedType.itemType());
    				Type newType = iItem.getType();
    				if (i == 0) {
    					elemType = newType;
    				}
    				else if (!elemType.equals(newType)) {
    					throw new CompilationException("Heterogeneous arrays are not supported");
    				}
    				iArray.addItem(iItem);
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
    			// <elemType>[int]
    			iArray.setType(elemType.arrayType(Types.INT));
    		    iArray.setFieldName(addInternedField("$arrayexpr", elemType.arrayType()));
    			return iArray;
	        }
	        case RANGE_EXPRESSION: {
    			ArrayInitializer ai = (ArrayInitializer) expression;
    			ArrayInitializer.Range range = ai.getRange();
    			
    			IRangeExpression iRange = new IRangeExpression();
    			
    			IExpression iFrom = expressionToKarajan(range.getFrom(), container);
    			IExpression iTo = expressionToKarajan(range.getTo(), container);
    			iRange.setFrom(iFrom);
    			iRange.setTo(iTo);
                
    			IExpression iStep = null;
    			if (range.getStep() != null) { // step is optional
    				iStep = expressionToKarajan(range.getStep(), container);
    				iRange.setStep(iStep);
    			}
    
    			Type fromType = iFrom.getType();
    			Type toType = iTo.getType();
    			if (!fromType.equals(toType)) {
    			    throw new CompilationException("To and from range values must have the same type");
                }
    			if (iStep != null && !iStep.getType().equals(fromType)) {
    			    throw new CompilationException("Step (" + iStep.getType() + 
    			            ") must be of the same type as from and to (" + toType + ")");
    			}
    			if (iStep == null && (!fromType.equals(Types.INT) || !toType.equals(Types.INT))) {
    				throw new CompilationException("Step in range specification can be omitted only when from and to types are int");
    			}
    			else if (fromType.equals(Types.INT) && toType.equals(Types.INT)) {
    			    // int[int]
    			    iRange.setType(Types.INT.arrayType(Types.INT));
    			}
    			else if (fromType.equals(Types.FLOAT) && toType.equals(Types.FLOAT) &&
    					iStep.getType().equals(Types.FLOAT)) {
    			    // float[int]
                    iRange.setType(Types.FLOAT.arrayType(Types.INT));
    			}
    			else {
    				throw new CompilationException("Range can only be specified with numeric types");
    			}
    			return iRange;
	        }
	        case FUNCTION_EXPRESSION: {
	            FunctionInvocation f = (FunctionInvocation) expression;
	            return functionExpr(f, container, expectedType);
	        }
	        case CALL_EXPRESSION: {
	            Call c = (Call) expression;
	            String name = c.getName();
	            ActualParameters actual = getActualParameters(c, container);
    		    if (expectedType == null) {
    		        actual.addReturn(null, new IActualParameter(null, Types.ANY));
    		    }
    		    else {
    		        actual.addReturn(null, new IActualParameter(null, expectedType));
    		    }
    		    if (functionsMap.isDefined(name)) {
    		        Signature sig = functionsMap.find(name, actual, true);
    		        if (sig != null) {
        		        if (sig.isProcedure()) {
        		            return callExpr(c, container, expectedType, sig, actual, lvalue);
        		        }
        		        else {
        		            return functionExpr(c, container, sig, actual);
        		        }
    		        }
    		    }
                throw noFunctionOrProcedure(name, actual, true);
	        }
	        case STRUCT_EXPRESSION: {
    		    StructInitializer s = (StructInitializer) expression;
    		    // distringuish between a struct or sparse array
    		    // expr by looking at the keys. If they are identifiers,
    		    // then struct, if expressions
    		    // if there are no kv pairs, then this is a sparse array expression
    		    boolean sparseArray;
    		    if (s.getFieldInitializers().size() == 0) {
    		        sparseArray = true;
    		    }
    		    else {
        		    FieldInitializer f = s.getFieldInitializers().get(0);
        		    if (f.getKey().getExpressionType() == Expression.Type.VARIABLE_REFERENCE) {
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
    		        return sparseArrayInitializer(s, container, lvalue, expectedType);
    		    }
    		    else {
    		        return structInitializer(s, container, lvalue, expectedType);
    		    }
    		}
	        default:
	            throw new CompilationException("unknown expression implemented by class " + 
	                expression.getClass() + " with type " + type + 
	                " and with content " + expression);
		}
		// perhaps one big throw catch block surrounding body of this method
		// which shows Compiler Exception and line number of error
	}

    private boolean isWrapped(VariableScope scope, String name, INode iDecl) {
        if (scope.getVariableOrigin(name) == VariableScope.VariableOrigin.AUTO) {
            return false;
        }
        else if (iDecl instanceof IFormalParameter) {
            IFormalParameter iParam = (IFormalParameter) iDecl;
            if (iParam.getProcedure().isApplication()) {
                return false;
            }
            else {
                if (iParam.isReturn()) {
                    return false;
                }
                else {
                    return true;
                }
            }
        }
        else {
            return true;
        }
    }

    private IUnaryOperator unaryOperator(IUnaryOperator.OperatorType type, UnaryOperator op, IStatementContainer container) 
            throws CompilationException {
        
        IUnaryOperator iOp = new IUnaryOperator(type, expressionToKarajan(op.getArg(), container));
        return iOp;
    }

    private IBinaryOperator binaryOperator(BinaryOperator binop, IStatementContainer container) throws CompilationException {
        IExpression iLeft = expressionToKarajan(binop.getLhs(), container);
        IExpression iRight = expressionToKarajan(binop.getRhs(), container);
        
        IBinaryOperator iOp = new IBinaryOperator(binop.getType().getOperator(), iLeft, iRight);
        
        return iOp;
    }

    private IExpression simpleBinaryOp(BinaryOperator o, IStatementContainer container, 
            String opName, Type paramTypes, Type retType) throws CompilationException {
        IBinaryOperator iOp = new IBinaryOperator();
        iOp.setOp(opName);
        
        IExpression iLeft = expressionToKarajan(o.getLhs(), container);
        IExpression iRight = expressionToKarajan(o.getRhs(), container);
        
        iOp.setLeft(iLeft);
        iOp.setRight(iRight);
        if (iLeft.getType().equals(paramTypes) && iRight.getType().equals(paramTypes)) {
            iOp.setType(retType);
        }
        else {
            throw new CompilationException(o.getExpressionType() + 
                " operation can only be applied to parameters of type " + paramTypes);
        }
        return iOp;
    }

    private IStructExpression structInitializer(StructInitializer s, IStatementContainer container, ILValue lvalue, Type expectedType) 
            throws CompilationException {
        IStructExpression iStruct = new IStructExpression();
        if (expectedType == null) {
            throw new CompilationException("Cannot infer destination type in structure initializer");
        }
        if (lvalue != null) {
            iStruct.setVar(lvalue);
        }
        else {
            iStruct.setFieldName(addInternedField("$structexpr", expectedType));
        }
        if (!expectedType.isComposite() || expectedType.isArray()) {
            throw new CompilationException("Cannot assign a structure to a non-structure");
        }
        Set<String> seen = new HashSet<String>();
        for (FieldInitializer fi : s.getFieldInitializers()) {
            iStruct.addItem(structField(fi, container, expectedType, seen));
        }
        if (expectedType.getFields().size() != seen.size()) {
            throw new CompilationException("Missing fields in structure initializer: " + getMissingFields(seen, expectedType));
        }
        iStruct.setType(expectedType);
        
        if (lvalue != null) {
            container.addStatement(iStruct);
            return null;
        }
        else {
            return iStruct;
        }
    }

    private IField structField(FieldInitializer field, IStatementContainer container, Type expectedType, Set<String> seen) 
            throws CompilationException {
        Expression xkey = field.getKey();
        if (xkey.getExpressionType() != Expression.Type.VARIABLE_REFERENCE) {
            // TODO better error message
            throw new CompilationException("Invalid field name " + xkey.getNodeName());
        }
        VariableReference var = (VariableReference) xkey;
        String name = var.getName();
        if (seen.contains(name)) {
            throw new CompilationException("Duplicate field: '" + name + "'");
        }
        seen.add(name);
        
        IField iField = new IField();
        iField.setKey(name);
        try {
            iField.setValue(expressionToKarajan(field.getValue(), 
                container, false, null, expectedType.getField(name).getType()));
        }
        catch (NoSuchFieldException e) {
            throw new CompilationException("Invalid field '" + name + "' for type '" + expectedType + "'");
        }
        return iField;
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

    private IExpression sparseArrayInitializer(StructInitializer s, IStatementContainer container, ILValue lvalue, Type expectedType) 
            throws CompilationException {
        ISparseArrayExpression iArray = new ISparseArrayExpression();
        if (lvalue != null) {
            iArray.setVar(lvalue);
        }
        else {
            if (expectedType == null) {
                throw new CompilationException("Could not infer type in sparse array initializer");
            }
            iArray.setFieldName(addInternedField("$arrayexpr", expectedType));
        }
        if (!expectedType.isArray()) {
            throw new CompilationException("Cannot assign an array to a non-array");
        }
        for (FieldInitializer fi : s.getFieldInitializers()) {
            iArray.addItem(sparseArrayField(fi, container, expectedType));
        }
        iArray.setType(expectedType);
        if (lvalue != null) {
            container.addStatement(iArray);
            return null;
        }
        else {
            return iArray;
        }
    }
    
    private IField sparseArrayField(FieldInitializer field, IStatementContainer container, Type expectedType) 
            throws CompilationException {
        Type keyType = expectedType.keyType();
        Type itemType = expectedType.itemType();
        
        IField iField = new IField();
        iField.setKey(expressionToKarajan(field.getKey(), container, false, null, keyType));
        iField.setValue(expressionToKarajan(field.getValue(), container, false, null, itemType));
        return iField;
    }

    private String getRootVar(IExpression iExpr) throws CompilationException {
        if (iExpr instanceof IVariableReference) {
            return ((IVariableReference) iExpr).getName();
        }
        if (iExpr instanceof IArrayReference) {
            return getRootVar(((IArrayReference) iExpr).getArray());
        }
        if (iExpr instanceof IStructReference) {
            return getRootVar(((IStructReference) iExpr).getStruct());
        }
        throw new CompilationException("Could not get variable name " + iExpr);
    }

    private IExpression callExpr(Call c, IStatementContainer container, Type expectedType, Signature sig, 
            ActualParameters actual, ILValue lvalue) 
            throws CompilationException {
        ReturnParameter ret = new ReturnParameter();
        c.addReturn(ret);
        VariableScope subscope = new VariableScope(this, container.getScope(), c);
        VariableReference ref = new VariableReference("swift.callintermediate");
        ret.setLValue(ref);

        if (sig.getReturns().size() != 1) {
            throw new CompilationException("Procedure '" + sig.getName() + "' must have exactly one " +
                    "return value to be used in an expression.");
        }
        
        ICallExpression iCall = new ICallExpression(subscope);
        if (lvalue != null) {
            iCall.addReturn(new IActualParameter(lvalue, lvalue.getType()));
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
            iCall.setMapping(true);
        }
        
        subscope.addInternalVariable("swift.callintermediate", type, null);
        
        actual.setReturn(0, actualReturn(c.getReturns().get(0), iCall));
        iCall.setType(type);
        iCall.setFieldName(addInternedField("swift.callintermediate", type));
        iCall.setCall(call(c, iCall, sig, actual, true));
        if (!isPrimitiveOrArrayOfPrimitives(type)) {
            iCall.setPrefix(UUIDGenerator.getInstance().generateRandomBasedUUID().toString());
        }
        return iCall;
    }
    
    private IFunctionCall functionExpr(Call c, IStatementContainer container, Signature sig, ActualParameters actual) 
            throws CompilationException {
        IFunctionCall iCall = function(c, container, sig, actual);
        /* Set function output type */
        /* Functions have only one output parameter */
        iCall.setType(sig.getReturnType());
        if (sig.isDeprecated()) {
            Warnings.warn(Warnings.Type.DEPRECATION, 
                c, "Function " + sig.getName() + " is deprecated");
        }
        
        return iCall;
    }

    private IFunctionCall functionExpr(FunctionInvocation f, IStatementContainer container, Type expectedType) 
            throws CompilationException {
        
        String name = f.getName();
        if (name.equals("")) {
            name = "filename";
        }
        else {
            // the @var shortcut for filename(var) is not deprecated
            Warnings.warn(Warnings.Type.DEPRECATION, 
                "The @ syntax for function invocation is deprecated");
        }
        
        ActualParameters actual = getActualParameters(f, container, expectedType);
        Signature funcSignature = functionsMap.find(name, actual, true);
        if (funcSignature == null) {
            throw noFunctionOrProcedure(name, actual, true);
        }
        IFunctionCall iFn = function(f, container, funcSignature, actual);
        /* Set function output type */    
        /* Functions have only one output parameter */
        iFn.setType(funcSignature.getReturnType());
        
        if (funcSignature.isDeprecated()) {
            Warnings.warn(Warnings.Type.DEPRECATION, 
                f, "Function " + name + " is deprecated");
        }
    
        return iFn;
    }
    
    /**
     * Translates a call(output, params) into a output = function(params) form.
     * @param actualParams 
     * @param proc 
     */
    private IAssignment functionAsCall(Call call, IStatementContainer container, Signature proc, 
            ActualParameters actualParams, Type expectedType) throws CompilationException {
        
        String name = call.getName();
        if (call.getReturns().isEmpty()) {
            throw new CompilationException("Call to a function that does not return a value");
        }
        if (call.getReturns().size() > 1) {
            throw new CompilationException("Cannot assign multiple values with a function invocation");
        }
        
        IFunctionCall iCall = function(call, name, getCallParams(call), container, expectedType);
        
        iCall.setType(proc.getReturnType());
        IAssignment iAssign = assignFromCallReturn(call, iCall, container);
        
        if (proc.isDeprecated()) {
            Warnings.warn(Warnings.Type.DEPRECATION, call, "Function " + name + " is deprecated");
        }
    
        return iAssign;
    }

    private List<ActualParameter> getCallParams(Call call) {
        return call.getParameters();
    }

    private IAssignment assignFromCallReturn(Call call, IFunctionCall iCall, IStatementContainer container) 
            throws CompilationException {
        IAssignment iAssign = new IAssignment();
        LValue var = call.getReturns().get(0).getLValue();
        IExpression iVar = expressionToKarajan(var, container, true, null);
        if (iVar instanceof ILValue) {
            iAssign.setLValue((ILValue) iVar);
        }
        else {
            throw new CompilationException("Expected an lvalue");
        }
        if (!iVar.getType().equals(iCall.getType())) {
            throw new CompilationException("You cannot assign value of type " + iCall.getType() +
                    " to a variable of type " + iVar.getType());
        }
        iAssign.setRValue(iCall);
        iAssign.setLine(call.getLine());
        addWriterToScope(container.getScope(), var, iVar, call, iAssign);
        return iAssign;
    }


    // TODO: this ad-hoc type checking bothers me
    void checkTypesInCondExpr(String op, Type left, Type right, IExpression iExpr)
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
		    iExpr.setType(Types.BOOLEAN);
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

	void checkTypesInArithmExpr(String op, Type left, Type right, IExpression iOp)
			throws CompilationException {
	    /* 
	     * +, -, * : int, int -> int
	     * / : int, int -> float
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
	            if (left.equals(Types.STRING) || right.equals(Types.STRING)) {
	                iOp.setType(Types.STRING);
	            }
	            else {
	                checkTypesInType1ArithmExpr(op, left, right, iOp, false);
	            }
	            break;
	        case '/':
	            checkTypesInType1ArithmExpr(op, left, right, iOp, true);
	            break;
            case '-':
	        case '*':
	            checkTypesInType1ArithmExpr(op, left, right, iOp, false);
	            break;
	        case '%':
	            checkTypesInType2ArithmExpr(op, left, right, iOp);
	            break;
	        default:
	            throw new CompilationException("Unknown operator '" + op + "'");
	    }
	}

	private void checkTypesInType1ArithmExpr(String op, Type left, Type right, IExpression iOp, boolean forceFloatReturn) 
	        throws CompilationException {
	    /* 
         * int, int -> int
         * float, float -> float
         * int, float -> float
         * float, int -> float
         */
	    if (left.equals(Types.INT)) {
	        if (right.equals(Types.INT)) {
	            if (forceFloatReturn) {
	                iOp.setType(Types.FLOAT);
	            }
	            else {
	                iOp.setType(Types.INT);
	            }
	            return;
	        }
	        else if (right.equals(Types.FLOAT)) {
	            iOp.setType(Types.FLOAT);
	            return;
	        }
	    }
	    else if (left.equals(Types.FLOAT)) {
	        if (right.equals(Types.INT) || right.equals(Types.FLOAT)) {
	            iOp.setType(Types.FLOAT);
	            return;
            }
	    }
	    throw new CompilationException("Operator '" + op + "' cannot be applied to '" + left + "' and '" + right + "'");
    }

    private void checkTypesInType2ArithmExpr(String op, Type left, Type right, IExpression iOp) 
            throws CompilationException {
        if (!left.equals(Types.INT) || !right.equals(Types.INT)) {
            throw new CompilationException("Operator '" + op + "' can only be applied to 'int' and 'int'");
        }
        iOp.setType(Types.INT);
    }

    public String abstractExpressionToRootVariable(Expression expr) throws CompilationException {
		Expression.Type type = expr.getExpressionType();
		if (type == Expression.Type.VARIABLE_REFERENCE) {
		    return ((VariableReference) expr).getName();
		} 
		else if (type == Expression.Type.ARRAY_SUBSCRIPT_EXPRESSION) {
			return abstractExpressionToRootVariable(((ArrayReference) expr).getBase());
		} 
		else if (type == Expression.Type.STRUCT_MEMBER_REFERENCE) {
			return abstractExpressionToRootVariable(((StructReference) expr).getBase());
		} 
		else {
			throw new CompilationException("Could not find root for abstract expression.");
		}
	}

	public WriteType getRootVariableWriteType(Expression expr) {
		Expression.Type type = expr.getExpressionType();
		if (type == Expression.Type.VARIABLE_REFERENCE) {
			return WriteType.FULL;
		} 
		else if (type == Expression.Type.ARRAY_SUBSCRIPT_EXPRESSION) {
			return WriteType.PARTIAL;
		} 
		else if (type == Expression.Type.STRUCT_MEMBER_REFERENCE) {
			return WriteType.PARTIAL;
		} 
		else {
			throw new RuntimeException("Could not find root for abstract expression.");
		}
	}
	
	public void generateInternedFields(IProgram iProgram) {
	    for (Map.Entry<InternedField, String> e : usedFields.entrySet()) {
	        IFieldConstant constant = new IFieldConstant(e.getValue(), e.getKey().name, e.getKey().type);
            iProgram.addConstant(constant);
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

    public void generateInternedConstants(IProgram iProgram) {
	    generateInternedConstants(iProgram, stringInternMap, Types.STRING, "sConst");
	    generateInternedConstants(iProgram, intInternMap, Types.INT, "iConst");
	    generateInternedConstants(iProgram, floatInternMap, Types.FLOAT, "fConst");
	}

	
	private void generateInternedConstants(IProgram iProgram, Map<?, String> map, Type type, String cTemplate) {
	    for (Object key : map.keySet()) {
            String variableName = map.get(key);
            
            IValue value = new IValue();
            value.setType(type);
            value.setValueRepr(toKarajanValue(key));
            
            IGlobalConstant gconstant = new IGlobalConstant();
            gconstant.setName(variableName);
            gconstant.setValue(value);
            
            iProgram.addConstant(gconstant);
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
