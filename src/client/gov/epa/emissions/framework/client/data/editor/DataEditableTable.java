package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class DataEditableTable extends EditableTable {

    private TableMetadata tableMetaData;

    private MessagePanel messagePanel;

    public DataEditableTable(TableModel tableModel, TableMetadata tableMetadata, MessagePanel messagePanel) {
        super(tableModel);
        this.tableMetaData = tableMetadata;
        this.messagePanel = messagePanel;
        headerRender();
    }

    private void headerRender() {
        JTableHeader tableHeader = new JTableHeader(getColumnModel());
        tableHeader.setBackground(UIManager.getDefaults().getColor("TableHeader.background"));
        tableHeader.setDefaultRenderer(new TableHeaderRenderer(tableHeader));
        setTableHeader(tableHeader);
    }

    public void setValueAt(Object value, int row, int column) {
        messagePanel.clear();
        if (getColumnClass(column) == String.class) {
            value = validate((String) value, row, column);
        }
        super.setValueAt(value, row, column);
    }

    private Object validate(String value, int row, int column) {
        ColumnMetaData metadata = columnMetadata(getColumnName(column));
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

    private ColumnMetaData columnMetadata(String columnName) {
        ColumnMetaData[] cols = tableMetaData.getCols();
        for (int i = 0; i < cols.length; i++) {
            if (columnName.equalsIgnoreCase(cols[i].getName())) {
                return cols[i];
            }
        }
        return null;
    }

    public class TableHeaderRenderer extends JPanel implements TableCellRenderer {
        // TODO: get the table meta data to get the type of the column

        private JTextArea textArea;

        public TableHeaderRenderer(JTableHeader tableHeader) {
            this.textArea = new JTextArea();
            textAreaSettings(tableHeader);
            setLayout(new BorderLayout());
            add(textArea);
        }

        private void textAreaSettings(JTableHeader tableHeader) {
            textArea.setForeground(tableHeader.getForeground());
            textArea.setBackground(tableHeader.getBackground());
            textArea.setFont(tableHeader.getFont());
            textArea.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            textArea.setText(headerText(value));
            return this;
        }

        private String headerText(Object value) {
            String header = ((value == null) ? "" : value.toString());
            return header + "\n(type)";
        }

    }

}
