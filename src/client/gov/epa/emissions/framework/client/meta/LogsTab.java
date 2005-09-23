package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.LoggingServices;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class LogsTab extends JPanel implements LogsTabView {

    public LogsTab(EmfDataset dataset, LoggingServices loggingServices, EmfFrame parentConsole) throws EmfException {
        super.setName("logsTab");

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        super.add(createLayout(dataset, loggingServices, parentConsole));
    }

    private JPanel createLayout(EmfDataset dataset, LoggingServices loggingServices, EmfFrame parentConsole)
            throws EmfException {
        JPanel layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));

        layout.add(createSortFilterPane(dataset, loggingServices, parentConsole));

        return layout;
    }

    private JScrollPane createSortFilterPane(EmfDataset dataset, LoggingServices loggingServices, EmfFrame parentConsole)
            throws EmfException {
        EmfTableModel model = new EmfTableModel(new AccessLogTableData(dataset, loggingServices));
        SortFilterSelectModel selectModel = new SortFilterSelectModel(model);

        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
        panel.getTable().setName("accessLogTable");

        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 60));

        return scrollPane;
    }

    public void display(AccessLog[] accessLogs) {
        // FIXME: activate this on tab-click
    }
}
