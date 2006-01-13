package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.versions.EditVersionsPanel;
import gov.epa.emissions.framework.client.meta.versions.EditVersionsPresenter;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class DataTab extends JPanel implements DataTabView {

    private EmfConsole parentConsole;

    private SingleLineMessagePanel messagePanel;

    public DataTab(EmfConsole parentConsole) {
        setName("dataTab");
        this.parentConsole = parentConsole;
    }

    public void display(EmfDataset dataset, DataEditorService service) {
        super.setLayout(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        add(messagePanel, BorderLayout.PAGE_START);
        add(createLayout(dataset, service, messagePanel), BorderLayout.CENTER);
    }

    private JPanel createLayout(EmfDataset dataset, DataEditorService service, MessagePanel messagePanel) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(versionsPanel(dataset, service, messagePanel));

        return container;
    }

    private EditVersionsPanel versionsPanel(EmfDataset dataset, DataEditorService service, MessagePanel messagePanel) {
        EditVersionsPanel versionsPanel = new EditVersionsPanel(dataset, messagePanel, parentConsole);
        EditVersionsPresenter versionsPresenter = new EditVersionsPresenter(dataset, service);
        try {
            versionsPresenter.display(versionsPanel);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        return versionsPanel;
    }

}
