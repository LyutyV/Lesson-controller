/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainFrame;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author incode3
 */
public class LessonTableModel extends AbstractTableModel {

    private ArrayList<Lesson> lessons = new ArrayList();
    
    @Override
    public int getRowCount() {
        return this.lessons.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Lesson L = this.lessons.get(rowIndex);
            switch (columnIndex)
            {
                case 0:
                    return L.Group;
                case 1:
                    return (String) new SimpleDateFormat("dd.MM.yyyy").format(L.Date);
                case 2:
                    return L.isSelected;
            }
            return null;
    }
    
    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Группа";
            case 1:
                return "Дата";
            case 2:
                return "Выделить";
        }
        return null;
    }
    
    @Override
    public int findColumn(String columnName) {
        for (int i = 0; i < getColumnCount(); i++) {
            if (columnName.equals(getColumnName(i))) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 2);
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Lesson H = this.lessons.get(rowIndex);
        switch (columnIndex)
        {
            case 0:
                H.Group = aValue.toString();
                break;
            case 1:
                H.Date = (Date) aValue;
                break;
            case 2:
                H.isSelected = (boolean) aValue;
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    
    public void addLesson(Lesson l) {
        this.lessons.add(l);
        this.fireTableDataChanged();
    }
    
    public void insertLessonAt(int index, Lesson l)
    {
        this.lessons.add(index, l);
        this.fireTableDataChanged();
    }
    
    public void removeLessonAt(int index)
    {
        this.lessons.remove(index);
        this.fireTableDataChanged();
    }

    public void removeLesson(Lesson l)
    {
        this.lessons.remove(l);
    }

    public void setLesson(Lesson l, int index)
    {
        this.lessons.remove(index);
        this.lessons.add(index, l);
    }
    
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
    
    public void Clear()
    {
        this.lessons.clear();
    }
}
