//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.frames;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.desktop.GridDesktopImpl;
import org.globus.cog.gridface.impl.desktop.interfaces.CoGTop;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopInternalFrame;
import org.globus.cog.gridface.impl.desktop.util.GCMLoggerTable;
import org.globus.cog.gridface.interfaces.GridCommand;

public class GCMLogFrame extends DesktopInternalFrameImpl implements StatusListener {
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GCMLogFrame.class.getName());
    
	protected CoGTop desktop = null;
	protected GCMLoggerTable gcmLogTable = null;
	
	public GCMLogFrame(GCMLoggerTable gcmLogTable){
		super(new JScrollPane(gcmLogTable),GridDesktopImpl.sLOG_GCM,true,false,true,true,true);
		this.desktop = gcmLogTable.getDesktop();
		this.gcmLogTable = gcmLogTable;
		
		
		JMenuBar gcmLogMenuBar = new JMenuBar();
		JMenu newMenu = new JMenu("Actions");
		JMenuItem newMenuItem = new JMenuItem("Remove all");
		newMenuItem.setEnabled(false);
		newMenu.add(newMenuItem);
		newMenuItem = new JMenuItem("Remove selected");
		newMenuItem.setEnabled(false);
		newMenu.add(newMenuItem);
		newMenuItem = new JMenuItem("Filter log");
		newMenuItem.setEnabled(false);
		newMenu.add(newMenuItem);

		gcmLogMenuBar.add(newMenu);
		
		setJMenuBar(gcmLogMenuBar);
		
		setSize(DesktopInternalFrame.LOGFRAME_WIDTH,DesktopInternalFrame.LOGFRAME_HEIGHT);
		desktop.addFrame(this);

	}
	
	public void update(){
		gcmLogTable.update();
		this.updateUI();
		this.repaint();
	}
	
	public void statusChanged(StatusEvent event) {
	    logger.debug("Status changed in GCM: "+event.getStatus().getStatusString());
		Status status = event.getStatus();
		final GridCommand command = (GridCommand) event.getSource();
		String identityString = command.getIdentity().toString();
		this.gcmLogTable.addCommand(command);
		desktop.info("Status changed for command: "+identityString+" to: "+event.getStatus().getStatusString());
		
		if(status.getStatusCode() == Status.COMPLETED || status.getStatusCode() == Status.FAILED){
			if(command.getExceptionString() != null){
				desktop.error("***********Command "+identityString+" EXCEPTION***********\n"+command.getExceptionString());
			}
			if(command.getOutput() != null){
			    desktop.info("***********Command "+identityString+" Out***********\n"+command.getOutput().toString());
			}
			if(command.getError()!=null){
				desktop.error("***********Command "+identityString+" Error***********\n"+command.getError());
			}
		}

	}
}
