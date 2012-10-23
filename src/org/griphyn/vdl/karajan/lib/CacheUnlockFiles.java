/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Dec 28, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.cache.CacheReturn;
import org.griphyn.vdl.karajan.lib.cache.File;
import org.griphyn.vdl.karajan.lib.cache.VDLFileCache;
import org.griphyn.vdl.mapping.AbsFile;

public class CacheUnlockFiles extends CacheFunction {
	public static final Arg FILE = new Arg.Positional("files");
	public static final Arg DIR = new Arg.Positional("dir");
	public static final Arg HOST = new Arg.Positional("host");
	public static final Arg FORCE = new Arg.Optional("force", Boolean.TRUE);

	static {
		setArguments(CacheUnlockFiles.class, new Arg[] { FILE, DIR, HOST, FORCE });
	}

	public void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		List pairs = TypeUtil.toList(FILE.getValue(stack));
		String dir = TypeUtil.toString(DIR.getValue(stack));
		Object host = HOST.getValue(stack);
		VDLFileCache cache = getCache(stack);
		List rem = new ArrayList();
		
		Iterator i = pairs.iterator();
		while (i.hasNext()) {
			String file = (String) i.next();
			File f = new File(new AbsFile(file).getPath(), dir, host, 0);
			CacheReturn cr = cache.unlockEntry(f, TypeUtil.toBoolean(FORCE.getValue(stack)));
			rem.addAll(cr.remove);
		}
		super.partialArgumentsEvaluated(stack);
		stack.setVar(CACHE_FILES_TO_REMOVE, rem);
		startRest(stack);
	}
}
