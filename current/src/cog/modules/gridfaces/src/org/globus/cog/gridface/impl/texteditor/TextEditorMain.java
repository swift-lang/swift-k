/*
 * Created on Jul 27, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.globus.cog.gridface.impl.texteditor;

import java.awt.GridLayout;

import javax.swing.JFrame;

/**
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TextEditorMain extends JFrame {
  public static void main(String[] args) {
  	TextEditorMain app = new TextEditorMain();
  	TextEditor editor = new TextEditor();
  	app.getContentPane().add(editor);
  	app.getContentPane().setLayout(new GridLayout());
  	
  	app.setSize(500,500);
  	app.setVisible(true);
  	
  	try {
		Thread.sleep(3000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	editor.loadFile("file:///test.TEST");
  }
}
