package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.SimpleTableModel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class LogsTab extends JPanel implements LogsTabView {

    private EmfConsole parentConsole;

    public LogsTab(EmfConsole parentConsole) {
        super.setName("logsTab");
        this.parentConsole = parentConsole;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(AccessLog[] accessLogs) {
        super.removeAll();
        super.add(createAccessLogsLayout(accessLogs, parentConsole));
    }

    private JPanel createAccessLogsLayout(AccessLog[] logs, EmfConsole parentConsole) {
        JPanel layout = new JPanel();
        layout.setBorder(new Border("Access Logs"));

        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        layout.add(createSortFilterPane(logs, parentConsole));

        return layout;
    }

    private JScrollPane createSortFilterPane(AccessLog[] logs, EmfConsole parentConsole) {
        EmfTableModel model = new EmfTableModel(new AccessLogTableData(logs));
        SimpleTableModel wrapperModel = new SimpleTableModel(model);

        SortFilterTablePanel panel = new SortFilterTablePanel(parentConsole, wrapperModel);
        panel.getTable().setName("accessLogTable");

        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 60));

        return scrollPane;
    }

}
