package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastAnalysisOutput;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FastAnalysisOutputsTab extends AbstractFastAnalysisTab {

    private SelectableSortFilterWrapper table;

    public FastAnalysisOutputsTab(FastAnalysis analysis, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList, EmfConsole parentConsole, FastAnalysisPresenter presenter) {

        super(analysis, session, messagePanel, changeablesList, parentConsole, presenter);
        this.setName("Outputs");
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

        FastAnalysisOutput[] analysisOutputs = getFastAnalysisOutputs();
        this.table.refresh(new FastAnalysisOutputTableData(analysisOutputs));
    }

    private JPanel createTablePanel(FastAnalysis analysis, EmfConsole parentConsole, EmfSession session) {

        FastAnalysisOutput[] analysisOutputs = getFastAnalysisOutputs();

        JPanel tablePanel = new JPanel(new BorderLayout());
        this.table = new SelectableSortFilterWrapper(parentConsole, new FastAnalysisOutputTableData(analysisOutputs),
                sortCriteria());
        tablePanel.add(this.table, BorderLayout.CENTER);

        return tablePanel;
    }

    private FastAnalysisOutput[] getFastAnalysisOutputs() {

        FastAnalysisOutput[] analysisOutputs = new FastAnalysisOutput[0];
        try {
            analysisOutputs = this.getFastAnalysisOutputs(this.getAnalysis().getId());
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        return analysisOutputs;
    }

    protected FastAnalysisOutput[] getFastAnalysisOutputs(int id) throws EmfException {
        return this.getSession().fastService().getFastAnalysisOutputs(id);
    }

    private SortCriteria sortCriteria() {
        return new SortCriteria(new String[] { "Type" }, new boolean[] { false }, new boolean[] { true });
    }

    public void viewOnly() {
    }
}
