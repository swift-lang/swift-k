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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.common.DataSampler;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.monitors.swing.GridView.Tree;

public class GraphsPanel extends JPanel {    
    public static final int RANGE_MIN = 1;
    public static final int RANGE_MAX = 4 * 60 + 1;
    
    private static final int V = GridView.Tree.V;
    private static final int H = GridView.Tree.H;
    
    public static final GridView.Tree[] LAYOUTS = new GridView.Tree[] {
            t(), null,
            t(H, 0.5), t(V, 0.5), null,
            t(H, 0.33, t(), t(H, 0.5)), t3(V), 
                t(V, 0.5, t(H, 0.5), t()), t(V, 0.5, t(), t(H, 0.5)), t(H, 0.5, t(V, 0.5), t()), t(H, 0.5, t(), t(V, 0.5)), null,
            t(H, 0.5, t(V, 0.5), t(V, 0.5)),
                t4(V), t4(H),
                t(V, 0.5, t3(H), t()), t(H, 0.5, t3(V), t()), t(V, 0.5, t(), t3(H)), t(H, 0.5, t(), t3(V)), null,
            t(V, 0.5, t(H, 0.5), t3(H)),
                t(V, 0.5, t3(H), t(H, 0.5)), t(H, 0.5, t3(V), t(V, 0.5)), t(H, 0.5, t(V, 0.5), t3(V)),
                t5(V), t5(H),
                t(V, 0.5, t4(H), t()), t(V, 0.5, t(), t4(H)), t(H, 0.5, t4(V), t()), t(H, 0.5, t(), t4(V)), null,
            t(V, 0.5, t3(H), t3(H)), t(H, 0.5, t3(V), t3(V)), null
    };
    
    private static GridView.Tree t() {
        return new GridView.Tree();
    }
    
    private static Tree t3(int splitType) {
        return  t(splitType, 0.34, t(), t(splitType, 0.5));
    }
    
    private static Tree t5(int splitType) {
        return  t(splitType, 0.4, t(splitType, 0.5), t3(splitType));
    }
    
    private static Tree t4(int splitType) {
        return  t(splitType, 0.5, t(splitType, 0.5), t(splitType, 0.5));
    }

    private static GridView.Tree t(int splitType, double position) {
        return new GridView.Tree(splitType, position);
    }
    
    private static GridView.Tree t(int splitType, double position, GridView.Tree first, GridView.Tree second) {
        return new GridView.Tree(splitType, position, first, second);
    }
    
    
    
    private SystemState state;
    private DataSampler sampler;
    
    private JPanel toolBar;
    private JPopupMenu layoutPopup;
    private LinkedList<GraphPanel> graphs;
    private GridView grid;
    private JSlider rangeSlider;
    private JLabel rangeLabel;
    
    public GraphsPanel(SystemState state) {
        this.state = state;
        sampler = (DataSampler) state.getItemByID(DataSampler.ID, StatefulItemClass.WORKFLOW);
        this.graphs = new LinkedList<GraphPanel>();
        setLayout(new BorderLayout());

        toolBar = new JPanel();
        add(toolBar, BorderLayout.NORTH);
        
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        final JButton layout = IconLoader.makeImageButton("gui/icons/layout.png", "Layout...");        
        toolBar.add(layout);
        layout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayLayoutPopup(layout);
            }
        });
        
        toolBar.add(new JLabel("Max. range: "));
        toolBar.add(rangeSlider = new JSlider(JSlider.HORIZONTAL, RANGE_MIN, RANGE_MAX, 30));
        rangeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setGraphRanges(rangeSlider.getValue());
            } 
        });
        toolBar.add(rangeSlider);
        toolBar.add(rangeLabel = new JLabel("00:01"));
        setGraphRanges(rangeSlider.getValue());
        
        makeLayoutPopup();
        
        grid = new GridView();
        add(grid, BorderLayout.CENTER);
        
        loadLayout();
    }

    protected void setGraphRanges(int value) {
        if (value == RANGE_MAX) {
            rangeLabel.setText("unlimited");
            for (GraphPanel gp : graphs) {
                gp.setMaxRange(0);
            }
        }
        else {
            int mins = value / 60;
            int secs = value % 60;
            
            rangeLabel.setText(pad(mins) + ":" + pad(secs));
            for (GraphPanel gp : graphs) {
                gp.setMaxRange(value);
            }
        }
    }

    private String pad(int ms) {
        if (ms < 10) {
            return "0" + ms;
        }
        else {
            return String.valueOf(ms);
        }
    }

    private void makeLayoutPopup() {
        layoutPopup = new JPopupMenu();
        JMenu crt = null;
        for (final GridView.Tree t : LAYOUTS) {
            if (t == null) {
                crt = null;
            }
            else {
                ImageIcon icon = t.makeIcon();
                if (crt == null) {
                    crt = new JMenu();
                    crt.setIcon(icon);
                    layoutPopup.add(crt);
                }
                JMenuItem mi = new JMenuItem();
                mi.setIcon(icon);
                crt.add(mi);
                mi.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        activateLayout(t);
                    } 
                });
            }
        }
    }

    protected void activateLayout(Tree t) {
        grid.setLayout(t.copy());
        int count = grid.getCellCount();
        while (count > graphs.size()) {
            GraphPanel gp = new GraphPanel(state, this);
            graphs.add(gp);
            grid.add(gp);
        }
        while (count < graphs.size()) {
            JComponent gp = graphs.removeLast();
            grid.remove(gp);
        }
        
        saveLayout();
    }

    void saveLayout() {
        /*
         * Stored are:
         * - the layout itself
         * - the graph count and what's being graphed in each
         * - the graph colors
         */
        try {
            Preferences prefs = Preferences.userNodeForPackage(GraphsPanel.class);
            Preferences layout = prefs.node("layout");
            grid.getTree().store(layout);
            prefs.putInt("graphCount", graphs.size());
            for (int i = 0; i < graphs.size(); i++) {
                Preferences gp = prefs.node("graph" + i);
                graphs.get(i).store(gp);
            }
        }
        catch (Exception e) {
            System.err.println("Failed to save layout: "  + e);
        }
    }
    
    private void loadLayout() {
        grid.clear();
        graphs.clear();
        try {
            Preferences prefs = Preferences.userNodeForPackage(GraphsPanel.class);
            if (prefs.nodeExists("layout")) {
                grid.setLayout(GridView.Tree.load(prefs.node("layout")));
                int gc = prefs.getInt("graphCount", 0);
                for (int i = 0; i < gc; i++) {
                    GraphPanel gp = GraphPanel.load(prefs.node("graph" + i), state, this);
                    graphs.add(gp);
                    grid.add(gp);
                }
            }
            else {
                setDefaultLayout();
            }
        }
        catch (Exception e) {
            System.err.println("Failed to load layout: "  + e + ". Using default.");
            setDefaultLayout();
        }
    }
    
    private void setDefaultLayout() {
        grid.clear();
        graphs.clear();
        grid.setLayout(new Tree());
        GraphPanel gp = new GraphPanel(state, this);
        gp.enable(sampler.getSeries("apps/Active"));
        gp.enable(sampler.getSeries("apps/Stage in"));
        gp.enable(sampler.getSeries("apps/Stage out"));
        graphs.add(gp);
        grid.add(gp);
        saveLayout();
    }

    protected void displayLayoutPopup(JButton src) {
        layoutPopup.show(src, 6, 6);
    }
}