// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser.atoms;


public class Subtract extends AbstractBinaryNumericAtom {

	public static class Eval extends AbstractBinaryNumericEvaluator {

		public Eval(Object v1, Object v2) {
			super(v1, v2);
		}

		public Number compute(Number v1, Number v2) {
			return new Double(v2.doubleValue() - v1.doubleValue());
		}

		public String toString() {
			return "-" + super.toString();
		}
	}

	protected AbstractBinaryNumericEvaluator newEval(Object v1, Object v2) {
		return new Eval(v1, v2);
	}
}