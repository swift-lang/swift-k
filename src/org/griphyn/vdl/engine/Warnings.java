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
 * Created on Nov 10, 2012
 */
package org.griphyn.vdl.engine;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.swift.parsetree.Node;

public class Warnings {
    public static final Logger logger = Logger.getLogger(Warnings.class);
    
    public static enum Type {
        DEPRECATION, 
        SHADOWING, 
        DATAFLOW,
        SITE,
        UNUSED
    }
    
    private static Set<String> warnings = new HashSet<String>();
    private static EnumSet<Type> enabledWarnings = EnumSet.noneOf(Type.class);
    
    static {
        enabledWarnings.add(Type.DEPRECATION);
        enabledWarnings.add(Type.DATAFLOW);
        enabledWarnings.add(Type.SITE);
        enabledWarnings.add(Type.UNUSED);
    }
    
    public static void warn(Type type, Node obj, String msg) {
        if (enabledWarnings.contains(type)) {
            if (!warnings.contains(msg)) {
                warnings.add(msg);
                msg = "Warning: " + msg + ", line " + obj.getLine();
                logger.info(msg);
                System.err.println(msg);
            }
        }
    }
    
    public static void warn(Type type, String msg) {
        if (enabledWarnings.contains(type)) {
            if (!warnings.contains(msg)) {
                warnings.add(msg);
                msg = "Warning: " + msg;
                logger.info(msg);
                System.err.println(msg);
            }
        }
    }
}
