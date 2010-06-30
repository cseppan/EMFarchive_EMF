package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.run.FastRunPresenter;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FastRunOutputsTab extends AbstractFastRunTab {

    private SelectableSortFilterWrapper table;

    public FastRunOutputsTab(FastRun run, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList, EmfConsole parentConsole, FastRunPresenter presenter) {

        super(run, session, messagePanel, changeablesList, parentConsole, presenter);
        this.setName("outputs");
    }

    public void display() {

        this.setLayout(new BorderLayout());
        this.add(createTablePanel(this.getRun(), this.getParentConsole(), this.getSession()), BorderLayout.CENTER);
        super.display();
    }

    protected void populateFields() {
        /*
         * no-op
         */
    }

    public void save(FastRun run) {
        this.clearMessage();
    }

    @Override
    void refreshData() {
        // this.table.refresh(new FastRunOutputTableData(this.getRun().getOutputs()));
    }

    private JPanel createTablePanel(FastRun run, EmfConsole parentConsole, EmfSession session) {

        JPanel tablePanel = new JPanel(new BorderLayout());
        // this.table = new SelectableSortFilterWrapper(parentConsole, new FastRunOutputTableData(run.getOutputs()),
        // sortCriteria());
        // tablePanel.add(this.table, BorderLayout.CENTER);

        return tablePanel;
    }

    private SortCriteria sortCriteria() {
        return new SortCriteria(new String[] { "Type" }, new boolean[] { false }, new boolean[] { true });
    }

    public void viewOnly() {
    }
}
