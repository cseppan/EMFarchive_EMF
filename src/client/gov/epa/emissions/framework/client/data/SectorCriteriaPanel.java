package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.SectorCriteria;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class SectorCriteriaPanel extends JPanel {

    private EmfTableModel tableModel;

    public SectorCriteriaPanel(SectorCriteriaTableData tableData) {
        super.add(doLayout(tableData));
    }

    private JPanel doLayout(SectorCriteriaTableData tableData) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(labelPanel());
        container.add(table(tableData));
        container.add(buttonsPanel(tableData));

        return container;
    }

    private JPanel labelPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        Label label = new Label("Criteria");
        panel.add(label, BorderLayout.WEST);

        return panel;
    }

    private JScrollPane table(SectorCriteriaTableData tableData) {
        tableModel = new EmfTableModel(tableData);

        JTable table = new JTable(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(300, 100));

        return new JScrollPane(table);
    }

    private JPanel buttonsPanel(final SectorCriteriaTableData tableData) {
        JPanel container = new JPanel();

        Label add = new Label("Add", "<html>&nbsp;&nbsp;&nbsp;&nbsp;<a href=''>Add</a></html>");
        add.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                SectorCriteria criterion = new SectorCriteria();
                criterion.setType("");
                criterion.setCriteria("");

                tableData.add(criterion);
                refresh();
            }
        });
        container.add(add);

        Label remove = new Label("Remove", "<html>&nbsp;&nbsp;&nbsp;&nbsp;<a href=''>Remove</a></html>");
        remove.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                SectorCriteria[] selected = tableData.getSelected();
                tableData.remove(selected);
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
