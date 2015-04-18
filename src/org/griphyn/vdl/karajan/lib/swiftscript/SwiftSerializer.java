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
 * Created on Apr 3, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import java.util.Map;

import org.globus.cog.karajan.compiled.nodes.Node;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.type.Type;

public interface SwiftSerializer {
    void writeData(String path, DSHandle src, Node owner, Map<String, Object> options);

    void checkParamType(Type type, Node owner);
}
