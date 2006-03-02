package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.gui.EditableTableModel;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.data.DataTableWidget;
import gov.epa.emissions.framework.ui.MessagePanel;

public class DataEditorTable extends DataTableWidget {

    private MessagePanel messagePanel;

    public DataEditorTable(EditableTableModel tableModel, TableMetadata tableMetadata, MessagePanel messagePanel) {
        super(tableModel, tableMetadata);
        this.messagePanel = messagePanel;
    }

    public void setValueAt(Object value, int row, int column) {
        messagePanel.clear();
        if (getColumnClass(column) == String.class) {
            value = validate((String) value, row, column);
        }
        super.setValueAt(value, row, column);
    }

    private Object validate(String value, int row, int column) {
        ColumnMetaData metadata = tableMetadata.columnMetadata(getColumnName(column));
        if (metadata == null) {
            return value;
        }
        if (!sizeCheck(value, metadata)) {
            messagePanel.setError("The column size is " + metadata.getSize());
            return getValueAt(row, column); // return the current value;
        }

        return value;

    }

    private boolean sizeCheck(String value, ColumnMetaData metadata) {
        int dbColumnSize = metadata.getSize();
        // -1=> no size constraints
        return (dbColumnSize == -1 || value == null || dbColumnSize > value.length());
    }

}
