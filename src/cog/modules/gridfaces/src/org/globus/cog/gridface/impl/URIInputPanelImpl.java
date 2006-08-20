
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyListener;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.globus.cog.gridface.interfaces.URIInputPanel;


public class URIInputPanelImpl extends JPanel implements URIInputPanel {
  private URI uri;
  private JLabel uriInputLabel;
  private JTextField uriField;

  /**
   * Creates the two components, <code>urInputLabel</code> and <code>uriField</code>.
   */
  public URIInputPanelImpl() {
    super(new GridLayout(0,1));
    uriInputLabel = new JLabel();
    uriField = new JTextField(20);
    uriField.setMargin(new Insets(0,0,0,0));
    //this.add(uriInputLabel);
    this.add(uriField);
    
  }
  
  public void setCursor(Cursor c) {
  	super.setCursor(c);
  	uriField.setCursor(c);
  }
  
  public void setEnabled(boolean choice) {
  	super.setEnabled(choice);
  	uriField.setEnabled(choice);
  }
  
  public Dimension getMinimumSize() {
  	return uriField.getSize();
  }

  /**
   * set the label that is used in the panel.
   * @param label The new label.
   */
  public void setLabel(String label) {
    uriInputLabel.setText(label);
  }

  /**
   * Set the value that is used in the panel.
   * @param value,  the new value.
   */
  public void set(URI value) {
    uri = value;
    uriField.setText(value.toString());
  }

  
  /**
   * Gets the value in the panel and sets the internal URI to that.
   * @param value,  the value in the text field.
   */
  public void get() {
    try {
      uri = new URI(uriField.getText());
    }
    catch (URISyntaxException ex) {
      uri = null;
    }
  }
  
  
  /**
   * Return the URI
   * @return URI
   */
  public URI getURI() {
    return uri;
  }
  
  public void addKeyListener(KeyListener listener){
  	uriField.addKeyListener(listener);
  }
}


