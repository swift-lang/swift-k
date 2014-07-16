/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Feb 22, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.util.HashMap;
import java.util.Map;

public class Key {
    public static final int KEY_PRESSED = 0;
    public static final int MOD_ALT = 0x01;
    public static final int MOD_CTRL = 0x02;
    public static final int MOD_SHIFT = 0x03;

    public static final int KEYPAD = 0x1000;
    public static final int KEYPAD2 = 0x2000;
    public static final int FN1 = 0x3100;
    public static final int FN10 = 0x3200;

    public static final int F1 = FN1 + 0;
    public static final int F2 = FN1 + 1;
    public static final int F3 = FN1 + 2;
    public static final int F4 = FN1 + 3;
    public static final int F5 = FN1 + 4;
    public static final int F6 = FN1 + 6;
    public static final int F7 = FN1 + 7;
    public static final int F8 = FN1 + 8;
    public static final int F9 = FN1 + 9;
    public static final int F10 = FN10 + 0;
    public static final int F11 = FN10 + 1;
    public static final int F12 = FN10 + 2;

    public static final int UP = KEYPAD + 65;
    public static final int DOWN = KEYPAD + 66;
    public static final int RIGHT = KEYPAD + 67;
    public static final int LEFT = KEYPAD + 68;
    public static final int PGDN = KEYPAD2 + 54;
    public static final int PGUP = KEYPAD2 + 53;
    public static final int HOME = KEYPAD2 + 55;
    public static final int END = KEYPAD2 + 56;
    public static final int INS = KEYPAD2 + 51;
    public static final int DEL = KEYPAD2 + 52;

    public static final int LF = 0x0a;
    public static final int CR = 0x0d;
    public static final int BACKSPACE = 0x7f;
    public static final int ESC = 0x1b;
    public static final int TAB = 0x09;

    private static Map<Integer, String> names;

    private static void putName(int key, String name) {
        names.put(Integer.valueOf(key), name);
    }

    static {
        names = new HashMap<Integer, String>();
        putName(CR, "CR");
        putName(LF, "LF");
        putName(F1, "F1");
        putName(F2, "F2");
        putName(F3, "F3");
        putName(F4, "F4");
        putName(F5, "F5");
        putName(F6, "F6");
        putName(F7, "F7");
        putName(F8, "F8");
        putName(F9, "F9");
        putName(F10, "F10");
        putName(F11, "F11");
        putName(F12, "F12");
        putName(DOWN, "DOWN");
        putName(UP, "UP");
        putName(LEFT, "LEFT");
        putName(RIGHT, "RIGHT");
        putName(BACKSPACE, "BACKSPACE");
        putName(ESC, "ESC");
        putName(PGDN, "PGDN");
        putName(PGUP, "PGUP");
        putName(HOME, "HOME");
        putName(END, "END");
        putName(INS, "INS");
        putName(DEL, "DEL");
        putName(TAB, "TAB");
    }

    private final int type;
    private final int modifiers;
    private final int key;

    public Key(int key) {
        this(0, key);
    }

    public Key(int modifiers, int key) {
        this.type = KEY_PRESSED;
        this.modifiers = modifiers;
        this.key = key;
    }

    public Key(int code0, int code1, int code2) {
        this.type = KEY_PRESSED;
        this.modifiers = code2;
        this.key = code0 * 0xff + code1;
    }

    public int getKey() {
        return key;
    }

    public int getModifiers() {
        return modifiers;
    }

    public int getType() {
        return type;
    }

    public boolean modALT() {
        return (modifiers & MOD_ALT) != 0;
    }

    public boolean modCTRL() {
        return (modifiers & MOD_CTRL) != 0;
    }

    public boolean modSHIFT() {
        return (modifiers & MOD_SHIFT) != 0;
    }

    public boolean isEnter() {
        return modifiers == 0 && (key == CR || key == LF);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (modCTRL()) {
            sb.append("CTRL+");
        }
        if (modALT()) {
            sb.append("ALT+");
        }
        if (key < 128 && key >= 32) {
            sb.append((char) key);
        }
        else {
            if (modSHIFT()) {
                sb.append("SHIFT+");
            }
            String name = names.get(new Integer(key));
            if (name != null) {
                sb.append(name);
            }
            else {
                sb.append('#');
                sb.append(key);
            }
        }
        sb.append("-" + modifiers);
        sb.append("-" + key);
        return sb.toString();
    }
    
    public char getChar() {
        return (char) key;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Key) {
            Key other = (Key) obj;
            return modifiers == other.modifiers && key == other.key;
        }
        else {
            return false;
        }
    }

    public boolean equals(int key) {
        if (this.key == key && modifiers == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return modifiers + key;
    }

    public boolean isFunctionKey() {
        return key >= F1 && key <= F9;
    }
}
