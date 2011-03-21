/*
 * Created on Dec 29, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.lib.cache.CacheReturn;
import org.griphyn.vdl.karajan.lib.cache.File;
import org.griphyn.vdl.karajan.lib.cache.VDLFileCache;

public class CacheAddAndLockFile extends CacheFunction {
	public static final String PFILE = "#pfile";

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
		File f = new File(file, dir, host, size);
		stack.setVar(PFILE, f);
		CacheReturn cr = cache.addAndLockEntry(f);
		if (cr.alreadyCached) {
			if (cr.cached.isLockedForProcessing()) {
				// then we must wait for it to be processed
				throw new FutureNotYetAvailable(cr.cached);
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

	protected void post(VariableStack stack) throws ExecutionException {
		File f = (File) stack.currentFrame().getVar(PFILE);
		if (f == null) {
			throw new ExecutionException("Weird inconsistency in " + this
					+ ". The file was not found on the current frame.");
		}
		VDLFileCache cache = CacheFunction.getCache(stack);
		cache.unlockFromProcessing(f);
		super.post(stack);
	}
	
	
    public void failed(VariableStack stack, ExecutionException e)
            throws ExecutionException {
    	VDLFileCache cache = CacheFunction.getCache(stack);
        cache.entryRemoved((File) stack.currentFrame().getVar(PFILE));
        super.failed(stack, e);
    }
}
