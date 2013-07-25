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
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.LinkedList;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.SummaryItem;
import org.griphyn.vdl.karajan.monitor.monitors.swing.GridView.Tree;

public class GraphsPanel extends JPanel {    
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
    
    private JPanel toolBar;
    private JPopupMenu layoutPopup;
    private LinkedList<GraphPanel> graphs;
    private GridView grid;
    
    public GraphsPanel(SystemState state) {
        this.state = state;
        this.graphs = new LinkedList<GraphPanel>();
        setLayout(new BorderLayout());

        toolBar = new JPanel();
        add(toolBar, BorderLayout.NORTH);
        
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        final JButton layout = makeButton("gui/icons/layout.png", "Layout...");        
        toolBar.add(layout);
        layout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayLayoutPopup(layout);
            } 
        });
        
        makeLayoutPopup();
        
        grid = new GridView();
        add(grid, BorderLayout.CENTER);
        
        loadLayout();
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
        gp.enable(SummaryItem.State.ACTIVE);
        gp.enable(SummaryItem.State.STAGE_IN);
        gp.enable(SummaryItem.State.STAGE_OUT);
        graphs.add(gp);
        grid.add(gp);
        saveLayout();
    }

    protected void displayLayoutPopup(JButton src) {
        layoutPopup.show(src, 6, 6);
    }

    public static JButton makeButton(String res, String alt) {
        JButton button;
        try {
            URL url = GraphsPanel.class.getClassLoader().getResource(res);
            if (url == null) {
                button = new JButton(alt);
            }
            else {
                BufferedImage icon = ImageIO.read(url);
                button = new JButton(new ImageIcon(icon));
            }
        }
        catch (Exception e) {
            button = new JButton(alt);
        }
        button.setToolTipText(alt);
        return button;
    }
}