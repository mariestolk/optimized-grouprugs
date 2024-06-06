/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbvis.visualsummaries.motionrugs;

import javax.swing.table.DefaultTableModel;


/**
 *
 * @author buchmueller
 */
public class StatsTableModel extends DefaultTableModel {

    public StatsTableModel(Object[] colnames, Object[][] values) {
        super(values, colnames);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
    
    @Override
    public Class getColumnClass(int columnIdx){
        return String.class;
    }

}
