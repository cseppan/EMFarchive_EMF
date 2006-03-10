package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class EditableKeywordsTab extends JPanel implements EditableKeywordsTabView, Editor {

    private EditableKeyValueTableData tableData;

    private EditableKeywordsPanel editableKeywordsPanel;

    private ManageChangeables changeablesList;

    public EditableKeywordsTab(ManageChangeables changeablesList) {
        this.changeablesList = changeablesList;
        super.setName("keywordsTab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(EmfDataset dataset, Keywords masterKeywords) {
        super.removeAll();
        removeDatasetTypeKeywords(dataset, masterKeywords);
        super.add(createLayout(dataset, masterKeywords));
    }

    private void removeDatasetTypeKeywords(EmfDataset dataset, Keywords masterKeywords) {
        Keyword[] datasetTypeKeywords = dataset.getDatasetType().getKeywords();
        for (int i = 0; i < datasetTypeKeywords.length; i++) {
            masterKeywords.remove(datasetTypeKeywords[i]);
        }
    }

    private JPanel createLayout(EmfDataset dataset, Keywords masterKeywords) {
        tableData = new EditableKeyValueTableData(dataset.getKeyVals(), dataset.getDatasetType().getKeyVals(), masterKeywords);
        editableKeywordsPanel = new EditableKeywordsPanel("", tableData, masterKeywords, changeablesList);
        return editableKeywordsPanel;
    }

    public KeyVal[] updates() throws EmfException {
        return tableData.sources();
    }

    public void commit() {
        editableKeywordsPanel.commit();
    }

}
