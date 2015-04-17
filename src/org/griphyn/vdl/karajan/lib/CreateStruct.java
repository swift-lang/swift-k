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

import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DuplicateMappingChecker;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.RootFutureStructDataNode;
import org.griphyn.vdl.type.Field;

public class CreateStruct extends AbstractCreateKVStruct<String> {
    @Override
    protected DSHandle newVar(Field field, DuplicateMappingChecker dmChecker) {
        AbstractDataNode h = new RootFutureStructDataNode(field, dmChecker);
        h.initialize();
        return h;
    }

    @Override
    protected DSHandle getField(DSHandle var, String key) throws NoSuchFieldException {
        return var.getField(key);
    }
}
