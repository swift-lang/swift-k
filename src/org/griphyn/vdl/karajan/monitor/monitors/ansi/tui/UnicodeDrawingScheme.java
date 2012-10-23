//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 12, 2012
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

public interface UnicodeDrawingScheme {
    char getChar(int code);
    
    
    public static class ASCII implements UnicodeDrawingScheme {
        @Override
        public char getChar(int code) {
            switch(code) {
                case ANSI.GCH_LR_CORNER:
                case ANSI.GCH_UR_CORNER:
                case ANSI.GCH_UL_CORNER:
                case ANSI.GCH_LL_CORNER:
                case ANSI.GCH_CROSS:
                case ANSI.GCH_H_LINE:
                case ANSI.GCH_V_LINE:
                case ANSI.GCH_ML_CORNER:
                case ANSI.GCH_MR_CORNER:
                case ANSI.GCH_LM_CORNER:
                case ANSI.GCH_UM_CORNER:
                case ANSI.GCH_HASH:
                case ANSI.GCH_BULLET:
                    return (char) code;
                case ANSI.GCH_ARROW_DOWN:
                case ANSI.GCH_ARROW_UP:
                    return '-';
                case ANSI.GCH_ARROW_LEFT:
                case ANSI.GCH_ARROW_RIGHT:
                    return '|';
                default:
                    return '.';
            }
        }   
    }
    
    public static class SquareHeavy implements UnicodeDrawingScheme {
        @Override
        public char getChar(int code) {
            switch(code) {
                case ANSI.GCH_LR_CORNER:
                    return '\u251b';
                case ANSI.GCH_UR_CORNER:
                    return '\u2513';
                case ANSI.GCH_UL_CORNER:
                    return '\u250f';
                case ANSI.GCH_LL_CORNER:
                    return '\u2517';
                case ANSI.GCH_CROSS:
                    return '\u254b';
                case ANSI.GCH_H_LINE:
                    return '\u2501';
                case ANSI.GCH_V_LINE:
                    return '\u2503';
                case ANSI.GCH_ML_CORNER:
                    return '\u2523';
                case ANSI.GCH_MR_CORNER:
                    return '\u252b';
                case ANSI.GCH_LM_CORNER:
                    return '\u253b';
                case ANSI.GCH_UM_CORNER:
                    return '\u2533';
                case ANSI.GCH_HASH:
                    return '\u2592';
                case ANSI.GCH_BULLET:
                    return '\u2022';
                case ANSI.GCH_ARROW_DOWN:
                    return '\u21e9';
                case ANSI.GCH_ARROW_UP:
                    return '\u21e7';
                case ANSI.GCH_ARROW_LEFT:
                    return '\u21e6';
                case ANSI.GCH_ARROW_RIGHT:
                    return '\u21e8';
                default:
                    return '.';
            }
        }   
    }
    
    public static class SquareLight implements UnicodeDrawingScheme {
        @Override
        public char getChar(int code) {
            switch(code) {
                case ANSI.GCH_LR_CORNER:
                    return '\u2518';
                case ANSI.GCH_UR_CORNER:
                    return '\u2510';
                case ANSI.GCH_UL_CORNER:
                    return '\u250c';
                case ANSI.GCH_LL_CORNER:
                    return '\u2514';
                case ANSI.GCH_CROSS:
                    return '\u253c';
                case ANSI.GCH_H_LINE:
                    return '\u2500';
                case ANSI.GCH_V_LINE:
                    return '\u2502';
                case ANSI.GCH_ML_CORNER:
                    return '\u251c';
                case ANSI.GCH_MR_CORNER:
                    return '\u2524';
                case ANSI.GCH_LM_CORNER:
                    return '\u2534';
                case ANSI.GCH_UM_CORNER:
                    return '\u252c';
                case ANSI.GCH_HASH:
                    return '\u2591';
                case ANSI.GCH_BULLET:
                    return '\u2022';
                case ANSI.GCH_ARROW_DOWN:
                    return '\u21e9';
                case ANSI.GCH_ARROW_UP:
                    return '\u21e7';
                case ANSI.GCH_ARROW_LEFT:
                    return '\u21e6';
                case ANSI.GCH_ARROW_RIGHT:
                    return '\u21e8';
                default:
                    return '.';
            }
        }
    }
    
    public static class RoundedLight implements UnicodeDrawingScheme {
        @Override
        public char getChar(int code) {
            switch(code) {
                case ANSI.GCH_LR_CORNER:
                    return '\u256f';
                case ANSI.GCH_UR_CORNER:
                    return '\u256e';
                case ANSI.GCH_UL_CORNER:
                    return '\u256d';
                case ANSI.GCH_LL_CORNER:
                    return '\u2570';
                case ANSI.GCH_CROSS:
                    return '\u253c';
                case ANSI.GCH_H_LINE:
                    return '\u2500';
                case ANSI.GCH_V_LINE:
                    return '\u2502';
                case ANSI.GCH_ML_CORNER:
                    return '\u251c';
                case ANSI.GCH_MR_CORNER:
                    return '\u2524';
                case ANSI.GCH_LM_CORNER:
                    return '\u2534';
                case ANSI.GCH_UM_CORNER:
                    return '\u252c';
                case ANSI.GCH_HASH:
                    return '\u2591';
                case ANSI.GCH_BULLET:
                    return '\u2263';
                case ANSI.GCH_ARROW_DOWN:
                    return '\u25bc';
                case ANSI.GCH_ARROW_UP:
                    return '\u25b2';
                case ANSI.GCH_ARROW_LEFT:
                    return '\u25c0';
                case ANSI.GCH_ARROW_RIGHT:
                    return '\u25b6';
                default:
                    return '.';
            }
        }
    }
}
