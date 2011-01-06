/*
 *
 */
package org.globus.cog.util.plugin;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.lang.reflect.Method;

import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

/**
 * 
 */
public class Support {
    private static final Logger logger = Logger.getLogger(Support.class);
    
    public static void inject(Object obj, String methodName, Class paramClass, Object value) {
        try {
		    Method method = obj.getClass().getMethod(methodName,new Class[] {paramClass});
		    method.invoke(obj,new Object[] {value});		    
		}catch(Exception e) {
		    logger.debug("couldn't inject",e);
		}
    }
    /**
     * <p>
     * Frame should implement the following methods:
     * </p>
     * 
     * <li>
     * <code>public void setIconImage(java.awt.Image image);</code>
     * </li>
     * <li>
     * <code>public void setJMenuBar(javax.swing.JMenuBar menu);</code>
     * </li>
     * 
     * <p>
     * It would be better if a common interface for JFrame and JInternalFrame
     * existed that defined these methods. If one is found, this should probably
     * change
     * </p>
     * 
     * @param plugin
     * @param frame
     * @return
     */
    public static Container injectPlugin(Plugin plugin, Object frame) {
        Container container = new JPanel();
		container.setLayout(new BorderLayout());
		container.add(plugin.getComponent(),BorderLayout.CENTER);
		ImageIcon icon = plugin.getImageIconC16x16();
		
		Method method;
		
		if(icon!=null) {
		  inject(frame,"setIconImage",Image.class,icon.getImage());
		}
		inject(frame,"setJMenuBar",JMenuBar.class,plugin.getMenuBar());
		
		JToolBar toolbar = plugin.getToolBar();
		if(toolbar!=null) {
		    container.add(toolbar,BorderLayout.NORTH);
		}
		
		return container;
    }
}
