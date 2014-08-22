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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.globus.swift.language.FormalParameter;


public class ProcedureSignature {

	private String name;
	private List<FormalArgumentSignature> inputArgs;
	private List<FormalArgumentSignature> outputArgs;
	private boolean anyNumOfInputArgs;
	private boolean anyNumOfOutputArgs; /* this is maybe unnecessary*/
	private int invocationMode;
	
	private boolean deprecated;

	/* Procedure is built in to Swift. */
	static public final int INVOCATION_INTERNAL = 600;

	/* Procedure is user defined. */
	static public final int INVOCATION_USERDEFINED = 601;

	public ProcedureSignature(String name) {
		this.name = name;
		inputArgs = new ArrayList<FormalArgumentSignature>();
		outputArgs = new ArrayList<FormalArgumentSignature>();
		anyNumOfInputArgs = false;
		anyNumOfOutputArgs = false;
		invocationMode = INVOCATION_USERDEFINED;
	}

	public String getName() {
		return name;
	}

	public void addInputArg(FormalArgumentSignature inputArg) {
		inputArgs.add(inputArg);
	}

	public void addOutputArg(FormalArgumentSignature outputArg) {
		outputArgs.add(outputArg);
	}

	public void setAnyNumOfInputArgs() {
		anyNumOfInputArgs = true;
	}

	public void setAnyNumOfOutputArgs() {
		anyNumOfOutputArgs = true;
	}

	public boolean getAnyNumOfInputArgs() {
		return anyNumOfInputArgs;
	}

	public boolean getAnyNumOfOutputArgs() {
		return anyNumOfOutputArgs;
	}

	public int sizeOfInputArray() {
		return inputArgs.size();
	}

	public int sizeOfOutputArray() {
		return outputArgs.size();
	}

	private static final FormalArgumentSignature[] FORMAL_ARGUMENT_SIGNATURE_ARRAY =
	    new FormalArgumentSignature[0];

	public FormalArgumentSignature[] getInputArray() {
		return inputArgs.toArray(FORMAL_ARGUMENT_SIGNATURE_ARRAY);
	}

	public FormalArgumentSignature[] getOutputArray() {
		return outputArgs.toArray(FORMAL_ARGUMENT_SIGNATURE_ARRAY);
	}

	public FormalArgumentSignature getInputArray(int i) {
		return inputArgs.get(i);
	}

	public FormalArgumentSignature getOutputArray(int i) {
		return outputArgs.get(i);
	}

	public void setInputArgs(FormalParameter[] fp) {
		for (int i = 0; i < fp.length; i++) {
			FormalArgumentSignature fas = 
			    new FormalArgumentSignature(fp[i].getType().getLocalPart(),
			        fp[i].getName());
			fas.setOptional(!fp[i].isNil());
			this.addInputArg(fas);
		}
	}

	public void setOutputArgs(FormalParameter[] fp) {
		for (int i = 0; i < fp.length; i++) {
			FormalArgumentSignature fas = new FormalArgumentSignature(fp[i].getType().getLocalPart(),
					                                                  fp[i].getName());
			/* fas.setOptional(!fp[i].isNil()); */
			/* unnecessary because output arg can not be optional */
			this.addOutputArg(fas);
		}
	}

	public void setInvocationMode(int i) {
		this.invocationMode = i;
	}

	public int getInvocationMode() {
		return this.invocationMode;
	}
	
	public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public static final String STRING = "string";
    public static final String STRING_ARRAY = "string[]";
    public static final String INT = "int";
    public static final String FLOAT = "float";
    public static final String BOOLEAN = "boolean";
    public static final String ANY = "any";
    public static final String VARGS = "-vargs";

	public static Map<String,ProcedureSignature>
	makeProcedureSignatures() {
		Map<String,ProcedureSignature> proceduresMap = 
		    new HashMap<String,ProcedureSignature>();
		
		add(proceduresMap, "readData", returns(ANY), args(ANY));
		add(proceduresMap, "readData2", returns(ANY), args(ANY));
		add(proceduresMap, "readStructured", returns(ANY), args(ANY));
		add(proceduresMap, "trace", returns(), args(VARGS));
		add(proceduresMap, "tracef", returns(), args(VARGS));
		add(proceduresMap, "printf", returns(), args(VARGS));
		add(proceduresMap, "fprintf", returns(), args(VARGS));
		add(proceduresMap, "assert", returns(), args(VARGS));
		add(proceduresMap, "writeData", returns(ANY), args(ANY));
		
		// backwards compatible; to be removed in the future
		addDeprecated(proceduresMap, "readdata", returns(ANY), args(ANY));
        addDeprecated(proceduresMap, "readdata2", returns(ANY), args(ANY));
        addDeprecated(proceduresMap, "readstructured", returns(ANY), args(ANY));
        addDeprecated(proceduresMap, "writedata", returns(ANY), args(ANY));
		
		for (Map.Entry<String, ProcedureSignature> e : proceduresMap.entrySet()) {
		    e.getValue().setInvocationMode(INVOCATION_INTERNAL);
		}

		return proceduresMap;
	}

	public static Map<String,ProcedureSignature> 
	makeFunctionSignatures() {
		Map<String,ProcedureSignature> functionsMap = 
		    new HashMap<String,ProcedureSignature>();

		add(functionsMap, "arg", returns(STRING), args(STRING, optional(STRING)));

		add(functionsMap, "extractInt", returns(INT), args(ANY));
		add(functionsMap, "extractFloat", returns(FLOAT), args(ANY));
		add(functionsMap, "filename", returns(STRING), args(ANY));
		add(functionsMap, "filenames", returns(STRING_ARRAY), args(ANY));
		add(functionsMap, "dirname", returns(STRING), args(ANY));
		add(functionsMap, "length", returns(INT), args(ANY));
		
		add(functionsMap, "regexp", returns(STRING), args(STRING, STRING, STRING));
		add(functionsMap, "strcat", returns(STRING), args(VARGS));
		add(functionsMap, "sprintf", returns(STRING), args(VARGS));
		add(functionsMap, "strcut", returns(STRING), args(STRING, STRING));
		add(functionsMap, "strstr", returns(INT), args(STRING, STRING));
		add(functionsMap, "strsplit", returns(STRING_ARRAY), args(STRING, STRING));
		add(functionsMap, "system", returns(STRING_ARRAY), args(STRING));
		add(functionsMap, "strjoin", returns(STRING), args(ANY, STRING));
		add(functionsMap, "toInt", returns(INT), args(ANY));
		add(functionsMap, "toFloat", returns(FLOAT), args(ANY));
		add(functionsMap, "toString", returns(STRING), args(ANY));
		
		add(functionsMap, "format", returns(STRING), args(VARGS));
		add(functionsMap, "pad", returns(STRING), args(INT, INT));
		
		add(functionsMap, "java", returns(ANY), args(VARGS));

		add(functionsMap, "exists", returns(BOOLEAN), args(VARGS));
		
		
		// backwards compatible and deprecated lower case version
		addDeprecated(functionsMap, "extractint", returns(INT), args(ANY));
		addDeprecated(functionsMap, "toint", returns(INT), args(ANY));
        addDeprecated(functionsMap, "tofloat", returns(FLOAT), args(ANY));
        addDeprecated(functionsMap, "tostring", returns(STRING), args(ANY));

		return functionsMap;
	}
	
	public static final FormalArgumentSignature VARG_SIG = new FormalArgumentSignature(true);

	private static void addDeprecated(Map<String, ProcedureSignature> map, 
            String name, FormalArgumentSignature[] returns, FormalArgumentSignature[] args) {
	    ProcedureSignature sig = buildSig(name, returns, args);
	    sig.setDeprecated(true);
	    map.put(name, sig);
	}
	
	private static void add(Map<String, ProcedureSignature> map, 
	        String name, FormalArgumentSignature[] returns, FormalArgumentSignature[] args) {
	    map.put(name, buildSig(name, returns, args));
    }

    private static ProcedureSignature buildSig(String name,
            FormalArgumentSignature[] returns, FormalArgumentSignature[] args) {
        ProcedureSignature sig = new ProcedureSignature(name);
        for (FormalArgumentSignature arg : args) {
            if (arg == VARG_SIG) {
                sig.setAnyNumOfInputArgs();
            }
            else {
                sig.addInputArg(arg);
            }
        }
        for (FormalArgumentSignature ret : returns) {
            sig.addOutputArg(ret);
        }
        return sig;
    }

    private static FormalArgumentSignature arg(String type) {
        if (type.equals(ANY)) {
            return new FormalArgumentSignature(true);
        }
        else if (type.equals(VARGS)) {
            return VARG_SIG;
        }
        else {
            return new FormalArgumentSignature(type);
        }
    }

    private static FormalArgumentSignature[] returns(Object ... returns) {
        return buildArgs(returns);
    }

    private static FormalArgumentSignature[] args(Object ... args) {
        return buildArgs(args);
    }

    private static FormalArgumentSignature[] buildArgs(Object[] args) {
        FormalArgumentSignature[] r = new FormalArgumentSignature[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof FormalArgumentSignature) {
                r[i] = (FormalArgumentSignature) args[i];
            }
            else {
                r[i] = arg((String) args[i]);
            }
        }
        return r;
    }
    
    private static FormalArgumentSignature optional(String type) {
        FormalArgumentSignature arg = arg(type);
        arg.setOptional(true);
        return arg;
    }

    public String toString() {
	    return outputArgs + " " + name + inputArgs;
	}
}
