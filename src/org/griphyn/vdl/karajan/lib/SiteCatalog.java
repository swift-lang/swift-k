//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 7, 2013
 */
package org.griphyn.vdl.karajan.lib;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.griphyn.vdl.util.SwiftConfig;

public class SiteCatalog extends AbstractSingleValuedFunction {
    private ArgRef<SwiftConfig> config;

    @Override
    protected Param[] getParams() {
        return params("config");
    }

    @Override
    public Object function(Stack stack) {
        SwiftConfig config = this.config.getValue(stack);
        return config.getSites();
    }
}
