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
 * Created on Mar 11, 2013
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.VarRef;

class StaticRefCount<T> {
    public static final Logger logger = Logger.getLogger(StaticRefCount.class);
    
    public final VarRef<T> ref;
    public final int count;

    public StaticRefCount(VarRef<T> ref, int count) {
        this.ref = ref;
        this.count = count;
    }
    
     public static <S> List<StaticRefCount<S>> build(Scope scope, String refs, boolean ignoreStaticRefs) {
        if (refs == null) {
            return null;
        }
        List<StaticRefCount<S>> l = new ArrayList<StaticRefCount<S>>();
        String name = null;
        boolean flip = true;
        StringTokenizer st = new StringTokenizer(refs);
        while (st.hasMoreTokens()) {
            if (flip) {
                name = st.nextToken();
            }
            else {
                int count = Integer.parseInt(st.nextToken());
                VarRef<S> ref = scope.getVarRef(name);
                if (ref.isStatic() && ignoreStaticRefs) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skipping refs for " + name + " beacuse it is static");
                    }
                }
                else {
                    l.add(new StaticRefCount<S>(ref, count));
                }
            }
            flip = !flip;
        }
        return l;
    }
}