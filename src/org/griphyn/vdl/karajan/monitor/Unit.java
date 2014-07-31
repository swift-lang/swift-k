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
 * Created on Aug 1, 2013
 */
package org.griphyn.vdl.karajan.monitor;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

public abstract class Unit {   
    private final String name;
    private UnitFormat format;
    
    protected Unit(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public synchronized NumberFormat formatInstance() {
        if (format == null) {
            format = new UnitFormat(this);
        }
        return format;
    }
    
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + getType().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Unit) {
            Unit u = (Unit) obj;
            return name.equals(u.name) && getType().equals(u.getType());
        }
        else {
            return false;
        }
    }
    
    public double getMultiplierValue(int multiplierLog) {
        double m = 1;
        while (multiplierLog < 0) {
            m = m / getLogBase();
            multiplierLog++;
        }
        while (multiplierLog > 0) {
            m = m * getLogBase();
            multiplierLog--;
        }
        return m;
    }
    
    public double getMultiplier(Number value) {
        return getMultiplierValue(getMultiplierLog(value));
    }

    public abstract String format(Number value);
    public abstract String getType();
    public abstract String getUnitPrefix(Number value);
    public abstract int getMultiplierLog(Number value);
    public abstract double getLogBase();
    public abstract NumberFormat getNumberFormat(Number value);
    
    public static class Fixed extends Unit {
        private static final NumberFormat NFI = new DecimalFormat("###");

        public Fixed(String name) {
            super(name);
        }
        
        public String format(Number value) {
            return NFI.format(value);
        }

        @Override
        public String getUnitPrefix(Number value) {
            return "";
        }

        @Override
        public int getMultiplierLog(Number value) {
            return 0;
        }

        @Override
        public double getLogBase() {
            return 0;
        }

        @Override
        public String getType() {
            return "Fixed";
        }

        @Override
        public NumberFormat getNumberFormat(Number value) {
            return NFI;
        }
    }
    
    public static class SI extends Unit {
        private static final NumberFormat NF = new DecimalFormat("###.##");
        private static final NumberFormat NFI = new DecimalFormat("###");
        
        public SI(String name) {
            super(name);
        }  
        
        @Override
        public int getMultiplierLog(Number value) {
            int l = 0;
            double v = value.doubleValue();
            while (v < 0.5) {
                v = v * 1000;
                l--;
            }
            while (v > 500) {
                v = v / 1000;
                l++;
            }
            return l;
        }
        
        @Override
        public String getUnitPrefix(Number value) {
            return unit(getMultiplierLog(value));
        }

        public String format(Number value) {
            String prefix = getUnitPrefix(value);
            if (prefix.equals("") && ((value instanceof Long) || (value instanceof Integer))) {
                return NFI.format(value) + getName();
            }
            else {
                return NF.format(value.doubleValue() / getMultiplier(value)) + prefix + getName();
            }
        }
        
        @Override
        public NumberFormat getNumberFormat(Number value) {
            String prefix = getUnitPrefix(value);
            if (prefix.equals("") && ((value instanceof Long) || (value instanceof Integer))) {
                return NFI;
            }
            else {
                return NF;
            }
        }
        
        @Override
        public String getType() {
            return "SI";
        }
        
        @Override
        public double getLogBase() {
            return 1000;
        }

        private String unit(int l) {
            switch(l) {
                case -4:
                    return "p";
                case -3:
                    return "n";
                case -2:
                    return "μ";
                case -1:
                    return "m";
                case 0:
                    return "";
                case 1:
                    return "K";
                case 2:
                    return "M";
                case 3:
                    return "G";
                case 4:
                    return "T";
                case 5:
                    return "P";
                default:
                    return "?";
            }
        }
    }
    
    public static class P2 extends Unit {
        private static final NumberFormat NF = new DecimalFormat("###.##");
        private static final NumberFormat NFI = new DecimalFormat("###");
        
        public P2(String name) {
            super(name);
        }
        
        @Override
        public int getMultiplierLog(Number value) {
            int l = 0;
            long v = value.longValue();
            while (v > 512) {
                v = v / 1024;
                l++;
            }
            return l;
        }

        public String getUnitPrefix(Number value) {
            return unit(getMultiplierLog(value));
        }
        
        public String format(Number value) {
            String prefix = getUnitPrefix(value);
            if (prefix.equals("") && ((value instanceof Long) || (value instanceof Integer))) {
                return NFI.format(value) + " " + getName();
            }
            else {
                return NF.format(value.doubleValue() / getMultiplier(value)) + " " + prefix + getName();
            }
        }
        
        
        
        @Override
        public NumberFormat getNumberFormat(Number value) {
            String prefix = getUnitPrefix(value);
            if (prefix.equals("") && ((value instanceof Long) || (value instanceof Integer))) {
                return NFI;
            }
            else {
                return NF;
            }
        }

        @Override
        public String getType() {
            return "P2";
        }

        @Override
        public double getLogBase() {
            return 1024;
        }

        private String unit(int l) {
            switch(l) {
                case 0:
                    return "";
                case 1:
                    return "K";
                case 2:
                    return "M";
                case 3:
                    return "G";
                case 4:
                    return "T";
                case 5:
                    return "P";
                default:
                    return "?";
            }
        }
    }
    
    private static class UnitFormat extends DecimalFormat {
        private Unit unit;
        
        public UnitFormat(Unit unit) {
            this.unit = unit;
        }

        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
            toAppendTo.append(unit.format(number));
            return toAppendTo;
        }

        @Override
        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
            toAppendTo.append(unit.format(number));
            return toAppendTo;
        }

        @Override
        public Number parse(String source, ParsePosition parsePosition) {
            throw new UnsupportedOperationException();
        }
    }
}
