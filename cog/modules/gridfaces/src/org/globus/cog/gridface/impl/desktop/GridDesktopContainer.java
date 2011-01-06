//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop;
/*
 * Desktop launcher main frame
 */

//Local imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.globus.cog.gridface.impl.desktop.frames.DesktopInternalFrameImpl;
import org.globus.cog.gridface.impl.desktop.icons.GenericIconImpl;
import org.globus.cog.gridface.impl.desktop.toolbar.DesktopToolBarImpl;
import org.globus.cog.gridface.impl.desktop.util.DesktopProperties;
import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;
import org.globus.cog.gridface.impl.desktop.util.ObjectPair;
import org.globus.cog.gridface.impl.directorybrowser.DirectoryBrowserImpl;
import org.globus.cog.gridface.impl.gftpanel.GridFTPPanelImpl;
import org.globus.cog.gridface.impl.imageviewer.ImageViewerImplToolBar;
import org.globus.cog.gui.grapheditor.Launcher;
import org.globus.cog.gui.grapheditor.targets.swing.SwingCanvasRenderer;
import org.globus.cog.karajan.viewer.KarajanFrame;
import org.globus.cog.karajan.viewer.KarajanRootNode;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;
import org.globus.cog.util.ImageLoader;
import org.globus.cog.util.plugin.Plugin;
import org.globus.cog.util.plugin.Support;
import org.globus.cog.util.plugin.ToolBarButton;

public class GridDesktopContainer
	extends AbstractDesktopContainer {
    public static final String PLUGIN_PREFIX = "desktop.plugin.";
	
	/** indicates whether to check proxy on loading */
	protected static boolean noproxy;
	
	public GridDesktopContainer() {
		super("GridDesktop");
		this.setIconImage(ImageLoader.loadIcon("images/logos/globus-icon.png").getImage());
	}
	public GridDesktopContainer(File xmlFile){
		super(xmlFile);
		this.setIconImage(ImageLoader.loadIcon("images/logos/globus-icon.png").getImage());
	}
	
	protected AbstractDesktop configureDesktop() {	
		return new GridDesktopImpl(this,getDefaultDesktopSize(),noproxy);
	}

	protected void configureDesktopIcons(AbstractDesktop myDesktop) {
	
		try {		
//			myDesktop.addIcon(null,null,"/bin/date",GridIconImpl.JOB_SUBMISSION,null);
//			myDesktop.addIcon("Service",GridIconImpl.SERVICE,null);
//			myDesktop.addIcon("JobSpec",GridIconImpl.JOB_SPECIFICATION,null);
		} catch (Exception exp) {
			//Caught when constructor arguments were passed without a Class
			exp.printStackTrace();
		}
	}
	
	public void loadPlugins(DesktopToolBarImpl desktopToolBar) {
	    logger.info("loadPlugins");
	    
	    Properties props = DesktopProperties.getDefault();
	    Iterator iProps = props.keySet().iterator();
	    while(iProps.hasNext()) {
	        String propName = (String)iProps.next();
	        logger.debug("propName="+propName);
	        if(propName.startsWith(PLUGIN_PREFIX)) {
	            String label = props.getProperty(propName);
	            String className = propName.substring(PLUGIN_PREFIX.length(),propName.length());
	            try {
	                Plugin plugin = (Plugin)Class.forName(className).newInstance();
	                addPluginToToolBar(plugin,desktopToolBar);
	            }catch(Exception e) {
	                logger.warn("Couldn't load plugin for key "+propName+".",e);
	            }
	        }
	    }
	}
	
	public void addPluginToToolBar(final Plugin plugin, DesktopToolBarImpl desktopToolBar) {
	    logger.debug("adding plugin="+plugin);
	    final ImageIcon pluginIcon = plugin.getImageIconC32x32();
	    final Icon darkPluginIcon = DesktopUtilities.makeIconDark(pluginIcon);
	    final JButton pluginButton = new ToolBarButton(pluginIcon);
	    final GridDesktopContainer _this = this;
	    pluginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Plugin p = (Plugin)plugin.getClass().newInstance();                        
                    final JInternalFrame frame = new DesktopInternalFrameImpl(p.getTitle(),true,true,true);
                    
                    p.setCloseAction(new AbstractAction() {
                        public void actionPerformed(ActionEvent aEvent) {
                            try {
                                frame.setClosed(true);
                            } catch (PropertyVetoException pvException) {}
                        }
                    });
                    frame.setContentPane(Support.injectPlugin(p,frame));
                    frame.pack();
                    getDesktop().addFrame(frame);
                    frame.setVisible(true);
                    try {
                        frame.setSelected(true);
                    } catch (java.beans.PropertyVetoException pvException) {}                       
                }catch(Exception exception) {
                    logger.warn("Couldn't start plugin",exception);
                }
            }               		        
	    });
	    desktopToolBar.add(pluginButton);
	}
	
	protected void configureToolBar(DesktopToolBarImpl desktopToolBar) {	
	    logger.info("configureToolBar");
		try {	    
		    loadPlugins(desktopToolBar);
			//Directory Browser icon
			GenericIconImpl newIcon =
				new GenericIconImpl(
					DirectoryBrowserImpl.class.getName(),
			((GridDesktopImpl)getDesktop()).getGCMObjectPair(),
					"Directory Browser",
					GenericIconImpl.SYSTEM,
			"images/32x32/co/view-tree.png");
			desktopToolBar.addIcon(newIcon,true);
			
			//Image Viewer icon
			newIcon =
				new GenericIconImpl(
					ImageViewerImplToolBar.class.getName(),
			((GridDesktopImpl)getDesktop()).getGCMObjectPair(),
					"Image Viewer",
					GenericIconImpl.SYSTEM,
			"images/32x32/co/kdmconfig.png");
			desktopToolBar.addIcon(newIcon,true);
			
			//GridFTP by Scott Goose	
			ObjectPair gridFTPargs = new ObjectPair();
			gridFTPargs.put(String.class.getName(),"wiggum.mcs.anl.gov");
			gridFTPargs.put(String.class.getName(),"2811");
			gridFTPargs.put(String.class.getName(),"/home/pankaj/imageTest.jpg");
			gridFTPargs.put(String.class.getName(),"wiggum.mcs.anl.gov");
			gridFTPargs.put(String.class.getName(),"2811");
			gridFTPargs.put(String.class.getName(),"/home/pankaj/test/imageTest.jpg");
			newIcon =
				new GenericIconImpl(
					GridFTPPanelImpl.class.getName(),
					gridFTPargs,
					"Java CoG Kit: Simple GridFTP Queue",
					GenericIconImpl.SYSTEM,
					"images/32x32/co/fileshare.png");
			desktopToolBar.addIcon(newIcon,true);

			//Add bug browser Panel icon
//			ObjectPair bugBrowargs = new ObjectPair();
//			bugBrowargs.put(String.class.getName(),"http://bugzilla.globus.org/globus/enter_bug.cgi?product=Java%20CoG%20Kit");
//			newIcon =
//				new GenericIconImpl(
//					"org.globus.cog.gridface.impl.texteditor.TextEditor",
//					bugBrowargs,
//					"Report Desktop Bug",
//					GenericIconImpl.SYSTEM,
//					"images/32x32/co/setup-icon.png");
			newIcon =
				new GenericIconImpl(null,"http://bugzilla.globus.org/globus/enter_bug.cgi?product=Java%20CoG%20Kit",null);
				newIcon.setIconImageURI("images/32x32/co/setup-icon.png");
				newIcon.setIconType(GenericIconImpl.SYSTEM);
			desktopToolBar.addIcon(newIcon,true);
			
			//Globus website icon
//			ObjectPair globusWeb = new ObjectPair();
//			globusWeb.put(String.class.getName(),"http://www.globus.org");
//			newIcon =
//				new GenericIconImpl(
//					"org.globus.cog.gridface.impl.texteditor.TextEditor",
//					globusWeb,
//					"Globus Website",
//					GenericIconImpl.SYSTEM,
//					"images/32x32/co/window-globus.png");
			newIcon =
			new GenericIconImpl(null,"http://www.cogkit.org",null);
			newIcon.setIconImageURI("images/32x32/co/window-globus.png");
			newIcon.setIconType(GenericIconImpl.SYSTEM);
			desktopToolBar.addIcon(newIcon,true);

			//Karajan Viewer icon
			newIcon =
				new GenericIconImpl(
					"org.globus.cog.karajan.viewer.KarajanFrame",
					null,
					"Java CoG Kit: Karajan Workflow",
					GenericIconImpl.SYSTEM,
					"images/32x32/co/window-graph.png"){
				public void launch(){
					new Thread(){
						public void run(){
							
							KarajanRootNode root = new KarajanRootNode();
							KarajanFrame frame = new KarajanFrame(root);
							Launcher.parseProperties("target.properties", root);
							Launcher.parseProperties("grapheditor.properties", root);
							root.setPropertyValue("karajan.frame", frame);
							((SwingCanvasRenderer)frame.getCanvasRenderer()).initialize();
							DesktopInternalFrameImpl karajanComponent = new DesktopInternalFrameImpl(
									frame.getContentPane()
									,"Java CoG Kit: Karajan Workflow",500,500,true,true,true,true,true);
							getDesktop().addFrame(karajanComponent);
				
						}
					}.start();
				}
			};
			desktopToolBar.addIcon(newIcon,true);


			//More to add later...

		} catch (Exception exp) {
			//Caught when constructor arguments were passed without a Class
			exp.printStackTrace();
			
		}
	}

	protected void configureStatusPanel(JPanel statusPanel) {
		JLabel statusLabel = new JLabel("Grid Desktop Status area..");
		statusLabel.setBorder(BorderFactory.createEtchedBorder());
		statusPanel.add(statusLabel);
	}


	public static void main(final String[] args) {
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    logger.debug("Argument Size: "+args.length);

				GridDesktopContainer gridDesktop=null;
				
				ArgumentParser ap = new ArgumentParser();
				configureArgumentParser(ap);
		        ap.addFlag("no-proxy", "Do not check for proxy on start up");
		        ap.addAlias("no-proxy", "np");

		        try {
		            ap.parse(args);
		            if (ap.isPresent("help")) {
		                ap.usage();
		            } else {
		                ap.checkMandatory();
		                //ap.check();
		                try {
		                	GridDesktopContainer.noproxy = ap.isPresent("no-proxy");
		                	saveChanges = !(ap.isPresent("no-save"));
		                	//-file filename has priority over the default desktop state file
		                	if(ap.isPresent("file")){
		                		if(ap.getStringValue("file")==null){
		                		    logger.fatal("Need to specify an XML file",new Exception());
									ap.usage();
									System.exit(1);
		                		}
		                		gridDesktop = new GridDesktopContainer(new File(ap.getStringValue("file")));
		                		
		                	}else if(!ap.isPresent("empty-state")){
		                	    
		                		// TODO: start Rob changed
		                		// Should not have a default file, only default properties, later remove this and just have else		                		
		                		gridDesktop = new GridDesktopContainer();
		                	    		                	    
		                	    // String desktopFile = DesktopProperties.getDefault().getProperty("desktopfile");
		                		// logger.debug("Loading desktop from: "+desktopFile);
		                		// gridDesktop = new GridDesktopContainer(new File(desktopFile));
		                	    // end changed

		                	}else{
		                		gridDesktop = new GridDesktopContainer();
		                	}
	                		//DesktopUtilities.showSplashWindow("images/logos/about.png",null,100);
	                		gridDesktop.show();
		                } catch (Exception e) {
		                    logger.error("Exception in GridDesktopContainer main",e);
		                }
		            }
		        } catch (ArgumentParserException e) {
		            logger.error("Error parsing arguments: " + e.getMessage(),e);
		            ap.usage();
		        }

			}
		});
	}

}
