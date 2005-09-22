package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.status.StatusWindow;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

public class StatusActions {

    private UserAcceptanceTestCase testcase;

    private EmfConsole console;

    public StatusActions(EmfConsole console, UserAcceptanceTestCase testcase) {
        this.console = console;
        this.testcase = testcase;
    }

    public StatusWindow window() {
        return (StatusWindow) testcase.findInternalFrame(console, "statusWindow");
    }

    public void clear() {
        testcase.click(window(), "clear");
    }

    private JTable table() {
        return (JTable) testcase.findByName(window(), "statusMessages");
    }

    public int messageCount() {
        return table().getRowCount();
    }

    public List filter(String type) {
        JTable table = table();
        List messages = new ArrayList();
        for (int i = 0; i < messageCount(); i++) {
            String message = (String) table.getValueAt(i, 1);
            if (contains(message, type))
                messages.add(message);
        }

        return messages;
    }

    private boolean contains(String message, String type) {
        return message.indexOf("import for " + type + ":") >= 0;
    }

    public boolean hasStarted(String type, String filename) {
        List statusMessages = filter(type);
        return statusMessages.contains("Started import for " + type + ":" + filename);
    }

    public boolean hasCompleted(String type, String filename) {
        List statusMessages = filter(type);
        return statusMessages.contains("Completed import for " + type + ":" + filename);
    }
}
