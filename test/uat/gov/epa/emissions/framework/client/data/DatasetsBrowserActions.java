package gov.epa.emissions.framework.client.data;

import javax.swing.JTable;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;

public class DatasetsBrowserActions {

    private UserAcceptanceTestCase testcase;

    private EmfConsole console;

    private DatasetsBrowserWindow browser;

    private JTable table;

    public DatasetsBrowserActions(UserAcceptanceTestCase testcase) {
        this.testcase = testcase;
    }

    public EmfConsole openConsole() throws Exception {
        console = testcase.openConsole();
        return console;
    }

    public Object cell(int row, int col) throws Exception {
        if (table == null)
            table();
        return table.getValueAt(row, col);
    }

    public DatasetsBrowserWindow open() throws Exception {
        testcase.click(console, "manage");
        testcase.click(console, "datasets");

        browser = (DatasetsBrowserWindow) testcase.findInternalFrame(console, "datasetsBrowser");
        return browser;
    }

    public JTable table() throws Exception {
        table = (JTable) testcase.findByName(browser, "datasetsTable");
        return table;
    }

    public void close() throws Exception {
        testcase.click(browser, "close");
    }
}
