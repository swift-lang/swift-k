// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 10, 2003
 */
package org.globus.cog.karajan.viewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.globus.cog.gui.util.UITools;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.util.ImageLoader;

public class ActionDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 7944331308564007578L;
	
	private List actions;
	private ThreadedUID element;
	private FailureNotificationEvent event;
	private Hook hook;
	private FailureAction result;
	private JCheckBox applyToAll, applyToElement;
	private static Point lastLocation;
	private JButton ok;

	public ActionDialog(Hook hook, List actions, ThreadedUID element,
			FailureNotificationEvent event) {
		super();
		this.hook = hook;
		this.actions = actions;
		this.element = element;
		this.event = event;
		setTitle("Error");
		JPanel main = new JPanel(new BorderLayout());
		JPanel center = new JPanel(new BorderLayout());
		main.add(center, BorderLayout.CENTER);
		JLabel icon = new JLabel();
		icon.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		icon.setIcon(ImageLoader.loadIcon("images/32x32/co/stop.png"));
		center.add(icon, BorderLayout.WEST);
		JPanel textAndOptions = new JPanel(new BorderLayout());
		JPanel text = new JPanel();
		text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
		JLabeledPanel error = new JLabeledPanel("Error details:");
		error.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		text.add(error);
		JTextArea message = new JTextArea();
		message.setText(event.getMessage());
		message.setEditable(false);
		error.add(message);
		JLabeledPanel location = new JLabeledPanel("Location:");
		location.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		text.add(location);
		message = new JTextArea();
		message.setText(element.getElement().toString());
		message.setEditable(false);
		location.add(message);
		center.add(textAndOptions, BorderLayout.CENTER);
		textAndOptions.add(text, BorderLayout.NORTH);
		applyToAll = new JCheckBox("Apply to all errors of this type");
		applyToElement = new JCheckBox("Apply to all errors for this element");
		JLabeledPanel checks = new JLabeledPanel(new GridLayout(0, 1));
		checks.setLabel("Options: ");
		checks.add(applyToAll);
		checks.add(applyToElement);
		textAndOptions.add(checks, BorderLayout.SOUTH);

		getContentPane().add(main);
		JLabeledPanel options = new JLabeledPanel("Actions: ");
		options.setLayout(new BoxLayout(options.getContentPane(), BoxLayout.Y_AXIS));
		textAndOptions.add(options, BorderLayout.CENTER);
		ButtonGroup group = new ButtonGroup();
		Iterator i = actions.iterator();
		while (i.hasNext()) {
			FailureAction action = (FailureAction) i.next();
			JComponent c = (JComponent) action.getComponent(group);
			c.setAlignmentX(0);
			options.add(c);
		}
		JPanel buttons = new JPanel(new FlowLayout());
		main.add(buttons, BorderLayout.SOUTH);
		ok = new JButton("Ok");
		ok.setIcon(ImageLoader.loadIcon("images/16x16/co/button-ok.png"));
		ok.addActionListener(this);
		buttons.add(ok);
		if (lastLocation == null) {
			UITools.center(null, this);
		}
		else {
			setLocation(lastLocation);
		}
	}

	public FailureAction choice() {
		pack();
		show();
		while (result == null) {
			try {
				Thread.sleep(200);
			}
			catch (InterruptedException e) {
			}
		}
		lastLocation = getLocation();
		hide();
		dispose();
		return result;
	}

	public void close() {
		hide();
		dispose();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ok) {
			Iterator i = actions.iterator();
			while (i.hasNext()) {
				FailureAction fa = (FailureAction) i.next();
				if (fa.isSelected()) {
					if (applyToAll.isSelected()) {
						hook.addGlobalFilter(event.getMessage(), fa);
					}
					if (applyToElement.isSelected()) {
						hook.addElementFilter(element, fa);
					}
					result = fa;
				}
			}
		}
	}
}
