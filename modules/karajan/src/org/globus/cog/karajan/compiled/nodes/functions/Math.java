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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 11, 2004
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import org.globus.cog.karajan.util.NumericEqualityComparator;

public class Math {

	public static class LessOrEqual extends BinaryOp<Number, Boolean> {
		@Override
		protected Boolean value(Number v1, Number v2) {
			return v1.doubleValue() <= v2.doubleValue();
		}
	}
	
	public static class GreaterOrEqual extends BinaryOp<Number, Boolean> {
		@Override
		protected Boolean value(Number v1, Number v2) {
			return v1.doubleValue() >= v2.doubleValue();
		}
	}
	
	public static class LessThan extends BinaryOp<Number, Boolean> {
		@Override
		protected Boolean value(Number v1, Number v2) {
			return v1.doubleValue() < v2.doubleValue();
		}
	}
	
	public static class GreaterThan extends BinaryOp<Number, Boolean> {
		@Override
		protected Boolean value(Number v1, Number v2) {
			return v1.doubleValue() > v2.doubleValue();
		}
	}
	
	private static final NumericEqualityComparator NEC = new NumericEqualityComparator();

	public static class EqualsNumeric extends BinaryOp<Object, Boolean> {
		@Override
		protected Boolean value(Object v1, Object v2) {
			return NEC.equals(v1, v2);
		}
	}

	public static class Product extends BinaryOp<Number, Number> {
		@Override
		protected Number value(Number v1, Number v2) {
			return v1.doubleValue() * v2.doubleValue();
		}
	}
	
	public static class Quotient extends BinaryOp<Number, Number> {
		@Override
		protected Number value(Number v1, Number v2) {
			return v1.doubleValue() / v2.doubleValue();
		}
	}
	
	public static class Remainder extends BinaryOp<Number, Number> {
		@Override
		protected Number value(Number v1, Number v2) {
			return v1.doubleValue() % v2.doubleValue();
		}
	}
	
	public static class Square extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			double v = v1.doubleValue();
			return v * v;
		}
	}
	
	public static class Neg extends UnaryOp<Number, Number> {
        @Override
        protected Number value(Number v1) {
            return  -v1.doubleValue();
        }
    }
	
	public static class Sqrt extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.sqrt(v1.doubleValue());
		}
	}

	public static class Difference extends BinaryOp<Number, Number> {
		@Override
		protected Number value(Number v1, Number v2) {
			return v1.doubleValue() - v2.doubleValue();
		}
	}
	
	public static class Sum extends BinaryOp<Number, Number> {
		@Override
		protected Number value(Number v1, Number v2) {
			return v1.doubleValue() + v2.doubleValue();
		}
	}
	
	public static class Min extends BinaryOp<Number, Number> {
		@Override
		protected Number value(Number v1, Number v2) {
			return java.lang.Math.min(v1.doubleValue(), v2.doubleValue());
		}
	}
	
	public static class Max extends BinaryOp<Number, Number> {
		@Override
		protected Number value(Number v1, Number v2) {
			return java.lang.Math.max(v1.doubleValue(), v2.doubleValue());
		}
	}

	public static class Floor extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.floor(v1.doubleValue());
		}
	}
	
	public static class Round extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.round(v1.doubleValue());
		}
	}

	public static class Ln extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.log(v1.doubleValue());
		}
	}
	
	public static class Exp extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.exp(v1.doubleValue());
		}
	}
	
	public static class Sin extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.sin(v1.doubleValue());
		}
	}
	
	public static class Cos extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.cos(v1.doubleValue());
		}
	}
	
	public static class Tan extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.tan(v1.doubleValue());
		}
	}
	
	public static class ASin extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.asin(v1.doubleValue());
		}
	}
	
	public static class ACos extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.acos(v1.doubleValue());
		}
	}
	
	public static class ATan extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.atan(v1.doubleValue());
		}
	}
	
	public static class Sinh extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.sinh(v1.doubleValue());
		}
	}
	
	public static class Cosh extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.cosh(v1.doubleValue());
		}
	}
	
	public static class Tanh extends UnaryOp<Number, Number> {
		@Override
		protected Number value(Number v1) {
			return java.lang.Math.tanh(v1.doubleValue());
		}
	}
	
	public static class Pow extends BinaryOp<Number, Number> {
		@Override
		protected Number value(Number v1, Number v2) {
			return java.lang.Math.pow(v1.doubleValue(), v2.doubleValue());
		}
	}
	
	public static class Random extends NullaryOp<Number> {
		@Override
		protected Number value() {
			return java.lang.Math.random();
		}
	}
}
