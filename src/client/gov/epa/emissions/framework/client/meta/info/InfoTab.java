package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

//FIXME: very similar to LogsTab (uneditable table displays). Refactor ?
public class InfoTab extends JPanel implements InfoTabView {

    private EmfConsole parentConsole;

    private JPanel sourcesPanel;

    //private SingleLineMessagePanel messagePanel;

    public InfoTab(EmfConsole parentConsole) {
        setName("infoTab");
        this.parentConsole = parentConsole;

        super.setLayout(new BorderLayout());

        //messagePanel = new SingleLineMessagePanel();
        //add(messagePanel, BorderLayout.PAGE_START);
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
        JPanel tablePanel = new JPanel(new BorderLayout());
        //SimpleTableModel wrapperModel = new SimpleTableModel(model);

        SelectableSortFilterWrapper table = new SelectableSortFilterWrapper(parentConsole, tableData, null);
        tablePanel.add(table);

        return tablePanel;
    }

}
