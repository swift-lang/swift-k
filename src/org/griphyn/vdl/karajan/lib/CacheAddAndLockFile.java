/*
 * Created on Dec 29, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.cache.CacheReturn;
import org.griphyn.vdl.karajan.lib.cache.File;
import org.griphyn.vdl.karajan.lib.cache.RemovalListener;
import org.griphyn.vdl.karajan.lib.cache.VDLFileCache;

public class CacheAddAndLockFile extends CacheFunction implements RemovalListener {
	public static final Arg FILE = new Arg.Positional("file");
	public static final Arg DIR = new Arg.Positional("dir");
	public static final Arg HOST = new Arg.Positional("host");
	public static final Arg SIZE = new Arg.Positional("size");

	static {
		setArguments(CacheAddAndLockFile.class, new Arg[] { FILE, DIR, HOST, SIZE });
	}
	
	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		String file = TypeUtil.toString(FILE.getValue(stack));
		String dir = TypeUtil.toString(DIR.getValue(stack));
		Object host = HOST.getValue(stack);
		long size = TypeUtil.toLong(SIZE.getValue(stack));
		VDLFileCache cache = CacheFunction.getCache(stack);
		CacheReturn cr = cache.addAndLockEntry(new File(file, dir, host, size));
		if (cr.alreadyCached) {
			if (cr.cached.isLockedForRemoval()) {
				//then we must wait for its removal
				cr.cached.addRemovalListener(this, stack);
			}
			else {
				complete(stack);
			}
		}
		else {
			super.partialArgumentsEvaluated(stack);
			stack.setVar(CACHE_FILES_TO_REMOVE, cr.remove);
			startRest(stack);
		}
	}

	public void fileRemoved(File f, Object param) {
		VariableStack stack = (VariableStack) param;
		try {
			partialArgumentsEvaluated(stack);
		}
		catch (ExecutionException e) {
			failImmediately(stack, e);
		}
	}
}
