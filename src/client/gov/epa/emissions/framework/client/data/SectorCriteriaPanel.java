package gov.epa.emissions.framework.client.data;

import java.awt.BorderLayout;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class SectorCriteriaPanel extends JPanel {

    public SectorCriteriaPanel(String label, SectorCriteriaTableData tableData) {
        ListPanel listPanel = new ListPanel(label, tableData);
        listPanel.setColumnEditor(typesCellEditor(), 1, "Select from the list");

        super.setLayout(new BorderLayout());
        super.add(listPanel, BorderLayout.CENTER);
    }

    private TableCellEditor typesCellEditor() {
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("SCC");
        comboBox.addItem("NAICS");
        comboBox.addItem("SICS");
        comboBox.addItem("IPM Flag");// True/False values

        return new DefaultCellEditor(comboBox);
    }

}
