//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
//Created on Sep 7, 2004

package org.globus.cog.gridface.impl.desktop.panels;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.globus.cog.gridface.impl.desktop.interfaces.FormPanel;

public class FormPanelSet extends JScrollPane implements FormPanel{
	protected JPanel buttonPanel = null;

	protected JPanel panelSet = new JPanel();
	
	public FormPanelSet() {
		this(null);
	}
	public FormPanelSet(String title){
	    panelSet.setBorder(BorderFactory.createTitledBorder(title));
	    panelSet.setLayout(new BoxLayout(panelSet,BoxLayout.Y_AXIS));
		buttonPanel = new JPanel();
		this.setViewportView(panelSet);
	}
//	public final void addForm(AbstractFormPanel sfp){
//		add(sfp);
//	}
	public final void addForm(AbstractFormPanel sfp){
	    panelSet.add(sfp);
	}
	
	public void display() {
		Component[] sfPanels = panelSet.getComponents();
		for (int i = 0; i < sfPanels.length; i++) {
			Component component = sfPanels[i];
			if(component instanceof FormPanel){
				((FormPanel)component).display();
			}
		}
		
		

	}
	public void export() {
		Component[] sfPanels = panelSet.getComponents();
		for (int i = 0; i < sfPanels.length; i++) {
			Component component = sfPanels[i];
			if(component instanceof FormPanel){
				((FormPanel)component).export();
			}
		}
		
	}
	
	public void export(Object updateObject) {
		Component[] sfPanels = panelSet.getComponents();
		for (int i = 0; i < sfPanels.length; i++) {
			Component component = sfPanels[i];
			if(component instanceof FormPanel){
				((FormPanel)component).export(updateObject);
			}
		}
	}
	
	public void load(ArrayList newKeys, Object origObject) {
		Component[] sfPanels = panelSet.getComponents();
		for (int i = 0; i < sfPanels.length; i++) {
			Component component = sfPanels[i];
			if(component instanceof FormPanel){
				((FormPanel)component).load(newKeys,origObject);
			}
		}
	}
	
	public void clear(){
	    panelSet.removeAll();
		this.clearButtonPanel();
	}
	public void addButtonAction(Action action){
		buttonPanel.add(new JButton(action));
	}
	public void clearButtonPanel(){
		buttonPanel.removeAll();
	}
	public void finishedAddingButtonActions(){
	    panelSet.add(buttonPanel);
	}
	
    public JPanel getPanel() {
        return panelSet;
    }
    public JScrollPane getScrollContainer() {
        return this;
    }
}
