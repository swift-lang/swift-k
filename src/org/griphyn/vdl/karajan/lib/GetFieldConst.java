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

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Types;

public class GetFieldConst extends AbstractSingleValuedFunction {
    private ArgRef<String> name;
    private ArgRef<String> type;

    
    @Override
    protected Param[] getParams() {
        return params("name", "type");
    }


    @Override
	public Object function(Stack stack) {
        String name = this.name.getValue(stack);
        String type = this.type.getValue(stack);
		try {
            return Field.Factory.getImmutableField(name, Types.getType(type));
        }
        catch (NoSuchTypeException e) {
            throw new ExecutionException("No such type: " + name, e);
        }
	}
}
