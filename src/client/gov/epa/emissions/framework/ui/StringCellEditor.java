package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

public class StringCellEditor extends DefaultCellEditor {

    private TableMetadata tableMetadata;

    private String editedColumnName;

    private MessagePanel messagePanel;
    
    private String oldValue;

    public StringCellEditor(TableMetadata tableMetadata, MessagePanel messagePanel) {
        super(new JTextField());
        this.tableMetadata = tableMetadata;
        this.messagePanel = messagePanel;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.oldValue = (String) value;
        this.editedColumnName = table.getColumnName(column);
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    public boolean stopCellEditing() {
        String editedValue = (String) super.getCellEditorValue();
        if (!validate(editedValue, editedColumnName)) {
            delegate.setValue(oldValue);
        }
        return super.stopCellEditing();
    }

    private boolean validate(Object editedValue, String editedColumnName) {
        // TODO: check for null values
        String value = editedValue.toString();
        ColumnMetaData column = exist(editedColumnName, tableMetadata);
        if (column == null) {
            return true;
        }
        return sizeCheck(value, column.getSize());
    }

    private boolean sizeCheck(String value, int size) {
        if (size != -1 && value.length() <= size) {
            return true;
        }
        messagePanel.setError("The maximun size of the column is " + size);
        return false;
    }

    private ColumnMetaData exist(String columnName, TableMetadata tableMetadata) {
        ColumnMetaData[] cols = tableMetadata.getCols();
        for (int i = 0; i < cols.length; i++) {
            if (columnName.equalsIgnoreCase(cols[i].getName())) {
                return cols[i];
            }
        }
        return null;
    }

}
