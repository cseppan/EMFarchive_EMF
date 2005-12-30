package gov.epa.emissions.framework.client.meta;

import java.awt.BorderLayout;

import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.client.data.ListPanel;
import gov.epa.emissions.framework.client.data.Keywords;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class KeywordsPanel extends JPanel {

    public KeywordsPanel(String label, KeyValueTableData tableData, Keywords masterKeywords) {
        ListPanel listPanel = new ListPanel(label, tableData);
        listPanel.setColumnEditor(keywordColumnEditor(masterKeywords), 1, "Select from the list");

        super.setLayout(new BorderLayout());
        super.add(listPanel, BorderLayout.CENTER);
    }

    private TableCellEditor keywordColumnEditor(Keywords masterKeywords) {
        JComboBox comboBox = new JComboBox();
        comboBox.setEditable(true);
        Keyword[] list = masterKeywords.all();
        for (int i = 0; i < list.length; i++)
            comboBox.addItem(list[i].getName());

        return new DefaultCellEditor(comboBox);
    }

}
