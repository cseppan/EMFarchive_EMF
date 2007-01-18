package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class TableColumnWidth {

    private JTable table;

    private static final int MAX_WIDTH = 300;

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
        table.repaint();
    }

    private void width(ColumnMetaData metaData, TableColumn tableColumn) {
        String header = (String) tableColumn.getHeaderValue();
        // factor(10) is selected by trial and error
        int width = header.length() * 10;
        int dbColumnSize = metaData.getSize() * 10;
        
        if (dbColumnSize < 0) { //for column type of String(*) -- text
            tableColumn.setPreferredWidth(2 * MAX_WIDTH);
            return;
        }
        
        dbColumnSize = (dbColumnSize < MAX_WIDTH) ? dbColumnSize : MAX_WIDTH;
        int preferedWidth = (width > dbColumnSize) ? width : dbColumnSize;

        tableColumn.setPreferredWidth(preferedWidth);
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
