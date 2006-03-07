package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.EditableTableModel;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.data.TableColumnHeaders;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.TableColumnWidth;

public class DataEditorTable extends EditableTable {

    private MessagePanel messagePanel;
    
    private TableMetadata tableMetadata;

    public DataEditorTable(EditableTableModel tableModel, TableMetadata tableMetadata, MessagePanel messagePanel) {
        super(tableModel);
        this.tableMetadata = tableMetadata;
        this.messagePanel = messagePanel;
        new TableColumnHeaders(this,tableMetadata).renderHeader();
        new TableColumnWidth(this, tableMetadata).columnWidths();
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
            messagePanel.setError("Enter a value no longer than " + metadata.getSize()+" characters");
            return getValueAt(row, column); // return the current value;
        }

        return value;

    }

    private boolean sizeCheck(String value, ColumnMetaData metadata) {
        int dbColumnSize = metadata.getSize();
        // -1=> no size constraints
        return (dbColumnSize == -1 || value == null || dbColumnSize >= value.length());
    }

}
