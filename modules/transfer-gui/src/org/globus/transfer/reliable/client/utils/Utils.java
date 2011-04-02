/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.globus.transfer.reliable.client.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Some utility functions
 * @author Wantao
 */
public class Utils {
    
    /**
     * get all file system root
     * @return
     */ 
    public static File[] getFileSystemRoots() {
        //File file = new File(".");
        File[] roots = File.listRoots();
        
        return roots;  
    }
    
    public static String getProperty(String propertyName, String propertyFileName) {
    	String globusDir = System.getProperty("user.home") + File.separator + ".globus";
		File dir = new File(globusDir, "GridFTP_GUI");
    	File propFile = new File(dir, propertyFileName);
    	String ret = null;
    	if (!propFile.exists()) {
    		return null;
    	}
    	
    	Properties prop = new Properties();
    	try {
			prop.load(new FileInputStream(propFile));
			ret = prop.getProperty(propertyName);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return ret;
    }
}
