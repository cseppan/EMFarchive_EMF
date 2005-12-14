package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.SimpleTableModel;
import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.TableData;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

//FIXME: very similar to LogsTab (uneditable table displays). Refactor ?
public class DataTab extends JPanel implements DataTabView {

    private EmfConsole parentConsole;

    private VersionsPanel versionsPanel;

    private JPanel sourcesPanel;

    private SingleLineMessagePanel messagePanel;

    public DataTab(EmfDataset dataset, DataEditorService service, EmfConsole parentConsole) {
        setName("logsTab");
        this.parentConsole = parentConsole;

        super.setLayout(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        add(messagePanel, BorderLayout.PAGE_START);
        add(createLayout(dataset, service, messagePanel), BorderLayout.CENTER);
    }

    private JPanel createLayout(EmfDataset dataset, DataEditorService service, MessagePanel messagePanel) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        sourcesPanel = new JPanel(new BorderLayout());
        container.add(sourcesPanel);

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

    public void displayInternalSources(InternalSource[] sources) {
        displaySources("Internal Sources", new InternalSourcesTableData(sources));
    }

    public void displayExternalSources(ExternalSource[] sources) {
        displaySources("External Sources", new ExternalSourcesTableData(sources));
    }

    public void displayVersions(Version[] versions, InternalSource[] sources) {
        versionsPanel.display(versions, sources);
    }

    private void displaySources(String title, TableData tableData) {
        sourcesPanel.removeAll();
        sourcesPanel.setBorder(new Border(title));
        sourcesPanel.add(createSortFilterPane(tableData, parentConsole));
    }

    private JPanel createSortFilterPane(TableData tableData, EmfConsole parentConsole) {
        EmfTableModel model = new EmfTableModel(tableData);
        SimpleTableModel wrapperModel = new SimpleTableModel(model);

        SortFilterTablePanel panel = new SortFilterTablePanel(parentConsole, wrapperModel);
        panel.getTable().setName("sourcesTable");

        panel.setPreferredSize(new Dimension(450, 200));// essential for SortFilterTablePanel

        return panel;
    }

}
