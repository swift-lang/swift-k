// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser.atoms;

import org.globus.cog.karajan.parser.EvaluationContext;
import org.globus.cog.karajan.parser.EvaluationException;

public abstract class AbstractBinaryNumericEvaluator extends AbstractEvaluator {

	public AbstractBinaryNumericEvaluator(Object o1, Object o2) {
		super(2);
		setChild(0, o1);
		setChild(1, o2);
	}

	public final Object evaluate(final EvaluationContext variables) throws EvaluationException {
		final Number v1 = toNumber(evalChild(0, variables));
		final Number v2 = toNumber(evalChild(1, variables));
		return compute(v1, v2);
	}

	private Number toNumber(final Object object) throws EvaluationException {
		if (object instanceof Number) {
			return (Number) object;
		}
		else if (object instanceof String) {
			return new Double((String) object);
		}
		throw new EvaluationException("Could not convert value to number: " + object);
	}

	public abstract Number compute(Number n1, Number n2);

}