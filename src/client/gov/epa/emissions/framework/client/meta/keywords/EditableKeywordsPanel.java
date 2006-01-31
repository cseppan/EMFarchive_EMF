package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.client.data.EditableTablePanel;

import java.awt.BorderLayout;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class EditableKeywordsPanel extends JPanel implements Editor {

    private EditableTablePanel editableTablePanel;

    public EditableKeywordsPanel(String label, EditableKeyValueTableData tableData, Keywords masterKeywords) {
        editableTablePanel = new EditableKeyValueTablePanel(label, tableData);
        editableTablePanel.setColumnEditor(keywordColumnEditor(masterKeywords), 1, "Select from the list");

        super.setLayout(new BorderLayout());
        super.add(editableTablePanel, BorderLayout.CENTER);
    }

    private TableCellEditor keywordColumnEditor(Keywords masterKeywords) {
        JComboBox comboBox = new JComboBox();
        comboBox.setEditable(true);
        Keyword[] list = masterKeywords.all();
        for (int i = 0; i < list.length; i++)
            comboBox.addItem(list[i].getName());
        
        return new DefaultCellEditor(comboBox);
    }

    public void commit() {
        editableTablePanel.commit();
    }

}
