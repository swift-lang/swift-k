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
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.scheduler.TaskConstraints;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.util.FQN;
import java.util.Collection;
import org.griphyn.vdl.karajan.lib.cache.CacheMapAdapter;

public class JobConstraints extends VDLFunction {
	public static final Arg A_TR = new Arg.Positional("tr");
	public static final Arg STAGE_IN = new Arg.Optional("stagein");
	
	static {
		setArguments(JobConstraints.class, new Arg[] { A_TR, STAGE_IN });
	}
	
	private static final String[] STRING_ARRAY = new String[0];

	public Object function(VariableStack stack) throws ExecutionException {
		String tr = TypeUtil.toString(A_TR.getValue(stack));
		String[] filenames = null;
		if (STAGE_IN.isPresent(stack)) {
			filenames = (String[]) ((Collection) STAGE_IN.getValue(stack)).toArray(STRING_ARRAY);
		}
		TaskConstraints tc = new TaskConstraints();
		tc.addConstraint("tr", tr);
		tc.addConstraint("trfqn", new FQN(tr));
		if (filenames != null) {
			tc.addConstraint("filenames", filenames);
			tc.addConstraint("filecache", new CacheMapAdapter(CacheFunction.getCache(stack)));
		}
		return tc;
	}
}
