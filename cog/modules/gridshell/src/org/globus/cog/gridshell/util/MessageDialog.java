/*
 * 
 */
package org.globus.cog.gridshell.util;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

/**
 * 
 */
public class MessageDialog {
    private static final Logger logger = Logger.getLogger(MessageDialog.class);
    
    private JOptionPane optionPane = new JOptionPane();
    private JDialog dialog;
    
    private Action okAction, cancelAction;
    
    public MessageDialog(Object main, Component parent,String title) {
        optionPane.setMessage(main);
		optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);		
		dialog = optionPane.createDialog(parent,title);
    }
    
    public void setOkAction(Action value) {
        okAction = value;
    }
    public void setCancelAction(Action value) {
        cancelAction = value;
    }
    public void okButtonPushed() {
        if(okAction != null) {
            logger.info("okAction!=null");
            okAction.actionPerformed(null);
        }else {
            logger.info("okAction==null");
        }
    }
    public void cancelButtonPushed() {
        if(cancelAction != null) {
            logger.info("cancelAction!=null");
            cancelAction.actionPerformed(null);
        }else {
            logger.info("cancelAction==null");
        }
    }
    
    public void show() {			
		dialog.show();			
		if(optionPane.getValue() != null 
				&& ((Integer)optionPane.getValue()).equals(
						new Integer(JOptionPane.OK_OPTION)) ) {
			logger.debug("okPushed");
			okButtonPushed();				
		}else {
			logger.debug("cancel pushed");
			cancelButtonPushed();			
		}
	}    
    public static MessageDialog createMessageDialog(Object main, Component parent,String title) {
        return new MessageDialog(main,parent,title);
    }
}
