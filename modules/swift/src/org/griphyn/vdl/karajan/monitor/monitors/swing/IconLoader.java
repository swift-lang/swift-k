//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 5, 2013
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class IconLoader {
    public static JButton makeImageButton(String res, String alt) {
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
    
    public static void setIcon(AbstractButton button, String res, String alt) {
        try {
            URL url = GraphsPanel.class.getClassLoader().getResource(res);
            if (url == null) {
                button.setText(alt);
            }
            else {
                BufferedImage icon = ImageIO.read(url);
                button.setIcon(new ImageIcon(icon));
            }
        }
        catch (Exception e) {
            button.setText(alt);
        }
        button.setToolTipText(alt);
    }
    
    public static ImageIcon loadIcon(String res) {
        try {
            URL url = GraphsPanel.class.getClassLoader().getResource(res);
            if (url == null) {
                return null;
            }
            else {
                BufferedImage icon = ImageIO.read(url);
                return new ImageIcon(icon);
            }
        }
        catch (Exception e) {
            return null;
        }
    }
}
