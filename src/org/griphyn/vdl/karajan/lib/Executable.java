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

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.swift.catalog.site.Application;
import org.globus.swift.catalog.site.SwiftContact;

public class Executable extends SwiftFunction {
    
    private ArgRef<String> tr;
    private ArgRef<SwiftContact> host;
    
	
	@Override
    protected Signature getSignature() {
        return new Signature(params("tr", "host"));
    }


    public Object function(Stack stack) {
		String tr = this.tr.getValue(stack);
		SwiftContact bc = this.host.getValue(stack);
		// at this point, a host has been allocated, so we already
		// know that the app is available on it
		Application app = bc.findApplication(tr);
		if (app.executableIsWildcard()) {
			return tr;
		}
		else {
			return app.getExecutable();
		}
	}
}
