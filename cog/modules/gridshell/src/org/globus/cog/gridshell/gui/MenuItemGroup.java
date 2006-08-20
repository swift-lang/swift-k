/*
 * 
 */
package org.globus.cog.gridshell.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JCheckBoxMenuItem;

/**
 * 
 */
public class MenuItemGroup {
    private Collection items = new LinkedList();
    
    public void add(JCheckBoxMenuItem item) {
        final JCheckBoxMenuItem i = item;
        i.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                update(i);
            }            
        });
        items.add(i);
    }
    private synchronized void update(JCheckBoxMenuItem i) {
        if(i.isSelected()) {
            Iterator iItems = getItems().iterator();
            while(iItems.hasNext()) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem)iItems.next();
                if(item!=i) {
                    item.setSelected(false);
                }
            }
         }else {
            i.setSelected(true);
         }
    }
    public Collection getItems() {
        return items;
    }

}
