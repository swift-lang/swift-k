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

import java.util.List;

import k.rt.Channel;
import k.rt.NullSink;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.parser.WrapperNode;

public class DynamicMain extends InternalFunction {
	public static final Logger logger = Logger.getLogger(DynamicMain.class);
	
	private ChannelRef<Object> c_stdout;
	private ChannelRef<Object> c_stderr;
	private ChannelRef<Object> c_export;
	private ChannelRef<Object> c_vargs;
	
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
	
	public void bindChannels(Channel<Object> rvargs, Channel<Object> rstdout,
			Channel<Object> rstderr, Stack stack) {
		c_vargs.set(stack, rvargs);
		c_stdout.set(stack, rstdout);
		c_stderr.set(stack, rstderr);
		c_export.set(stack, new NullSink());
	}
	
	@Override
	protected void initializeArgs(Stack stack) {
	}
	
	@Override
	protected void addLocals(Scope scope) {
		scope.addVar(Namespace.VAR_NAME, "");
		super.addLocals(scope);
	}

	@Override
	public String getFileName() {
		return fileName;
	}
}