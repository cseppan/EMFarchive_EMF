package gov.epa.emissions.framework.ui;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ScrollableTable extends JScrollPane {

    public ScrollableTable(EmfTableModel tableModel) {
        super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JTable table = table(tableModel);
        super.setViewportView(table);
    }

    private JTable table(EmfTableModel tableModel) {
        JTable table = new JTable(tableModel);
        table.setRowHeight(25);
        // essential for horizontal scrolling
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setColumnWidths(table.getColumnModel());
        table.repaint();

        return table;
    }

    private void setColumnWidths(TableColumnModel model) {
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn col = model.getColumn(i);
            String headerValue = (String) col.getHeaderValue();
            int width = headerValue.length() * 10;
            col.setMinWidth(width);
            col.setResizable(true);
        }
    }

}
