package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.ui.TableData;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.SelectableEmfTableData;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ListPanel extends JPanel {

    private EmfTableModel tableModel;

    private JTable table;

    public ListPanel(String label, SelectableEmfTableData tableData) {
        super.add(doLayout(label, tableData));
    }

    private JPanel doLayout(String label, SelectableEmfTableData tableData) {
        JPanel container = new JPanel(new BorderLayout());

        container.add(labelPanel(label), BorderLayout.PAGE_START);
        container.add(table(tableData), BorderLayout.CENTER);
        container.add(buttonsPanel(tableData), BorderLayout.PAGE_END);

        return container;
    }

    private JPanel labelPanel(String name) {
        JPanel panel = new JPanel(new BorderLayout());

        Label label = new Label(name);
        panel.add(label, BorderLayout.WEST);

        return panel;
    }

    private JScrollPane table(TableData tableData) {
        tableModel = new EmfTableModel(tableData);

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setPreferredScrollableViewportSize(new Dimension(300, 200));

        return new JScrollPane(table);
    }

    private JPanel buttonsPanel(final SelectableEmfTableData tableData) {
        JPanel container = new JPanel();

        Label add = new Label("Add", "<html>&nbsp;&nbsp;&nbsp;&nbsp;<a href=''>Add</a></html>");
        add.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableData.addBlankRow();
                refresh();
            }
        });
        container.add(add);

        Label remove = new Label("Remove", "<html>&nbsp;&nbsp;&nbsp;&nbsp;<a href=''>Remove</a></html>");
        remove.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableData.removeSelected();
                refresh();
            }
        });
        container.add(remove);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private void refresh() {
        tableModel.refresh();
        super.revalidate();
    }

    public void setColumnEditor(TableCellEditor editor, int columnIndex, String toolTip) {
        TableColumnModel colModel = table.getColumnModel();
        TableColumn col = colModel.getColumn(columnIndex);
        col.setCellEditor(editor);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(toolTip);
        col.setCellRenderer(renderer);
    }

}
