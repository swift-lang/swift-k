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
 * Created on Jul 24, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.SpringLayout;

public class SpringUtils {

    public static void verticalLayout(JComponent c, SpringLayout l, int gap, boolean n, boolean s, boolean w, boolean e) {
        Component prev = null;
        if (n) {
            l.putConstraint(SpringLayout.NORTH, c.getComponent(0), 1, SpringLayout.NORTH, c);
        }
        for (Component cc : c.getComponents()) {
            if (w) {
                l.putConstraint(SpringLayout.WEST, cc, 1, SpringLayout.WEST, c);
            }
            if (e) {
                l.putConstraint(SpringLayout.EAST, cc, 1, SpringLayout.EAST, c);
            }
            if (prev != null) {
                l.putConstraint(SpringLayout.NORTH, cc, gap, SpringLayout.SOUTH, prev);
            }
            prev = cc;
        }
        if (s) {
            l.putConstraint(SpringLayout.SOUTH, c.getComponent(c.getComponentCount() - 1), 1, SpringLayout.SOUTH, c);
        }
        
        int maxw = 0;
        
        for (Component cc : c.getComponents()) {
            maxw = Math.max(maxw, cc.getPreferredSize().width);
        }
        
        c.setPreferredSize(new Dimension(maxw, 0));
    }

    public static void gridLayout(JComponent c, int rows, int cols, int gap, boolean n, boolean s, boolean w, boolean e) {
        SpringLayout sl = new SpringLayout();
        c.setLayout(sl);
        for (int row = 0; row < rows; row++) {
            if (n) {
                if (row == 0) {
                    sl.putConstraint(SpringLayout.NORTH, c.getComponent(0), 0, 
                        SpringLayout.NORTH, c);
                }
            }
            if (row != 0) {
                sl.putConstraint(SpringLayout.NORTH, c.getComponent(cols * row), gap, 
                    SpringLayout.SOUTH, c.getComponent(cols * row - cols));
            }
            if (w) {
                sl.putConstraint(SpringLayout.WEST, c.getComponent(cols * row), 0, 
                    SpringLayout.WEST, c);
            }
            for (int col = 1; col < cols; col++) {
                sl.putConstraint(SpringLayout.WEST, c.getComponent(cols * row + col), gap,
                    SpringLayout.EAST, c.getComponent(cols * row + col - 1));
                sl.putConstraint(SpringLayout.SOUTH, c.getComponent(cols * row + col), 0,
                    SpringLayout.SOUTH, c.getComponent(cols * row + col - 1));
                if (row != 0) {
                    sl.putConstraint(SpringLayout.WEST, c.getComponent(cols * row + col), 0,
                    SpringLayout.WEST, c.getComponent(cols * (row - 1) + col));
                }
            }
            if (e) {
                sl.putConstraint(SpringLayout.EAST, c.getComponent(cols * row + cols - 1), 0, 
                    SpringLayout.EAST, c);
            }
            if (s) {
                if (row == rows - 1) {
                    sl.putConstraint(SpringLayout.SOUTH, c.getComponent(cols * row), 0, 
                        SpringLayout.SOUTH, c);
                }
            }
        }
    }

}
