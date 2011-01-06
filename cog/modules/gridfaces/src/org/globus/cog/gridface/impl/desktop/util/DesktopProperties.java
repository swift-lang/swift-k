//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.util;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DesktopProperties extends java.util.Properties {
    static org.apache.log4j.Logger logger = 
    	org.apache.log4j.Logger.getLogger(DesktopProperties.class.getName());
    public static final String PATH = System.getProperty("user.home")+File.separator+".globus"+File.separator;
    public static final String PROPERTY_FILE = "desktop.properties";
	/** Default desktop preferences file name */
	protected static final String DESKTOP_FILE = 
		"desktop"+File.separatorChar+"desktop.xml";
	
	private static DesktopProperties defaultProperties=null;
	
	public static void setDefault(DesktopProperties props) {
		defaultProperties = props;
	}

	public static DesktopProperties getDefault() {
		if (defaultProperties == null) {
			try {
				defaultProperties = new DesktopProperties();
				defaultProperties.load(new FileInputStream(new File(PATH,PROPERTY_FILE)));
			}
			catch (Exception e) {
				logger.info("did not find desktop properties", e);
				defaultProperties = createDefaultProperties();								
			}
		}
		return defaultProperties;
	}
	protected static DesktopProperties createDefaultProperties() {
	    DesktopProperties props = new DesktopProperties();
	
	    Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
	    props.setProperty("desktopfile",System.getProperty("user.home")+File.separator+DESKTOP_FILE);
	    props.setProperty("maxwidth",String.valueOf(screenSize.width));
	    props.setProperty("maxheight",String.valueOf(screenSize.height));
	    
	    // cog plugins
	    props.setProperty("desktop.plugin.org.globus.cog.gridshell.ctrl.GridShellImpl","GridShell");
	    		
		defaultProperties = props;
		try {
		    // if the .globus directory doesnt exist, create it.
		    File f = new File(PATH);
		    if (!f.isDirectory()){
		        f.mkdir();
		    }
            defaultProperties.store(new FileOutputStream(new File(PATH,PROPERTY_FILE)),"Desktop Properties File");
        } catch (Exception e) {
            logger.debug("Couldn't store newly created properties",e);
            defaultProperties = null;
        }
		return defaultProperties;
	}	
}
