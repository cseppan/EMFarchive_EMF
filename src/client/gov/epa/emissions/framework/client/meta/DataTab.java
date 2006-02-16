package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.versions.VersionsPanel;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class DataTab extends JPanel implements DataTabView {

    private EmfConsole parentConsole;

    private SingleLineMessagePanel messagePanel;

    private DesktopManager desktopManager;

    private DataTabPresenter presenter;

    public DataTab(EmfConsole parentConsole, DesktopManager desktopManager) {
        setName("dataTab");
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
    }

    public void observe(DataTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(EmfDataset dataset) {
        super.setLayout(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        add(messagePanel, BorderLayout.PAGE_START);
        add(createLayout(dataset, messagePanel), BorderLayout.CENTER);
    }

    private JPanel createLayout(EmfDataset dataset, MessagePanel messagePanel) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(versionsPanel(dataset, messagePanel));

        return container;
    }

    private VersionsPanel versionsPanel(EmfDataset dataset, MessagePanel messagePanel) {
        VersionsPanel versionsPanel = new VersionsPanel(dataset, messagePanel, parentConsole, desktopManager);
        try {
            presenter.displayVersions(versionsPanel);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        return versionsPanel;
    }

}
