// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

import org.globus.cog.gridface.impl.util.FormInputPanelException;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.globus.cog.gridface.impl.util.InputField;

/**
A panel that can input a number of attributes.

  The  Panel looks similar to <br>
  +--------------------------------+<br>
  | Label1 : ___________________   |<br>
  | Label2 : ___________________   |<br>
  | Label3 : ___________________   |<br>
  | Label4 : ___________________   |<br>
  | Label5 : ___________________   |<br>
  | Label6 : ___________________   |<br>
  +--------------------------------+<br>
 it is useful to input quickly a number of inputs.
**/


/* Modifications ****************
 * added FormInputPanelException
 * changed add/remove Listener to addPropertyChangeListener
 *   now we don't use Listener
 * added functions:
 *    String getLabel(int index)
 *    void appendInput(String type, String label)
 *    void insertInput(int index, String type, String label)
 *    int getInputCount()
 *    JTextField getInputAt(int index)
 *    JLabel getLabelAt(int index)
 *
 *
 ***************************************

*/

public interface FormInputPanel {
  /**
   * Adds a listener for the ith field.
   *
   * @param i an <code>int</code> to identify which field we select.
   * @param listener a <code>Listener</code> code that is than registered with the ith field
   */
  public void addPropertyChangeListener(int i, PropertyChangeListener listener) throws FormInputPanelException;
  /**
   *
   * @param type String type type of input box
   * @param label String the label to give the input box
   */
  public void appendInput(InputField inputField, String label) throws FormInputPanelException ;
  /**
   * Faster way of doing appendInput(new InputField(InputField.TEXTFIELD,label)
   * @param label String
   * @throws FormInputPanelException
   */
  public void appendInput(String label) throws FormInputPanelException;
  /**
   * gets the ith value that is used in the panel.
   * @param value,  the value in the ith text field.
   */
  public String get(int i) throws FormInputPanelException  ;
  /**
   * gets the values coresponding to the label that is used in the panel.
   * @param value,  the value in the labeld text field.
   */
  public List get(String label) throws FormInputPanelException  ;
  /**
   * returns the InputField at a given index
   * @param index int
   * @throws FormInputPanelException
   * @return InputField
   */
  public InputField getInputAt(int index) throws FormInputPanelException;
  /**
   * returns the number of inputs
   * @return int
   */
  public int getInputCount();
  /**
   * Returns the label at index i, if i is out of bounds throws FormInputPanelException
   * @param i int
   * @throws FormInputPanelException
   * @return String
   */
  public String getLabel(int i) throws FormInputPanelException;
  /**
   * Insert an input box
   *
   * @param index int the position to insert the input box
   * @param type String type type of input box
   * @param label String the label to give the input box
   */
  public void insertInput(int index, InputField input, String label) throws FormInputPanelException;
  /**
   * Faster way of doing
   * InsertInput(index,new InputField(InputField.TEXTFIELD,label)
   *
   * @param index int
   * @param label String
   * @throws FormInputPanelException
   */
  public void insertInput(int index, String label) throws FormInputPanelException;
  /**
   * removes an input and its label at a given index
   * @param index int
   * @throws FormInputPanelException
   */
  public void removeInput(int index) throws FormInputPanelException;
  /**
   * removes a listener for the ith field.
   *
   * @param i an <code>int</code> to identify which field we select.
   * @param listener a <code>Listener</code> code that is than deregistered with the ith field
   */
  public void removePropertyChangeListener(int i, PropertyChangeListener listener) throws FormInputPanelException;
  /**
   * Set the ith label that is used in the panel.
   * @param label The new ith label.
   */
  public void setLabel(int i, String label) throws FormInputPanelException ;
  /**
   * Set the ith value that is used in the panel.
   * @param value,  the new value.
   */
  public void set(int i, String value)  throws FormInputPanelException ;


}
