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
package org.griphyn.vdl.karajan.lib;

import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.nodes.ReadRefWrapper;

public class UnwrapVariable extends Node {
	final static Logger logger = Logger.getLogger(UnwrapVariable.class);

	private VarRef<DSHandle> refStatic;
	private VarRef<ReadRefWrapper> ref;
	private ChannelRef<Object> _vargs;

	@Override
	public void run(LWThread thr) {
		Stack stack = thr.getStack();
		DSHandle h;
		if (refStatic != null) {
		    h = refStatic.getValue();
		}
		else {
		    ReadRefWrapper w = ref.getValue(stack);
		    h = w.getHandle();
		}
        _vargs.append(stack, h);
	}

	@SuppressWarnings("unchecked")
    @Override
    public Node compile(WrapperNode wn, Scope scope) throws CompilationException {
        super.compile(wn, scope);
        String name = wn.getNode(0).getText();
        
        VarRef<?> ref = scope.getVarRef(name);
        Var.Channel vargs = scope.lookupChannel(Param.VARGS);
        
        if (ref.isStatic()) {
            if (vargs.append(ref.getValue())) {
                return null;
            }
            else {
                this.refStatic = (VarRef<DSHandle>) ref;
                
                _vargs = scope.getChannelRef(vargs);
                return this;
            }
        }
        else {
            this.ref = (VarRef<ReadRefWrapper>) ref;
            vargs.appendDynamic();
            _vargs = scope.getChannelRef(vargs);
            return this;
        }
    }
}
