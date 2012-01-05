
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.util;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.globus.cog.gui.util.UITools;

public class BusyFeedback extends Thread implements ActionListener {
	private static BusyFeedback bsThread;
	private static Callback callback;
	private Dialog dialog;
	private JLabel label;
	private JPanel panel;
	private JButton cancel;
	private boolean visible;
	private boolean finished;
	private Frame parent;

	public BusyFeedback(Frame parent) {
		this.parent = parent;
		callback = null;
		dialog = new Dialog(parent, true);
		dialog.setLayout(new BorderLayout());
		label = new JLabel("");
		panel = new JPanel();
		panel.add(label);
		panel.setBorder(BorderFactory.createRaisedBevelBorder());
		dialog.add(panel, BorderLayout.CENTER);
		cancel = new JButton("Cancel");
		dialog.add(cancel, BorderLayout.SOUTH);
		cancel.addActionListener(this);
		finished = false;
	}

	public static void initialize(Frame parent) {
		/* The whole thread thing is needed because show() will
		* block for a modal dialog
		*/
		bsThread = new BusyFeedback(parent);
		bsThread.start();
	}

	public static void setCallback(Callback cb) {
		callback = cb;
	}

	public static void removeCallback() {
		callback = null;
	}

	public void run() {
		while (!finished) {
			if ((visible) && (!dialog.isVisible())) {
				dialog.pack();
				UITools.center(parent, dialog);
				dialog.show();
			}
			try {
				sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		if ((visible == false) && (dialog.isVisible())) {
			dialog.hide();
		}
	}

	public static void show() {
		bsThread.setVisible(true);
	}

	public static void hide() {
		bsThread.setVisible(false);
	}

	public void setText(String message) {
		label.setText(message);
	}

	public static void setMessage(String message) {
		bsThread.setText(message);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancel) {
			if (callback != null) {
				callback.callback(this, null);
			}
		}
	}
}
