package gov.epa.emissions.framework.client.casemanagement.history;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ShowHistoryTab extends JPanel implements ShowHistoryTabView, RefreshObserver {
    private EmfConsole parentConsole;

    private ShowHistoryTabPresenter presenter;

    private int caseId;

    private JobMessagesTableData tableData;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;
    
    private MessagePanel messagePanel;

    private EmfSession session;

    public ShowHistoryTab(EmfConsole parentConsole, MessagePanel messagePanel, EmfSession session) {
        super.setName("showCaseHistoryTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.session = session;
        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, int caseId, ShowHistoryTabPresenter presenter) {
        super.removeAll();

        this.caseId = caseId;
        this.presenter = presenter;

        try {
            super.add(createLayout(new JobMessage[0], parentConsole), BorderLayout.CENTER);
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case histories. " + e.getMessage());
        }

        kickPopulateThread();
    }

    private void kickPopulateThread() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                retrieveJobMsgs();
            }
        });
        populateThread.start();
    }

    public void retrieveJobMsgs() {
        try {
            messagePanel.setMessage("Please wait while retrieving all case histories...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            refreshTab(presenter.getJobMessages(this.caseId));
            messagePanel.clear();
        } catch (Exception e) {
            e.printStackTrace();
            messagePanel.setError("Cannot retrieve all case histories. " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void refreshTab(JobMessage[] msgs) throws Exception {
        super.removeAll();
        super.add(createLayout(msgs, parentConsole), BorderLayout.CENTER);
    }

    private JPanel createLayout(JobMessage[] msgs, EmfConsole parentConsole) throws Exception {
        final JPanel layout = new JPanel(new BorderLayout());
        layout.add(tablePanel(msgs, parentConsole), BorderLayout.CENTER);

        return layout;
    }

    private JPanel tablePanel(JobMessage[] msgs, EmfConsole parentConsole) {
        tableData = new JobMessagesTableData(msgs, session);
        selectModel = new SortFilterSelectModel(new EmfTableModel(tableData));

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createSortFilterPanel(parentConsole), BorderLayout.CENTER);

        return tablePanel;
    }

    private JScrollPane createSortFilterPanel(EmfConsole parentConsole) {
        SortFilterSelectionPanel sortFilterPanel = new SortFilterSelectionPanel(parentConsole, selectModel);
        sortFilterPanel.sort(sortCriteria());

        JScrollPane scrollPane = new JScrollPane(sortFilterPanel);
        sortFilterPanel.setPreferredSize(new Dimension(450, 60));
        return scrollPane;
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Job", "Received Date", "Exec. Mod. Date" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true }, new boolean[] { false, false, false });
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    public void doRefresh() throws EmfException {
        try {
            kickPopulateThread();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }

    }

}
