package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.versions.VersionsPanel;
import gov.epa.emissions.framework.client.meta.versions.VersionsViewPresenter;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class DataTab extends JPanel implements DataTabView {

    private EmfConsole parentConsole;

    private SingleLineMessagePanel messagePanel;

    private DesktopManager desktopManager;

    public DataTab(EmfConsole parentConsole, DesktopManager desktopManager) {
        setName("dataTab");
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
    }

    public void display(EmfDataset dataset, DataViewService service) {
        super.setLayout(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        add(messagePanel, BorderLayout.PAGE_START);
        add(createLayout(dataset, service, messagePanel), BorderLayout.CENTER);
    }

    private JPanel createLayout(EmfDataset dataset, DataViewService service, MessagePanel messagePanel) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(versionsPanel(dataset, service, messagePanel));

        return container;
    }

    private VersionsPanel versionsPanel(EmfDataset dataset, DataViewService service, MessagePanel messagePanel) {
        VersionsPanel versionsPanel = new VersionsPanel(dataset, messagePanel, parentConsole, desktopManager);
        VersionsViewPresenter versionsPresenter = new VersionsViewPresenter(dataset, service);
        try {
            versionsPresenter.display(versionsPanel);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        return versionsPanel;
    }

}
