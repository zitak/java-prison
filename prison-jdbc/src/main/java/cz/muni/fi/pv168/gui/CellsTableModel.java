
package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.prison.backend.Cell;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Zita
 */
public class CellsTableModel extends AbstractTableModel {
    
    private List<Cell> cells = new ArrayList<Cell>();

    @Override
    public int getRowCount() {
        return cells.size();
    }
    
    @Override
    public int getColumnCount() {
        return 3;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex){
        Cell cell = cells.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return cell.getId();
            case 1:
                return cell.getFloor();
            case 2:
                return cell.getCapacity();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    public void addCell(Cell cell){
        cells.add(cell);
        int lastRow = cells.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return "Id";
            case 1:
                return "Floor";
            case 2:
                return "Capacity";
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex){
        switch(columnIndex){
            case 0:
                return Integer.class;
            case 1:
                return Integer.class;
            case 2:
                return Integer.class;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Cell cell = cells.get(rowIndex);
        switch(columnIndex) {
            case 0:
                cell.setId((Long) aValue);
                break;
            case 1:
                cell.setFloor((Integer) aValue);
                break;
            case 2:
                cell.setCapacity((Integer) aValue);
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
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
}
