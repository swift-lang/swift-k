//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 6, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript.v2;


public class Math {
    public static class Sin extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.sin(arg);
        }
    }
    
    public static class Cos extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.cos(arg);
        }
    }
    
    public static class Tan extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.tan(arg);
        }
    }
    
    public static class ASin extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.asin(arg);
        }
    }
    
    public static class ACos extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.acos(arg);
        }
    }
    
    public static class ATan extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.atan(arg);
        }
    }
    
    public static class ATan2 extends FTypes.FloatPFloatFloat {
        @Override
        protected double v(double y, double x) {
            return java.lang.Math.atan2(y, x);
        }
    }
    
    public static class Exp extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.exp(arg);
        }
    }
    
    public static class Ln extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.log(arg);
        }
    }
    
    public static class Log extends FTypes.FloatPFloatFloat {
        @Override
        protected double v(double x, double base) {
            return java.lang.Math.log(x) / java.lang.Math.log(base);
        }
    }
    
    public static class Log10 extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.log10(arg);
        }
    }
    
    /**
     * Implementation of both pow(float, float) and pow(int, int)
     *
     */
    public static class Pow extends FTypes.FloatPNumNum {
        @Override
        protected double v(Number base, Number exponent) {
            return java.lang.Math.pow(base.doubleValue(), exponent.doubleValue());
        }
    }
    
    public static class Sqrt extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.sqrt(arg);
        }
    }
    
    public static class Cbrt extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.cbrt(arg);
        }
    }
    
    public static class Ceil extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.ceil(arg);
        }
    }
    
    public static class Floor extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.floor(arg);
        }
    }
    
    public static class Round extends FTypes.FloatPFloat {
        @Override
        protected double v(double arg) {
            return java.lang.Math.round(arg);
        }
    }
    
    public static class MinI extends FTypes.IntPIntInt {
        @Override
        protected int v(int a, int b) {
            return java.lang.Math.min(a, b);
        }
    }
    
    public static class MinF extends FTypes.FloatPFloatFloat {
        @Override
        protected double v(double a, double b) {
            return java.lang.Math.min(a, b);
        }
    }
    
    public static class MaxI extends FTypes.IntPIntInt {
        @Override
        protected int v(int a, int b) {
            return java.lang.Math.max(a, b);
        }
    }
    
    public static class MaxF extends FTypes.FloatPFloatFloat {
        @Override
        protected double v(double a, double b) {
            return java.lang.Math.max(a, b);
        }
    }
    
    public static class AbsI extends FTypes.IntPInt {
        @Override
        protected int v(int x) {
            return java.lang.Math.abs(x);
        }
    }
    
    public static class AbsF extends FTypes.FloatPFloat {
        @Override
        protected double v(double x) {
            return java.lang.Math.abs(x);
        }
    }
    
    public static class IsNaN extends FTypes.BoolPFloat {
        @Override
        protected boolean v(double arg) {
            return Double.isNaN(arg);
        }
    }
}
