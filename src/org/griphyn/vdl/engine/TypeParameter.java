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
 * Created on Mar 13, 2015
 */
package org.griphyn.vdl.engine;

import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.impl.TypeImpl;

public class TypeParameter extends TypeImpl {
    private Type binding;
    
    public TypeParameter(String name) {
        super(name);
    }
    
    public boolean tryBindOrMatch(Type type) {
        if (this.binding != null) {
            return this.binding.equals(type);
        }
        else {
            this.binding = type;
            return true;
        }
    }
    
    public void clearBinding() {
        this.binding = null;
    }

    @Override
    public boolean canBeAssignedTo(Type type) {
        return tryBindOrMatch(type);
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        return tryBindOrMatch(type);
    }
}
