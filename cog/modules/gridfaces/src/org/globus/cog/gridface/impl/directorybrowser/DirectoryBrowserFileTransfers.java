
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.directorybrowser;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.globus.cog.gridface.impl.desktop.interfaces.AccessClose;
import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.interfaces.GridCommandManager;


public class DirectoryBrowserFileTransfers extends JPanel implements AccessClose{

	private DirectoryBrowserImpl leftPane;
	private DirectoryBrowserImpl rightPane;
	private ButtonListener buttonListener = new ButtonListener();
	
	public DirectoryBrowserFileTransfers() throws Exception {
		this(new GridCommandManagerImpl());
	}
	
	public DirectoryBrowserFileTransfers(GridCommandManager gcm)  {
		super(new GridBagLayout());
		try {
			leftPane = new DirectoryBrowserImpl(gcm);
			rightPane = new DirectoryBrowserImpl(gcm);
		} catch (Exception e) {
		}
		
		
		JButton rightArrow = new JButton("-->");
		rightArrow.addActionListener(buttonListener);
		JButton leftArrow = new JButton("<--");
		leftArrow.addActionListener(buttonListener);
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 2;
		this.add(leftPane, c);
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight=1;
		c.anchor = GridBagConstraints.PAGE_END;
		this.add(rightArrow, c);
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridy = 1;
		this.add(leftArrow, c);
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 2;
		c.gridy = 0;
		c.gridheight =2;
		this.add(rightPane, c);
		
	}
	
	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			String label = ((JButton)event.getSource()).getText();
			if(label.equals("-->")) {
				DirectoryBrowserTransferable t = new DirectoryBrowserTransferable(leftPane.getSelectedItemsURI(), 
						leftPane.getSelected(), leftPane.getFileTransferObject().getSessionId(), 
						leftPane.getSelectedItemsGridFile().isDirectory());
				rightPane.tree.getTransferHandler().importData(leftPane.tree, t);
			} else if (label.equals("<--")) {
				DirectoryBrowserTransferable t = new DirectoryBrowserTransferable(rightPane.getSelectedItemsURI(), 
						rightPane.getSelected(), rightPane.getFileTransferObject().getSessionId(), 
						rightPane.getSelectedItemsGridFile().isDirectory());
				leftPane.tree.getTransferHandler().importData(rightPane.tree, t);
			}
			
		}
		
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessClose#close()
	 */
	public boolean close() {
		// TODO Auto-generated method stub
		return false;
	}
}
