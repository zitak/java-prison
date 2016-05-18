
package cz.muni.fi.pv168.gui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import cz.muni.fi.pv168.prison.backend.Sentence;

/**
 *
 * @author Zita
 */
public class SentencesTableModel extends AbstractTableModel {
    private List<Sentence> sentences = new ArrayList<Sentence>();
    
    @Override
    public int getRowCount() {
        return sentences.size();
    }
    
    @Override
    public int getColumnCount() {
        return 5;
    }
    
    
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Sentence sentence = sentences.get(rowIndex);
        switch(columnIndex) {
            case 0:
                return sentence.getPrisonerId();
            case 1:
                return sentence.getCellId();
            case 2:
                return sentence.getStartDay();
            case 3:
                return sentence.getEndDay();
            case 4:
                return sentence.getPunishment();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    public void addSentence(Sentence sentence) {
        sentences.add(sentence);
        int lastRow = sentences.size() -1;
        fireTableRowsInserted(lastRow, lastRow);
    }
    
    public void deleteSentence(int index) {
        sentences.remove(index);
        fireTableRowsDeleted(index, index);
    }
    
    public void updateSentence(Sentence sentence, int index) {
        this.sentences.set(index, sentence);
        fireTableRowsUpdated(index, index);
    }
    
    public void refresh() {
        this.sentences.clear();
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return "Prisoner's Id";
            case 1:
                return "Cell's Id";
            case 2:
                return "Starting Day";
            case 3:
                return "Ending Day";
            case 4:
                return "Description";
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            case 1:
                return Long.class;
            case 2:
                return LocalDate.class;
            case 3:
                return LocalDate.class;
            case 4:
                return String.class;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Sentence sentence = sentences.get(rowIndex);
        switch(columnIndex) {
            case 0:
                sentence.setPrisonerId((Long) aValue);
                break;
            case 1:
                sentence.setCellId((Long) aValue);
                break;
            case 2:
                sentence.setStartDay((LocalDate) aValue);
                break;
            case 3:
                sentence.setEndDay((LocalDate) aValue);
                break;
            case 4:
                sentence.setPunishment((String) aValue);
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
                return true;
            case 4:
                return true;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
}
