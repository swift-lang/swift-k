package org.griphyn.vdl.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.globus.swift.language.ActualParameter;
import org.globus.swift.language.ApplicationBinding;
import org.globus.swift.language.Array;
import org.globus.swift.language.Assign;
import org.globus.swift.language.BinaryOperator;
import org.globus.swift.language.Binding;
import org.globus.swift.language.Call;
import org.globus.swift.language.Continue;
import org.globus.swift.language.Dataset;
import org.globus.swift.language.Foreach;
import org.globus.swift.language.FormalParameter;
import org.globus.swift.language.Function;
import org.globus.swift.language.If;
import org.globus.swift.language.Iterate;
import org.globus.swift.language.LabelledBinaryOperator;
import org.globus.swift.language.Procedure;
import org.globus.swift.language.ProgramDocument;
import org.globus.swift.language.Range;
import org.globus.swift.language.Switch;
import org.globus.swift.language.UnlabelledUnaryOperator;
import org.globus.swift.language.Variable;
import org.globus.swift.language.Dataset.Mapping;
import org.globus.swift.language.Dataset.Mapping.Param;
import org.globus.swift.language.If.Else;
import org.globus.swift.language.If.Then;
import org.globus.swift.language.Procedure;
import org.globus.swift.language.ProgramDocument.Program;
import org.globus.swift.language.StructureMember;
import org.globus.swift.language.Switch.Case;
import org.globus.swift.language.Switch.Default;
import org.globus.swift.language.TypesDocument.Types;
import org.safehaus.uuid.UUIDGenerator;
import org.w3c.dom.Node;

public class Karajan {
	public static final Logger logger = Logger.getLogger(Karajan.class);
	
	public static final String TEMPLATE_FILE_NAME = "Karajan.stg"; 

	StringTemplateGroup m_templates;

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Please provide a SwiftScript program file.");
			System.exit(1);
		}
		compile(args[0], System.out);
	}
	
	public static void compile(String in, PrintStream out) throws Exception {
		Karajan me = new Karajan();
		StringTemplateGroup templates = new StringTemplateGroup(new InputStreamReader(
				Karajan.class.getClassLoader().getResource(TEMPLATE_FILE_NAME).openStream()));

		ProgramDocument programDoc = parseProgramXML(in);

		Program prog = programDoc.getProgram();

		me.setTemplateGroup(templates);
		StringTemplate code = me.program(prog);
		out.println(code.toString());
	}

	public static ProgramDocument parseProgramXML(String defs) 
		throws XmlException, IOException {

		XmlOptions options = new XmlOptions();
		Collection errors = new ArrayList();
		options.setErrorListener(errors);
		options.setValidateOnSet();
		options.setLoadLineNumbers();

		ProgramDocument programDoc;
		programDoc  = ProgramDocument.Factory.parse(new File(defs), options);

		if(programDoc.validate(options)) {
			logger.info("Validation of XML intermediate file was successful");
		} else {
			logger.warn("Validation of XML intermediate file failed.");
				// these errors look rather scary, so output them at
				// debug level
			logger.debug("Validation errors:");
			Iterator i = errors.iterator();
			while(i.hasNext()) {
				XmlError error = (XmlError) i.next();
				logger.debug(error.toString());
			}
			System.exit(3);
		}
		return programDoc;
	}

	public Karajan() {
	}

	void setTemplateGroup(StringTemplateGroup tempGroup) throws IOException {
		m_templates = tempGroup;
	}

	protected StringTemplate template(String name) {
		return m_templates.getInstanceOf(name);
	}

	public StringTemplate program(Program prog) throws Exception {
		StringTemplate progST = template("program");

		progST.setAttribute("types", prog.getTypes());

		for (int i = 0; i < prog.sizeOfProcedureArray(); i++) {
			Procedure proc = prog.getProcedureArray(i);
			procedure(proc, progST);
		}

		statements(prog, progST);
		return progST;
	}

	public void procedure(Procedure proc, StringTemplate progST) throws Exception {
		StringTemplate procST = template("procedure");
		progST.setAttribute("procedures", procST);
		procST.setAttribute("name", proc.getName());
		for (int i = 0; i < proc.sizeOfOutputArray(); i++) {
			FormalParameter param = proc.getOutputArray(i);
			StringTemplate paramST = parameter(param);
			procST.setAttribute("outputs", paramST);
			if (!param.isNil())
				procST.setAttribute("optargs", paramST);
			else
				procST.setAttribute("arguments", paramST);
		}
		for (int i = 0; i < proc.sizeOfInputArray(); i++) {
			FormalParameter param = proc.getInputArray(i);
			StringTemplate paramST = parameter(param);
			procST.setAttribute("inputs", paramST);
			if (!param.isNil())
				procST.setAttribute("optargs", paramST);
			else
				procST.setAttribute("arguments", paramST);
		}

		Binding bind;
		if ((bind = proc.getBinding()) != null) {
			binding(bind, procST);
		}
		else {
			statements(proc, procST);
		}

	}

	public StringTemplate parameter(FormalParameter param) {
		StringTemplate paramST = new StringTemplate("parameter");
		StringTemplate typeST = new StringTemplate("type");
		paramST.setAttribute("name", param.getName());
		typeST.setAttribute("name", param.getType().getLocalPart());
		typeST.setAttribute("namespace", param.getType().getNamespaceURI());
		paramST.setAttribute("type", typeST);
		paramST.setAttribute("isArray", new Boolean(param.getIsArray1()));
		if(!param.isNil())
			paramST.setAttribute("default",expressionToKarajan(param.getAbstractExpression()));
		return paramST;
	}

	public void variable(Variable var, StringTemplate progST) throws Exception {
		StringTemplate variableST = template("variable");
		progST.setAttribute("declarations", variableST);
		variableST.setAttribute("name", var.getName());
		variableST.setAttribute("type", var.getType().getLocalPart());
		variableST.setAttribute("isArray", Boolean.valueOf(var.getIsArray1()));

		if(!var.isNil()) {
			variableST.setAttribute("expr",expressionToKarajan(var.getAbstractExpression()));
		} else {
			// add temporary mapping info
			StringTemplate mappingST = new StringTemplate("mapping");
			mappingST.setAttribute("descriptor", "concurrent_mapper");
			StringTemplate paramST = template("vdl_parameter");
			paramST.setAttribute("name", "prefix");
			paramST.setAttribute("expr", var.getName() + "-"
					+ UUIDGenerator.getInstance().generateRandomBasedUUID().toString());
			mappingST.setAttribute("params", paramST);
			variableST.setAttribute("mapping", mappingST);
			variableST.setAttribute("nil", Boolean.TRUE);
		}
	}

	public void dataset(Dataset dataset, StringTemplate progST) throws Exception {
		StringTemplate datasetST = template("variable");
		progST.setAttribute("declarations", datasetST);
		datasetST.setAttribute("name", dataset.getName());
		datasetST.setAttribute("type", dataset.getType().getLocalPart());
		if (dataset.isSetIsArray1()) {
			datasetST.setAttribute("isArray", Boolean.valueOf(dataset.getIsArray1()));
		}
		if (dataset.getFile() != null) {
			StringTemplate fileST = new StringTemplate("file");
			fileST.setAttribute("name", dataset.getFile().getName());
			fileST.defineFormalArgument("params");
			datasetST.setAttribute("file", fileST);
		}
		Mapping mapping = dataset.getMapping();

		if (mapping != null) {
			StringTemplate mappingST = new StringTemplate("mapping");
			mappingST.setAttribute("descriptor", mapping.getDescriptor());
			for (int i = 0; i < mapping.sizeOfParamArray(); i++) {
				Param param = mapping.getParamArray(i);
				StringTemplate paramST = template("vdl_parameter");
				paramST.setAttribute("name", param.getName());
				paramST.setAttribute("expr",expressionToKarajan(param.getAbstractExpression()));
				mappingST.setAttribute("params", paramST);
			}
			datasetST.setAttribute("mapping", mappingST);
		}
	}

	public void assign(Assign assign, StringTemplate progST) throws Exception {
		StringTemplate assignST = template("assign");
		progST.setAttribute("declarations", assignST);
		assignST.setAttribute("var", expressionToKarajan(assign.getAbstractExpressionArray(0)));
		assignST.setAttribute("value", expressionToKarajan(assign.getAbstractExpressionArray(1)));
	}

	public void statements(XmlObject prog, StringTemplate progST) throws Exception {
		XmlCursor cursor = prog.newCursor();
		cursor.selectPath("*");

		// iterate over the selection
		while (cursor.toNextSelection()) {
			// two views of the same data:
			// move back and forth between XmlObject <-> XmlCursor
			XmlObject child = cursor.getObject();

			StringTemplate st = null;
			if (child instanceof Variable) {
				variable((Variable) child, progST);
			}
			else if (child instanceof Dataset) {
				dataset((Dataset) child, progST);
			}
			else if (child instanceof Assign) {
				assign((Assign) child, progST);
			}
			else if (child instanceof Call) {
				Call call = (Call) child;
				st = template("call");
				progST.setAttribute("statements", st);
				call(call, st);
			}
			else if (child instanceof Foreach) {
				Foreach foreach = (Foreach) child;
				st = template("foreach");
				progST.setAttribute("statements", st);
				foreachStat(foreach, st);
			}
			else if (child instanceof Iterate) {
				Iterate iterate = (Iterate) child;
				st = template("iterate");
				progST.setAttribute("statements",st);
				iterateStat(iterate, st);
			}
			else if (child instanceof If) {
				If ifstat = (If) child;
				st = template("if");
				progST.setAttribute("statements", st);
				ifStat(ifstat, st);
			} else if (child instanceof Switch) {
				Switch switchstat = (Switch) child;
				st = template("switch");
				progST.setAttribute("statements", st);
				switchStat(switchstat, st);
			} else if (child instanceof Procedure
				|| child instanceof Types
				|| child instanceof Continue
				|| child instanceof FormalParameter) {
				// ignore these - they're expected but we don't need to
				// do anything for them here
			} else {
				throw new RuntimeException("Unexpected element in XML. Implementing class "+child.getClass()+", content "+child);
			}
		}
	}

	public void call(Call call, StringTemplate callST) throws Exception {
		callST.setAttribute("func", call.getProc().getLocalPart());
		StringTemplate parentST = callST.getEnclosingInstance();
		for (int i = 0; i < call.sizeOfInputArray(); i++) {
			ActualParameter input = call.getInputArray(i);
			StringTemplate argST = actualParameter(input);
			callST.setAttribute("inputs", argST);
			markDataset((StringTemplate) argST.getAttribute("expr"), parentST, true);
		}
		for (int i = 0; i < call.sizeOfOutputArray(); i++) {
			ActualParameter output = call.getOutputArray(i);
			StringTemplate argST = actualParameter(output);
			callST.setAttribute("outputs", argST);
			markDataset((StringTemplate) argST.getAttribute("expr"), parentST, false);
		}
	}

	public void iterateStat(Iterate iterate, StringTemplate iterateST) throws Exception {
		XmlObject cond = iterate.getAbstractExpression();
		StringTemplate condST = expressionToKarajan(cond);
		markDataset(condST, iterateST, true);
		iterateST.setAttribute("cond", condST);
		iterateST.setAttribute("var", iterate.getVar());
		statements(iterate.getBody(), iterateST);
	}


	public void foreachStat(Foreach foreach, StringTemplate foreachST) throws Exception {
		foreachST.setAttribute("var", foreach.getVar());
		foreachST.setAttribute("indexVar", foreach.getIndexVar());
		XmlObject in = foreach.getIn().getAbstractExpression();
		StringTemplate inST = expressionToKarajan(in);
		markDataset(inST, foreachST, true);
		foreachST.setAttribute("in", inST);
		statements(foreach.getBody(), foreachST);
	}

	public void ifStat(If ifstat, StringTemplate ifST) throws Exception {
		StringTemplate conditionST = expressionToKarajan(ifstat.getAbstractExpression());
		ifST.setAttribute("condition", conditionST.toString());

		Then thenstat = ifstat.getThen();
		Else elsestat = ifstat.getElse();
		StringTemplate thenST = template("sub_comp");
		ifST.setAttribute("vthen", thenST);

		statements(thenstat, thenST);

		if (elsestat == null)
			return;

		StringTemplate elseST = template("sub_comp");
		ifST.setAttribute("velse", elseST);

		statements(elsestat, elseST);

	}

	public void switchStat(Switch switchstat, StringTemplate switchST) throws Exception {
		StringTemplate conditionST = expressionToKarajan(switchstat.getAbstractExpression());
		switchST.setAttribute("condition", conditionST.toString());

		for (int i=0; i< switchstat.sizeOfCaseArray(); i++) {
			Case casestat = switchstat.getCaseArray(i);
			StringTemplate caseST = new StringTemplate("case");
			switchST.setAttribute("cases", caseST);
			caseStat(casestat, caseST);
		}
		Default defaultstat = switchstat.getDefault();
		if (defaultstat == null)
			return;
		StringTemplate defaultST = template("sub_comp");
		switchST.setAttribute("sdefault", defaultST);

		statements(defaultstat, defaultST);
	}

	public void caseStat(Case casestat, StringTemplate caseST) throws Exception {
		StringTemplate valueST = expressionToKarajan(casestat.getAbstractExpression());
		caseST.setAttribute("value", valueST.toString());
		statements(casestat.getStatements(), caseST);
	}

	public StringTemplate actualParameter(ActualParameter arg) throws Exception {
		StringTemplate argST = template("call_arg");
		argST.setAttribute("bind", arg.getBind());
		argST.setAttribute("expr", expressionToKarajan(arg.getAbstractExpression()));
		return argST;
	}

	public void binding(Binding bind, StringTemplate procST) throws Exception {
		StringTemplate bindST = new StringTemplate("binding");
		ApplicationBinding app;
		if ((app = bind.getApplication()) != null) {
			bindST.setAttribute("application", application(app));
			procST.setAttribute("binding", bindST);
		} else throw new RuntimeException("Unknown binding: "+bind);
	}

	public StringTemplate application(ApplicationBinding app) throws Exception {
		StringTemplate appST = new StringTemplate("application");
		appST.setAttribute("exec", app.getExecutable());
		for (int i = 0; i < app.sizeOfAbstractExpressionArray(); i++) {
			XmlObject argument = app.getAbstractExpressionArray(i);
			StringTemplate argumentST = expressionToKarajan(argument);
			appST.setAttribute("arguments", argumentST);
		}
		if(app.getStdin()!=null)
			appST.setAttribute("stdin", expressionToKarajan(app.getStdin().getAbstractExpression()));
		if(app.getStdout()!=null)
			appST.setAttribute("stdout", expressionToKarajan(app.getStdout().getAbstractExpression()));
		if(app.getStderr()!=null)
			appST.setAttribute("stderr", expressionToKarajan(app.getStderr().getAbstractExpression()));
		return appST;
	}

	/** Produces a Karajan function invocation from a SwiftScript invocation.
	  * The Karajan invocation will have the same name as the SwiftScript
	  * function, in the 'vdl' Karajan namespace. Parameters to the
	  * Karajan function will differ from the SwiftScript parameters in
	  * a number of ways - read the source for the exact ways in which
	  * that happens.
	  */

	public StringTemplate function(Function func) {
		StringTemplate funcST = template("function");
		funcST.setAttribute("name", func.getName());
		XmlObject[] arguments = func.getAbstractExpressionArray();
		for(int i = 0; i < arguments.length; i++ ) {
			funcST.setAttribute("args",expressionToKarajan(arguments[i]));
		}

		return funcST;
	}

	static final String SWIFTSCRIPT_NS = "http://ci.uchicago.edu/swift/2007/07/swiftscript";

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

	/** converts an XML intermediate form expression into a
	 *  Karajan expression.
	 */
	public StringTemplate expressionToKarajan(XmlObject expression)
	{
		Node expressionDOM = expression.getDomNode();
		String namespaceURI = expressionDOM.getNamespaceURI();
		String localName = expressionDOM.getLocalName();
		QName expressionQName = new QName(namespaceURI, localName);

		if(expressionQName.equals(OR_EXPR))
		{
			StringTemplate st = template("or");
			BinaryOperator o = (BinaryOperator)expression;
			st.setAttribute("left", expressionToKarajan(o.getAbstractExpressionArray(0)));
			st.setAttribute("right", expressionToKarajan(o.getAbstractExpressionArray(1)));
			return st;
		} else if (expressionQName.equals(AND_EXPR)) {
			StringTemplate st = template("and");
			BinaryOperator o = (BinaryOperator)expression;
			st.setAttribute("left", expressionToKarajan(o.getAbstractExpressionArray(0)));
			st.setAttribute("right", expressionToKarajan(o.getAbstractExpressionArray(1)));
			return st;
		} else if (expressionQName.equals(BOOL_EXPR)) {
			XmlBoolean xmlBoolean = (XmlBoolean) expression;
			boolean b = xmlBoolean.getBooleanValue();
			StringTemplate st = template("bConst");
			st.setAttribute("value",""+b);
			return st;
		} else if (expressionQName.equals(INT_EXPR)) {
			XmlInt xmlInt = (XmlInt) expression;
			int i = xmlInt.getIntValue();
			StringTemplate st = template("iConst");
			st.setAttribute("value",""+i);
			return st;
		} else if (expressionQName.equals(FLOAT_EXPR)) {
			XmlFloat xmlFloat = (XmlFloat) expression;
			float f = xmlFloat.getFloatValue();
			StringTemplate st = template("fConst");
			st.setAttribute("value",""+f);
			return st;
		} else if (expressionQName.equals(STRING_EXPR)) {
			XmlString xmlString = (XmlString) expression;
			String s = xmlString.getStringValue();
			StringTemplate st = template("sConst");
			st.setAttribute("innervalue",s);
			return st;
		} else if (expressionQName.equals(COND_EXPR)) {
			StringTemplate st = template("binaryop");
			LabelledBinaryOperator o = (LabelledBinaryOperator) expression;
			st.setAttribute("op", o.getOp());
			st.setAttribute("left", expressionToKarajan(o.getAbstractExpressionArray(0)));
			st.setAttribute("right", expressionToKarajan(o.getAbstractExpressionArray(1)));
			return st;
		} else if (expressionQName.equals(ARITH_EXPR)) {
			LabelledBinaryOperator o = (LabelledBinaryOperator) expression;
			StringTemplate st = template("binaryop");
			st.setAttribute("op", o.getOp());
			st.setAttribute("left", expressionToKarajan(o.getAbstractExpressionArray(0)));
			st.setAttribute("right", expressionToKarajan(o.getAbstractExpressionArray(1)));
			return st;
		} else if (expressionQName.equals(UNARY_NEGATION_EXPR)) {
			UnlabelledUnaryOperator e = (UnlabelledUnaryOperator) expression;
			StringTemplate st = template("unaryNegation");
			st.setAttribute("exp",expressionToKarajan(e.getAbstractExpression()));
			return st;
		} else if (expressionQName.equals(NOT_EXPR)) {
// TODO not can probably merge with 'unary'
			UnlabelledUnaryOperator e = (UnlabelledUnaryOperator)expression;
			StringTemplate st = template("not");
			st.setAttribute("exp",expressionToKarajan(e.getAbstractExpression()));
			return st;
		} else if (expressionQName.equals(VARIABLE_REFERENCE_EXPR)) {
			XmlString xmlString = (XmlString) expression;
			String s = xmlString.getStringValue();
			StringTemplate st = template("id");
			st.setAttribute("var",s);
			return st;

		} else if (expressionQName.equals(ARRAY_SUBSCRIPT_EXPR)) {
			BinaryOperator op = (BinaryOperator) expression;
			StringTemplate newst = template("extractarrayelement");
			newst.setAttribute("arraychild",expressionToKarajan(op.getAbstractExpressionArray(1)));
			newst.setAttribute("parent",expressionToKarajan(op.getAbstractExpressionArray(0)));
			return newst;
		} else if (expressionQName.equals(STRUCTURE_MEMBER_EXPR)) {
			StructureMember sm = (StructureMember) expression;
			StringTemplate newst = template("extractarrayelement");
			newst.setAttribute("memberchild", sm.getMemberName());
			newst.setAttribute("parent",expressionToKarajan(sm.getAbstractExpression()));
			return newst;
			// TODO the template layout for this and ARRAY_SUBSCRIPT are
			// both a bit convoluted for historical reasons.
			// should be straightforward to tidy up.
		} else if (expressionQName.equals(ARRAY_EXPR)) {
			Array array = (Array)expression;
			StringTemplate st = template("array");
			for (int i = 0; i < array.sizeOfAbstractExpressionArray(); i++) {
				XmlObject expr = array.getAbstractExpressionArray(i);
				st.setAttribute("elements", expressionToKarajan(expr));
			}
			return st;
		} else if (expressionQName.equals(RANGE_EXPR)) {
			Range range = (Range)expression;
			StringTemplate st = template("range");
			st.setAttribute("from", expressionToKarajan(
				range.getAbstractExpressionArray(0)));
			st.setAttribute("to", expressionToKarajan(
				range.getAbstractExpressionArray(1)));
			if(range.sizeOfAbstractExpressionArray()==3) // step is optional
				st.setAttribute("step", expressionToKarajan(
				 range.getAbstractExpressionArray(2)));
			return st;
		} else if (expressionQName.equals(FUNCTION_EXPR)) {
			Function f = (Function) expression;
			StringTemplate st = function(f);
			return st;
		} else {
			throw new RuntimeException("unknown expression implemented by class "+expression.getClass()+" with node name "+expressionQName +" and with content "+expression);
		}
	}


	/** Traverses an expression template marking input datasets as such.
	  *
	  * If the expression is an identifier, get the variable name
	  * and call markDataset on that.
	  *
	  * Otherwise, go through all the subelements of expr
	  * and call markdataset on them, keeping st and isInput the
	  * same.
	  *
	  * @param exprST an expression
	  *
	  * @param st a program context
	  *
	  * @param isInput whether the expression is to be regarded as 
	  *        an input or an output
	  **/

	protected void markDataset(StringTemplate exprST, StringTemplate st, boolean isInput) {
		if(logger.isDebugEnabled()) logger.debug("markDataset exprST="+exprST+" st="+st+" input="+isInput);
		if (exprST == null || st == null)
			return;
		if (exprST.getName().equals("id")) {
			// variable reference, mark the variable
			logger.debug("treating as a variable");
			String var = (String) exprST.getAttribute("var");
			markDataset(var, st, isInput);
		} else if(exprST.getName().equals("in")) {
			markDataset((String) exprST.getAttribute("var"), st, isInput);
		} else {
			// an expression, mark all subelements
			Map subSTMap = exprST.getAttributes();
			for (Iterator it = subSTMap.values().iterator(); it.hasNext();) {
				Object sub = it.next();
				if (sub instanceof StringTemplate)
					markDataset((StringTemplate) sub, st, isInput);
			}
		}
	}


	/** Mark datasets given a name rather than an expr. 
	  * (c.f.  markdataset which takes StringTemplate as first parameter).
	  *
	  * If 'st' is a foreach statement and 'name' is the name of the
	  * iteration variable of the foreach statement, we mark the
	  * input variable rather than continuing with the rest of the procedure.
	  *
	  * Otherwise, we go through the <em>declarations</em> in the 'st'
	  * enclosing structure (if the enclosing structure has any or is a
	  * declaration set?) and consider each declaration in turn to see if
	  * its an assign, a variable or a dataset. In the case of a dataset,
	  * recurse on the dataset parameters.
	  *
	  * If it is any of those, we call
	  * <code>markDataset</code> on the particular declaration and then return.
	  *
	  * Otherwise, if 'st' is a procedure we give up and return
	  *
	  * Otherwise, we find the structure the encloses 'st' and recurse
	  * over that greater scope.
	  *
	  * @param name the name of variable to mark. may be null.
	  * @param st a program fragment
	  * @param isInput whether the variable is null or not.
	  **/

	protected void markDataset(String name, StringTemplate st, boolean isInput) {
		if (name == null || st == null)
			return;

		// check if it is an iteration variable in foreach
		if (st.getName().equals("foreach") && name.equals(st.getAttribute("var"))) {
			StringTemplate inST = (StringTemplate) st.getAttribute("in");
			if (inST == null)
				throw new RuntimeException("invalid foreach statement:\n" + st);
			String inVar = (String) inST.getAttribute("var");
			if (inVar == null)
				throw new RuntimeException("invalid foreach statement:\n" + st);
			// replace search name with in.var and go up one level
			markDataset(inVar, st.getEnclosingInstance(), isInput);
			return;
		}

		// check assignment/dataset/variable
		Object decls = st.getAttribute("declarations");
		if (decls != null) {
			if (decls instanceof StringTemplate) {
				StringTemplate declST = (StringTemplate) decls;
				String declName = declST.getName();
				if (declName.equals("assign")) {
					// found that it is assigned
					if (declST.getAttribute("var").equals(name)) {
						markDatasetParam(declST, st, isInput);
						return;
					}
				} else
				if (declName.equals("variable")) {
					if (declST.getAttribute("name").equals(name)) {
						if (declST.getAttribute("nil") != null)
							return;
						markDatasetParam(declST, st, isInput);
						return;
					}
				} else
				if (declName.equals("dataset")) {
					throw new RuntimeException("dataset template is unsupported");
				}
			}
			else {
				// a list of declarations
				Iterator it = ((List) decls).iterator();
				while (it.hasNext()) {
					StringTemplate declST = (StringTemplate) it.next();
					if (declST.getName().equals("assign")) {
						// found that it is assigned
						if (declST.getAttribute("var").equals(name)) {
							markDatasetParam(declST, st, isInput);
							return;
						}
					}
				}
				it = ((List) decls).iterator();
				while (it.hasNext()) {
					StringTemplate declST = (StringTemplate) it.next();
					String declName = declST.getName();
					if (declName.equals("variable")) {
						if (declST.getAttribute("name").equals(name)) {
							if (declST.getAttribute("nil") != null)
								return;
							markDatasetParam(declST, st, isInput);
							return;
						}
					} else 
					if (declName.equals("dataset")) {
						throw new RuntimeException("dataset template is unsupported");
					}
				}
			}
		}

		// for procedures, no need to go up.
		if (st.getName().equals("procedure"))
			return;

		// not found, go up one level
		StringTemplate parentST = st.getEnclosingInstance();
		markDataset(name, parentST, isInput);
	}

	/** mark all the dataset references in a function as input. */

	protected void markFunction(StringTemplate funcST, StringTemplate st) {
		Object expr = funcST.getAttribute("expr");
		if (expr == null)
			return;
		
		if (expr instanceof StringTemplate) {
			StringTemplate exprST = (StringTemplate) expr;
			markDataset(exprST, st, true);
		} else {
			// a list of functions
			Iterator it = ((List) expr).iterator();
			while (it.hasNext()) {
				StringTemplate subFuncST = (StringTemplate) it.next();
				markFunction(subFuncST, st);
			}
		}
	}

	/** Marks parameters of dataset declarations.
	  *
	  * If there is a parameter labelled 'input' and this method
	  * is called with isInput false, then that parameter will be
	  * replaced by one indicating that this is not an input.  If
	  * we reach the end of the parameters without finding an 'input',
	  * then we create one.
	  *
	  * For other parameters, markDataset will be recursed into,
	  * indicating each parameter is an input (rather than passing
	  * through the supplied 'isInput' value).
	  *
	  * This method mutates the template structures, and is the only one 
	  * to do so.
	  **/

	protected void markDatasetParam(StringTemplate datasetST, StringTemplate st, boolean isInput) {
		logger.debug("markdatasetparam(3)");
		if (datasetST == null) {
			logger.debug("null dataset - returning. (5)");
			return;
		}

		// process file mapping
		StringTemplate mappingST = (StringTemplate) datasetST.getAttribute("file");
		if (mappingST == null) {
			mappingST = (StringTemplate) datasetST.getAttribute("mapping");
			if (mappingST == null) {
				logger.debug("dataset mapping and file are both null (6)");
				return;
			}
		}

		// mark input as true or false accordingly
		Boolean value = new Boolean(isInput);

		Object params = mappingST.getAttribute("params");
		if (params != null) {
			if(logger.isDebugEnabled()) logger.debug("we have some parameter(s) to process (7): "+params);
			if (params instanceof StringTemplate) {
				if(logger.isDebugEnabled()) logger.debug("we have just one parameter to process(8)");
				StringTemplate paramST = (StringTemplate) params;
				if (paramST.getAttribute("name").equals("input")) {
					logger.debug("name is input");
					if (!isInput) {
						logger.debug("it is not an input(4)");
						// mark as false
						paramST.removeAttribute("expr");
						paramST.setAttribute("expr", new Boolean(false));
					} else {
						logger.debug("it is an input(4)");
					}
					return;
				} else {
					// always try to mark references at the RHS of a mapper as input
					StringTemplate exprST = (StringTemplate)paramST.getAttribute("expr");
					if (exprST != null) {
						markDataset(exprST, st, true);
					} else {
						StringTemplate funcST = (StringTemplate)paramST.getAttribute("func");
						if (funcST != null) {
							markFunction(funcST, st);
						}
					}
				}
			}
			else {
				logger.debug("we have a list of parameters to process(9)");
				// a list of params
				Iterator it = ((List) params).iterator();
				while (it.hasNext()) {
					StringTemplate paramST = (StringTemplate) it.next();
					boolean foundInput = false;
					if (paramST.getAttribute("name").equals("input")) {
						if (!isInput) {
							// mark as false
							paramST.removeAttribute("expr");
							paramST.setAttribute("expr", new Boolean(false));
						}
						foundInput = true;
					} else {
						// always try to mark references at the RHS of a mapper as input
						StringTemplate exprST = (StringTemplate)paramST.getAttribute("expr");
						if (exprST != null) {
							markDataset(exprST, st, true);
						} else {
							StringTemplate funcST = (StringTemplate)paramST.getAttribute("func");
							if (funcST != null) {
								markFunction(funcST, st);
							}
						}
					}
					if (foundInput) {
						return;
					}
				}
			}
		} else {
			logger.debug("we have no parameter(s) to process (9)");
		}
		StringTemplate iparamST = template("vdl_parameter");
		iparamST.setAttribute("name", "input");
		iparamST.setAttribute("expr", value);
		mappingST.setAttribute("params", iparamST);
	}
}
