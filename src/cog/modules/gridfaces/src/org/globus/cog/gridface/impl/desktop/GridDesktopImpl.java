//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop;
/*
* Main container for our desktop.
*/

//Local imports
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.gridface.impl.commands.EXECCommandImpl;
import org.globus.cog.gridface.impl.desktop.frames.GCMLogFrame;
import org.globus.cog.gridface.impl.desktop.icons.GridIconImpl;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessGCM;
import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;
import org.globus.cog.gridface.impl.desktop.util.GCMLoggerTable;
import org.globus.cog.gridface.impl.desktop.util.ObjectPair;
import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;
import org.globus.cog.gui.setup.CoGSetup;
import org.globus.common.CoGProperties;

public class GridDesktopImpl
	extends AbstractDesktop
	implements AccessGCM{

	public static final String sCOGSETUP = "CoG Setup";

	public static final String sLOADGCM = "Load GCM(Under Construction)";
	public static final String sSAVEGCM = "Save GCM";

	public static final String sLOG_GCM = "Grid Monitor";

	/** Central GCM per desktop */
	protected GridCommandManager gcm = null;

	/** Frame holding GCM Log Table */
	protected GCMLogFrame gcmLogFrame = null;

	public GridDesktopImpl(AbstractDesktopContainer dFrame, Dimension screenSize,boolean noproxy) {
		this(dFrame, null, screenSize,noproxy);
	}
	/**
	 * Main constructor for our Grid Deskop, sets up screen size
	 * and our central GCM
	 * @param gcm desktop GCM
	 * @param screenSize Size of desktop
	 */
	public GridDesktopImpl(final AbstractDesktopContainer dFrame, GridCommandManager gcm,
			Dimension screenSize, final boolean noproxy) {
		super(dFrame, screenSize);
		
		final AbstractAction performStartupChecksAction = new AbstractAction() {
			public void actionPerformed(ActionEvent aEvent) {
				performStartupChecks(dFrame,noproxy);
			}
		};
		
		dFrame.addWindowListener(new WindowListener() {
			
			// when the window opens do our checks
			public void windowOpened(WindowEvent wEvent) {
				performStartupChecksAction.actionPerformed(
						new ActionEvent(dFrame,1,"DesktopWindowOpen.performStartupChecks")
				);
			}
			
			// not using these, just to implement the interface
			public void windowActivated(WindowEvent arg0) {			}
			public void windowClosed(WindowEvent arg0) {			}
			public void windowClosing(WindowEvent arg0) {			}
			public void windowDeactivated(WindowEvent arg0) {		}
			public void windowDeiconified(WindowEvent arg0) {		}
			public void windowIconified(WindowEvent arg0) {			}
		});
		

		if (gcm == null) {
			try {
				this.setGCM(new GridCommandManagerImpl());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			this.gcm = gcm;
		}

		this.startGCMLogger();

	}
	/**
	 * Performs various checks for the user before starting the desktop
	 * @param dFrame
	 * @param noproxy
	 */
	public void performStartupChecks(final AbstractDesktopContainer dFrame,final boolean noproxy) {		
		// check for cog.properties, if we don't have it we should run cogsetup
		final String cogPropFile = DesktopUtilities.GLOBUS_HOME+File.separator+"cog.properties";		
		if (!new File(cogPropFile).exists()) {
			// if no ~/.globus make it
			File globusHome = new File(DesktopUtilities.GLOBUS_HOME);
			if(!globusHome.exists()) {
				logger.warn("No globs dir..making dir: "+globusHome.toString());
				globusHome.mkdirs();
			}
			// let the user know what is going on (the don't have cogproperties file
			JOptionPane
					.showMessageDialog(
							this,
							"You do not have a CoGProperties file at '"+cogPropFile+"'\nwhich may cause the CoG Desktop to work improperly.You should run the \nCoGKit Setup Wizzard and save the cog.properties file to the above mentioned location.",
							"CoGKit Setup Information",
							JOptionPane.INFORMATION_MESSAGE);
		
			// run the wizard for them
			DesktopUtilities.showCreateCoGSetup(dFrame,new AbstractAction() {
				// once we have finished the setup run check the GridProxyCheck
				public void actionPerformed(ActionEvent e) {
					// see if the saved cog.properties to the correct location
					if(!new File(cogPropFile).exists()) {
						JOptionPane
								.showMessageDialog(
										dFrame,
										"CoG Desktop does not work properly without the file '"+cogPropFile+"',\n which does not exist. Ensure that you save the cog.properties file to the above\nmentioned location.\n\nTo run CoGSetup again and correct the problem go to File->CoG Setup",
										"CoGKit Setup Information",
										JOptionPane.WARNING_MESSAGE);
					}
					// now check for a current gridproxy
					performGridProxyCheck(dFrame,noproxy);					
				}});
		}else {
			// if don't need to run CoGSetup then just call performGridProxyCheck
			performGridProxyCheck(dFrame,noproxy);
		}		
	}	
	
	public void performGridProxyCheck(AbstractDesktopContainer dFrame, boolean noproxy) {
      //	if we are checking proxy check for a proxy		
		if (!noproxy && !DesktopUtilities.checkGridProyInfo()) {			
				JOptionPane
						.showMessageDialog(
								this,
								"You do not have a current grid proxy certificate and this may cause the\nCoG Desktop to work improperly. This error may occur if your\ncertificate does not exist or is expired\nat '"+CoGProperties.getDefault().getProxyFile()+"'.\nYou should run Grid Proxy Init and ensure to\nuse 'Options' to specify the 'Proxy File'\nto '"+CoGProperties.getDefault().getProxyFile()+"'.",
								"CoGKit Setup Information",
								JOptionPane.INFORMATION_MESSAGE);
				DesktopUtilities.showCreateGridProxy(dFrame,true);
				//	 ensure the created the proxy
				if (!DesktopUtilities.checkGridProyInfo()) { 
					JOptionPane
					.showMessageDialog(
							this,
							"You still do not have a current grid proxy certificate and this may cause the\nCoG Desktop to work improperly.\n\nTo correct the problem run Grid Proxy Init again by going to Security->Create Proxy\nand ensure to use 'Options' to specify the 'Proxy File'\nto '"+CoGProperties.getDefault().getProxyFile()+"'.",
							"CoGKit Setup Information",
							JOptionPane.INFORMATION_MESSAGE);
				}
		}
	}
	

	public boolean loadGCM(){
		final File GCMsourceXMLFile = DesktopUtilities.getSourceFile(".",".xml","GCM from XML file");
		if(GCMsourceXMLFile !=null){
						try{
						//Needs to get figured out..TODO
						setGCM(new GridCommandManagerImpl(GCMsourceXMLFile));
						}catch(Exception ioe){
							ioe.printStackTrace();
							return false;
						}
					}
		return false;
	}
	public boolean saveGCM(){
		File GCMdestXML = DesktopUtilities.getDestinationFile(".",".xml","GCM to XML file");
		if(GCMdestXML !=null){
			try{
				getGCM().toXML(GCMdestXML.getCanonicalPath());
				return true;
			}catch(Exception ioe){
				ioe.printStackTrace();
			}
		}
		return false;
	}

	public GridCommandManager getGCM() {
		return this.gcm;
	}

	public void setGCM(GridCommandManager gcm) {
		this.gcm = gcm;
		this.gcm.setDesktop(this);
		//TODO 10/8/04 this needs to be checked heavily
		//I am still trying to figure out which is the best way
		//to update my GCM table after loading GCM from xml file
				Enumeration enumer = getGCM().getAllCommands();
			    for (; enumer.hasMoreElements(); ) {
			    	Task task = (Task)enumer.nextElement();
			    	if(task.getName().equalsIgnoreCase("exec")){
				    	GridCommand command = new EXECCommandImpl();
				    	command.setTask(task);
				    	command.addStatusListener(GridDesktopImpl.this);
				    	command.setStatus(task.getStatus());
			    	}

			    }

	    if(this.gcmLogFrame != null){
	    	this.gcmLogFrame.update();
	    }

	}
	//Desktop Logger operations
	public void startGCMLogger() {
		try {
			this.gcmLogFrame = new GCMLogFrame(new GCMLoggerTable(this));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public ObjectPair getGCMObjectPair() {
		ObjectPair classObjectPair = new ObjectPair();
		//Create a central GCM for all gridface components
		classObjectPair.put(GridCommandManager.class.getName(), gcm);
		return classObjectPair;
	}

	protected void configureMenuBar(JMenuBar desktopMenuBar) {
		super.configureMenuBar(desktopMenuBar);
		JMenu newMenu;
		JMenu newSubMenu;
		JMenuItem newMenuItem;

		newMenu = DesktopUtilities.getMenu("File",desktopMenuBar);
		newMenu.insert(new LoadGCMAction(),newMenu.getMenuComponentCount()-2);
		newMenu.insert(new SaveGCMAction(),newMenu.getMenuComponentCount()-2);
		newMenuItem = new JMenuItem(new AbstractAction(sCOGSETUP){
			public void actionPerformed(ActionEvent e){
				DesktopUtilities.showCoGSetup();
			}
		});
		newMenu.insert(newMenuItem,newMenu.getMenuComponentCount()-2);

		newMenu = DesktopUtilities.getMenu("View",desktopMenuBar);
		final JCheckBox chkGCM = new JCheckBox(sLOG_GCM,gcmLogFrame.isVisible());
		chkGCM.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				gcmLogFrame.setVisible(chkGCM.isSelected());
			}
		});
		newMenu.add(chkGCM,newMenu.getMenuComponentCount()-2);

		newMenu = new JMenu("Options");
		newSubMenu = new JMenu("Job History");
		newMenu.add(newSubMenu);
		final JCheckBox chkOverWrite = new JCheckBox("Append duplicate StdOut & StdError w/ Identity",false);
		chkGCM.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//TODO, 10/15/04 this might need some help from abstraction
			}
		});
		chkOverWrite.setEnabled(false);
		newSubMenu.add(chkOverWrite);

		newMenuItem = new JMenuItem("Mime types");
		newMenuItem.setEnabled(false);
		newMenu.add(newMenuItem);
		newMenuItem = new JMenuItem("Default Web-Browser");
		newMenuItem.setEnabled(false);
		newMenu.add(newMenuItem);


		newMenuItem = new JMenuItem("Tools");
		newMenuItem.setEnabled(false);
		newMenu.add(newMenuItem);
		desktopMenuBar.add(newMenu);

		newMenu = new JMenu("Security");
		newMenuItem = new JMenuItem(new AbstractAction("Create Proxy"){
			public void actionPerformed(ActionEvent e){
				DesktopUtilities.showCreateGridProxy(getDesktopFrame(),true);
			}
		});
		newMenu.add(newMenuItem);

		newMenuItem = new JMenuItem(new AbstractAction("Destroy Proxy"){
			public void actionPerformed(ActionEvent e){
				DesktopUtilities.showDestroyGridProxy(getDesktopFrame());
			}
		});
		newMenu.add(newMenuItem);

		newMenuItem = new JMenuItem(new AbstractAction("Proxy Info"){
			public void actionPerformed(ActionEvent e){
				DesktopUtilities.showGridProxyInfo(getDesktopFrame());
			}
		});
		newMenu.add(newMenuItem);

		desktopMenuBar.add(newMenu);

	}
	public void statusChanged(StatusEvent event) {
		this.gcmLogFrame.statusChanged(event);
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessIcons#getAvailableIconTypes()
	 */
	public Vector getAvailableIconTypes() {
		Vector availTypes = new Vector();
		availTypes.addElement(GridIconImpl.JOB_SUBMISSION);
		availTypes.addElement(GridIconImpl.JOB_SPECIFICATION);
		availTypes.addElement(GridIconImpl.SERVICE);
		availTypes.addAll(super.getAvailableIconTypes());

		return availTypes;
	}

	class LoadGCMAction extends AbstractAction	 {
		public LoadGCMAction() {
			super(sLOADGCM);
		}
		public void actionPerformed(ActionEvent e) {
			loadGCM();
		}
	}
	class SaveGCMAction extends AbstractAction	 {
		public SaveGCMAction() {
			super(sSAVEGCM);
		}
		public void actionPerformed(ActionEvent e) {
			saveGCM();
		}
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessClose#close()
	 */
	public boolean close() {
		int answer =
			DesktopUtilities.optionConfirmation(
				this,
				"Save GCM state to XML?",
				"Save GCM state?",
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			try {
				return saveGCM() && super.close();

			} catch (Exception exp) {
				exp.printStackTrace();
			}

		} else if(answer == JOptionPane.NO_OPTION){
			return super.close();
		}
		//dont close desktop if cancel is pressed
		return false;
	}

}
