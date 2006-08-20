
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.directorybrowser;

import javax.swing.JFrame;

public class DirectoryBrowserFileTransferTest {
	
	private static void create() {
	JFrame myFrame = new JFrame("Directory Browser");
	DirectoryBrowserFileTransfers db = null;
	try {
		db = new DirectoryBrowserFileTransfers();
	} catch (Exception e) {
	}
	myFrame.getContentPane().add(db);
	myFrame.pack();
	myFrame.setVisible(true);
	}
	
	public static void main(String[] args) {
		/*try {
        UIManager.setLookAndFeel(
        		UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e) { 
    
    }*/
		
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
	        create();
	      }
	    });
	}
}
