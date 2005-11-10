package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.SimpleTableModel;
import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.TableData;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//FIXME: very similar to LogsTab (uneditable table displays). Refactor ?
public class DataTab extends JPanel implements DataTabView {

    private EmfFrame parentConsole;

    public DataTab(EmfFrame parentConsole) {
        super.setName("logsTab");
        this.parentConsole = parentConsole;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void displayInternalSources(InternalSource[] sources) {
        displaySources("Internal Sources", new InternalSourcesTableData(sources));
    }

    public void displayExternalSources(ExternalSource[] sources) {
        displaySources("External Sources", new ExternalSourcesTableData(sources));
    }

    private void displaySources(String title, TableData tableData) {
        super.removeAll();
        super.add(createLayout(title, tableData, parentConsole));
    }

    private JPanel createLayout(String title, TableData tableData, EmfFrame parentConsole) {
        JPanel layout = new JPanel();
        layout.setBorder(new Border(title));
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));

        layout.add(createSortFilterPane(tableData, parentConsole));

        return layout;
    }

    private JScrollPane createSortFilterPane(TableData tableData, EmfFrame parentConsole) {
        EmfTableModel model = new EmfTableModel(tableData);
        SimpleTableModel wrapperModel = new SimpleTableModel(model);

        SortFilterTablePanel panel = new SortFilterTablePanel(parentConsole, wrapperModel);
        panel.getTable().setName("sourcesTable");

        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 60));

        return scrollPane;
    }

}
