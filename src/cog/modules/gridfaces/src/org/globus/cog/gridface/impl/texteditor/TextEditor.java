/*
 * Created on Jul 27, 2004
 */
package org.globus.cog.gridface.impl.texteditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.log4j.Logger;
import org.globus.cog.gridface.interfaces.GridCommandManager;


/**
 * TODO: I need a drawing how all these components relate to each other.
 */
public class TextEditor extends JPanel {
  private static final Logger logger = Logger.getLogger(TextEditor.class);	
	
  private String fileName, filePath;
  private JEditorPane window;
  private JScrollPane scrollWindow;
  private static int KEYCODE_N = 78;
  private static int KEYCODE_S = 83;
  private static int KEYCODE_E = 69;
  private static int KEYCODE_O = 79;
  
  public TextEditor(GridCommandManager gcm, URI uri) {
  	this();  	
  	window.setFont(new Font("COURIER",Font.PLAIN,12));
	loadFile(uri.toString());
  }
  
  //For easy access from Desktop Icons, 10/7/04
  public TextEditor(String uri){
  	this();  	
  	window.setFont(new Font("COURIER",Font.PLAIN,12));
  	loadFile(uri);
  	this.setPreferredSize(new Dimension(500,400));
  }
  
  public TextEditor() {
  	this.setLayout(new BorderLayout());
  	window = new JEditorPane(); 
  	
  	addHyperlinkListener(window);
  	
  	addKeyListener(window);
	scrollWindow = new JScrollPane(window,
		      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	this.add(scrollWindow,BorderLayout.CENTER);
  }

  public void addHyperlinkListener(final JEditorPane editorPane) {
  	editorPane.setEditable(false);
    editorPane.addHyperlinkListener(
      new HyperlinkListener() {
      	public void hyperlinkUpdate(HyperlinkEvent event) {
      	    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      	      try {
      	        editorPane.setPage(event.getURL());
      	      } catch(IOException ioe) {
      	        // Some warning to user
      	      }
      	    }
      	  }
      });
  }
  
  public void addKeyListener(javax.swing.JComponent compenent) {
  	compenent.addKeyListener( new java.awt.event.KeyListener() {
    	  public void keyPressed(java.awt.event.KeyEvent event) {
System.out.println("P: "+event.getKeyCode()+ " | "+event.isControlDown());
    	    if(event.getKeyCode() == KEYCODE_S && event.isControlDown()) {
  	  		  save();
   	  	    }else if(event.getKeyCode() == KEYCODE_O && event.isControlDown()) {
      	  	  open();
     	  	}else if(event.getKeyCode() == KEYCODE_N && event.isControlDown()) {
        	  	  newFile();
     	  	}else if(event.getKeyCode() == KEYCODE_E && event.isControlDown()) {
    	  	  editable();
     	  	}
    	    
    	  }
    	  public void keyTyped(java.awt.event.KeyEvent event) { }
    	  public void keyReleased(java.awt.event.KeyEvent event) {  }
    	});
  }
  public void newFile() {
  	window.setText("");
  }
  
  public void setContentType(String contentType) {
  	window.setContentType(contentType);
  }
  
  public void setText(String content){
  	window.setText(content);
  }
  
  public void setPage(URL url) {
  	try {
		window.setPage(url);
	} catch (IOException ioException) {
		window.setText("<h1>Error: "+ioException+"</h1>");
	}
  }
  
  public void loadFile(String fileName) {
  	try {
  		if(fileName == null) {
  			fileName = chooseFile(false);
  		  	fileName = (fileName != null) ? (new File(fileName)).toURL().toString() : null;
  		}
  		this.fileName = fileName;
		window.setPage(fileName);
  		
		
	} catch (IOException e) {
		this.fileName = null;
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  public void editable() {  	
  	this.window.setEditable(!this.window.isEditable());
  	if(this.window.isEditable()) { window.getCaret().setVisible(true); }
  }
  
  public void open() {
  	loadFile(null);
  }
  
  public void save() {
  	saveToFile(null);
  }
  
  public void saveToFile(String fileName) {
    if(fileName == null) {
      this.fileName = fileName = chooseFile(true);	
    }
    if(fileName != null) {
      try {      	
		FileWriter fh = new FileWriter(fileName,false);

		fh.flush();
        window.write(fh);
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		saveToFile(null);
		e.printStackTrace();
	  }
    }else {
      // user canceled
    }
  }
  
  public String chooseFile(boolean isSave) {
  	
  	JFileChooser chooser = new JFileChooser(filePath);
  	if(isSave) {
  	  chooser.showSaveDialog(this);
  	}else {
  	  chooser.showOpenDialog(this);
  	}
  	if(chooser != null && chooser.getSelectedFile() != null) {
  	  filePath = chooser.getSelectedFile().getPath();
  	  return chooser.getSelectedFile().toString();
  	}else {
  	  return null;
  	}
  	
  }
}
