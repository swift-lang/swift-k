package org.globus.ogce.beans.filetransfer.gui;

import org.apache.log4j.Logger;
import org.globus.ogce.beans.filetransfer.FtpProperties;
import org.globus.ogce.beans.filetransfer.gui.local.DirEvent;
import org.globus.ogce.beans.filetransfer.gui.local.DirListener;
import org.globus.ogce.beans.filetransfer.gui.local.LocalTreePanel;
import org.globus.ogce.beans.filetransfer.gui.monitor.MonitorPanel;
import org.globus.ogce.beans.filetransfer.gui.monitor.QueuePanel;
import org.globus.ogce.beans.filetransfer.gui.remote.common.DisplayInterface;
import org.globus.ogce.beans.filetransfer.gui.remote.ftp.FtpClient;
import org.globus.ogce.beans.filetransfer.gui.remote.ftp.FtpDirEvent;
import org.globus.ogce.beans.filetransfer.gui.remote.ftp.FtpDirListener;
import org.globus.ogce.beans.filetransfer.gui.remote.gridftp.GridClient;
import org.globus.ogce.beans.filetransfer.gui.remote.gridftp.RemDirEvent;
import org.globus.ogce.beans.filetransfer.gui.remote.gridftp.RemDirListener;
import org.globus.ogce.beans.filetransfer.transfer.DirTransferRequest;
import org.globus.ogce.beans.filetransfer.transfer.FileRequest;
import org.globus.ogce.beans.filetransfer.transfer.RFTFileTransfer;
import org.globus.ogce.beans.filetransfer.transfer.UrlCopyFileTransfer;
import org.globus.ogce.beans.filetransfer.util.DirQueue;
import org.globus.ogce.beans.filetransfer.util.FileOperations;
import org.globus.ogce.beans.filetransfer.util.GridBrokerQueue;
import org.globus.ogce.beans.filetransfer.util.MessagePanel;
import org.globus.rft.generated.TransferType;
import org.globus.tools.ui.util.UITools;
import org.globus.transfer.reliable.client.RFTClient;
import org.globus.transfer.reliable.client.RFTJob;
import org.globus.transfer.reliable.client.RFTOptions;
import org.globus.transfer.reliable.client.RFTTransferParam;
import org.globus.transfer.reliable.client.utils.Utils;
import org.globus.util.ConfigUtil;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Properties;

/**
 * This is a panel that integrates the LocalTreeBean and RemoteTreeBean to
 * demonstrate the use of grid gui beans in the design of composite beans
 * that provide File Transfer in Grids with drag and drop functionality.
 *
 * @author Beulah Kurian Alunkal

 */
public class FileTransferMainPanel extends MainInterface
        implements DirListener,
        RemDirListener, FtpDirListener, Serializable {
    public static FileTransferMainPanel mainPanel = new FileTransferMainPanel(true);
    private static Logger logger =
            Logger.getLogger(FileTransferMainPanel.class.getName());
    protected String defaultDir = ConfigUtil.globus_dir;
    protected LocalTreePanel frame1[];
    protected DisplayInterface frame2[];

    //Counters are used to allow the users to add any no of local or remote
    //file browser beans.
    protected int remCounter = 0;
    protected int lclCounter = 0;
    private JFrame sFrame = null, mFrame = null;


    //Hashtables to retrive the ftp clients selected.
    public Hashtable gridIFTable,ftpIFTable;
    protected boolean draglocal;

    //Queues for storing the requests.
    protected GridBrokerQueue mainQueue = null;
    protected GridBrokerQueue requestQueue = null;
    protected DirQueue saveQueue = null;

    protected DisplayInterface fromRemote = null;
    protected LocalTreePanel fromLocal = null;
    protected RFTFileTransfer rftFileTransfer = null;
    protected UrlCopyFileTransfer urlcopyFileTransfer = null;
    protected FileRequest fileRequest = null;
    protected MonitorPanel monitorPanel = null;
    String from = null;

    protected JPanel panel = null;
    DirTransferRequest dirRequest = null;
    int maxSites = 10;
    //Parameters for bookmark.
    String profile, user, host, pass;
    int port = 0;
    int scH = 0, scW = 0;
    JDesktopPane desktop = null;
    protected JInternalFrame messageFrame;
    protected MessagePanel messagePanel = null;


    //RFT transfer
    public static int jobID = 0;
    private QueuePanel queuePanel = null;
	private RFTClient rftClient = null;
	public static int transferID = 1;
	
    
    /*In general for a bean default constructor*/
    public FileTransferMainPanel() {
        init();
        setLayout(new GridLayout(1, 1));
        //add(monitorPanel);
    }
    /*Static variable uses this contructor to create a hidden bean*/

    public FileTransferMainPanel(boolean val) {    	
        init();
        sFrame = new JFrame("Status Window");
        sFrame.getContentPane().setLayout(new GridLayout(1, 1));
        sFrame.getContentPane().add(monitorPanel);
        sFrame.pack();
        sFrame.setSize(350, 400);
        sFrame.setVisible(false);
        UITools.center(this,sFrame);

        mFrame = new JFrame("Messages Window");
        mFrame.getContentPane().setLayout(new GridLayout(1, 1));
        mFrame.getContentPane().add(messagePanel);
        mFrame.pack();
        mFrame.setSize(350, 400);
        mFrame.setVisible(false);
    }

    public void showStatusWindow() {
        sFrame.setVisible(true);
    }

       public void showMessagesWindow() {
        mFrame.setVisible(true);
    }
    /* Normal constructor used by all graphical interfaces */
    public FileTransferMainPanel(String label) {
        init();
    }

    public void init() {
        //	setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED),""));
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        scW = dimension.width - 90;
        scH = dimension.height - 90 * 2;
        setBounds(0, 0, scW + 50, scH);
        System.out.println("Creating a File Transfer Listener");
        //	this.panel = panel;
        //	this.desktop = desktop;
        desktop = new JDesktopPane();
        gridIFTable = new Hashtable();
        ftpIFTable = new Hashtable();

        mainQueue = new GridBrokerQueue();
        requestQueue = new GridBrokerQueue();
        saveQueue = new DirQueue();

        frame1 = new LocalTreePanel[maxSites];
        frame2 = new DisplayInterface[maxSites];

        createInitialFrames();
        rftFileTransfer = new RFTFileTransfer(this, monitorPanel);
        urlcopyFileTransfer = new UrlCopyFileTransfer(this, monitorPanel);
        fileRequest = new FileRequest(requestQueue, monitorPanel);
        System.out.println("Adding Creating a File Transfer Listener");
        desktop.putClientProperty("JDesktopPane.dragMode", "outline");
    }

    public JPanel createMonitorPanel() {
        return monitorPanel;
    }

    protected void createInitialFrames() {
        JInternalFrame mFrame = new JInternalFrame(
                "Monitor Frame"
                , true, false, true, true);
        mFrame.setVisible(true);        
        mFrame.getContentPane().setLayout(new GridLayout(1, 1));
        monitorPanel = new MonitorPanel(this);
        mFrame.getContentPane().add(monitorPanel);
        mFrame.pack();
        mFrame.setSize(350, 330);
        //dimensionnew.width / 5 - 30, dimensionnew.height - 95);
        mFrame.setLocation(5, 10);
        
        desktop.add(mFrame);

        createNewLocalFrame();

        String newline = new String(System.getProperty("line.separator"));
        String msgTxt = new String("File Transfer Component"+ newline +
                 "Here  the Server messages be shown"
                 + newline);


        messageFrame = new JInternalFrame(
                "Message Window"
                , true, false, true, true);
        messageFrame.setVisible(true);
        
        messageFrame.getContentPane().setLayout(new GridLayout(1, 1));
        messagePanel = new MessagePanel(msgTxt);
        messageFrame.getContentPane().add(messagePanel);
        messageFrame.pack();
        messageFrame.setLocation(5, 340);
        messageFrame.setSize(350, 135);
        //messageFrame.setVisible(true);
        desktop.add(messageFrame);
        
        rftClient = new RFTClient(null, monitorPanel.getUrlCopyPanel().getQueuePanel());

    }

    public MonitorPanel getMonitorFrame() {
        return monitorPanel;
    }

    public void processRequest() {
        fileRequest.updateQueue(dirRequest);
    }

    public void msgOut(String s) {
        messagePanel.msgOut(s);
    }

    public void clearQueue(String provider) {
        logger.info("Invoked the clearQueue in main");
        logger.info("\nPlease wait... Clearing the queues.");
        if (provider.equals("rft")) {
            rftFileTransfer.clearAllQueues();
        } else if (provider.equals("urlcopy")) {
            urlcopyFileTransfer.clearAllQueues();
        } else {
            fileRequest.clearRequestQueue();
        }
    }


    public void controlExecutionQueue(boolean start, String provider) {
        if (!start) {
            JOptionPane.showMessageDialog(panel, "Will stop the job execution" +
                    " only after the current active job is done. ");
        } else {
            logger.info("Calling the start transfer ");
        }
        if (provider.equals("rft")) {
            rftFileTransfer.setControl(start);
        } else if (provider.equals("urlcopy")) {
            urlcopyFileTransfer.setControl(start);
        } else {
            fileRequest.setControl(start);
        }
    }

    public JDesktopPane getDesktopPane() {
        return desktop;
    }

    public void saveQueueToFile(String provider) {
        JFileChooser fileChooser = new JFileChooser(defaultDir);
        fileChooser.setApproveButtonText("Save");
        fileChooser.setDialogTitle("Save Jobs to File");
        int popdownState = fileChooser.showSaveDialog(panel);
        if (popdownState == JFileChooser.CANCEL_OPTION) {
            return;
        } else {
            File saveFile = fileChooser.getSelectedFile();
            //writing the default job file name into ftp.properties
            FtpProperties props = null;
            try {
                props = new FtpProperties(FtpProperties.configFile);
                if (provider.equals("rft")) {
                    props.setRFTFile(saveFile.getAbsolutePath());
                } else {
                    props.setQueueFile(saveFile.getAbsolutePath());
                }
                logger.info("\nThe Queue file default location saved="
                        + props.getQueueFile());
                props.save(FtpProperties.configFile);

            } catch (Exception e) {
                logger.debug("The system could not open the specified file\n");
            }
            saveQueueToFile(saveFile, provider);

        }

    }

    public void saveQueueToFile(File saveFile, String provider) {
        GridBrokerQueue saveQueue = null;
        String displayString = null;
        if (provider.equals("rft")) {
            saveQueue = rftFileTransfer.getSaveQueue();
            displayString = "RFT Jobs: ";
        } else {
            saveQueue = urlcopyFileTransfer.getSaveQueue();
            displayString = "Local Provider Jobs: ";
        }
        if (saveQueue.size() > 0) {
            int savedJobsCount = FileOperations.saveQueueToFile(saveQueue,
                    saveFile,
                    this,
                    provider);
            JOptionPane.showMessageDialog(panel, displayString +
                    "Successfully saved   " +
                    savedJobsCount + " jobs  " +
                    " are not completed yet.");
        } else {
            JOptionPane.showMessageDialog(panel, displayString + "All jobs are done." +
                    " Nothing needs to be saved ");
            saveFile.delete();
        }
    }

    public void setSaveQueue(GridBrokerQueue saveQueue, String provider) {
        if (provider.equals("rft")) {
            rftFileTransfer.setSaveQueue(saveQueue);
        } else {
            urlcopyFileTransfer.setSaveQueue(saveQueue);
        }
    }


    public void loadQueueFromFile(String provider) {
        JFileChooser fileChooser = new JFileChooser(defaultDir);
        fileChooser.setApproveButtonText("Load");
        fileChooser.setDialogTitle("Load Jobs File");
        int popdownState = fileChooser.showOpenDialog(panel);
        if (popdownState == JFileChooser.CANCEL_OPTION) {
            return;
        } else {

            File loadFile = fileChooser.getSelectedFile();
            monitorPanel.clear(provider);
            loadQueueFromFile(loadFile, false, provider);
        }
    }

    public void loadQueueFromFile(File loadFile, boolean startup, String provider) {
        FileOperations fop = new FileOperations();
        int filesCount = fop.loadQueueFromFile(loadFile, this, provider);
        String msg = null;
        if (filesCount > 0) {
            msg = "Successfully loaded " + filesCount + " jobs";

        } else {
            msg = "Trying to load an invalid file. Each line in the file should consists of fromUrl, \n" +
                    " toUrl and job id separated by semicolon. ";
            JOptionPane.showMessageDialog(panel, msg);
        }
    }


    public JPanel createNewLocalFrame() {
        if (lclCounter >= maxSites) {
            String msg = "You cannot open more than 10 local windows" +
                    " at the same time. \n Please close few of them" +
                    " to open new ones.";
            JOptionPane.showMessageDialog(panel, msg);
            return null;
        }

        frame1[lclCounter] = new LocalTreePanel("Local File System");
        frame1[lclCounter].addDirListener(this);
        JInternalFrame localIF = new JInternalFrame("Local System",
                true, true, true, true);
        localIF.setVisible(true);
        localIF.getContentPane().setLayout(new GridLayout(1, 1));
        localIF.getContentPane().add(frame1[lclCounter]);
        localIF.pack();

        localIF.setSize(300, 450);
        localIF.setLocation(scW / 3 + 50 + (lclCounter) * 20,
                10 + (lclCounter) * 20);
        localIF.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                lclCounter--;
                System.gc();
            }
        });
        desktop.add(localIF);
        try {
            localIF.setSelected(true);
        } catch (PropertyVetoException propertyvetoexception1) {
            propertyvetoexception1.printStackTrace();
        }
        JPanel localPanel = frame1[lclCounter];
        System.out.println("Local count incremented.");
        lclCounter++;
        return localPanel;
    }

    public void registerLocalComponent(LocalTreePanel local) {
        frame1[lclCounter] = local;
        frame1[lclCounter].addDirListener(this);
        lclCounter++;
    }

    public void registerRemoteComponent(DisplayInterface display, int index) {
        if (index == 1) {
            GridClient gc = (GridClient) display;
            gc.addRemDirListener(this);
            frame2[remCounter] = gc;
        } else {
            FtpClient fc = (FtpClient) display;
            fc.addFtpDirListener(this);
            frame2[remCounter] = fc;
        }

        remCounter++;
    }

    /**
     * Creates a frame for remote file transfer client either ftp or gridftp
     * based on the request.
     *
     * @param ftpindex indicates the client
     */

    public JPanel createRemoteFrame(int ftpindex, String host,
                                    int port) {
        return createRemoteFrame(ftpindex, host, port, "anonymous", "password");
    }

    public JPanel createRemoteFrame(int ftpindex, String host, int port,
                                    String user, String pwd) {
        if (remCounter >= maxSites) {
            String msg = "You cannot open more than 10 remote windows" +
                    " at the same time. \n Please close few of them" +
                    " to open new ones.";
            JOptionPane.showMessageDialog(panel, msg);
            return null;
        }
        if (ftpindex == 1) {
            frame2[remCounter] = new GridClient("Remote System -GridFTP ");
            final GridClient gc = (GridClient) frame2[remCounter];
            gc.addRemDirListener(this);
            final JInternalFrame gridIF = new JInternalFrame(
                    "Remote System -GridFTP"
                    , true, true, true, true);
            gridIF.setVisible(true);
            gridIF.getContentPane().setLayout(new GridLayout(1, 1));
            gridIF.getContentPane().add(gc);
            gridIF.pack();
            JFrame topLevelFrame = (JFrame)getDesktopPane().getTopLevelAncestor();
            int frameWidth = topLevelFrame.getWidth();
            int frameHeight = topLevelFrame.getHeight();
            gridIF.setSize(frameWidth / 4, frameHeight / 2);
            gridIF.setLocation((topLevelFrame.getX() + frameWidth / 5) - remCounter * 20, topLevelFrame.getY() + frameHeight / 10 + remCounter * 20);
            //gridIF.setSize(300, 450);
            //dimensionnew.width / 5 - 30, dimensionnew.height - 95);
            //gridIF.setLocation(scW / 2 + 210 - (remCounter) * 20, 10 + (remCounter) * 20);


            gridIF.addInternalFrameListener(new InternalFrameAdapter() {
                public void internalFrameClosing(InternalFrameEvent e) {
                    gc.disconnect();
                    //	    gridIF = null;
                    System.gc();
                }
            });
            desktop.add(gridIF);
            try {
                gridIF.setSelected(true);
            } catch (PropertyVetoException propertyvetoexception1) {
                propertyvetoexception1.printStackTrace();
            }
            gridIFTable.put(gc, gridIF);
            remCounter++;
            if (host != null) {
                gc.setHost(host);
                if (port != 0) {
                    gc.setPort(port);
                }
                if (!gc.setConnectDetails(false)) {
                    return null;
                }

            } else {
                gc.connectDlg(null);
                UITools.center(panel, gc.dlgConnect);
            }
            return gc;
        } else if (ftpindex == 3) {
            frame2[remCounter] = new FtpClient("Remote System -FTP");
            final FtpClient fc = (FtpClient) frame2[remCounter];
            fc.addFtpDirListener(this);
            JInternalFrame ftpIF = new JInternalFrame(
                    "Remote System -FTP",
                    true, true, true, true);
            ftpIF.getContentPane().setLayout(new GridLayout(1, 1));
            ftpIF.getContentPane().add(fc);
            ftpIF.pack();
            ftpIF.setSize(300, 450);
            //dimensionnew.width / 5 - 30, dimensionnew.height - 95);
            ftpIF.setLocation(scW / 2 + 210 - (remCounter) * 20,
                    10 + (remCounter) * 20);

            ftpIF.addInternalFrameListener(new InternalFrameAdapter() {
                public void internalFrameClosing(InternalFrameEvent e) {
                    fc.remoteTreeFrame._actionDisconnect();
                    //	    gridIF = null;
                    System.gc();
                }

            });
            desktop.add(ftpIF);
            try {
                ftpIF.setSelected(true);
            } catch (PropertyVetoException propertyvetoexception1) {
                propertyvetoexception1.printStackTrace();
            }
            ftpIFTable.put(fc, ftpIF);
            ftpIF.setVisible(true);
            remCounter++;
            if (host != null) {
                fc.setHost(host);
                if (port != 0) {
                    fc.setPort(port);
                }
                if (user != null) {
                    fc.setUser(user);
                }
                if (user != null) {
                    fc.setPwd(pwd);
                }
                if (!fc.setConnectDetails(false)) {
                    return null;
                }
            } else {
                fc.connectDlg(null);
                UITools.center(panel, fc.dlgConnect);
            }
            return fc;
        } else {
            logger.info("Extended Feature ");
            return null;
        }
    } // end of createRemoteFrame()

    /**
     * Methods to listen to the drag and drop events.
     *
     */
    public void dragLocal(DirEvent e, String from, LocalTreePanel local) {
    	String rftEnabled = Utils.getProperty("rft_enabled", "rft.properties");
    	if ("true".equals(rftEnabled)) {
    		JOptionPane.showMessageDialog(null, "RFT is enabled, you can not transfer between local machine" +
    				"and GridFTP server", "Error", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
        draglocal = true;
        fromLocal = local;
        this.from = from;
        logger.info("Dragging  Local files...");
    }

    public void dragGridFtp(RemDirEvent e, String from,
                            GridClient gc) {
    	System.out.println("dragGridFTP");
        this.from = from;
        draglocal = false;
        fromRemote = gc;
        logger.info("Dragging monitorPanel file...");
    }

    public void dragFtp(FtpDirEvent e, String from,
                        FtpClient fc) {
        this.from = from;
        draglocal = false;
        fromRemote = fc;
        logger.info("From Remote =" + fromRemote.getRootURL());
        logger.info("Dragging Ftp file...");
    }

    public void dropLocal(DirEvent e, String to, LocalTreePanel toLocal) {

        boolean dropLocal = true;
        logger.info("Dropping the file...");
        dirRequest = new DirTransferRequest(this,
                fromLocal, toLocal,
                fromRemote, null,
                from, to,
                draglocal, dropLocal,
                monitorPanel.getProvider());
       
        new AlertThread(this).start();


    }//end of the function

    public void dropGridFtp(RemDirEvent e, String to, GridClient toRemote) {    	
        dirRequest = new DirTransferRequest(this,
                fromLocal, null,
                fromRemote, toRemote,
                from, to,
                draglocal, false,
                monitorPanel.getProvider());  
              
        //----------------invode rft to transfer the file--------------------------------------
        Properties prop = getRFTProperties("rft.properties");
        if (null != prop) {
        	String rftEnabled = prop.getProperty("rft_enabled");
            
            //using RFT
            if ("true".equals(rftEnabled)) {
            	String fromHost = fromRemote.getHost();
            	String fromPort = Integer.toString(fromRemote.getPort());
            	String toHost = toRemote.getHost();
            	String toPort = Integer.toString(toRemote.getPort());
            	String destSN = toRemote.getSubject();
                String sourceSN = ((GridClient)fromRemote).getSubject();
            	RFTWorker worker = new RFTWorker(prop, from, fromHost, fromPort, 
            			to, toHost, toPort, sourceSN, destSN);
                worker.start();
            } else { 
            	new AlertThread(this).start();
            }
        } else { 
        	new AlertThread(this).start();
        }
        
        
        //----------------invoke rft end-------------------------------------------------------
        
    }


    public void dropFtp(FtpDirEvent e, String to, FtpClient toRemote) {
        dirRequest = new DirTransferRequest(this,
                fromLocal, null,
                fromRemote, toRemote,
                from, to,
                draglocal, false,
                monitorPanel.getProvider());
        new AlertThread(this).start();
    }

    private Properties getRFTProperties(String fileName) {
    	String globusDir = System.getProperty("user.home") + File.separator + ".globus";
		File dir = new File(globusDir, "GridFTP_GUI");
		File propFile = new File(dir, fileName);
    	if (!propFile.exists()) {
    		return null;
    	}
    	FileInputStream fis = null;
    	Properties prop = new Properties();
    	try {
    		fis = new FileInputStream(propFile);
        	prop.load(fis);    			 
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		try {
				fis.close();
			} catch (IOException e) {				
			}
    	}
    	
    	return prop;
    }
    /**
     * Methods to increment the counter to keep track of the no of remote
     * clients currently active.
     */
    public void ftpCounter(FtpDirEvent e, FtpClient gc) {
        if (remCounter > 0) {
            JInternalFrame gridframe = (JInternalFrame) ftpIFTable.get(gc);
            gridframe.setVisible(false);
            remCounter--;
            logger.info("\nFtp site is disconnected. ");
        }

    }

    public void gridCounter(RemDirEvent e, GridClient gc) {
        if (remCounter > 0) {
            JInternalFrame gridframe = (JInternalFrame) gridIFTable.get(gc);
            gridframe.setVisible(false);
            remCounter--;
            logger.info("\nGridFTP site is disconnected. ");
        }

    }

    public void startActualTransfer(String provider) {
        if (provider.equals("rft")) {
            rftFileTransfer.run();
        } else {
            urlcopyFileTransfer.run();
        }

    }

    public void startActualTransfer(GridBrokerQueue requestQueue,
                                    String provider) {
        System.out.println("TESTING : Request Queue file size=" + requestQueue.size());
        //monitorPanel.setFocusTab(1);
        if (provider.equals("rft")) {
            monitorPanel.setFocusTab(1);
            rftFileTransfer.updateQueues(requestQueue);
            monitorPanel.setFocusTab(1);
        } else
        {
            monitorPanel.setFocusTab(0);
            urlcopyFileTransfer.updateQueues(requestQueue);
        }
    }

    public FileTransferMainPanel(JPanel panel, JDesktopPane desktop) {
        this();
    }

    class RFTWorker extends Thread {
    	Properties prop = null;
    	String from;
    	String fromHost; 
    	String fromPort;
    	String to;
    	String toHost;
		String toPort; 
		String sourceSN; 
		String destSN;
		
    	public RFTWorker(Properties prop, String from, String fromHost, String fromPort, 
    			String to, String toHost, String toPort, String sourceSN, String destSN) {
    		this.prop = prop;
    		this.from = from;
    		this.fromHost = fromHost;
    		this.fromPort = fromPort;
    		this.to = to;
    		this.toHost = toHost;
    		this.toPort = toPort;
    		this.sourceSN = sourceSN;
    		this.destSN = destSN;
    	}
    	
    	public void run() {
    		String rftFrom = "gsiftp://" + fromHost + ":" + fromPort + from;
            String rftTo = "gsiftp://" + toHost + ":" + toPort + to;         
            
            RFTTransferParam param = new RFTTransferParam(rftFrom, rftTo, 
            		prop.getProperty("host"), prop.getProperty("port"));
            int concurrent = Integer.parseInt(prop.getProperty("concurrent"));
            int parallelStream = Integer.parseInt(prop.getProperty("parallelstream"));
            int bufferSize = Integer.parseInt(prop.getProperty("tcpbuffersize"));
    		RFTOptions options = new RFTOptions(concurrent, parallelStream, bufferSize, 
    				destSN, sourceSN);
            RFTJob job = new RFTJob(++FileTransferMainPanel.jobID, options, param);
            TransferType transfer = param.getTransfers1()[0]; 
            monitorPanel.setFocusTab(0);
            monitorPanel.getUrlCopyPanel().addTransfer(Integer.toString(FileTransferMainPanel.jobID), 
            		rftFrom, rftTo, "true");            
            
            try {
            	//queuePanel.addTransfer(cols);        	
        		rftClient.startTransfer(job);        		
            } catch (Exception e1) {
            	e1.printStackTrace();
            	JOptionPane.showMessageDialog(null,e1.getMessage(), "Error",
                        JOptionPane.WARNING_MESSAGE);
            	monitorPanel.getUrlCopyPanel().updateTransfer(Integer.toString(FileTransferMainPanel.jobID), 
            			"Failed", "N/A", "N/A", e1.getMessage());
               
            }
    	}
    }
    
}
