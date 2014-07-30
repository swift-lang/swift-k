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
 * Created on Jul 25, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class GridView extends JPanel {
    public static final int DIVIDER_SIZE = 10;

    public static final Cursor H_RESIZE_CURSOR = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
    public static final Cursor V_RESIZE_CURSOR = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
    
    public static final Stroke DIVIDER_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0, 
        new float[] {5.0f}, 0f);
    
    private Tree tree;
    private List<Divider> dividers;
    private int cellCount, dividerIndex;
    
    public GridView() {
        dividers = new ArrayList<Divider>();
        setLayout(new Tree());
    }
    
    public void setLayout(Tree t) {
        this.tree = t;
        cellCount = this.tree.cellCount();
        validate();
        repaint();
    }
    
    public void clear() {
        tree = null;
        removeAll();
    }
    
    public Tree getTree() {
        return tree;
    }
    
    public int getCellCount() {
        return cellCount;
    }
    
    @Override
    public synchronized void doLayout() {
        dividerIndex = 0;
        layout(tree, 0, 0, getWidth(), getHeight(), 0);
        while (dividers.size() > dividerIndex + 1) {
            Divider d = dividers.get(dividers.size() - 1);
            dividers.remove(dividers.size() - 1);
            remove(d);
        }
    }
    
    @Override
    public synchronized Component add(Component comp) {
        // add before dividers
        return super.add(comp, getComponentCount() - dividers.size());
    }

    private int layout(GridView.Tree t, int x, int y, int w, int h, int index) {
        if (t == null) {
            return index;
        }
        if (t.splitType == Tree.NONE) {
            if (cellCount > index) {
                Component c = getComponent(index);
                c.setSize(w, h);
                c.setLocation(x, y);
            }
            return index + 1;
        }
        else if (t.splitType == Tree.V) {
            int h1 = (int) (t.splitPosition * h);
            int h2 = h - h1;
            setDivider(Tree.V, x, y + h1 - DIVIDER_SIZE / 2, w, DIVIDER_SIZE, h, t);
            index = layout(t.first, x, y, w, h1 - DIVIDER_SIZE / 2, index);
            return layout(t.second, x, y + h1 + DIVIDER_SIZE / 2, w, h2, index);
        }
        else if (t.splitType == Tree.H) {
            int w1 = (int) (t.splitPosition * w);
            int w2 = w - w1;
            setDivider(Tree.H, x + w1 - DIVIDER_SIZE / 2, y, DIVIDER_SIZE, h, w, t);
            index = layout(t.first, x, y, w1 - DIVIDER_SIZE / 2, h, index);
            return layout(t.second, x + w1 + DIVIDER_SIZE / 2, y, w2, h, index);
        }
        return index;
    }
    
    private void setDivider(int dir, int x, int y, int w, int h, int range, Tree t) {
        if (dividers.size() <= dividerIndex) {
            Divider d = new Divider(dir, x, y, w, h, range, t);
            dividers.add(d);
            super.add(d);
        }
        else {
            Divider d = dividers.get(dividerIndex);
            d.set(dir, x, y, w, h, range, t);
            d.validate();
        }
        dividerIndex++;
    }

    private void addDivider(Divider divider) {
        dividers.add(divider);
        this.add(divider);
    } 
    
    public static class Tree {
        public static final int NONE = 0;
        public static final int H = 1;
        public static final int V = 2;
        
        private int splitType;
        private double splitPosition;
        private Tree first, second;
                
        public Tree() {
        }
        
        public Tree(int splitType, double position) {
            this();
            split(splitType, position);
        }
        
        public Tree(int splitType, double position, Tree first, Tree second) {
            this();
            split(splitType, position, first, second);
        }
        
        public void split(int splitType, double position) {
            split(splitType, position, new Tree(), new Tree());
        }
        
        public void split(int splitType, double position, Tree first, Tree second) {
            this.splitType = splitType;
            this.splitPosition = position;
            this.first = first;
            this.second = second;
        }

        public static Tree newTree() {
            return new Tree();
        }
        
        public int cellCount() {
            switch (splitType) {
                case NONE:
                    return 1;
                default:
                    return (first != null ? first.cellCount() : 0) + (second != null ? second.cellCount() : 0);
            }
        }

        
        public ImageIcon makeIcon() {
            return new TreeIcon(this);
        }

        public Tree copy() {
            return new Tree(splitType, splitPosition, copyIfNotNull(first), copyIfNotNull(second));
        }

        private Tree copyIfNotNull(Tree t) {
            if (t == null) {
                return null;
            }
            else {
                return t.copy();
            }
        }

        public void store(Preferences p) {
            p.putInt("splitType", splitType);
            p.putDouble("splitPosition", splitPosition);
            if (first != null) {
                Preferences p1 = p.node("first");
                first.store(p1);
            }
            if (second != null) {
                Preferences p2 = p.node("second");
                second.store(p2);
            }
        }
        
        public static Tree load(Preferences p) throws BackingStoreException {
            Tree tree = new Tree();
            tree.splitType = p.getInt("splitType", H);
            tree.splitPosition = p.getDouble("splitPosition", 0.5);
            if (tree.splitType != NONE) {
                tree.first = load(p.node("first"));
                tree.second = load(p.node("second"));
            }
            return tree;
        }
    }
    
    private static class TreeIcon extends ImageIcon {
        private static final int W = 24;
        private static final int H = 24;
        private Tree tree;
        
        public TreeIcon(Tree tree) {
            this.tree = tree;
        }

        @Override
        public int getIconWidth() {
            return W;
        }

        @Override
        public int getIconHeight() {
            return H;
        }

        @Override
        public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(UIManager.getColor("Button.shadow"));
            g.fillRect(x, y, W, H);
            g.setColor(UIManager.getColor("Button.foreground"));
            paint(g, tree, x + 1, y + 1, W - 2, H - 2);
        }

        private void paint(Graphics g, Tree t, int x, int y, int w, int h) {
            if (t.splitType == Tree.NONE) {
                g.fillRect(x + 1, y + 1, w - 2, h - 2);
            }
            else if (t.splitType == Tree.V) {
                int h1 = (int) (t.splitPosition * h);
                int h2 = h - h1;
                paint(g, t.first, x, y, w, h1);
                paint(g, t.second, x, y + h1, w, h2);
            }
            else if (t.splitType == Tree.H) {
                int w1 = (int) (t.splitPosition * w);
                int w2 = w - w1;
                paint(g, t.first, x, y, w1, h);
                paint(g, t.second, x + w1, y, w2, h);
            }
        }
    }
    
    private static class Divider extends JComponent implements MouseListener, MouseMotionListener {
        private int type, range;
        private Tree tree;
        private boolean dragging;
        private int vd;
        
        public Divider(int type, int x, int y, int w, int h, int range, Tree tree) {
            this.type = type;
            this.tree = tree;
            this.range = range;
            this.setLocation(x, y);
            this.setSize(w, h);
            if (type == Tree.H) {
                this.setCursor(H_RESIZE_CURSOR);
            }
            else {
                this.setCursor(V_RESIZE_CURSOR);
            }
            addMouseListener(this);
            addMouseMotionListener(this);
        }
    
        public void set(int dir, int x, int y, int w, int h, int range, Tree t) {
            this.type = dir;
            this.tree = t;
            this.range = range;
            setLocation(x, y);
            setSize(w, h);
            if (type == Tree.H) {
                this.setCursor(H_RESIZE_CURSOR);
            }
            else {
                this.setCursor(V_RESIZE_CURSOR);
            }
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            
            if (dragging) {
                g2.setColor(UIManager.getColor("Separator.highlight"));
            }
            else {
                g2.setColor(UIManager.getColor("Separator.foreground"));
            }
            //g2.setColor(Color.BLACK);
            g2.setStroke(DIVIDER_STROKE);
            if (type == Tree.H) {
                int w2 = getWidth() / 2;
                g.drawLine(w2, 0, w2, getHeight());
            }
            else {
                int h2 = getHeight() / 2;
                g.drawLine(0, h2, getWidth(), h2);
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            int v;
            if (type == Tree.V) {
                v = e.getYOnScreen() - vd;
            }
            else {
                v = e.getXOnScreen() - vd;
            }
        
            if (v < 0) {
                v = 0;
            }
            if (v > range) {
                v = range;
            }
            double pos = (double) v / range;
            tree.splitPosition = pos;
            getParent().invalidate();
            getParent().repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (dragging) {
                mouseDragged(e);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            dragging = true;
            if (type == Tree.H) {
                int w2 = getX() + getWidth() / 2;
                vd = e.getXOnScreen() - w2;
            }
            else {
                int h2 = getY() + getHeight() / 2;
                vd = e.getYOnScreen() - h2;
            }
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            dragging = false;
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }
}