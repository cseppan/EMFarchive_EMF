package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.framework.client.data.Keywords;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class EditableKeywordsTab extends JPanel implements EditableKeywordsTabView, Editor {

    private EditableKeyValueTableData tableData;

    private EditableKeywordsPanel editableKeywordsPanel;

    public EditableKeywordsTab() {
        super.setName("keywordsTab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(KeyVal[] values, Keywords masterKeywords) {
        super.removeAll();
        super.add(createLayout(values, masterKeywords));
    }

    private JPanel createLayout(KeyVal[] values, Keywords masterKeywords) {
        tableData = new EditableKeyValueTableData(values, masterKeywords);
        editableKeywordsPanel = new EditableKeywordsPanel("", tableData, masterKeywords);
        return editableKeywordsPanel;
    }

    public KeyVal[] updates() {
        return tableData.sources();
    }

    public void commit() {
       editableKeywordsPanel.commit();
    }

}
