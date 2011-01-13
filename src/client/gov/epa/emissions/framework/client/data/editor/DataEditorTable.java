package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.EditableTableModel;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.data.DoubleRenderer;
import gov.epa.emissions.framework.client.data.TableColumnHeaders;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.TableColumnWidth;

public class DataEditorTable extends EditableTable {

    public DataEditorTable(EditableTableModel tableModel, TableMetadata tableMetadata, MessagePanel messagePanel) {
        super(tableModel);
        new TableColumnHeaders(this, tableMetadata).renderHeader();
        new TableColumnWidth(this, tableMetadata).columnWidths();
        setDefaultRenderer(Double.class, new DoubleRenderer());
        setDefaultRenderer(Float.class, new DoubleRenderer());
        NumericTableCellEditor numericTableCellEditor = new NumericTableCellEditor(messagePanel);
        setDefaultEditor(Double.class, numericTableCellEditor);
        setDefaultEditor(Float.class, numericTableCellEditor);
        setDefaultEditor(Integer.class, numericTableCellEditor);
        setDefaultEditor(String.class, new StringTableCellEditor(tableMetadata, messagePanel));
        DateDataCellEditor dateCellEditor = new DateDataCellEditor(messagePanel);
        setDefaultEditor(java.sql.Timestamp.class, dateCellEditor);
        setDefaultEditor(java.sql.Time.class, dateCellEditor);
        setDefaultEditor(java.sql.Date.class, dateCellEditor);
        setDefaultEditor(java.util.Date.class, dateCellEditor);
        setDefaultEditor(java.util.Calendar.class, dateCellEditor);
        setDefaultEditor(java.util.GregorianCalendar.class, dateCellEditor);
    }

    public void setValueAt(Object value, int row, int column) {
        super.setValueAt(value, row, column);
    }
}
