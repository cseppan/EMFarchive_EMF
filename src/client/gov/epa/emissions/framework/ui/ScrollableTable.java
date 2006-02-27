package gov.epa.emissions.framework.ui;

import java.awt.event.KeyListener;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ScrollableTable extends JScrollPane {

    private JTable table;

    public ScrollableTable(EmfTableModel tableModel) {
        super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

        table = table(tableModel);
        super.setViewportView(table);
    }

    public ScrollableTable(JTable table) {
        super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.table = table;
        super.setViewportView(table);
    }

    private JTable table(EmfTableModel tableModel) {
        JTable table = new JTable(tableModel);
        table.setRowHeight(18);

        enableScrolling(table);
        setColWidthsBasedOnColNames(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        if (tableModel.getRowCount() == 1) {
            if ((tableModel.getColumnClass(0).isInstance(Boolean.TRUE))
                    && tableModel.getColumnName(0).equalsIgnoreCase("Select")) {
                tableModel.setValueAt(Boolean.TRUE, 0, 0);
            }
        }

        return table;
    }

    private void enableScrolling(JTable table) {
        // essential for horizontal scrolling
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    private void setColWidthsBasedOnColNames(JTable table) {
        TableColumnModel model = table.getColumnModel();

        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn col = model.getColumn(i);
            String headerValue = (String) col.getHeaderValue();
            int width = headerValue.length() * 10;
            col.setMinWidth(width);
            col.setResizable(true);
        }
        table.repaint();
    }

    public void disableScrolling() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    }

    public void moveToBottom() {
        JScrollBar vertical = getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum() - 1);

        selectLastRow();
    }

    public void selectLastRow() {
        int total = table.getModel().getRowCount();
        table.setRowSelectionInterval(total - 1, total - 1);
    }

    public void addListener(KeyListener listener) {
        table.addKeyListener(listener);
    }

}
