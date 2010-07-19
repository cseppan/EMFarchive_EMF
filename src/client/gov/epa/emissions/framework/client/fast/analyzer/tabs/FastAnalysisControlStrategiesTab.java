package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisPresenter;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;

@SuppressWarnings("serial")
public class FastAnalysisControlStrategiesTab extends AbstractFastAnalysisTab {

    public FastAnalysisControlStrategiesTab(FastAnalysis analysis, EmfSession session, MessagePanel messagePanel,
            EmfInternalFrame parentInternalFrame, DesktopManager desktopManager, EmfConsole parentConsole,
            FastAnalysisPresenter presenter) {

        super(analysis, session, messagePanel, parentInternalFrame, desktopManager, parentConsole, presenter);
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
