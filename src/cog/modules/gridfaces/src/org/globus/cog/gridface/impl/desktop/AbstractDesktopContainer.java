//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
/*
 * Created on Aug 6, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.globus.cog.gridface.impl.desktop;

//Local imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.globus.cog.gridface.impl.desktop.interfaces.AccessClose;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessDesktop;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessPreferences;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessSaveChanges;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessToolBar;
import org.globus.cog.gridface.impl.desktop.interfaces.CoGTop;
import org.globus.cog.gridface.impl.desktop.toolbar.DesktopToolBarImpl;
import org.globus.cog.gridface.impl.desktop.util.DesktopProperties;
import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;
import org.globus.cog.gridface.interfaces.Desktop;
import org.globus.cog.util.ArgumentParser;


public abstract class AbstractDesktopContainer extends JFrame
	implements AccessDesktop, AccessToolBar,AccessSaveChanges,AccessClose,AccessPreferences,
	NodeChangeListener {
    
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbstractDesktopContainer.class.getName());
    
	public static final String sTITLEDELIMETER = "::";
	public static final String sTITLESUFFIX = " - Java CoG Kit";
	
	/** Flag to ask questions before exiting frame */
	protected static boolean saveChanges = true;
	
	/** Scroll Pane to hold desktop */
	protected JScrollPane scroll = new JScrollPane();
	/** Current visible desktop */
	protected CoGTop currentDesktop = null;
	/** Desktop Toolbar */
	protected DesktopToolBarImpl desktopToolBar = null;
	/** Default offset location for this frame on screen */
	protected static int inset = 50;
	
	/** Preferences file name path */
	protected String prefFile = "";
	
	/**Argument parser for desktop*/
	protected static ArgumentParser argParser = null;

	//************DEFAULT CONSTRUCTOR*************
	public AbstractDesktopContainer(String title) {
		configureFrame(title);
	}

	public AbstractDesktopContainer(File xmlFile){
		if(!xmlFile.exists()){
		    logger.fatal("XML file "+xmlFile.getAbsolutePath()+" does not exist",new Exception());
			System.exit(1);
		}
		fromXML(xmlFile);
	}
	//************ABSTRACTIONS*************
	protected abstract AbstractDesktop configureDesktop();
	protected abstract void configureDesktopIcons(AbstractDesktop desktop);
	protected abstract void configureToolBar(DesktopToolBarImpl desktopToolBar);
	protected abstract void configureStatusPanel(JPanel statusPanel);

	//	************STATIC METHODS*************
	protected static void configureArgumentParser(ArgumentParser ap){
	    argParser = ap;
	    
		ap.setExecutableName("cog-desktop");
		
        ap.addOption("file", "XML file containing Desktop Icon State. NOTE: Will override any other desktop state file arguments", "file",
        ArgumentParser.OPTIONAL);
        ap.addAlias("file", "f");
        
        ap.addFlag("no-save","No confirmation before frame exit");
        ap.addAlias("no-save","ns");
        
        ap.addFlag("empty-state","By pass loading default desktop file specified in .globus/desktop.properties");
        ap.addAlias("empty-state","es");
        
        ap.addOption("maxwidth","Maximum width for desktop","integer",ArgumentParser.OPTIONAL);
        ap.addAlias("maxwidth","mxw");
        
        ap.addOption("maxheight","Maximum height for desktop","integer",ArgumentParser.OPTIONAL);
        ap.addAlias("maxheight","mxh");
        
        ap.addFlag("help", "Display usage");
        ap.addAlias("help", "h");
        
        
	}
	//************COMMON IMPLEMENTATIONS*************
	
	public boolean isSaveChanges() {
		return saveChanges;
	}
	public void setSaveChanges(boolean saveChange) {
		saveChanges = saveChange;
	}
	protected final Rectangle getDefaultFrameSize(){
		return new Rectangle(inset, inset, getDefaultDesktopSize().width, getDefaultDesktopSize().height);
	}
	protected final Dimension getDefaultDesktopSize(){
		Dimension dSize =  Toolkit.getDefaultToolkit().getScreenSize();

		dSize=getDefaultMaxFrameSize().intersection(new Rectangle(dSize)).getSize();
		dSize.width -= inset * 2;
		dSize.height -= inset * 2;
		
		logger.debug("Default Desktop Size: "+dSize);
		
		return dSize;
	}

	protected final Rectangle getDefaultMaxFrameSize(){
	    int maxwidth;
	    int maxheight;
	    Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
	    
	    if(argParser.isPresent("maxwidth")){
	        maxwidth = argParser.getStringValue("maxwidth")==null?
	                screen.width:Integer.parseInt(argParser.getStringValue("maxwidth"));
	    }else{
	        maxwidth =Integer.parseInt(DesktopProperties.getDefault().getProperty("maxwidth",
			        new Integer(screen.width).toString()));
	    }
	    
	    if(argParser.isPresent("maxheight")){
	        maxheight = argParser.getStringValue("maxheight")==null?
	                screen.height:Integer.parseInt(argParser.getStringValue("maxheight"));
	    }else{
	        maxheight = Integer.parseInt(DesktopProperties.getDefault().getProperty("maxheight",
			        new Integer(screen.height).toString()));
	    }
		
	    return new Rectangle(new Dimension(maxwidth,maxheight));
	}
	protected final void configureFrame(){
		configureFrame(Desktop.sNEWDESKTOP);
	}
	protected final void configureFrame(String title){
		configureFrame(title,getDefaultFrameSize());
	}
	protected final void configureFrame(String title,Rectangle frameSize){
		this.setFrameTitle(title);
		
		this.setBounds(frameSize.intersection(getDefaultMaxFrameSize()));
		this.setMaximizedBounds(getDefaultMaxFrameSize());
		//this.setBounds(frameSize);
		
		//Configure Desktop scroll
		configureScroll(scroll);
		getContentPane().add(scroll, BorderLayout.CENTER);

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//Add window close confirmation
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (close()) {
					System.exit(0);
				}
			}
		});

		//Configure desktop
		AbstractDesktop myDesktop = configureDesktop();
		this.setDesktop(myDesktop);
		
		//Configure Desktop Icons
		configureDesktopIcons(myDesktop);
		
		//Configure Desktop menu bar
		JMenuBar desktopMenuBar = new JMenuBar();
		setJMenuBar(desktopMenuBar);
		myDesktop.setMenuBar(desktopMenuBar);

		//Configure toolbar
		DesktopToolBarImpl desktopToolBar = new DesktopToolBarImpl();
		this.setToolBar(desktopToolBar);
		configureToolBar(desktopToolBar);
		
		//Configure status panel
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
		getContentPane().add(statusPanel, BorderLayout.SOUTH);
		
		//TODO 10/11/04, serves no purpose for now
		//configureStatusPanel(statusPanel);
		
		//Need this as the desktop frame is sometimes
		//grey when loading from XML or creating "NEW DESKTOP".  
		this.repaint();
		this.validate();
	}
	
	public final void setFrameTitle(String title){
		String frameTitle = title + ((!this.prefFile.equals("")) ? sTITLEDELIMETER+this.prefFile:"") + sTITLESUFFIX;
		super.setTitle(frameTitle);
	}
	public final String getFrameTitle() {
		//Frame title follows form:
		//Title[sTITLEDELIMETER][STITLESUFFIX]
		
		//Remove the sTITLEDDELIMETER
		String[] justTitle = super.getTitle().split(sTITLEDELIMETER);
		//Means delimeter did not exist, so return the split
		//with title suffix
		if(justTitle.length == 1){
			return justTitle[0].split(sTITLESUFFIX)[0];
		}
		//Means delimeter existed so just return index 0 of split
		else if(justTitle.length ==2){
			return justTitle[0];
		}
		//Title was empty
		else{
			return "";
		}
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessDesktop#getDesktop()
	 */
	public final CoGTop getDesktop() {
		return this.currentDesktop;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessDesktop#setDesktop(org.globus.cog.gridface.impl.desktop.DesktopImpl)
	 */
	public final void setDesktop(CoGTop setdesktop) {
		this.currentDesktop = setdesktop;
		scroll.setViewportView((AbstractDesktop)setdesktop);
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessToolBar#getToolBar()
	 */
	public final DesktopToolBarImpl getToolBar() {
		return this.desktopToolBar;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessToolBar#setToolBar(org.globus.cog.gridface.impl.desktop.toolbar.DesktopToolBar)
	 */
	public final void setToolBar(DesktopToolBarImpl toolbar) {		
		//Remove toolbar if it already exists
		//NOTE: if toolbar is floating then it is not in content pane and cannot be removed
		//this way.
		if(this.desktopToolBar != null){
			getContentPane().remove(getToolBar());
			repaint();
		}
		this.desktopToolBar = toolbar;
		this.currentDesktop.setToolBar(getToolBar());
		
		this.desktopToolBar.setTransferHandler(
			this.currentDesktop.getDefaultDesktopTransferHandler());
		getContentPane().add(getToolBar(), BorderLayout.WEST);
	}

	public void savePreferences(Preferences desktopPref) {
		try{
		desktopPref.sync();
		desktopPref.put("frame.title", this.getFrameTitle());
		desktopPref.put("frame.class",this.getClass().getName());
		desktopPref.putInt("frame.width", this.getWidth());
		desktopPref.putInt("frame.height", this.getHeight());
		desktopPref.putInt("frame.x", this.getLocation().x);
		desktopPref.putInt("frame.y", this.getLocation().y);

		getDesktop().savePreferences(desktopPref);
		}catch(BackingStoreException be){
		    logger.error(be);
		}
	}

	public void loadPreferences(Preferences fPref) throws Exception {
	    logger.debug("Loading preference in node:"+fPref.name());
		int xLoc = fPref.getInt("frame.x", 0);
		int yLoc = fPref.getInt("frame.y", 0);
		int width = fPref.getInt("frame.width", 500);
		int height = fPref.getInt("frame.height", 500);
		String frameClass = fPref.get("frame.class","org.globus.cog.gridface.impl.desktop.GridDesktopContainer"); 
		// String frameClass = fPref.get("frame.class","NotValid"); 
		// TODO: Rob changed
		// I changed the default value to be a valid value instead of "NotValid" and crashing if one was not specified
		// probably need to use a log4j warn here in the future though
		
		if(frameClass.equals(this.getClass().getName())){
			String title = fPref.get("frame.title", Desktop.sNEWDESKTOP);
			logger.debug("Loading frame... [title,xLoc,yLoc,width,height]=["+title+","+xLoc+","+yLoc+","+width+","+height+"]");
			configureFrame(title,new Rectangle(xLoc,yLoc,width,height));
		
			getDesktop().loadPreferences(fPref);
		}else{
			//Need to remove the node from preferences else it will still be present
			// in the registry and will cause future preference loading to fail
			// as we depend on detecting new nodes when XML file is imported
			String errorString = "XML file is of frame class: \""+frameClass+"\"\n"+"But looking for Frame Class: \""+this.getClass().getName()+"\"";
			fPref.removeNode();
			if(this.isVisible()){
				JOptionPane.showMessageDialog(this,errorString);
			}
			throw new Exception(errorString);
		}
	}

	public final boolean fromXML(File xmlFile){
		if(xmlFile != null){
			try {
				Preferences.userRoot().addNodeChangeListener(this);
				setPrefFile(xmlFile.getAbsolutePath());
				logger.debug("Loading Desktop preferences from file >> "+getPrefFileName());
				InputStream is =
					new BufferedInputStream(
						new FileInputStream(xmlFile));
				Preferences.importPreferences(is);
				is.close();
				return true;
			} catch (IOException ioEx) {
			    logger.error(ioEx);
			} catch (InvalidPreferencesFormatException ipFx) {
			    logger.error(ipFx);
			}
		}
	
		return false;
	}
	public final boolean toXML(File xmlFile){
		if(xmlFile != null){
			try {
				String nodeName =
					"Desktop_"
						+ getDesktop().getId()
						+ "__"
						+ System.currentTimeMillis();
				Preferences rootPref = Preferences.userRoot().node(nodeName);
				savePreferences(rootPref);
				logger.debug("Saving Desktop State of node:"+nodeName+" to file >> " + xmlFile.getName());
				OutputStream osTree =
					new BufferedOutputStream(
						new FileOutputStream(xmlFile));
				rootPref.exportSubtree(osTree);
				osTree.close();
					
				Preferences.userRoot().node(nodeName).removeNode();
				return true;
					
			} catch (IOException ioEx) {
			    logger.error(ioEx);
			} catch (BackingStoreException bsEx) {
			    logger.error(bsEx);
			}
			return true;
		}
		return false;
	}
	
	protected final void setPrefFile(String fileName){
		this.prefFile = fileName;
	}
	protected final String getPrefFile(){
		return this.prefFile;
	}
	protected final String getPrefFileName(){
		return new File(this.prefFile).getName();
	}
	protected final void clearPrefFileName(){
		this.prefFile="";
	}
	/* (non-Javadoc)
	 * @see java.util.prefs.NodeChangeListener#childAdded(java.util.prefs.NodeChangeEvent)
	 */
	public final void childAdded(NodeChangeEvent nceEvt) {
		try{
			Preferences prefNode = nceEvt.getChild();
			Preferences.userRoot().sync();
			logger.debug(prefNode + " added to preferences");
			loadPreferences(prefNode);
			prefNode.removeNode();
		
		}catch (BackingStoreException bsEx) {
		    logger.error(bsEx);
		    //If error occored during argument loading of XML file 
		    //then stop the JVM
		    if(this.isVisible()==false){
		        System.exit(1);
		    }
		} catch(Exception e){
		    logger.error(e);
		    //If error occored during argument loading of XML file 
		    //then stop the JVM
		    if(this.isVisible()==false){
		        System.exit(1);
		    }
		}	
	}

	/* (non-Javadoc)
	 * @see java.util.prefs.NodeChangeListener#childRemoved(java.util.prefs.NodeChangeEvent)
	 */
	public final void childRemoved(NodeChangeEvent nceEvt) {
	    logger.debug(nceEvt.getChild() + " removed from preferences");
		Preferences.userRoot().removeNodeChangeListener(this);
		
		//Need this as the desktop frame is sometimes
		//grey when loading from XML.  
		this.repaint();
		this.validate();
	}
	/**
	 * Setup scrollable container for our desktop
	 */
	protected final void configureScroll(JScrollPane configScrlPane) {
		configScrlPane.setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		configScrlPane.setHorizontalScrollBarPolicy(
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// set scrollbars to scroll by 5 pixels each...
		configScrlPane.getHorizontalScrollBar().setUnitIncrement(5);
		configScrlPane.getVerticalScrollBar().setUnitIncrement(5);
	}
	
	class NewDesktop extends AbstractAction {
		public NewDesktop() {
			super(Desktop.sNEWDESKTOP);
		}
		public void actionPerformed(ActionEvent e) {
			if(getDesktop().close()){
				clearPrefFileName();
				String dialogInput=JOptionPane.showInputDialog(null, "New Desktop Name");
				if(dialogInput != null){
					configureFrame( dialogInput );
				}
				
			}
		}
	}
	
	class LoadDesktopPreferences extends AbstractAction	 {
		public LoadDesktopPreferences() {
			super(Desktop.sLOADDESKTOP);
		}
		public void actionPerformed(ActionEvent e) {
			//fromXML(DesktopUtilities.getSourceFile(".",".xml",null));
			fromXML(DesktopUtilities.getSourceFile(new File(prefFile).getParent(),".xml",null));
		}
	}
	class SaveDesktopPreferences extends AbstractAction {
		public SaveDesktopPreferences() {
			super(Desktop.sSAVEDESKTOP);
		}
		public void actionPerformed(ActionEvent e) {
			//toXML(DesktopUtilities.getDestinationFile(".",".xml",null));
			toXML(DesktopUtilities.getDestinationFile(new File(prefFile).getParent(),".xml",null));
		}
	}
	class CloseDesktopFrame extends AbstractAction {
		public CloseDesktopFrame() {
			super(Desktop.sEXIT);
		}
		public void actionPerformed(ActionEvent e) {
			if (close()) {
				System.exit(0);
			}

		}
	}
	

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessClose#close()
	 */
	public boolean close() {
		//If saveChanges==true then show confirmations and close
		if(isSaveChanges() && getDesktop().close()){
			setVisible(false);
			dispose();
			return true;
		}
		//else if saveChanges==false just dispose frame
		else if(!isSaveChanges()){
			setVisible(false);
			dispose();
			return true;
		}
		return false;
	}
}
