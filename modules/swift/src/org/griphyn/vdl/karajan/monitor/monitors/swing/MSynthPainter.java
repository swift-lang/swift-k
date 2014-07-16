//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthPainter;

public class MSynthPainter extends SynthPainter {
    /*public static final Color INACTIVE = new Color(30, 30, 30);
    public static final Color BG = new Color(60, 60, 60);
    public static final Color SELECTED = new Color(100, 100, 100);
    public static final Color HIGHLIGHT = new Color(140, 140, 140);
    public static final Color ACCENT = new Color(180, 160, 40);*/

    public static final Color INACTIVE = new Color(190, 190, 190);
    public static final Color BG = new Color(220, 220, 220);
    public static final Color SELECTED = new Color(180, 180, 180);
    public static final Color HIGHLIGHT = new Color(255, 255, 255);
    public static final Color ACCENT = new Color(30, 160, 180);
    public static final Color BLANK = Color.WHITE;

  
    @Override
    public void paintTabbedPaneBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
    }

    @Override
    public void paintTabbedPaneTabAreaBackground(SynthContext context, Graphics g, int x, int y, int w, int h,
            int orientation) {
        g.setColor(BLANK);
        g.fillRect(x, y, w, h);
    }
    
    private void paintGenericRoundedBackground(Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(BG);
        g.fillRoundRect(x, y, w, h, 5, 5);
    }
    
    private void paintGenericRoundedBackground(Graphics g, int x, int y, int w, int h, Color c) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(c);
        g.fillRoundRect(x, y, w, h, 5, 5);
    }

    @Override
    public void paintTabbedPaneContentBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
        paintGenericRoundedBackground(g, x, y, w, h);
    }

    @Override
    public void paintTabbedPaneContentBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(BG);
        g.drawRoundRect(x, y, w, h, 6, 6);
    }

    @Override
    public void paintTabbedPaneTabBackground(SynthContext context, Graphics g, int x, int y, int w, int h,
            int tabIndex, int orientation) {
        JTabbedPane t = (JTabbedPane) context.getComponent();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (t.getSelectedIndex() == tabIndex) {
            g.setColor(BG);
        }
        else {
            g.setColor(INACTIVE);
        }
        g.fillRoundRect(x, y, w - 2, h, 5, 5);
        g.fillRect(x, y + 5, w - 2, h - 3);
    }
    
    @Override
    public void paintTabbedPaneTabBorder(SynthContext context, Graphics g, int x, int y, int w, int h, int tabIndex) {
        JTabbedPane t = (JTabbedPane) context.getComponent();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (t.getSelectedIndex() == tabIndex) {
            g.setColor(ACCENT);
            g.fillRoundRect(x, y, w - 2, 5, 5, 5);
            g.setColor(BG);
            g.fillRect(x, y + 2, w - 2, 4);
        }
    }

    @Override
    public void paintProgressBarBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(BG);
        g.fillRoundRect(x, y, w, h, 4, 4);
        g.setColor(BLANK);
        
        g.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 3, 3);
    }

    @Override
    public void paintProgressBarBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
    }

    @Override
    public void paintProgressBarForeground(SynthContext context, Graphics g, int x, int y, int w, int h, int orientation) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(ACCENT);
        g.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 3, 3);
    }

    @Override
    public void paintTableHeaderBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
        g.setColor(BG);
        g.fillRect(x, y, 4, h);
        g.fillRect(x + w - 3, y, 4, h);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(SELECTED);
        g.fillRoundRect(x, y, w, h, 5, 5);
    }

    @Override
    public void paintTableHeaderBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
    }

    @Override
    public void paintTableBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
        g.setColor(BG);
        g.fillRect(x, y, w, h);
    }

    @Override
    public void paintTableBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
        g.setColor(BLANK);
        g.drawRect(x, y, w, h);
    }

    @Override
    public void paintScrollPaneBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
        paintGenericRoundedBackground(g, x, y, w, h);
    }

    @Override
    public void paintScrollPaneBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
    }    

    @Override
    public void paintScrollBarBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
        this.paintGenericRoundedBackground(g, x, y, w, h, Color.BLACK);
    }

    @Override
    public void paintScrollBarBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
    }

    @Override
    public void paintScrollBarThumbBackground(SynthContext context, Graphics g, int x, int y, int w, int h,
            int orientation) {
        this.paintGenericRoundedBackground(g, x, y, w, h, SELECTED);
    }

    @Override
    public void paintScrollBarThumbBorder(SynthContext context, Graphics g, int x, int y, int w, int h, int orientation) {
    }

    @Override
    public void paintScrollBarTrackBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
    }

    @Override
    public void paintScrollBarTrackBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
        super.paintScrollBarTrackBorder(context, g, x, y, w, h);
    }

    @Override
    public void paintArrowButtonBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {        
    }

    @Override
    public void paintArrowButtonBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
    }

    @Override
    public void paintArrowButtonForeground(SynthContext context, Graphics g, int x, int y, int w, int h, int direction) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(SELECTED);
        switch (direction) {
            case SwingConstants.NORTH:
                g2.fillRoundRect(x, y, w, h + 4, 5, 5);
                g2.setColor(Color.BLACK);
                g2.fillRect(x, y + h, w, 4);
                break;
            case SwingConstants.SOUTH:
                g2.fillRoundRect(x, y - 4, w, h + 4, 5, 5);
                g2.setColor(Color.BLACK);
                g2.fillRect(x, y - 4, w, 4);
                break;
            case SwingConstants.WEST:
                g2.fillRoundRect(x, y, w + 4, h, 5, 5);
                g2.setColor(Color.BLACK);
                g2.fillRect(x + w, y, 4, h);
                break;
            case SwingConstants.EAST:
                g2.fillRoundRect(x - 4, y, w + 4, h, 5, 5);
                g2.setColor(Color.BLACK);
                g2.fillRect(x - 4, y, 4, h);
                break;
        }
        
        
        g2.setColor(ACCENT);
        Path2D p = new Path2D.Double();
        p.moveTo(4, 0);
        p.lineTo(8, 6);
        p.lineTo(0, 6);
        p.closePath();
        
        switch (direction) {
            case SwingConstants.NORTH:
                // no transform
                break;
            case SwingConstants.SOUTH:
                p.transform(AffineTransform.getRotateInstance(Math.PI, 4, 4));
                break;
            case SwingConstants.WEST:
                p.transform(AffineTransform.getRotateInstance(-Math.PI / 2, 4, 4));
                break;
            case SwingConstants.EAST:
                p.transform(AffineTransform.getRotateInstance(Math.PI / 2, 4, 4));
                break;
        }
        
        p.transform(AffineTransform.getTranslateInstance((w - 8) / 2.0, (h - 7) / 2.0));
        g2.fill(p);
    }

    @Override
    public void paintViewportBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
        g.setColor(BG);
        g.fillRect(x, y, w, h);
    }

    @Override
    public void paintTextAreaBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
        this.paintGenericRoundedBackground(g, x, y, w, h, Color.BLACK);
    }

    @Override
    public void paintTextAreaBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
    }

    @Override
    public void paintFormattedTextFieldBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
        this.paintGenericRoundedBackground(g, x, y, w, h, Color.BLACK);
    }

    @Override
    public void paintFormattedTextFieldBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
    }

    @Override
    public void paintButtonBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (context.getComponentState() == SynthConstants.PRESSED) {
            g.setColor(ACCENT);
        }
        else {
            g.setColor(SELECTED);
        }
        g.fillRoundRect(x + 1, y + 1, w - 2, h - 2, 5, 5);
    }

    @Override
    public void paintButtonBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
    }
}
