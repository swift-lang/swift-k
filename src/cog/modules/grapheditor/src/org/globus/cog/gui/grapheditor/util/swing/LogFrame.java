
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util.swing;

import javax.swing.*;
import java.awt.*;

/**
 * A simple log frame
 */
public class LogFrame extends JFrame {

    JTextArea textArea;
    JScrollPane scrollPane;

    public LogFrame() {
        super();
        setTitle("Output log");
        textArea = new JTextArea();
        textArea.setEditable(false);
        //Font monospaced = new Font("monospaced", Font.PLAIN, 11);
        //textArea.setFont(monospaced);
        scrollPane = new JScrollPane(textArea);
        getContentPane().setLayout(new BorderLayout());
        setSize(500, 200);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        show();
    }

    public void append(String text) {
        textArea.append(text + "\n");
        textArea.getCaret().setDot(textArea.getText().length());
        scrollPane.scrollRectToVisible(textArea.getVisibleRect());
    }
	
	public void clear(){
		textArea.setText("");
	}
}
