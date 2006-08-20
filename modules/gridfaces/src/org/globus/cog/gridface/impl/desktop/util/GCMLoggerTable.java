//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
//Created on Sep 22, 2004

package org.globus.cog.gridface.impl.desktop.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.gridface.impl.desktop.GridDesktopImpl;
import org.globus.cog.gridface.impl.desktop.frames.DesktopInternalFrameImpl;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessDesktop;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessPopup;
import org.globus.cog.gridface.impl.desktop.interfaces.CoGTop;
import org.globus.cog.gridface.impl.desktop.panels.ErrorPanel;
import org.globus.cog.gridface.impl.desktop.panels.OutputPanel;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.util.ImageLoader;

public class GCMLoggerTable extends JTable implements MouseListener,AccessPopup,AccessDesktop   {
	
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GCMLoggerTable.class.getName());
    CoGTop desktop = null;
	GCMTableModel gcmTableModel = null;
	
	public static ImageIcon unknownStatusImage= new ImageIcon(ImageLoader.loadIcon("images/32x32/co/exit.png").getImage().getScaledInstance(10,10,Image.SCALE_AREA_AVERAGING));
	public static ImageIcon cancelledImage= new ImageIcon(ImageLoader.loadIcon("images/32x32/bw/button-cancel.png").getImage().getScaledInstance(10,10,Image.SCALE_AREA_AVERAGING));
	public static ImageIcon completedImage= new ImageIcon(ImageLoader.loadIcon("images/32x32/co/button-ok.png").getImage().getScaledInstance(10,10,Image.SCALE_AREA_AVERAGING));
	public static ImageIcon failImage= new ImageIcon(ImageLoader.loadIcon("images/32x32/co/button-cancel.png").getImage().getScaledInstance(10,10,Image.SCALE_AREA_AVERAGING));
	public static ImageIcon activeImage= new ImageIcon(ImageLoader.loadIcon("images/32x32/co/task.png").getImage().getScaledInstance(10,10,Image.SCALE_AREA_AVERAGING));
	public static ImageIcon suspendedImage= new ImageIcon(ImageLoader.loadIcon("images/32x32/co/exec.png").getImage().getScaledInstance(10,10,Image.SCALE_AREA_AVERAGING));
	
	
	
	protected static JLabel labelRenderer = new JLabel();
	
	public static final String READY_ERROR = "Errors";
	public static final String READY_ERROR_INFILE = "Error File";
	public static final String READY_OUTPUT_INFILE = "Output File";
	public static final String READY_OUTPUT = "View";
	public static final String READY_NOTEXT = "No Text";
	public static final String NOTREADY = "None";
	
	Vector stdOutButtons = new Vector();
	Vector stdErrorButtons = new Vector();
	
	JPopupMenu popup = null;
	
	public GCMLoggerTable(CoGTop desktop) {
		super();
		this.setDesktop(desktop);
		gcmTableModel = new GCMTableModel();
		this.setModel(gcmTableModel);
		this.addMouseListener(this);
		setDefaultRenderer(Status.class,new StatusCellRenderer() );
		this.setPopup(new JPopupMenu());
		
		labelRenderer.setHorizontalAlignment(JLabel.CENTER);
		labelRenderer.setVerticalAlignment(JLabel.CENTER);
		
//		TableCellRenderer defaultButtonRenderer = getDefaultRenderer (JButton.class);
//	    setDefaultRenderer(JButton.class,
//	                  new StdOutputRenderer(defaultButtonRenderer));

	     
		//Not allowed to move columns .. this messes up the rendering for the 
		//buttons
		getTableHeader().setReorderingAllowed(false);

	   DesktopUtilities.findTableColumn(this, GCMTableModel.STDOUT_COL).setCellRenderer(new TableButtonRenderer());
	   DesktopUtilities.findTableColumn(this, GCMTableModel.STDERROR_COL).setCellRenderer(new TableButtonRenderer());
	}


	public boolean addPopupMenuItem(JMenuItem newMenuItem) {
		this.popup.insert(newMenuItem,0);
		
		return true;
	}
	public JPopupMenu getPopup() {
		return this.popup;
	}
	public void setPopup(JPopupMenu popup) {
		this.popup = popup;
		this.configurePopup(popup);
	}
	public void showPopupAt(int xLoc, int yLoc) {
		this.popup.show(this,xLoc,yLoc);
	}
	
	public void configurePopup(JPopupMenu popup) {
		   addPopupMenuItem(new JMenuItem(new AbstractAction("Cancel"){
	   		public void actionPerformed(ActionEvent e){
	   			try{
		   			int[] rows = getSelectedRows();
		   			for (int i = 0; i < rows.length; i++) {
		   			    logger.debug("Cancelling command in row:"+rows[i]);
		   				GridCommand command = getCommandAtRow(rows[i]);
		   				if(command.getStatus().getStatusCode() == Status.ACTIVE){
		   					((GridDesktopImpl)getDesktop()).getGCM().cancel(command.getIdentity());
		   				}
					}
	   			}catch(Exception ex){ex.printStackTrace();}
	   			
	   		}
	   }));
	   
	   JMenuItem suspendMenuItem = new JMenuItem(new AbstractAction("Suspend"){
   		public void actionPerformed(ActionEvent e){
   			try{
	   			int[] rows = getSelectedRows();
	   			for (int i = 0; i < rows.length; i++) {
	   				
	   				GridCommand command = getCommandAtRow(rows[i]);
	   				if(command.getStatus().getStatusCode() == Status.ACTIVE){
	   				    logger.debug("Suspending command in row:"+rows[i]);
	   					((GridDesktopImpl)getDesktop()).getGCM().suspend(command.getIdentity());
	   				}
				}
   			}catch(Exception ex){ex.printStackTrace();}
   			
   		}
   });
	   suspendMenuItem.setEnabled(false);
	   addPopupMenuItem(suspendMenuItem);
	   
	   JMenuItem resumeMenuitem = new JMenuItem(new AbstractAction("Resume"){
   		public void actionPerformed(ActionEvent e){
   			try{
	   			int[] rows = getSelectedRows();
	   			for (int i = 0; i < rows.length; i++) {
	   				
	   				GridCommand command = getCommandAtRow(rows[i]);
	   				if(command.getStatus().getStatusCode() == Status.SUSPENDED){
	   				 logger.debug("Resuming command in row:"+rows[i]);
	   					((GridDesktopImpl)getDesktop()).getGCM().resume(command.getIdentity());
	   				}
				}
   			}catch(Exception ex){ex.printStackTrace();}
   			
   		}
   });
	   resumeMenuitem.setEnabled(false);
	   addPopupMenuItem(resumeMenuitem);

	}
	public CoGTop getDesktop() {
		return this.desktop;
	}
	public void setDesktop(CoGTop desktop) {
		this.desktop = desktop;
	}
	public void addCommand(GridCommand command){
		gcmTableModel.addCommand(command);
	}
	public GridCommand getCommandAtRow(int row){
		return gcmTableModel.getCommandAtRow(row);
	}
	public void update(){
		this.updateUI();
		this.repaint();
		gcmTableModel.fireTableDataChanged();
	}
	public void mouseClicked(MouseEvent e) {
		TableColumnModel columnModel = getColumnModel();
		int column = columnModel.getColumnIndexAtX(e.getX());
		int row = e.getY() / getRowHeight();

		if (row >= getRowCount() || row < 0 || column >= getColumnCount()
				|| column < 0){
			return;
		}

		if (SwingUtilities.isLeftMouseButton(e)) {
			if (column == GCMTableModel.STDOUT_COL) {
				((JButton) stdOutButtons.get(row)).doClick();
			} else if (column == GCMTableModel.STDERROR_COL) {
				((JButton) stdErrorButtons.get(row)).doClick();
			}
		}else if(SwingUtilities.isRightMouseButton(e)) {
			//this.setRowSelectionInterval(row,row);
			this.showPopupAt(e.getX(),e.getY());
		}

	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {

	}
	public void mousePressed(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}
	
	abstract class TableButton extends JButton implements ActionListener {
		GridCommand myCommand = null;
		protected boolean finishedUpdating;
		
		public TableButton(GridCommand command) {
			this.addActionListener(this);
			myCommand = command;
		}
		public GridCommand getCommand() {
			return this.myCommand;
		}
		public String toString() {
			return this.getText();
		}
		
		public void setText(String text) {
			super.setText(text);
			
			if(!finishedUpdating){
				if(text.equals(READY_OUTPUT)){
					if (getCommand().getOutput() instanceof String) {
						String outputBuffer = getCommand().getOutput().toString();
						if (outputBuffer.length() > 300) {
							outputBuffer = outputBuffer.substring(0, 300)
									+ "<font color=red>...</font>";
						}
						setToolTipText("<html><body><pre>" + outputBuffer
								+ "</pre></body></html>");
					}
					finishedUpdating = true;
				}
				else if(text.equals(READY_OUTPUT_INFILE)){
					setToolTipText("<html><body><pre> file:" + 
							(getCommand().getAttribute("directory")==null?"":getCommand().getAttribute("directory")+"/")
							+getCommand().getAttribute("stdoutput")
							+ "</pre></body></html>");
				}
				else if(text.equals(READY_ERROR)){
					if(getCommand().getExceptionString()!=null){
							
						String outputBuffer=getCommand().getExceptionString();
						if(outputBuffer.length() > 300){
							outputBuffer = outputBuffer.substring(0,300)+"<font color=red>...</font>";
						}
						setToolTipText("<html><body><pre>"+
								outputBuffer+"</pre></body></html>");	
					}
					finishedUpdating = true;
				}
				else if(text.equals(READY_ERROR_INFILE)){
					setToolTipText("<html><body><pre> file:" + 
							(getCommand().getAttribute("directory")==null?"":getCommand().getAttribute("directory")+"/")
							+getCommand().getAttribute("stderror")
							+ "</pre></body></html>");
				}
			}

			
			
			
		}
	}

	class StdOutputButton extends TableButton {
		public StdOutputButton(GridCommand command) {
			super(command);
		}

		public void actionPerformed(ActionEvent e) {
			if (this.getText().equals(READY_OUTPUT) || this.getText().equals(READY_OUTPUT_INFILE)) {
//TESTING
//				getDesktop().addFrame(
//						new DesktopInternalFrameImpl(new OutputPanel(myCommand),
//								"Output Monitor",true, true, true,
//								true,false), null, null, null, null);
				getDesktop().addFrame(
						new DesktopInternalFrameImpl(new OutputPanel(myCommand).getScrollContainer(),
								"Output Monitor",true, true, true,
								true,false), null, null, null, null);
				
			}
		}
	}

	class StdErrorButton extends TableButton {
		public StdErrorButton(GridCommand command) {
			super(command);

		}
		public void actionPerformed(ActionEvent e) {
			if (this.getText().equals(READY_ERROR) || 
					this.getText().equals(READY_ERROR_INFILE)) {
			    //TESTING
//				getDesktop().addFrame(
//						new DesktopInternalFrameImpl(new ErrorPanel(myCommand),
//								"Error Monitor", true, true, true,
//								true,false), null, null, null, null);
				getDesktop().addFrame(
						new DesktopInternalFrameImpl(new ErrorPanel(myCommand).getScrollContainer(),
								"Error Monitor", true, true, true,
								true,false), null, null, null, null);
				
			}
		}


	}
	class TableButtonRenderer implements TableCellRenderer{

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			TableButton button=null;
			if(column == GCMTableModel.STDOUT_COL){
				if(((String)value).equals(READY_OUTPUT)){
					button= (TableButton)stdOutButtons.get(row);
					button.setForeground(Color.BLUE);
					button.setText((String)value);
					return button;
				}
				else if(((String)value).equals(READY_OUTPUT_INFILE)){
					button= (TableButton)stdOutButtons.get(row);
					button.setForeground(Color.MAGENTA);
					button.setText((String)value);
					return button;
				}
				else{
					labelRenderer.setText((String)value);
					return labelRenderer;
				}

			}else if(column == GCMTableModel.STDERROR_COL){
				if(((String)value).equals(READY_ERROR)){
					button = (TableButton)stdErrorButtons.get(row);
					button.setForeground(Color.RED);
					button.setText((String)value);
					return button;
				}
				else if(((String)value).equals(READY_ERROR_INFILE)){
					button= (TableButton)stdErrorButtons.get(row);
					button.setForeground(Color.MAGENTA);
					button.setText((String)value);
					return button;
				}
				else{
					labelRenderer.setText((String)value);
					return labelRenderer;
				}
			}
			return null;
		}
			
	}

	class StatusCellRenderer extends JLabel implements TableCellRenderer{

		public StatusCellRenderer() {
			super();
			this.setVerticalAlignment(CENTER);
			this.setHorizontalAlignment(CENTER);
			
		}
			public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Status status = (Status)value;
			if(status.getStatusCode() == Status.COMPLETED){
				setIcon(completedImage);
			}else if(status.getStatusCode() == Status.FAILED){
				setIcon(failImage);
			}else if(status.getStatusCode() == Status.ACTIVE){
				setIcon(activeImage);
			}else if(status.getStatusCode() == Status.CANCELED){
				setIcon(cancelledImage);
			}else if(status.getStatusCode() == Status.SUSPENDED){
				setIcon(suspendedImage);
			}
			else{
				setIcon(unknownStatusImage);
			}
			return this;
		}
}

	class GCMTableModel extends AbstractTableModel{
		CommandsList gcmCommands = new CommandsList();

		//Date format
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
		
		//Columns Number. 
		public static final int STATUS_COL = 0;
		public static final int EXECUTABLE_COL = 1;
		public static final int DIRECTORY_COL = 2;
		public static final int STDOUT_COL = 3;
		public static final int STDERROR_COL = 4;
		public static final int SERVICE_COL = 5;
		public static final int IDENTITY_COL = 6;
		public static final int SUBMITTIME_COL = 7;
		public static final int COMPLETETIME_COL = 8;
		public static final int PROVIDER_COL = 9;
		public static final int NAME_COL = 10;
		
		class CommandsList extends ArrayList{
			HashSet identities = new HashSet();
			
			public void addCommand(GridCommand command){
				//Adds unique identities and update the rest
				if(!identities.contains(command.getIdentity().toString())){
					identities.add(command.getIdentity().toString());
					this.add(0,command);
					stdOutButtons.add(0,new StdOutputButton(command));
					stdErrorButtons.add(0,new StdErrorButton(command));
				}

			}
}
		//Names of the columns 
		public String[] colNames =
		{
			"Status",
			"Executable",
			"Directory",
			"StdOut",
			"StdError",
			"Service",
			"Identity",
			"Submit time",
			"Complete time",
			"Provider",
			"Name"
			};
		// Types of the columns. 
		public Class[] colTypes =
			{
				Status.class,
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
				String.class,
			};

		public void addCommand(GridCommand command){
			gcmCommands.addCommand(command);
			gcmTableModel.fireTableDataChanged();
		}
		public GridCommand getCommandAtRow(int row){
			return (GridCommand)gcmCommands.get(row);
		}
		public int getColumnCount() {
			return colNames.length;
		}

		public int getRowCount() {
			return gcmCommands.size();
		}

		public String getColumnName(int col) {
			return colNames[col];
		}

		public Class getColumnClass(int col) {
			return colTypes[col];
		}

		public Object getValueAt(int row, int col) {
			GridCommand command=(GridCommand)gcmCommands.get(row);
			switch (col) {
				case NAME_COL :
					return command.getCommand();
				case STATUS_COL :
					return command.getStatus();
				case EXECUTABLE_COL :
					return (String)command.getAttribute("executable");
				case DIRECTORY_COL:
					return (String)command.getAttribute("directory");
				case SUBMITTIME_COL :
					if (command.getSubmittedTime() != null) {
						String submitTime = sdf.format(command
							.getSubmittedTime().getTime());
						return submitTime;
					} else {
						return "Not available";
					}
				case COMPLETETIME_COL :
					if (command.getCompletedTime() != null) {
						String submitTime = sdf.format(command
							.getCompletedTime().getTime());
						return submitTime;
					} else {
						return "Not available";
					}
				case PROVIDER_COL :
					return command.getAttribute("provider");
				case SERVICE_COL :
					ServiceContact serviceContact=((ServiceContact)command.getAttribute("servicecontact"));
					return (serviceContact==null)?null:serviceContact.getHost();
				case IDENTITY_COL :
					return command.getIdentity().toString();
				case STDOUT_COL :
					
					if(command != null){
						if(command.getAttribute("stdoutput")!=null && command.getStatus().getStatusCode() == Status.COMPLETED){
							return READY_OUTPUT_INFILE;
						}
						else if(command.getOutput()!=null){
							if(command.getOutput() instanceof String){
								return READY_OUTPUT;
							}
							else{
								return READY_NOTEXT;
							}
						}
						else{
							return NOTREADY;
						}
					}
					
					
				case STDERROR_COL:
					
					if(command != null && command.getException()!=null){
							if(command.getAttribute("stderror")!=null && command.getStatus().getStatusCode() == Status.FAILED){
								return READY_ERROR_INFILE;
							}else{
								return READY_ERROR;
							}
					}
					else{
						return NOTREADY;
					}
				default:
					return null;
			}
			//return null;
		}
	}
}
