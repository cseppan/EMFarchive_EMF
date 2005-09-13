package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.EmfDataset;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class SummaryTab extends JPanel {

    private EmfDataset dataset;

    public SummaryTab(EmfDataset dataset) {
        this.dataset = dataset;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setLayout();
    }

    private void setLayout() {
        super.add(createOverview());

        JPanel localePanel = createLocalePanel();
        JPanel statusPanel = createStatusPanel();

        JPanel lowerPanel = new JPanel();
        lowerPanel.add(localePanel);
        lowerPanel.add(statusPanel);

        super.add(lowerPanel);
    }

    private JPanel createStatusPanel() {
        return new JPanel();
    }

    private JPanel createLocalePanel() {
        return new JPanel();
    }

    private JPanel createOverview() {
        JPanel panel = new JPanel();
        
        
        return panel;
    }

}
