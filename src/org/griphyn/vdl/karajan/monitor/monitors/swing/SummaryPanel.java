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
 * Created on Jul 19, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.util.Map;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.common.GlobalTimer;
import org.griphyn.vdl.karajan.monitor.items.ApplicationState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.items.SummaryItem;

public class SummaryPanel extends JPanel {
    private SystemState state;

    private JProgressBar[] bars;
    private JProgressBar memory;
    private int[] stateCounts = new int[SummaryItem.STATES.length];
    private int maxCount;
    
    private long start;

    public SummaryPanel(SystemState state) {
        this.state = state;
        this.start = state.getCurrentTime();
        SpringLayout l = new SpringLayout();
        setLayout(l);
        
        makeProgressBars(l);
        
        GlobalTimer.getTimer().schedule(new TimerTask() {
            public void run() {
                update();
            }
        }, 1000, 1000);
    }


    private void makeProgressBars(SpringLayout l) {
        JComponent prevLabel = null, prevBar = null;
        bars = new JProgressBar[SummaryItem.STATES.length];
        
        SpringLayout ls = new SpringLayout();
        JPanel appSummary = new JPanel();
        appSummary.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "App Summary"));
        add(appSummary);
        l.putConstraint(SpringLayout.WEST, appSummary, 5, SpringLayout.WEST, this);
        l.putConstraint(SpringLayout.EAST, appSummary, -5, SpringLayout.EAST, this);
        l.putConstraint(SpringLayout.NORTH, appSummary, 25, SpringLayout.NORTH, this);
        
        appSummary.setLayout(ls);
        
        
        for (int i = 0; i < SummaryItem.STATES.length; i++) {
            JLabel label = new JLabel(SummaryItem.STATES[i] + ":");
            appSummary.add(label);
            
            JProgressBar lb = new JProgressBar();
            bars[i] = lb;
            lb.setString("0");
            lb.setStringPainted(true);
            appSummary.add(lb);
        
            fixEdges(ls, label, lb, appSummary);
            if (prevLabel == null) {
                ls.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.NORTH, appSummary);
            }
            else {
                ls.putConstraint(SpringLayout.NORTH, label, 10, SpringLayout.SOUTH, prevLabel);
            }
            prevLabel = label;
            prevBar = lb;
        }
        
        JLabel hl = new JLabel("Heap:");
        memory = makeProgress(l, hl);
        
        l.putConstraint(SpringLayout.SOUTH, appSummary, -25, SpringLayout.NORTH, hl);
        l.putConstraint(SpringLayout.SOUTH, hl, -25, SpringLayout.SOUTH, this);
        
        Spring maxW = Spring.constant(0);
        
        for (int i = 0; i < SummaryItem.STATES.length; i++) {
            maxW = Spring.max(maxW, ls.getConstraints(appSummary.getComponent(i * 2)).getWidth());
        }
        
        for (int i = 0; i < SummaryItem.STATES.length; i++) {
            SpringLayout.Constraints c = ls.getConstraints(appSummary.getComponent(i * 2));
            c.setWidth(maxW);
        }
    }


    private JProgressBar makeProgress(SpringLayout sl, JLabel label) {
        if (label != null) {
            add(label);
        }
        
        JProgressBar pb = new JProgressBar();
        pb.setString("");
        pb.setStringPainted(true);
        add(pb);
        
        fixEdges(sl, label, pb, this);
        
        return pb;
    }


    private void fixEdges(SpringLayout l, JLabel label, JComponent c, JComponent container) {
        if (label == null) {
            l.putConstraint(SpringLayout.WEST, c, 5, SpringLayout.WEST, container);
            l.putConstraint(SpringLayout.EAST, c, -5, SpringLayout.EAST, container);
        }
        else {
            l.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.WEST, container);
            l.putConstraint(SpringLayout.WEST, c, 5, SpringLayout.EAST, label);
            l.putConstraint(SpringLayout.EAST, c, -5, SpringLayout.EAST, container);
            l.putConstraint(SpringLayout.NORTH, c, 0, SpringLayout.NORTH, label);
        }
    }


    private void update() {
        SummaryItem summary = (SummaryItem) state.getItemByID(SummaryItem.ID, StatefulItemClass.WORKFLOW);
        if (summary != null) {
            Map<ApplicationState, Integer> counts = summary.getCounts(state);
            for (int i = 0; i < SummaryItem.STATES.length; i++) {
                Integer v = counts.get(SummaryItem.STATES[i]);
                if (v != null) {
                    if (v > maxCount) {
                        maxCount = v;
                        for (int j = 0; j < SummaryItem.STATES.length; j++) {
                            bars[j].setMaximum(maxCount);
                        }
                    }
                    bars[i].setValue(v);
                    bars[i].setString(v.toString());
                }
                else {
                    bars[i].setValue(0);
                    bars[i].setString("0");
                }
            }
        }
        long heapMax = state.getMaxHeap();
        long heapCrt = state.getUsedHeap();
        memory.setMaximum((int) (heapMax / 1000000));
        memory.setValue((int) (heapCrt / 1000000));
        memory.setString(state.getCurrentHeapFormatted() + " / " + state.getMaxHeapFormatted());
        
        repaint();
    }
}
