package org.globus.cog.gridshell.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

/**
 * Prevents the prompt from being tampered with
 * 
 * 
 */
public class PromptFilterImpl implements VetoableChangeListener {
	// the text component that is to be restricted
	private JTextComponent textComponent;

	// the prompt value
	private String prompt;

	// Actions that will be used to override key bindings
	public Action beginLineAction, selectionBeginLineAction, backwardAction,
			selectionBackwardAction, selectAllAction, previousWordAction,
			selectionPreviousWordAction;

	// keys for each key binding to be overriden
	public String beginLine = "BEGIN_LINE",
			selectionBeginLine = "SELECTION_BEGIN_LINE", backward = "BACKWARD",
			selectionBackward = "SELECTION_BACKWARD", selectAll = "SELECT_ALL",
			previousWord = "PREVIOUS_WORD",
			selectionPreviousWord = "SELECTION_PREVIOUS_WORD";

	public PromptFilterImpl(JTextComponent tComponent) {
		this.textComponent = tComponent;

		initActions();
	}

	private void initActions() {
		// overrides home key
		beginLineAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				int promptLastIndex = (prompt != null) ? prompt.length() : 0;
				int lastTextComponentIndex = textComponent.getDocument()
						.getLength();
				textComponent.setCaretPosition(Math.min(promptLastIndex,
						lastTextComponentIndex));
			}
		};
		this.textComponent.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), this.beginLine);
		this.textComponent.getActionMap().put(beginLine, beginLineAction);

		// overrides left arrow
		backwardAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				int promptLastIndex = (prompt != null) ? Math.max(prompt
						.length() - 1, 0) : 0;
				int lastTextComponentIndex = textComponent.getDocument()
						.getLength();
				int currentIndex = textComponent.getCaretPosition();

				if (currentIndex - 1 > promptLastIndex && currentIndex - 1 > 0) {
					textComponent.getActionMap().get(
							DefaultEditorKit.backwardAction).actionPerformed(e);
				} else {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		};
		this.textComponent.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), backward);
		this.textComponent.getActionMap().put(backward, backwardAction);

		// overrides shift+home
		selectionBeginLineAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				int promptLastIndex = (prompt != null) ? prompt.length() : 0;
				int lastTextComponentIndex = textComponent.getDocument()
						.getLength();
				textComponent.moveCaretPosition(Math.min(promptLastIndex,
						lastTextComponentIndex));
			}
		};
		this.textComponent.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_MASK),
				this.selectionBeginLine);
		this.textComponent.getActionMap().put(this.selectionBeginLine,
				this.selectionBeginLineAction);

		// overrides ctrl+a
		selectAllAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				beginLineAction.actionPerformed(e);
				textComponent.getActionMap().get(
						DefaultEditorKit.selectionEndAction).actionPerformed(e);
			}
		};
		this.textComponent.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK),
				this.selectAll);
		this.textComponent.getActionMap().put(this.selectAll,
				this.selectAllAction);

		// overrides shift+left
		selectionBackwardAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				int promptLastIndex = (prompt != null) ? Math.max(prompt
						.length() - 1, 0) : 0;
				int lastTextComponentIndex = textComponent.getDocument()
						.getLength();
				int currentIndex = textComponent.getCaretPosition();

				if (currentIndex - 1 > promptLastIndex && currentIndex - 1 > 0) {
					textComponent.getActionMap().get(
							DefaultEditorKit.selectionBackwardAction)
							.actionPerformed(e);
				} else {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		};
		this.textComponent.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_MASK),
				this.selectionBackward);
		this.textComponent.getActionMap().put(this.selectionBackward,
				this.selectionBackwardAction);

		// ctrl+left
		previousWordAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				int promptLastIndex = (prompt != null) ? Math.max(prompt
						.length() - 1, 0) : 0;
				int lastTextComponentIndex = textComponent.getDocument()
						.getLength();
				int currentIndex = textComponent.getCaretPosition();

				if (currentIndex - 1 > promptLastIndex && currentIndex - 1 > 0) {
					textComponent.getActionMap().get(
							DefaultEditorKit.previousWordAction)
							.actionPerformed(e);
				} else {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		};
		this.textComponent.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_MASK),
				this.previousWord);
		this.textComponent.getActionMap().put(this.previousWord,
				this.previousWordAction);

		// shift+ctrl+left
		selectionPreviousWordAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				int promptLastIndex = (prompt != null) ? Math.max(prompt
						.length() - 1, 0) : 0;
				int lastTextComponentIndex = textComponent.getDocument()
						.getLength();
				int currentIndex = textComponent.getCaretPosition();

				if (currentIndex - 1 > promptLastIndex && currentIndex - 1 > 0) {
					textComponent.getActionMap().get(
							DefaultEditorKit.selectionPreviousWordAction)
							.actionPerformed(e);
				} else {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		};
		this.textComponent.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_MASK
						| KeyEvent.SHIFT_MASK), this.selectionPreviousWord);
		this.textComponent.getActionMap().put(this.selectionPreviousWord,
				this.selectionPreviousWordAction);
	}

	/**
	 * Sets the prompt value to prevent from editing
	 * 
	 * @param nPrompt -
	 *            the new prompt value
	 */
	public void setPrompt(String nPrompt) {
		prompt = nPrompt;
	}

	/**
	 * Gets the prompt value
	 * 
	 * @return
	 */
	public String getPrompt() {
		return prompt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.VetoableChangeListener#vetoableChange(java.beans.PropertyChangeEvent)
	 */
	public void vetoableChange(PropertyChangeEvent pEvent)
			throws PropertyVetoException {
		String newValue = pEvent.getNewValue().toString();

		if (prompt == null || newValue.startsWith(prompt)) {
			// it is acceptable
		} else {
			// do not allow the change
			throw new PropertyVetoException(newValue, pEvent);
		}
	}
}