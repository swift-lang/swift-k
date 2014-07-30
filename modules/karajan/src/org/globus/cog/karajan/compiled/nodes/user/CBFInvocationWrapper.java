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
 * Created on Jan 11, 2013
 */
package org.globus.cog.karajan.compiled.nodes.user;

import java.util.List;

import k.rt.ExecutionException;
import k.rt.FutureObject;
import k.rt.KRunnable;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ChannelRef;

public class CBFInvocationWrapper extends InvocationWrapper {

	public CBFInvocationWrapper(Function fn) {
		super(fn);
	}

	@Override
	protected ChannelRef<?> makeMixedChannel(int index, List<Object> staticValues) {
		return new ChannelRef.MixedFuture<Object>(index, staticValues);
	}

	@Override
	protected ChannelRef<?> makeDynamicChannel(String name, int index) {
		return new ChannelRef.DynamicFuture<Object>(name, index);
	}
	
	protected ChannelRef<Object> makeArgMappingFixedChannel(int firstDynamicIndex, int dynamicCount, int vargsIndex) {
		return new ChannelRef.ArgMappingFixedFuture<Object>(firstDynamicIndex, dynamicCount, vargsIndex);
	}

	protected ChannelRef<Object> makeArgMappingChannel(int firstDynamicIndex, int dynamicCount, int vargsIndex) {
		return new ChannelRef.ArgMappingFuture<Object>(firstDynamicIndex, dynamicCount, vargsIndex);
	}
	
	@Override
	public void run(LWThread thr) throws ExecutionException {
		int ec = childCount();
        int i = thr.checkSliceAndPopState();
        FutureObject fo = (FutureObject) thr.popState();
        Stack stack = thr.getStack();
        try {
	        switch (i) {
	        	case 0:
	        		initializeArgs(stack);
	        		fo = new FutureObject();
	        		final FutureObject ffo = fo;
	        		LWThread nt = thr.fork(new KRunnable() {
						@Override
						public void run(LWThread thr) {
							try {
								runBody(thr);
								ffo.setValue(true);
							}
							catch (ExecutionException e) {
								ffo.fail(e);
							}
							catch (RuntimeException e) {
								ffo.fail(e);
							}
						}
	        		});
	        		nt.start();
	        		i++;
	        	default:
			            for (; i <= ec; i++) {
			            	runChild(i - 1, thr);
			            }
			            closeChannels(thr.getStack());
			            i = Integer.MAX_VALUE;
	        	case Integer.MAX_VALUE:
	        		fo.getValue();
		    }
        }
        catch (Yield y) {
        	y.getState().push(fo);
            y.getState().push(i);
            throw y;
        }
	}

	private void closeChannels(Stack stack) {
		List<ChannelRef<?>> l = this.getChannelParams();
		if (l != null) {
			for (ChannelRef<?> r : l) {
				r.close(stack);
			}
		}
	}

	@Override
	protected void runBody(LWThread thr) {
		super.runBody(thr);
	}

	
}
