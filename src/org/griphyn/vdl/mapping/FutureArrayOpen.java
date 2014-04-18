//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 17, 2014
 */
package org.griphyn.vdl.mapping;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.globus.cog.karajan.workflow.futures.FutureListener;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.karajan.ArrayIndexFutureList;

public class FutureArrayOpen extends FutureNotYetAvailable {
    public FutureArrayOpen(ArrayIndexFutureList f) {
        super(new Wrapper(f));
    }

    @Override
    public Future getFuture() {
        return super.getFuture();
    }
    
    private static class Wrapper implements Future {
        private final ArrayIndexFutureList f;

        public Wrapper(ArrayIndexFutureList f) {
            this.f = f;
        }

        @Override
        public void close() {
            f.close();
        }

        @Override
        public boolean isClosed() {
            return f.isClosed();
        }

        @Override
        public Object getValue() {
            return f.getValue();
        }

        @Override
        public void fail(FutureEvaluationException e) {
            f.fail(e);
        }

        @Override
        public void addModificationAction(FutureListener target, VariableStack stack) {
            f.addModificationAction(target, stack, false);
        }
    }
}
