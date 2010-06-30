package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;

@SuppressWarnings("serial")
public class FastAnalysisControlStrategiesTab extends AbstractAnalysisFastTab {

    public FastAnalysisControlStrategiesTab(FastAnalysis analysis, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList, EmfConsole parentConsole) {

        super(analysis, session, messagePanel, changeablesList, parentConsole);
        this.setName("controlStrategies");
    }

    public void display() {

        this.setLayout(new BorderLayout());
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
    }

    public void viewOnly() {
    }
}
