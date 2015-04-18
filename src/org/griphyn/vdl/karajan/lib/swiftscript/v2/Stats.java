//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 7, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript.v2;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;

public class Stats {
    
    private static class IntAccumulator {
        public int value;
    }
    
    public static class SumInt extends FTypes.ArrayReducerInt<Integer, IntAccumulator> {
        @Override
        protected IntAccumulator initial(Stack stack) {
            return new IntAccumulator();
        }

        @Override
        protected IntAccumulator reduceOne(IntAccumulator current, Integer value) {
            current.value += value;
            return current;
        }

        @Override
        protected Integer getValue(IntAccumulator current) {
            return current.value;
        }
    }
    
    private static class DoubleAccumulator {
        public double value;
    }
    
    public static class SumFloat extends FTypes.ArrayReducerFloat<Double, DoubleAccumulator> {

        @Override
        protected DoubleAccumulator initial(Stack stack) {
            return new DoubleAccumulator();
        }

        @Override
        protected DoubleAccumulator reduceOne(DoubleAccumulator current, Double value) {
            current.value += value;
            return current;
        }

        @Override
        protected Double getValue(DoubleAccumulator current) {
            return current.value;
        }
    }
    
    private static class AvgAccumulator {
        public double value;
        public int count;
    }
    
    public static class AvgInt extends FTypes.ArrayReducerFloat<Integer, AvgAccumulator> {
        @Override
        protected AvgAccumulator initial(Stack stack) {
            return new AvgAccumulator();
        }

        @Override
        protected AvgAccumulator reduceOne(AvgAccumulator current, Integer value) {
            current.value += value;
            current.count++;
            return current;
        }

        @Override
        protected Double getValue(AvgAccumulator current) {
            return current.value / current.count;
        }
    }
    
    public static class AvgFloat extends FTypes.ArrayReducerFloat<Double, AvgAccumulator> {
        @Override
        protected AvgAccumulator initial(Stack stack) {
            return new AvgAccumulator();
        }

        @Override
        protected AvgAccumulator reduceOne(AvgAccumulator current, Double value) {
            current.value += value;
            current.count++;
            return current;
        }

        @Override
        protected Double getValue(AvgAccumulator current) {
            return current.value / current.count;
        }
    }
    
    private static class MomentAccumulator {
        public double sum;
        public double sum2;
        public double sumN;
        public int n;
        public Double center;
        public int count;
        public boolean useCenter;
    }
    
    public static class Moment extends FTypes.ArrayReducerFloat<Number, MomentAccumulator> {
        private ArgRef<Integer> n;
        private ArgRef<Double> center;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("array", "n", "center"));
        }
        
        @Override
        protected MomentAccumulator initial(Stack stack) {
            MomentAccumulator acc = new MomentAccumulator();
            acc.n = n.getValue(stack);
            if (acc.n < 1) {
                throw new ExecutionException(this, "Invalid moment: " + acc.n);
            }
            acc.center = center.getValue(stack);
            if (acc.n > 2 && (acc.center == null)) {
                throw new ExecutionException(this, "A central value must be specified for n > 2");
            }
            return acc;
        }
        
        @Override
        protected MomentAccumulator reduceOne(MomentAccumulator current, Number value) {
            double val = value.doubleValue();
            current.sum += val;
            current.sum2 += val * val;
            current.count++;
            if ((current.n > 2) || (current.n == 2 && current.center != null)) {
                current.sumN += java.lang.Math.pow(val - current.center, current.n);
            }
            return current;
        }
        
        @Override
        protected Double getValue(MomentAccumulator current) {
            switch (current.n) {
                case 1:
                    if (current.center == null) {
                        return current.sum / current.count;
                    }
                    else {
                        return current.sum / current.count - current.center;
                    }
                case 2:
                    if (center == null) {
                        double mean = current.sum / current.count;
                        return current.sum2 / current.count - mean * mean;
                    }
                    else {
                        return current.sumN / current.count;
                    }
                default:
                    return current.sumN / current.count;
            }
        }
    }
}
