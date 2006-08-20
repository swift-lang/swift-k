/*
 * Created on Dec 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.globus.cog.gridshell.gui;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;

/**
 * 
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

public class VetoableTextAreaImpl extends JTextArea {
  class VetoDocumentFilter extends DocumentFilter {
    private VetoableChangeSupport vetoers = new VetoableChangeSupport(this);

    public void addVetoableChangeListener(VetoableChangeListener listener) {
      vetoers.addVetoableChangeListener(listener);
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener) {
      vetoers.removeVetoableChangeListener(listener);
    }

    public void insertString(FilterBypass fb, int offs, String str,
        AttributeSet a) throws BadLocationException {
      String text = fb.getDocument().getText(0, fb.getDocument().getLength());

      try {
        vetoers
            .fireVetoableChange(new PropertyChangeEvent(this, "value", text,
                text.substring(0, offs) + str
                    + text.substring(offs, text.length())));
        super.insertString(fb, offs, str, a);
      } catch (PropertyVetoException e) {
        Toolkit.getDefaultToolkit().beep();
      }
    }

    public void replace(FilterBypass fb, int offs, int length, String str,
        AttributeSet a) throws BadLocationException {
      String text = fb.getDocument().getText(0, fb.getDocument().getLength());
      try {
        vetoers
            .fireVetoableChange(new PropertyChangeEvent(this, "value", text,
                text.substring(0, offs) + str
                    + text.substring(offs, text.length())));
        super.replace(fb, offs, length, str, a);
      } catch (PropertyVetoException e) {
        Toolkit.getDefaultToolkit().beep();
      }
    }

    public void remove(FilterBypass fb, int offs, int length)
        throws BadLocationException {
      String text = fb.getDocument().getText(0, fb.getDocument().getLength());
      try {
        vetoers.fireVetoableChange(new PropertyChangeEvent(this, "value", text,
            text.substring(0, offs)
                + text.substring(offs + length, text.length())));
        super.remove(fb, offs, length);
      } catch (PropertyVetoException e) {
        Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  public VetoableTextAreaImpl() {
    setDocumentFilter(new VetoDocumentFilter());    
  }

  private DocumentFilter getDocumentFilter() {
    return ((AbstractDocument) this.getDocument()).getDocumentFilter();
  }

  private void setDocumentFilter(DocumentFilter filter) {
    ((AbstractDocument) this.getDocument()).setDocumentFilter(filter);
  }

  public void addVetoableChangeListener(VetoableChangeListener listener) {
    ((VetoDocumentFilter) getDocumentFilter())
        .addVetoableChangeListener(listener);
  }

  public void removeVetoableChangeListener(VetoableChangeListener listener) {
    ((VetoDocumentFilter) getDocumentFilter())
        .removeVetoableChangeListener(listener);

  }

  public static void createAndShowGUI() {
    JFrame frame = new JFrame();
    VetoableTextAreaImpl area = new VetoableTextAreaImpl();

    Action[] actions = area.getKeymap().getBoundActions();
    for (int i = 0; i < actions.length; i++) {
      System.out.println(actions[i]);
    }

    PromptFilterImpl filter = new PromptFilterImpl(area);
    area.addVetoableChangeListener(filter);
    area.setText(">> ");
    frame.getContentPane().add(area);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.show();
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }

}