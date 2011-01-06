//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.icons;
/*
 * Icon implementation for Desktop.
 */

//Local imports
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.commands.EXECCommandImpl;
import org.globus.cog.gridface.impl.desktop.GridDesktopImpl;
import org.globus.cog.gridface.impl.desktop.dnd.DesktopIconTransferable;
import org.globus.cog.gridface.impl.desktop.frames.DesktopInternalFrameImpl;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopIcon;
import org.globus.cog.gridface.impl.desktop.panels.AbstractFormPanel;
import org.globus.cog.gridface.impl.desktop.panels.JobMonitorPanel;
import org.globus.cog.gridface.impl.desktop.panels.JobSpecificationPanel;
import org.globus.cog.gridface.impl.desktop.panels.ServiceContactPanel;
import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;
import org.globus.cog.gridface.impl.desktop.util.ObjectPair;
import org.globus.cog.gridface.impl.util.LoggerImpl;
import org.globus.cog.gridface.interfaces.GridCommand;

public class GridIconImpl extends AbstractIcon implements StatusListener {
	/** Command used to submit jobs to grid from this icon*/
	public GridCommand command = null;
	public ServiceContact serviceContact = null;
	
	/** Frame used to show job output */
	protected DesktopInternalFrameImpl jobMonitorFrame = null;
	
	public static final String sNEWICON = "New Grid Icon";
	public static final String sRUNTASK = "Run Task";
	
	//Grid Icon types
	public static final String JOB_SUBMISSION =
		"org.globus.cog.gridface.impl.desktop.icons.GridIconImpl:JOB_SUBMISSION";
	public static final String JOB_SPECIFICATION =
		"org.globus.cog.gridface.impl.desktop.icons.GridIconImpl:JOB_SPECIFICATION";
	public static final String SERVICE =
		"org.globus.cog.gridface.impl.desktop.icons.GridIconImpl:SERVICE";

	protected boolean[][] canImportMatrix = {
			//[SERVICE,JOB_SUBMISSION,JOB_SPECIFICATION] (Drop Icon)
			{false,false,false},	//SERVICE (Drag Icon)
			{true,true,false},		//JOB_SUBMISSION (Drag Icon)
			{true,false,true},		//JOB_SPECIFICATION (Drag Icon)
			{false,true,true}		//NATIVE - RSL FILE(Drag Icon)
	};
	
	public GridIconImpl() throws Exception {
		this(null, null, sNEWICON, JOB_SPECIFICATION, null);
	}
	public GridIconImpl(
		String applicationClass,
		ObjectPair arguments,
		String text,
		String iconType)
		throws Exception {
		this(applicationClass, arguments, text, iconType, null);

	}
	public GridIconImpl(
		String applicationClass,
		ObjectPair arguments,
		String text,
		String iconType,
		String iconImage)
		throws Exception {
		super(applicationClass, arguments, text, iconType, iconImage);
		
		if(iconType != JOB_SPECIFICATION){
			iconAttributes.setAttribute("provider","GT2");
			iconAttributes.setAttribute("servicecontactname","wiggum.mcs.anl.gov");
			iconAttributes.setAttribute("servicecontactport","2119");
		}
		if(iconType != SERVICE){
			iconAttributes.setAttribute("executable","/bin/date");
			iconAttributes.setAttribute("redirected","true");
			iconAttributes.setAttribute("localexecutable","false");
			iconAttributes.setAttribute("batchjob","false");
		}
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessIconProperties#getGenericIconImageURI()
	 */
	public String getDefaultIconImageURI() {
		if(this.iconType.equals(JOB_SUBMISSION)){
			return "images/32x32/co/kcmsystem.png";
		}else if(this.iconType.equals(JOB_SPECIFICATION)){
			return "images/32x32/co/jobspec-icon.png";
		}else if(this.iconType.equals(SERVICE)){
			return "images/32x32/co/konsole2.png";
		}else{
			return "images/32x32/co/window-globus-ogsa.png";
		}
	}

	public String getDefaultIconText() {
		if(this.iconType.equals(JOB_SUBMISSION)){
			return "Job Sub:"+this.getId();
		}else if(this.iconType.equals(JOB_SPECIFICATION)){
			return "Job Spec:"+this.getId();
		}else if(this.iconType.equals(SERVICE)){
			return "Service:"+this.getId();
		}else{
			return super.getDefaultIconText();
		}
	}
	public void launch() {
		if(getIconType().equals(JOB_SUBMISSION) && command != null){
			if(jobMonitorFrame != null){
				getDesktop().removeFrame(jobMonitorFrame);
			}
			//TESTING
			//getDesktop().addFrame(new DesktopInternalFrameImpl(new JobMonitorPanel(command),"Job monitor",true,true,true,true,false));
			getDesktop().addFrame(new DesktopInternalFrameImpl(new JobMonitorPanel(command).getScrollContainer(),"Job monitor",true,true,true,true,false));
			return;
		}
		else {
			showIconProperties();
		}
	}

	public void configurePopup(JPopupMenu popup) {
		super.configurePopup(popup);
		if(this.iconType.equals(JOB_SUBMISSION)){
			popup.insert(new ExecuteGridTaskAction(),0);
			popup.insert( new JPopupMenu.Separator(),1);
			//TODO add cancel
		}

	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.desktop.interfaces.AccessPropertiesPanel#getPropertiesPanel()
	 */
	public JComponent getPropertiesPanel() {
		this.propertiesPanelSet.clear();
		
		//TESTING
//		JobSpecificationPanel jobF = new JobSpecificationPanel();
//		jobF.load(null,iconAttributes);
//		ServiceContactPanel serviceC = new ServiceContactPanel();
//		serviceC.load(null,iconAttributes);
		
		JobSpecificationPanel jobF = new JobSpecificationPanel();
		jobF.load(null,iconAttributes);

		
		ServiceContactPanel serviceC = new ServiceContactPanel();
		serviceC.load(null,iconAttributes);

		
		if(this.iconType.equals(JOB_SPECIFICATION)){
			propertiesPanelSet.addForm(jobF);
		}else if(this.iconType.equals(SERVICE)){
			propertiesPanelSet.addForm(serviceC);
		}else if(this.iconType.equals(JOB_SUBMISSION)){
			propertiesPanelSet.addForm(serviceC);
			propertiesPanelSet.addForm(jobF);
			propertiesPanelSet.addButtonAction(new AbstractAction("Execute") {
				public void actionPerformed(ActionEvent e) {
					processPropertiesOKButton();
					executeGridTask();
				}
			});
		}
		//TESTING
		//propertiesPanelSet.addForm((SimpleFormPanel)super.getPropertiesPanel());
		propertiesPanelSet.addForm((AbstractFormPanel)super.getPropertiesPanel());
		
		this.propertiesPanelSet.addButtonAction(new PropertiesPanel_OKButtonAction());
		this.propertiesPanelSet.addButtonAction(new PropertiesPanel_CancelButtonAction());
		this.propertiesPanelSet.finishedAddingButtonActions();
		
		//TESTING
		return propertiesPanelSet;
		//return propertiesPanelSet.getScrollContainer();

	}

	protected int getCanImportMatrixIndex(String type){
		if(type.equals(SERVICE)){
			return 0;
		}
		if(type.equals(JOB_SUBMISSION)){
			return 1;
		}
		if(type.equals(JOB_SPECIFICATION)){
			return 2;
		}
		if(type.equals(GenericIconImpl.NATIVE)){
			return 3;
		}
		return -1;
	}
	public boolean canImportTypePair(String type1,String type2){
		int row = getCanImportMatrixIndex(type1);
		int col = getCanImportMatrixIndex(type2);
		return canImportMatrix[row][col];
	}

	public boolean importDataToComponent(JComponent dropComponent,
			Transferable t, JComponent dragComponent, Point dragPoint,
			Point dropPoint) {

		try {
			DesktopIconGroup iconGroup = (DesktopIconGroup) t
					.getTransferData(DesktopIconTransferable.groupIconDataFlavor);

			if (this.iconType.equals(SERVICE)) {
				for (Iterator iter = iconGroup.iterator(); iter.hasNext();) {
					DesktopIcon element = (DesktopIcon) iter.next();
					//According to canImport matrix only icons that make it till here are JOB_SPECIFICATION and 
					//JOB_SUBMISSION
					if (element instanceof GridIconImpl) {
						String serviceContactPort = (String)iconAttributes.getAttribute("servicecontactport");
						String serviceContactName = (String)iconAttributes.getAttribute("servicecontactname");
						String provider = (String)iconAttributes.getAttribute("provider");
						
						GridIconImpl gridIcon = (GridIconImpl) element;
					
						if(serviceContactName != null){
							gridIcon.getAttributesHolder().setAttribute("servicecontactname",serviceContactName);
						}
						if(serviceContactPort!=null){
							gridIcon.getAttributesHolder().setAttribute("servicecontactport",serviceContactPort);
						}
						if(provider!=null){
							gridIcon.getAttributesHolder().setAttribute("provider",provider);
						}

						gridIcon.executeGridTask();
					}

				}
				return super.importDataToComponent(dropComponent, t,
						dragComponent, dragPoint, dropPoint);
				
			} else if (this.iconType.equals(JOB_SPECIFICATION) ||
					this.iconType.equals(JOB_SUBMISSION)) {
				//Make sure all icons being imported are atleast all of type
				//NATIVE
				//The native file used will be the first one from iconGroup
				//if size of iconGroup is more than 1
				if (iconGroup.areAllIconsOfType(GenericIconImpl.NATIVE) && !iconGroup.isEmpty()) {
					//TODO load this icon according to RSL file
					GenericIconImpl genIcon = (GenericIconImpl)iconGroup.get(0);
					String rslFileName = genIcon.getExecutable();
					if(DesktopUtilities.loadRSLToIconAttributes(rslFileName,this)){
						return super.importDataToComponent(dropComponent, t,
								dragComponent, dragPoint, dropPoint);
					}
				}
				//Else copy job attributes to the drop icon
				else{
					for (Iterator iter = iconGroup.iterator(); iter.hasNext();) {
						DesktopIcon element = (DesktopIcon) iter.next();
						if (element instanceof GridIconImpl) {
							GridIconImpl gridIcon = (GridIconImpl) element;
							this.iconAttributes.setAttributes((Hashtable)gridIcon.iconAttributes.getAttributes().clone());
						}

					}
					return super.importDataToComponent(dropComponent, t,
							dragComponent, dragPoint, dropPoint);
				}
			} else {
				return false;
			}
		}  catch (Exception ioe) {
			getDesktop().error(LoggerImpl.getExceptionString(ioe));
			ioe.printStackTrace();
		} 
		return false;
	}

	class ExecuteGridTaskAction extends AbstractAction {
		public ExecuteGridTaskAction() {
			super(sRUNTASK);
		}
		public void actionPerformed(ActionEvent e) {
			executeGridTask();
		}
	}

	public boolean executeGridTask() {
		if (iconType.equals(JOB_SUBMISSION)
				|| iconType.equals(JOB_SPECIFICATION)) {
			command = new EXECCommandImpl();
			command.addStatusListener(this);
			command.setAttributes((Hashtable) this.iconAttributes
					.getAttributes().clone());
			
			String serviceContactPort = (String)iconAttributes.getAttribute("servicecontactport");
			String serviceContactName = (String)iconAttributes.getAttribute("servicecontactname");
			//String provider = (String)iconAttributes.getAttribute("provider");
			
			if (serviceContactPort != null) {
				serviceContact = new ServiceContactImpl(serviceContactName
						+ ":" + serviceContactPort);
			} else {
				serviceContact = new ServiceContactImpl(serviceContactName);
			}
			command.setAttribute("servicecontact", serviceContact);

			try {
				((GridDesktopImpl) getDesktop()).getGCM()
						.execute(command, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.abstraction.interfaces.StatusListener#statusChanged(org.globus.cog.abstraction.impl.common.StatusEvent)
	 */
	public synchronized void statusChanged(StatusEvent event) {
		Status status = event.getStatus();

		if (status.getStatusCode() == Status.COMPLETED) {
			this.removeAllOverlays();
			this.addOverlay("images/32x32/co/button-ok.png",true);
		} else if (status.getStatusCode() == Status.FAILED) {
			this.removeAllOverlays();
			this.addOverlay("images/32x32/co/button-cancel.png",true);
		}else if (status.getStatusCode() == Status.ACTIVE) {
			this.removeAllOverlays();
			this.addOverlay("images/32x32/co/task.png",true);
		}else if (status.getStatusCode() == Status.CANCELED) {
			this.removeAllOverlays();
			this.addOverlay("images/32x32/bw/button-cancel.png",true);
		}else if (status.getStatusCode() == Status.SUSPENDED) {
			this.removeAllOverlays();
			this.addOverlay("images/32x32/bw/exec.png",true);
		}
		else{
			this.removeAllOverlays();
			this.addOverlay("images/32x32/co/exit.png",true);
		}
	}
}
