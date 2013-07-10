/*
 *
 */
package org.globus.cog.util.plugin;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

/**
 * This class allows panels to be plugged into frames
 * 
 * TODO: This should idealy be in CoG
 */
public interface Plugin {
    /**
     * Returns the component that will be added
     * @return
     */
    public Component getComponent();
    /**
     * Returns the toolbar for this plugin
     * @return
     */
    public JToolBar getToolBar();
    /**
     * Returns the Icon for this plugin
     * @return
     */
    public ImageIcon getImageIconC32x32();
    /**
     * Returns the Icon for this plugin
     * @return
     */
    public ImageIcon getImageIconC16x16();    
    /**
     * Get's the title for the plugin
     * @return
     */
    public String getTitle();
    /**
     * Returns the MenuBar for this plugin
     * @return
     */
    public JMenuBar getMenuBar();
    /**
     * <p>
     * The action used to close this plugin. This is good so the plugin knows
     * how to close its frame properly (ie an error occurs, or a command should
     * close it). Also it doesn't need to know where it is ie a frame, an
     * internal frame, etc. It is up to the eternal environment to set this
     * action (similar to JFrame.setDefaultCloseOperation).
     * </p>
     * 
     * @return
     */
    public Action getCloseAction();
    /**
     * Sets the action to close this plugin
     * @return
     */
    public void setCloseAction(Action value);
    /**
     * <p>
     * This method should always be called by when closing to ensure the plugin
     * can cleanly exit.
     * </p>
     */
    public void destroy();
}
