package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FastAnalysisOutputsTab extends AbstractAnalysisFastTab {

    private SelectableSortFilterWrapper table;

    public FastAnalysisOutputsTab(FastAnalysis analysis, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList, EmfConsole parentConsole) {

        super(analysis, session, messagePanel, changeablesList, parentConsole);
        this.setName("outputs");
    }

    public void display() {

        this.setLayout(new BorderLayout());
        this.add(createTablePanel(this.getAnalysis(), this.getParentConsole(), this.getSession()), BorderLayout.CENTER);
        super.display();
    }

    protected void populateFields() {
        /*
         * no-op
         */
    }

    public void save(FastAnalysis analysis) {
        this.clearMessage();
    }

    @Override
    void refreshData() {
//        this.table.refresh(new FastOutputTableData(this.getAnalysis().getOutputs()));
    }

    private JPanel createTablePanel(FastAnalysis analysis, EmfConsole parentConsole, EmfSession session) {

        JPanel tablePanel = new JPanel(new BorderLayout());
//        this.table = new SelectableSortFilterWrapper(parentConsole, new FastOutputTableData(analysis.getOutputs()),
//                sortCriteria());
        tablePanel.add(this.table, BorderLayout.CENTER);

        return tablePanel;
    }

    private SortCriteria sortCriteria() {
        return new SortCriteria(new String[] { "Type" }, new boolean[] { false }, new boolean[] { true });
    }

    public void viewOnly() {
    }
}
