package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.data.ListPanel;
import gov.epa.emissions.framework.services.EmfKeyVal;
import gov.epa.emissions.framework.services.EmfKeyword;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class KeywordsPanel extends JPanel {

    public KeywordsPanel(String label, KeywordsTableData tableData, EmfKeyword[] keywords) {
        ListPanel listPanel = new ListPanel(label, tableData);
        listPanel.setColumnEditor(keywordColumnEditor(tableData.sources(), keywords), 1, "Select from the list");

        super.add(listPanel);
    }

    private TableCellEditor keywordColumnEditor(EmfKeyVal[] vals, EmfKeyword[] keywords) {
        JComboBox comboBox = new JComboBox();
        for (int i = 0; i < vals.length; i++)
            comboBox.addItem(vals[i].getKeyword());

        for (int i = 0; i < keywords.length; i++)
            comboBox.addItem(keywords[i].getKeyword());

        return new DefaultCellEditor(comboBox);
    }

}
