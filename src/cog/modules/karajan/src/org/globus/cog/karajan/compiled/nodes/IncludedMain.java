// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 6, 2003
 *
 */
package org.globus.cog.karajan.compiled.nodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.NamedValue;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.parser.WrapperNode;

public class IncludedMain extends FramedInternalFunction {
	public static final Logger logger = Logger.getLogger(IncludedMain.class);
	
	private ChannelRef<Object> c_stdout;
	private ChannelRef<Object> c_stderr;
	private ChannelRef<NamedValue> c_export;
	private ChannelRef<Object> c_vargs;
	private ChannelRef<Object> cr_stdout;
	private ChannelRef<Object> cr_stderr;
	private ChannelRef<NamedValue> cr_export;
	private ChannelRef<Object> cr_vargs;
	
	private List<Object> exports;
	
	private String fileName;

	@Override
	protected Signature getSignature() {
		return new Signature(
				params(channel("stdout"), channel("stderr"), channel("export"), channel("..."))
		);
	}

	@Override
	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		fileName = (String) w.getProperty(WrapperNode.FILENAME);
		return super.compile(w, scope);
	}
	
	@Override
	protected void addLocals(Scope scope) {
		scope.addVar(Namespace.VAR_NAME, "");
		super.addLocals(scope);
	}
	
	

	@Override
	protected void addParams(WrapperNode w, Signature sig, Scope scope, List<Param> channels,
			List<Param> optional, List<Param> positional) throws CompilationException {
		super.addParams(w, sig, scope, channels, optional, positional);
		Scope prev = scope.getRoot().getImportScope();
        
        cr_export = bind(prev, scope, "export");
        cr_stdout = bind(prev, scope, "stdout");
        cr_stderr = bind(prev, scope, "stderr");
        cr_vargs = bind(prev, scope, "...");
	}

	private <T> ChannelRef<T> bind(Scope scope, Scope prev, String name) {
		Var.Channel r = prev.lookupChannel(name);
		Var.Channel p = scope.lookupChannel(name);
		
		p.setValue(r.getValue());
		return prev.getChannelRef(p);
	}

	@Override
	protected void initializeArgs(Stack stack) {
		super.initializeArgs(stack);
		c_export.set(stack, cr_export.get(stack));
		c_stdout.set(stack, cr_stdout.get(stack));
		c_stderr.set(stack, cr_stderr.get(stack));
		c_vargs.set(stack, cr_vargs.get(stack));
	}

	public List<Object> getExports() {
		return exports;
	}

	public void setExports(List<Object> exports) {
		this.exports = exports;
	}

	@Override
	public String getFileName() {
		return fileName;
	}
	
	public void dump(File file) throws IOException {
		PrintStream ps = new PrintStream(new FileOutputStream(file));
		dump(ps, 0);
		ps.close();
	}
}