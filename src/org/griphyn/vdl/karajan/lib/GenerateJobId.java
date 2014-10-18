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
 * Created on Jul 18, 2010
 */
package org.griphyn.vdl.karajan.lib;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractFunction;
import org.globus.cog.karajan.compiled.nodes.functions.Misc.UID;

public class GenerateJobId extends AbstractFunction {
	private ArgRef<String> tr;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("tr"), returns(channel("...", 2)));
    }

    @Override
    public Object function(Stack stack) {
    	String tr = this.tr.getValue(stack);
        String uid = UID.nextUID();
        String jobdir = uid.substring(0, 1);
        ret(stack, removeSpecialChars(tr) + "-" + uid);
        ret(stack, jobdir);
        return null;
    }

    private String removeSpecialChars(String tr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tr.length(); i++) {
        	char c = tr.charAt(i);
        	if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
        		sb.append(c);
        	}
        	else {
        		sb.append('_');
        	}
        }
        return sb.toString();
    }
}
