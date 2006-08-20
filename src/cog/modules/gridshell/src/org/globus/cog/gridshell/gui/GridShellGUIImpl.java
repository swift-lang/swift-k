
package org.globus.cog.gridshell.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.interfaces.GridShellSwingGUI;

/**
 * 
 */
public class GridShellGUIImpl extends JPanel implements GridShellSwingGUI {
  private static Logger logger = Logger.getLogger(GridShellGUIImpl.class);
  // The scroll panes
  private JScrollPane scrollHistory, scrollCommand;
  // The history
  private JTextComponent history = new JTextArea();
  // The command
  private VetoableTextAreaImpl command = new VetoableTextAreaImpl();
  // A filter for the command area (prevents prompt from being messed with)
  private PromptFilterImpl filter;  
  // A height and width
  private final int HEIGHT = 300;
  private final int WIDTH = 500;
  
  private static final Insets INSETS = new Insets(5,5,5,5);
  
  
  public class Bool {
  	private boolean isLocked;
  	public Bool(boolean isLocked) {
  		this.isLocked = isLocked;
  	}
  	public boolean isLocked() {
  		return isLocked;
  	}
  	public synchronized void lock() {
  		isLocked = true;
  	}
  	public synchronized void unlock() {
  		isLocked = false;
  	}
  }
  private final Bool historyLock = new Bool(false);
    
  public GridShellGUIImpl() {  	
    // initialize things
    filter = new PromptFilterImpl(command);
    command.addVetoableChangeListener(filter);    
    
    this.setLayout(new BorderLayout());

    history.setMargin(INSETS);
    // Put the history in a scroll panel
    scrollHistory = new JScrollPane(history,
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollHistory.setPreferredSize(new Dimension(WIDTH,HEIGHT));

    // Diable the history field
    //getHistoryField().setEnabled(false);
    history.setEditable(false);

    command.setMargin(INSETS);
    // Put the command field in a scroll panel
    JScrollPane scrollCommand = new JScrollPane(command,
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollCommand.setPreferredSize(new Dimension(WIDTH,(int)(.3*HEIGHT)));

    // Create a split pane and set the layout and set the size
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT); // new JPanel(new GridLayout(0,1));
    split.setResizeWeight(1.0);
    split.add(scrollHistory);
    split.add(scrollCommand);

    this.setPreferredSize(split.getPreferredSize());   
       
    this.add(split);
  }
  /*  (non-Javadoc)
   * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellGUI#getCommandField()
   */
  public JTextComponent getCommandField() {
  	return command;
  }
  /*  (non-Javadoc)
   * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellGUI#getHistoryField()
   */
  public JTextComponent getHistoryField() {
  	return history;
  }
  
  
  /*
   *  (non-Javadoc)
   * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellGUI#appendHistoryValue(java.lang.String)
   */
  public void appendHistoryValue(final String value) {
  	logger.debug("appendHistoryValue ("+value+")");
    final String oldValue = getHistoryValue();
    setHistoryValue(oldValue+value);    
  }
  /*
   *  (non-Javadoc)
   * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellGUI#setHistoryValue(java.lang.String)
   */
  public void setHistoryValue(final String value) {
  	logger.info("setHistory ("+value+")");
  	history.setText(value);
  	history.setCaretPosition(history.getText().length());
  }
  /*
   *  (non-Javadoc)
   * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellGUI#getHistoryValue()
   */
  public String getHistoryValue() {
    return history.getText();
  }
  /*
   *  (non-Javadoc)
   * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellGUI#setCommandValue(java.lang.String)
   */
  public void setCommandValue(final String value) {
  	command.setText(value);    
  }
  /*
   *  (non-Javadoc)
   * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellGUI#getCommandValue()
   */
  public String getCommandValue() {
    return command.getText();
  }  
  /*
   *  (non-Javadoc)
   * @see org.globus.cog.gridface.impl.gridshell.interfaces.GridShellGUI#getJComponent()
   */
  public JComponent getJComponent() {
  	return (JComponent)this;
  }
  /**
   * Used to debug ShellPanelGUI
   */
  public static void createAndShowGUI() {
    JFrame frame = new JFrame();
    frame.getContentPane().add(new GridShellGUIImpl());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.show();
  }
  /**
   * Used to debug ShellPanelGUI
   * @param args
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }
  
  public void propertyChange(PropertyChangeEvent pEvent) {
  	if(pEvent != null && pEvent.getPropertyName() != null && pEvent.getPropertyName().equals("prompt")) {
  		setPrompt(pEvent.getNewValue().toString());
  	}
  }
  
  private void setPrompt(final String value) {
  	final String oldPrompt = filter.getPrompt();
  	filter.setPrompt(value);
  	  			
	String cValue = getCommandValue();
	if(cValue != null && oldPrompt != null) {
	  cValue = cValue.replaceFirst(oldPrompt,value);
	}else {
	  cValue = value;
	} 			
    setCommandValue(cValue);  	    
  }
}
