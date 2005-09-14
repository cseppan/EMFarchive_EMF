package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.client.EmfInteralFrame;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MetadataWindow extends EmfInteralFrame implements MetadataView {

    public MetadataWindow() {
        super("Metadata Editor");

        super.setSize(new Dimension(700, 425));
    }

    private Component createLayout(EmfDataset dataset) {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Summary", new SummaryTab(dataset));
        tabbedPane.addTab("Data", createTab());
        tabbedPane.addTab("Keywords", createTab());
        tabbedPane.addTab("Logs", createTab());
        tabbedPane.addTab("Info", createTab());

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private JPanel createTab() {
        JPanel panel = new JPanel(false);

        return panel;
    }

    public void display(EmfDataset dataset) {
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        contentPane.add(createLayout(dataset));

        this.setVisible(true);
    }

}
