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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class GridView extends JPanel {
    public static final int DIVIDER_SIZE = 10;

    
    private GridView.Tree tree;
    
    public GridView() {
        tree = new Tree();
    }
    
    public void setLayout(Tree t) {
        this.tree = t;
        revalidate();
    }
    
    public int getCellCount() {
        return tree.cellCount();
    }
    
    @Override
    public void doLayout() {  
        layout(tree, 0, 0, getWidth(), getHeight(), 0);
    }

    private int layout(GridView.Tree t, int x, int y, int w, int h, int index) {
        if (t.splitType == Tree.NONE) {
            if (getComponentCount() > index) {
                Component c = getComponent(index);
                c.setSize(w, h);
                c.setLocation(x, y);
            }
            return index + 1;
        }
        else if (t.splitType == Tree.V) {
            int h1 = (int) (t.splitPosition * h) - DIVIDER_SIZE / 2;
            int h2 = h - h1 - DIVIDER_SIZE;
            index = layout(t.first, x, y, w, h1, index);
            return layout(t.second, x, y + h1 + DIVIDER_SIZE, w, h2, index);
        }
        else if (t.splitType == Tree.H) {
            int w1 = (int) (t.splitPosition * w) - DIVIDER_SIZE / 2;
            int w2 = w - w1 - DIVIDER_SIZE;
            index = layout(t.first, x, y, w1, h, index);
            return layout(t.second, x + w1 + DIVIDER_SIZE, y, w2, h, index);
        }
        return index;
    }
    
    private final Stroke DIVIDER_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0, 
        new float[] {5.0f}, 0f); 
    
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(UIManager.getColor("Table.gridColor"));
        g2.setStroke(DIVIDER_STROKE);
        paintDivider(g, tree, 0, 0, getWidth(), getHeight());
    }

    private void paintDivider(Graphics g, Tree t, int x, int y, int w, int h) {
        if (t == null || t.splitType == Tree.NONE) {
        }
        else if (t.splitType == Tree.V) {
            int h1 = (int) (t.splitPosition * h);
            g.drawLine(x, y + h1, x + w, y + h1);
            paintDivider(g, t.first, x, y, w, h1 - DIVIDER_SIZE / 2);
            paintDivider(g, t.second, x, y + h1 + DIVIDER_SIZE / 2, w, h1 - DIVIDER_SIZE / 2);
        }
        else if (t.splitType == Tree.H) {
            int w1 = (int) (t.splitPosition * w);
            g.drawLine(x + w1, y, x + w1, y + h);
            paintDivider(g, t.first, x, y, w1 - DIVIDER_SIZE / 2, h);
            paintDivider(g, t.second, x + w1 + DIVIDER_SIZE / 2, y, w1 - DIVIDER_SIZE / 2, h);
        }
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
}