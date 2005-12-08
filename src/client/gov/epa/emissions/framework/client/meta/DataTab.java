package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.SimpleTableModel;
import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.ScrollableTable;
import gov.epa.emissions.framework.ui.TableData;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//FIXME: very similar to LogsTab (uneditable table displays). Refactor ?
public class DataTab extends JPanel implements DataTabView {

    private EmfFrame parentConsole;

    private JPanel versionsPanel;

    private JPanel sourcesPanel;

    public DataTab(EmfFrame parentConsole) {
        super.setName("logsTab");
        this.parentConsole = parentConsole;

        super.setLayout(new BorderLayout());
        super.add(createLayout(), BorderLayout.CENTER);
    }

    private JPanel createLayout() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        sourcesPanel = new JPanel();
        container.add(sourcesPanel);

        versionsPanel = new JPanel();
        versionsPanel.setBorder(new Border("Versions"));
        container.add(versionsPanel);

        return container;
    }

    public void displayInternalSources(InternalSource[] sources) {
        displaySources("Internal Sources", new InternalSourcesTableData(sources));
    }

    public void displayExternalSources(ExternalSource[] sources) {
        displaySources("External Sources", new ExternalSourcesTableData(sources));
    }

    public void displayVersions(Version[] versions) {
        AbstractTableData tableData = new VersionsTableData(versions);
        JScrollPane table = new ScrollableTable(new EmfTableModel(tableData));
        versionsPanel.add(table);
    }

    private void displaySources(String title, TableData tableData) {
        sourcesPanel.removeAll();
        sourcesPanel.add(createSourcesLayout(title, tableData, parentConsole));
    }

    private JPanel createSourcesLayout(String title, TableData tableData, EmfFrame parentConsole) {
        JPanel sourcesPanel = new JPanel();
        sourcesPanel.setBorder(new Border(title));
        sourcesPanel.add(createSortFilterPane(tableData, parentConsole));

        return sourcesPanel;
    }

    private JScrollPane createSortFilterPane(TableData tableData, EmfFrame parentConsole) {
        EmfTableModel model = new EmfTableModel(tableData);
        SimpleTableModel wrapperModel = new SimpleTableModel(model);

        SortFilterTablePanel panel = new SortFilterTablePanel(parentConsole, wrapperModel);
        panel.getTable().setName("sourcesTable");

        return new JScrollPane(panel);
    }

}
