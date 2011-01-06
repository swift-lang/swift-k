
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.util;

import org.globus.cog.gridface.interfaces.FormInputPanel;

import javax.swing.*;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;

// gvl: the use of this may need to be documented in the programmers manual

public class FormInputPanelImpl extends JPanel implements FormInputPanel {

  private JPanel inputForm = new JPanel();
  private JScrollPane scrollPane;

  private List inputLabels = new ArrayList();
  private List inputBoxes = new ArrayList();

  public FormInputPanelImpl() throws FormInputPanelException {
    this(0,2);
  }

  public FormInputPanelImpl(int rows,int cols) throws FormInputPanelException {
    if(cols<0 || rows<0) {
      throw new FormInputPanelException("Error: Both rows and cols must be an int > 0 to the FormInputPanelImp constructor");
    }

    // Set the Layouts
    this.setLayout(new BorderLayout());
    inputForm.setLayout(new GridLayout(rows,cols));

    // Add each panel to the main panel (this)
    scrollPane = new JScrollPane(inputForm);
    this.add(scrollPane,BorderLayout.CENTER);
  }
  /**
   * Set the ith label that is used in the panel.
   *
   * @param index int
   * @param label String
   * @throws FormInputPanelException
   */
  public synchronized void setLabel(final int index, final String label) throws FormInputPanelException {
      if(index<0 || index >= inputBoxes.size())
        throw new FormInputPanelException("Error: set cannot set value at "+index+" it doesn't exists!");

      SwingUtilities.invokeLater( new Runnable() {
        public synchronized void run() {
          ((JLabel)inputLabels.get(index)).setText(label);
        }
      });
    }
    /**
     * Set the ith value that is used in the panel.
     * If it doesn't exist throw FormInputPanelException
     *   NOTE: this is because we have no label to give it
     * @param index int
     * @param value String
     * @throws FormInputPanelException
     */
    public synchronized void set(final int index, final String value) throws FormInputPanelException {
      if(index<0 || index >= inputBoxes.size()) {
        throw new FormInputPanelException("Error: set cannot set value at "+index+" it doesn't exists!");
      }
      SwingUtilities.invokeLater(new Runnable(){
        public synchronized void run() {
          ((InputField)inputBoxes.get(index)).setString(value);
        }
      });
    }
    /**
     * gets the ith value that is used in the panel.
     * @param index int
     * @throws FormInputPanelException
     * @return String
     */
    public synchronized String get(int index)  throws FormInputPanelException {
      if(index >=0 && index < inputBoxes.size() ) {
        return  (getInputAt(index)).getString();
      }else {
        throw new FormInputPanelException("Error: get cannot get value at "+index+" it doesn't exists!");
      }
    }

    /**
     * gets all values that coresponds to the label that is used in the panel.
     * @param label String
     * @throws FormInputPanelException
     * @return List
     */
    public synchronized List get(String label)  throws FormInputPanelException {
      List values = new ArrayList();
      for(int i=0;i<inputLabels.size();i++) {
        if(label.equals(getLabel(i))) {
          values.add(get(i));
        }
      }

      if(values.size() > 0) {
        return values;
      }else {
        throw new FormInputPanelException(
            "Error: get(String label) did not find label " + label + ".");
      }
    }

    public synchronized void appendInput(final String label) {
      try {
        appendInput(new InputField(InputField.FIELD_TEXTFIELD), label);
      }
      catch (FormInputPanelException ex) {
        System.err.println(ex);
      }
    }

    /**
     * Equivalent of insertInput(getInputCount(),jInput,label)
     *
     * @param type String type type of input box
     * @param label String the label to give the input box
     */
     public synchronized void appendInput(final InputField jInput, final String label) {
      try {
        insertInput(getInputCount(), jInput, label);
      }
      catch (FormInputPanelException ex) {
        System.err.println(ex);
      }

     }
     /**
      * equivalent of:
      *   insertInput(index,new InputField(InputField.TEXTFIELD),label)
      *
      * @param index int
      * @param label String
      * @throws FormInputPanelException
      */
     public synchronized void insertInput(int index, String label) throws FormInputPanelException {
      try {
        insertInput(index, new InputField(InputField.FIELD_TEXTFIELD), label);
      }
      catch (FormInputPanelException ex) {
        System.err.println(ex);
      }
     }

     /**
      * Insert an input box at index
      *
      * @param index int the position to insert the input box
      * @param type String type type of input box
      * @param label String the label to give the input box
      * @throws FormInputPanelException
      */
     public synchronized void insertInput(final int index,final InputField inputField, String label) throws
      FormInputPanelException {
       if(index<0 || index>this.getInputCount()) {
         throw new FormInputPanelException("Error: Could not insert at index '"+index+"'. Constraint: 0<=index<=inputCount.\ncurrently inputCount="+getInputCount());
       }
       final JLabel jLabel = new JLabel(label);

       inputBoxes.add(index,inputField);
       inputLabels.add(index,jLabel);
       SwingUtilities.invokeLater(new Runnable() {
         public void run() {
           getInputForm().add(jLabel,index*2);
           getInputForm().add(inputField,index*2+1);
           getScrollPane().validate(); // used when inserting
         }
       });
     }
     public synchronized void removeInput(final int index) throws
          FormInputPanelException {
           if(index<0 || index>=this.getInputCount()) {
             throw new FormInputPanelException("Error: Could not insert at index '"+index+"'. Constraint: 0<=index<inputCount.\ncurrently inputCount="+getInputCount());
           }
           final Object inputField = this.getInputAt(index);
           final Object inputLabel = this.getLabelAt(index);
           SwingUtilities.invokeLater(new Runnable() {
             public void run() {
               getInputForm().remove(index*2);
               getInputForm().remove(index*2); // really at index*2 +1
                                               // but just removed one
               inputLabels.remove(inputLabel);
               inputBoxes.remove(inputField);
               getScrollPane().validate(); // used when inserting
             }
           });
         }

     /**
      * returns the number of inputs (which equals the number of labels)
      * @return int
      */
     public synchronized int getInputCount() {
       return inputBoxes.size();
     }
     /**
      * gets the scrollPane
      * @return JScrollPane
      */
     public synchronized JScrollPane getScrollPane() {
       return scrollPane;
     }
     /**
      * sets the scrollPane to value
      * @param value JScrollPane
      */
     public synchronized void setScrollPane(JScrollPane value) {
       scrollPane = value;
     }

     public synchronized JPanel getInputForm() {
       return inputForm;
     }


    /**
     * Adds a listener for the ith field.
     *
     * @param i an <code>int</code> to identify which field we select.
     * @param listener a <code>Listener</code> code that is than registered with the ith field
     */
     public synchronized void addPropertyChangeListener(int index, PropertyChangeListener listener) throws FormInputPanelException {
       getInputAt(index).addPropertyChangeListener(listener);
     }

    /**
     * removes a listener for the ith field.
     *
     * @param i an <code>int</code> to identify which field we select.
     * @param listener a <code>Listener</code> code that is than deregistered with the ith field
     */
    public synchronized void removePropertyChangeListener(int index, PropertyChangeListener listener) throws FormInputPanelException {
      getInputAt(index).removePropertyChangeListener(listener);
    }



    public synchronized InputField getInputAt(int index) throws FormInputPanelException {
      if(index>=0 && index<inputBoxes.size()) {
        return ( (InputField)this.inputBoxes.get(index));
      }else {
        throw new FormInputPanelException("Error: Cannot get input at index "+index+", index is out of bounds.");
      }
    }
    public synchronized JLabel getLabelAt(int index) throws FormInputPanelException {
      return ((JLabel)this.inputLabels.get(index));
    }



 /**
  * Get the ith label that is used in the panel. Helper for get(String label)
  * @param label The new ith label.
  */
  public synchronized String getLabel(int index)  throws FormInputPanelException {
    if(index < inputLabels.size() ) {
      return ((JLabel)this.inputLabels.get(index)).getText();
    }else {
      throw new FormInputPanelException("Error: setLabel cannot set label at "+index+" it doesn't exists!");
    }
  }  

  /**
   * Standard output
   * @param message
   */
  private synchronized void standardOutput(String message) {
  	System.out.println(message);
  }

  /**
   * For error output
   * @param message
   */
  private synchronized void errorOutput(String message) {
  	System.err.println(message);
  }
}
