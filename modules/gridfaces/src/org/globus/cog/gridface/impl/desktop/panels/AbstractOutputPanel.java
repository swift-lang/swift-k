//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;
import org.globus.cog.gridface.interfaces.GridCommand;


public abstract class AbstractOutputPanel extends AbstractFormPanel{
	public String fromFile=new String();
	protected GridCommand gridcommand = null;
	
	public AbstractOutputPanel(String panelTitle,GridCommand command) {
		super(panelTitle);
		this.gridcommand = command;
		setPreferredSize(new Dimension(300,300));
	}
	protected Component getNewComponentForObject(Object hashValue) {
		//In output panels we want to show String in a JTextArea instead
		//of JTextField
		if (hashValue instanceof String) {
			return new JScrollPane(new JTextArea((String)hashValue));
		}else if(hashValue == null){
			return getNewComponentForObject("None");
		} else{
			return super.getNewComponentForObject(hashValue);
		}
	}
	public boolean loadFileAttributeFromCommand(String attrib){
		//Output file was specified..
		if (gridcommand.getAttribute(attrib) != null) {
			try {
				//Output is stored in local file..
				if (gridcommand.getAttribute("redirected").equals("true")) {
					fromFile = DesktopUtilities.getStringFromFile((String) gridcommand.getAttribute(attrib));
//					BufferedReader in = new BufferedReader(new FileReader(
//							(String) gridcommand.getAttribute(attrib)));
//					String str;
//					while ((str = in.readLine()) != null) {
//						fromFile += str+"\n";
//					}
//					in.close();
					keys.add("FromFile");
					load(null, this);
					return true;
				}
				//Output is on a remote machine..
				else{
					//TODO... Pnakaj,10/14/04,getting output file from remote location is to be done..
					fromFile = "NOTE: " +attrib+" was directed to remote machine\n\n"+
							"NOTE: Loading file from remote machine is not implemented yet";
					
					keys.add("FromFile");
					load(null, this);
					return true;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (IOException io) {
				io.printStackTrace();
				return false;
			}

		} 
		return false;
	}
	public String getFromFile(){
		return this.fromFile;
	}
}

