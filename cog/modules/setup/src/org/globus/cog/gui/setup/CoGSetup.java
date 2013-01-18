// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup;

import java.io.File;
import java.io.Serializable;

import javax.swing.JOptionPane;

import org.globus.cog.gui.setup.util.BusyFeedback;
import org.globus.cog.gui.util.UITools;

/**
 * The main class for the setup. It creates and displays the setup frame
 */
public class CoGSetup extends Thread implements Serializable {
	private boolean done = false;
	private SetupFrame frame;

	/**
	 * Use this as a blocking method of running the setup wizard. It will return
	 * only after the wizard terminates.
	 */
	public void run() {
		done = false;
		if (checkGlobusDirectory()) {
			show();
			while (!done) {
				try {
					sleep(100);
				}
				catch (InterruptedException e) {

				}
			}
			destroy();
		}
	}

	/**
	 * Checks if the ~/.globus directory exists. If not, it creates one. Returns
	 * false if an error has occured
	 */
	private boolean checkGlobusDirectory() {
		File globusDir = new File(System.getProperty("user.home"), ".globus");
		if (globusDir.exists()) {
			if (!globusDir.isDirectory()) {
				JOptionPane.showMessageDialog(
						null,
						".globus is not a directory.\nA .globus directory is needed in your home directory by the Java CoG Kit.\nHowever this directory could not be created because a file with that name already exists.\nPlease either remove or rename the .globus file, then restart this wizard",
						"Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else {
				return true;
			}
		}
		else {
			globusDir.mkdir();
			return true;
		}
	}

	/**
	 * Shows/activates the setup window
	 */
	public void show() {
		frame = new SetupFrame(this);
		frame.pack();
		frame.setSize(720, 480);
		UITools.center(null, frame);
		BusyFeedback.initialize(frame);
		frame.setVisible(true);
	}

	public void destroy() {
		hide();
		frame.dispose();
	}

	/**
	 * Hides the setup wizard frame
	 */
	public void hide() {
		frame.setVisible(true);
	}

	public void frameClosed() {
		done = true;
		frame.dispose();
	}

	public static void main(String[] argv) {
		CoGSetup CS = new CoGSetup();
		CS.run();
		System.exit(0);
	}
}