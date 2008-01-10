package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypeKeyValueTableData;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class EditableKeywordsTab extends JPanel implements EditableKeywordsTabView, Editor {

    private EditableKeyValueTableData tableData;

    private EditableKeywordsPanel editableKeywordsPanel;

    private ManageChangeables changeablesList;

    private EmfConsole parent;

    private SingleLineMessagePanel messagePanel;

    public EditableKeywordsTab(ManageChangeables changeablesList, EmfConsole parent) {
        this.changeablesList = changeablesList;
        this.parent = parent;
        super.setName("editKeywordsTab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(EmfDataset dataset, Keywords masterKeywords) {
        super.removeAll();
        messagePanel = new SingleLineMessagePanel();
        super.add(messagePanel);
        super.add(createDSTypeKeywordsPanel(dataset.getDatasetType().getKeyVals()));
        super.add(createDSKeywordsPanel(dataset, masterKeywords));
    }

    private JPanel createDSKeywordsPanel(EmfDataset dataset, Keywords masterKeywords) {
        tableData = new EditableKeyValueTableData(dataset.getKeyVals(), masterKeywords);
        editableKeywordsPanel = new EditableKeywordsPanel("", tableData, masterKeywords, changeablesList, parent);
        editableKeywordsPanel.setBorder(BorderFactory.createTitledBorder("Keywords Specific to Dataset"));
        
        return editableKeywordsPanel;
    }

    private JPanel createDSTypeKeywordsPanel(KeyVal[] vals) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Keywords Specific to Dataset Type"));

        TableData tableData = new DatasetTypeKeyValueTableData(vals);
        JTable table = new JTable(new EmfTableModel(tableData));
        table.setRowHeight(16);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    public KeyVal[] updates() throws EmfException {
        return tableData.sources();
    }

    public void commit() {
        editableKeywordsPanel.commit();
    }

}
