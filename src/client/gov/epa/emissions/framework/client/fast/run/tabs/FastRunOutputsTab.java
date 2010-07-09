package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.run.FastRunPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.services.fast.FastRunOutput;
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
        this.setName("Outputs");
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

    protected FastRunOutput[] getRunOutputs(int id) throws EmfException {
        return this.getSession().fastService().getFastRunOutputs(id);
    }

    @Override
    void refreshData() {

        FastRunOutput[] runOutputs = new FastRunOutput[0];
        try {
            runOutputs = this.getRunOutputs(this.getRun().getId());
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        this.table.refresh(new FastRunOutputTableData(runOutputs));
    }

    private JPanel createTablePanel(FastRun run, EmfConsole parentConsole, EmfSession session) {

        FastRunOutput[] runOutputs = new FastRunOutput[0];
        try {
            runOutputs = this.getRunOutputs(this.getRun().getId());
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        JPanel tablePanel = new JPanel(new BorderLayout());
        this.table = new SelectableSortFilterWrapper(parentConsole, new FastRunOutputTableData(runOutputs),
                sortCriteria());
        tablePanel.add(this.table, BorderLayout.CENTER);

        return tablePanel;
    }

    private SortCriteria sortCriteria() {
        return new SortCriteria(new String[] { "Type" }, new boolean[] { false }, new boolean[] { true });
    }

    public void viewOnly() {
    }
}
