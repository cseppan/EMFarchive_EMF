package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.gui.SimpleTableModel;
import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.TableData;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

//FIXME: very similar to LogsTab (uneditable table displays). Refactor ?
public class InfoTab extends JPanel implements InfoTabView {

    private EmfConsole parentConsole;

    private JPanel sourcesPanel;

    private SingleLineMessagePanel messagePanel;

    public InfoTab(EmfConsole parentConsole) {
        setName("infoTab");
        this.parentConsole = parentConsole;

        super.setLayout(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        add(messagePanel, BorderLayout.PAGE_START);
        add(createLayout(), BorderLayout.CENTER);
    }

    private JPanel createLayout() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        sourcesPanel = new JPanel(new BorderLayout());
        container.add(sourcesPanel);

        return container;
    }

    public void displayInternalSources(InternalSource[] sources) {
        displaySources("Data Tables", new InternalSourcesTableData(sources));
    }

    public void displayExternalSources(ExternalSource[] sources) {
        displaySources("External Files", new ExternalSourcesTableData(sources));
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
