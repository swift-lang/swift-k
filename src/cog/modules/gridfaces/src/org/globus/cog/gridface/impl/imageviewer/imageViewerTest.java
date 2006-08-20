
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.imageviewer;

import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.*;

// gvl: the test is veri limited and should at least containa laso the
// loading of remote files through gridftp and other protocols.

public class imageViewerTest {

  public static void create() {
    JFrame frame = new JFrame("Java CoG Kit - Image Viewer");
    URI myURI = null;
    try {
	// gvl: this is hardcoded and clearly not good.
	myURI = new URI("file:///Users/mbone/Desktop/big.jpg");
	} catch (URISyntaxException e) {
	}
    ImageViewerImplToolBar mypanel = null;
	try {
		mypanel = new ImageViewerImplToolBar();
	} catch (Exception e1) {
	}
	frame.getContentPane().add(mypanel);
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String args[]) {
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
