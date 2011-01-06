
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 10, 2003
 */
package org.globus.cog.karajan.viewer;


import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.globus.cog.util.ImageLoader;

public class RestartAction extends FailureAction implements ChangeListener {
	private JPanel panel;

	private JSpinner spinner;

	private JRadioButton radio;

	private JLabel times;

	private int timesLeft;

	public RestartAction() {
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		radio = (JRadioButton) super.getComponent(null);
		panel.add(radio);
		times = new JLabel("; times: ");
		panel.add(times);
		spinner = new JSpinner(new SpinnerNumberModel(2, 1, 99, 1));
		panel.add(spinner);
		spinner.setEnabled(false);
		times.setEnabled(false);
		radio.addChangeListener(this);
		timesLeft = -1;
	}

	public void handleFailure(EventListener element,
		NotificationEvent event) {
		if (timesLeft == -1) {
			timesLeft = ((SpinnerNumberModel) spinner.getModel()).getNumber()
				.intValue();
		}
		if (timesLeft == 0) {
			EventBus.send(element, event);
		}
		else {
			((FlowNode) event.getFlowElement()).restartElement((FlowElement) element, event
				.getStack());
		}
		timesLeft--;
	}

	public String getName() {
		return "Restart";
	}

	public String getDescription() {
		return "Restarts the execution of this element";
	}

	public Icon getIcon() {
		return ImageLoader.loadIcon("images/16x16/co/arrow-reload.png");
	}

	public Component getComponent(ButtonGroup group) {
		super.getComponent(group);
		return panel;
	}

	public void stateChanged(ChangeEvent e) {
		spinner.setEnabled(radio.isSelected());
		times.setEnabled(radio.isSelected());
	}

	public boolean isComplete() {
		return timesLeft == 0;
	}

	public FailureAction newInstance() {
		RestartAction fa = (RestartAction) super.newInstance();
		fa.setTimesLeft(timesLeft = ((SpinnerNumberModel) spinner.getModel())
			.getNumber().intValue());
		return fa;
	}

	public int getTimesLeft() {
		return timesLeft;
	}

	public void setTimesLeft(int timesLeft) {
		this.timesLeft = timesLeft;
	}

}
