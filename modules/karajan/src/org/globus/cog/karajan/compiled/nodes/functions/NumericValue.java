/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;

public class NumericValue extends Node {
	private ChannelRef<Object> _vargs_r;
    private Number value;

    @Override
    public void run(LWThread thr) {
        _vargs_r.append(thr.getStack(), value);
    }

    @Override
    public Node compile(WrapperNode w, Scope scope) throws CompilationException {
    	super.compile(w, scope);
    	if (w.getText().indexOf(".") >= 0) {
    		value = Double.parseDouble(w.getText());
    	}
    	else {
    	    value = Integer.parseInt(w.getText());
    	}
        
        Var.Channel cv = scope.lookupChannel(Param.VARGS);  
        
        if (cv.append(value)) {
            return null;
        }
        else {
            _vargs_r = scope.getChannelRef(cv);
            return this;
        }
    }
}