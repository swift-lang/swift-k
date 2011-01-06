
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.util;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.awt.GridLayout;
import java.util.Iterator;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.globus.cog.gridface.interfaces.FormInputPanel;
/**
 * A Demo of how FormInputPanel works
 */
public class FormInputPanelMain {
  private FormInputPanel formPanel;        // the main form that you modify
  private FormInputPanel indexValueForm;   // a form that allows modification of the formPanel form

  public FormInputPanelMain() throws FormInputPanelException {
    formPanel = new FormInputPanelImpl(0,4); // creates a FormInputPanel with 0 rows and 4 columns

    SwingUtilities.invokeLater(
      new Runnable() {
        public void run() {
          try {
            createAndShowGUI();
          }
          catch (FormInputPanelException ex) {
            standardOutput(ex.toString());
          }
          catch (Exception ex) {
            standardOutput(ex.getStackTrace().toString());
          }
        }
      });

  }
  public void createAndShowGUI() throws FormInputPanelException, Exception {
     final JFrame frame = new JFrame("Test Frame");
     JPanel mainPanel = new JPanel();
     
         
      
    //########### Start creation of indexValueForm ################

     /**
      * Now isn't this handy we will use the FormInputPanel to show how we
      * can modify the other FormInputPanel
      */
     indexValueForm = new FormInputPanelImpl(1,0); // 1 row and many columns
     InputField indexField = new InputField(InputField.FIELD_FORMATTED_TEXTFIELD);
     indexValueForm.appendInput(indexField,"Index (Int): ");
     
     // Below We are accessing the input after it has been added
     // we could also use getInputAt(0).getInputAsTextField() and modify it as
     // a JTextField
     indexValueForm.getInputAt(0).setMask("\\d*"); // must be positive int
     indexValueForm.appendInput("Value: ");
     // Combo for the types:
     InputField type = new InputField(InputField.FIELD_COMBOFIELD); 
     // add values to the combo
     type.getFieldAsComboField().addItem(InputField.FIELD_TEXTFIELD);
     type.getFieldAsComboField().addItem(InputField.FIELD_TEXTAREA);
     type.getFieldAsComboField().addItem(InputField.FIELD_COMBOFIELD);
     type.getFieldAsComboField().addItem(InputField.FIELD_FORMATTED_TEXTFIELD);
     type.getFieldAsComboField().addItem(InputField.FIELD_DATE_TEXTFIELD);
     type.getFieldAsComboField().addItem(InputField.FIELD_TIME_TEXTFIELD);

     // do this to allow new types to be added you will get erros when
     // trying to add a component of any new types, but shows that it
     // gives an error
     type.getFieldAsComboField().setEditable(true);
     type.getFieldAsComboField().getEditor().getEditorComponent().addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        checkThisOutMike(e);
      }
      public void keyReleased(KeyEvent e) {
      }
      public void keyTyped(KeyEvent e) {
      }
    });

     indexValueForm.appendInput(type,"Type");
     //########### End creation of indexValueForm ################
     
     
     
    //########### Start Create buttons ################

     // button to get all values
     JButton getValues = new JButton("Get all (int/String) Values");
     ActionListener getValuesListener = new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          getValues();
        }
      };
     getValues.addActionListener(getValuesListener);


    // button for get(int) values
    JButton getInt = new JButton("Get (int) Values");
    ActionListener getIntListener = new ActionListener(){
       public void actionPerformed(ActionEvent e) {
         getIntValue();
       }
     };
    getInt.addActionListener(getIntListener);

     // button for get(String) values
     JButton getString = new JButton("Get (String) Values");
     ActionListener getStringListener = new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          getStringValue();
        }
      };
     getString.addActionListener(getStringListener);    

     // Create a setField button (sets the value in the Field
     JButton setField = new JButton("Set Value Test");
     ActionListener setFieldListener = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         setValue();
       }
     };
     setField.addActionListener(setFieldListener);

     // Create a setLabel button (sets the label)
     JButton setLabel = new JButton("Set Label Test");
     ActionListener setLabelListener = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         setLabel();
       }
     };
     setLabel.addActionListener(setLabelListener);

     // Create an insert button
     JButton insert = new JButton("insert Test");
     ActionListener insertListener = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         insert();
       }
     };
     insert.addActionListener(insertListener);

     // Create a remove button
     JButton remove = new JButton("remove Test");
     ActionListener removeListener = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         remove();
       }
     };
     remove.addActionListener(removeListener);
     //########### End Create buttons ################
     
     // Set the main panel's layout
     mainPanel.setLayout(new GridLayout(0,1));
     
     //########### Start adding to formPanel ################
     // gvl: not sure why this itterates over 2 and 10, no time to look into this.
     try { // Add some stuff (one of each type for each iteration)
       for(int i=1;i<=2;i++) {
       	 
       	 // has a mask "http://.*"
         InputField ioField = new InputField(InputField.FIELD_FORMATTED_TEXTFIELD);
         ioField.setMask("http://.*");
         ioField.setString("http://");
         
         InputField ioField1 = new InputField(InputField.FIELD_TEXTAREA,200,10); // lets have 200 rows 10 cols
         
         // this is a comboField
         InputField ioField2 = new InputField(InputField.FIELD_COMBOFIELD);
         // add items to the comboField
         for(int j=0;j<10;j++) {
           // Notice we can try and get any field and modify them as you would
           // an ordinary field of their type (this case JCobmoField)
           // getFieldAsComobField() returns a JComboField
           ioField2.getFieldAsComboField().addItem("item: " + j); // add some items to the COMBO
         }
         
         // Create predefined Date Field (my change the locale by uncommenting differentLocale)
         // See InputField for link to all available Locales
         Locale differentLocale = new Locale("de","German");
         InputField ioField3 = new InputField(InputField.FIELD_DATE_TEXTFIELD/*, differentLocale*/);         
         
         InputField ioField4 = new InputField(InputField.FIELD_TIME_TEXTFIELD/*, differentLocale*/);
         
         // Create a formattter for the new field 
         NumberFormat formatter = NumberFormat.getInstance();
         formatter.setMinimumFractionDigits(3); // must have 3 decimals
         formatter.setMaximumFractionDigits(3);
         InputField ioField5 = new InputField(InputField.FIELD_FORMATTED_TEXTFIELD,formatter);
         
         // add the inputs
         formPanel.appendInput(ioField, "FORMATTED_TEXTFIELD (start http://): " + i);
         formPanel.appendInput(ioField1,"TEXTAREA: " + i);
         formPanel.appendInput(ioField2,"COMBOFIELD: "+i);
         formPanel.appendInput(ioField3,"DATEFIELD: "+i);
         formPanel.appendInput(ioField4,"TIMEFIELD: "+i);
         formPanel.appendInput(ioField5,"3 fract digits: "+i );
       }
     }
     catch (FormInputPanelException ex) {
       System.err.print(ex);
       System.exit(0);
     }
     
     //########## End Add to formPanel #################
     
     
     //############ Start adding components to the mainPanel################

     // add the main FormInputPanel
     mainPanel.add((JPanel)formPanel);

     // Add the stuff to test the main FormInputPanel to a JPanel
     JPanel south = new JPanel(new GridLayout(0,1));
     south.add(getValues);
     south.add(getInt);
     south.add(getString);
     south.add((JPanel)indexValueForm);
     south.add(setField);
     south.add(setLabel);
     south.add(insert);
     south.add(remove);

     // Add the Panel containing the testing components
     mainPanel.add(south);
     
     //######### End add components to mainPanel ########

     
     //######## Start Frame changes ###########
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     frame.setContentPane(mainPanel); // adds everything to this frame


     // You must do this if you want the frame to resize after adding components
     // It MUST be in SwingUtilities.invokeLater
     SwingUtilities.invokeLater(new Runnable() {
       public void run() {
         frame.pack();
       }
     });
     frame.setVisible(true);
     //####### End Frame changes ##########
   }
  
  
  //#########################################################
  //########## Start Button handle methods ##################  
  //#########################################################
  /**
   * Shows how to get all the values using either their label or their index (no input needed)
   */
   public void getValues() {
    try {
      standardOutput("--------------Test get(int)-------------");

      for(int i=0;i<formPanel.getInputCount();i++)
        standardOutput("Label: '"+formPanel.getLabel(i)+"'='"+formPanel.get(i)+"'");

      standardOutput("--------------Test get(String)-------------");

      for(int i=0;i<formPanel.getInputCount();i++) {
        List values = formPanel.get(formPanel.getLabel(i));
        standardOutput("Label: '"+formPanel.getLabel(i)+"'");
        for(int j=0;j<values.size();j++) {
          standardOutput("  Value: '"+values.get(j) + "'");
        }
      }
    }
    catch (FormInputPanelException ex) {
      standardOutput(ex.toString());
    }

   }
   /**
    * Shows how to get the value based upon the Label
    * 
    * Need to have something in the Field Labeled Value: of the indexValue form
    *
    */
   public void getStringValue() {
     try {
      String value = indexValueForm.get(1);
      List l = formPanel.get(value);
      Iterator i = l.iterator();
      standardOutput("Label: "+value);
      while(i.hasNext()) {
        standardOutput("  Value"+(String)i.next());
      }
    }
    catch (FormInputPanelException ex) {
      errorOutput(ex.toString());
    }
    catch (NumberFormatException ex) {
      errorOutput(ex.toString());
    }
   }
   /**
    * Shows how to get the value based upon the index
    * 
    * Need to have something in the Field Labeled Index (Int): of the indexValue form
    */
   public void getIntValue() {
        try {
         int index = Integer.parseInt(indexValueForm.get(0));
         standardOutput(formPanel.get(index));
       }
       catch (FormInputPanelException ex) {
         errorOutput(ex.toString());
       }
       catch (NumberFormatException ex) {
         errorOutput(ex.toString());
       }

      }
   /**
    * Shows how to set the value of a field
    * 
    * Need to have something in the Field Labeled Index (Int): of the indexValue form
    * Need to have something in the Field Labeled Value: of the indexValue form
    */
   public void setValue() {
     try {
      int index = Integer.parseInt(indexValueForm.get(0));
      String value = indexValueForm.get(1);
      formPanel.set(index,value);

    }
    catch (FormInputPanelException ex) {
      errorOutput(ex.toString());
    }
    catch (NumberFormatException ex) {
      errorOutput(ex.toString());
    }
   }
   /**
    * Shows how to set the label of a field
    * 
    * Need to have something in the Field Labeled Index (Int): of the indexValue form
    * Need to have something in the Field Labeled Value: of the indexValue form
    */
   public void setLabel() {
     try {
       int index = Integer.parseInt(indexValueForm.get(0));
       String value = indexValueForm.get(1);
       formPanel.setLabel(index,value);
     }
     catch (FormInputPanelException ex) {
       errorOutput(ex.toString());
     }
     catch (NumberFormatException ex) {
       errorOutput(ex.toString());
     }
   }
   /**
    * Shows how to remove a field
    * 
    * Need to have something in the Field Labeled Index (Int): of the indexValue form
    */
   public void remove() {
     try {
       int index = Integer.parseInt(indexValueForm.get(0));
       formPanel.removeInput(index);
     }
     catch (FormInputPanelException ex) {
       errorOutput(ex.toString());
     }
     catch (NumberFormatException ex) {
       errorOutput(ex.toString());
     }
   }
   /**
    * Shows how to insert a field
    * 
    * Need to have something in the Field Labeled Index (Int): of the indexValue form
    * Need to have something selected in the Field Labeled Type: of the indexValue form
    */
   public void insert() {
     try {
       int index = Integer.parseInt(indexValueForm.get(0));
       String value = indexValueForm.get(1);
       String type = indexValueForm.get(2);
       formPanel.insertInput(index,new InputField(type),value);
     }
     catch (FormInputPanelException ex) {
       errorOutput(ex.toString());
     }
     catch (NumberFormatException ex) {
       errorOutput(ex.toString());
     }
   }
   //#########################################################
   //########### End Button handle methods ###################  
   //#########################################################
   
   
   /**
    * This was for mike who wanted to know how to add items from an editable ComboBox
    * @param e
    */
  public void checkThisOutMike(KeyEvent e) {
    if("Enter".equals(KeyEvent.getKeyText(e.getKeyCode()))) {
      try {
        JComboBox combo = indexValueForm.getInputAt(2).getFieldAsComboField(); // value to set the combo to
        String value = (String)combo.getEditor().getItem();
        standardOutput("Setting as..."+value);
        indexValueForm.set(2,value);
      }
      catch (FormInputPanelException ex) {
        errorOutput("Error in checkThisOutMike. "+ex);
      }
    }
  }

  /**
   * Starts up the program
   * @param args
   */
   public static void main(String[] args) {
    try {
      new FormInputPanelMain();
    }
    catch (FormInputPanelException ex) {
      errorOutput(ex.toString());
      System.exit(0);
    }

  }
   
    // gvl: should they realy be stdout, and not going to the log
    // window.  maybe its nesessary to thing about a global method
    // that allows switching or so.


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
