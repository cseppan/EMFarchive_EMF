package gov.epa.emissions.framework.client.data;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class SectorCriteriaPanel extends JPanel {

    public SectorCriteriaPanel(String label, SectorCriteriaTableData tableData) {
        ListPanel listPanel = new ListPanel(label, tableData);
        listPanel.setColumnEditor(cellEditor(), 1, "Select from the list");

        super.add(listPanel);
    }

    private TableCellEditor cellEditor() {
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("SCC");
        comboBox.addItem("NAICS");
        comboBox.addItem("IPM");

        return new DefaultCellEditor(comboBox);
    }

}
