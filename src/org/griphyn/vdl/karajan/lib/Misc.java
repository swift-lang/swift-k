package org.griphyn.vdl.karajan.lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.RootDataNode;

import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.mapping.InvalidPathException;

import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;

public class Misc extends FunctionsCollection {

	private static final Logger logger = Logger.getLogger(FunctionsCollection.class);

	public static final SwiftArg PA_INPUT = new SwiftArg.Positional("input");
	public static final SwiftArg PA_PATTERN = new SwiftArg.Positional("regexp");

	static {
		//Don't use SwiftArg.VARGS here, since Karajan won't recognize it
		setArguments("vdl_strcat", new Arg[] { Arg.VARGS });
		setArguments("vdl_strcut", new Arg[] { PA_INPUT, PA_PATTERN });
	}

	public DSHandle vdl_strcat(VariableStack stack) throws ExecutionException, NoSuchTypeException,
			InvalidPathException {
		//Use SwiftArg.VARGS to unwrap DSHandles automatically
		Object[] args = SwiftArg.VARGS.asArray(stack);
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			buf.append(TypeUtil.toString(args[i]));
		}
		DSHandle handle = new RootDataNode("string");
		handle.setValue(buf.toString());
		handle.closeShallow();
		return handle;
	}

	public DSHandle vdl_strcut(VariableStack stack) throws ExecutionException, NoSuchTypeException,
			InvalidPathException {
		String inputString = TypeUtil.toString(PA_INPUT.getValue(stack));
		String pattern = TypeUtil.toString(PA_PATTERN.getValue(stack));
		if (logger.isDebugEnabled()) {
			logger.debug("strcut will match '" + inputString + "' with pattern '" + pattern + "'");
		}

		String group;
		try {
			Pattern p = Pattern.compile(pattern);
			// TODO probably should memoize this?

			Matcher m = p.matcher(inputString);
			m.find();
			group = m.group(1);
		}
		catch (IllegalStateException e) {
			throw new ExecutionException("@strcut could not match pattern " + pattern
					+ " against string " + inputString, e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("strcut matched '" + group + "'");
		}
		DSHandle handle = new RootDataNode("string");
		handle.setValue(group);
		handle.closeShallow();
		return handle;
	}
}
