package org.globus.ogce.beans.filetransfer.gui.monitor;

import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

/**
 * A panel which Displays a set of jobs in a queue. The user can add any
 * number of columns to the table. It is assumed that the Jobid is the
 * first column and it is used to distinguish values contained in each row.
 *
 * @author Beulah Kurian Alunkal
 * @version 1.0
 */
public class QueuePanel extends JPanel implements MouseListener {
    private static Logger logger =
            Logger.getLogger(QueuePanel.class.getName());

    private JTable queueTable = null;
    private JPopupMenu jpopup = null;
    JTable completeTable = null;

    public QueuePanel() {
        setLayout(new BorderLayout());
        setSize(200, 300);
        setVisible(true);
        queueTable
                = new JTable();

        JScrollPane jobsScrollPane
                = new JScrollPane(queueTable);

        add(jobsScrollPane, BorderLayout.CENTER);

        //Current transfers border
        Border jobsBorder = BorderFactory.createTitledBorder(
                BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                "Transfer Queue");
        setBorder(jobsBorder);
        queueTable.addMouseListener(this);

        jpopup = new JPopupMenu();

        completeTable = queueTable;
    } // end of constructor

    public void createHeader(String cols[]) {
        QueueTableModel tableModel = new QueueTableModel();
        Vector columnIDs = new Vector();
        for (int i = 0; i < cols.length; i++) {
            columnIDs.add(cols[i]);
        }
        tableModel.setColumnIdentifiers(columnIDs);
        queueTable.setModel(tableModel);

        queueTable.getTableHeader().setReorderingAllowed(false);


    }

    public void addTransfer(String cols[]) {
        DefaultTableModel tableModel
                = (DefaultTableModel) getModel();
        int row = tableModel.getRowCount();
        Vector rowData = new Vector();
        for (int i = 0; i < cols.length; i++) {
            rowData.add(cols[i]);
        }
        tableModel.insertRow(row, rowData);
        return;
    }


    public void updateTransfer(String cols[]) {
        DefaultTableModel tableModel
                = (DefaultTableModel) getModel();
        if (tableModel.getRowCount() > 0) {
            int selectedRow = getRowIndex(cols[0]);
            for (int i = 0; i < cols.length; i++) {
                if (cols[i] != null) {
                    tableModel.setValueAt(cols[i], selectedRow, i);
                }
            }
        }
    }

    public void setFocus(String jobID) {
        queueTable.repaint();
        int row = getRowIndex(jobID);
        DefaultTableModel tableModel
                = (DefaultTableModel) getModel();
        int noRows = tableModel.getRowCount();
        if (noRows > 1) {
            if (row < noRows - 1) {
                row += 1;
            }
            queueTable.scrollRectToVisible(queueTable.getCellRect(row, 0, true));
            //   queueTable.revalidate();

        }
    }

    public void deleteTransfer(String jobid) {
        DefaultTableModel tableModel
                = (DefaultTableModel) getModel();
        int selectedRow = getRowIndex(jobid);
        tableModel.removeRow(selectedRow);

    }

    public JPopupMenu getPopupMenu() {
        return jpopup;
    }

    public int getRowIndex(String jobid) {
        DefaultTableModel tableModel
                = (DefaultTableModel) getModel();
        int noRows = tableModel.getRowCount();
        int selectedRow = 0;
        for (int j = 0; j < noRows; j++) {
            if (jobid.equals(getColumnValue(j, 0))) {
                selectedRow = j;
                break;
            }
        }
        return selectedRow;
    }
    
    public int getRowIndex(java.lang.String value, int col)
    {
        javax.swing.table.DefaultTableModel tableModel = getModel();
        int noRows = tableModel.getRowCount();
        int selectedRow = 0;
        for(int j = 0; j < noRows; j++)
        {
            if(!value.equals(getColumnValue(j, col)))
                continue;
            selectedRow = j;
            break;
        }

        return selectedRow;
    }

    public void clear() {
        DefaultTableModel tableModel
                = (DefaultTableModel) getModel();
        int rowCount = tableModel.getRowCount();
        if (rowCount > 0) {
            for (int row = 0; row < rowCount; row++) {
                tableModel.removeRow(0);

            }

        }
        return;
    }

    public int tableSize() {
        DefaultTableModel tableModel
                = (DefaultTableModel) getModel();
        int rowCount = tableModel.getRowCount();
        return rowCount;
    }

    public DefaultTableModel getModel() {
        return (DefaultTableModel) queueTable.getModel();
    }

    public String getSelectedJob() {
        int selectedRow = queueTable.getSelectedRow();
        return getColumnValue(selectedRow, 0);
    }

    public String getColumnValue(int row, int col) {
        DefaultTableModel tableModel
                = (DefaultTableModel) getModel();
        return (String) tableModel.getValueAt(row, col);
    }

    public void setColumnValue(int row, int col, String value) {
        DefaultTableModel tableModel
                = (DefaultTableModel) getModel();
        tableModel.setValueAt(value, row, col);
    }

    //####################################################
    //		MouseListener methods
    //####################################################
    public void mousePressed(MouseEvent me) {
        Point clickPoint = new Point(me.getX(), me.getY());

        int selectedRow = queueTable.rowAtPoint(clickPoint);


        queueTable.setRowSelectionInterval(selectedRow, selectedRow);

        if (me.isPopupTrigger()) {

            //Popup menu trigger button was pressed, so display
            //the choices for interacting with the selected transfer job

            //to show the popup uncomment this line.
            jpopup.show(queueTable, me.getX(), me.getY());


        }
        if (me.getClickCount() == 2) {
            //Double click event
            logger.info("Double clicked");
        }
    }

    public void mouseClicked(MouseEvent me) {
    }

    public void mouseReleased(MouseEvent me) {
        if (me.isPopupTrigger()) {
            mousePressed(me);
        }

    }

    public void mouseEntered(MouseEvent me) {
    }

    public void mouseExited(MouseEvent me) {
    }

    public void createButtonsPanel(String args[], ActionListener parent) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 0));
        for (int i = 0; i < args.length; i++) {
            JButton newButton = new JButton(args[i]);
            newButton.addActionListener(parent);
            buttonPanel.add(newButton);
        }
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void createCheckBoxPanel(JCheckBox args[]) {
        JPanel checkBoxPanel = new JPanel();
        //	checkBoxPanel.setLayout(new GridLayout(1,0));
        for (int i = 0; i < args.length; i++) {
            checkBoxPanel.add(args[i]);
        }
        //	checkBoxPanel.setBorder(new TitledBorder(new BevelBorder(BevelBorder.RAISED),"Display"));
        add(checkBoxPanel, BorderLayout.NORTH);
    }

    public void addPopupItems(String items[], ActionListener parent) {
        int sep = 0;
        for (int i = 0; i < items.length; i++) {

            JMenuItem menuItem = new JMenuItem(items[i]);
            menuItem.addActionListener(parent);
            jpopup.add(menuItem);
            sep++;
            if (sep == 2) {
                jpopup.addSeparator();
                sep = 0;
            }
        }
    }

    public void showRows(String status, int col) {
        if (status.equals("All")) {
            queueTable = completeTable;
        } else {
            logger.info("Not yet implemented");
            /*	int noRows = tableModel.getRowCount();

            for(int j =0 ; j< noRows;j++){
            if(status.equals(getColumnValue(j,col))){
            //		addTransfer(

            }
            }*/
        }
    }

} // end of class

class QueueTableModel extends DefaultTableModel {
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}

