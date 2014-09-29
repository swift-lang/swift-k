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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 4, 2013
 */
package org.griphyn.vdl.karajan.lib;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.functions.BinaryOp;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;

public abstract class SwiftBinaryOp extends BinaryOp<AbstractDataNode, DSHandle> {

    @Override
    public DSHandle function(Stack stack) {
        try {
        	AbstractDataNode v1 = this.v1.getValue(stack);
        	v1.waitFor(this);
        	AbstractDataNode v2 = this.v2.getValue(stack);
        	v2.waitFor(this);
            return value(v1, v2);
        }
        catch (DependentException e) {
            return NodeFactory.newRoot(getReturnType(), e);
        }
    }
    
    @Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        if (v1.isStatic() && v2.isStatic()) {
            AbstractDataNode v1s = v1.getValue();
            AbstractDataNode v2s = v2.getValue();
            if (v1s.isClosed() && v2s.isClosed()) {
                if (staticReturn(scope, value(v1s, v2s))) {
                    return null;
                }
            }
        }
        returnDynamic(scope);
        return this;
    }

    protected Field getReturnType() {
        return Field.GENERIC_ANY; 
    }
}
