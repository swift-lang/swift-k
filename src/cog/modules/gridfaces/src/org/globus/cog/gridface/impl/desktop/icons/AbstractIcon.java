//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.icons;

//Local imports
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.globus.cog.gridface.impl.desktop.AbstractDesktop;
import org.globus.cog.gridface.impl.desktop.frames.DesktopInternalFrameImpl;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessImageOverlay;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessPropertiesPanel;
import org.globus.cog.gridface.impl.desktop.interfaces.CanImportActionProxy;
import org.globus.cog.gridface.impl.desktop.interfaces.CoGTop;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopIcon;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopToolBar;
import org.globus.cog.gridface.impl.desktop.interfaces.FormPanel;
import org.globus.cog.gridface.impl.desktop.interfaces.ImportDataActionProxy;
import org.globus.cog.gridface.impl.desktop.interfaces.MouseActionProxy;
import org.globus.cog.gridface.impl.desktop.panels.FormPanelSet;
import org.globus.cog.gridface.impl.desktop.panels.SimpleFormPanel;
import org.globus.cog.gridface.impl.desktop.util.AttributesHolder;
import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;
import org.globus.cog.gridface.impl.desktop.util.ObjectPair;
import org.globus.cog.gui.grapheditor.util.graphics.ImageProcessor;
import org.globus.cog.util.ImageLoader;
/**
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class AbstractIcon
	extends JLabel
	implements DesktopIcon,AccessImageOverlay,
	CanImportActionProxy,MouseActionProxy,ImportDataActionProxy, Comparable {

    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbstractIcon.class.getName());
    
	//	************ICON CONTAINMENTS*************
	/** Desktop */
	protected CoGTop desktop = null;
	/** DesktopIcon JPopupMenu */
	protected JPopupMenu popupMenu = null;
	
	//	************ICON IMAGE*************\
	/** Original image icon backup*/
	protected ImageIcon origIconBackup = null;
	/** image of icon */
	protected ImageIcon icon = null;
	/** image URI location */
	protected String iconImageURI = null;
	/** darkened image of selected icon */
	protected ImageIcon selectedIcon = null;
	/** icon overlays */
	protected List overlays = null;
	
	//	************ICON PROPERTIES*************\
	/** Icon attributes used to edit in icon properties form */
	protected AttributesHolder iconAttributes = null;
	/** Icon type, default is NATIVE */
	protected String iconType = GenericIconImpl.NATIVE;
	/** Application Container Class associated for this icon */
	protected String appClass = null;
	/** Application Container Class constructor arguments */
	protected ObjectPair appClassArgs = null;
	/** Application should be launched using static main method.
	 * Is a String instead of boolean for easy access through
	 * the Abstract Form Panel framework */
	protected String launchStaticMain = "false";
	
	/** icon unique ID */
	protected int id;
	/** icon ID static counter */
	protected static int id_counter = 0;
	/** is the icon selected */
	protected boolean selected = false;
	/** x axis location where mouse was pressed */
	public int xPressLoc;
	/** y axis location where mouse was pressed */
	public int yPressLoc;

	//	************ICON PROPERTIES FRAME*************\
	/** Icon properties frame */
	protected DesktopInternalFrameImpl propertiesFrame = null;
	/** Panel used to populate icon properties form */
	protected FormPanelSet propertiesPanelSet = new FormPanelSet();

	//************COMMON IMPLEMENTATIONS*************
	public AbstractIcon(
		String applicationClass,
		ObjectPair arguments,
		String text,
		String iconType,
		String iconImageURI)
		throws Exception {

		if (applicationClass == null && arguments != null) {
			throw new Exception("Cannot pass constructor arguments without application class!");
		}
		this.setIconType(iconType); //set icon type
		this.setIconImageURI(iconImageURI);
		this.setAppClass(applicationClass); //save application Container
		this.setAppClassArgsObject(arguments); //save application Constructor arguments

		this.id = id_counter++; //Set icon id
		
		this.setVerticalAlignment(JLabel.CENTER);
		this.setVerticalTextPosition(JLabel.BOTTOM);
		this.setHorizontalAlignment(JLabel.CENTER);
		this.setHorizontalTextPosition(JLabel.CENTER);

		this.iconAttributes=new AttributesHolder();
		
		this.overlays = new LinkedList();
		this.setPopup(new JPopupMenu());
		
		this.setIconText(text);
	}

	
	public final void remove(){
		if(this.getParent() instanceof AbstractDesktop){
			getDesktop().removeIcon(this);
		}else if(this.getParent() instanceof DesktopToolBar){
			getDesktop().getToolBar().removeIcon(this);
		}
	}
	
	public void launch() {
			if(appClass!= null){
				//Launch in a desktop internal frame if !getUserMainMethod()
				if(!new Boolean(getUseMainMethod()).booleanValue()){
					try{
						DesktopInternalFrameImpl iconFrame = new DesktopInternalFrameImpl(appClass,
								appClassArgs, (getText() == null) ? getToolTipText()
										: getText(), true, true, true, true,true);
			
						getDesktop().addFrame(iconFrame);
					}catch(Exception ex){ex.printStackTrace();}
				
				}
				//Else.. start new application using static main method
				else{
					getDesktop().info("Launching static main method of class:"+appClass);
					if(appClassArgs == null){
						appClassArgs = new ObjectPair();
					}
					if(appClassArgs.toString()!=null){
						Thread th=new Thread(){
							public void run(){
								Class params[] = {String[].class};
								String[] args=appClassArgs.toString().split(" ");
								//if there were no String arguments to split we must
								//set args to new String[]{} so static main method will
								//see it as arguments size of zero.
								//NOTE: If the follwing if block is skipped then the split
								//command returns a String[] of size one, which is not 
								//what the main method is looking for when arguments are supposed
								//to be of size 0
								if(args.length==1){
									if(args[0].equals("")){
										args = new String[]{};
									}
								}
								try{
								Class.forName(appClass).
					               getDeclaredMethod("main",  params).
					                  invoke(null, new Object[] {args});
								}
								catch(Exception es){
								    logger.error(es);
									//getDesktop().error(LoggerImpl.getExceptionString(es));
									//es.printStackTrace();
								}
							}
						};
						th.start();
					}
				}
			}
	}
	//Icon types
	public final String getIconType() {
		return this.iconType;
	}

	public final String getIconImageURI(){
		return this.iconImageURI;
	}
	public String getDefaultIconImageURI() {
		return "images/32x32/co/binary.png";
	}
	public final void setIconImageURI(String imageURI){
		if(imageURI != null){
			this.iconImageURI = imageURI;
			//Set image only if it is not a native icon flag
			if(!imageURI.equals(DesktopIcon.NATIVEURI)){
				//Save icon image master copy, used to reset icon
				//overlays
				//this.origIconBackup = ImageLoader.loadIcon(imageURI);
			    //TESTING
				//resize icon images to 32x32 pixels
			    ImageIcon loadedImage = ImageLoader.loadIcon(imageURI);
			    if(loadedImage != null && loadedImage.getImage() != null){
			        this.origIconBackup = new ImageIcon(loadedImage.getImage().getScaledInstance(32,32,Image.SCALE_AREA_AVERAGING));
			    }else{
			        setIconImageURI(getDefaultIconImageURI());
			    }
				this.setIconImage(this.origIconBackup);
			}
		}else{
			setIconImageURI(getDefaultIconImageURI());
			return;
		}
	}
	public final void setIconImage(ImageIcon iconImage){
		if(iconImage != null){
			//CoG image loader returns a null image inside iconimage if invalid
			//image URI was specified
			if(iconImage.getImage() != null){
			    
				super.setIcon(iconImage); //set lable icon
				this.icon = iconImage; //Save default icon image
				// Process selected icon image
				this.selectedIcon = DesktopUtilities.makeIconDark(this.icon);
			}else{
				setIconImageURI(getDefaultIconImageURI());
				return;
			}

		}else{
			setIconImageURI(getDefaultIconImageURI());
			return;
		}
	}
	
	public final void addOverlay(String overlayName, boolean instantUpdate) {
		overlays.add(overlayName);
		if(instantUpdate){
			doOverlay();
		}
	}

	public final void removeAllOverlays(){
		overlays.clear();
		doOverlay();
	}
	public final void removeOverlay(String overlayName) {
		overlays.remove(overlayName);
		doOverlay();
	}

	public final void doOverlay() {
		Iterator i = overlays.iterator();
		ImageIcon overlayicon = this.origIconBackup;
		while (i.hasNext()) {
			String overlay = (String) i.next();
			overlayicon = new ImageIcon(ImageProcessor.compose(overlayicon.getImage(), ImageLoader.loadIcon(overlay).getImage()));
			this.setIconImage(overlayicon);
		}
	}
	
	public String getDefaultIconText() {
		if(this.getIconType().equals(GenericIconImpl.SYSTEM)){
			return "System:"+DesktopIcon.sNEWICON;
		}else{
			return DesktopIcon.sNEWICON;
		}
	}
	public final void setIconText(String text){
		if(text!=null){
			super.setText(text);
			super.setToolTipText(text);
		}else{
			this.setIconText(getDefaultIconText());
		}
		
	}
	
	public String getIconText() {
		return super.getText();
	}
	public final void setIconType(String iconType) {
		if(iconType == null || iconType.length()==0){
			this.iconType = GenericIconImpl.NATIVE;
		}else{
			this.iconType = iconType;
		}
		
	}
	public final Dimension getDimension(){
		int iconWidth =
			getIcon().getIconWidth() + DesktopIcon.ICONTEXT_WIDTH;
		int iconHeight =
			getIcon().getIconHeight() + DesktopIcon.ICONTEXT_HEIGHT;
		return new Dimension(iconWidth,iconHeight);
	}
	
	public final boolean isSelected() {
		return this.selected;
	}

	public final void setSelected(boolean selection) {
		this.selected = selection;
		if (selection) {
			this.setIcon(this.selectedIcon);
		} else {
			this.setIcon(this.icon);
		}
	}

	public final int getId() {
		return this.id;
	}

	//DesktopIcon popup menu
	public final JPopupMenu getPopup() {
		return this.popupMenu;
	}
	public final void setPopup(JPopupMenu desktopPopup) {
		this.popupMenu = desktopPopup;
	}
	public final boolean addPopupMenuItem(JMenuItem newMenuItem) {
		if(this.popupMenu!=null){
			this.popupMenu.add(newMenuItem);
			return true;
		}else{
			return false;
		}
		
	}
	public void configurePopup(JPopupMenu popup){
			JMenuItem newMenuItem = new JMenuItem("Cut");
			newMenuItem.setEnabled(false);
			popup.add(newMenuItem);
	
			newMenuItem = new JMenuItem("Copy");
			newMenuItem.setEnabled(false);
			popup.add(newMenuItem);
			popup.add(new DeleteIconAction());
			popup.addSeparator();
			popup.add(new ShowIconPropertiesAction());
	}
	public final void showPopupAt(int xLoc, int yLoc) {
		//No popups for SYSTEM icons for now
		if(this.iconType != GenericIconImpl.SYSTEM){
			//If popup is empty, then configure it before displaying
			if(this.popupMenu.getComponentCount()==0){
				configurePopup(this.popupMenu);
			}
			this.getDesktop().getToolBar().deselectAllIcons();
			this.getPopup().show(this, xLoc, yLoc);
			this.getDesktop().deselectAllIcons();
			this.setSelected(true);
		}
	}

	public final void setDesktop(CoGTop myDesktop) {
		this.desktop = myDesktop;
	}
	
	public final CoGTop getDesktop() {
		if (this.getParent() instanceof DesktopToolBar) {
			return ((DesktopToolBar) this.getParent()).getDesktop();
		} else {
			return (AbstractDesktop) this.getParent();
		}
	}
	public final String getAppClass() {
		return this.appClass;
	}
	public final void setAppClass(String myAppClass) {
		this.appClass = myAppClass;
		
	}
	public final ObjectPair getAppClassArgsObject(){
		return this.appClassArgs;
	}
	public final String getAppClassArgs(){
		if(this.appClassArgs == null){
			return null;
		}else{
			return (this.appClassArgs.toString() == null)? FormPanel.UNDISPLAYABLE: this.appClassArgs.toString();
		}
	}
	public final void setAppClassArgs(String args) {
		if(args == null){
			setAppClassArgsObject(null);
			return;
		}
		if (!args.equals(FormPanel.UNDISPLAYABLE)) {
			String[] argArray = args.split(" ");
			ObjectPair op = new ObjectPair();
			for (int i = 0; i < argArray.length; i++) {
				op.put(String.class.getName(), argArray[i]);
			}
			this.setAppClassArgsObject(op);
		}
	}
	public final void setAppClassArgsObject(ObjectPair args){
		this.appClassArgs = args;
	}

	public AttributesHolder getAttributesHolder(){
		return this.iconAttributes;
	}

	public String getUseMainMethod(){
		return this.launchStaticMain;
	}
	public void setUseMainMethod(String sBoolean){
		this.launchStaticMain = sBoolean;
	}

	/**
	 * @return Returns the canImportActionProxy.
	 */
	public final CanImportActionProxy getCanImportActionProxy() {
		return (CanImportActionProxy)this;
	}
	/**
	 * @return Returns the importDataActionProxy.
	 */
	public final ImportDataActionProxy getImportDataActionProxy() {
		return (ImportDataActionProxy)this;
	}
	/**
	 * @return Returns the mouseActionProxy.
	 */
	public final MouseActionProxy getMouseActionProxy() {
		return (MouseActionProxy)this;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessPreferences#savePreferences(java.util.prefs.Preferences)
	 */
	public void savePreferences(Preferences iconNode) {				
		if (getText() != null) {
			iconNode.put("icon.name", getText());
		} else {
			iconNode.put("icon.name", getToolTipText());
		}
		iconNode.putInt("icon.x", getLocation().x);
		iconNode.putInt("icon.y", getLocation().y);
		iconNode.put("icon.type", getIconType());
		iconNode.put("icon.imageURI", getIconImageURI());
		if (getAppClass() != null) {
			iconNode.put("App_Container_class", getAppClass());
		} 

		if(appClassArgs != null){
			Preferences classesPref = iconNode.node("App_Container_class_args");
			appClassArgs.savePreferences(classesPref);	
		}

		iconNode.put("icon.launchStaticMain",this.getUseMainMethod());
		//Save attributes in attributes holder in Preferences
		this.iconAttributes.savePreferences(iconNode);
	}
	public void loadPreferences(Preferences iconNode) {
	    logger.debug("Loading Preferences from AbstractIcon");
		this.setIconText(iconNode.get("icon.name",null));
		this.setLocation(iconNode.getInt("icon.x",DesktopIcon.DEFAULT_LOCATION),iconNode.getInt("icon.y",DesktopIcon.DEFAULT_LOCATION));
		this.setIconType(iconNode.get("icon.type",GenericIconImpl.NATIVE));
		this.setIconImageURI(iconNode.get("icon.imageURI",getDefaultIconImageURI()));
			String appContainerClassName = iconNode.get("App_Container_class","null");
			if(!appContainerClassName.equals("null")){
				this.setAppClass(appContainerClassName);
			}
			
			try{
				if(iconNode.nodeExists("App_Container_class_args")){
					appClassArgs = new ObjectPair();
					appClassArgs.loadPreferences(iconNode.node("App_Container_class_args"));	
				}
			}catch (Exception ex){
				logger.error(ex);
			}
			
        this.setUseMainMethod(iconNode.get("icon.launchStaticMain","false"));
		//Load attributes from Preferences to attributes holder
		this.iconAttributes.loadPreferences(iconNode);
		
	}
	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessPropertiesPanel#getPropertiesPanel()
	 */
	public JComponent getPropertiesPanel() {
		SimpleFormPanel sfp = new SimpleFormPanel("Basic Icon Properties");
		
		ArrayList keys = new ArrayList();
		keys.add("IconText");
		keys.add("IconImageURI");
		if(this.iconType.equals(GenericIconImpl.NATIVE)){
			keys.add("IconType");
			keys.add("AppClass");
			keys.add("AppClassArgs");
			keys.add("UseMainMethod");
		}
		sfp.load(keys,this);
		
		//###TESTING
		return sfp;
		//return sfp.getContainer();
		
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessPropertiesPanel#processPropertiesOKButton()
	 */
	public final void processPropertiesOKButton() {
		this.propertiesPanelSet.export();
		processPropertiesCancelButton();
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessPropertiesPanel#processCancelButton()
	 */
	public final void processPropertiesCancelButton() {
		if(getPropertiesFrame() != null){
			getPropertiesFrame().doDefaultCloseAction();
		}
	}
	
	public boolean canImportToComponent(JComponent dropComponent,DesktopIconGroup iconGroup) {
		for (Iterator iter = iconGroup.getIconTypes().iterator(); iter.hasNext();) {
			String ticonType = (String) iter.next();
			return canImportTypePair(ticonType,this.iconType);
			}
		return false;
	}
	
	public void mouseClicked(Object source, int clickCount,
			boolean isLeftMouseClick, int xLoc, int yLoc) {
		DesktopIcon icon = ((DesktopIcon) source);
		//execute launch
		if(clickCount == 2 && isLeftMouseClick){
			icon.launch();
			icon.setSelected(false);
		}
		//show popup
		else if(!isLeftMouseClick){
			icon.showPopupAt(xLoc, yLoc);
		}

	}
	public boolean importDataToComponent(JComponent dropComponent,
			Transferable t, JComponent dragComponent, Point dragPoint,
			Point dropPoint) {

		getDesktop().deselectAllIcons();
		setSelected(true);
		//clear predrag selection icons
		getDesktop().clearCapturedIconSelection();
		return true;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessPropertiesPanel#getPropertiesFrame()
	 */
	public final JInternalFrame getPropertiesFrame() {
		return this.propertiesFrame;
	}

	public void showIconProperties() {
		if(propertiesFrame != null){
			getDesktop().removeFrame(propertiesFrame);
		}
		propertiesFrame = new DesktopInternalFrameImpl(getPropertiesPanel(),getIconText()+":"+DesktopIcon.sPROPERTIES,false,false,true,true,false);
		
		getDesktop().deselectAllIcons();
		
		getDesktop().getToolBar().deselectAllIcons();
		getDesktop().addFrame(propertiesFrame);

	}
	public int compareTo(Object o) {
		if(o instanceof DesktopIcon){
			DesktopIcon icon1 = (DesktopIcon)o;
			//-1 makes NATIVE Icons come at the end of sorting
			return -1*getIconType().compareTo(icon1.getIconType());
			
		}else{
			return 0;
		}
	}
	
	
    public void setLocation(int x, int y) {
        if(this.getParent() instanceof CoGTop){
            DesktopUtilities.fitIconInBounds(this,getDesktop().getBounds(),x,y);
        }
    }

	class ShowIconPropertiesAction extends AbstractAction {
		public ShowIconPropertiesAction() {
			super(DesktopIcon.sPROPERTIES);
		}
		public void actionPerformed(ActionEvent e) {
			showIconProperties();
//			if(propertiesFrame != null){
//				getDesktop().removeFrame(propertiesFrame);
//			}
//			propertiesFrame = new DesktopInternalFrameImpl(getPropertiesPanel(),getIconText()+":"+DesktopIcon.sPROPERTIES,false,false,true,true,false);
//			
//			getDesktop().deselectAllIcons();
//			
//			getDesktop().getToolBar().deselectAllIcons();
//			getDesktop().addFrame(propertiesFrame);
		}
	}
	class PropertiesPanel_OKButtonAction extends AbstractAction {
		public PropertiesPanel_OKButtonAction() {
			super(AccessPropertiesPanel.sOK_button);
		}
		public void actionPerformed(ActionEvent e) {
			processPropertiesOKButton();
		}
	}
	
	class PropertiesPanel_CancelButtonAction extends AbstractAction {
		public PropertiesPanel_CancelButtonAction() {
			super(AccessPropertiesPanel.sCancel_button);
		}
		public void actionPerformed(ActionEvent e) {
			processPropertiesCancelButton();
		}
	}	

	class DeleteIconAction extends AbstractAction {
		public DeleteIconAction() {
			super(DesktopIcon.sDELETEICON);
		}
		public void actionPerformed(ActionEvent e) {
			int answer = 
			DesktopUtilities.optionConfirmation((java.awt.Component)e.getSource(),"Delete Icon?","Delete Icon",JOptionPane.YES_NO_OPTION);
			
			if (answer == JOptionPane.YES_OPTION) {
				remove();
			} 
		}
	}


}
