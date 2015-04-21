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


package org.griphyn.vdl.karajan.lib;

import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.futures.FutureFault;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.mapping.file.ConcurrentMapper;
import org.griphyn.vdl.mapping.nodes.RootClosedArrayDataNode;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class CreateArray extends SetFieldValue {
	public static final Logger logger = Logger.getLogger(CreateArray.class);
	
	private ArgRef<Field> field;
	private ArgRef<Object> value;

	@Override
    protected Signature getSignature() {
        return new Signature(params("field", "value"));
    }

    public Object function(Stack stack) {
        Field field = this.field.getValue(stack);
		Object value = this.value.getValue(stack);
		try {
			if (!(value instanceof List)) {
				throw new RuntimeException("An array variable can only be initialized with a list of values");
			}

			Type type = field.getType();
			
			RootHandle handle = new RootClosedArrayDataNode(field, (List<?>) value, null);
			if (type.hasMappedComponents()) {
			    handle.init(new ConcurrentMapper());
			}
			else {
			    handle.init(null);
			}
			
			if (logger.isInfoEnabled()) {
                logger.info("CREATEARRAY array=" + handle.getIdentifier());
            }

			return handle;
		}
		catch (FutureFault e) {
		    throw e;
		}
		catch (Exception e) {
			throw new ExecutionException(this, e);
		}
	}
}
