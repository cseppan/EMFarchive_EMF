package gov.epa.emissions.framework.client.meta.logs;

import java.awt.BorderLayout;

import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

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
        JPanel layout = new JPanel(new BorderLayout(5, 10));
        layout.setBorder(new Border("Access Logs"));

        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        layout.add(tablePanel(logs, parentConsole));

        return layout;
    }

    private JPanel tablePanel(AccessLog[] logs, EmfConsole parentConsole) {
        JPanel tablePanel = new JPanel(new BorderLayout());
        //SimpleTableModel wrapperModel = new SimpleTableModel(model);

        SelectableSortFilterWrapper table = new SelectableSortFilterWrapper(parentConsole, new LogsTableData(logs), null);
//        panel.getTable().setName("accessLogTable");
//
//        JScrollPane scrollPane = new JScrollPane(panel);
//        panel.setPreferredSize(new Dimension(450, 60));
        tablePanel.add(table);
        return tablePanel;
    }

}
