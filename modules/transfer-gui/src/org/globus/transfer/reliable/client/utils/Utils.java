/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.globus.transfer.reliable.client.utils;

import java.io.File;

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
}
