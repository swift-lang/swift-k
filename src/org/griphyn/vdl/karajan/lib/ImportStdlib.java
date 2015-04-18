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
 * Created on Mar 6, 2015
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Map;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Scope.JavaDef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.engine.StandardLibrary;
import org.griphyn.vdl.mapping.nodes.RootClosedPrimitiveDataNode;
import org.griphyn.vdl.type.Field;

public class ImportStdlib extends InternalFunction {
    public static final String NS_PREFIX = "swiftscript";
    
    private ArgRef<Integer> version;

    @Override
    protected Signature getSignature() {
        return new Signature(params("version"));
    }

    @Override
    public Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
        Integer version = this.version.getValue();
        if (version == null) {
            throw new CompilationException(w, "Could not statically determine standard library version to import");
        }
        
        Map<org.griphyn.vdl.engine.Signature, Class<? extends Node>> functions;
        Map<String, Object> constants;
        
        if (version == 1) {
            functions = StandardLibrary.LEGACY.getDefs();
            constants = StandardLibrary.LEGACY.getConstants();
        }
        else {
            functions = StandardLibrary.NEW.getDefs();
            constants = StandardLibrary.NEW.getConstants();
        }
        
        for (Map.Entry<org.griphyn.vdl.engine.Signature, Class<? extends Node>> e : functions.entrySet()) {
            scope.parent.addDef(NS_PREFIX, e.getKey().getMangledName(), new JavaDef(e.getValue()));
        }
        
        for (Map.Entry<String, Object> e : constants.entrySet()) {
            scope.parent.addVar(e.getKey(), new RootClosedPrimitiveDataNode(inferField(w, e.getValue()), e.getValue()));
        }
        return null;
    }

    private Field inferField(WrapperNode w, Object value) throws CompilationException {
        if (value instanceof Double) {
            return Field.GENERIC_FLOAT;
        }
        if (value instanceof Integer) {
            return Field.GENERIC_INT;
        }
        if (value instanceof String) {
            return Field.GENERIC_STRING;
        }
        throw new CompilationException(w, "Cannot infer Swift type from value: '" + value + "'");
    }
}
