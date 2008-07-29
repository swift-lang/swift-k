/*
 * GridFTPGUIView.java
 */

package org.globus.transfer.reliable.client;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.globus.common.CoGProperties;
import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.ogce.beans.filetransfer.FtpProperties;
import org.globus.ogce.beans.filetransfer.gui.FileTransferMainPanel;
import org.globus.ogce.beans.filetransfer.gui.remote.common.DisplayInterface;
import org.globus.ogce.beans.filetransfer.gui.remote.ftp.FtpClient;
import org.globus.ogce.beans.filetransfer.gui.remote.gridftp.GridClient;
import org.globus.ogce.util.StringUtil;
import org.globus.tools.proxy.GridProxyInit;
import org.globus.tools.ui.util.CustomFileFilter;
import org.globus.tools.ui.util.UITools;
import org.globus.transfer.reliable.client.credential.CredentialDialog;
import org.globus.transfer.reliable.client.credential.myproxy.MyProxyLogonGUI;
import org.globus.transfer.reliable.client.utils.LogFileUtils;
import org.globus.transfer.reliable.client.utils.UIConstants;
import org.globus.transfer.reliable.client.utils.Utils;
import org.globus.util.ConfigUtil;
import org.globus.util.Util;

/**
 * The application's main frame.
 */
public class GridFTPGUIView extends FrameView {
    private Log logger = LogFactory.getLog(GridFTPGUIView.class);
    
    public GridFTPGUIView(SingleFrameApplication app) {
        super(app);

        initComponents();
        //
        //fileTransferMainPanel1.add(fileTransferMainPanel1.getDesktopPane());
        //mainPanel.add(fileTransferMainPanel1.getDesktopPane());
        //mainPanel.add(fileTransferPanel.getDesktopPane());
        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = GridFTPGUIApp.getApplication().getMainFrame();
            aboutBox = new GridFTPGUIAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        GridFTPGUIApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        credential_button = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        gridftp_button = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        local_button = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        jDesktopPane1 =  fileTransferMainPanel1.getDesktopPane();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.globus.transfer.reliable.client.GridFTPGUIApp.class).getContext().getResourceMap(GridFTPGUIView.class);
        
        credential_button.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
        credential_button.setText(resourceMap.getString("credential_button.text")); // NOI18N
        credential_button.setToolTipText(resourceMap.getString("credential_button.toolTipText")); // NOI18N
        credential_button.setFocusable(false);
        credential_button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        credential_button.setName("credential_button"); // NOI18N
        credential_button.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        credential_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	credential_buttonActionPerformed(evt);
            }
        });
        jToolBar1.add(credential_button);
        
        
        jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setToolTipText(resourceMap.getString("jButton1.toolTipText")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        //jToolBar1.add(jButton1);

        gridftp_button.setIcon(resourceMap.getIcon("gridftp_button.icon")); // NOI18N
        gridftp_button.setText(resourceMap.getString("gridftp_button.text")); // NOI18N
        gridftp_button.setToolTipText(resourceMap.getString("gridftp_button.toolTipText")); // NOI18N
        gridftp_button.setFocusable(false);
        gridftp_button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        gridftp_button.setName("gridftp_button"); // NOI18N
        gridftp_button.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        gridftp_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	gridftp_buttonActionPerformed(evt);
            }
        });
        jToolBar1.add(gridftp_button);

        jButton4.setIcon(resourceMap.getIcon("jButton4.icon")); // NOI18N
        jButton4.setText(resourceMap.getString("jButton4.text")); // NOI18N
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setName("jButton4"); // NOI18N
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        //jToolBar1.add(jButton4);

        local_button.setIcon(resourceMap.getIcon("local_button.icon")); // NOI18N
        local_button.setText(resourceMap.getString("local_button.text")); // NOI18N
        local_button.setFocusable(false);
        local_button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        local_button.setName("local_button"); // NOI18N
        local_button.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        local_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                local_buttonActionPerformed(evt);
            }
        });
        jToolBar1.add(local_button);
        
        jButton5.setIcon(resourceMap.getIcon("jButton5.icon")); // NOI18N
        jButton5.setText(resourceMap.getString("jButton5.text")); // NOI18N
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton5);
        
        jButton7.setIcon(resourceMap.getIcon("jButton7.icon")); // NOI18N
        jButton7.setText("MyProxy"); // NOI18N
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setName("jButton7"); // NOI18N
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        //jToolBar1.add(jButton7);
        
        jButton8.setIcon(resourceMap.getIcon("jButton8.icon")); // NOI18N
        jButton8.setText(resourceMap.getString("jButton8.text")); // NOI18N
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setName("jButton8"); // NOI18N
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        //jToolBar1.add(jButton8);

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        jDesktopPane1.setBackground(resourceMap.getColor("jDesktopPane1.background")); // NOI18N
        jDesktopPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jDesktopPane1.setName("jDesktopPane1"); // NOI18N

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 834, Short.MAX_VALUE))
            .add(jToolBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 846, Short.MAX_VALUE)
            .add(jDesktopPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 846, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createSequentialGroup()
                .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jDesktopPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenuItem6.setText(resourceMap.getString("jMenuItem6.text")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem6);

        jMenuItem7.setText(resourceMap.getString("jMenuItem7.text")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem7);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.globus.transfer.reliable.client.GridFTPGUIApp.class).getContext().getActionMap(GridFTPGUIView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N
        jMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu1ActionPerformed(evt);
            }
        });

        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        menuBar.add(jMenu1);

        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });

        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);

        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        menuBar.add(jMenu2);
        
        jMenu3.setText(resourceMap.getString("jMenu3.text")); // NOI18N
        jMenu3.setName("jMenu3"); // NOI18N
        jMenu3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu3ActionPerformed(evt);
            }
        });
        
        jMenuItem11.setText(resourceMap.getString("jMenuItem11.text")); // NOI18N
        jMenuItem11.setName("jMenuItem11"); // NOI18N
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem11);
        
        jMenuItem12.setText(resourceMap.getString("jMenuItem12.text")); // NOI18N
        jMenuItem12.setName("jMenuItem12"); // NOI18N
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem12);
        menuBar.add(jMenu3);
        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusMessageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 662, Short.MAX_VALUE)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusMessageLabel)
                    .add(statusAnimationLabel)
                    .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    protected void credential_buttonActionPerformed(ActionEvent evt) {
		CredentialDialog d = new CredentialDialog(null, false);
		d.setLocation(100, 100);
		d.setVisible(true);
		
	}

	private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        localFrameHandle();        
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenu1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu1ActionPerformed
       
    }//GEN-LAST:event_jMenu1ActionPerformed

    private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed
                
    }//GEN-LAST:event_jMenu2ActionPerformed
    
    private void jMenu3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed
        
    }//GEN-LAST:event_jMenu2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        createProxy();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        CoGProperties props = CoGProperties.getDefault();

                String proxyFileName = props.getProxyFile();
                if (proxyFileName.length() > 0) {

                    File proxyFile = new File(proxyFileName);
                    if (!proxyFile.exists()) {
                        JOptionPane.showMessageDialog(
                                mainPanel,
                                "Your Grid proxy certificate is already destroyed.",
                                "Security Message",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    Util.destroy(proxyFile);
                    JOptionPane.showMessageDialog(
                            mainPanel,
                            "Your Grid proxy certificate has been destroyed.",
                            "Security Message",
                            JOptionPane.INFORMATION_MESSAGE);

                } else
                    JOptionPane.showMessageDialog(
                            mainPanel,
                            "Your Grid proxy certificate is not read or is destroyed",
                            "Security Message",
                            JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        showProxyInfo();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
         fileTransferMainPanel1.createRemoteFrame(1, null, 2811);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        loadFile();
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        saveFile();
    }//GEN-LAST:event_jMenuItem7ActionPerformed
    
    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        File logConfigFile = new File(UIConstants.LOG_CONFIG);
        String logLocation = null;
        if (!logConfigFile.exists()) {
        	String dir;
			try {
				dir = new File(".").getCanonicalPath();
				logLocation = dir + File.separator + "logoutput";
			} catch (IOException e) {

			}
        	
        } else {
        	InputStream is = null;
        	try {
				is = new BufferedInputStream(new FileInputStream(logConfigFile));
				Properties prop = new Properties();
	        	prop.load(is);
	        	logLocation = prop.getProperty("log4j.appender.A1.file");
			} catch (Exception e) {
				logLocation="can not get log file location";
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					
				}
			}        	
        }
        
        JOptionPane.showMessageDialog(null, logLocation, "Log Location", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItem7ActionPerformed
    
    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
    	JFileChooser chooser = new JFileChooser();
    	chooser.setDialogTitle("Choose Log File");
    	int flag = chooser.showSaveDialog(null);
    	if (flag == JFileChooser.APPROVE_OPTION) {
    		File f = chooser.getSelectedFile();
    		try {
    			logFileName = f.getCanonicalPath();
    			File logConfigFile = new File(UIConstants.LOG_CONFIG);
    			if (!logConfigFile.exists() || !logConfigFile.isFile()) {
    				LogFileUtils.createNewLogConfigFile(logFileName);
    			} else {
    				LogFileUtils.updateLogConfigFile(logFileName);
    			}
    			PropertyConfigurator.configure(UIConstants.LOG_CONFIG);
			} catch (IOException e) {
				logFileName = "error_log";
			}
    	}
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void localFrameHandle() {
    	boolean isRFTEnabled = (new Boolean((String)Utils.getProperty("rft_enabled", "rft.properties"))).booleanValue();
        if (isRFTEnabled) {
        	JOptionPane.showMessageDialog(null, "RFT is enabled, you can not use local dialog",
        			"Message", JOptionPane.ERROR_MESSAGE);
        } else {
        	fileTransferMainPanel1.createNewLocalFrame();
        }
    }
    private void local_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        localFrameHandle();
    	
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    	jMenuItem10ActionPerformed(evt);
    }//GEN-LAST:event_jButton3ActionPerformed
    
    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    	MyProxyLogonGUI.main(null);
    }//GEN-LAST:event_jButton3ActionPerformed
    
    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    	JFileChooser chooser = new JFileChooser();
    	chooser.setDialogTitle("Choose Log File");
    	int flag = chooser.showSaveDialog(null);
    	if (flag == JFileChooser.APPROVE_OPTION) {
    		File f = chooser.getSelectedFile();
    		try {
    			logFileName = f.getCanonicalPath();
    			File logConfigFile = new File(UIConstants.LOG_CONFIG);
    			if (!logConfigFile.exists() || !logConfigFile.isFile()) {
    				LogFileUtils.createNewLogConfigFile(logFileName);
    			} else {
    				LogFileUtils.updateLogConfigFile(logFileName);
    			}
			} catch (IOException e) {
				logFileName = "error_log";
			}
    	}
    	
    }//GEN-LAST:event_jButton3ActionPerformed
    
    private void gridftp_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        fileTransferMainPanel1.createRemoteFrame(1, null, 2811);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        createProxy();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        fileTransferMainPanel1.createRemoteFrame(3, null, 21);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        JOptionPane.showMessageDialog(mainPanel, "Not yet implemented");
        fileTransferMainPanel1.createRemoteFrame(2, null, 0);
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
    	JFrame frame = new JFrame("Advanced Options");
    	RFTPanel rftPanel = new RFTPanel();
    	rftPanel.setFrame(frame);
    	frame.getContentPane().add(rftPanel);
    	frame.setSize(500, 500);
    	frame.setLocation(100, 100);
    	//frame.pack();
    	frame.setVisible(true);
    }//GEN-LAST:event_jMenuItem9ActionPerformed
    
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        fileTransferMainPanel1.createRemoteFrame(3, null, 21);
    }//GEN-LAST:event_jButton4ActionPerformed
        
    private void createProxy() {
        GridProxyInit proxyInitFrame = new GridProxyInit(null, true);
        proxyInitFrame.setRunAsApplication(false);
        proxyInitFrame.setCloseOnSuccess(true);
        proxyInitFrame.pack();
        UITools.center(mainPanel, proxyInitFrame);
        proxyInitFrame.setVisible(true);        
    }
    
    private void showProxyInfo() {
        GlobusCredential proxy = null;
        String file = null;

        try {
            if (file == null) {
                file = CoGProperties.getDefault().getProxyFile();
            }
            proxy = new GlobusCredential(file);
        } catch (Exception e) {
            logger.debug("Unable to load the user proxy : "
                    + e.getMessage());

            JOptionPane.showMessageDialog(
                    mainPanel,
                    "Unable to load Grid proxy certificate.\nError: "
                    + e.getMessage(),
                    "Security Message",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        StringBuffer proxyInfoBuffer = new StringBuffer();
        proxyInfoBuffer.append("Subject: "
                + CertUtil.toGlobusID(proxy.getSubject())
                + "\n");

        proxyInfoBuffer.append("Strength: "
                + proxy.getStrength() + " bits"
                + "\n");

        proxyInfoBuffer.append("Time Left: "
                + Util.formatTimeSec(proxy.getTimeLeft()));

        JOptionPane.showMessageDialog(
                mainPanel,
                proxyInfoBuffer.toString(),
                "Grid Proxy Certificate Information",
                JOptionPane.INFORMATION_MESSAGE);
        //Find out how to display proxyInfo.
    }
    
        public void saveFile() {
        //Determine the name of the file to save to
        JFileChooser fileChooser = new JFileChooser(ConfigUtil.globus_dir);
        fileChooser.setFileFilter(new CustomFileFilter(".ftp",
                "GridFTP sites list file (*.ftp)"));
        int popdownState = fileChooser.showSaveDialog(mainPanel);
        if (popdownState == JFileChooser.CANCEL_OPTION) {
            return;
        }
        File saveFile = fileChooser.getSelectedFile();
        String fileName = saveFile.getAbsolutePath();
        if (!fileName.endsWith(".ftp")) {
            fileName = fileName + ".ftp";
            saveFile = new File(fileName);
        }

        //writing the default job file name into ftp.properties
        FtpProperties props = null;
        try {
            props = new FtpProperties(FtpProperties.configFile);
            props.setFtpFile(saveFile.getAbsolutePath());
            logger.info("\nThe ftp file default location saved="
                    + props.getFtpFile());
            props.save(FtpProperties.configFile);

        } catch (Exception e) {
            logger.debug("The system could not open the specified file\n");
        }

        //Saving the names of the gatekeeper into a file.
        FileWriter fileout = null;
        try {
            fileout = new FileWriter(saveFile.getAbsolutePath());
            fileout.write(getAllOpenSites());
            fileout.close();
        } catch (Exception e) {
            logger.info("IOException occured when creating"
                    + " output stream :" + e.getMessage());
        } finally {
            if (fileout != null) {
                try {
                    fileout.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
        
    public String getAllOpenSites() {
        String s = new String("");
        DisplayInterface gc = null;
        for (int i = 0; i < remCounter; i++) {

            try {
                gc = (GridClient) frame2[i];
                s += "gridftp:" + gc.getHost() + ":" + gc.getPort() + "\n";
            } catch (Exception e) {
                logger.debug("The file does not save the ftp sites "
                        + "needs to handle user and password.");
                FtpClient fc = (FtpClient) frame2[i];
                s += "ftp:" + fc.getHost() + ":" + fc.getPort() + ":" + fc.getUser() +
                        ":" + fc.getPwd() + "\n";
            }

        }
        return s;
    }
    
    public void loadFile() {

        JFileChooser filechooser = new JFileChooser(ConfigUtil.globus_dir);
        //	filechooser.setCurrentDirectory( new File(".") );
        filechooser.setFileFilter(new CustomFileFilter(".ftp",
                "Ftp file (*.ftp)"));
        filechooser.setApproveButtonText("Load");
        filechooser.setDialogTitle("Load Ftp File");

        if (filechooser.showOpenDialog(mainPanel) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = filechooser.getSelectedFile();
        loadFile(file.getAbsolutePath());

    }    
    
    public void loadFile(String filename) {
        try {
            readFile(filename);

        } catch (IOException e) {
            String msg = "Failed to load gatekeeper file:\n " + e.getMessage();
            JOptionPane.showMessageDialog(mainPanel,
                    msg,
                    "Error Loading Ftp File",
                    JOptionPane.ERROR_MESSAGE);
        }

    } 

        public void readFile(String filename) throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(filename));
            String machine;
            while ((machine = in.readLine()) != null) {
                machine = machine.trim();
                if (machine.equals("") || machine.startsWith("#")) {
                    logger.info("Machine name commented.");
                } else {
                    StringUtil splitString = new StringUtil(machine);
                    String[] tokens = splitString.split(":");
                    int noTokens = tokens.length;
                    for (int i = 0; i < noTokens; i++) {
                        //	tokens[i] = tokens[i].trim();
                    }
                    int port1 = Integer.parseInt(tokens[2]);
                    //Protocol:host:port:user:passwd
                    if (tokens[0].equals("gridftp")) {
                        fileTransferMainPanel1.createRemoteFrame(1, tokens[1], port1);
                        System.out.println("\nMachine line =" + machine);
                    } else if (noTokens > 3) {
                        logger.info("\nMachine line =" +
                                machine.substring(0,
                                        machine.lastIndexOf(":")));

                       fileTransferMainPanel1.createRemoteFrame(3, tokens[1], port1, tokens[3]
                                , tokens[4]);
                    } else {
                        fileTransferMainPanel1.createRemoteFrame(3, tokens[1], port1);
                    }
                }
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
        
    public static String getErrorLogFileName() {
    	return logFileName;
    }
 
   protected JInternalFrame messageFrame;  
   
   //Counters are used to allow the users to add any no of local or remote
   //file browser beans.
   protected int remCounter = 0;
   protected DisplayInterface frame2[];
  private FileTransferMainPanel fileTransferMainPanel1 = new FileTransferMainPanel();
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton gridftp_button;
    private javax.swing.JButton local_button;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton credential_button;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
    
    private static String logFileName = "error_log";
}
