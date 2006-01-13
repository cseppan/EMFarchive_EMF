package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class VersionsEditorWindow extends ReusableInteralFrame implements VersionsEditorView {

    private EmfConsole parentConsole;

    private SingleLineMessagePanel messagePanel;

    private VersionsEditorPresenter presenter;

    private JPanel layout;

    public VersionsEditorWindow(EmfConsole parentConsole) {
        super("Versions Editor", new Dimension(600, 400), parentConsole.desktop());

        this.parentConsole = parentConsole;
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void display(EmfDataset dataset, DataEditorService service) {
        layout.setLayout(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.PAGE_START);
        layout.add(createLayout(dataset, service, messagePanel), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.PAGE_END);
    }

    private JPanel createLayout(EmfDataset dataset, DataEditorService service, MessagePanel messagePanel) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        VersionsPanel versionsPanel = createVersionsPanel(dataset, service, messagePanel);
        container.add(versionsPanel);

        return container;
    }

    private VersionsPanel createVersionsPanel(EmfDataset dataset, DataEditorService service, MessagePanel messagePanel) {
        VersionsPanel versionsPanel = new VersionsPanel(dataset, messagePanel, parentConsole);
        VersionsPresenter versionsPresenter = new VersionsPresenter(dataset, service);
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

    public void observe(VersionsEditorPresenter presenter) {
        this.presenter = presenter;
    }
}
