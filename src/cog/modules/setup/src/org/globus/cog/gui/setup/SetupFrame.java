
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup;

import java.awt.Dimension;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.globus.cog.gui.setup.events.NavActionListener;
import org.globus.cog.gui.setup.events.NavEvent;
import org.globus.cog.gui.setup.panels.AboutPanel;
import org.globus.cog.gui.setup.panels.ComponentPanel;
import org.globus.cog.gui.setup.panels.ListPanel;
import org.globus.cog.gui.setup.panels.LogoPanel;
import org.globus.cog.gui.setup.panels.NavPanel;
import org.globus.cog.gui.setup.panels.TitlePanel;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.SimpleGridLayout;
import org.globus.cog.util.ImageLoader;

/**
 *  Implements the main frame of the wizard
 */
public class SetupFrame extends JFrame implements NavActionListener {
	private ListPanel listPanel;
	private LogoPanel logoPanel;
	private AboutPanel aboutPanel;
	private TitlePanel titlePanel;
	private NavPanel navPanel;
	private ComponentPanel componentPanel;
	private CoGSetup cgs;

	/**
	 * Creates a new SetupFrame
	 * @param cgs the main setup controller
	 */
	public SetupFrame(CoGSetup cgs) {
		super();
		this.cgs = cgs;
		setTitle("Java CoG Kit setup wizard");
		ImageLoader il = new ImageLoader();
		setIconImage(il.loadImage("images/16x16/co/setup-gear.png").getImage());

		getContentPane().setLayout(new SimpleGridLayout(1, 1));

		GridContainer contentPane = new GridContainer(1, 2);
		contentPane.setPreferredSize(SimpleGridLayout.Expand, SimpleGridLayout.Expand);
		getContentPane().add(contentPane);

		GridContainer leftPanel = new GridContainer(3, 1);
		leftPanel.setPreferredSize(new Dimension(164, SimpleGridLayout.Expand));

		logoPanel = new LogoPanel();
		logoPanel.setPreferredSize(new Dimension(160, 40));
		listPanel = new ListPanel();
		listPanel.setPreferredSize(new Dimension(160, SimpleGridLayout.Expand));
		aboutPanel = new AboutPanel();
		aboutPanel.setPreferredSize(new Dimension(160, 50));

		leftPanel.add(logoPanel);
		leftPanel.add(listPanel);
		leftPanel.add(aboutPanel);

		contentPane.add(leftPanel);

		GridContainer centerPanel = new GridContainer(3, 1);
		centerPanel.setPreferredSize(
			new Dimension(SimpleGridLayout.Expand, SimpleGridLayout.Expand));

		titlePanel = new TitlePanel();
		titlePanel.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 40));
		navPanel = new NavPanel();
		navPanel.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 50));
		navPanel.addNavEventListener(this);
		componentPanel =
			new ComponentPanel(titlePanel.getLabel(), listPanel, navPanel);
		componentPanel.setPreferredSize(
			new Dimension(SimpleGridLayout.Expand, SimpleGridLayout.Expand));

		centerPanel.add(titlePanel);
		centerPanel.add(componentPanel);
		centerPanel.add(navPanel);

		contentPane.add(centerPanel);
	}

	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			quit();
		}
	}

	/**
	 * Terminate the application
	 */
	public void quit() {
		cgs.frameClosed();
	}

	public void navAction(NavEvent e) {
		if (e.getNavAction() == NavEvent.Cancel) {
			int ret =
				JOptionPane.showConfirmDialog(
					null,
					"The setup was not completed\nAre you sure you want to quit?",
					"Warning!",
					JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.YES_OPTION) {
				quit();
			}
		}
		if (e.getNavAction() == NavEvent.Finish) {
			if (componentPanel.getVisibleComponent().finish()) {
				quit();
			}
		}
	}
}
