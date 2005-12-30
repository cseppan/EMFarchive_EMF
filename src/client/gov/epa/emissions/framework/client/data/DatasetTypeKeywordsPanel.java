package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Keyword;

import java.awt.BorderLayout;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class DatasetTypeKeywordsPanel extends JPanel {

    public DatasetTypeKeywordsPanel(KeywordsTableData tableData, Keyword[] keywords) {
        ListPanel listPanel = new ListPanel("Keywords", tableData);
        listPanel.setColumnEditor(cellEditor(keywords), 1, "Select from the list");

        super.setLayout(new BorderLayout());
        super.add(listPanel, BorderLayout.CENTER);
    }

    private TableCellEditor cellEditor(Keyword[] keywords) {
        JComboBox comboBox = new JComboBox();
        comboBox.setEditable(true);

        for (int i = 0; i < keywords.length; i++)
            comboBox.addItem(keywords[i].getName());

        return new DefaultCellEditor(comboBox);
    }

}
