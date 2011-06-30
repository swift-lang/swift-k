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

	public static Map<String,ProcedureSignature>
	makeProcedureSignatures() {
		Map<String,ProcedureSignature> proceduresMap = 
		    new HashMap<String,ProcedureSignature>();

		ProcedureSignature readData = new ProcedureSignature("readData");
		FormalArgumentSignature rdInputArg = new FormalArgumentSignature(true);
		readData.addInputArg(rdInputArg);
		FormalArgumentSignature rdOutputArg = new FormalArgumentSignature(true);
		readData.addOutputArg(rdOutputArg);
		readData.setInvocationMode(INVOCATION_INTERNAL);
		proceduresMap.put("readData", readData);

		ProcedureSignature readData2 = new ProcedureSignature("readData2");
		FormalArgumentSignature rd2InputArg = new FormalArgumentSignature(true);
		readData2.addInputArg(rd2InputArg);
		FormalArgumentSignature rd2OutputArg = new FormalArgumentSignature(true);
		readData2.addOutputArg(rd2OutputArg);
		readData2.setInvocationMode(INVOCATION_INTERNAL);
		proceduresMap.put("readData2", readData2);

		ProcedureSignature readStructured = new ProcedureSignature("readStructured");
		FormalArgumentSignature rStInputArg = new FormalArgumentSignature(true);
		readStructured.addInputArg(rStInputArg);
		FormalArgumentSignature rStOutputArg = new FormalArgumentSignature(true);
		readStructured.addOutputArg(rStOutputArg);
		readStructured.setInvocationMode(INVOCATION_INTERNAL);
		proceduresMap.put("readStructured", readStructured);

		ProcedureSignature trace = new ProcedureSignature("trace");
		trace.setAnyNumOfInputArgs();
		trace.setInvocationMode(INVOCATION_INTERNAL);
		proceduresMap.put("trace", trace);

		ProcedureSignature tracef = new ProcedureSignature("tracef");
		tracef.setAnyNumOfInputArgs();
		tracef.setInvocationMode(INVOCATION_INTERNAL);
		proceduresMap.put("tracef", tracef);

		ProcedureSignature fprintf = new ProcedureSignature("ftracef");
		fprintf.setAnyNumOfInputArgs();
		fprintf.setInvocationMode(INVOCATION_INTERNAL);
		proceduresMap.put("fprintf", fprintf);

		ProcedureSignature assrt = new ProcedureSignature("assert");
		assrt.setAnyNumOfInputArgs();
		assrt.setInvocationMode(INVOCATION_INTERNAL);
		proceduresMap.put("assert", assrt);

		ProcedureSignature writeData = new ProcedureSignature("writeData");
		FormalArgumentSignature wdInputArg = new FormalArgumentSignature(true);
		writeData.addInputArg(wdInputArg);
		FormalArgumentSignature wdOutputArg = new FormalArgumentSignature(true);
		writeData.addOutputArg(wdOutputArg);
		writeData.setInvocationMode(INVOCATION_INTERNAL);
		proceduresMap.put("writeData", writeData);

		return proceduresMap;
	}

	public static Map<String,ProcedureSignature> 
	makeFunctionSignatures() {
		Map<String,ProcedureSignature> functionsMap = 
		    new HashMap<String,ProcedureSignature>();

		ProcedureSignature arg = new ProcedureSignature("arg");
		FormalArgumentSignature argIn1 = new FormalArgumentSignature("string");
		arg.addInputArg(argIn1);
		FormalArgumentSignature argIn2 = new FormalArgumentSignature("string");
		argIn2.setOptional(true);
		arg.addInputArg(argIn2);
		FormalArgumentSignature argOut1 = new FormalArgumentSignature("string");
		arg.addOutputArg(argOut1);
		functionsMap.put(arg.getName(), arg);

		ProcedureSignature extractint = new ProcedureSignature("extractint");
		FormalArgumentSignature extractintIn1 = new FormalArgumentSignature(true); /* file can be specified as any type */
		extractint.addInputArg(extractintIn1);
		FormalArgumentSignature extractintOut1 = new FormalArgumentSignature("int");
		extractint.addOutputArg(extractintOut1);
		functionsMap.put(extractint.getName(), extractint);

		ProcedureSignature filename = new ProcedureSignature("filename");
		FormalArgumentSignature filenameIn1 = new FormalArgumentSignature(true); /* file can be specified as any type */
		filename.addInputArg(filenameIn1);
		FormalArgumentSignature filenameOut1 = new FormalArgumentSignature("string");
		filename.addOutputArg(filenameOut1);
		functionsMap.put(filename.getName(), filename);

		ProcedureSignature filenames = new ProcedureSignature("filenames");
		FormalArgumentSignature filenamesIn1 = new FormalArgumentSignature(true); /* file can be specified as any type */
		filenames.addInputArg(filenamesIn1);
		FormalArgumentSignature filenamesOut1 = new FormalArgumentSignature("string[]"); /* i think this is what it returns */
		filenames.addOutputArg(filenamesOut1);
		functionsMap.put(filenames.getName(), filenames);

        ProcedureSignature dirname = new ProcedureSignature("dirname");
		FormalArgumentSignature dirnameIn1 = new FormalArgumentSignature(true); /* dir can be specified as any type */
		dirname.addInputArg(dirnameIn1);
		FormalArgumentSignature dirnameOut1 = new FormalArgumentSignature("string");
		dirname.addOutputArg(dirnameOut1);
		functionsMap.put(dirname.getName(), dirname);

		ProcedureSignature length = new ProcedureSignature("length");
		FormalArgumentSignature lengthIn1 = new FormalArgumentSignature(true);
		length.addInputArg(lengthIn1);
		FormalArgumentSignature lengthOut1 = new FormalArgumentSignature("int");
		length.addOutputArg(lengthOut1);
		functionsMap.put(length.getName(), length);

		ProcedureSignature regexp = new ProcedureSignature("regexp");
		FormalArgumentSignature regexpIn1 = new FormalArgumentSignature("string");
		regexp.addInputArg(regexpIn1);
		FormalArgumentSignature regexpIn2 = new FormalArgumentSignature("string");
		regexp.addInputArg(regexpIn2);
		FormalArgumentSignature regexpIn3 = new FormalArgumentSignature("string");
		regexp.addInputArg(regexpIn3);
		FormalArgumentSignature regexpOut1 = new FormalArgumentSignature("string");
		regexp.addOutputArg(regexpOut1);
		functionsMap.put(regexp.getName(), regexp);

		ProcedureSignature strcat = new ProcedureSignature("strcat");
		strcat.setAnyNumOfInputArgs();
		FormalArgumentSignature strcatOut1 = new FormalArgumentSignature("string");
		strcat.addOutputArg(strcatOut1);
		functionsMap.put(strcat.getName(), strcat);

		ProcedureSignature sprintf = new ProcedureSignature("sprintf");
		sprintf.setAnyNumOfInputArgs();
		FormalArgumentSignature sprintfOut1 = new FormalArgumentSignature("string");
		sprintf.addOutputArg(sprintfOut1);
		functionsMap.put(sprintf.getName(), sprintf);

		ProcedureSignature strcut = new ProcedureSignature("strcut");
		FormalArgumentSignature strcutIn1 = new FormalArgumentSignature("string");
		strcut.addInputArg(strcutIn1);
		FormalArgumentSignature strcutIn2 = new FormalArgumentSignature("string");
		strcut.addInputArg(strcutIn2);
		FormalArgumentSignature strcutOut1 = new FormalArgumentSignature("string");
		strcut.addOutputArg(strcutOut1);
		functionsMap.put(strcut.getName(), strcut);

		ProcedureSignature strstr = new ProcedureSignature("strstr");
		FormalArgumentSignature strstrIn1 = new FormalArgumentSignature("string");
		strstr.addInputArg(strstrIn1);
		FormalArgumentSignature strstrIn2 = new FormalArgumentSignature("string");
		strstr.addInputArg(strstrIn2);
		FormalArgumentSignature strstrOut1 = new FormalArgumentSignature("int");
		strstr.addOutputArg(strstrOut1);
		functionsMap.put(strstr.getName(), strstr);

		ProcedureSignature strsplit = new ProcedureSignature("strsplit");
		FormalArgumentSignature strsplitIn1 = new FormalArgumentSignature("string");
		strsplit.addInputArg(strsplitIn1);
		FormalArgumentSignature strsplitIn2 = new FormalArgumentSignature("string");
		strsplit.addInputArg(strsplitIn2);
		FormalArgumentSignature strsplitOut1 = new FormalArgumentSignature("string[]");
		strsplit.addOutputArg(strsplitOut1);
		functionsMap.put(strsplit.getName(), strsplit);

		ProcedureSignature toint = new ProcedureSignature("toint");
		FormalArgumentSignature tointIn1 = new FormalArgumentSignature(true);
		toint.addInputArg(tointIn1);
		FormalArgumentSignature toOut1 = new FormalArgumentSignature("int");
		toint.addOutputArg(toOut1);
		functionsMap.put(toint.getName(), toint);

		ProcedureSignature tofloat = new ProcedureSignature("tofloat");
		FormalArgumentSignature tofloatIn1 = new FormalArgumentSignature(true);
		tofloat.addInputArg(tofloatIn1);
		FormalArgumentSignature tofloatOut1 = new FormalArgumentSignature("float");
		tofloat.addOutputArg(tofloatOut1);
		functionsMap.put(tofloat.getName(), tofloat);

	    ProcedureSignature format = new ProcedureSignature("format");
	    FormalArgumentSignature formatIn1 = new FormalArgumentSignature("float");
	    FormalArgumentSignature formatIn2 = new FormalArgumentSignature("int");
	    format.addInputArg(formatIn1);
	    format.addInputArg(formatIn2);
	    FormalArgumentSignature formatOut = new FormalArgumentSignature("string");
	    format.addOutputArg(formatOut);
	    functionsMap.put(format.getName(), format);

	    ProcedureSignature pad = new ProcedureSignature("pad");
        FormalArgumentSignature padIn1 = new FormalArgumentSignature("int");
        FormalArgumentSignature padIn2 = new FormalArgumentSignature("int");
        pad.addInputArg(padIn1);
        pad.addInputArg(padIn2);
        FormalArgumentSignature padOut = new FormalArgumentSignature("string");
        pad.addOutputArg(padOut);
        functionsMap.put(pad.getName(), pad);

		ProcedureSignature tostring = new ProcedureSignature("tostring");
		FormalArgumentSignature tostringIn1 = new FormalArgumentSignature(true);
		tostring.addInputArg(tostringIn1);
		FormalArgumentSignature tostringOut1 = new FormalArgumentSignature("string");
		tostring.addOutputArg(tostringOut1);
		functionsMap.put(tostring.getName(), tostring);

		ProcedureSignature java = new ProcedureSignature("java");
		java.setAnyNumOfInputArgs();
		FormalArgumentSignature output = new FormalArgumentSignature("java");
		java.addOutputArg(output);
		functionsMap.put(java.getName(), java);

		ProcedureSignature exists = new ProcedureSignature("exists");
		exists.setAnyNumOfInputArgs();
		FormalArgumentSignature existsOut1 = new FormalArgumentSignature("boolean");
		exists.addOutputArg(existsOut1);
		functionsMap.put(exists.getName(), exists);

		return functionsMap;
	}

	public String toString() {
	    return outputArgs + " " + name + inputArgs;
	}
}
