/*
 * Created on Dec 29, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.cache.File;
import org.griphyn.vdl.karajan.lib.cache.VDLFileCache;

public class CacheFileRemoved extends CacheFunction {
	public static final Arg PATH = new Arg.Positional("path");
	public static final Arg HOST = new Arg.Positional("host");

	static {
		setArguments(CacheFileRemoved.class, new Arg[] { PATH, HOST });
	}
	
	public void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		String path = TypeUtil.toString(PATH.getValue(stack));
		Object host = HOST.getValue(stack);
		VDLFileCache cache = getCache(stack);
		File f = new File(path, host, 0);
		cache.entryRemoved(f);
		complete(stack);
	}

}
