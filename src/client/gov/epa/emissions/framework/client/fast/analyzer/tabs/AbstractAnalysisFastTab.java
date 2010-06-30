package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisTabView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Cursor;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class AbstractAnalysisFastTab extends JPanel implements FastAnalysisTabView {

    private FastAnalysis analysis;

    private MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    private EmfSession session;

    private EmfConsole parentConsole;

    public AbstractAnalysisFastTab(FastAnalysis analysis, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList, EmfConsole parentConsole) {

        this.analysis = analysis;
        this.session = session;
        this.messagePanel = messagePanel;
        this.changeablesList = changeablesList;
        this.parentConsole = parentConsole;
    }

    public FastAnalysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(FastAnalysis analysis) {
        this.analysis = analysis;
    }

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    public ManageChangeables getChangeablesList() {
        return changeablesList;
    }

    public EmfSession getSession() {
        return session;
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }

    public void showError(String message) {

        this.messagePanel.setError(message);
        this.refreshLayout();
    }

    public void showMessage(String message) {

        this.messagePanel.setMessage(message);
        this.refreshLayout();
    }

    public void clearMessage() {

        this.messagePanel.clear();
        this.refreshLayout();
    }

    public void refresh(FastAnalysis analysis) {

        this.setAnalysis(analysis);

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            this.refreshData();
            this.refreshLayout();
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    abstract void refreshData();

    void refreshLayout() {
        this.validate();
    }

    public void display() {
        this.populateFields();
    }

    abstract void populateFields();

    public void modify() {
        // NOTE Auto-generated method stub

    }

    public void save(FastAnalysis analysis) throws EmfException {
        // NOTE Auto-generated method stub

    }

    public void viewOnly() {
        // NOTE Auto-generated method stub

    }

}
