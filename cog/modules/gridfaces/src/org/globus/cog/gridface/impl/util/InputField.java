
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.util;


import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.JTextComponent;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.text.DateFormat;
import java.text.Format;
import java.util.Date;
import java.util.Locale;
/**
 * For a listing of all Locale's go to:
 * http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt
 * 
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class InputField extends JPanel {
  private String type;
  private JComponent field;
 
    
  /* Allows you to set the type for constructor easier */
  public static final String FIELD_TEXTFIELD = "JTextField";
  public static final String FIELD_COMBOFIELD = "JComboBox";
  public static final String FIELD_TEXTAREA = "JTextArea";
  public static final String FIELD_FORMATTED_TEXTFIELD = "JFormattedTextField";
  // Both DateField and TimeField are predefined types of FORMATTED_TEXTFIELD and when
  // getting calling getType they return JFormattedTextField
  public static final String FIELD_DATE_TEXTFIELD = "DateField"; 
  public static final String FIELD_TIME_TEXTFIELD = "TimeField";
  
  /* Easy masks to add to a FIELD_FORMATTED_TEXTFIELD */
  public static final String MASK_DOUBLE = "-?\\d*\\.?\\d*";
  public static final String MASK_INT = "-?\\d*";
  public static final String MASK_POS_INT = "\\d*";

  /* Create a InputField that is type type */
  public InputField(String type) throws FormInputPanelException {
    this(type,100,10);    
  }
  /* Allows you to specify a type and a locale for an InputField */
  public InputField(String type, Locale locale) throws FormInputPanelException {
    this(type,100,10,null,locale);    
  }
  /* Allows you to specify a type and a formatter (formatter should only be used on FORMATTED_TEXTFIELD */
  public InputField(String type, Format formatter) throws FormInputPanelException {
    this(type,100,10,formatter,getDefaultLocale());    
  }
  /* Allows user to specify a type, formatter, and a locale */
  public InputField(String type, Format formatter, Locale locale) throws FormInputPanelException {
    this(type,100,10,formatter,locale);    
  }
  
  /* Allows user to specify a type, rows, and cols  */  
  public InputField(String type, int rows, int cols) throws FormInputPanelException {  	
  	this(type,rows,cols,null,getDefaultLocale());
  }
  
  /**
   * Main constructor
   * @param type  the type of InputField (Defaults to InputField)
   * @param rows  the number of rows (Defaults to 100)
   * @param cols  the number of cols (Defaults to 10)
   * @param formatter the formatter for a FORMATTED_TEXTFIELD (Defaults to none)
   * @param myLocale the locale used for Date and Time Fields (Defaults to the default locale w/in Java)
   * @throws FormInputPanelException
   */
  public InputField(String type,int rows, int cols, Format formatter, Locale myLocale) throws FormInputPanelException {
  	super();
    setType(type); // set the type
    
    JComponent newField; // the field to be added to contents
    JComponent contents; // if TEXTAREA contains newField else = newField

    if(FIELD_TEXTFIELD.equals(type)) { // This is a JTextField
      contents = newField = new JTextField(cols);
    }else if(FIELD_COMBOFIELD.equals(type)) { // This is a JComboBox
      JComboBox combo = new JComboBox();
      contents = newField = combo;
    }else if(FIELD_TEXTAREA.equals(type)) { // This is a JTextArea
      newField = new JTextArea();
      contents = new JScrollPane(newField);    // TextAreas are contained in newField
      contents.setPreferredSize(new Dimension(rows,cols)); // set the preferredSize
    }else if(FIELD_FORMATTED_TEXTFIELD.equals(type)) { // JFormattedTextField
      if(formatter != null) { // of formatter is not null use it
      	contents = newField = new JFormattedTextField(formatter);	
      }else { // don't use the formatter (it is null)
      	contents = newField = new JFormattedTextField();
      }            
    }else if(FIELD_DATE_TEXTFIELD.equals(type)) { // A type of JFormattedTextField
      Format format = DateFormat.getDateInstance(DateFormat.MEDIUM,myLocale);  
      
      contents = newField = new JFormattedTextField(format);
      String value =format.format(new Date());
      setType(FIELD_FORMATTED_TEXTFIELD);
      this.setString(value);      
    }else if(FIELD_TIME_TEXTFIELD.equals(type)) { // A type of JFormattedTextField
        Format format = DateFormat.getTimeInstance(DateFormat.DEFAULT,myLocale);        
        contents = newField = new JFormattedTextField(format);
        String value =format.format(new Date());
        setType(FIELD_FORMATTED_TEXTFIELD);
        this.setString(value);
    }else { // Invalid string passed throw exception    
      throw new FormInputPanelException("Error: Invalid type, '"+type+"',for InputField");
    }
    setField(newField); // set the field to newField
    this.setLayout(new GridLayout(1,1)); // set our layout (GridLayout greedy for space)
    this.add(contents); // add our contents
  }

  /**
   * returns the field as JComponent
   * @return JComponent
   */
  public synchronized JComponent getField() {
    return field;
  }

  /**
   * returns the field as a JTextField if it is that type else exception thrown
   * @throws FormInputPanelException
   * @return JTextField
   */

  public synchronized JTextField getFieldAsTextField() throws FormInputPanelException {
    if(FIELD_TEXTFIELD.equals(getType())) {
      return (JTextField) getField();
    }else {
      throw new FormInputPanelException("Error cannot get InputField as type TextField, it is type "+getType());
    }
  }
  /**
   * returns field as JComboBox if not correct type throw exception
   * @throws FormInputPanelException
   * @return JComboBox
   */
  public synchronized JComboBox getFieldAsComboField() throws
      FormInputPanelException {
    if(FIELD_COMBOFIELD.equals(getType())) {
      return (JComboBox)getField();
    }else {
      throw new FormInputPanelException("Error cannot get InputField as type COMBOFIELD, it is type "+getType());
    }
  }
  /**
   * returns field as JTextArea if not correct type throw exception
   *
   * @throws FormInputPanelException
   * @return JTextArea
   */
  public synchronized JFormattedTextField getFieldAsFormattedTextField() throws
      FormInputPanelException {
    if(FIELD_FORMATTED_TEXTFIELD.equals(getType())) {
      return (JFormattedTextField)getField();
    }else {
      throw new FormInputPanelException("Error cannot get InputField as type TEXTAREA, it is type "+getType());
    }
  }
  /**
   * returns field as JTextArea if not correct type throw exception
   *
   * @throws FormInputPanelException
   * @return JTextArea
   */
  public synchronized JTextArea getFieldAsTextArea() throws
      FormInputPanelException {
    if(FIELD_TEXTAREA.equals(getType())) {
      return (JTextArea)getField();
    }else {
      throw new FormInputPanelException("Error cannot get InputField as type TEXTAREA, it is type "+getType());
    }
  }
  /**
   * returns all of the choices of a COMBOBOX as Object[] if COMBOBOX else
   * throws an exception
   *
   * @throws FormInputPanelException
   * @return Object[]
   */
  public synchronized Object[] getComboChoices() throws FormInputPanelException {
    JComboBox combo = this.getFieldAsComboField();
    int length = combo.getItemCount();
    Object[] items = new Object[length];

    for(int i=0;i<length;i++) {
      items[i] = combo.getItemAt(i);
    }
    return items;
  }

  /**
   * this is the function that allows you to get the value in any of the input
   * types (what makes it useful)
   *
   * NOTE: Swing is not thread-safe and you should use SwingUtilities.invokeLater
   *       for this function
   *
   * @return String
   */
  public synchronized String getString() {
    if(FIELD_TEXTFIELD.equals(getType()) 
    		|| FIELD_TEXTAREA.equals(getType())
			|| FIELD_FORMATTED_TEXTFIELD.equals(getType())) {
      return ((JTextComponent)getField()).getText();
    } else if(FIELD_COMBOFIELD.equals(getType())) {
      JComboBox box = ((JComboBox)getField());
      if(box.getSelectedItem() != null) {
        return box.getSelectedItem().toString();
      }else {
        return "";
      }
    } else { // This should NEVER happen!
      errorOutput("Error: Invalid InputField type...something is seriously wrong!");
      System.exit(0);
      return null; // unreachable
    }
  }

  /**
   * This will set the value of a TEXTFIELD or TEXTAREA to value
   * it will addItem value to a COMBOFIELD
   * @param value String
   */
  public synchronized void setString(final String value) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if(FIELD_TEXTFIELD.equals(getType()) 
        		|| FIELD_TEXTAREA.equals(getType())
				|| FIELD_FORMATTED_TEXTFIELD.equals(getType())) {
           ((JTextComponent)getField()).setText(value);
        } else if(FIELD_COMBOFIELD.equals(getType())) {
          try {
            JComboBox box = getFieldAsComboField();
            boolean shouldAddValue = true;
            for(int i=0;i<box.getItemCount();i++) {
              if(box.getItemAt(i) != null && box.getItemAt(i).equals(value)) {
                shouldAddValue = false;
                break;
              }
            }
            if(shouldAddValue) {
              box.addItem(value);
            }
            
            box.setSelectedItem(value);
          }
          catch (FormInputPanelException ex) {
            errorOutput(ex.toString());
          }

        }
      }
    });
  }
  /**
   * This sets a listener to prevent the input from looking like match as a
   * regular expression
   *
   * A hint at producing a mask is to ensure that the expression is valid at
   * every increment ie: you want it to be a valid double use: "-?\\d*\\.?\\d*"
   * 
   * It is recommended for you to use the formatter option rather than setting a mask
   * if there is a formatter available.
   *
   * it will be valid at any point in the entry:
   *       # nothing must be valid too! (unless you set the inital value)
   * -
   * -1
   * -12
   * -12.
   * -12.3
   * 
   * It must be valid at any time, that means if we take out anything such as the decimal or
   * the - it must still match.
   * 
   * See Links below for using RegExp:
   * http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.htm
   * http://java.sun.com/docs/books/tutorial/extra/regex/
   *
   * @param match String
   * @throws FormInputPanelException
   */
  public synchronized void setMask(String match) throws FormInputPanelException {
  	  if(InputField.FIELD_FORMATTED_TEXTFIELD.equals(getType())) {
  	    RegexFormatter formatter = new RegexFormatter(match);
      
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        formatter.setOverwriteMode(false);
        this.getFieldAsFormattedTextField().setFocusLostBehavior(JFormattedTextField.REVERT);
        this.getFieldAsFormattedTextField().setFormatterFactory(new DefaultFormatterFactory(formatter));
  	  } else {
  	  	throw new FormInputPanelException("Error: Can only set mask for type FIELD_FORMATTED_TEXTFIELD.\nThis instance is: "+getType());
  	  }
  }
  
  
  /**
   * private function that sets field (should only be done in constructor)
   * @param newField JComponent
   */
  private synchronized void setField(JComponent newField) {
    this.field = newField;
  }
  /**
   * private function that sets the type (should only be done in the constructor)
   * @param type String
   */
  private synchronized void setType(String type) { this.type = type; }
  /**
   * returns the type Note DateField and TimeField return are types simply
   * predefined formatted fields so they return as JFormattedField
   * 
   * @return String
   */
  public synchronized String getType() { return this.type; }
  

    // gvl: should this be going to the log window?  
  /**
   * Standard output
   * @param message
   */
  private static synchronized void standardOutput(String message) {
  	System.out.println(message);
  }

  /**
   * For error output
   * @param message
   */
  private static synchronized void errorOutput(String message) {
  	System.err.println(message);
  }
  
  
}
