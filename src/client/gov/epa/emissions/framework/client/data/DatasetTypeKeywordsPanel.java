package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class DatasetTypeKeywordsPanel extends JPanel {

    public DatasetTypeKeywordsPanel(DatasetType type, DatasetTypeKeywordsTableData tableData) {
        ListPanel listPanel = new ListPanel("Keywords", tableData);
        listPanel.setColumnEditor(cellEditor(type), 1, "Select from the list");

        super.add(listPanel);
    }

    private TableCellEditor cellEditor(DatasetType type) {
        JComboBox comboBox = new JComboBox();
        Keyword[] keywords = type.getKeywords();
        for (int i = 0; i < keywords.length; i++)
            comboBox.addItem(keywords[i].getName());
        comboBox.setEditable(true);

        return new DefaultCellEditor(comboBox);
    }

}
