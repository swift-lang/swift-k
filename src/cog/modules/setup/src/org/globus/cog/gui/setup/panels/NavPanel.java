
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.panels;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.globus.cog.gui.setup.events.NavActionListener;
import org.globus.cog.gui.setup.events.NavEvent;
import org.globus.cog.gui.setup.util.HSpacer;

/**
 *  Contains the navigation buttons
 */
public class NavPanel extends JPanel implements ActionListener {

	private JButton cancel, prev, next, finish;

	public NavPanel() {
		super();

		setLayout(new FlowLayout());
		setBorder(BorderFactory.createEtchedBorder());

		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		cancel.setAlignmentX(1);
		add(cancel);

		add(new HSpacer(10));

		prev = new JButton("<Previous");
		prev.addActionListener(this);
		add(prev);

		next = new JButton("Next>");
		next.addActionListener(this);
		add(next);

		add(new HSpacer(10));

		finish = new JButton("Finish");
		finish.addActionListener(this);
		finish.setAlignmentX(1);
		add(finish);
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

	public void actionPerformed(ActionEvent e) {
		if (e.getID() == ActionEvent.ACTION_PERFORMED) {
			if (e.getSource() == cancel) {
				fireNavEvent(new NavEvent(this, NavEvent.Cancel));
			}
			else if (e.getSource() == next) {
				fireNavEvent(new NavEvent(this, NavEvent.Next));
			}
			else if (e.getSource() == prev) {
				fireNavEvent(new NavEvent(this, NavEvent.Prev));
			}
			else if (e.getSource() == finish) {
				fireNavEvent(new NavEvent(this, NavEvent.Finish));
			}
		}
	}

	public void setNextEnabled(boolean enabled) {
		next.setEnabled(enabled);
	}

	public void setPrevEnabled(boolean enabled) {
		prev.setEnabled(enabled);
	}

	public void setFinishEnabled(boolean enabled) {
		finish.setEnabled(enabled);
	}
}
