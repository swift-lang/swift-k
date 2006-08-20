// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.directorybrowser;

import javax.swing.JFrame;

public class DirectoryBrowserImplTest {

    private static void create() {
        JFrame myFrame = new JFrame("Directory Browser");
        DirectoryBrowserImpl db = null;
        try {
            db = new DirectoryBrowserImpl();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        myFrame.getContentPane().add(db);
        myFrame.pack();
        myFrame.setVisible(true);
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                create();
            }
        });
    }
}