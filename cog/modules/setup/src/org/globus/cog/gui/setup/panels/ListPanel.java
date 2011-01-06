
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.panels;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.globus.cog.gui.setup.components.SetupComponent;
import org.globus.cog.gui.setup.events.NavActionListener;
import org.globus.cog.gui.setup.events.NavEvent;

/**
 *  Contains the list of steps.
 */
public class ListPanel extends JPanel implements ActionListener {

	private JPanel panel;
	private List items;

	public ListPanel() {
		super();
		setBorder(BorderFactory.createEtchedBorder());

		items = new ArrayList();

		panel = new JPanel();
		panel.setLayout(new GridLayout(0, 1, 2, 2));

		JScrollPane sp = new JScrollPane(panel);

		sp.setBorder(BorderFactory.createEmptyBorder());
		//sp.setPreferredSize(new Dimension(154, 200));

		JLabel label = new JLabel("Progress:");

		label.setHorizontalAlignment(SwingConstants.LEFT);

		add(label);
		add(sp);
	}

	public void addComponent(SetupComponent SC) {
		JCheckBox CB = new JCheckBox(SC.getTitle());

		if (SC.getDependencies().size() != 0) {
			CB.setEnabled(false);
		}
		panel.add(CB);
	}

	public void setComponents(Iterator Components) {
		while (Components.hasNext()) {
			addComponent((SetupComponent) Components.next());
		}
	}

	public void setItemEnabled(String Title, boolean enabled) {
		Component[] Components = getComponents();

		for (int i = 0; i < Components.length; i++) {
			JCheckBox CB = (JCheckBox) Components[i];

			if (CB.getText().compareTo(Title) == 0) {
				CB.setEnabled(enabled);
				break;
			}
		}
	}

	public Container getContainer() {
		return panel;
	}

	public void addItem(JButton Item) {
		Item.addActionListener(this);
		items.add(Item);
		panel.add(Item);
	}

	public void actionPerformed(ActionEvent e) {
		int index = items.indexOf(e.getSource());

		if (index != -1) {
			fireNavEvent(new NavEvent(this, NavEvent.Jump, index));
		}
	}

	public void addNavEventListener(NavActionListener NAL) {
		listenerList.add(NavActionListener.class, NAL);
	}

	public void fireNavEvent(NavEvent e) {

		Object[] listeners = listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == NavActionListener.class) {
				((NavActionListener) listeners[i + 1]).navAction(e);
			}
		}

	}
}
