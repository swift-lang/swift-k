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
package org.griphyn.vdl.karajan.lib.swiftscript;

import k.rt.Stack;

import org.griphyn.vdl.karajan.FileNameResolver;
import org.griphyn.vdl.karajan.FileNameResolver.MultiMode;
import org.griphyn.vdl.karajan.FileNameResolver.Transform;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;

public class FileNames extends FileName {

    @Override
	public Object function(Stack stack) {
        AbstractDataNode var = this.var.getValue(stack);
        
        if (!inAppInvocation) {
            try {
                var.waitForAll(this);
            }
            catch (DependentException e) {
                return NodeFactory.newRoot(Field.GENERIC_ANY, e);
            }
        }
    	
        DSHandle result;
        
        if (inAppInvocation) {
            result = NodeFactory.newRoot(Field.GENERIC_ANY, 
                new FileNameResolver(var, MultiMode.SEPARATE, Transform.REMOTE));
        }
        else {
            result = NodeFactory.newRoot(Field.GENERIC_STRING_ARRAY, 
                new FileNameResolver(var, MultiMode.SEPARATE, Transform.NONE).toStringList());
        }
		
		if (PROVENANCE_ENABLED) {
		    int provid = SwiftFunction.nextProvenanceID();
		    logProvenanceParameter(provid, var, "input");
		    logProvenanceResult(provid, result, "filenames");
		}

		return result;
	}
}
