package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.client.data.ListPanel;
import gov.epa.emissions.framework.client.data.MasterKeywords;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class KeywordsPanel extends JPanel {

    public KeywordsPanel(String label, KeywordsTableData tableData, MasterKeywords keywords) {
        ListPanel listPanel = new ListPanel(label, tableData);
        listPanel.setColumnEditor(keywordColumnEditor(keywords), 1, "Select from the list");

        super.add(listPanel);
    }

    private TableCellEditor keywordColumnEditor(MasterKeywords keywords) {
        JComboBox comboBox = new JComboBox();
        comboBox.setEditable(true);
        Keyword[] list = keywords.all();
        for (int i = 0; i < list.length; i++)
            comboBox.addItem(list[i].getName());

        return new DefaultCellEditor(comboBox);
    }

}
