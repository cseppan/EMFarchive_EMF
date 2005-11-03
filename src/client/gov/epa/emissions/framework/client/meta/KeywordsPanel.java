package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.client.data.ListPanel;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class KeywordsPanel extends JPanel {

    public KeywordsPanel(String label, KeywordsTableData tableData, Keyword[] keywords) {
        ListPanel listPanel = new ListPanel(label, tableData);
        listPanel.setColumnEditor(keywordColumnEditor(keywords), 1, "Select from the list");

        super.add(listPanel);
    }

    private TableCellEditor keywordColumnEditor(Keyword[] keywords) {
        JComboBox comboBox = new JComboBox();
        comboBox.setEditable(true);
        for (int i = 0; i < keywords.length; i++)
            comboBox.addItem(keywords[i].getName());

        return new DefaultCellEditor(comboBox);
    }

}
