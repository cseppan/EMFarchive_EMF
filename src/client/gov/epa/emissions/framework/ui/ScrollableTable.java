package gov.epa.emissions.framework.ui;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ScrollableTable extends JScrollPane {

    private JTable table;

    public ScrollableTable(EmfTableModel tableModel) {
        super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

        table = table(tableModel);
        super.setViewportView(table);
    }

    private JTable table(EmfTableModel tableModel) {
        JTable table = new JTable(tableModel);
        table.setRowHeight(25);

        enableScrolling(table);
        setColWidthsBasedOnColNames(table);

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
        vertical.setValue(vertical.getMaximum());
    }

}
