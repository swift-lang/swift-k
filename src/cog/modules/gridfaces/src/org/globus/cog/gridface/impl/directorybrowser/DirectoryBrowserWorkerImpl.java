
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.directorybrowser;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.file.GridFileImpl;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.interfaces.FileTransferObject;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;



public class DirectoryBrowserWorkerImpl implements StatusListener {
	private FileTransferObject fileTrans;
	
	private DefaultTreeModel model;
	private GridFileTreeNode top;

	private String pwd;
	private URI topURI;
	private String fileSeparator = File.separator;
	
	private GridCommandManager gcm;
	private DirectoryBrowserImpl dbgui;
	
	private Hashtable submittedCommands = new Hashtable();
	private GridCommand executingCommand;
	private Vector stoppedCommands = new Vector(5,1);
	
	static Logger logger = Logger.getLogger(DirectoryBrowserWorkerImpl.class.getName());
	
	//private DirectoryBrowserLoggerImpl logger = new DirectoryBrowserLoggerImpl();
	//private LoggerImpl logger = LoggerImpl.createInstance(DirectoryBrowserImpl.class.getName());
	//private int defaultLogLevel = LoggerImpl.DEBUG;
	
	public DirectoryBrowserWorkerImpl(DirectoryBrowserImpl dbgui, GridCommandManager gcm) {
		this.dbgui = dbgui;
		//Build the directory tree area
		top = new GridFileTreeNode(new GridFileImpl());
		model = new DefaultTreeModel(top, true);
		fileTrans = new FileTransferObjectImpl(this, gcm);
		//logger.setOutput(dbgui.getLogWindow());
	}
	
	/**
	 * Sets the file separator based on the protocol.  If this is local, use the local file
	 * separator, otherwise use a slash;
	 * @param protocol
	 */
	private void setFileSeparator(String protocol) {
		//if(protocol.equals("file"))
		//	fileSeparator = File.separator;
		//else
			fileSeparator = "/";
	}
	
	protected FileTransferObject getFileTransferObject() {
		return fileTrans;
	}
	
	/**
	 * Return the GridFile object of the selected item.
	 * @return
	 */
	protected GridFile getGridFileFromTreePath(TreePath path) {
		return (GridFile) ((GridFileTreeNode) path.getLastPathComponent()).getUserObject();
	}
	
	protected URI processURIFromTopPanel(URI uri) {
		URI returnURI = null;
		String returnString = null;
		try {
		    if(uri.getScheme()!=null) {
		        returnString = uri.getPath().substring(1);
		    }else {
		        returnString = "";
		    }
		} catch (StringIndexOutOfBoundsException e) {
			returnString = "";
		}
		
		if(returnString.equals(""))
			returnString = ".";
		
		try {
			returnURI = new URI(null, returnString, null);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		return returnURI;
	}
	
	protected URI processURIForTopPanel(URI uri) {
		URI returnURI = null;
		String authority = topURI.getAuthority();
		if(authority ==null)
			authority = "";
		try {
			returnURI = new URI(topURI.getScheme(), authority, fileSeparator + uri.normalize().getPath(), null, null);
		} catch (URISyntaxException e) {
			
		}
		return returnURI;
	}
	
	
	/**
	 * Given a selected item's <code>TreePath</code> construct and return its relative path as a string.
	 * @param treePath The <code>TreePath</code> of the selected item.
	 * @return The path of the item in string form
	 */
	protected URI getPathFromTreePath(TreePath treePath) {
		int count = treePath.getPathCount();
		String path = pwd;
		
		for(int i =1; i<count;i++) {
			path+=treePath.getPathComponent(i) + fileSeparator;
		}
		
		//If the last node is not a directory, remove the fileseparator
		if(!getGridFileFromTreePath(treePath).isDirectory()) {
			path = path.substring(0, (path.length()-1));
		}
		
		URI returnURI = null;
			
		try{
			returnURI = new URI(null, path, null);
		} catch (URISyntaxException e) {
		}
		
		//the URI must be normalized first to put in the proper number of fileseps...
		//return this.normalizeAbsoluteURI(returnURI);
		return returnURI.normalize();
	}
	
	
	/**
	 * Returns the uri of the treepath if the last node in the path is a directory.
	 * Otherwise the uri of the parent of the last node is returned.
	 * Only a directory uri is returned.
	 * @param treePath
	 * @return
	 */
	protected URI getDirectoryPathFromTreePath(TreePath treePath) {
		int count = treePath.getPathCount();
		String path = pwd;
		
		int i = 1;
		while(i<count && ((DefaultMutableTreeNode) treePath.getPathComponent(i)).getAllowsChildren()) {
			path+=treePath.getPathComponent(i) + fileSeparator;
			i++;
		}	
		URI returnURI = null;
		
		try{
			returnURI = new URI(null, path, null);
		} catch (URISyntaxException e) {
		}
		return returnURI.normalize();
	}
	
	
	/**
	 * Returns the string representation of the last item in the tree path.
	 * @param path
	 * @return
	 */
	protected String getStringOfTreePath(TreePath path) {
		return path.getLastPathComponent().toString();
	}
	
	
	/**
	 * Return the datamodel of the tree...used in JTree creation.
	 * @return data model for directory tree
	 */
	protected DefaultTreeModel getTreeDataModel() {
		return model;
	}
	
	
	/**
	 * Called when the user asks to delete an item.  
	 * @param path TreePath of item to delete.
	 */
	protected void deleteItem(TreePath path) {
		logger.info("Deleting " + this.getStringOfTreePath(path));
		GridFile tempFileInfo = this.getGridFileFromTreePath(path);
		GridCommand deleteCommand = null;
		if (tempFileInfo.isDirectory()) {
			deleteCommand = fileTrans.rmdir(this.getPathFromTreePath(path));
		} else {
			deleteCommand = fileTrans.rmfile(this.getPathFromTreePath(path));
		}
		this.setExecutingCommand(deleteCommand);
		this.addCommand(deleteCommand, path);
		try {
			fileTrans.execute(deleteCommand, true);
		} catch (Exception except) {
		}
	}
	
	
	protected void upDirectory() {
		top.removeAllChildren();
		model.reload(top);
		URI oldURI = this.getPathFromTreePath(new TreePath(top));
		//this takes the old uri, normalizes it and resolves the directory above and gets the path
		//normalizing it cleans out any ../ or ./
		URI newURI = oldURI.resolve(".." + fileSeparator);
		
		GridCommand chdirCommand = fileTrans.setCurrentDirectory(newURI);
		this.setExecutingCommand(chdirCommand);
		try {
			fileTrans.execute(chdirCommand, true);
		} catch (Exception e1) {
		}
		dbgui.setURI(this.processURIForTopPanel(newURI));
	}
	
	protected void makeDirectory(String newDirName) {
		this.makeDirectory(new TreePath(top), newDirName);
	}
	
	/**
	 * Called when the user asks to create a new directory.
	 * @param path TreePath of selection. (ie) where to put the directory.
	 * @param newDirName The name of the new directory
	 */
	protected void makeDirectory(TreePath path, String newDirName) {
		logger.info("Creating directory " + newDirName);
		
		URI newURI = null;
		URI nodeURI = null;
		TreePath hashPath = null; //helps us decide what node to refresh
		
		//if our selection allows kids (is a directory) create the directory under it.
		//else create the directory under the parent (which has to be a directory)
		if(((DefaultMutableTreeNode) path.getLastPathComponent()).getAllowsChildren()) {
			nodeURI = this.getPathFromTreePath(path);
			hashPath = path;
		} else {
			nodeURI = this.getPathFromTreePath(path.getParentPath());
			hashPath = path.getParentPath(); //refresh the parent node in this case
		}
		//create the newURI to make
		try {
			newURI = new URI(nodeURI.getScheme(), nodeURI.getAuthority(), nodeURI.getPath() + newDirName, null, null);
		} catch (URISyntaxException e) {
			return;
		}
		GridCommand mkdirCommand = fileTrans.makeDirectory(newURI);
		this.addCommand(mkdirCommand, hashPath);
		this.setExecutingCommand(mkdirCommand);
		try {
			fileTrans.execute(mkdirCommand, true);
		} catch (Exception except) {
		}
	}
	
	
	/**
	 * Called when the user asks to 'go into' a directory rather than just expanding it.
	 * @param path TreePath of selected item.
	 */
	protected void goInto(TreePath path) {
		logger.info("Going into " + this.getStringOfTreePath(path));
		model.setRoot((GridFileTreeNode) path.getLastPathComponent());
		top = (GridFileTreeNode) path.getLastPathComponent();
		URI pathURI = getPathFromTreePath(path);
		GridCommand chdirCommand = fileTrans.setCurrentDirectory(pathURI);
		this.setExecutingCommand(chdirCommand);
		dbgui.setURI(this.processURIForTopPanel(pathURI));
		try {
			fileTrans.execute(chdirCommand, true);
		} catch (Exception e) {
		}
	}
	
	
	protected void refresh() {
		this.refresh(new TreePath(top));
	}
	
	/**
	 * Called when the user asks to refresh an already open folder.
	 * @param path TreePath of selected item.
	 */
	protected void refresh(TreePath path) {
		logger.info("Refreshing " + this.getStringOfTreePath(path));
		treeWillExpand(path);
	}
	
	/**
	 * A tree is expanded so all data for the corresponding directory is fetched.
	 * @param path TreePath of the node being expanded.
	 */
	protected void treeWillExpand(TreePath path) {
		int count = path.getPathCount();
		
		GridCommand lsCommand;
		
		//just perform a regular ls if the top is expanding
		if(path.getLastPathComponent()==top) {
			logger.info("Listing contents of present working directory");
			lsCommand = fileTrans.ls();
		
		} else {
			logger.info("Listing contents of " + this.getStringOfTreePath(path));
			//if not expanding the top, get the path of the selected item and get its contents
			URI lsURI = getPathFromTreePath(path);
			lsCommand = fileTrans.ls(lsURI);
		}
		
		this.addCommand(lsCommand, path.getLastPathComponent());
		this.setExecutingCommand(lsCommand);
		
		try {
			fileTrans.execute(lsCommand, true);
		} catch (Exception except) {
		}
	}
	
	/**
	 * When a tree is collapsed all the subnodes are cleared since we're refetching them anyway.
	 * This keeps the user from briefly seeing the old nodes.
	 * @param path TreePath of the node being collapsed.
	 */
	protected void treeWillCollapse(TreePath path) {
		GridFileTreeNode node = (GridFileTreeNode) path.getLastPathComponent();
		node.removeAllChildren();
		model.reload(node);
	}
	

	
	protected void setUsername(String username) {
		logger.debug("setUsername("+username+")");
		fileTrans.setUsername(username);
	}
	
	
	protected void setPassword(String password) {	
		fileTrans.setPassword(password);
	}
	
	
	/**
	 * The method performed when the go button is pushed.  This sets up the first
	 * directory, making it visible if its the first location that has been loaded.
	 *
	 */
	protected void goButtonPushed() {
		logger.info("Connecting....");
		topURI = dbgui.getURI();
		
		if(topURI==null){
			dbgui.errorOpen();
			return;
		}
		
		String uriScheme = topURI.getScheme();
		String uriHost = topURI.getHost();
		if (uriScheme == null || "".equals(uriScheme)) {
			uriScheme = "gridftp";
		}
		if (uriHost == null || "".equals(uriScheme)) {
			uriHost = dbgui.getURI().toString();
		}
		
		logger.debug("provider="+uriScheme);
		logger.debug("host="+uriHost);
		
		//set the file separator to the local machine separator or the remote machine separator
		this.setFileSeparator(uriScheme);
		
		if(!(uriScheme.equals("gridftp")||uriScheme.equals("gsiftp")||uriScheme.equals("file"))){
		    dbgui.setEnabled(false);
			dbgui.createUsernameDialog();
			dbgui.setEnabled(true);
		}
		
		try {
		    logger.info("setting execute");
			setExecutingCommand(fileTrans.connect(uriScheme, uriHost, String.valueOf(topURI.getPort())));
			logger.info("done setting execute");
		} catch (Exception e) { 
		    logger.debug("caught exception",e);
		}
		
	}
	
	
	/**
	 * Disconnect when the button is pushed.  Called by an actionlistener.
	 * Also clear out all the old stuff in the window.
	 */
	protected void disconnectButtonPushed() {
		logger.info("Disconnecting...");
		top.removeAllChildren();
		model.reload(top);
		fileTrans.disConnect();
	}
	
	
	protected void stopButtonPushed() {
		try{
			//stoppedCommands.add(executingCommand.getIdentity());
			stoppedCommands.add(executingCommand.getId().toString());
		} catch(NullPointerException e) {
			
		}
	}
	/**
	 * Creates a tree based on an ls performed somewhere.  
	 * @param uri where to perform the ls
	 * @param parent the node on which to add the new children
	 */
	private void createTree(Collection dir, DefaultMutableTreeNode parent) {
		GridFileImpl fileInfo;
		parent.removeAllChildren();
		Iterator iDir = dir.iterator();
		while (iDir.hasNext()) {
			fileInfo = (GridFileImpl) iDir.next();
			parent.add(new GridFileTreeNode(fileInfo));
			if(fileInfo.isDirectory()) {
				//a directory
				((DefaultMutableTreeNode) parent.getLastChild()).setAllowsChildren(true);
			} else if(fileInfo.isSoftLink()) {
				//a softlink
				((DefaultMutableTreeNode) parent.getLastChild()).setAllowsChildren(false);
				String name = ((GridFile)((DefaultMutableTreeNode) parent.getLastChild()).getUserObject()).getName();
				
				//split the  name into the actual name and the thing its linking to
				String[] parts = name.split(" -> ");
				((GridFile)((DefaultMutableTreeNode) parent.getLastChild()).getUserObject()).setName(parts[0]);
				
				URI isDirURI = getPathFromTreePath(new TreePath(parent.getPath()));
			
				//resolve the uri with the second part and normalize it for the grid
				GridCommand isDirCommand = fileTrans.isDirectory(isDirURI.resolve(parts[1]));
				
				addCommand(isDirCommand, parent.getLastChild());
				try {
					fileTrans.execute(isDirCommand, true);
				} catch (Exception e1) {
				}
				
			} else {
				//not a directory or a softlink
				((DefaultMutableTreeNode) parent.getLastChild()).setAllowsChildren(false);
			}
		}
		model.reload(parent);
	}
	
	
	
	protected void addCommand(GridCommand command, Object obj) {
		//submittedCommands.put(command.getIdentity().toString(), obj);
		submittedCommands.put(command.getId().toString(), obj);
	}
	
	private Object getCommand(GridCommand command) {
		//return submittedCommands.get(command.getIdentity().toString());
		return submittedCommands.get(command.getId().toString());
	}
	
	private void removeCommand(GridCommand command) {
		//submittedCommands.remove(command.getIdentity().toString());
		submittedCommands.remove(command.getId().toString());
	}
	
	/**
	 * This sets the currently executing command so that users can cancel it if they wish 
	 * with the stop button.  
	 * @param command
	 */
	private void setExecutingCommand(GridCommand command) {
		executingCommand = command;
	}
	
	private boolean hasCommandBeenStopped(GridCommand command) {
		//return stoppedCommands.contains(command.getIdentity());
		return stoppedCommands.contains(command.getId().toString());
	}
	
	private void removeStoppedCommand(GridCommand command) {
		//stoppedCommands.remove(command.getIdentity());
		stoppedCommands.remove(command.getId().toString());
	}
	
	/**
	 * This is the status changed listener that is called whenever a command completes, 
	 * fails, etc.  Much of the work is done here, but beware, this does not always
	 * run in the event queue thread.
	 */
	public void statusChanged(StatusEvent event) {
	    
		Status status = event.getStatus();
		GridCommand command = (GridCommand) event.getSource();
		
		logger.debug("stats="+status.getStatusString()+", type="+command.getClass());
		
		//What to do if things complete successfully.
		if (status.getStatusCode() == Status.COMPLETED) {
			//System.out.println(command.getCommand() + " Command Completed");
			if(this.hasCommandBeenStopped(command)) {
				this.removeStoppedCommand(command);
				return;
			}
			
			if (command.getCommand().equals(FileOperationSpecification.START)) {
				//open returns sessionid for future reference
				fileTrans.setSessionId((Identity) command.getOutput());
				//set the current directory to what the user has entered
				GridCommand chdirCommand = fileTrans.setCurrentDirectory(this.processURIFromTopPanel(topURI));
				addCommand(chdirCommand, "connect");
				try {
					fileTrans.execute(chdirCommand, true);
				} catch (Exception e) {
				    logger.debug("caught",e);
				}
				
			} else if (command.getCommand().equals(FileOperationSpecification.CD)) {
				String isConnect = (String) getCommand(command);
				
				
				//get and save the current directory 
				//this is required to get the _absolute_ path of the current dir 
				//so we can construct URI's in the future.
				GridCommand pwdCommand = fileTrans.getCurrentDirectory();
				
				//are we doing this in the connect sequence? if so, pass on that information to
				//the next step in the sequence, getCurrentDirectory
				if(isConnect !=null && isConnect.equals("connect")) {
					addCommand(pwdCommand, "connect");
					removeCommand(command);
				} 
				
				try {
					fileTrans.execute(pwdCommand, true);
				} catch (Exception e) {
				}
				
			} else if (command.getCommand().equals(FileOperationSpecification.PWD)) {
				final String isConnect = (String) getCommand(command);
				//this is important but we probably do it only if we're first connecting
				
				
				pwd = ((String) command.getOutput());
				if(!pwd.endsWith(fileSeparator))
					pwd += fileSeparator;
				
				//the following should only occur if first connecting or 'going into'
				GridFile topFileInfo = new GridFileImpl();
				topFileInfo.setName(topURI.getPath());
				topFileInfo.setFileType(GridFile.DIRECTORY);
				top.setUserObject(topFileInfo);
				
				treeWillExpand(new TreePath(top.getPath()));
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						boolean isInConnectSequence = false;
						if(isConnect !=null && isConnect.equals("connect")) {
							isInConnectSequence = true;
						} 
						dbgui.getCurrentDirectoryCompleted(isInConnectSequence);
					}
				});
				
			} else if (command.getCommand().equals(FileOperationSpecification.LS)) {
				final Collection lsEnum = (Collection) command.getOutput();
				final GridFileTreeNode parentNode = (GridFileTreeNode) getCommand(command);
				removeCommand(command);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						createTree(lsEnum, parentNode);
						dbgui.listingCompleted();
					}
				});
			} else if (command.getCommand().equals(FileOperationSpecification.MKDIR)) {
				final TreePath path = (TreePath) getCommand(command);
				removeCommand(command);
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						treeWillExpand(path);
					}
				});
			
			} else if (command.getCommand().equals(FileOperationSpecification.RMFILE)) {
				final TreePath path = (TreePath) getCommand(command);
				removeCommand(command);
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						treeWillExpand(path.getParentPath());
					}
				});
			
			} else if (command.getCommand().equals(FileOperationSpecification.RMDIR)) {
				final TreePath path = (TreePath) getCommand(command);
				removeCommand(command);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						treeWillExpand(path.getParentPath());
					}
				});
			} else if (command.getCommand().equals(FileOperationSpecification.PUTFILE) || command.getCommand().equals(FileOperationSpecification.GETFILE) 
					||command.getCommand().equals("urlcopy")||command.getCommand().equals(FileOperationSpecification.PUTDIR)
					||command.getCommand().equals(FileOperationSpecification.GETDIR)) {
				
				final TreePath path = (TreePath) getCommand(command);
				
				
				if(path!=null) {
					removeCommand(command);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							treeWillExpand(path);
							dbgui.fileTransferCompleted();
						}
					});
				}
			} else if (command.getCommand().equals(FileOperationSpecification.STOP)) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dbgui.disconnectCompleted();
					}
				});
			} else if (command.getCommand().equals(FileOperationSpecification.ISDIRECTORY)){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) getCommand(command);
				final DefaultMutableTreeNode finalnode = (DefaultMutableTreeNode) getCommand(command);
				//this means the thing we're linking to is a directory
				if(((Boolean)command.getOutput()).booleanValue()) {
					node.setAllowsChildren(true);
				}
				removeCommand(command);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						model.reload(finalnode);
					}
				});
				
			}
			
		//What to do if things fail:	
		} else if (status.getStatusCode() == Status.FAILED) {
			logger.error("A command failed.");
			if(command.getCommand().equals(FileOperationSpecification.START)) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						logger.error("Could not connect!");
						dbgui.errorOpen();
					}});
			} else if(command.getCommand().equals(FileOperationSpecification.LS)) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						logger.error("Could not list directory!");
						dbgui.errorList();
					}
				});
				//error in setcurrentdirectory, let the gui know if we're in the connect sequence
				//or not
			} else if(command.getCommand().equals(FileOperationSpecification.CD)) {
				final String isConnect = (String) getCommand(command);
				removeCommand(command);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						boolean isInConnectSequence = false;
						if(isConnect !=null && isConnect.equals("connect")) {
							isInConnectSequence = true;
						} 
						logger.error("Could not set current directory!");
						dbgui.errorSetCurrentDirectory(isInConnectSequence);
					}
				});
				//error in getcurrentdirectory, let the gui know if we're in the connect sequence
				//or not
			} else if(command.getCommand().equals(FileOperationSpecification.PWD)) {
				final String isConnect = (String) getCommand(command);
				removeCommand(command);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						boolean isInConnectSequence = false;
						if(isConnect !=null && isConnect.equals("connect")) {
							isInConnectSequence = true;
						} 
						logger.error("Could not get the name of the current directory!");
						dbgui.errorGetCurrentDirectory(isInConnectSequence);
					}
				});
			} else if(command.getCommand().equals(FileOperationSpecification.RMFILE) || command.getCommand().equals(FileOperationSpecification.RMDIR)) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dbgui.errorDelete();
					}
				});
			}
		}
	}
}
