package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
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

    private SingleLineMessagePanel messagePanel;

    public MetadataWindow() {
        super("Dataset Properties Editor");

        super.setSize(new Dimension(700, 485));
    }

    private JTabbedPane createTabbedPane(EmfDataset dataset, MessagePanel messagePanel) {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Summary", createSummaryTab(dataset, messagePanel));
        tabbedPane.addTab("Data", createTab());
        tabbedPane.addTab("Keywords", createTab());
        tabbedPane.addTab("Logs", createTab());
        tabbedPane.addTab("Info", createTab());

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private SummaryTab createSummaryTab(EmfDataset dataset, MessagePanel messagePanel) {
        SummaryTab view = new SummaryTab(dataset, messagePanel);
        presenter.add(view);

        return view;
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
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        panel.add(createTabbedPane(dataset, messagePanel), BorderLayout.CENTER);
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
        JPanel buttonsPanel = new JPanel();

        Button save = new Button("Save", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doSave();
            }
        });
        buttonsPanel.add(save);

        Button close = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        getRootPane().setDefaultButton(close);
        buttonsPanel.add(close);

        return buttonsPanel;
    }

    public void observe(MetadataPresenter presenter) {
        this.presenter = presenter;
    }

    // FIXME: should this be mandatory for all EmfViews ?
    public void showError(String message) {
        // TODO: error should go away at some point. when ?
        messagePanel.setError(message);
    }

}
