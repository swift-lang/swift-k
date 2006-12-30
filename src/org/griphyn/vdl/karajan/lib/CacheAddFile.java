/*
 * Created on Dec 28, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.cache.CacheReturn;
import org.griphyn.vdl.karajan.lib.cache.File;
import org.griphyn.vdl.karajan.lib.cache.VDLFileCache;

public class CacheAddFile extends CacheFunction {
	public static final Arg FILE = new Arg.Positional("file");
	public static final Arg DIR = new Arg.Positional("dir");
	public static final Arg HOST = new Arg.Positional("host");
	public static final Arg SIZE = new Arg.Positional("size");

	static {
		setArguments(CacheAddFile.class, new Arg[] { FILE, DIR, HOST, SIZE });
	}

	public void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		String file = TypeUtil.toString(FILE.getValue(stack));
		String dir = TypeUtil.toString(DIR.getValue(stack));
		Object host = HOST.getValue(stack);
		long size = TypeUtil.toLong(SIZE.getValue(stack));
		VDLFileCache cache = getCache(stack);
		File f = new File(file, dir, host, size);
		CacheReturn cr = cache.addEntry(f);
		if (cr.alreadyCached) {
			throw new ExecutionException("The cache already contains " + f + ".");
		}
		super.partialArgumentsEvaluated(stack);
		stack.setVar(CacheFunction.CACHE_FILES_TO_REMOVE, cr.remove);
		startNext(stack);
	}
}
