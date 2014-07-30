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
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class ColorPalette {
    public static final int[] DEFAULT = new int[] {0xbf5b17, 0x33a02c, 0x386cb0, 0xf0027f, 0xffff99, 0xfdc086, 
                           0xbeaed4, 0x666666, 0x7fc97f, 0xff7f00, 0xfdbf6f, 0xe31a1c};
    public static final int[] Q1 = new int[] {0x7FC97F, 0xBEAED4, 0xFDC086, 0xFFFF99, 0x386CB0, 0xF0027F, 0xBF5B17, 0x666666};
    public static final int[] Q2 = new int[] {0x1B9E77, 0xD95F02, 0x7570B3, 0xE7298A, 0x66A61E, 0xE6AB02, 0xA6761D, 0x666666};
    public static final int[] Q3 = new int[] {0x1B9E77, 0xD95F02, 0x7570B3, 0xE7298A, 0x66A61E, 0xE6AB02, 0xA6761D, 0x666666};
    public static final int[] Q4 = new int[] {0xE41A1C, 0x377EB8, 0x4DAF4A, 0x984EA3, 0xFF7F00, 0xFFFF33, 0xA65628, 0xF781BF};
    
    public static ColorPalette newDefaultPalette() {
        return new ColorPalette(Q2);
    }
    
    private LinkedList<Color> unused;
    private Set<Color> used;
    private static final Color RAN_OUT_OF_COLORS = new Color(255, 0, 0, 254);
    
    private ColorPalette(int[] colors) {
        unused = new LinkedList<Color>();
        used = new HashSet<Color>();
        
        for (int c : colors) {
            int r = (c & 0x00ff0000) >> 16;
            int g = (c & 0x0000ff00) >> 8;
            int b = (c & 0x000000ff);
            unused.add(new Color(r, g, b));
        }
    }
    
    public Color allocate() {
        if (unused.isEmpty()) {
            return RAN_OUT_OF_COLORS;
        }
        else {
            Color c = unused.removeFirst();
            used.add(c);
            return c;
        }
    }
    
    public void release(Color color) {
        if (used.contains(color)) {
            used.remove(color);
            unused.addFirst(color);
        }
    }
}
