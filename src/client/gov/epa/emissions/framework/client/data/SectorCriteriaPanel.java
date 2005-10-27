package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.SectorCriteria;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
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

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                tableData.add(new SectorCriteria());
                tableModel.refresh();

                SectorCriteriaPanel.this.revalidate();
            }
        });
        container.add(addButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            }
        });
        container.add(deleteButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.EAST);

        return panel;
    }

}
