//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
//Created on Oct 7, 2004

package org.globus.cog.gridface.impl.desktop.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;

import org.globus.cog.gridface.impl.desktop.AbstractDesktop;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessDesktop;
import org.globus.cog.gridface.impl.desktop.interfaces.CoGTop;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopInternalFrame;
import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;
import org.globus.cog.gridface.impl.util.Logger;
import org.globus.cog.gridface.impl.util.LoggerImpl;

public class CoGLogFrame extends DesktopInternalFrameImpl implements AccessDesktop {
	CoGTop desktop = null;
	LoggerImpl logger = null;
	public CoGLogFrame(CoGTop desktop, LoggerImpl log) {
		super(
				log,
				AbstractDesktop.sLOG_CoG,
				true,
				false,
				true,
				true,
				true);
		this.logger = log;
		setDesktop(desktop);
		
		setSize(DesktopInternalFrame.LOGFRAME_WIDTH,DesktopInternalFrame.LOGFRAME_HEIGHT);

		desktop.addFrame(this);

		configureMenuBar();
	}
	public void configureMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		int defaultLogLevel = logger.getLevel();
		
		JMenu newMenu = new JMenu("Actions");
		newMenu.add(new AbstractAction("Clear"){
			public void actionPerformed(ActionEvent e) {
				logger.clearLog();
			}
		});
		newMenu.add(new AbstractAction("Save Log"){
			public void actionPerformed(ActionEvent e) {
				try{
					File saveLogFile = DesktopUtilities.getDestinationFile(".",".log","Save "+AbstractDesktop.sLOG_CoG);
					logger.saveLogToFile(saveLogFile!=null ? saveLogFile.getCanonicalPath(): null);
				}catch(IOException ex){
					logger.error("IO Error in Saving Log to File");
				}
			}
		});
		JMenu newSubMenu = new JMenu("Log Level");

		JRadioButtonMenuItem debugButton =
			new JRadioButtonMenuItem(
				"Debug",
				true ? defaultLogLevel == Logger.DEBUG : false);
		debugButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.setLevel(Logger.DEBUG);
			}
		});

		JRadioButtonMenuItem infoButton =
			new JRadioButtonMenuItem(
				"Info",
				true ? defaultLogLevel == Logger.INFO : false);
		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.setLevel(Logger.INFO);
			}
		});

		JRadioButtonMenuItem warnButton =
			new JRadioButtonMenuItem(
				"Warn",
				true ? defaultLogLevel == Logger.WARN : false);
		warnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.setLevel(Logger.WARN);
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(debugButton);
		group.add(infoButton);
		group.add(warnButton);

		newSubMenu.add(debugButton);
		newSubMenu.add(infoButton);
		newSubMenu.add(warnButton);
		newMenu.add(newSubMenu);
		menuBar.add(newMenu);
		
		newMenu = new JMenu("Help");
		newMenu.add(new AbstractAction(Logger.sABOUT){
			public void actionPerformed(ActionEvent e){
				logger.showAboutFrame(null);
			}
		});
		menuBar.add(newMenu);
		
	}
	
	public CoGTop getDesktop() {
		return this.desktop;
	}
	public void setDesktop(CoGTop desktop) {
		this.desktop = desktop;

	}
}
