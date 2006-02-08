package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class VersionedDataWindow extends ReusableInteralFrame implements VersionedDataView {

    private EmfConsole parentConsole;

    private SingleLineMessagePanel messagePanel;

    private VersionedDataPresenter presenter;

    private JPanel layout;

    public VersionedDataWindow(EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Data Versions Editor", new Dimension(750, 350), parentConsole.desktop(),desktopManager);
        this.parentConsole = parentConsole;
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void display(EmfDataset dataset, EditVersionsPresenter versionsPresenter) {
        layout.setLayout(new BorderLayout());
        this.setTitle("Dataset Versions Editor: " + dataset.getName());
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.PAGE_START);
        layout.add(createVersionsPanelLayout(versionsPresenter, dataset, messagePanel), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.PAGE_END);

        super.display();
    }

    private JPanel createVersionsPanelLayout(EditVersionsPresenter versionsPresenter, EmfDataset dataset,
            MessagePanel messagePanel) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        EditVersionsPanel versionsPanel = createVersionsPanel(versionsPresenter, dataset, messagePanel);
        container.add(versionsPanel);

        return container;
    }

    private EditVersionsPanel createVersionsPanel(EditVersionsPresenter versionsPresenter, EmfDataset dataset,
            MessagePanel messagePanel) {
        EditVersionsPanel versionsPanel = new EditVersionsPanel(dataset, messagePanel, parentConsole, desktopManager);
        try {
            versionsPresenter.display(versionsPanel);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        return versionsPanel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel closePanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });

        closePanel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);
        panel.add(closePanel, BorderLayout.EAST);

        return panel;
    }

    public void observe(VersionedDataPresenter presenter) {
        this.presenter = presenter;
    }
}
