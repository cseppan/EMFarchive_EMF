package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.data.ListPanel;
import gov.epa.emissions.framework.services.EmfKeyVal;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class KeywordsTab extends JPanel implements KeywordsTabView {

    public KeywordsTab() {
        super.setName("keywordsTab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(EmfKeyVal[] values) {
        super.removeAll();
        super.add(createLayout(values));
    }

    private JPanel createLayout(EmfKeyVal[] values) {
        KeywordsTableData tableData = new KeywordsTableData(values);
        return new ListPanel("Keywords + Values", tableData);
    }

}
