package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

public class CriteriaPanel extends JPanel {

    public CriteriaPanel(SectorCriteriaTableData criteriaTableData) {
        super.add(doLayout(criteriaTableData));
    }

    private JPanel doLayout(SectorCriteriaTableData criteriaTableData) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(new Label("Criteria"));
        container.add(table(criteriaTableData));

        return container;
    }

    private JScrollPane table(SectorCriteriaTableData criteriaTableData) {
        TableModel model = new EmfTableModel(criteriaTableData);

        JTable table = new JTable(model);
        table.setPreferredScrollableViewportSize(new Dimension(300, 40));
        JScrollPane scrollPane = new JScrollPane(table);
        return scrollPane;
    }

}
