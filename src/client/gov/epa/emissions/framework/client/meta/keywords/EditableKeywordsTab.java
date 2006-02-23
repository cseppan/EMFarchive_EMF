package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ChangeObserver;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class EditableKeywordsTab extends JPanel implements EditableKeywordsTabView, Editor {

    private EditableKeyValueTableData tableData;

    private EditableKeywordsPanel editableKeywordsPanel;
    
    private ManageChangeables changeablesList;
    
    private ChangeObserver changeObserver;

    public EditableKeywordsTab(ManageChangeables changeablesList) {
        this.changeablesList = changeablesList;
        super.setName("keywordsTab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(EmfDataset dataset, Keywords masterKeywords) {
        super.removeAll();
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
        editableKeywordsPanel = new EditableKeywordsPanel("", tableData, masterKeywords, changeablesList);
        listenForKeyEvents();
        return editableKeywordsPanel;
    }

    public KeyVal[] updates() throws EmfException {
        return tableData.sources();
    }

    public void commit() {
       editableKeywordsPanel.commit();
    }
    
    private void listenForKeyEvents() {
        editableKeywordsPanel.addListener(new KeyTabKeyListener());
        editableKeywordsPanel.addComboBoxListener(new KeyTabComboBoxChangesListener());
    }

    public void observeChanges(ChangeObserver observer) {
        this.changeObserver = observer;
    }
    
    public class KeyTabKeyListener extends KeyAdapter {
        public void keyTyped(KeyEvent e) {
            if (changeObserver != null)
                changeObserver.onChange();
        }
    }

    public class KeyTabComboBoxChangesListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if (changeObserver != null)
                changeObserver.onChange();
        }
    }

}
