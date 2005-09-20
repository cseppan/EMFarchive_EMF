package gov.epa.emissions.framework.client.data;

import javax.swing.JTable;

import abbot.tester.JTableTester;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.exim.ExportWindow;

public class DatasetsBrowserActions {

    private UserAcceptanceTestCase testcase;

    private EmfConsole console;

    private DatasetsBrowserWindow browser;

    private JTable table;

    public DatasetsBrowserActions(EmfConsole console, UserAcceptanceTestCase testcase) {
        this.console = console;
        this.testcase = testcase;
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

    public void select(int row) throws Exception {
        refresh();

        JTableTester tester = new JTableTester();
        tester.actionSelectCell(table(), row, 1);// 'Select' col is 2nd
    }

    public void refresh() throws Exception {
        testcase.click(browser, "refresh");
    }

    public ExportWindow export() throws Exception {
        testcase.click(browser, "export");
        return (ExportWindow) testcase.findInternalFrame(console, "exportWindow");
    }

    public void export(int row) throws Exception {
        select(row);
        export();
    }

    public void selectLast() throws Exception {
        select(table().getRowCount() - 1);
    }

    public void selectDataset(String dataset) throws Exception {
        refresh();

        int rows = table().getRowCount();
        for (int i = 0; i < rows; i++) {
            String actualDataset = (String) cell(i, 2);
            if (dataset.equals(actualDataset)) {
                select(i);
                return;
            }
        }
    }
}
