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

import static org.griphyn.vdl.type.Types.ANY;
import static org.griphyn.vdl.type.Types.BOOLEAN;
import static org.griphyn.vdl.type.Types.FLOAT;
import static org.griphyn.vdl.type.Types.INT;
import static org.griphyn.vdl.type.Types.STRING;

import java.util.HashMap;
import java.util.Map;

import org.globus.cog.karajan.compiled.nodes.Node;
import org.griphyn.vdl.type.Type;

public abstract class StandardLibrary {
    public static final StandardLibrary LEGACY = new StandardLibrary.Legacy();
    public static final StandardLibrary NEW = new StandardLibrary.New();
            
    protected FunctionsMap functionsMap;
    private Map<Signature, Class<? extends Node>> defs;
    private Map<String, Object> constants;
    
    private StandardLibrary() {
        functionsMap = new FunctionsMap();
        defs = new HashMap<Signature, Class<? extends Node>>();
        constants = new HashMap<String, Object>();
        makeProcedureSignatures();
        makeFunctionSignatures();
        makeConstants();
    }
    
    protected abstract void makeFunctionSignatures();
    protected abstract void makeProcedureSignatures();
    protected abstract void makeConstants();
    
    public Map<Signature, Class<? extends Node>> getDefs() {
        return defs;
    }
    
    public Map<String, Object> getConstants() {
        return constants;
    }
    
    public FunctionsMap getFunctionSignatures() {
        return functionsMap;
    }
        
    private void addDef(Signature sig, Class<? extends Node> cls) {
        defs.put(sig, cls);
    }
    
    protected void addConstant(String name, Object value) {
        constants.put(name, value);
    }

    protected void addDeprecated(Map<String, Signature> map, 
            String name, Signature.Parameter[] returns, Signature.Parameter[] args) {
        Signature sig = buildSig(name, null, returns, args);
        sig.setDeprecated(true);
        map.put(name, sig);
    }
    
    protected Signature addDeprecated(FunctionsMap map, String name, 
            Signature.Parameter[] returns, Signature.Parameter[] args, Class<? extends Node> cls) {
        Signature sig = buildSig(name, null, returns, args);
        sig.setDeprecated(true);
        map.addInternalFunction(name, sig);
        addDef(sig, cls);
        return sig;
    }
    
    protected Signature add(FunctionsMap map, String name, TypeParameter[] typeParams, 
            Signature.Parameter[] returns, Signature.Parameter[] args, 
            Class<? extends Node> cls) {
        Signature sig = buildSig(name, typeParams, returns, args);
        map.addInternalFunction(name, sig);
        addDef(sig, cls);
        return sig;
    }
    
    protected void addProc(String name, Signature.Parameter[] returns, 
            Signature.Parameter[] args, Class<? extends Node> cls) {
        Signature ps = add(functionsMap, name, null, returns, args, cls);
        ps.setIsProcedure(true);
        ps.setType(Signature.InvocationType.INTERNAL);
    }
        
    protected void addDeprecatedProc(String name, Signature.Parameter[] returns, 
            Signature.Parameter[] args, Class<? extends Node> cls) {
        Signature ps = addDeprecated(functionsMap, name, returns, args, cls);
        ps.setIsProcedure(true);
        ps.setType(Signature.InvocationType.INTERNAL);
    }
    
    protected void addFunc(String name, Signature.Parameter[] returns, 
            Signature.Parameter[] args, Class<? extends Node> cls) {
        addFunc(name, null, returns, args, cls);
    }
    
    protected void addFunc(String name, TypeParameter[] typeParams, Signature.Parameter[] returns, 
            Signature.Parameter[] args, Class<? extends Node> cls) {
        Signature ps = add(functionsMap, name, typeParams, returns, args, cls);
        ps.setIsProcedure(false);
    }
    
    protected void addDeprecatedFunc(String name, Signature.Parameter[] returns, 
            Signature.Parameter[] args, Class<? extends Node> cls) {
        Signature ps = addDeprecated(functionsMap, name, returns, args, cls);
        ps.setIsProcedure(false);
    }
    
    private static Signature buildSig(String name, TypeParameter[] typeParams,
            Signature.Parameter[] returns, Signature.Parameter[] args) {
        Signature sig = new Signature(name);
        if (typeParams != null) {
            sig.setTypeParameters(typeParams);
        }
        for (Signature.Parameter arg : args) {
            if ("...".equals(arg.getName())) {
                sig.setVarArgs(true);
                sig.setVarArgsType(arg.getType());
            }
            else {
                sig.addInputArg(arg);
            }
        }
        for (Signature.Parameter ret : returns) {
            sig.addOutputArg(ret);
        }
        return sig;
    }
    
    private static Signature.Parameter vargs(Type type) {
        return new Signature.Parameter(type, "...");
    }

    private static Signature.Parameter arg(Type type) {
        return new Signature.Parameter(type);
    }
    
    private static Signature.Parameter arg(Type type, String name) {
        return new Signature.Parameter(type, name);
    }
    
    private static String arrayType(String itemType, String keyType) {
        return itemType + "[" + keyType + "]";
    }
    
    private static TypeParameter typeArg(String name) {
        return new TypeParameter(name);
    }


    private static Signature.Parameter[] returns(Object ... returns) {
        return buildArgs(returns);
    }

    private static Signature.Parameter[] args(Object ... args) {
        return buildArgs(args);
    }
    
    private static TypeParameter[] typeArgs(Object ... args) {
        return buildTypeArgs(args);
    }

    private static Signature.Parameter[] buildArgs(Object[] args) {
        Signature.Parameter[] r = new Signature.Parameter[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Signature.Parameter) {
                r[i] = (Signature.Parameter) args[i];
            }
            else {
                r[i] = arg((Type) args[i]);
            }
        }
        return r;
    }
    
    private static TypeParameter[] buildTypeArgs(Object[] args) {
        TypeParameter[] r = new TypeParameter[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof TypeParameter) {
                r[i] = (TypeParameter) args[i];
            }
            else {
                r[i] = typeArg((String) args[i]);
            }
        }
        return r;
    }
    
    private static Signature.Parameter optional(Type type) {
        Signature.Parameter arg = arg(type);
        arg.setOptional(true);
        return arg;
    }
    
    private static Signature.Parameter optional(Type type, String name) {
        Signature.Parameter arg = arg(type, name);
        arg.setOptional(true);
        return arg;
    }

    
    public static class Legacy extends StandardLibrary {
        @Override
        protected void makeConstants() {
            // no constants here
        }

        protected void makeProcedureSignatures() {
            Map<String,Signature> proceduresMap = new HashMap<String,Signature>();
            
            addProc("readData", returns(ANY), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.ReadData.class);
            addProc("readData2", returns(ANY), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.ReadStructured.class);
            addProc("readStructured", returns(ANY), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.ReadStructured.class);
            addProc("writeData", returns(ANY), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.WriteData.class);
            
            addProc("trace", returns(), args(vargs(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.Trace.class);
            addProc("tracef", returns(), args(vargs(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.Tracef.class);
            addProc("printf", returns(), args(vargs(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.Printf.class);
            addProc("fprintf", returns(), args(vargs(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.Fprintf.class);
            
            addProc("assert", returns(), args(BOOLEAN),
                org.griphyn.vdl.karajan.lib.swiftscript.Assert.class);
            addProc("assert", returns(), args(BOOLEAN, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.Assert.class);
            addProc("assert", returns(), args(INT),
                org.griphyn.vdl.karajan.lib.swiftscript.Assert.class);
            addProc("assert", returns(), args(INT, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.Assert.class);
            
            // backwards compatible; to be removed in the future
            addDeprecatedProc("readdata", returns(ANY), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.ReadData.class);
            addDeprecatedProc("readdata2", returns(ANY), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.ReadStructured.class);
            addDeprecatedProc("readstructured", returns(ANY), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.ReadStructured.class);
            addDeprecatedProc("writedata", returns(ANY), args(ANY), 
                org.griphyn.vdl.karajan.lib.swiftscript.WriteData.class);
            
            for (Map.Entry<String, Signature> e : proceduresMap.entrySet()) {
                e.getValue().setType(Signature.InvocationType.INTERNAL);
            }
        }
        
        protected void makeFunctionSignatures() {    
            addFunc("arg", returns(STRING), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.FnArg.class);
            addFunc("arg", returns(STRING), args(STRING, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.FnArg.class);
            addFunc("args", returns(STRING.arrayType(STRING)), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.FnArgs.class);
    
            addFunc("extractInt", returns(INT), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.ExtractInt.class);
            addFunc("extractFloat", returns(FLOAT), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.ExtractFloat.class);
            
            addFunc("filename", returns(STRING), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.FileName.class);
            addFunc("filenames", returns(STRING.arrayType()), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.FileNames.class);
            addFunc("dirname", returns(STRING), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.Dirname.class);
            
            addFunc("length", returns(INT), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.Length.class);
            
            addFunc("regexp", returns(STRING), args(STRING, STRING, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.Regexp.class);
            addFunc("strcat", returns(STRING), args(vargs(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.StrCat.class);
            addFunc("sprintf", returns(STRING), args(vargs(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.Sprintf.class);
            addFunc("strcut", returns(STRING), args(STRING, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.StrCut.class);
            addFunc("strstr", returns(INT), args(STRING, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.StrStr.class);
            addFunc("strsplit", returns(STRING.arrayType()), args(STRING, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.StrSplit.class);
            
            addFunc("system", returns(STRING.arrayType()), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.ExecSystem.class);
            addFunc("strjoin", returns(STRING), args(ANY, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.StrJoin.class);
            addFunc("toInt", returns(INT), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.ToInt.class);
            addFunc("toFloat", returns(FLOAT), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.ToFloat.class);
            addFunc("toString", returns(STRING), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.ToString.class);
            
            addFunc("format", returns(STRING), args(vargs(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.Format.class);
            addFunc("pad", returns(STRING), args(INT, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.Pad.class);
            
            TypeParameter T = new TypeParameter("T");
            addFunc("java", typeArgs(T), returns(T), args(STRING, STRING, vargs(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.Java.class);
    
            addFunc("exists", returns(BOOLEAN), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.Exists.class);
            
            
            // backwards compatible and deprecated lower case version
            addDeprecatedFunc("extractint", returns(INT), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.ExtractInt.class);
            addDeprecatedFunc("toint", returns(INT), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.ToInt.class);
            addDeprecatedFunc("tofloat", returns(FLOAT), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.ToFloat.class);
            addDeprecatedFunc("tostring", returns(STRING), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.ToString.class);
        }
    }
    
    public static class New extends StandardLibrary {
        @Override
        protected void makeConstants() {
            addConstant("PI", Math.PI);
            addConstant("E", Math.E);
        }
        
        protected void makeProcedureSignatures() {
        	addProc("trace", returns(), args(vargs(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.Trace.class);
        	
        	
        	// ***** I/O *****
        	addProc("read", returns(ANY), args(ANY, optional(STRING, "format"), 
        	    optional(ANY.arrayType(STRING), "options")),
        	    org.griphyn.vdl.karajan.lib.swiftscript.v2.IO.Read.class);
        	addProc("write", returns(ANY), args(ANY, optional(STRING, "format"), 
        	    optional(ANY.arrayType(STRING), "options")),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.IO.Write.class);
        	
        	// ***** Assertions *****
            addProc("assert", returns(), args(BOOLEAN, optional(STRING, "msg")),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Assertions.Assert.class);
            addProc("assertEqual", returns(), args(STRING, STRING, optional(STRING, "msg")),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Assertions.AssertEqual.class);
            addProc("assertEqual", returns(), args(INT, INT, optional(STRING, "msg")),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Assertions.AssertEqual.class);
            addProc("assertEqual", returns(), args(FLOAT, FLOAT, optional(STRING, "msg")),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Assertions.AssertEqualFloat.class);
            addProc("assertAlmostEqual", returns(), args(FLOAT, FLOAT, FLOAT, optional(STRING, "msg")),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Assertions.AssertAlmostEqual.class);
            addProc("assertEqual", returns(), args(BOOLEAN, BOOLEAN, optional(STRING, "msg")),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Assertions.AssertEqual.class);
            addProc("assertLT", returns(), args(INT, INT, optional(STRING, "msg")),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Assertions.AssertLT.class);
            addProc("assertLT", returns(), args(FLOAT, FLOAT, optional(STRING, "msg")),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Assertions.AssertLT.class);
            addProc("assertLTE", returns(), args(INT, INT, optional(STRING, "msg")),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Assertions.AssertLTE.class);
            addProc("assertLTE", returns(), args(FLOAT, FLOAT, optional(STRING, "msg")),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Assertions.AssertLTE.class);
        }
        
        protected void makeFunctionSignatures() {
            // ****** Old Stuff that needs to be here
            addFunc("arg", returns(STRING), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.FnArg.class);
            addFunc("arg", returns(STRING), args(STRING, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.FnArg.class);
            addFunc("args", returns(STRING.arrayType(STRING)), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.FnArgs.class);
            
            addFunc("filename", returns(STRING), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.FileName.class);
            addFunc("filenames", returns(STRING.arrayType()), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.FileNames.class);
            addFunc("dirname", returns(STRING), args(ANY),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.Dirname.class);
            
            // ****** Math ******
            
            // Trig functions
            addFunc("sin", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Sin.class);
            addFunc("cos", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Cos.class);
            addFunc("tan", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Tan.class);
            addFunc("asin", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.ASin.class);
            addFunc("acos", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.ACos.class);
            addFunc("atan", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.ATan.class);
            addFunc("atan2", returns(FLOAT), args(FLOAT, FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.ATan2.class);
            
            // Exponentials/Powers
            addFunc("exp", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Exp.class);
            addFunc("ln", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Ln.class);
            addFunc("log", returns(FLOAT), args(FLOAT, FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Log.class);
            addFunc("log10", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Log10.class);
            addFunc("pow", returns(FLOAT), args(FLOAT, FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Pow.class);
            addFunc("sqrt", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Sqrt.class);
            addFunc("cbrt", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Cbrt.class);
            
            // Rounding
            addFunc("ceil", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Ceil.class);
            addFunc("floor", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Floor.class);
            addFunc("round", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.Round.class);
            
            // Misc.
        	addFunc("min", returns(INT), args(INT, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.MinI.class);
        	addFunc("min", returns(FLOAT), args(FLOAT, FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.MinF.class);
        	addFunc("max", returns(INT), args(INT, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.MaxI.class);
            addFunc("max", returns(FLOAT), args(FLOAT, FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.MaxF.class);
            addFunc("abs", returns(INT), args(INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.AbsI.class);
            addFunc("abs", returns(FLOAT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.AbsF.class);
            addFunc("isNaN", returns(BOOLEAN), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Math.IsNaN.class);
        	
            // ***** Random Numbers *****
            
            addFunc("randomInt", returns(INT), args(INT, INT, INT, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Random.RandomInt.class);
            addFunc("randomFloat", returns(FLOAT), args(INT, INT, FLOAT, FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Random.RandomFloat.class);
            addFunc("randomGaussian", returns(FLOAT), args(INT, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Random.GaussianRandom.class);
            
            // ***** Stats *****
            
            addFunc("sum", returns(INT), args(INT.arrayType(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Stats.SumInt.class);
            addFunc("sum", returns(FLOAT), args(FLOAT.arrayType(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Stats.SumFloat.class);
            addFunc("avg", returns(FLOAT), args(INT.arrayType(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Stats.AvgInt.class);
            addFunc("avg", returns(FLOAT), args(FLOAT.arrayType(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Stats.AvgFloat.class);
            addFunc("moment", returns(FLOAT), args(INT.arrayType(ANY), INT, FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Stats.Moment.class);
            addFunc("moment", returns(FLOAT), args(FLOAT.arrayType(ANY), INT, FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Stats.Moment.class);
            
            // ***** Conversion Functions *****
            addFunc("toInt", returns(INT), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Conversion.ToInt.class);
            addFunc("toFloat", returns(FLOAT), args(INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Conversion.ToFloat.class);
            addFunc("parseInt", returns(INT), args(STRING, optional(INT, "base")),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Conversion.ParseInt.class);
            addFunc("parseFloat", returns(FLOAT), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Conversion.ParseFloat.class);
            addFunc("toString", returns(STRING), args(INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Conversion.IntToString.class);
            addFunc("toString", returns(STRING), args(FLOAT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Conversion.FloatToString.class);
            addFunc("toString", returns(STRING), args(BOOLEAN),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.Conversion.BoolToString.class);
            
            // ***** String Functions *****
            addFunc("strcat", returns(STRING), args(vargs(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.Strcat.class);
            addFunc("length", returns(INT), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.Length.class);
            addFunc("split", returns(STRING.arrayType()), args(STRING, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.Split.class);
            addFunc("split", returns(STRING.arrayType()), args(STRING, STRING, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.Split2.class);
            addFunc("splitRe", returns(STRING.arrayType()), args(STRING, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.SplitRe.class);
            addFunc("splitRe", returns(STRING.arrayType()), args(STRING, STRING, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.SplitRe2.class);
            addFunc("trim", returns(STRING), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.Trim.class);
            addFunc("substring", returns(STRING), args(STRING, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.Substring.class);
            addFunc("substring", returns(STRING), args(STRING, INT, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.Substring2.class);
            addFunc("toUpper", returns(STRING), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.ToUpper.class);
            addFunc("toLower", returns(STRING), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.ToLower.class);
            addFunc("join", returns(STRING), args(STRING.arrayType(), STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.Join.class);
            addFunc("replaceAll", returns(STRING), args(STRING, STRING, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.ReplaceAll.class);
            addFunc("replaceAll", returns(STRING), args(STRING, STRING, STRING, INT, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.ReplaceAll2.class);
            addFunc("replaceAllRe", returns(STRING), args(STRING, STRING, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.ReplaceAllRe.class);
            addFunc("replaceAllRe", returns(STRING), args(STRING, STRING, STRING, INT, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.ReplaceAllRe2.class);
            addFunc("indexOf", returns(INT), args(STRING, STRING, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.IndexOf.class);
            addFunc("indexOf", returns(INT), args(STRING, STRING, INT, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.IndexOf2.class);
            addFunc("lastIndexOf", returns(INT), args(STRING, STRING, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.LastIndexOf.class);
            addFunc("lastIndexOf", returns(INT), args(STRING, STRING, INT, INT),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.LastIndexOf2.class);
            addFunc("matches", returns(BOOLEAN), args(STRING, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.Matches.class);
            addFunc("findAllRe", returns(STRING.arrayType()), args(STRING, STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.StringFunctions.FindAllRe.class);
            
            
            // ***** Arrays *****
            addFunc("length", returns(INT), args(ANY.arrayType(ANY)),
                org.griphyn.vdl.karajan.lib.swiftscript.Misc.Length.class);
            
            // ***** IO *****
            addFunc("getEnv", returns(STRING), args(STRING),
                org.griphyn.vdl.karajan.lib.swiftscript.v2.IO.GetEnv.class);
            
        }
    }
}
