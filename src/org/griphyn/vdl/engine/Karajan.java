package org.griphyn.vdl.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.griphyn.vdl.model.ActualParameter;
import org.griphyn.vdl.model.ApplicationBinding;
import org.griphyn.vdl.model.Argument;
import org.griphyn.vdl.model.Array;
import org.griphyn.vdl.model.Assign;
import org.griphyn.vdl.model.Binding;
import org.griphyn.vdl.model.Call;
import org.griphyn.vdl.model.Dataset;
import org.griphyn.vdl.model.Foreach;
import org.griphyn.vdl.model.FormalParameter;
import org.griphyn.vdl.model.Function;
import org.griphyn.vdl.model.FunctionArgument;
import org.griphyn.vdl.model.If;
import org.griphyn.vdl.model.Procedure;
import org.griphyn.vdl.model.ProgramDocument;
import org.griphyn.vdl.model.Range;
import org.griphyn.vdl.model.Repeat;
import org.griphyn.vdl.model.Switch;
import org.griphyn.vdl.model.Variable;
import org.griphyn.vdl.model.While;
import org.griphyn.vdl.model.Dataset.Mapping;
import org.griphyn.vdl.model.Dataset.Mapping.Param;
import org.griphyn.vdl.model.If.Else;
import org.griphyn.vdl.model.If.Then;
import org.griphyn.vdl.model.ProgramDocument.Program;
import org.griphyn.vdl.model.Switch.Case;
import org.griphyn.vdl.model.Switch.Default;
import org.griphyn.vdl.parser.VDLExpression;
import org.safehaus.uuid.UUIDGenerator;

public class Karajan {
	StringTemplateGroup m_templates;
	VDLExpression m_exprParser;

	public static void main(String[] args) throws Exception {
		Karajan me = new Karajan();
		String templateFileName = "Karajan.stg";

		if (args.length < 1) {
			System.err.println("Please provide a SwiftScript program file.");
			System.exit(1);
		}

		String defs = args[0];

		// System.out.println("input:" + defs);
		StringTemplateGroup templates = new StringTemplateGroup(new InputStreamReader(
				Karajan.class.getClassLoader().getResource(templateFileName).openStream()));

		ProgramDocument programDoc = ProgramDocument.Factory.parse(new File(defs));
		Program prog = programDoc.getProgram();

		me.setTemplateGroup(templates);
		StringTemplate code = me.program(prog);
		System.out.println(code.toString());
	}

	public Karajan() {
	}

	void setTemplateGroup(StringTemplateGroup tempGroup) throws IOException {
		m_templates = tempGroup;
		m_exprParser = VDLExpression.instance(m_templates);
	}

	protected StringTemplate template(String name) {
		return m_templates.getInstanceOf(name);
	}

	public String dequote(String s) {
		if (s == null)
			return null;
		if (s.startsWith("\"") && s.endsWith("\"")) {
			return s.substring(1, s.length() - 1);
		}
		if (s.startsWith("&quot;") && s.endsWith("&quot;")) {
			return s.substring(6, s.length() - 6);
		}
		return s;
	}

	public String quote(String s) {
		if (s == null)
			return null;
		String s1 = s.replaceAll("\"", "&quot;");
		// String s2 = s1.replaceAll("'", "&apos;");
		return s1;
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
			if (paramST.getAttribute("default") != null)
				procST.setAttribute("optargs", paramST);
			else
				procST.setAttribute("arguments", paramST);
		}
		for (int i = 0; i < proc.sizeOfInputArray(); i++) {
			FormalParameter param = proc.getInputArray(i);
			StringTemplate paramST = parameter(param);
			procST.setAttribute("inputs", paramST);
			if (paramST.getAttribute("default") != null)
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
		paramST.defineFormalArgument("default");
		paramST.setAttribute("default", quote(param.getDefault()));
		return paramST;
	}

	public void variable(Variable var, StringTemplate progST) throws Exception {
		StringTemplate variableST = template("variable");
		progST.setAttribute("declarations", variableST);
		variableST.setAttribute("name", var.getName());
		variableST.setAttribute("type", var.getType().getLocalPart());
		variableST.setAttribute("isArray", Boolean.valueOf(var.getIsArray1()));
		Array array = var.getArray();
		Range range = var.getRange();
		Function func = var.getFunction();
		if (array != null) {
			array(array, variableST);
		}
		else if (range != null) {
			range(range, variableST);
		}
		else if (func != null) {
			String value = getText(var);
			StringTemplate funcST = function(func);
			funcST.setName(func.getName());
			variableST.setAttribute("expr", funcST);
		}
		else {
			String content = getText(var);
			setExprOrValue(variableST, content);
		}
		if (variableST.getAttribute("nil") != null) {
			// add temporary mapping info
			StringTemplate mappingST = new StringTemplate("mapping");
			mappingST.setAttribute("descriptor", "concurrent_mapper");
			StringTemplate paramST = template("vdl_parameter");
			paramST.setAttribute("name", "prefix");
			paramST.setAttribute("value", var.getName() + "-"
					+ UUIDGenerator.getInstance().generateRandomBasedUUID().toString());
			mappingST.setAttribute("params", paramST);
			variableST.setAttribute("mapping", mappingST);
		}
	}

	public void dataset(Dataset dataset, StringTemplate progST) throws Exception {
		StringTemplate datasetST = template("dataset");
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
				int j = 0;
				while (j < param.sizeOfFunctionArray()) {
					Function func = param.getFunctionArray(j);
					StringTemplate funcST = function(func);
					paramST.setAttribute("func", funcST);
					j++;
				}
				if (j == 0) {
					String value = getText(param);
					StringTemplate funcST = template("function");
					setExprOrValue(funcST, value, true, true);
					paramST.setAttribute("func", funcST);
				}
				mappingST.setAttribute("params", paramST);
			}
			datasetST.setAttribute("mapping", mappingST);
		}
	}

	public void assign(Assign assign, StringTemplate progST) throws Exception {
		StringTemplate assignST = template("assign");
		progST.setAttribute("declarations", assignST);
		String dest = assign.getTo();
		setPath(assignST, dest);

		Array array = assign.getArray();
		Range range = assign.getRange();
		Function func = assign.getFunction();
		
		if (array != null) {
			array(array, assignST);
		}
		else if (range != null) {
			range(range, assignST);
		}
		else if (func != null) {
			String value = getText(assign);
			StringTemplate funcST = function(func);
			funcST.setName(func.getName());
			assignST.setAttribute("expr", funcST);
		}
		else {
			// check the rvalue
			String src = getText(assign);

			StringTemplate srcST = new StringTemplate("src");
			setExprOrValue(srcST, src);
			assignST.setAttribute("value", srcST.getAttribute("value"));
			assignST.setAttribute("expr", srcST.getAttribute("expr"));
		}
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
			else if (child instanceof If) {
				If ifstat = (If) child;
				st = template("if");
				progST.setAttribute("statements", st);
				ifStat(ifstat, st);
			}
			else if (child instanceof While) {
				While whilestat = (While) child;
				st = template("while");
				progST.setAttribute("statements", st);
				whileStat(whilestat, st);
			} else if (child instanceof Repeat) {
				Repeat repeatstat = (Repeat) child;
				st = template("repeat");
				progST.setAttribute("statements", st);
				repeatStat(repeatstat, st);
			} else if (child instanceof Switch) {
				Switch switchstat = (Switch) child;
				st = template("switch");
				progST.setAttribute("statements", st);
				switchStat(switchstat, st);
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

	public void foreachStat(Foreach foreach, StringTemplate foreachST) throws Exception {
		foreachST.setAttribute("var", foreach.getVar());
		foreachST.setAttribute("indexVar", foreach.getIndexVar());
		String in = foreach.getIn();
		StringTemplate inST = new StringTemplate("in");
		setPath(inST, in);

		foreachST.setAttribute("in", inST);

		statements(foreach, foreachST);
	}

	public void whileStat(While whilestat, StringTemplate whileST) throws Exception {
		String condition = whilestat.getTest();
		StringTemplate conditionST = m_exprParser.parse(condition);
		whileST.setAttribute("condition", conditionST.toString());

		statements(whilestat, whileST);
	}

	public void repeatStat(Repeat repeatstat, StringTemplate repeatST) throws Exception {
		String condition = repeatstat.getUntil();
		StringTemplate conditionST = m_exprParser.parse(condition);
		repeatST.setAttribute("condition", conditionST.toString());

		statements(repeatstat, repeatST);
	}

	public void ifStat(If ifstat, StringTemplate ifST) throws Exception {
		String condition = ifstat.getTest();
		StringTemplate conditionST = m_exprParser.parse(condition);
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
		String condition = switchstat.getTest();
		StringTemplate conditionST = m_exprParser.parse(condition);
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
		String value = casestat.getValue();
		StringTemplate valueST = m_exprParser.parse(value);
		caseST.setAttribute("value", valueST.toString());
		if (casestat.getFallThrough()) {
			caseST.setAttribute("break", new Boolean(true));
		}

		statements(casestat, caseST);
	}

	public StringTemplate actualParameter(ActualParameter arg) throws Exception {
		StringTemplate argST = template("call_arg");
		argST.setAttribute("bind", arg.getBind());
		Array array = arg.getArray();
		Range range = arg.getRange();
		if (array != null) {
			array(array, argST);
		}
		else if (range != null) {
			range(range, argST);
		}
		else {
			String content = getText(arg);
			// check for quoted string
			setExprOrValue(argST, content, false);
		}
		return argST;
	}

	public void array(Array array, StringTemplate st) throws Exception {
		StringTemplate arrayST = template("array");
		st.setAttribute("array", arrayST);
		for (int i = 0; i < array.sizeOfElementArray(); i++) {
			XmlObject elem = array.getElementArray(i);
			StringTemplate elemST = template("element");
			String content = getText(elem);
			setExprOrValue(elemST, content, false);
			arrayST.setAttribute("elements", elemST);
		}
	}

	public void range(Range range, StringTemplate st) throws Exception {
		StringTemplate rangeST = template("range");
		st.setAttribute("range", rangeST);

		StringTemplate fromST = template("element");
		String content = range.getFrom().getStringValue();
		setExprOrValue(fromST, content, false, false);
		rangeST.setAttribute("from", fromST);

		StringTemplate toST = template("element");
		content = range.getTo().getStringValue();
		setExprOrValue(toST, content, false, false);
		rangeST.setAttribute("to", toST);

		if (range.getStep() != null) {
			StringTemplate stepST = template("element");
			content = range.getStep().getStringValue();
			setExprOrValue(stepST, content, false, false);
			rangeST.setAttribute("step", stepST);
		}
	}

	public void binding(Binding bind, StringTemplate procST) throws Exception {
		StringTemplate bindST = new StringTemplate("binding");
		ApplicationBinding app;
		if ((app = bind.getApplication()) != null) {
			bindST.setAttribute("application", application(app));
			procST.setAttribute("binding", bindST);
		}
	}

	public StringTemplate application(ApplicationBinding app) throws Exception {
		StringTemplate appST = new StringTemplate("application");
		appST.setAttribute("exec", getText(app.getExecutable()));
		for (int i = 0; i < app.sizeOfArgumentArray(); i++) {
			Argument argument = app.getArgumentArray(i);
			StringTemplate argumentST = argument(argument);
			appST.setAttribute("arguments", argumentST);
		}
		appST.setAttribute("stdin", argument(app.getStdin()));
		appST.setAttribute("stdout", argument(app.getStdout()));
		appST.setAttribute("stderr", argument(app.getStderr()));
		return appST;
	}

	public StringTemplate argument(Argument arg) throws Exception {
		if (arg == null)
			return null;

		StringTemplate argumentST = template("argument");
		int i = 0;
		while (i < arg.sizeOfFunctionArray()) {
			Function func = arg.getFunctionArray(i);
			StringTemplate funcST = function(func);
			argumentST.setAttribute("func", funcST);
			i++;
		}
		if (i == 0) {
			String value = getText(arg);
			StringTemplate funcST = template("function");
			setExprOrValue(funcST, value, true, false);
			argumentST.setAttribute("func", funcST);
		}
		return argumentST;
	}

	/** Produces a Karajan function invocation from a SwiftScript invocation.
	  * The Karajan invocation will have the same name as the SwiftScript
	  * function, in the 'vdl' Karajan namespace. Parameters to the
	  * Karajan function will differ from the SwiftScript parameters in
	  * a number of ways - read the source for the exact ways in which
	  * that happens.
	  */

	public StringTemplate function(Function func) throws Exception {
		StringTemplate funcST = template("function");
		funcST.setAttribute("name", func.getName());
		FunctionArgument[] arguments = func.getArgumentArray();
		for(int i = 0; i < arguments.length; i++ ) {
			FunctionArgument thisArgument = arguments[i];
			// handle each argument in turn
			// its either an expression or a function invocation
			// and we need to treat the argument differently
			// depending on which it is.
			if(thisArgument.isSetFunction()) {
				funcST.setAttribute("args", function(thisArgument.getFunction()));
			} else { // it is an expression or value
				StringTemplate argST = template("functionArg");
				String content = getText(thisArgument);
				setExprOrValue(argST, content, true);

				// TODO: make function deal with expr directly.
				StringTemplate exprST = (StringTemplate) argST.getAttribute("expr");

				// In the case that the supplied expression is an identifier,
				// we override the normal parameter passing (through expr
				// or value, and instead pass a var/path pair for that
				// identifier
				if (exprST != null && exprST.getName().equals("id")) {
					setPath(argST, content);
				}

				funcST.setAttribute("args",argST);
			}
		}

		return funcST;
	}

	/** Populates 'var' and 'path' attributes of the supplied StringTemplate
	 *  based on the supplied SwiftScript identifier.
	 *  The 'var' attribute will be populated with the base name of the
	 *  identifier. The 'path' attribute will be populated with the
	 *  remainder of the identifier, or will be left unset if there
	 *  is no further part to the identifier.
	 *
	 *  Examples:
	 *
	 *    content = "foo"      var = "foo"  path unset.
	 *    content = "foo[3]"   var = "foo"  path = "[3]"
	 *    content = "foo.bar"  var = "foo"  path = "bar"
	 *
	 *  @param st the StringTemplate to populate
	 *  @param content the SwiftScript variable identifier
	 */

	protected void setPath(StringTemplate st, String content) {
		if (content == null || content.trim().equals(""))
			return;

		// whitespace on the start or finish causes whitespace to
		// appear in bad places in the output karajan code
		content = content.trim();

		int i = content.indexOf('.');
		int j = content.indexOf('[');
		if (i != -1 || j != -1) {
			if (i == -1 || (j != -1 && j < i)) {
				st.setAttribute("var", content.substring(0, j));
				st.setAttribute("path", content.substring(j));
			}
			else {
				st.setAttribute("var", content.substring(0, i));
				st.setAttribute("path", content.substring(i + 1));
			}
		}
		else {
			st.setAttribute("var", content);
		}
	}

	/**
	 * default string dequote to true
	 */
	public void setExprOrValue(StringTemplate st, String content) throws Exception {
		setExprOrValue(st, content, true);
	}

	/**
	 * default reference to true, as most of the time we need the handle itself
	 * instead of its value
	 */
	public void setExprOrValue(StringTemplate st, String content, boolean dequote) throws Exception {
		setExprOrValue(st, content, dequote, true);
	}

	/** sets the 'expr' or 'value' attributes of the supplied StringTemplate
	 *  based on the supplied 'content' parameter.
	 *
	 *  This is used as a helper for the convention in Karajan.stg
	 *  templates that a template can have various attributes, one of which
	 *  will be set depending on the results of parsing the content
	 *  parameter: If the content is empty, then the 'nil' attribute will
	 *  be set on the supplied template; otherwise, if the content is
	 *  a constant, then that constant value will be placed in the
	 *  'value' attribute; otherwise, the 'expr' attribute will be set
	 *  to a karajan code fragment representing the input expression.
	 *
	 *  @param st A StringTemplate object to populate
	 *  @param content The content to parse and use for template population
	 *  @param dequote
	 *  @param reference If set to true, then the 'reference' attribute will
	 *    be set to 'true' when the content parses to a SwiftScript identifier.
	 */

	public void setExprOrValue(StringTemplate st, String content, boolean dequote, boolean reference)
			throws Exception {
		if (content == null || content.trim().equals("")) {
			st.setAttribute("nil", new Boolean(true));
			return;
		}
		StringTemplate valueST = m_exprParser.parse(content);
		String name = (String) valueST.getName();
		if (dequote && name.equals("sConst")) {
			st.setAttribute("value", valueST.getAttribute("innervalue"));
			return;
		}
		if (name.endsWith("Const")) {
			st.setAttribute("value", valueST.getAttribute("value"));
			return;
		}
		if (reference && name.equals("id")) {
			valueST.setAttribute("reference", new Boolean(true));
		}
		st.setAttribute("expr", valueST);
	}

	public String getText(XmlObject xo) {
		XmlCursor xc = xo.newCursor();
		while (!xc.toNextToken().isNone()) {
			if (xc.isText()) {
				// System.out.println("content - " + xc.getTextValue());
				return xc.getTextValue();
			}
		}
		return null;
	}

	protected void markDataset(StringTemplate exprST, StringTemplate st, boolean isInput) {
		if (exprST == null || st == null)
			return;
		if (exprST.getName().equals("id")) {
			// variable reference, mark the variable
			StringTemplate varST = (StringTemplate) exprST.getAttribute("var");
			markDataset((String) varST.getAttribute("var"), st, isInput);
		}
		else {
			// an expression, mark all subelements
			Map subSTMap = exprST.getAttributes();
			for (Iterator it = subSTMap.values().iterator(); it.hasNext();) {
				Object sub = it.next();
				if (sub instanceof StringTemplate)
					markDataset((StringTemplate) sub, st, isInput);
			}
		}
	}

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
						checkAssign(name, declST, st, isInput);
						return;
					}
				} else
				if (declName.equals("variable")) {
					if (declST.getAttribute("name").equals(name)) {
						if (declST.getAttribute("nil") != null)
							return;
						checkAssign(name, declST, st, isInput);
						return;
					}
				} else
				if (declName.equals("dataset")) {
					if (declST.getAttribute("name").equals(name)) {
						markDatasetParam(declST, st, isInput);
						return;
					}
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
							checkAssign(name, declST, st, isInput);
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
							checkAssign(name, declST, st, isInput);
							return;
						}
					} else
					if (declName.equals("dataset")) {
						if (declST.getAttribute("name").equals(name)) {
							markDatasetParam(declST, st, isInput);
							return;
						}
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

	protected void checkAssign(String name, StringTemplate assignST, StringTemplate st,
			boolean isInput) {
		if (!isInput) {
			throw new RuntimeException("The variable " + name + " can not be used as an output!\n"
					+ assignST);
		}
		// stop if it is primitive value
		if (assignST.getAttribute("value") != null)
			return;
		StringTemplate exprST = (StringTemplate) assignST.getAttribute("expr");

		if (exprST != null) {
			// now it comes from another source
			markDataset(exprST, st, isInput);
		}
		else {
			// assigned value is an array
			StringTemplate arrayST = (StringTemplate) assignST.getAttribute("array");
			if (arrayST != null) {
				// get elements of the array
				Object elems = arrayST.getAttribute("elements");
				if (elems instanceof StringTemplate) {
					// just one single element
					StringTemplate elemST = (StringTemplate) elems;
					if (elemST.getAttribute("value") != null)
						return;
					StringTemplate elemExprST = (StringTemplate) elemST.getAttribute("expr");
					markDataset(elemExprST, st, isInput);
				}
				else {
					Iterator it = ((List) elems).iterator();
					while (it.hasNext()) {
						StringTemplate elemST = (StringTemplate) it.next();
						if (elemST.getAttribute("value") != null)
							continue;
						StringTemplate elemExprST = (StringTemplate) elemST.getAttribute("expr");
						markDataset(elemExprST, st, isInput);
					}
				}
			}
			else {
				// assigned value is a range
				StringTemplate rangeST = (StringTemplate) assignST.getAttribute("range");
				if (rangeST != null) {
					StringTemplate fromST = (StringTemplate) rangeST.getAttribute("from");
					StringTemplate fromExprST = (StringTemplate) fromST.getAttribute("expr");
					markDataset(fromExprST, st, isInput);
					StringTemplate toST = (StringTemplate) rangeST.getAttribute("to");
					StringTemplate toExprST = (StringTemplate) toST.getAttribute("expr");
					markDataset(toExprST, st, isInput);
					StringTemplate stepST = (StringTemplate) rangeST.getAttribute("step");
					if (stepST != null) {
						StringTemplate stepExprST = (StringTemplate) stepST.getAttribute("expr");
						markDataset(stepExprST, st, isInput);
					}
				}
			}
		}
	}

	protected void markFunction(StringTemplate funcST, StringTemplate st) {
		// mark all the dataset references in a function as input
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
	protected void markDatasetParam(StringTemplate datasetST, StringTemplate st, boolean isInput) {
		if (datasetST == null)
			return;

		// process file mapping
		StringTemplate mappingST = (StringTemplate) datasetST.getAttribute("file");
		if (mappingST == null) {
			mappingST = (StringTemplate) datasetST.getAttribute("mapping");
			if (mappingST == null)
				return;
		}

		// mark input as true or false accordingly
		Boolean value = new Boolean(isInput);

		Object params = mappingST.getAttribute("params");
		if (params != null) {
			if (params instanceof StringTemplate) {
				StringTemplate paramST = (StringTemplate) params;
				if (paramST.getAttribute("name").equals("input")) {
					if (!isInput) {
						// mark as false
						paramST.removeAttribute("value");
						paramST.setAttribute("value", new Boolean(false));
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
				// a list of params
				Iterator it = ((List) params).iterator();
				while (it.hasNext()) {
					StringTemplate paramST = (StringTemplate) it.next();
					boolean foundInput = false;
					if (paramST.getAttribute("name").equals("input")) {
						if (!isInput) {
							// mark as false
							paramST.removeAttribute("value");
							paramST.setAttribute("value", new Boolean(false));
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
		}
		StringTemplate iparamST = template("vdl_parameter");
		iparamST.setAttribute("name", "input");
		iparamST.setAttribute("value", value);
		mappingST.setAttribute("params", iparamST);
	}
}
