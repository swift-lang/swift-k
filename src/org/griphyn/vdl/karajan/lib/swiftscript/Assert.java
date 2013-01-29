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


package org.griphyn.vdl.karajan.lib.swiftscript;

// import org.apache.log4j.Logger;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.karajan.AssertFailedException;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.type.Types;

/**
    Throw AssertionException if input is false or 0. 
    Optional second argument is string message printed on failure. 
 */
public class Assert extends SwiftFunction {
    private ArgRef<AbstractDataNode> value;
    private ArgRef<AbstractDataNode> message;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("value", "message"));
    }

    @Override
    public Object function(Stack stack) {
        AbstractDataNode hmessage = this.message.getValue(stack);
        hmessage.waitFor(this);
        String message = (String) hmessage.getValue();
        AbstractDataNode hvalue = this.value.getValue(stack);
        hvalue.waitFor(this);
                 
        checkAssert(hvalue, message);
        
        return null;
    }

    private void checkAssert(DSHandle value, String message) {
        boolean success = true; 
        if (value.getType() == Types.BOOLEAN) { 
            if (! (Boolean) value.getValue())
                success = false;
        }
        else if (value.getType() == Types.INT) {
            double d = ((Integer) value.getValue()).intValue();
            if (d == 0)
                success = false;
        } 
        else {
            throw new ExecutionException(this, "First argument to assert() must be boolean or int!");
        }
        if (!success) {
            throw new AssertFailedException(message);
        }
    }
}
