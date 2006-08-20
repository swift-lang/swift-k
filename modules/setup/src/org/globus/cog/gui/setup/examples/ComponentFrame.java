
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.examples;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.globus.cog.gui.setup.components.IPAddressComponent;
import org.globus.cog.gui.setup.components.SetupComponent;
import org.globus.cog.gui.setup.util.BusyFeedback;
import org.globus.common.CoGProperties;

public class ComponentFrame extends JFrame implements ActionListener {
	private SetupComponent comp;
	private CoGProperties props;
	private JButton close;

	public ComponentFrame() {
		super();
		//get the default properties
		//the IPAddressComponent needs to have some properties passed to its constructor
		props = CoGProperties.getDefault();
		comp = new IPAddressComponent(props);
		close = new JButton("Close");
		close.addActionListener(this);

		//initialize the busy notification mechanism
		//since it's a modal frame it needs a parent frame
		BusyFeedback.initialize(this);

		//set the title of this frame to be the title of the component
		setTitle(comp.getTitle());
		//set the layout and add the objects
		getContentPane().setLayout(new BorderLayout());
		//getVisualComponent() returns an Object. In this case we know it's a Swing component
		getContentPane().add((Component) comp.getVisualComponent(), BorderLayout.CENTER);
		getContentPane().add(close, BorderLayout.SOUTH);
		setSize(400, 300);
		show();

		//tell the component it's active now
		comp.enter();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == close) {
			//see if the component has any problems exiting
			if (comp.leave()) {
				System.out.println(props.getIPAddress());
				System.exit(0);
			}
		}
	}

	public static void main(String[] args) {
		new ComponentFrame();
	}
}