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
