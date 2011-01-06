// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
/*
 * Created on Aug 9, 2004
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.globus.cog.gridface.impl.desktop.icons;

//Local imports
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.globus.cog.gridface.impl.desktop.interfaces.DesktopIcon;
import org.globus.cog.gridface.impl.desktop.panels.AbstractFormPanel;
import org.globus.cog.gridface.impl.desktop.panels.SimpleFormPanel;
import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;
import org.globus.cog.gridface.impl.desktop.util.ObjectPair;

public class GenericIconImpl extends AbstractIcon {
	public static final String NATIVE = "org.globus.cog.gridface.impl.desktop.icons.GenericIconImpl:Native_Icon";
	public static final String SYSTEM =
		"org.globus.cog.gridface.impl.desktop.icons.GenericIconImpl:System";

	private String executable = null;

	private String arguments = null;
	
	public GenericIconImpl() throws Exception {
		this(null, null, null, GenericIconImpl.NATIVE, null);
	}

	public GenericIconImpl(String applicationClass, ObjectPair arguments,
			String text, String iconType) throws Exception {
		this(applicationClass, arguments, text, iconType, null);
	}

	public GenericIconImpl(String exec, String arg, String output)
			throws Exception {
		this();
		setIconImageURI(DesktopIcon.NATIVEURI);
		this.setExecutable(exec);
		this.setArguments(arg);

	}

	public GenericIconImpl(String applicationClass, ObjectPair arguments,
			String text, String iconType, String iconImage) throws Exception {
		super(applicationClass, arguments, text, iconType, iconImage);
	}

	public void configurePopup(JPopupMenu popup) {
		super.configurePopup(popup);
		JMenuItem newMenuitem = new JMenuItem("Run");
		newMenuitem.setEnabled(false);
		popup.add(newMenuitem,0);
	}
	
    public String getDefaultIconImageURI() {
      if(this.iconType.equals(NATIVE) && this.getExecutable() != null){
          String execToLowerCase = this.getExecutable().toLowerCase();
          if(execToLowerCase.endsWith(".doc")){
              return "images/32x32/co/win-doc.png";
          }else if(execToLowerCase.endsWith(".vsd")){
              return "images/32x32/co/win-vsd.png";
          }else if(execToLowerCase.endsWith(".pdf")){
              return "images/32x32/co/win-pdf.png";
          }else if(execToLowerCase.endsWith(".xls")){
              return "images/32x32/co/win-xls.png";
          }else if(execToLowerCase.endsWith(".ppt")){
             return "images/32x32/co/win-ppt.png";
          }else{
              return DesktopIcon.NATIVEURI;
          }
      }
     
      return super.getDefaultIconImageURI();
    }
	public boolean canImportTypePair(String type1, String type2) {
		//Generic Icons cannot be dropped on at this point..
		return false;
	}
	public void launch() {
		String iconType = getIconType();
		try {
		    logger.debug("Attempting to launch GenericIcon..");
			//if (iconType.equals(GenericIconImpl.NATIVE)) {
		
				if (getExecutable() != null) {
					try {
						String cmd = DesktopUtilities
								.getPlatformCommandString()
								+ DesktopUtilities
										.convertFileNametoExternalForm(getExecutable());
						if (getArguments() != null) {
							cmd += " " + getArguments();
						}
						logger.debug("Executing:" + cmd);
						
						Runtime.getRuntime().exec(cmd);
					} catch (IOException e) {
						e.printStackTrace();
					}

				} 
				//since in windows, commands are executed by appending getExecutable() to
				//platform command string "cmd /c start [executable] [arguments]"
				//it can take http://www.globus.org for an argument with no executable
				//to invoke default browser..
				else if(getArguments()!=null && DesktopUtilities.isWindowsPlatform()){
						try {
						    String cmd = DesktopUtilities
							.getPlatformCommandString()
							+ getArguments();
						    
							logger.debug("Executing:" + cmd);
							
							Runtime.getRuntime().exec(cmd);
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
				else {
					super.launch();
				}

//			} else {
//				super.launch();
//			}

		} catch (Exception exp) {
			exp.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessPropertiesPanel#getPropertiesPanel()
	 */
	public JComponent getPropertiesPanel() {
		this.propertiesPanelSet.clear();
		
		SimpleFormPanel sfp = new SimpleFormPanel("Generic Icon Properties");
		ArrayList keys = new ArrayList();
		keys.add("Executable");
		keys.add("Arguments");

		sfp.load(keys, this);
		//this.propertiesPanelSet.addForm(sfp);
		//TESTING
		this.propertiesPanelSet.addForm(sfp);
		
		
//		propertiesPanelSet.addForm((SimpleFormPanel) super
//				.getPropertiesPanel());	
		propertiesPanelSet.addForm((AbstractFormPanel)super
		.getPropertiesPanel());	
		
		this.propertiesPanelSet.addButtonAction(new PropertiesPanel_OKButtonAction());
		this.propertiesPanelSet.addButtonAction(new PropertiesPanel_CancelButtonAction());
		this.propertiesPanelSet.finishedAddingButtonActions();
		
		//TESTING
		return this.propertiesPanelSet;
		//return this.propertiesPanelSet.getScrollContainer();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessPreferences#savePreferences(java.util.prefs.Preferences)
	 */
	public void savePreferences(Preferences iconNode) {
		super.savePreferences(iconNode);
		if(getExecutable()!=null){
			iconNode.put("executable", getExecutable());
		}
		if(getArguments()!=null){
			iconNode.put("arguments", getArguments());
		}
	}

	public void loadPreferences(Preferences iconNode) {
	    logger.debug("Loading Preferences from GenericIconImpl");
		super.loadPreferences(iconNode);
		setArguments(iconNode.get("arguments", null));
		setExecutable(iconNode.get("executable", null));
	}

	/**
	 * @return
	 */
	public String getArguments() {
		return arguments;
	}

	/**
	 * @return
	 */
	public String getExecutable() {
		return executable;
	}

	/**
	 * @param string
	 */
	public void setArguments(String string) {
		arguments = string;
	}

	/**
	 * @param string
	 */
	public void setExecutable(String exec) {
		if (exec != null) {
			this.executable = exec;
			this.setIconImageURI(this.getDefaultIconImageURI());
			setIconText(new File(this.executable).getName());
			
//			//TESTING
//			if (exec.length() != 0) {
			if (exec.length() != 0 && this.getIconImageURI().equals(DesktopIcon.NATIVEURI)) {
				setIconImage(DesktopUtilities.getSystemIconForFile(exec));
//				setIconText(new File(this.executable).getName());
////				setIconImageURI(DesktopIcon.NATIVEURI);
			}
		} else {
			this.executable = exec;
		}
	}
}
