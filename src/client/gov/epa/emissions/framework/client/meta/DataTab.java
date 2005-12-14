package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

//FIXME: very similar to LogsTab (uneditable table displays). Refactor ?
public class DataTab extends JPanel implements DataTabView {

    private EmfConsole parentConsole;

    private VersionsPanel versionsPanel;

    private SingleLineMessagePanel messagePanel;

    public DataTab(EmfDataset dataset, DataEditorService service, EmfConsole parentConsole) {
        setName("dataTab");
        this.parentConsole = parentConsole;

        super.setLayout(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        add(messagePanel, BorderLayout.PAGE_START);
        add(createLayout(dataset, service, messagePanel), BorderLayout.CENTER);
    }

    private JPanel createLayout(EmfDataset dataset, DataEditorService service, MessagePanel messagePanel) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        versionsPanel = createVersionsPanel(dataset, service, messagePanel);
        container.add(versionsPanel);

        return container;
    }

    private VersionsPanel createVersionsPanel(EmfDataset dataset, DataEditorService service, MessagePanel messagePanel) {
        VersionsPanel versionsPanel = new VersionsPanel(dataset, messagePanel, parentConsole);
        VersionsPresenter versionsPresenter = new VersionsPresenter(dataset, service);
        versionsPresenter.observe(versionsPanel);

        return versionsPanel;
    }

    public void displayVersions(Version[] versions, InternalSource[] sources) {
        versionsPanel.display(versions, sources);
    }

}
