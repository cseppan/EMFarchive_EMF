package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.ui.EmfTableData;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.SelectableEmfTableData;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class ListPanel extends JPanel {

    private EmfTableModel tableModel;

    public ListPanel(String label, SelectableEmfTableData tableData) {
        super.add(doLayout(label, tableData));
    }

    private JPanel doLayout(String label, SelectableEmfTableData tableData) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(labelPanel(label));
        container.add(table(tableData));
        container.add(buttonsPanel(tableData));

        return container;
    }

    private JPanel labelPanel(String name) {
        JPanel panel = new JPanel(new BorderLayout());

        Label label = new Label(name);
        panel.add(label, BorderLayout.WEST);

        return panel;
    }

    private JScrollPane table(EmfTableData tableData) {
        tableModel = new EmfTableModel(tableData);

        JTable table = new JTable(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(300, 100));

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

}
