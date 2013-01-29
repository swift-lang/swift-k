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

import java.util.Collection;

import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.scheduler.TaskConstraints;
import org.griphyn.vdl.karajan.lib.cache.CacheMapAdapter;
import org.griphyn.vdl.util.FQN;

public class JobConstraints extends CacheFunction {
    private ArgRef<String> tr;
    private ArgRef<Collection<String>> stagein;
    private ChannelRef<Object> cr_vargs;
    
	@Override
    protected Signature getSignature() {
        return new Signature(params("tr", optional("stagein", null)), returns(channel("...", 1)));
    }
	
	private static final String[] STRING_ARRAY = new String[0];

	@Override
    public void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		String tr = this.tr.getValue(stack);
		String[] filenames = null;
		Collection<String> c = this.stagein.getValue(stack);
		if (c != null) {
			filenames = c.toArray(STRING_ARRAY);
		}
		TaskConstraints tc = new TaskConstraints();
		tc.addConstraint("tr", tr);
		tc.addConstraint("trfqn", new FQN(tr));
		if (filenames != null) {
			tc.addConstraint("filenames", filenames);
			tc.addConstraint("filecache", new CacheMapAdapter(getCache(stack)));
		}
		cr_vargs.append(stack, tc);
	}
}
