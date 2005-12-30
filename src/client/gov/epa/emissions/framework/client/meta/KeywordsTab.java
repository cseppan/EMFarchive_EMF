package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.framework.client.data.Keywords;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class KeywordsTab extends JPanel implements KeywordsTabView {

    private KeyValueTableData tableData;

    public KeywordsTab() {
        super.setName("keywordsTab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(KeyVal[] values, Keywords masterKeywords) {
        super.removeAll();
        super.add(createLayout(values, masterKeywords));
    }

    private JPanel createLayout(KeyVal[] values, Keywords masterKeywords) {
        tableData = new KeyValueTableData(values, masterKeywords);
        return new KeywordsPanel("", tableData, masterKeywords);
    }

    public KeyVal[] updates() {
        return tableData.sources();
    }

}
