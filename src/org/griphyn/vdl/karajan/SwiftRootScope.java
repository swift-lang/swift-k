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
 * Created on Jan 6, 2013
 */
package org.griphyn.vdl.karajan;

import java.io.File;

import k.rt.Context;

import org.globus.cog.karajan.analyzer.RootScope;
import org.globus.cog.karajan.util.KarajanProperties;
import org.griphyn.vdl.mapping.DuplicateMappingChecker;
import org.griphyn.vdl.util.SwiftConfig;

public class SwiftRootScope extends RootScope {

    public SwiftRootScope(KarajanProperties props, String file, Context context) {
        super(props, file, context);
        context.setAttribute("SWIFT:DM_CHECKER", new DuplicateMappingChecker(
        		(SwiftConfig) context.getAttribute("SWIFT:CONFIG")));
        
        addVar("PATH_SEPARATOR", File.separator);
        addVar("SWIFT:DRY_RUN", context.getAttribute("SWIFT:DRY_RUN"));
        addVar("SWIFT:RUN_ID", context.getAttribute("SWIFT:RUN_ID"));
        addVar("SWIFT:SCRIPT_NAME", context.getAttribute("SWIFT:SCRIPT_NAME"));
        addVar("SWIFT:DEBUG_DIR_PREFIX", context.getAttribute("SWIFT:DEBUG_DIR_PREFIX"));
    }
}
