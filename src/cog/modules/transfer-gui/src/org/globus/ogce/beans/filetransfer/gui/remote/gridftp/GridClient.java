package org.globus.ogce.beans.filetransfer.gui.remote.gridftp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.globus.common.CoGProperties;
import org.globus.ftp.*;
import org.globus.ftp.exception.ServerException;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.IdentityAuthorization;
import org.globus.ogce.beans.filetransfer.gui.FileTransferMainPanel;
import org.globus.ogce.beans.filetransfer.gui.monitor.WindowProgress;
import org.globus.ogce.beans.filetransfer.gui.remote.common.DisplayInterface;
import org.globus.ogce.beans.filetransfer.gui.remote.common.GridEvent;
import org.globus.ogce.beans.filetransfer.gui.remote.common.GridListener;
import org.globus.ogce.beans.filetransfer.gui.remote.common.RemoteTreeFrame;
import org.globus.ogce.beans.filetransfer.util.DirQueue;
import org.globus.tools.ui.util.UITools;
import org.ietf.jgss.GSSCredential;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Vector;


public class GridClient extends JPanel implements PropertyChangeListener, DisplayInterface, GridListener, ActionListener, Runnable, Serializable {
    private static Log logger = LogFactory.getLog(GridClient.class.getName());
    
    private GridFTPClient client;
    private GridFTPClient client1;
    private GridFTPClient client2;
    private GridFTPClient client3;
    WindowProgress wndPreload = null;

    private final int GRID_FTP_PORT = 2811;

    public String host;

    private int port;

    private String profile;

    private String subject = null;
    
    public RemoteTreeFrame remoteTreeFrame;

    public String remoterootDir;

    public boolean isConnected = false;

    public boolean busy;


    private boolean put = false;


    protected JComboBox txtHost;

    protected JTextField txtPort;

    protected JTextField txtUName;

    protected JTextField txtprofileName;
    
    protected JTextField txtSubject;

    public JDialog dlgConnect;

    private String FileSep;

    private GridBagConstraints gbc;

    private DirQueue queue;

    private String status = null, url = null;

    private boolean bean = false;
    protected Vector remdirlisteners = new Vector();
    public static String subject1;
    /** Register an action listener to be notified when a button is pressed */
    public void addRemDirListener(RemDirListener l) {
        remdirlisteners.addElement(l);
    }

    /** Remove an Answer listener from our list of interested listeners */
    public void removeRemDirListener(RemDirListener l) {
        remdirlisteners.removeElement(l);
    }

    /** Send an event to all registered listeners */
    public void fireRemDirEvent(RemDirEvent e, DirQueue dirqueue, String path, GridClient gc) {
        // Make a copy of the list and fire the events using that copy.
        Vector list = (Vector) remdirlisteners.clone();
        for (int i = 0; i < list.size(); i++) {
            RemDirListener listener = (RemDirListener) list.elementAt(i);
            try {
                switch (e.getID()) {
                    case RemDirEvent.DIR:
                        listener.dropGridFtp(e, path, gc);
                        break;
                    case RemDirEvent.REMOTEDRAG:
                        listener.dragGridFtp(e, path, gc);
                        break;
                    case RemDirEvent.COUNTER:
                        listener.gridCounter(e, gc);
                        break;

                }
            } catch (Exception direx) {
                direx.printStackTrace();
            }
        }
    }

    public GridClient() {
        this(null, true);
        bean = true;
        FileTransferMainPanel.mainPanel.registerRemoteComponent(this, 1);
        remoteTreeFrame.statusOut("Status: Not connected");
     }

    public GridClient(String s) {
        this(s, false);
    }

    public GridClient(String s, boolean bean) {

        remoteTreeFrame = new RemoteTreeFrame(this, bean);
        remoteTreeFrame.setVisible(true);
        setLayout(new GridLayout(1, 1));
        add(remoteTreeFrame);
        setVisible(true);

        remoteTreeFrame.addPropertyChangeListener(this);

        remoteTreeFrame.addGridListener(this);

        txtHost = null;
        txtPort = null;
        host = null;
        port = GRID_FTP_PORT;
        FileSep = null;

        isConnected = false;
        dlgConnect = null;
        gbc = new GridBagConstraints();
        queue = new DirQueue();
        remoteTreeFrame.setProtocol("gsiftp");
        remoteTreeFrame.setPort(GRID_FTP_PORT);
      }

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public void setStatus(String status) {
        this.status = status;
        remoteTreeFrame.statusOut(status);
    }

    public void setUrl(String url) {
        this.url = url;
        remoteTreeFrame.setUrl(url);
    }
    
    private JComboBox initComboBox() {
    	JComboBox box = new JComboBox();
    	FileReader reader = null;
    	BufferedReader bufReader = null;
    	
    	try {    		
    		String globusDir = System.getProperty("user.home") + File.separator + ".globus";
    		File f = new File(globusDir, "sitesName");
    		reader = new FileReader(f);
    		bufReader = new BufferedReader(reader);
    		String line = null;
    		while ((line = bufReader.readLine()) != null) {
    			box.addItem(line);
    		}
    	} catch (Exception e) {
    		logger.debug(e.getMessage(), e);
    	} finally {
    		try {
    			if (null != reader) { 
    				reader.close();
    			}
    			if (null != bufReader) {
    				bufReader.close();
    			}	
			} catch (IOException e) {				
			}
    	}
    	
    	return box;
    }

    public void connectDlg(JFrame frame) {
        if (isConnected) {
            remoteTreeFrame.statusOut("Connection Exists");
            return;
        }
        remoteTreeFrame.statusOut("Please wait. Connecting");
        dlgConnect = new JDialog(frame);
        System.out.println("dlgConnect");
        dlgConnect.setTitle("Connect to GridFTP");
        UITools.center(frame, dlgConnect);
        dlgConnect.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowevent) {
                windowevent.getWindow().dispose();
                System.gc();
                dlgConnect.removeAll();
                remoteTreeFrame.setConnected(false);
            }
        });
        txtHost = initComboBox();
        
        txtHost.setPreferredSize(new Dimension(225, 20));
        txtHost.setEditable(true);
        txtHost.setFont(new Font("Times New Roman", 0, 15));
        txtprofileName = new JTextField(20);
        txtprofileName.setText(GRID_FTP_PORT + "");
        txtprofileName.setFont(new Font("Times New Roman", 0, 15));
        txtSubject = new JTextField(20);        
        txtSubject.setFont(new Font("Times New Roman", 0, 15));
        JLabel jhost = new JLabel("Host ");
        JLabel jprofile = new JLabel("Port  ");
        JLabel jsubject = new JLabel("Subject  ");
        JButton jbutton = new JButton("ok");
        jbutton.addActionListener(this);
        jbutton.setActionCommand("10");
        JButton jbutton1 = new JButton("cancel");
        jbutton1.addActionListener(this);
        jbutton1.setActionCommand("11");
        JPanel jpanel4 = new JPanel();
        jpanel4.add(jbutton);
        jpanel4.add(jbutton1);

        Container container = dlgConnect.getContentPane();
        container.setLayout(new BorderLayout());
        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        jp.setPreferredSize(new Dimension(350, 200));

        jp.add(jhost, getGBC(0, 16, 0.300000000000000000, 1.0, 0, 2, 1, 2));
        jp.add(txtHost, getGBC(0, 16, 0.7000000000000000, 0.0, 1, 3, 4, 1));
        jp.add(jprofile, getGBC(0, 16, 0.30000000000000000, 1.0, 0, 4, 1, 2));        
        jp.add(txtprofileName, getGBC(0, 16, 0.70000000000000000, 0.0, 1, 5, 4, 1));
        jp.add(jsubject, getGBC(0, 16, 0.30000000000000000, 1.0, 0, 6, 1, 2));
        jp.add(txtSubject, getGBC(0, 16, 0.70000000000000000, 0.0, 1, 7, 4, 1));
        jp.add(jpanel4, getGBC(0, 16, 0.30000000000000000, 1.0, 1, 8, 4, 1));
        container.add(jp, BorderLayout.CENTER);

        txtHost.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent keyevent) {
                char c = keyevent.getKeyChar();
                if (c == '\n') {
                    txtprofileName.requestFocus();
                }
            }
        });
        txtprofileName.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent keyevent) {
                remoteTreeFrame.statusOut("Please wait. Connecting");
                char c = keyevent.getKeyChar();
                if (c == '\n') {
                    doConnectOK();
                }
            }
        });

        dlgConnect.pack();
        dlgConnect.setVisible(true);
        dlgConnect.show();

    }

    protected GridBagConstraints getGBC(int i, int j, double d, double d1, int k, int l, int i1, int j1) {
        gbc.fill = i;
        gbc.anchor = j;
        gbc.weightx = d;
        gbc.weighty = d1;
        gbc.gridx = k;
        gbc.gridy = l;
        gbc.gridwidth = i1;
        gbc.gridheight = j1;
        return gbc;
    }

    public void doConnectOK() {

        dlgConnect.setVisible(false);
        dlgConnect.dispose();
        dlgConnect.removeAll();
        dlgConnect = null;
        System.gc();
        wndPreload = new WindowProgress("Connecting ... Please wait", 25);
        centerWindow(wndPreload);
        //	wndPreload.setVisible(true);
        wndPreload.setProgressValue(5);
        try {
            port = Integer.parseInt(txtprofileName.getText());            
        } catch (Exception e) {
            e.printStackTrace(System.out);
            remoteTreeFrame.setConnected(false);
        }

        host = txtHost.getSelectedItem().toString();
        subject = txtSubject.getText();
        subject1 = subject;
        profile = host + ":" + port;//txtprofileName.getText();
        if (profile.length() <= 0) {
            profile = host;
        }
        wndPreload.setProgressValue(8);
        setConnectDetails(true);
        
        //save the site name to a file
        if (-1 == txtHost.getSelectedIndex()) {
        	String globusDir = System.getProperty("user.home") + File.separator + ".globus";
            File sitesNameFile = new File(globusDir, "sitesName");
            FileWriter writer = null;
            try {
            	File dir = new File(globusDir);
            	if (!dir.isDirectory() || !dir.exists()) {
            		dir.mkdirs();
            	}
            	if (!sitesNameFile.exists() || !sitesNameFile.isFile()) {
                	sitesNameFile.createNewFile();
                }
            	
            	writer = new FileWriter(sitesNameFile, true);
            	writer.write(host);
            	writer.write("\n");
            } catch (Exception e) {
            	logger.debug(e.getMessage(), e);
            } finally {
            	try {
    				writer.close();
    			} catch (IOException e) {				
    			}
            }
        }        
    }

    public boolean setConnectDetails(boolean interactive) {
        remoteTreeFrame.setProtocol("gsiftp");
        remoteTreeFrame.setHost(host);
        remoteTreeFrame.setPort(port);        
        if (wndPreload != null) {
            wndPreload.setProgressValue(10);
        }
        final boolean isInteractive = interactive;
        Thread connectThread = new Thread(){
            public void run(){
                remoteTreeFrame._actionConnect(isInteractive);
            }
        };
        connectThread.start();
        if (wndPreload != null) {
            wndPreload.setProgressValue(24);
        }
        if (wndPreload != null) {
            wndPreload.setProgressValue(25);
            wndPreload.setVisible(false);

            wndPreload = null;
        }
        if (isConnected) {
            return true;
        } else {
            return false;
        }
    }

    public void refresh() {
        remoteTreeFrame._actionRefresh();

    }

    public void connectRemote() {
        System.out.println("\n  HOST = " + host + " port = " + port);
        try {
            client = new GridFTPClient(host, port);
            client1 = new GridFTPClient(host, port);
            client2 = new GridFTPClient(host, port);
            client3 = new GridFTPClient(host, port);
            if (null != subject && !"".equals(subject.trim())) {
            	Authorization auth = new IdentityAuthorization(subject);
            	client.setAuthorization(auth);
            	client1.setAuthorization(auth);
            	client2.setAuthorization(auth);
            	client3.setAuthorization(auth);
            }
        } catch (ServerException fte) {
            JOptionPane.showMessageDialog(this, "The host: " +
                    host + "\n or the port number: " +
                    port + " is invalid.\n Please try again. ");
            logger.debug("ServerException instantiating client.");
            logger.debug(fte.getMessage(), fte);
                        
            //connectDlg(null);
            //	    remoteTreeFrame.setConnected(false);
            return;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "The host : " +
                    host + "\n or the port number: " +
                    port + " is invalid.\n  Please try again.");
            logger.debug("IOException instantiating client.");
            logger.debug(ioe.getMessage(), ioe);
            //connectDlg(null);
            //	    remoteTreeFrame.setConnected(false);
            return;
        }
        if (wndPreload != null) {
            wndPreload.setProgressValue(13);
        }
        String file = null;
        GlobusCredential credentials = null;
        GSSCredential proxy = null;
        
        try {
            if (file == null) {
                file = CoGProperties.getDefault().getProxyFile();
            }
            credentials = new GlobusCredential(file);
            proxy = new GlobusGSSCredentialImpl(credentials,
                    GSSCredential.ACCEPT_ONLY);

        } catch (Exception e) {
            System.err.println("Unable to load the user proxy : " + e.getMessage());
            JOptionPane.showMessageDialog(
                    this,
                    "Authentication Failed.\n\n"
                    + e.getMessage(),
                    "Security Message",
                    JOptionPane.WARNING_MESSAGE);
            //connectDlg(null);
            logger.debug(e.getMessage(), e);
            //    remoteTreeFrame.setConnected(false);
            return;
        }
        if (wndPreload != null) {
            wndPreload.setProgressValue(16);
        }
        try {
            client.authenticate(null);
            client1.authenticate(null);
            client2.authenticate(null);
            client3.authenticate(null);
        } catch (ServerException fte) {
            JOptionPane.showMessageDialog(
                    this,
                    "Authentication Failed.\n\n"
                    + fte.getMessage(),
                    "Security Message",
                    JOptionPane.WARNING_MESSAGE);
            logger.debug("Credentials are not valid. Use the Security menu");
            logger.debug(fte.getMessage(), fte);
            //remoteTreeFrame.setConnected(false);
            return;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                    this,
                    "Authentication Failed.\n\n"
                    + ioe.getMessage(),
                    "Security Message",
                    JOptionPane.WARNING_MESSAGE);
            logger.debug("Credentials are not valid. Use the Security Menu");
            logger.debug(ioe.getMessage(), ioe);
            //remoteTreeFrame.setConnected(false);
            return;
        }
        if (wndPreload != null) {
            wndPreload.setProgressValue(18);
        }
        try {
            client.setDataChannelAuthentication(DataChannelAuthentication.NONE);
            client1.setDataChannelAuthentication(DataChannelAuthentication.NONE);
            client2.setDataChannelAuthentication(DataChannelAuthentication.NONE);
            client3.setDataChannelAuthentication(DataChannelAuthentication.NONE);
            logger.debug("Setting Data Channel Authorization to none.");
        } catch (ServerException fte) {
            JOptionPane.showMessageDialog(this, "Error setting Data " +
                    "Channel Authentication.");
            logger.debug(fte.getMessage(), fte);
            // remoteTreeFrame.setConnected(false);
            return;
        } catch (IOException ioe) {
        	logger.debug(ioe.getMessage(), ioe);
            JOptionPane.showMessageDialog(this, "Error setting Data " +
                    "Channel Authentication.");
            //	    remoteTreeFrame.setConnected(false);
            return;
        }
        if (wndPreload != null) {
            wndPreload.setProgressValue(20);
        }
        isConnected = true;
        remoteTreeFrame.setConnected(true);
    }

    public void disconnect() {
        remoteTreeFrame._actionDisconnect();
    }

    public boolean createClient(GridFTPClient newClient) {
        try {
            //	    newClient.close();
            newClient = null;
            newClient = new GridFTPClient(host, port);
            newClient.authenticate(null);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentDir() {
        try {
            remoterootDir = client.getCurrentDir();
        } catch (ServerException fte) {
            logger.debug("ServerException getting remote root directory.");
            logger.debug(fte.getMessage(), fte);
            String errorMsg = fte.getMessage();
            if (errorMsg.indexOf("Timeout") > 0) {
                JOptionPane.showMessageDialog(this, "Connection Timed Out");
                remoteTreeFrame.setConnected(false);
            }
            logger.debug(errorMsg);
            return null;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Disconnected due to" +
                    " Network problems");
            logger.debug("IOException getting remote root directory.");
            logger.debug(ioe.getMessage(), ioe);
            remoteTreeFrame.setConnected(false);
            return null;
        } catch (Exception ioe) {
            logger.debug("Exception getting remote root directory.");
            logger.debug(ioe.getMessage(), ioe);
            return null;
        }

        remoteTreeFrame.setServerOpSys(1);
        //client.pwd();

        return remoterootDir;
    }


    public void disconnectRemote(boolean connectionAndGUI) {
        if (client != null || client1 != null || client2 != null || client3 != null) {


            try {
                client.close();
                client = null;
                if (client1 != null) {
                    client1.close();
                    client1 = null;
                }
                if (client2 != null) {
                    client2.close();
                    client2 = null;
                }
                if (client3 != null) {
                    client3.close();
                    client3 = null;
                }


            } catch (ServerException fte) {
                logger.debug("ServerException disconnecting.");
                logger.debug(fte.getMessage(), fte);
            } catch (IOException ioe) {
                logger.debug("IOException disconnecting.");
                logger.debug(ioe.getMessage(), ioe);
            }
        }
        if (isConnected) {
            isConnected = false;
        }
        if(connectionAndGUI){
        System.gc();
        fireRemDirEvent(new RemDirEvent(this, RemDirEvent.COUNTER), null, "", this);
        }
        logger.debug("Returned correctly after disconnect.");
        return;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("\n In the property change method.");
        host = remoteTreeFrame.getHost();
        remoteTreeFrame.statusOut("Connecting. Please wait ....");
        isConnected = false;
        setConnectDetails(true);
        logger.debug("Remote url changed.display the authorization dialog ");
    }

    public Vector listDir(String dirname) {
        logger.debug("\nEntered the listdir function in grid client.");
        Vector listing = null;
        if (client1 == null) {
            logger.debug("Client null...Trying to create a new instance");
            try {
                client1 = new GridFTPClient(host, port);
                System.out.println("subject:" + subject);
                if (null != subject && !"".equals(subject.trim())) {                	
                	Authorization auth = new IdentityAuthorization(subject);
                	//client.setAuthorization(auth);
                	client1.setAuthorization(auth);
//                	client2.setAuthorization(auth);
//                	client3.setAuthorization(auth);
                }
                client1.authenticate(null);
                client1.setDataChannelAuthentication(DataChannelAuthentication.NONE);
            } catch (Exception e) {
                logger.debug("Client null...Failed the trial to create one.");
                remoteTreeFrame.setConnected(false);
                return null;
            }
        }
        try {
            client1.setClientWaitParams(200 * 900000, 300);
            client1.changeDir(dirname);
            client1.setPassive();
            client1.setLocalActive();
//            client1.setLocalPassive();
//            client1.setActive();
            
            client1.setLocalNoDataChannelAuthentication();
            logger.debug("\nSET THE PARAMETERS." + client1);
            //listing = client1.list();
            listing = client1.mlsd();
            logger.debug("Returned correctly from list.");
        } catch (ServerException fte) {
            logger.debug("ServerException listing directory." + client1);
            fte.printStackTrace(System.out);
            String errorMsg = fte.getMessage();
            if (errorMsg.indexOf("Timeout") > 0) {
                JOptionPane.showMessageDialog(this, "Connection Timed Out");
                remoteTreeFrame.setConnected(false);
            }
            logger.debug(errorMsg);
            return null;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Disconnected due to" +
                    " Network problems");
            logger.debug("IOException listing directory.");
            logger.debug(ioe.getMessage());
            ioe.printStackTrace(System.out);
            remoteTreeFrame.setConnected(false);
            return null;
        } catch (Exception ioe) {
            logger.debug("Exception listing the remote directory.");
            logger.debug(ioe.getMessage());
            ioe.printStackTrace();
            return null;
        }

        return listing;
        //remoteTreeFrame.setList(listing);
    }

    public Vector listAllDir(String dirname) {
        Vector listing = null;
        Vector list = new Vector();
        if (client == null) {
            logger.debug("Client null...Trying to create a new instance");
            try {
                client = new GridFTPClient(host, port);
                client.authenticate(null);
                client.setDataChannelAuthentication(DataChannelAuthentication.NONE);
            } catch (Exception e) {
                logger.debug("Client null...Failed the trial to create one.");
                remoteTreeFrame.setConnected(false);
                return null;
            }


        }

        final ByteArrayOutputStream received = new ByteArrayOutputStream(1000);
        String output = null;
        try {
            client.setClientWaitParams(200 * 900000, 300);
            client.changeDir(dirname);

            client.setPassive();
            client.setLocalActive();
//            client.setLocalPassive();
//            client.setActive();
            
            client.setLocalNoDataChannelAuthentication();
            client.list("*", "-d", new DataSink() {
                public void write(Buffer buffer)
                        throws IOException {

                    received.write(buffer.getBuffer(),
                            0,
                            buffer.getLength());

                }

                public void close()
                        throws IOException {
                };
            });
            output = received.toString();
            logger.debug("\nReceived of directory listing\n" + output);
            BufferedReader reader = new BufferedReader(new StringReader(received.toString()));


            String line = null;

            while ((line = reader.readLine()) != null) {
                /*	    if (logger.isDebugEnabled()) {
                        logger.debug("line ->" + line);
                        }*/
                if (line.startsWith("total")) {
                    continue;
                }
                list.addElement(line);
                logger.debug("\nline = " + line);
            }

            listing = list;
        } catch (Exception e) {
            logger.debug("Parameterized list also failed");
            e.printStackTrace(System.out);
        }

        return listing;

    }

    public Vector listTransferDir(String dirname) {
        Vector listing = null;
        if (client2 == null) {
            logger.debug("Client null...Trying to create a new instance");
            try {
                client2 = new GridFTPClient(host, port);
                client2.authenticate(null);
                client2.setDataChannelAuthentication(DataChannelAuthentication.NONE);
            } catch (Exception e) {
                logger.debug("Client null...Failed the trial to create one.");
                JOptionPane.showMessageDialog(this, "Please drag a smaller" +
                        "directory.\n Server is not" +
                        " to remain connected.");
                remoteTreeFrame.setConnected(false);
                return null;
            }

        }
        try {
            client2.setClientWaitParams(200 * 900000, 300);
            client2.changeDir(dirname);
            client2.setPassive();
            client2.setLocalActive();
//            client2.setLocalPassive();
//            client2.setActive();
            
            client2.setLocalNoDataChannelAuthentication();
            listing = client2.mlsd();
            logger.debug("Returned correctly from list.");
        } catch (ServerException fte) {
            logger.debug("ServerException listing directory.");
            fte.printStackTrace(System.out);
            String errorMsg = fte.getMessage();
            if (errorMsg.indexOf("Timeout") > 0) {
                JOptionPane.showMessageDialog(this, "Connection Timed Out");
                remoteTreeFrame.setConnected(false);
            }
            logger.debug(errorMsg);
            return null;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Disconnected due to" +
                    " Network problems");
            logger.debug("IOException listing directory.");
            logger.debug(ioe.getMessage());
            ioe.printStackTrace(System.out);
            remoteTreeFrame.setConnected(false);
            return null;
        } catch (Exception ioe) {
            logger.debug("Exception listing the remote directory.");
            logger.debug(ioe.getMessage());
            ioe.printStackTrace(System.out);
            return null;
        }

        return listing;
        //remoteTreeFrame.setList(listing);
    }

    public Vector listDeleteDir(String dirname) {
        Vector listing = null;
        if (client3 == null) {
            logger.debug("Client null...returning from listCurrentDirectory");
            try {
                client3 = new GridFTPClient(host, port);
                client3.authenticate(null);
                client3.setDataChannelAuthentication(DataChannelAuthentication.NONE);
            } catch (Exception e) {
                logger.debug("Client null...Failed the trial to create one.");
                remoteTreeFrame.setConnected(false);
                return null;
            }
        }
        try {
            client3.setClientWaitParams(200 * 900000, 300);
            client3.changeDir(dirname);
            client3.setPassive();
            client3.setLocalActive();
//            client3.setLocalPassive();
//            client3.setActive();
            
            client3.setLocalNoDataChannelAuthentication();
            listing = client3.list();
            logger.debug("Returned correctly from list.");
        } catch (ServerException fte) {
            logger.debug("ServerException listing directory.");
            String errorMsg = fte.getMessage();
            if (errorMsg.indexOf("Timeout") > 0) {
                JOptionPane.showMessageDialog(this, "Connection Timed Out");
                remoteTreeFrame.setConnected(false);
            }
            logger.debug(errorMsg);
            return null;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Disconnected due to" +
                    " Network problems");
            logger.debug("IOException listing directory.");
            logger.debug(ioe.getMessage());
            ioe.printStackTrace(System.out);
            remoteTreeFrame.setConnected(false);
            return null;
        } catch (Exception ioe) {
            logger.debug("Exception listing the remote directory.");
            logger.debug(ioe.getMessage());
            ioe.printStackTrace(System.out);
            return null;
        }

        return listing;
        //remoteTreeFrame.setList(listing);
    }

    public void pwd() {
        try {
            client.getCurrentDir();
            logger.debug("Returned correctly from pwd.");
        } catch (ServerException fte) {
            logger.debug("ServerException showing pwd.");
            String errorMsg = fte.getMessage();
            if (errorMsg.indexOf("Timeout") > 0) {
                JOptionPane.showMessageDialog(this, "Connection Timed Out");
                remoteTreeFrame.setConnected(false);
            }
            logger.debug(errorMsg);

            return;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Disconnected due to" +
                    " Network problems");
            logger.debug("IOException showing pwd.");
            logger.debug(ioe.getMessage());
            remoteTreeFrame.setConnected(false);
            return;
        }
    }

    public void get(String remote, File local) {
        /*        try {
              client.get(remote,local);
              logger.debug("Returned correctly from pwd.");
              }
              catch (ServerException fte) {
              logger.debug("ServerException showing pwd.");
              logger.debug(fte.getMessage());
              return ;
              }
              catch (IOException ioe) {
              logger.debug("IOException showing pwd.");
              logger.debug(ioe.getMessage());
              return ;
              }*/
    }

    public void put(File local, String remote) {
        /*        try {
              client.put(local,remote,false);
              logger.debug("Returned correctly from pwd.");
              }
              catch (ServerException fte) {
              logger.debug("ServerException showing pwd.");
              logger.debug(fte.getMessage());
              return ;
              }
              catch (IOException ioe) {
              logger.debug("IOException showing pwd.");
              logger.debug(ioe.getMessage());
              return ;
              }*/
    }

    public boolean chdir(String s) {
        try {
            client.changeDir(s);
            logger.debug("Returned correctly from change dir command");
            return true;
        } catch (ServerException fte) {
            logger.debug("ServerException during change dir command.");

            String errorMsg = fte.getMessage();
            if (errorMsg.indexOf("Timeout") > 0) {
                JOptionPane.showMessageDialog(this, "Connection Timed Out");
                remoteTreeFrame.setConnected(false);
            }
            logger.debug(errorMsg);
            return false;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Disconnected due to" +
                    " Network problems");
            logger.debug("IOException during change dir command.");
            logger.debug(ioe.getMessage());
            remoteTreeFrame.setConnected(false);
            return false;
        } catch (Exception ioe) {
            logger.debug("Exception during change dir command.");
            logger.debug(ioe.getMessage());
            return false;
        }

    }


    public void setType(boolean flag) {
        if (flag) {
            try {
                client.setType(Session.TYPE_ASCII);
                logger.debug("Returned correctly from setType.");
            } catch (ServerException fte) {
                logger.debug("ServerException during  setType.");
                String errorMsg = fte.getMessage();
                if (errorMsg.indexOf("Timeout") > 0) {
                    JOptionPane.showMessageDialog(this, "Connection Timed Out");
                    remoteTreeFrame.setConnected(false);
                }
                logger.debug(errorMsg);
                return;
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Disconnected due to" +
                        " Network problems");
                logger.debug("IOException during  setType.");
                logger.debug(ioe.getMessage());
                remoteTreeFrame.setConnected(false);
                return;
            }
        } else {
            try {
                client.setType(Session.TYPE_IMAGE);
                logger.debug("Returned correctly from  setType.");
            } catch (ServerException fte) {
                logger.debug("ServerException during  setType.");
                String errorMsg = fte.getMessage();
                if (errorMsg.indexOf("Timeout") > 0) {
                    JOptionPane.showMessageDialog(this, "Connection Timed Out");
                    remoteTreeFrame.setConnected(false);
                }
                logger.debug(errorMsg);
                return;
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Disconnected due to" +
                        " Network problems");
                logger.debug("IOException  in setType.");
                logger.debug(ioe.getMessage());
                remoteTreeFrame.setConnected(false);
                return;
            }
        }
    }

    public boolean rename(String s, String s1) {
        try {
            client.rename(s, s1);
            logger.debug("Returned correctly from rename.");
            return true;
        } catch (ServerException fte) {
            logger.debug("ServerException renaming directory.");
            String errorMsg = fte.getMessage();
            if (errorMsg.indexOf("Timeout") > 0) {
                JOptionPane.showMessageDialog(this, "Connection Timed Out");
                remoteTreeFrame.setConnected(false);
            }
            logger.debug(errorMsg);
            return false;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Disconnected due to" +
                    " Network problems");
            logger.debug("IOException renaming directory.");
            logger.debug(ioe.getMessage());
            ioe.printStackTrace(System.out);
            remoteTreeFrame.setConnected(false);
            return false;
        }
    }

    public boolean exists(String s1) {
        try {
            return client.exists(s1);
        } catch (ServerException fte) {
            logger.debug("ServerException renaming directory.");
            String errorMsg = fte.getMessage();
            if (errorMsg.indexOf("Timeout") > 0) {
                JOptionPane.showMessageDialog(this, "Connection Timed Out");
                remoteTreeFrame.setConnected(false);
            }
            logger.debug(errorMsg);
            return false;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Disconnected due to" +
                    " Network problems");
            logger.debug("IOException renaming directory.");
            logger.debug(ioe.getMessage());
            ioe.printStackTrace(System.out);
            remoteTreeFrame.setConnected(false);
            return false;
        }
    }

    public boolean mkdir(String s) {
        try {
            if (!exists(s)) {
                client.makeDir(s);
                logger.debug("Returned correctly from make directory.");
            } else {
                System.out.println("The already directory exists");
            }
            return true;
        } catch (ServerException fte) {
            logger.debug("Directory exists or Permission denied.");
            String errorMsg = fte.getMessage();
            if (errorMsg.indexOf("exists") > 0) {
                remoteTreeFrame.setError("exists");
                return true;
            } else if (errorMsg.indexOf("Timeout") > 0) {
                JOptionPane.showMessageDialog(this, "Connection Timed Out");
                remoteTreeFrame.setConnected(false);
                logger.debug(errorMsg);
                return false;
            } else {
                logger.debug(errorMsg);
                return false;
            }

        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Disconnected due to" +
                    " Network problems");
            logger.debug("No network connection or Firewall prevents it.");
            logger.debug(ioe.getMessage());
            remoteTreeFrame.setConnected(false);
            return false;
        }
    }

    public boolean mkdirs(String dir, String path) {
        try {
            int index = dir.lastIndexOf("/");
            int index1 = path.lastIndexOf("/");

            logger.info("\nSTART DIR = " + dir + "  index = " + index);
            logger.info("\npath = " + path + " index1 = " + index1);
            logger.info("\nMAKING DIR = " + path.substring(0, index1));
            if (index1 > index) {
                mkdirs(dir, path.substring(0, index1));

            }

            if (mkdir(path)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return true;
        }

    }

    public boolean removeDir(String s) {
        try {
            client.deleteDir(s);
            logger.debug("Returned correctly after deleting directory.");
            return true;
        } catch (ServerException fte) {
            logger.debug("ServerException deleting directory.");
            String errorMsg = fte.getMessage();
            if (errorMsg.indexOf("Timeout") > 0) {
                JOptionPane.showMessageDialog(this, "Connection Timed Out");
                remoteTreeFrame.setConnected(false);
            }
            logger.debug(errorMsg);
            return false;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Disconnected due to" +
                    " Network problems");
            logger.debug("IOException deleting directory.");
            logger.debug(ioe.getMessage());
            remoteTreeFrame.setConnected(false);
            return false;
        }
    }

    public boolean removeFile(String s) {
        try {
            client.deleteFile(s);
            logger.debug("Returned correctly after deleting file.");
            return true;
        } catch (ServerException fte) {
            logger.debug("ServerException deleting file.");
            String errorMsg = fte.getMessage();
            if (errorMsg.indexOf("Timeout") > 0) {
                JOptionPane.showMessageDialog(this, "Connection Timed Out");
                remoteTreeFrame.setConnected(false);
            }
            logger.debug(errorMsg);
            return false;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Disconnected due to" +
                    " Network problems");
            logger.debug("IOException deleting file.");
            logger.debug(ioe.getMessage());
            remoteTreeFrame.setConnected(false);
            return false;
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getSubject() {
    	if ("".equals(subject.trim())) {
    		subject = null;
    	}
    	
    	return subject;
    }
    
    public void rootDirRemote() {
        remoteTreeFrame.setRootRemote(remoterootDir);
    }

    public String getRootURL() {
        return remoteTreeFrame.getBaseUrl();
    }

    public void setDragDetails(GridEvent e, String from) {
        remoteTreeFrame.statusOut("Copying the file ...");
        fireRemDirEvent(new RemDirEvent(this, RemDirEvent.REMOTEDRAG), null, from, this);
    }

    public void setDropDetails(GridEvent e, String to) {
        fireRemDirEvent(new RemDirEvent(this, RemDirEvent.DIR), null, to, this);

    }

    /*This method sets the selected source directory for transfer*/
    public void setSelectedSource() {
        remoteTreeFrame.right.setSelectedSource();
    }

    /*This method sets the selected destination directory for transfer*/
    public void setSelectedDestination() {
        remoteTreeFrame.right.setSelectedDestination();
    }

    /*Please make sure to call the transfer method of the Destination Bean*/
    public void transfer() {
        remoteTreeFrame.right.transfer();
    }

    public void callGridEditFrame(GridEvent e, String filesep, String seldir[]) {
        /*      editFrame = new GridRemEditFrame(this,seldir, filesep);
            editFrame.addWindowListener(new WindowAdapter() {

public void windowClosing(WindowEvent windowevent)
{
            editFrame = null;
            System.gc();
}

            });
            editFrame.pack();
            editFrame.setVisible(true);
        */
    }


    public void run() {

        doConnectOK();

    }

    public void actionPerformed(ActionEvent ae) {
        String s = ae.getActionCommand();
        int i = 0;
        try {
            i = Integer.valueOf(s).intValue();
        } catch (NumberFormatException numberformatexception) {
            //theApp.toolkit.beep();
            remoteTreeFrame.statusOut("Action Error: " + numberformatexception.getMessage());
        }
        //RemRenameDialog RemRenameDialog;
        switch (i) {
            default :
                break;

            case 10: // dialog enter the remote details ok button            	
                Thread connect = new Thread(this);
                connect.start();
                remoteTreeFrame.statusOut("Connecting ... Please wait");
                break;
            case 11: // cancel button
                if (!bean) {
                    remoteTreeFrame.setConnected(false);
                }
                dlgConnect.dispose();
                break;

        }

    }

    public static void centerWindow(Window guiComponent) {

        //Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = guiComponent.getSize();

        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }

        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }

        guiComponent.setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);
    }
      public static void main(String arg[]){
        GridClient gridftpPanel = new GridClient();
        JFrame sFrame = new JFrame("Remote FTP System:  "+arg[0]);
        sFrame.getContentPane().setLayout(new GridLayout(1, 1));
        sFrame.getContentPane().add(gridftpPanel);
        sFrame.pack();
        sFrame.setSize(300, 400);
        sFrame.setVisible(true);
        UITools.center(null, sFrame);
        gridftpPanel.setHost(arg[0]);
        gridftpPanel.setConnectDetails(true);
      }
}
