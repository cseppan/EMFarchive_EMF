package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypeKeyValueTableData;
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

public class KeywordsTab extends JPanel implements KeywordsTabView {

    private SingleLineMessagePanel messagePanel;
    
    public KeywordsTab() {
        super.setName("viewKeywordsTab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(EmfDataset dataset) {
        messagePanel = new SingleLineMessagePanel();
        super.add(messagePanel);
        super.add(createDSTypeKeywordsPanel(dataset.getDatasetType().getKeyVals()));
        super.add(createDSKeywordsPanel(dataset.getKeyVals()));
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
    
    private JPanel createDSKeywordsPanel(KeyVal[] values) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Keywords Specific to Dataset"));

        KeyValueTableData tableData = new KeyValueTableData(values);
        JTable table = new JTable(new EmfTableModel(tableData));
        table.setRowHeight(20);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);


        return panel;
    }

}
