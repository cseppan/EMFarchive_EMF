package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.client.data.ListPanel;
import gov.epa.emissions.framework.services.EmfDataset;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class KeywordsTab extends JPanel implements KeywordsTabView {

    private KeywordsTableData tableData;

    public KeywordsTab() {
        super.setName("keywordsTab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(KeyVal[] values, Keyword[] keywords) {
        super.removeAll();
        super.add(createLayout(values, keywords));
    }

    private JPanel createLayout(KeyVal[] values, Keyword[] keywords) {
        tableData = new KeywordsTableData(values, keywords);
        return new ListPanel("Keywords + Values", tableData);
    }

    public void update(EmfDataset dataset) {
        dataset.setKeyVals(tableData.sources());
    }

}
