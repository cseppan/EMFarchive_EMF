package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.SimpleTableModel;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.EmfFrame;
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

    public void displayInternalSources(InternalSource[] internalSources) {
        // FIXME: activate this on tab-click

        super.removeAll();
        super.add(createLayout(internalSources, parentConsole));
    }

    private JPanel createLayout(InternalSource[] internalSources, EmfFrame parentConsole) {
        JPanel layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));

        layout.add(createSortFilterPane(internalSources, parentConsole));

        return layout;
    }

    private JScrollPane createSortFilterPane(InternalSource[] internalSources, EmfFrame parentConsole) {
        EmfTableModel model = new EmfTableModel(new InternalSourcesTableData(internalSources));
        SimpleTableModel wrapperModel = new SimpleTableModel(model);

        SortFilterTablePanel panel = new SortFilterTablePanel(parentConsole, wrapperModel);
        panel.getTable().setName("internalSourcesTable");

        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 60));

        return scrollPane;
    }

}
