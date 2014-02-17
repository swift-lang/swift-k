
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.compiled.nodes.grid;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractFunction;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.ContactSet;


public class GridNode extends AbstractFunction {
	private ChannelRef<BoundContact> c_vargs;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params("..."));
	}
	
	@Override
	public Object function(Stack stack) {
		ContactSet grid = new ContactSet();
		for (BoundContact bc : c_vargs.get(stack)) {
			grid.addContact(bc);
		}
		return grid;
	}
}
