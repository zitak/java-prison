package cz.muni.fi.pv168.prison.gui;

import cz.muni.fi.pv168.prison.backend.Prisoner;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Zita
 */
public class PrisonersTableModel extends AbstractTableModel {
    
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("cz/muni/fi/pv168/prison/gui/strings", Locale.getDefault());
    private List<Prisoner> prisoners = new ArrayList<Prisoner>();

    @Override
    public int getRowCount() {
        return prisoners.size();
    }
    
    @Override
    public int getColumnCount() {
        return 4;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex){
        Prisoner prisoner = prisoners.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return prisoner.getId();
            case 1:
                return prisoner.getName();
            case 2:
                return prisoner.getSurname();
            case 3:
                return prisoner.getBorn();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    public void addPrisoner(Prisoner prisoner){
        prisoners.add(prisoner);
        int lastRow = prisoners.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }
    
    public void deleterisoner(int index) {
        this.prisoners.remove(index);
        fireTableRowsDeleted(index, index);
    }
    
    public void updatePrisoner(Prisoner prisoner, int index) {
        prisoners.set(index, prisoner);
        fireTableRowsUpdated(index, index);
        
    }
    
    public void refresh() {
        this.prisoners.clear();
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return resourceBundle.getString("table_prisoners_header_id");
            case 1:
                return resourceBundle.getString("table_prisoners_header_name");
            case 2:
                return resourceBundle.getString("table_prisoners_header_surname");
            case 3:
                return resourceBundle.getString("table_prisoners_header_born");
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex){
        switch(columnIndex){
            case 0:
                return Long.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return LocalDate.class;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Prisoner prisoner = prisoners.get(rowIndex);
        switch(columnIndex) {
            case 0:
                prisoner.setId((Long) aValue);
                break;
            case 1:
                prisoner.setName((String) aValue);
                break;
            case 2:
                prisoner.setSurname((String) aValue);
                break;
            case 3:
                prisoner.setBorn((LocalDate) aValue);
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch(columnIndex) {
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return true;
            case 3:
                return false;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
}

