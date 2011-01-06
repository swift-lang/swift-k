
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.directorybrowser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import org.globus.cog.abstraction.impl.common.AbstractionProperties;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.gridface.impl.GridFaceImpl;
import org.globus.cog.gridface.impl.URIInputPanelImpl;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessClose;
import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.impl.mimehandler.MimeHandler;
import org.globus.cog.gridface.impl.util.RemoteToTempFile;
import org.globus.cog.gridface.interfaces.DirectoryBrowser;
import org.globus.cog.gridface.interfaces.FileTransferObject;
import org.globus.cog.gridface.interfaces.GridCommandManager;
import org.globus.cog.gridface.interfaces.GridFace;
import org.globus.cog.util.ImageLoader;

public class DirectoryBrowserImpl extends JPanel implements DirectoryBrowser, AccessClose, Serializable {
	Logger logger = Logger.getLogger(DirectoryBrowserImpl.class);
	
	//utility declarations
	private GridCommandManager gcm;
	private GridFace gridface = new GridFaceImpl();
	private DirectoryBrowserWorkerImpl dbworker;
	private MimeHandler mimehandler;
	private ImageLoader imageLoader = new ImageLoader();
	private ButtonListener buttonListener;
	//top panel declarations
	private JPanel topPanel;
	private URIInputPanelImpl topURIPanel;
	private JButton disconnectButton;
	private JButton goButton;
	
	//middle declarations (all these in scrollpane)
	protected JTree tree;
	private JPopupMenu rightClickPopup;
	
	//bottom declarations (all these in a bottom panel)
	private JProgressBar progress = new JProgressBar();
	private JLabel progressInfo = new JLabel(" ");
	private JButton stopButton;
	
	//popupmenu declarations
	private JMenuItem newDirectory; 
	private JMenuItem deleteEntry; 
	private JMenuItem getInfo; 
	private JMenuItem goInto; 
	private JMenuItem refresh; 
	
	private boolean connected;
	
	//logger declarations
	//JTextArea logArea = new JTextArea();
	
	public DirectoryBrowserImpl() throws Exception {
		this(new GridCommandManagerImpl());
	}
	
	public DirectoryBrowserImpl(GridCommandManager gcm) {
		super(new GridBagLayout());
		
		this.gcm = gcm;
		
		GridBagConstraints c = new GridBagConstraints();
		
		dbworker = new DirectoryBrowserWorkerImpl(this, gcm);
		mimehandler = new MimeHandler(gcm);
		
		//create all the action listener classes
		buttonListener = new ButtonListener();
		
		//Build the top panel
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new Insets(2,2,2,2);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		topPanel = this.createTopPanel();
		this.add(topPanel, c);
		
		//build the icon panel
		c.gridy = 1;
		this.add(this.createIconPanel(), c);
		
		//build the tree scroll window
		c.gridy=2;
		c.weightx = 2.0;
		c.weighty = 2.0;
		c.fill = GridBagConstraints.BOTH;
		this.add(this.createTreePanel(), c);
		
		//build the bottom panel
		c.gridy = 3;
		c.weightx =0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(this.createBottomPanel(), c) ;
		
		//Build the popup menu
		rightClickPopup = this.createPopupMenu();
		
		//JDialog outputDialog = new JDialog((JFrame)null, "Directory Browser Log");
		//outputDialog.getContentPane().add(logArea);
		//outputDialog.setVisible(true);
	}
	
	//protected JTextArea getLogWindow() {
	//	return logArea;
	//}
	
	/**
	 * Execute the commands to notify the user that we are waiting for something.
	 *
	 */
	protected void startWaiting() {
		this.startWaiting(" ", false);
	}
	
	/** 
	 * Execute the commands to notify the user that we are waiting for something and
	 * display a message above the progress bar.
	 * @param message String to display above the message bar.
	 */
	protected void startWaiting(String message, boolean canStop){
		progress.setIndeterminate(true);
		progressInfo.setText(message);
		if(canStop) {
			stopButton.setEnabled(true);
		}
	}
	
	/**
	 * Execute the commands to notify the user that we are done waiting.
	 *
	 */
	protected void stopWaiting() {
		progress.setIndeterminate(false);
		progressInfo.setText(" ");
		stopButton.setEnabled(false);
	}
	
	/**
	 * Notify the user of something but do not indicate any waiting.  Probably for a background
	 * process.
	 * @param message
	 */
	protected void notifyUser(String message) {
		progressInfo.setText(message);
	}
	
	protected DirectoryBrowserWorkerImpl getWorker() {
		return dbworker;
	}
	
///Methods that return information about the currently selected item///	
	/**
	 * Get the <code>TreePath</code> of the selected item.
	 * @return
	 */
	protected TreePath getSelectedItemsTreePath() {
		return tree.getSelectionPath();
	}
	
	/**
	 * Return the FileInfo object of the selected item.
	 * @return
	 */
	protected GridFile getSelectedItemsGridFile() {
		try{
			return (GridFile) ((GridFileTreeNode) this.getSelectedItemsTreePath().getLastPathComponent()).getUserObject();
		} catch(NullPointerException e) {
			return null;
		}
	}
	
	/**
	 * Return the selected items uri
	 * @return
	 */
	protected URI getSelectedItemsURI() {
		return dbworker.processURIForTopPanel(dbworker.getPathFromTreePath(getSelectedItemsTreePath()));
	}
	
	/**
	 * If the selected item is a directory, return its uri, otherwise return the uri 
	 * of its parent.
	 * @return
	 */
	protected URI getSelectedItemsDirURI() {
		return dbworker.processURIForTopPanel(dbworker.getDirectoryPathFromTreePath(getSelectedItemsTreePath()));
	}

	/**
	 * Return the filetransferobject used by this directorybrowser.  This is for the
	 * copy commander
	 * @return
	 */
	protected FileTransferObject getFileTransferObject() {
		return dbworker.getFileTransferObject();
	}
	
///Methods that create panels///
	/**
	 * Create and return a JPanel with the icons that contain the icons to be placed at the top of the panel.
	 * @return
	 */
	private JPanel createIconPanel() {
		JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,0));
		//These are loaded from the cog-resources jar file.
		JButton upDirectoryButton = new JButton(imageLoader.loadImage("images/16x16/co/arrow-up.png"));//should be arrow-up
		JButton reloadButton = new JButton(imageLoader.loadImage("images/16x16/co/arrow-reload.png"));
		JButton newDirectoryButton = new JButton(imageLoader.loadImage("images/16x16/co/window-new.png"));
		JButton getInfoButton = new JButton(imageLoader.loadImage("images/16x16/co/window-about.png"));
		
		upDirectoryButton.addActionListener(buttonListener);
	    reloadButton.addActionListener(buttonListener);
	    newDirectoryButton.addActionListener(buttonListener);
	    getInfoButton.addActionListener(buttonListener);
	    
		upDirectoryButton.setToolTipText("Up a Directory");
		reloadButton.setToolTipText("Refresh");
		newDirectoryButton.setToolTipText("New Directory");
		getInfoButton.setToolTipText("Get Info");
		
	    upDirectoryButton.setBorder(BorderFactory.createEmptyBorder(2,2,0,0));
	    reloadButton.setBorder(BorderFactory.createEmptyBorder(2,2,0,0));
	    newDirectoryButton.setBorder(BorderFactory.createEmptyBorder(2,2,0,0));
	    getInfoButton.setBorder(BorderFactory.createEmptyBorder(2,2,0,0));
		
		iconPanel.add(upDirectoryButton);
		iconPanel.add(reloadButton);
		iconPanel.add(newDirectoryButton);
		iconPanel.add(getInfoButton);
		
		return iconPanel;
	}
	
	
	private JPanel createTopPanel() {
		JPanel topPanel = new JPanel();
		
		TopKeyListener topKeyListener = new TopKeyListener();
		topURIPanel = new URIInputPanelImpl();
		topURIPanel.addKeyListener(topKeyListener);
		
		topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,0));
		goButton = new JButton(imageLoader.loadImage("images/16x16/co/button-ok.png"));
		goButton.addActionListener(buttonListener);
		goButton.setToolTipText("Connect");
		goButton.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		
		disconnectButton = new JButton(imageLoader.loadImage("images/16x16/co/button-cancel.png"));
		disconnectButton.addActionListener(buttonListener);
		disconnectButton.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		disconnectButton.setToolTipText("Disconnect");
		disconnectButton.setEnabled(false);
		
		topPanel.add(topURIPanel);
		topPanel.add(goButton);
		topPanel.add(disconnectButton);
		return topPanel;
	}
	
	private JScrollPane createTreePanel() {
		TreeListener treeListener = new TreeListener();
		TreeMouseListener treeMouseListener = new TreeMouseListener();
		//Build the directory tree area
		tree = new JTree(dbworker.getTreeDataModel());
		tree.addTreeWillExpandListener(treeListener);
		tree.addMouseListener(treeMouseListener);
		///tree.addMouseMotionListener(treeMouseListener);
		tree.setRootVisible(false);
		
		//Drag and drop stuff
		tree.setDragEnabled(true);
		tree.setTransferHandler(new DirectoryBrowserTransferHandler(this));
		tree.setDropTarget(new DropTarget(tree,new DirectoryBrowserDropTargetListener(this)));
		return new JScrollPane(tree);
	}
	
	private JPanel createBottomPanel() {
		JPanel bottomPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth=2;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.LINE_START;
		bottomPanel.add(progressInfo, c);
		c.weightx=0;
		c.weighty=0;
		c.gridwidth=1;
		c.gridy=1;
		c.fill = GridBagConstraints.NONE;
		bottomPanel.add(progress, c);
		stopButton = new JButton(imageLoader.loadImage("images/16x16/co/button-cancel.png"));
		//stopButton.addActionListener(buttonListener);
		stopButton.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		stopButton.setToolTipText("Stop");
		stopButton.setEnabled(false);
		stopButton.addActionListener(buttonListener);
		c.gridx = 1;
		bottomPanel.add(stopButton,c);
		
		return bottomPanel;
	}
	
	private JPopupMenu createPopupMenu() {
		PopupListener popupListener = new PopupListener();
		rightClickPopup = new JPopupMenu();
		newDirectory = new JMenuItem("New Directory");
		deleteEntry = new JMenuItem("Delete Item");
		getInfo = new JMenuItem("Get Info");
		goInto = new JMenuItem("Go Into");
		refresh = new JMenuItem("Refresh");
		
		rightClickPopup.add(newDirectory);
		rightClickPopup.add(deleteEntry);
		rightClickPopup.add(getInfo);
		rightClickPopup.addSeparator();
		rightClickPopup.add(goInto);
		rightClickPopup.add(refresh);
		
		newDirectory.addActionListener(popupListener);
		deleteEntry.addActionListener(popupListener);
		getInfo.addActionListener(popupListener);
		goInto.addActionListener(popupListener);
		refresh.addActionListener(popupListener);
		return rightClickPopup;
	}
	
	
	
///Methods that create dialogs///
	/** Create the dialog to be popped up asking the user for their username 
	 * and password if they are not connecting to a gridftp server.
	 *
	 */
	protected void createUsernameDialog() {
		final JDialog usernameDialog = new JDialog((JFrame) null, "Username & Password", true);
		Container content = usernameDialog.getContentPane();
		content.setLayout(new BorderLayout());
		
		// contains the labels
		JPanel west = new JPanel(new GridLayout(0,1));
		west.add(new JLabel("Username:"));
		west.add(new JLabel("Password"));
		
		// contains the username and password fields and goes in content.CENTER
		JPanel middle = new JPanel(new GridLayout(0,1));
		final JTextField usernameField = new JTextField(20);
		final JPasswordField passwordField = new JPasswordField(20);		
		
		middle.add(usernameField);
		middle.add(passwordField);
		
		// contains anonymousPanel and buttonPanel and goes in content.SOUTH
		JPanel controls = new JPanel(new GridLayout(1,0));
		
		JPanel anonymousPanel = new JPanel(new GridLayout(1,0));
		final JCheckBox isAnonymous = new JCheckBox("Anonymous");
		isAnonymous.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent aEvent) {
				if(isAnonymous.isSelected()) {
					usernameField.setText("anonymous");
				}
			}
		});
		anonymousPanel.add(isAnonymous);		
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,0));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent aEvent) {
				usernameDialog.dispose();
			}
		});
		JButton okButton = new JButton("Ok");		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dbworker.setUsername(usernameField.getText());
				dbworker.setPassword(new String(passwordField.getPassword()));
				usernameDialog.dispose();
			}
		});
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		
		// add to controls panel
		controls.add(anonymousPanel);
		controls.add(buttonPanel);
		
		// add to content
		content.add(west,BorderLayout.WEST);
		content.add(middle,BorderLayout.CENTER);
		content.add(controls,BorderLayout.SOUTH);
		
		usernameDialog.pack();
		usernameDialog.setVisible(true);
	}
	
	/**
	 * create the confirmation dialog for deleting an object.
	 * @param itemToDelete String representation of item to delete.
	 * @return boolean whether or not to delete the item
	 */
	private boolean createDeleteDialog(String itemToDelete) {
		Object[] choices = {"Delete", "Do not delete"};
		int finalChoice = JOptionPane.showOptionDialog(this, "Are you sure you want to delete " + 
				itemToDelete +"?\nThis operation cannot be undone!", "Delete?", 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, choices, choices[1]);
		if(finalChoice == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * create the dialog that asks the user the name of the directory to create.
	 * @param parentsURI 
	 * @return
	 */
	private String createNewDirectoryDialog() {
		return (String) JOptionPane.showInputDialog(this,
	            "What is the name of the new directory?", "New Directory", JOptionPane.QUESTION_MESSAGE);
	}
	
	/**
	 * Create the Get Info dialog that pops up after a user clicks on an item and selects "Get Info"
	 * @param gridFile The <code>FileInfo</code> object of the selected item.
	 */
	private void createGetInfoDialog(GridFile gridFile) {
		if(gridFile==null)
			return;
		
		String kindString = "";
		if(gridFile.isDirectory()) {
			kindString = "Directory";
		} else if(gridFile.isFile()) {
			kindString = "File";
		} else if(gridFile.isSoftLink()) {
			kindString = "Link";
		} else if (gridFile.isDevice()) {
			kindString = "Device";
		}
		
		JLabel kind = new JLabel("<html><b>Kind:</b> " + kindString + "<html");
		JLabel size = new JLabel("<html><b>Size:</b> " + gridFile.getSize() + " bytes<html>");
		JLabel modified = new JLabel("<html><b>Modified:</b> " + gridFile.getLastModified()+"<html>");
		
		String userPermissionsString ="";
		String groupPermissionsString = "";
		String worldPermissionsString = "";
		if(gridFile.userCanRead()) {
			userPermissionsString = "Read";
		}
		if(gridFile.userCanWrite()) {
			if(userPermissionsString!="") userPermissionsString += ", ";
			userPermissionsString += "Write";
		}
		if(gridFile.userCanExecute()) {
			if(userPermissionsString!="") userPermissionsString += ", ";
			userPermissionsString += "Execute";
		}
		
		if(gridFile.groupCanRead()) {
			groupPermissionsString = "Read";
		}
		if(gridFile.groupCanWrite()) {
			if(groupPermissionsString!="") groupPermissionsString += ", ";
			groupPermissionsString += "Write";
		}
		if(gridFile.groupCanExecute()) {
			if(groupPermissionsString!="") groupPermissionsString += ", ";
			groupPermissionsString += "Execute";
		}
		
		if(gridFile.allCanRead()) {
			worldPermissionsString = "Read";
		}
		if(gridFile.allCanWrite()) {
			if(worldPermissionsString!="") worldPermissionsString += ", ";
			worldPermissionsString += "Write";
		}
		if(gridFile.allCanExecute()) {
			if(worldPermissionsString!="") worldPermissionsString += ", ";
			worldPermissionsString += "Execute";
		}
		
		JLabel userPermissions = new JLabel("<html><b>User Permissions:</b> " + userPermissionsString + "</html>");
		JLabel groupPermissions = new JLabel("<html><b>Group Permissions:</b> " + groupPermissionsString + "</html>");
		JLabel worldPermissions = new JLabel("<html><b>Others Permissions:</b> " + worldPermissionsString + "</html>");
		
		JDialog getInfoDialog = new JDialog();
		JPanel getInfoDialogPanel = new JPanel(new GridLayout(6,1));
		((GridLayout) getInfoDialogPanel.getLayout()).setVgap(5);
		getInfoDialog.setContentPane(getInfoDialogPanel);
		
		getInfoDialogPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Information about " + gridFile.getName()));
		getInfoDialogPanel.add(kind);
		getInfoDialogPanel.add(size);
		getInfoDialogPanel.add(modified);
		getInfoDialogPanel.add(userPermissions);
		getInfoDialogPanel.add(groupPermissions);
		getInfoDialogPanel.add(worldPermissions);
		
		getInfoDialog.setTitle(gridFile.getName() + " Info");
		getInfoDialog.pack();
		getInfoDialog.setVisible(true);
	}
	
///Methods called when operations are completed///
	
	protected void listingCompleted() {
		this.stopWaiting();
	}
	
	protected void disconnectCompleted() {
		stopWaiting();
		disconnectButton.setEnabled(false);
		goButton.setEnabled(true);
		topURIPanel.setEnabled(true);
		connected = false;
	}
	
	protected void getCurrentDirectoryCompleted(boolean isInConnectSequence) {
		if(isInConnectSequence) {
			topURIPanel.setEnabled(true);
			goButton.setEnabled(false);
			disconnectButton.setEnabled(true);
			connected = true;
		}
	}
	
	protected void fileTransferCompleted() {
		this.notifyUser("Transfer Complete!");
	}
	

	
///Methods called when errors occur///
	protected void errorOpen() {
		this.connectFailed("Connection failed.\nCould not open connection with server.");
	}
	
	protected void errorSetCurrentDirectory(boolean isInConnectSequence) {
		if(isInConnectSequence) {
			//close the connection and let the user know what happened
			dbworker.disconnectButtonPushed();
			this.connectFailed("Connection failed.\nThe specified directory may not exist.");
		} else {
		JOptionPane.showMessageDialog(this, "Could not set current directory.",
                "Error",
                JOptionPane.WARNING_MESSAGE);
		disconnectButton.setEnabled(true);
		this.stopWaiting();
		}
		
	}
	
	protected void errorGetCurrentDirectory(boolean isInConnectSequence) {
		if(isInConnectSequence) {
			//close the connection and let the user know what happened
			dbworker.disconnectButtonPushed();
			this.connectFailed("Connection failed.\nCould not get the current directory.");
		} else {
		JOptionPane.showMessageDialog(this, "Could not get current directory.",
                "Error",
                JOptionPane.WARNING_MESSAGE);
		disconnectButton.setEnabled(true);
		this.stopWaiting();
		}
	}
	
	protected void errorList() {
		JOptionPane.showMessageDialog(this, "Could not open directory.",
                "Error",
                JOptionPane.WARNING_MESSAGE);
		this.stopWaiting();
	}
	
	protected void errorMimeType() {
		JOptionPane.showMessageDialog(this, "No class associated with that type.",
                "Error",
                JOptionPane.WARNING_MESSAGE);
		this.stopWaiting();
	}
	
	protected void errorDelete() {
		JOptionPane.showMessageDialog(this, "Could not delete item.",
                "Error",
                JOptionPane.WARNING_MESSAGE);
		this.stopWaiting();
	}
	
	/**
	 * What to do when one of the methods called during connecting fails.
	 * 
	 * @param message More detailed information as to the nature of the failure.
	 */
	private void connectFailed(String message) {
		JOptionPane.showMessageDialog(this, message,
                "Could not connect",
                JOptionPane.WARNING_MESSAGE);
		this.stopWaiting();
		disconnectButton.setEnabled(false);
		goButton.setEnabled(true);
		topURIPanel.setEnabled(true);
		connected = false;
	}
	
	/**
	 * Return the <code>Dimension</code> of the top panel and the extra pixels on the side
	 * so the window only gets as small as the top panel.
	 * 
	 * @return Dimension <code>Dimension</code> of the top panel.
	 */
	  public Dimension getMinimumSize() {
	  	Dimension returnDimension = new Dimension();
	  	returnDimension.setSize(topPanel.getPreferredSize().getWidth()+4, topPanel.getPreferredSize().getHeight()+4);
	  	return returnDimension;
	  }
	
	public void setURI(URI uri) {
		topURIPanel.set(uri);
	}

	public URI getURI() {
		topURIPanel.get();
		return topURIPanel.getURI();
	}

	public URI getSelectedURI() {
		return this.getSelectedItemsURI();
	}
	
	public URI getSelectedURIDir() {
		return this.getSelectedItemsDirURI();
	}
	
	public void setSelected(String filename) {

	}

	public String getSelected() {
		return getSelectedItemsGridFile().getName();
	}

	public void update() {
		gridface.update();
	}

	public Date lastUpdateTime() {
		return gridface.lastUpdateTime();
	}

	public void setName(String name) {
		gridface.setName(name);
	}

	public void setLabel(String label) {
		gridface.setLabel(label);
	}

	public void register(GridFace connection) {
		gridface.register(connection);
	}
	
	public boolean close() {
		if(connected) {
			dbworker.disconnectButtonPushed();
		}
		return true;
	}
	
	private void showPopupMenu(Component component, int x, int y) {
		if(!(((GridFileTreeNode) this.getSelectedItemsTreePath().getLastPathComponent()).getAllowsChildren())){
			goInto.setEnabled(false);
			refresh.setEnabled(false);
		} else {
			goInto.setEnabled(true);
			refresh.setEnabled(true);
		}
		rightClickPopup.show(component, x, y);
	}
	
	protected class TreeMouseListener implements MouseListener {
		
		public void mouseClicked(MouseEvent mEvent) {
			
			int mouseRow = tree.getRowForLocation(mEvent.getX(), mEvent.getY());
			if (mouseRow != -1){
				//If we have clicked a real row, select it, and continue with our logic
				tree.setSelectionRow(mouseRow);
				//if a single right click, popup the menu
				if(mEvent.getButton() != MouseEvent.BUTTON1) {
					showPopupMenu(tree, mEvent.getX(), mEvent.getY());
				//if a double click:	
					
				} else if ((mEvent.getButton()==MouseEvent.BUTTON1) && (mEvent.getClickCount()==2)) {
					TreePath treePath = tree.getPathForRow(mouseRow);
					GridFileTreeNode selectedNode = (GridFileTreeNode) treePath.getLastPathComponent();
					if(selectedNode.getAllowsChildren()){
						//If here, user has double clicked on a directory
					} else {
//						JDialog newDialog = new JDialog();
						URI lookupURI = null;
						try {
							String port = String.valueOf(topURIPanel.getURI().getPort());
							String host = topURIPanel.getURI().getHost();
							
							if(port != null & !port.equals("-1")) {
								port = ":"+port;
							}else {
								port = "";
							}
							
							if(host == null) {
								host = "";
							}
							
							
							lookupURI = new URI(topURIPanel.getURI().getScheme()+"://"+host+port+ dbworker.getPathFromTreePath(getSelectedItemsTreePath()) );
							
							logger.debug("lookupURI="+lookupURI);
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String uriPath = lookupURI.getPath();
//						JPanel newPanel = (JPanel) mimehandler.lookup(dbworker.processURIForTopPanel(lookupURI));
//						if(newPanel==null) {
//							errorMimeType();
//						} else {
//							newDialog.getContentPane().add(newPanel);
//							newDialog.pack();
//							newDialog.setVisible(true);
//						}
						
						logger.debug("uriPath="+uriPath);
						int index = uriPath.lastIndexOf(".")+1;
						String extension =  "";
						if(index >0) {
							extension = uriPath.substring(index);
						}
						logger.debug("extension="+extension);
						try {  
						  // have a provider
						  if(isProvider(lookupURI)) {
						  	final File tempFile = File.createTempFile("CoGTempFile","."+extension);
						  	
						  	// define the start program action
						  	Action startProgram = new AbstractAction() {
							  	public void actionPerformed(ActionEvent aEvent) {
							  		startProgram(tempFile.toURI());
							  	}
							  };
							  
						  	// transfer the file, then start program
						  	RemoteToTempFile.remoteToTemp(gcm,lookupURI.toString(),
						  			tempFile.toURI().toString(),startProgram,null,null);	  	
						  	  	
						  }else {
						  	startProgram(lookupURI);
						  }
						}catch(Exception exception) {
							 logger.error("exception ",exception);
							 startProgram(lookupURI);
						}
					}
				}
			}
		}
		/**
		 * Should be in the mime handler
		 * @param lookupURI
		 * @return
		 */
		public boolean isProvider(URI lookupURI) {
		  	String provider = lookupURI.getScheme();
		  	
		  	if(provider == null) {
		  		return false;
		  	}
		  	
		  	Iterator iProviders = AbstractionProperties.getProviders().iterator();
		  	while(iProviders.hasNext()) {
		  		Object item = iProviders.next();
		  		if(provider.equals(item)) {
		  			return true;
		  		}
		  	}
		  	
		  	return false;
		  }

		/**
		 * Should be in the mime handler
		 * @param lookupURI
		 */
		  public void startProgram(URI lookupURI) {
		  	JDialog newDialog = new JDialog();
		  	JPanel newPanel = (JPanel) mimehandler.lookup(lookupURI);
		  	if(newPanel==null) {
		  		errorMimeType();
		  	} else {
		  		newDialog.getContentPane().add(newPanel);
		  		newDialog.setSize(500,500);  	
		  		newDialog.setVisible(true);
		  		newDialog.toFront();
		  		newDialog.setTitle("View: "+lookupURI);
		  	}  	
		  }

		public void mousePressed(MouseEvent event) {
		}

		public void mouseReleased(MouseEvent event) {
		}
		
		public void mouseEntered(MouseEvent event) {
		}

		public void mouseExited(MouseEvent event) {
		}
	}
	
	protected class TreeListener implements TreeWillExpandListener {
		public void treeWillExpand(TreeExpansionEvent tee) {
			startWaiting("Getting directory listing...", true);
			dbworker.treeWillExpand(tee.getPath());
		}
		
		public void treeWillCollapse(TreeExpansionEvent tee) {
			dbworker.treeWillCollapse(tee.getPath());
		}
		
	}
	
	protected class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			String label = ((JButton) event.getSource()).getToolTipText();
			if(label.equals("Connect")) {
				startWaiting("Connecting...", true);
				topURIPanel.setEnabled(false);
				goButton.setEnabled(false);
				disconnectButton.setEnabled(false);
				dbworker.goButtonPushed();
			} else if (label.equals("Disconnect")) {
				startWaiting("Disconnecting...", false);
				dbworker.disconnectButtonPushed();
			} else if (label.equals("Up a Directory")) {
				if(connected) {
					startWaiting("Going up a directory...", true);
					dbworker.upDirectory();
				}
			} else if (label.equals("Refresh")) {
				if(connected) {
					startWaiting("Refreshing...", true);
					dbworker.refresh();
				}
			} else if (label.equals("New Directory")) {
				if(connected) {
					dbworker.makeDirectory(createNewDirectoryDialog());
					startWaiting("Creating new directory...", false);
				}
			} else if (label.equals("Get Info")) {
				if(connected) {
					createGetInfoDialog(getSelectedItemsGridFile());
				}
			} else if (label.equals("Stop")) {
				stopWaiting();
				topURIPanel.setEnabled(true);
				if(connected){
					goButton.setEnabled(false);
					disconnectButton.setEnabled(true);
				} else {
					goButton.setEnabled(true);
					disconnectButton.setEnabled(false);
				}
				dbworker.stopButtonPushed();
				
			}
			
		}
	}
	
	protected class PopupListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JMenuItem item = (JMenuItem)event.getSource();
			if(item == getInfo){
				createGetInfoDialog(getSelectedItemsGridFile());
			} else if(item == newDirectory) {
				dbworker.makeDirectory(getSelectedItemsTreePath(), createNewDirectoryDialog());
				startWaiting("Creating new directory...", false);
			} else if(item == deleteEntry) {
				if(createDeleteDialog(getSelectedItemsGridFile().getName()))
					startWaiting("Deleting item...", false);
					dbworker.deleteItem(getSelectedItemsTreePath());
			} else if(item == goInto) {
				startWaiting("Going into directory...", true);
				dbworker.goInto(getSelectedItemsTreePath());
			} else if(item == refresh) {
				startWaiting("Refreshing...", true);
				dbworker.refresh(getSelectedItemsTreePath());
			}
		}
	}

protected class TopKeyListener implements KeyListener {
	public void keyTyped(KeyEvent arg0) {
	}

	public void keyPressed(KeyEvent keyevent) {
		if(keyevent.getKeyCode()==KeyEvent.VK_ENTER){
			if(!connected){
				startWaiting("Connecting...", true);
				topURIPanel.setEnabled(false);
				goButton.setEnabled(false);
				disconnectButton.setEnabled(false);
				dbworker.goButtonPushed();
			}
		}
	}

	public void keyReleased(KeyEvent arg0) {
	}	
}




}

