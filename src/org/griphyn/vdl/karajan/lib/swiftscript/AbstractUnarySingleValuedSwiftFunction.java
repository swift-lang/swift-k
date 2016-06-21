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
 * Created on Apr 17, 2014
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.util.SwiftConfig;

// TODO: this and UnaryOp seem to overlap a lot
public abstract class AbstractUnarySingleValuedSwiftFunction<R, P> extends AbstractSingleValuedSwiftFunction {
    public static final boolean PROVENANCE_ENABLED = SwiftConfig.getDefault().isProvenanceEnabled();
    
    private final String name;
    private final Field fieldType;
    private ArgRef<AbstractDataNode> param;
    
    protected AbstractUnarySingleValuedSwiftFunction(String name, Field fieldType) {
        this.name = name;
        this.fieldType = fieldType;
    }
    
    @Override
    protected Field getFieldType() {
        return fieldType;
    }

    @Override
    protected Signature getSignature() {
        return new Signature(params("param"), returns(channel("...", 1)));
    }
                
    @Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        if (this.param.isStatic()) {
            AbstractDataNode param = this.param.getValue();
            if (param.isClosed()) {
                DSHandle handle = function(param);
                if (staticReturn(scope, handle)) {
                    // only log if the static return actually succeeded
                    if (PROVENANCE_ENABLED) {
                        int provid = SwiftFunction.nextProvenanceID();
                        SwiftFunction.logProvenanceParameter(provid, param, "param");
                        SwiftFunction.logProvenanceResult(provid, handle, name);
                    }
                    return null;
                }
            }
        }
        return super.compileBody(w, argScope, scope);
    }

    @Override
    public Object function(Stack stack) {
        AbstractDataNode hparam = param.getValue(stack);
        DSHandle ret = function(hparam);
        if (PROVENANCE_ENABLED) {
            int provid = SwiftFunction.nextProvenanceID();
            SwiftFunction.logProvenanceParameter(provid, hparam, "param");
            SwiftFunction.logProvenanceResult(provid, ret, name);
        }
        return ret;
    }

    private DSHandle function(AbstractDataNode hparam) {
        P vp = SwiftFunction.unwrap(this, hparam);
        DSHandle handle = NodeFactory.newRoot(getFieldType(), function(vp));
        return handle;
    }

    protected abstract R function(P v);
}
