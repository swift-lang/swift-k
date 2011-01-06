
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.examples.mimehandler;

import javax.swing.JFrame;

import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.impl.mimehandler.MimeHandler;
import org.globus.cog.gridface.impl.mimehandler.MimeHandlerEditor;

public class MimeHandlerEditorTest {
	
	private static void create() throws Exception {
	JFrame myFrame = new JFrame("Mime Handler Editor");
	MimeHandlerEditor mhe = new MimeHandlerEditor(new MimeHandler(new GridCommandManagerImpl()));
	myFrame.getContentPane().add(mhe);
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
	        try {
				create();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      }
	    });
	}
}
