package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.ui.TableColumnWidth;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class DataTableWidget extends EditableTable {

    protected TableMetadata tableMetadata;

    public DataTableWidget(TableModel tableModel, TableMetadata tableMetadata) {
        super(tableModel);
        this.tableMetadata = tableMetadata;
        new TableColumnWidth(this, tableMetadata).columnWidths();
        headerRender();
    }

    private void headerRender() {
        JTableHeader tableHeader = new JTableHeader(getColumnModel());
        tableHeader.setBackground(UIManager.getDefaults().getColor("TableHeader.background"));
        tableHeader.setDefaultRenderer(new TableHeaderRenderer(tableHeader, tableMetadata));
        setTableHeader(tableHeader);
    }


    public class TableHeaderRenderer extends JPanel implements TableCellRenderer {

        private JTextArea textArea;
        
        private TableMetadata metadata;

        public TableHeaderRenderer(JTableHeader tableHeader, TableMetadata tableMetadata) {
            this.textArea = new JTextArea();
            this.metadata= tableMetadata;
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
            return header + type(header);
        }

        private String type(String header) {
            ColumnMetaData data = metadata.columnMetadata(header);
            return data == null? "":"\n"+parse(data.getType())+"("+data.getSize()+")";
        }

        private String parse(String type) {
            int index = type.lastIndexOf('.');
            return type.substring(index+1);
        }
    }

}
