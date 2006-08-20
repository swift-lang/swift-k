//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
/*
 * Created on Aug 3, 2004
 */
package org.globus.cog.gridface.impl.desktop;

//Local imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;
import javax.swing.event.InternalFrameListener;

import org.globus.cog.gridface.impl.desktop.dnd.DesktopIconTransferable;
import org.globus.cog.gridface.impl.desktop.dnd.DesktopTransferHandler;
import org.globus.cog.gridface.impl.desktop.frames.CoGLogFrame;
import org.globus.cog.gridface.impl.desktop.frames.DesktopInternalFrameImpl;
import org.globus.cog.gridface.impl.desktop.frames.listener.DesktopInternalFrameListener;
import org.globus.cog.gridface.impl.desktop.icons.AbstractIcon;
import org.globus.cog.gridface.impl.desktop.icons.DesktopIconGroup;
import org.globus.cog.gridface.impl.desktop.icons.GenericIconImpl;
import org.globus.cog.gridface.impl.desktop.icons.listener.DesktopIconListener;
import org.globus.cog.gridface.impl.desktop.interfaces.CanImportActionProxy;
import org.globus.cog.gridface.impl.desktop.interfaces.CoGTop;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopIcon;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopInternalFrame;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopToolBar;
import org.globus.cog.gridface.impl.desktop.interfaces.ImportDataActionProxy;
import org.globus.cog.gridface.impl.desktop.interfaces.MouseActionProxy;
import org.globus.cog.gridface.impl.desktop.listener.DesktopListener;
import org.globus.cog.gridface.impl.desktop.toolbar.DesktopToolBarImpl;
import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;
import org.globus.cog.gridface.impl.desktop.util.ObjectPair;
import org.globus.cog.gridface.impl.util.Logger;
import org.globus.cog.gridface.impl.util.LoggerImpl;
import org.globus.cog.gridface.interfaces.Desktop;
import org.globus.cog.gridface.interfaces.GridFace;
import org.globus.cog.gui.about.CoGAbout;
import org.globus.cog.gui.grapheditor.util.swing.MemoryStatisticsFrame;
import org.globus.cog.gui.grapheditor.util.swing.SwingInspectorFrame;

public abstract class AbstractDesktop
	extends JDesktopPane
	implements CoGTop,CanImportActionProxy,ImportDataActionProxy {
	
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbstractDesktop.class.getName());
    
	//************DESKTOP FRAME*************
	protected AbstractDesktopContainer desktopFrame= null;
	protected boolean showWallpaper = true;
	//************ID*************
	/** icon unique ID */
	protected int id;
	/** icon ID static counter */
	protected static int id_counter = 0;
		
	//************ICONS*************	
	/** Desktop icons in desktop */
	protected DesktopIconGroup myDesktopIcons = new DesktopIconGroup();
	/** 
	 * Capture selected icons before mouse press, helps during mouse drag
	 * of multiple icons.
	 */
	protected DesktopIconGroup preDragIconSelection = null;

	//************MENU/TOOL BARS*************
	/** Desktop JMenuBar */
	protected JMenuBar menuBar = null;
	/** Desktop toolbar */
	protected DesktopToolBarImpl toolBar = null;

	//************POPUPS*************
	/** Desktop JPopupMenu */
	protected JPopupMenu popupMenu = null;
	/** Popup point */
	protected static Point popupLocation = new Point();
	
	//************DESKTOP DIMENSIONS*************
	/** Desktop size */
	protected Dimension screenSize = null;

	/** Initial X-Axis location for next icon added to desktop */
	protected int xAxis_init ;
	protected int yAxis_init ;

	//************DEFAULT LISTENERS*************
	/** Default Desktop events listener */
	protected static DesktopListener defaultDesktopListener = null;
	/** Default DesktopIcon events listener */
	protected static DesktopIconListener defaultDesktopIconListener = null;
	/** Default DesktopInternalFrame events listener */
	protected static DesktopInternalFrameListener defaultDesktopFrameListener = null;
	/** Default Desktop Transfer Handler for drag and drop */
	protected static DesktopTransferHandler defaultDesktopTransferHandler =
	null;

	//************LOGGER*************
	/** Desktop Logger instance */
	protected int defaultLogLevel = Logger.INFO;
	public static final String sLOG_CoG = "CoGTop Log";
	public static String sDefaultLoggerName = Desktop.class.getName();
	public static Logger desktopLogger = LoggerImpl.getInstance(sDefaultLoggerName);
	protected CoGLogFrame cogLogFrame = null;
	
	//************GRIDFACE*************
	protected Date lastUpdateTime = new Date(System.currentTimeMillis());
	protected String stringLabel = sNEWDESKTOP;
	protected Vector registeredGridFaces = new Vector();

	//************DEFAULT CONSTRUCTOR*************
	public AbstractDesktop(AbstractDesktopContainer desktopFrame,Dimension screenSize) {
		this.resetDefaultIconLocation();
		this.setDesktopFrame(desktopFrame);
		this.setScreenSize(screenSize);
		this.setPopup(new JPopupMenu());
		this.id = id_counter++;				//Set icon id
		this.setDefaultListeners();
		this.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		this.startLogger();
		this.setWallpaper(org.globus.cog.util.ImageLoader.loadIcon("images/logos/cogdesktop.png"));
//		try{
//			this.setWallpaper(new URI("http://www.yahoo.com"));
//		}catch(URISyntaxException e){e.printStackTrace();}
		this.setBackground(new Color(22,92,163));
	}

	//************COMMON IMPLEMENTATIONS*************
	public final AbstractDesktopContainer getDesktopFrame(){
		return this.desktopFrame;
	}
	public final void setDesktopFrame(AbstractDesktopContainer desktopFrm){
		this.desktopFrame = desktopFrm;
	}
	
	public final int getId() {
		return this.id;
	}
	
	
	public final void debug(String message) {
		desktopLogger.debug(message);

	}
	public final void error(String message) {
		desktopLogger.error(message);

	}
	public final void fatal(String message) {
		desktopLogger.fatal(message);

	}
	public final int getLevel() {
		return desktopLogger.getLevel();
	}
	public final void info(String message) {
		desktopLogger.info(message);

	}
	public final void setLevel(int logLevel) {
		desktopLogger.setLevel(logLevel);

	}
	public final void warn(String message) {
		desktopLogger.warn(message);
	}
	
	public void showAboutFrame(Component parent) {
		CoGAbout ca = new CoGAbout(null, true);		
		ca.show();
	}
	
	public final void clearLog() {
		desktopLogger.clearLog();

	}
	
	public final void saveLogToFile(String fileName) throws IOException{
		desktopLogger.saveLogToFile(fileName);
	}
	
	public void resetDefaultIconLocation() {
		xAxis_init = 10;
		yAxis_init = 0;

	}
	public final void addIcon() throws Exception {
		this.addIcon(
				null,
				GenericIconImpl.NATIVE,
				null,
				DesktopIcon.DEFAULT_LOCATION,
				DesktopIcon.DEFAULT_LOCATION);
	}
	public final void addIcon(String text) throws Exception {
		this.addIcon(text, GenericIconImpl.NATIVE, null);
	}
	public final void addIcon(String iconType, String iconImage) throws Exception {
		this.addIcon(null, iconType, iconImage);
	}
	public final void addIcon(String text, String iconType, String iconImage)
		throws Exception {
		this.addIcon(
			text,
			iconType,
			iconImage,
			DesktopIcon.DEFAULT_LOCATION,
			DesktopIcon.DEFAULT_LOCATION);
	}

	public final void addIcon(String text, int xLoc, int yLoc) throws Exception {
		this.addIcon(text, GenericIconImpl.NATIVE, null, xLoc, yLoc);
	}
	public final void addIcon(String iconType, String iconImage, int xLoc, int yLoc)
		throws Exception {
		this.addIcon(null, iconType, iconImage, xLoc, yLoc);
	}
	public final void addIcon(
		String text,
		String iconType,
		String iconImage,
		int xLoc,
		int yLoc)
		throws Exception {
		this.addIcon(null, null, text, iconType, iconImage, xLoc, yLoc);
	}

	public final void addIcon(String applicationClass, ObjectPair arguments)
		throws Exception {
		this.addIcon(
				applicationClass,
				arguments,
				null,
				GenericIconImpl.NATIVE,
				null,
				DesktopIcon.DEFAULT_LOCATION,
				DesktopIcon.DEFAULT_LOCATION);
	}

	public final void addIcon(
		String applicationClass,
		ObjectPair arguments,
		String text)
		throws Exception {
		this.addIcon(
			applicationClass,
			arguments,
			text,
			GenericIconImpl.NATIVE,
			null,
			DesktopIcon.DEFAULT_LOCATION,
			DesktopIcon.DEFAULT_LOCATION);
	}

	public final void addIcon(
		String applicationClass,
		ObjectPair arguments,
		String text,
		String iconType,
		String iconImage)
		throws Exception {
		this.addIcon(
			applicationClass,
			arguments,
			text,
			iconType,
			iconImage,
			DesktopIcon.DEFAULT_LOCATION,
			DesktopIcon.DEFAULT_LOCATION);
	}

	public final void addIcon(
		String applicationClass,
		ObjectPair arguments,
		String text,
		String iconType,
		String iconImage,
		int xLoc,
		int yLoc)
		throws Exception {

		AbstractIcon newIcon = DesktopUtilities.createIcon(
			applicationClass,
			arguments,
			text,
			iconType,
			iconImage
		);
		logger.debug("addIcon( "+applicationClass+", "+arguments+", "+text);
		this.addIcon(
			newIcon,
			xLoc,
			yLoc,
			this.getDefaultDesktopIconListener(),
			this.getDefaultDesktopIconListener(),
			this.getDefaultDesktopTransferHandler(),
			new DropTarget(newIcon, DesktopTransferHandler.getDropHandler()));

	}
	public final void addIcon(AbstractIcon newIcon) {
		this.addIcon(
			newIcon,
			DesktopIcon.DEFAULT_LOCATION,
			DesktopIcon.DEFAULT_LOCATION,
		this.getDefaultDesktopIconListener(),
		this.getDefaultDesktopIconListener(),
		this.getDefaultDesktopTransferHandler(),
		new DropTarget(newIcon, DesktopTransferHandler.getDropHandler()));

	}
	public final void addIcon(
		AbstractIcon newIcon,
		int xLoc,
		int yLoc,
		MouseListener mouseListener,
		MouseMotionListener mouseMotionListener,
		TransferHandler transferHandler,
		DropTarget dropTarget) {
	
		if(!myDesktopIcons.contains(newIcon)){
			myDesktopIcons.addElement(newIcon); //Add icon to local vector
			this.add(newIcon); 						//Add icon to desktop				
			newIcon.setDesktop(this); 				//Links icon with desktop
		}
		//Add listeners to icon..
		if (mouseListener != null) {
			newIcon.addMouseListener(mouseListener);
		}
		if (mouseMotionListener != null) {
			newIcon.addMouseMotionListener(mouseMotionListener);
		}
		if (transferHandler != null) {
			newIcon.setTransferHandler(transferHandler);
		}
		if (dropTarget != null) {
			newIcon.setDropTarget(dropTarget);
		}

		Dimension iconDim = newIcon.getDimension();
		int iconWidth =	iconDim.width;
		int iconHeight =iconDim.height;
		
		//location specified is not in bounds of desktop screen
			//find default location
		if ((xLoc == DesktopIcon.DEFAULT_LOCATION)
			&& (yLoc == DesktopIcon.DEFAULT_LOCATION)) {
			if (!this
				.getBounds()
				.contains(xAxis_init, yAxis_init + iconHeight)) {
				xAxis_init = xAxis_init + iconWidth;
				yAxis_init = 10;
			}

			DesktopUtilities.fitIconInBounds(newIcon,getBounds(),xAxis_init,yAxis_init);
			yAxis_init = yAxis_init + iconHeight;
			
		} else{
			DesktopUtilities.fitIconInBounds(newIcon,getBounds(),xLoc,yLoc);
		}
	}
	public final void placeIcon(AbstractIcon oldIcon){
		//Used when bringing icon back from toolbar, or rearranging icons
		oldIcon.setBorder(BorderFactory.createEmptyBorder());
		this.addIcon(
		oldIcon,
					DesktopIcon.DEFAULT_LOCATION,
					DesktopIcon.DEFAULT_LOCATION,
				null,
				null,
		null,
		null);
		
		oldIcon.setText(oldIcon.getToolTipText());
	}
	public final void removeIcon(AbstractIcon icon) {
		myDesktopIcons.remove(icon);
		this.remove(icon);
		repaint();
	}
	public final void removeIcons(DesktopIconGroup icons) {
		for(int i =0;i<icons.size();i++){
			removeIcon((AbstractIcon)icons.elementAt(i));		
		}
	}
	public final void removeAllIcons(){
		for(int i=0;i<myDesktopIcons.size();i++){
			this.remove((AbstractIcon)myDesktopIcons.get(i));
		}
		myDesktopIcons.clear();
		
		repaint();
	}
	public final AbstractIcon getIcon(int IconId) {
		// Iterate over desktop icons
		for (int i = 0; i < this.getAllIcons().size(); i++) {
			AbstractIcon curIcon =
				((AbstractIcon) myDesktopIcons.elementAt(i));
			if (curIcon.getId() == IconId) {
				return curIcon;
			}
		}
		return null;
	}

	public final DesktopIconGroup getAllIcons() {
		return this.myDesktopIcons;
	}

	public final DesktopIconGroup getSelectedIcons() {
		DesktopIconGroup selectedIcons = new DesktopIconGroup();
		// Iterate over desktop icons
		for (int i = 0; i < this.getAllIcons().size(); i++) {
			AbstractIcon curIcon =
				((AbstractIcon) myDesktopIcons.elementAt(i));
			if (curIcon.isSelected()) {
				selectedIcons.addElement(curIcon);
			}

		}
		selectedIcons.trimToSize();
		return selectedIcons;
	}

	public final DesktopIconGroup getUnSelectedIcons() {
		DesktopIconGroup selectedIcons = new DesktopIconGroup();
		// Iterate over desktop icons
		for (int i = 0; i < this.getAllIcons().size(); i++) {
			AbstractIcon curIcon =
				((AbstractIcon) myDesktopIcons.elementAt(i));
			if (!curIcon.isSelected()) {
				selectedIcons.addElement(curIcon);
			}

		}
		selectedIcons.trimToSize();
		return selectedIcons;
	}
	
	public final void selectAllIcons() {
		selectIcons(getAllIcons());
	}

	public final void deselectAllIcons() {
		deselectIcons(getAllIcons());
	}

	public final void invertIconSelection(){
		DesktopIconGroup unselecIcons = getUnSelectedIcons();
		deselectAllIcons();
		selectIcons(unselecIcons);
	}
	public final void selectIcons(DesktopIconGroup icons) {
		// Iterate over the icons
		for (int i = 0; i < icons.size(); i++) {
			AbstractIcon curIcon = (AbstractIcon) icons.elementAt(i);
			this.selectIcon(curIcon.getId());
		}
	}

	public final void deselectIcons(DesktopIconGroup icons) {
		// Iterate over the icons
		for (int i = 0; i < icons.size(); i++) {
			AbstractIcon curIcon = (AbstractIcon) icons.elementAt(i);
			this.deselectIcon(curIcon.getId());
		}
	}

	public final void selectIcon(int iconId) {
		// Iterate over desktop icons
		for (int i = 0; i < this.getAllIcons().size(); i++) {
			AbstractIcon curIcon =
				(AbstractIcon) myDesktopIcons.elementAt(i);
			if (iconId == curIcon.getId()) {
				curIcon.setSelected(true);
				return;
			}
		}
	}
	public final void deselectIcon(int iconId) {
		// Iterate over desktop icons
		for (int i = 0; i < this.getAllIcons().size(); i++) {
			AbstractIcon curIcon =
				(AbstractIcon) myDesktopIcons.elementAt(i);
			if (iconId == curIcon.getId()) {
				curIcon.setSelected(false);
				return;
			}
		}
		
	}

	public final void dissableIcons(DesktopIconGroup icons) {
		// Iterate over the icons
		for (int i = 0; i < icons.size(); i++) {
			AbstractIcon curIcon = (AbstractIcon) icons.elementAt(i);
			this.dissableIcon(curIcon.getId());
		}

	}

	public final void enableIcons(DesktopIconGroup icons) {
		// Iterate over the icons
		for (int i = 0; i < icons.size(); i++) {
			AbstractIcon curIcon = (AbstractIcon) icons.elementAt(i);
			this.enableIcon(curIcon.getId());
		}

	}

	public final void dissableIcon(int iconId) {
		// Iterate over desktop icons
		for (int i = 0; i < this.getAllIcons().size(); i++) {
			AbstractIcon curIcon =
				(AbstractIcon) myDesktopIcons.elementAt(i);
			if (curIcon.getId() == iconId) {
				curIcon.setEnabled(false);
				return;
			}
		}
	}

	public final void enableIcon(int iconId) {
		// Iterate over desktop icons
		for (int i = 0; i < this.getAllIcons().size(); i++) {
			AbstractIcon curIcon =
				(AbstractIcon) myDesktopIcons.elementAt(i);
			if (curIcon.getId() == iconId) {
				curIcon.setEnabled(true);
				return;
			}
		}
	}
	
	
	public final void arrangeIcons() {
		this.resetDefaultIconLocation();
		this.getAllIcons().sortByType();
		for (int i = 0; i < myDesktopIcons.size(); i++) {
			AbstractIcon curIcon =
				(AbstractIcon) myDesktopIcons.elementAt(i);
			this.placeIcon(curIcon);
		}
		//TODO
	}
	//<<-- END AccessIcons implementation.

	//Desktop screen size API
	public final void setScreenSize(Dimension screenSize) {
		this.screenSize = screenSize;
		this.setBounds(
			0,
			0,
			(int) screenSize.getWidth(),
			(int) screenSize.getHeight());
		this.setPreferredSize(screenSize);

//		//TODO, , 10/11/04
//		DesktopIconGroup allIcons = this.getAllIcons();
//		if(allIcons!=null){
//			for (Iterator iter = allIcons.iterator(); iter.hasNext();) {
//				AbstractIcon element = (AbstractIcon) iter.next();
//				DesktopUtilities.fitIconInBounds(element,this.getBounds(),element.getLocation().x,element.getLocation().y);
//			}
//		}
	}

	public final Dimension getScreenSize() {
		return (this.screenSize);
	}

	//DesktopInternalFrame API
	public final void addFrame(JInternalFrame newFrame) {
		this.addFrame(
			newFrame,
			this.getDefaultDesktopTransferHandler(),
			new DropTarget(newFrame, DesktopTransferHandler.getDropHandler()),
			this.getDefaultDesktopFrameListener(),
			this.getDefaultDesktopFrameListener());
	}
	public final void addFrame(
		JInternalFrame newFrame,
		TransferHandler transferHandler,
		DropTarget dropTarget,
		VetoableChangeListener vetoChangeListener,
		InternalFrameListener internalFrameListener) {

		if (!this.containsFrame(newFrame)) {
			this.add(newFrame);

			if (transferHandler != null) {
				newFrame.setTransferHandler(transferHandler);
			}
			if (dropTarget != null) {
				newFrame.setDropTarget(dropTarget);
			}
			if (vetoChangeListener != null) {
				newFrame.addVetoableChangeListener(vetoChangeListener);
			}
			if (internalFrameListener != null) {
				newFrame.addInternalFrameListener(internalFrameListener);
			}

			//Adjust the frame bounds if it is outside desktop size
			newFrame.setLocation(this.getX(),this.getY());
			newFrame.setBounds(this.getBounds().intersection(newFrame.getBounds()));
			
			newFrame.setVisible(true);

		} else {
			try {
				newFrame.setSelected(true);
			} catch (PropertyVetoException exception) {
				exception.printStackTrace();
			}

		}
	}
	public final void removeFrame(JInternalFrame frame) {
		this.remove(frame);
		update();
	}

	public final boolean containsFrame(JInternalFrame checkFrame) {
		JInternalFrame allFrames[] = this.getAllFrames();
		for (int i = 0; i < allFrames.length; i++) {
			if (allFrames[i].equals(checkFrame)) {
				return true;
			}
		}
		return false;
	}

	//Desktop internal frame operations.
	public final void closeAllFrames() {
		JInternalFrame[] frames = getAllFrames();

		for (int i = 0; i < frames.length; ++i) {
			if (!frames[i].isIcon()) {
				try {
					frames[i].setIcon(true);
				} catch (java.beans.PropertyVetoException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	public final void openAllFrames() {
		JInternalFrame[] frames = getAllFrames();

		for (int i = 0; i < frames.length; ++i) {
			if (frames[i].isIcon()) {
				try {
					frames[i].setIcon(false);
				} catch (java.beans.PropertyVetoException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	public final void cascadeAllFrames() {
		JInternalFrame[] frames = getAllFrames();
		int x = 0, y = 0;

		for (int i = 0; i < frames.length; ++i) {
			if (!frames[i].isIcon()) {
				frames[i].setBounds(
					x,
					y,
					frames[i].getWidth(),
					frames[i].getHeight());
				x += DesktopInternalFrame.XOFFSET;
				y += DesktopInternalFrame.YOFFSET;
			}
		}
	}

	//Desktop menubar
	public final JMenuBar getMenuBar() {
		return this.menuBar;
	}
	public final void setMenuBar(JMenuBar desktopMenuBar) {
		this.menuBar = desktopMenuBar;
		configureMenuBar(desktopMenuBar);
	}
	public final boolean addMenuBarMenu(JMenu newMenu) {
		if(this.menuBar!=null){
			this.menuBar.add(newMenu);
			return true;
		}else{
			return false;
		}
	}
	protected void configureMenuBar(JMenuBar desktopMenuBar){
		JMenu newMenu = null;
		JMenu newSubMenu = null;
		JMenuItem newMenuItem = null;
		
		newMenu = new JMenu("File");
		newMenu.add(getDesktopFrame().new NewDesktop());
		newMenu.add(new RenameDesktop());		
		newMenu.add(getDesktopFrame().new LoadDesktopPreferences());			
		newMenu.add(getDesktopFrame().new SaveDesktopPreferences());
		newMenu.addSeparator();
		newMenu.add(getDesktopFrame().new CloseDesktopFrame());
		desktopMenuBar.add(newMenu);
		
		newMenu = new JMenu("View");
		final JCheckBox chkCoG = new JCheckBox(sLOG_CoG,cogLogFrame.isVisible());
		chkCoG.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				cogLogFrame.setVisible(chkCoG.isSelected());
			}
		});
		newMenu.add(chkCoG);
		
		newSubMenu = new JMenu("Window");
		newSubMenu.add(new MaximizeAllFramesAction());
		newSubMenu.add(new MinimizeAllFramesAction());
		newSubMenu.add(new CascadeAllFramesAction());
		newMenu.add(newSubMenu);
		
		desktopMenuBar.add(newMenu);
		
		newMenu = new JMenu("Help");

		newSubMenu = new JMenu("Debug");
		newSubMenu.add(new ShowSwingInspectorAction(this));
		newSubMenu.add(new ShowMemInfoAction());
		newSubMenu.add(new DoGarbageCollectionAction());
		newMenu.add(newSubMenu);
		
		newMenuItem = new JMenuItem(new AbstractAction(Desktop.sABOUT){
			public void actionPerformed(ActionEvent e){
				showAboutFrame(null);
			}
		});
		newMenu.add(newMenuItem);

		desktopMenuBar.add(newMenu);
	}
	//Desktop Logger operations
	public final void startLogger() {
		desktopLogger.setLevel(this.defaultLogLevel);
		cogLogFrame = new CoGLogFrame(this,(LoggerImpl)desktopLogger);
		//Dont show frame on desktop startup
		cogLogFrame.setVisible(false);			
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.AbstractDesktop#getLogger()
	 */
	protected final Logger getLogger() {
		return desktopLogger;
	}

	//Desktop popup menu
	public final JPopupMenu getPopup() {
		return this.popupMenu;
	}
	public final void setPopup(JPopupMenu desktopPopup) {
		this.popupMenu = desktopPopup;
		configurePopup(desktopPopup);
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
		logger.info("configurePopu");
		popup.add(getAddNewIconMenu());
		JMenuItem newMenuItem = new JMenuItem(new ArrangeIconAction());
		//newMenuItem.setEnabled(false);
		popup.add(newMenuItem);
		popup.addSeparator();
		
		newMenuItem = new JMenuItem("Paste");
		newMenuItem.setEnabled(false);
		popup.add(newMenuItem);
		
	}
	
	public final void showPopupAt(int xLoc, int yLoc) {
		popupLocation.setLocation(xLoc,yLoc);
		this.getPopup().show(this, xLoc, yLoc);
	}

	//Desktop tool bar
	public final DesktopToolBarImpl getToolBar() {
		return this.toolBar;
	}
	//Toolbar
	public final void setToolBar(DesktopToolBarImpl toolbar) {
		this.toolBar = toolbar;
		toolBar.setDesktop(this);
	}

	//Abstract Action classes for desktop actions.
	class MaximizeAllFramesAction extends AbstractAction {
		public MaximizeAllFramesAction() {
			super(DesktopInternalFrame.sMAXIMIZE_ALL);
		}
		public void actionPerformed(ActionEvent e) {
			openAllFrames();
		}
	}

	class MinimizeAllFramesAction extends AbstractAction {
		public MinimizeAllFramesAction() {
			super(DesktopInternalFrame.sMINIMIZE_ALL);
		}
		public void actionPerformed(ActionEvent e) {
			closeAllFrames();
		}
	}

	class CascadeAllFramesAction extends AbstractAction {
		public CascadeAllFramesAction() {
			super(DesktopInternalFrame.sCASCADE_ALL);
		}
		public void actionPerformed(ActionEvent e) {
			cascadeAllFrames();
		}
	}

	class SetDesktopBGColor extends AbstractAction {
		public SetDesktopBGColor() {
			super(Desktop.sBGCOLOR);
		}
		public void actionPerformed(ActionEvent e) {
			showBGColorChooser();

		}
	}
	public final void showBGColorChooser() {
		Color color =
			JColorChooser.showDialog(
				this,
				"Choose background color",
				getBackground());
		if (color != null) {
			setBackground(color);
			repaint();
		}
	}
	class SetScreenSizeAction extends AbstractAction {
		Dimension newSize = null;
		public SetScreenSizeAction(Dimension size) {
			super(size.toString());
			this.newSize = size;
		}
		public void actionPerformed(ActionEvent e) {
			setScreenSize(newSize);

		}
	}

	public final DesktopInternalFrameListener getDefaultDesktopFrameListener() {
		return defaultDesktopFrameListener;
	}

	public final DesktopIconListener getDefaultDesktopIconListener() {
		return defaultDesktopIconListener;
	}

	public final DesktopListener getDefaultDesktopListener() {
		return defaultDesktopListener;
	}

	public final DesktopTransferHandler getDefaultDesktopTransferHandler() {
		return defaultDesktopTransferHandler;
	}

	public final void setDefaultDesktopFrameListener(DesktopInternalFrameListener listener) {
		defaultDesktopFrameListener = listener;
	}

	public final void setDefaultDesktopIconListener(DesktopIconListener listener) {
		defaultDesktopIconListener = listener;
	}

	public final void setDefaultDesktopListener(DesktopListener listener) {
		defaultDesktopListener = listener;
	}

	public final void setDefaultDesktopTransferHandler(DesktopTransferHandler handler) {
		defaultDesktopTransferHandler = handler;
	}

	public void setDefaultListeners() {
		defaultDesktopListener = new DesktopListener();
		defaultDesktopIconListener = new DesktopIconListener();
		defaultDesktopFrameListener = new DesktopInternalFrameListener();
		defaultDesktopTransferHandler = new DesktopTransferHandler();
		this.addMouseListener(this.getDefaultDesktopListener());
		this.setDropTarget(
			new DropTarget(this, DesktopTransferHandler.getDropHandler()));
		this.setTransferHandler(this.getDefaultDesktopTransferHandler());
	}


	public final int captureIconSelection() {
		this.preDragIconSelection = this.getSelectedIcons();
		return this.preDragIconSelection.size();
	}
	public final void clearCapturedIconSelection() {
		if(this.preDragIconSelection != null){
			this.preDragIconSelection.clear();
		}
	}

	public final int releaseIconSelection() {
		if(this.preDragIconSelection != null){
			this.selectIcons(this.preDragIconSelection);
			return this.preDragIconSelection.size();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.interfaces.GridFace#lastUpdateTime()
	 */
	public final Date lastUpdateTime() {
		return lastUpdateTime;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.interfaces.GridFace#register(org.globus.cog.gridface.interfaces.GridFace)
	 */
	public void register(GridFace connection) {
		registeredGridFaces.addElement(connection);
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.interfaces.GridFace#setLabel(java.lang.String)
	 */
	public final void setLabel(String label) {
		stringLabel = label;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.interfaces.GridFace#update()
	 */
	public void update() {
		lastUpdateTime.setTime(System.currentTimeMillis());
		repaint();
		revalidate();
		updateUI();
	}



	public final void setWallpaper(ImageIcon backIcon){
		if(this.showWallpaper){
			//JLabel background = new JLabel(DesktopUtilities.makeIconGray(backIcon));
			JLabel background = new JLabel(backIcon);
			background.setBounds(
					this.getWidth()/2 - backIcon.getIconWidth()/2,
					this.getHeight()/2 - backIcon.getIconHeight()/2,
					backIcon.getIconWidth(),
					backIcon.getIconHeight());
	
			this.add(background, new Integer(Integer.MIN_VALUE));
		}
	}
	
	//TODO,, 10/12/04, see if you can have HTML page as background
	public final void setWallpaper(URI activeDesktop){
		try{
		JEditorPane txtEdit = new JEditorPane("http://www.globus.org");
		txtEdit.setPage("http://www.globus.org");
		txtEdit.setPreferredSize(new Dimension(300,500));
		this.add(txtEdit,new Integer(Integer.MIN_VALUE));
		}catch(IOException e){e.printStackTrace();}
		
	}
	
	public void savePreferences(Preferences startNode) {
		try{
			startNode.sync();
			
			startNode.putInt("icon.count",getAllIcons().size());
			AbstractIcon curIcon=null;
			for (Enumeration e = getAllIcons().elements(); e.hasMoreElements();) {
				curIcon = (AbstractIcon) e.nextElement();
				curIcon.savePreferences(startNode.node("icons/"+"ICON_ID_" + new Integer(curIcon.getId()).toString()));
			}
		
			getToolBar().savePreferences(startNode.node("toolbar"));
		}catch(BackingStoreException b){
			error(LoggerImpl.getExceptionString(b));
		}
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessPreferences#loadPreferences(java.util.prefs.Preferences)
	 */
	public void loadPreferences(Preferences startNode) {
	    logger.debug("In AbstractDesktop load preferences");
		removeAllIcons();
		int iconCount = startNode.getInt("icon.count",0);
		String frameTitle = startNode.get("frame.title",Desktop.sNEWDESKTOP);
		this.setLabel(frameTitle);
		
		logger.debug("[Icon count,Frame Title]= ["+iconCount+","+frameTitle+"]");
		//All icon types must be defined as "org.globus....IconClassName:TypeTitle"
		String[] iconClassName = null;
		AbstractIcon iconObject = null;
		
		Preferences iconNodePref =null;
		Preferences iconsPref = startNode.node("icons");
		
		try{

			String[] prefChildren = iconsPref.childrenNames();
			try{
				for(int i=0; i<prefChildren.length; i++){
					logger.debug("Loading icon node: "+prefChildren[i]);
					iconNodePref = iconsPref.node(prefChildren[i]);
					String iconType = iconNodePref.get("icon.type",GenericIconImpl.NATIVE);
					logger.debug("Loading icon type: "+iconType);
					iconClassName = iconType.split(":");
					iconObject = (AbstractIcon)Class.forName(iconClassName[0]).newInstance();				
					this.addIcon(iconObject);
					iconObject.loadPreferences(iconNodePref);
					
				}
				getToolBar().loadPreferences(startNode.node("toolbar"));
				this.update();
			}catch (InstantiationException e) {
				error(LoggerImpl.getExceptionString(e));
				e.printStackTrace();
			} 
			catch (IllegalAccessException e) {
				error(LoggerImpl.getExceptionString(e));
				e.printStackTrace();
			} 
			catch (IllegalArgumentException e) {
				error(LoggerImpl.getExceptionString(e));
				e.printStackTrace();
			} 
			
		}
		catch(ClassNotFoundException ex){
			error(LoggerImpl.getExceptionString(ex));
			ex.printStackTrace();
		}
		catch (BackingStoreException be){
			error(LoggerImpl.getExceptionString(be));
			be.printStackTrace();
		}  
		catch (Exception e){
			error(LoggerImpl.getExceptionString(e));
			e.printStackTrace();
		}

	}
	

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessIcons#getAvailableIconTypes()
	 */
	public Vector getAvailableIconTypes() {
		Vector availTypes = new Vector();
		availTypes.addElement(GenericIconImpl.NATIVE);
			
		return availTypes;
	}
	
	
	class ArrangeIconAction extends AbstractAction {
	
		public ArrangeIconAction() {
			super(sARRANGEICONS);
		}
		public void actionPerformed(ActionEvent e) {
			arrangeIcons();
		}
	}
	
	private static Map displayActionNames;
	/**
	 * This is a quick hack to fix the display names
	 * @param iconType
	 * @return
	 */
	private static String getDisplayName(String iconType) {			
		String result = iconType.split(":")[1];
		if(displayActionNames == null) {
			displayActionNames = new HashMap();
		
			synchronized(displayActionNames) {
				displayActionNames.put("SERVICE","Job Submission Service");
				displayActionNames.put("JOB_SPECIFICATION","Job Specification");
				displayActionNames.put("JOB_SUBMISSION","Job Form");
				displayActionNames.put("NATIVE","Native Job");
			}
		}
		
		synchronized(displayActionNames) {
			result = (String) displayActionNames.get(result);
		}
		
		return result;
	}

	class AddNewIconAction extends AbstractAction {		
		String newIconType = null;
		Point newIconXY=null;

		public AddNewIconAction(String iconType, Point xyLoc) {
			super(getDisplayName(iconType));

			this.newIconXY = xyLoc;
			this.newIconType = iconType;
		}

		public void actionPerformed(ActionEvent e) {
			logger.info("actionPerformed");
			try {
				addIcon(this.newIconType, null, newIconXY.x, newIconXY.y);
			} catch (Exception ex) {
				error(LoggerImpl.getExceptionString(ex));
				ex.printStackTrace();
			}
		}		
	}
	
	
	class ShowSwingInspectorAction extends AbstractAction {
		java.awt.Component owner;
		public ShowSwingInspectorAction(java.awt.Component comp) {
			super("Swing Inspector");
			this.owner = comp;
		}
		public void actionPerformed(ActionEvent e) {
			JFrame swingIns= new SwingInspectorFrame(owner);
			swingIns.hide();
			addFrame(new DesktopInternalFrameImpl(swingIns.getRootPane(),"Swing Inspector",300,400,true,true,true,true,false));
		}
	}
	class ShowMemInfoAction extends AbstractAction {
		public ShowMemInfoAction() {
			super("Memory Info");
		}

		public void actionPerformed(ActionEvent e) {
			addFrame(new DesktopInternalFrameImpl(new MemoryStatisticsFrame().getContentPane(),"Memory Info",400,100,true,true,true,true,false));
		}
	}
	class DoGarbageCollectionAction extends AbstractAction {
		public DoGarbageCollectionAction() {
			super("Run Garbage Collection");
		}

		public void actionPerformed(ActionEvent e) {
			System.gc();
		}
	}
	
	public JMenu getAddNewIconMenu() {
		logger.info("getAddNewIconMenu");
		JMenu addNewIconMenu = new JMenu(Desktop.sADD_ICON);
		Vector iconTypes = getAvailableIconTypes();
		for (Iterator iter = iconTypes.iterator(); iter.hasNext();) {
			String iconType = (String) iter.next();
			addNewIconMenu.add( new AddNewIconAction(iconType,popupLocation) );
			if(logger.isDebugEnabled()) {
				logger.debug("Added iconType="+iconType);
			}
		}
		return addNewIconMenu;
	}

	class RenameDesktop extends AbstractAction {
		public RenameDesktop() {
			super(Desktop.sRENAMEDESKTOP);
		}
		public void actionPerformed(ActionEvent e) {
			String dialogInput=JOptionPane.showInputDialog(null, "New Desktop Name",getDesktopFrame().getFrameTitle());
			getDesktopFrame().setFrameTitle((dialogInput!=null) ? dialogInput:getDesktopFrame().getFrameTitle());
		}
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
		//All mouse actions for the destkop are taken care of by
		//the DesktopListener
		return null;
	}
	
	public final boolean canImportToComponent(JComponent dropComponent,
			DesktopIconGroup iconGroup) {
		//Desktop can import all DesktopIconGroup icons
		return true;
	}
	public final boolean importDataToComponent(JComponent dropComponent,
			Transferable t, JComponent dragComponent, Point dragPoint,
			Point dropPoint) {
		try {
			//Desktop icon group being imported
			if (DesktopTransferHandler.isImportDesktopIconGroup) {
				DesktopIconGroup iconGroup = (DesktopIconGroup) t
						.getTransferData(DesktopIconTransferable.groupIconDataFlavor);
				Point dragPointOnScreen = dragComponent.getLocation();
				Point dropPointOnScreen = dropPoint;
				dropPointOnScreen.x -= dragPoint.x;
				dropPointOnScreen.y -= dragPoint.y;

				int xOffset = dropPointOnScreen.x - dragPointOnScreen.x;
				int yOffset = dropPointOnScreen.y - dragPointOnScreen.y;

				//Drop target is Desktop, move selected icons
				for (int i = 0; i < iconGroup.size(); i++) {
					AbstractIcon icon = (AbstractIcon) iconGroup.elementAt(i);

					int xLoc = icon.getX() + xOffset;
					int yLoc = icon.getY() + yOffset;

					//Dragging from toolbar
					if (icon.getParent() instanceof DesktopToolBar) {
						placeIcon(icon);
						getToolBar().removeIcon(icon);
					}
					DesktopUtilities.fitIconInBounds(icon,
							getBounds(), xLoc, yLoc);
				}
				return true;
			}
			//Windows native icons drop import
			else if (DesktopTransferHandler.isSystemWindowsImportToDesktop) {
				List fileList = (List) t
						.getTransferData(DataFlavor.javaFileListFlavor);
				
				info(">>>Importing native Windows icons to desktop!");
				logger.debug("Native windows icon drop files:");
				logger.debug(fileList);
				for (int i = 0; i < fileList.size(); i++) {
					File curFile = (File) fileList.get(i);
					AbstractIcon nativeIcon = new GenericIconImpl(curFile
							.getCanonicalPath(), null, null);
					//TESTING
					//nativeIcon.setIconImageURI(DesktopIcon.NATIVEURI);
					addIcon(nativeIcon);
					//Only add icon at drop location for single icon, if more
					// are present then
					//they will get added at default position on desktop
					if (fileList.size() <= 1) {
						nativeIcon.setLocation(dropPoint);
					}
				}
				return true;
			}
			//Linux native icons drop import
			else if (DesktopTransferHandler.isSystemLinuxImportToDesktop) {
				String[] fileList = ((String) t
						.getTransferData(DataFlavor.stringFlavor)).split("\n");

				info(">>>Importing native UNIX icons to desktop!");
				logger.debug("UNIX native icon drop files:");
				logger.debug(fileList);
				for (int i = 0; i < fileList.length; i++) {
					String fileExec = fileList[i].trim();
					fileExec = fileExec.substring("file:".length());
					AbstractIcon nativeIcon = new GenericIconImpl(fileExec,
							null, null);
					
					//Only add icon at drop location for single icon, if more
					// are present then
					//they will get added at default position on desktop
					addIcon(nativeIcon);
					if (fileList.length <= 1) {
						nativeIcon.setLocation(dropPoint);
					}
				}

				return true;
			}

		} catch (UnsupportedFlavorException ufe) {
			error(LoggerImpl.getExceptionString(ufe));
			ufe.printStackTrace();
		} catch (IOException ioe) {
			error(LoggerImpl.getExceptionString(ioe));
			ioe.printStackTrace();
		} catch (Exception e) {
			error(LoggerImpl.getExceptionString(e));
			e.printStackTrace();
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessClose#close()
	 */
	public boolean close() {
		int answer =
			DesktopUtilities.optionConfirmation(
				this,
				"Save Desktop state ?",
				"Save Desktop state?",
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			File destXML = DesktopUtilities.getDestinationFile(new File(getDesktopFrame().prefFile).getParent(),".xml","Desktop State");
			if(destXML !=null){
				getDesktopFrame().toXML(destXML);
				return true;
			}
			
		}else if(answer == JOptionPane.NO_OPTION){
			return true;
		}
		//dont close desktop if cancel or no is pressed
		return false;
	}
	
}
