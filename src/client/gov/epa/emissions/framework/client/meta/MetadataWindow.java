package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MetadataWindow extends DisposableInteralFrame implements MetadataView {

    private MetadataPresenter presenter;

    public MetadataWindow() {
        super("Metadata Editor");

        super.setSize(new Dimension(700, 450));
    }

    private JTabbedPane createTabbedPane(EmfDataset dataset) {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Summary", new SummaryTab(dataset));
        tabbedPane.addTab("Data", createTab());
        tabbedPane.addTab("Keywords", createTab());
        tabbedPane.addTab("Logs", createTab());
        tabbedPane.addTab("Info", createTab());

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    // TODO: other tabs
    private JPanel createTab() {
        JPanel panel = new JPanel(false);

        return panel;
    }

    public void display(EmfDataset dataset) {
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createTabbedPane(dataset), BorderLayout.CENTER);
        panel.add(createBottomPanel(), BorderLayout.PAGE_END);

        contentPane.add(panel);

        super.display();
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createButtonsPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel createButtonsPanel() {
        Button close = new Button("close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.notifyClose();
            }
        });
        getRootPane().setDefaultButton(close);

        JPanel closePanel = new JPanel();
        closePanel.add(close);

        return closePanel;
    }

    public void register(MetadataPresenter presenter) {
        this.presenter = presenter;
    }

}
