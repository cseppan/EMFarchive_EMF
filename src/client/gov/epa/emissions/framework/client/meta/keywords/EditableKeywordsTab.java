package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.services.EmfDataset;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class EditableKeywordsTab extends JPanel implements EditableKeywordsTabView, Editor {

    private EditableKeyValueTableData tableData;

    private EditableKeywordsPanel editableKeywordsPanel;

    public EditableKeywordsTab() {
        super.setName("keywordsTab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(EmfDataset dataset, Keywords masterKeywords) {
        super.removeAll();
        //TODO: where to do this removal?
        removeDatasetTypeKeywords(dataset,masterKeywords);
        super.add(createLayout(dataset, masterKeywords));
    }

    private void removeDatasetTypeKeywords(EmfDataset dataset, Keywords masterKeywords) {
        Keyword[] datasetTypeKeywords = dataset.getDatasetType().getKeywords();
        for (int i = 0; i < datasetTypeKeywords.length; i++) {
            masterKeywords.remove(datasetTypeKeywords[i]);
        }
    }

    private JPanel createLayout(EmfDataset dataset, Keywords masterKeywords) {
        tableData = new EditableKeyValueTableData(dataset, masterKeywords);
        editableKeywordsPanel = new EditableKeywordsPanel("", tableData, masterKeywords);
        return editableKeywordsPanel;
    }

    public KeyVal[] updates() throws EmfException {
        return tableData.sources();
    }

    public void commit() {
       editableKeywordsPanel.commit();
    }

}
