// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 11, 2004
 */
package org.globus.cog.karajan.compiled.nodes.functions;


public class Logic {
	
	public static class Or extends BinaryOp<Boolean, Boolean> {
		@Override
		protected Boolean value(Boolean v1, Boolean v2) {
			return v1 || v2;
		}
	}
	
	public static class And extends BinaryOp<Boolean, Boolean> {
		@Override
		protected Boolean value(Boolean v1, Boolean v2) {
			return v1 && v2;
		}
	}
	
	public static class Not extends UnaryOp<Boolean, Boolean> {
		@Override
		protected Boolean value(Boolean v1) {
			return !v1;
		}
	}
}
