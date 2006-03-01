package gov.epa.emissions.framework.ui;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;

public class TableColumnWidth {

    private JTable table;

    private TableMetadata tableMetadata;

    public TableColumnWidth(JTable table, TableMetadata tableMetadata) {
        this.table = table;
        this.tableMetadata = tableMetadata;
    }

    public void columnWidths() {
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();
        ColumnMetaData metaData = null;
        for (int i = 0; i < columnCount; i++) {
            String columnName = table.getColumnName(i);
            if ((metaData = exist(columnName, tableMetadata)) != null) {
                TableColumn tableColumn = columnModel.getColumn(i);
                width(metaData, tableColumn);
            }
        }
    }

    private void width(ColumnMetaData metaData, TableColumn tableColumn) {
        String header = (String) tableColumn.getHeaderValue();
        int width = header.length() * 10;
        int dbColumnSize = metaData.getSize() + 10;
        tableColumn.setWidth((width > dbColumnSize) ? width : dbColumnSize);
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
