package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.framework.client.data.MasterKeywords;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class KeywordsTab extends JPanel implements KeywordsTabView {

    private KeywordsTableData tableData;

    public KeywordsTab() {
        super.setName("keywordsTab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(KeyVal[] values, MasterKeywords keywords) {
        super.removeAll();
        super.add(createLayout(values, keywords));
    }

    private JPanel createLayout(KeyVal[] values, MasterKeywords keywords) {
        tableData = new KeywordsTableData(values, keywords);
        return new KeywordsPanel("", tableData, keywords);
    }

    public KeyVal[] updates() {
        return tableData.sources();
    }

}
