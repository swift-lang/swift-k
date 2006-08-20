//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.util;

//Local imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.globus.cog.gridface.impl.desktop.AbstractDesktop;
import org.globus.cog.gridface.impl.desktop.icons.AbstractIcon;
import org.globus.cog.gridface.impl.desktop.icons.GenericIconImpl;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopIcon;
import org.globus.cog.gridface.impl.util.LoggerImpl;
import org.globus.cog.gui.setup.CoGSetup;
import org.globus.cog.gui.setup.SetupFrame;
import org.globus.cog.util.ImageLoader;
import org.globus.common.CoGProperties;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.rsl.ParseException;
import org.globus.rsl.RSLParser;
import org.globus.rsl.RslNode;
import org.globus.rsl.Value;
import org.globus.tools.proxy.GridProxyInit;
import org.globus.tools.ui.util.UITools;
import org.globus.util.Util;
public class DesktopUtilities{
	private static Logger logger = Logger.getLogger(DesktopUtilities.class);
	
	public final static String GLOBUS_HOME = System.getProperty("user.home")+File.separator+".globus";

	// Max byte count is 3/4 max string length (see Preferences
	// documentation).
	static private final int pieceLength =
	  ((3*Preferences.MAX_VALUE_LENGTH)/4);


	public static String getStringFromFile(String fileName) throws IOException{
		String stringFromFile=new String();
		BufferedReader in = new BufferedReader(new FileReader(
				fileName));
		String str;
		while ((str = in.readLine()) != null) {
			stringFromFile += str+"\n";
		}
		in.close();
		return stringFromFile;
	}
	public static boolean loadRSLToIconAttributes(String rslFileName, DesktopIcon icon) throws FileNotFoundException,IOException{
		List values;
		String rslFromFile = getStringFromFile(rslFileName);
		try{
			icon.getDesktop().info("Attempting to Parse file: "+rslFileName+" as RSL file");
			RslNode node = RSLParser.parse(rslFromFile);
			icon.getDesktop().info("RSL file: "+rslFileName+" was parsed SUCCESSFULLY");
			icon.getDesktop().info("NOTE: Only RSL attributes supported are: EXECUTABLE, ARGUMENTS, DIRECTORY, STDOUT, STDERROR AND STDIN");
			icon.getDesktop().info("RSL description was:");
			icon.getDesktop().info(rslFromFile);

			if(node.getParam("EXECUTABLE")!=null){
				values = node.getParam("EXECUTABLE").getValues();
				icon.getAttributesHolder().setAttribute("executable",((Value)values.get(0)).getCompleteValue());
			}
			if(node.getParam("ARGUMENTS")!=null){
				values = node.getParam("ARGUMENTS").getValues();
				icon.getAttributesHolder().setAttribute("taskarguments",listToString(values," "));
			}
			if(node.getParam("DIRECTORY")!=null){
				values = node.getParam("DIRECTORY").getValues();
				icon.getAttributesHolder().setAttribute("directory",((Value)values.get(0)).getCompleteValue());
			}
			if(node.getParam("STDOUT")!=null){
				values = node.getParam("STDOUT").getValues();
				icon.getAttributesHolder().setAttribute("stdoutput",((Value)values.get(0)).getCompleteValue());
			}
			if(node.getParam("STDERROR")!=null){
				values = node.getParam("STDERROR").getValues();
				icon.getAttributesHolder().setAttribute("stderror",((Value)values.get(0)).getCompleteValue());
			}
			if(node.getParam("STDIN")!=null){
				values = node.getParam("STDIN").getValues();
				icon.getAttributesHolder().setAttribute("stdinput",((Value)values.get(0)).getCompleteValue());
			}


		}catch(ParseException pe){
			icon.getDesktop().error("RSL file parsing FAILED");
			icon.getDesktop().error(LoggerImpl.getExceptionString(pe));
			JOptionPane.showMessageDialog(icon.getDesktop().getDesktopFrame(),"Not a valid RSL file: IMPORT_VETOED");
			return false;
		}

		return true;
	}

	public static String listToString(List list, String delimeter){
		boolean firstValue=true;
		StringBuffer valuesToString=new StringBuffer();
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			//Skip the delimeter if its the first value
			//being put in the string buffer
			if(firstValue){
				valuesToString.append(element.toString());
				firstValue=false;
			}else{
				valuesToString.append(delimeter+element.toString());
			}
		}
		return valuesToString.toString();
	}

	public static AbstractIcon createIcon(String applicationClass,
			ObjectPair arguments,
	String text,
	String iconType,
	String iconImage) throws Exception{
		if(iconType == null || iconType.length()==0){
			iconType = GenericIconImpl.NATIVE;
		}
		//All icon types must be defined as "org.globus....IconClassName:TypeTitle"
		String[] iconClassName = iconType.split(":");
		Class iconClass = Class.forName(iconClassName[0]);

		Constructor iconClassConstructor =
			iconClass.getConstructor(
				new Class[] {
					String.class,
					ObjectPair.class,
					String.class,
					String.class,
					String.class });
			return	(AbstractIcon) DesktopUtilities.createObject(
				iconClassConstructor,
				new Object[] {
					applicationClass,
					arguments,
					text,
					iconType,
					iconImage });
	}
	public static Object createObject(
		Constructor constructor,
		Object[] arguments) throws Exception {
		Object object = null;

		try {
			object = constructor.newInstance(arguments);
			return object;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return object;
	}
	public static ImageIcon getSystemIconForFile(String execFileName) {
		try{
			if(DesktopUtilities.isWindowsPlatform()){
				File execFile = new File(execFileName);

				//1 . Large icons.. unsupported in some JVM's..
				sun.awt.shell.ShellFolder sf = sun.awt.shell.ShellFolder.getShellFolder(execFile);
		        ImageIcon iconImage = new ImageIcon(sf.getIcon(true), sf.getFolderType());

		        //2. System icon, only gets the lower quality icons
				//ImageIcon iconImage = (ImageIcon)FileSystemView.getFileSystemView().getSystemIcon(execFile);

				iconImage.setImage(iconImage.getImage().getScaledInstance(32,32,Image.SCALE_AREA_AVERAGING));
				return iconImage;
			}
			return null;
		} catch (Exception ex){ex.printStackTrace();}
	return null;
	}

	public static int optionConfirmation(
		Component sourceComp,
		String confirmString,
		String closeTitle,
		int optionType) {
		int answer =
			JOptionPane.showOptionDialog(
				sourceComp,
				confirmString,
				closeTitle,
				optionType,
				JOptionPane.QUESTION_MESSAGE,
				null,
				null,
				null);
		return answer;
	}
	public static ImageIcon makeIconDark(ImageIcon icon) {
		return new ImageIcon(
			Toolkit.getDefaultToolkit().createImage(
				new FilteredImageSource(
					(icon).getImage().getSource(),
					new DarkenImage())));
	}
	public static ImageIcon makeIconGray(ImageIcon icon) {
		return new ImageIcon(
			Toolkit.getDefaultToolkit().createImage(
				new FilteredImageSource(
					(icon).getImage().getSource(),
					new GrayFilter())));
	}
	public static class DarkenImage extends RGBImageFilter {
		public DarkenImage() {
			super();
			canFilterIndexColorModel = true;
		}
		public int filterRGB(int x, int y, int rgb) {
			return (rgb & 0xff3f3f3f);
		}
	}

	public static class GrayFilter extends RGBImageFilter {
		public GrayFilter() {
			canFilterIndexColorModel = true;
		}
		public int filterRGB(int x, int y, int rgb) {
			int a = rgb & 0xff000000;
			int r = ((rgb & 0xff0000) + 0xff0000) / 2;
			int g = ((rgb & 0x00ff00) + 0x00ff00) / 2;
			int b = ((rgb & 0x0000ff) + 0x0000ff) / 2;
			return a | r | g | b;
		}
	}
	public static void makeCompactGrid(
		Container parent,
		int rows,
		int cols,
		int initialX,
		int initialY,
		int xPad,
		int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
		    AbstractDesktop.logger.error("The first argument to makeCompactGrid must use SpringLayout.");
			exc.printStackTrace();
			return;
		}

		//Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width =
					Spring.max(
						width,
						getConstraintsForCell(r, c, parent, cols).getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints =
					getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		//Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height =
					Spring.max(
						height,
						getConstraintsForCell(r, c, parent, cols).getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints =
					getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		//Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}

	private static SpringLayout.Constraints getConstraintsForCell(
		int row,
		int col,
		Container parent,
		int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component c = parent.getComponent(row * cols + col);
		return layout.getConstraints(c);
	}

  public static TableColumn findTableColumn(JTable table, int columnModelIndex) {
      java.util.Enumeration enumer = table.getColumnModel().getColumns();
      for (; enumer.hasMoreElements(); ) {
          TableColumn col = (TableColumn)enumer.nextElement();
          if (col.getModelIndex() == columnModelIndex) {
              return col;
          }
      }
      return null;
  }
  public static JMenu getMenu(String name, JMenuBar menuBar){
  	for (int i = 0; i < menuBar.getMenuCount(); i++) {
			JMenu menu = menuBar.getMenu(i);
			if(menu.getText().equals(name)){
				return menu;
			}
		}
  	return null;
  }

	public static File getSourceFile(String startLoc,final String dotExtention,String dialogTitle){
		JFileChooser fc = new JFileChooser(startLoc);
		fc.setDialogTitle("Load - " + dialogTitle);
		fc.addChoosableFileFilter(new FileFilter() {
			public boolean accept(File file) {
				String filename = file.getName();

				return (filename.endsWith(dotExtention) || file.isDirectory());
			}
			public String getDescription() {
				return dotExtention;
			}
		});
		// Open file dialog.
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		}
		return null;
	}

	public static File getDestinationFile(String startLoc,final String dotExtention,String dialogTitle){
		JFileChooser fc = new JFileChooser(startLoc);
		fc.setDialogTitle("Save - " + dialogTitle);
		fc.addChoosableFileFilter(new FileFilter() {
			public boolean accept(File file) {
				return true;
			}
			public String getDescription() {
				return dotExtention;
			}
		});
		// Save file dialog.
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			//try{
				File fileSelec = fc.getSelectedFile();
//				if(!fileSelec.getName().endsWith(dotExtention)){
//					fileSelec.renameTo(new File(fileSelec.getCanonicalPath().concat(dotExtention)));
//				}
				return fileSelec;
			//}catch(IOException ex){ex.printStackTrace();}
		}
		return null;
	}

	public static boolean isWindows9xPlatform(){
		return (System.getProperty("os.name").toLowerCase().indexOf("windows 9")>-1 ? true: false);
	}
	public static boolean isWindowsNTBasedPlatform(){
		String OS = System.getProperty("os.name").toLowerCase();
		return ((OS.indexOf("nt") > -1)
		|| (OS.indexOf("windows 2000") > -1 )
		|| (OS.indexOf("windows xp") > -1)) ? true : false;
	}
	public static boolean isWindowsPlatform(){
		return isWindows9xPlatform()||isWindowsNTBasedPlatform();
	}
	public static boolean isLinuxPlatform(){
		return (!isWindowsPlatform()) ? true : false;
	}
	public static String getPlatformCommandString(){
		if(isWindows9xPlatform()){
			return "command.com /c start ";
		}else if(isWindowsNTBasedPlatform()){
			return "cmd.exe /c start ";
		}else{
			return "sh ";
		}
	}

	public static String convertFileNametoExternalForm(String fileName){
		try{
			if(fileName!=null && isWindowsPlatform()){
				File file = new File(fileName);
				return file.toURL().toExternalForm().replaceAll("\\s","%20");
			} else{
				return fileName;
			}

		}catch(java.net.MalformedURLException me){me.printStackTrace();}
		return null;
	}

	public static Point fitIconInBounds(AbstractIcon icon, Rectangle bounds,int xLoc, int yLoc){
		Dimension iconDim = icon.getDimension();
		int iconWidth =	iconDim.width;
		int iconHeight =iconDim.height;

		xLoc = (xLoc < 0) ? 0 : xLoc;
		yLoc = (yLoc < 0) ? 0 : yLoc;
		xLoc =
			(xLoc > (int) bounds.getWidth() - iconWidth)
				? (int) bounds.getWidth() - iconWidth
				: xLoc;
		yLoc =
			(yLoc > (int) bounds.getHeight() - iconHeight)
				? (int) bounds.getHeight() - iconHeight
				: yLoc;
		//icon.setLocation(xLoc,yLoc);
		icon.setBounds(xLoc, yLoc, iconWidth, iconHeight);
		return new Point(xLoc,yLoc);

	}


	public static void showSplashWindow(String imageURI, Frame f, int waitTime){
		JWindow splash = new SplashWindow(imageURI,f,waitTime);
		splash.show();
	}
	public static class SplashWindow extends JWindow
	{
		public SplashWindow(String imageURI, Frame f, int waitTime)
		{
			super(f);
			JLabel l = new JLabel(ImageLoader.loadIcon(imageURI));
			getContentPane().add(l, BorderLayout.CENTER);
			pack();
			Dimension screenSize =
			  Toolkit.getDefaultToolkit().getScreenSize();
			Dimension labelSize = l.getPreferredSize();
			setLocation(screenSize.width/2 - (labelSize.width/2),
						screenSize.height/2 - (labelSize.height/2));
			addMouseListener(new MouseAdapter()
				{
					public void mousePressed(MouseEvent e)
					{
						setVisible(false);
						dispose();
					}
				});
			final int pause = waitTime;
			final Runnable closerRunner = new Runnable()
				{
					public void run()
					{
						setVisible(false);
						dispose();
					}
				};
			Runnable waitRunner = new Runnable()
				{
					public void run()
					{
						try
							{
								Thread.sleep(pause);
								SwingUtilities.invokeAndWait(closerRunner);
							}
						catch(Exception e)
							{
								e.printStackTrace();
								// can catch InvocationTargetException
								// can catch InterruptedException
							}
					}
				};
			setVisible(true);
			Thread splashThread = new Thread(waitRunner, "SplashThread");
			splashThread.start();
		}
	}

	public static void showCoGSetup(){
		try
		{
			CoGSetup setup = new CoGSetup();
			setup.show();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static boolean checkGridProyInfo(){
		String file = null;
		GlobusCredential proxy = null;

		try {
			if (file == null) {
				file = CoGProperties.getDefault().getProxyFile();
			}
			proxy = new GlobusCredential(file);
		} catch (Exception gpe) {
			logger.debug("Appears no credential",gpe);
			return false;
		}

		if(proxy.getTimeLeft() == 0 ){
			return false;
		}else{
			return true;
		}
	}
	
	public static void showCreateGridProxy(JFrame container,boolean modal){
		GridProxyInit proxyInitFrame = new GridProxyInit(container,modal);
		proxyInitFrame.setRunAsApplication(false);
		proxyInitFrame.setCloseOnSuccess(true);
		proxyInitFrame.pack();
		UITools.center(container, proxyInitFrame);
		proxyInitFrame.setVisible(true);
	}
	/**
	 * Customized version of the setup that calls closeAction.actionPerformed upon completion
	 * @param container - 
	 * @param closeAction - an action to do after closing the window
	 */
	public static void showCreateCoGSetup(final JFrame container, final Action closeAction){
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final CoGSetup setup = new CoGSetup() {
					// override the close
					public void frameClosed() {
						super.frameClosed();
						if(closeAction != null) {
						  closeAction.actionPerformed(new ActionEvent(this,1,"CoGSetup.closeAction"));
						}
					}
				};
				setup.show();
			}
		});		
	}
	
	public static void showDestroyGridProxy(JFrame container){
		String proxyFileName =
			CoGProperties.getDefault().getProxyFile();
		if (proxyFileName != null) {

			File proxyFile = new File(proxyFileName);
			if (!proxyFile.exists()) {
				JOptionPane.showMessageDialog(
						container,
					"Your Grid proxy certificate is already destroyed.",
					"Security Message",
					JOptionPane.WARNING_MESSAGE);
				return;
			} else {
				int answer =
					JOptionPane.showOptionDialog(
							container,
						"Destroy your proxy?",
						"Destry proxy Confirmation",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null);

				if (answer == JOptionPane.YES_OPTION) {
					Util.destroy(proxyFile);
					JOptionPane.showMessageDialog(
							container,
						"Your Grid proxy certificate has been destroyed.",
						"Security Message",
						JOptionPane.INFORMATION_MESSAGE);
				}
			}

		}
	}
	public static void showGridProxyInfo(JFrame container){
		String file = null;
		GlobusCredential proxy = null;

		try {
			if (file == null) {
				file = CoGProperties.getDefault().getProxyFile();
			}
			proxy = new GlobusCredential(file);
		} catch (Exception gpe) {
			JOptionPane.showMessageDialog(
					container,
				"Unable to load Grid proxy certificate.\nError: "
					+ gpe.getMessage(),
				"Security Message",
				JOptionPane.WARNING_MESSAGE);
			return;
		}

			StringBuffer proxyInfoBuffer = new StringBuffer();
			proxyInfoBuffer.append(
				"Subject: " + CertUtil.toGlobusID(proxy.getSubject()) + "\n");

			proxyInfoBuffer.append(
				"Strength: " + proxy.getStrength() + " bits" + "\n");

			proxyInfoBuffer.append(
				"Time Left: " + Util.formatTimeSec(proxy.getTimeLeft()));

			JOptionPane.showMessageDialog(
					container,
				proxyInfoBuffer.toString(),
				"Grid Proxy Certificate Information",
				JOptionPane.INFORMATION_MESSAGE);
	}
}

