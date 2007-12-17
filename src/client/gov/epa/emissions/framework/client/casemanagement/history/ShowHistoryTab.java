package gov.epa.emissions.framework.client.casemanagement.history;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class ShowHistoryTab extends JPanel implements ShowHistoryTabView, RefreshObserver {
    private EmfConsole parentConsole;

    private ShowHistoryTabPresenter presenter;

    private int caseId;

    private JobMessagesTableData tableData;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private EmfSession session;

    private ComboBox jobCombo;

    private List<CaseJob> caseJobs; 

    private CaseJob selectedJob=null;

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
            getAllJobs();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        try {
            super.add(createLayout(new JobMessage[0], parentConsole), BorderLayout.CENTER);
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case histories. " + e.getMessage());
        }

  //      kickPopulateThread(new JobMessage[0]);
    }

    private void kickPopulateThread(final JobMessage[] msgs) {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                retrieveJobMsgs(msgs);
            }
        });
        populateThread.start();
    }

    public synchronized void retrieveJobMsgs(JobMessage[] msgs) {
        try {
            messagePanel.setMessage("Please wait while retrieving all case histories...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            refreshTab(msgs);
            messagePanel.clear();
            super.revalidate();
        } catch (Exception e) {
            e.printStackTrace();
            messagePanel.setError("Cannot retrieve all case histories. " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void refreshTab(JobMessage[] msgs) throws Exception {
        selectedJob=(CaseJob) jobCombo.getSelectedItem();
        super.removeAll();
        super.add(createLayout(msgs, parentConsole), BorderLayout.CENTER);
    }

    private JPanel createLayout(JobMessage[] msgs, EmfConsole parentConsole) throws Exception {
        final JPanel layout = new JPanel(new BorderLayout());
        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(msgs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(), BorderLayout.PAGE_END);
        return layout;
    }
    
    private void getAllJobs() throws EmfException {
        this.caseJobs = new ArrayList<CaseJob>();
        caseJobs.add(new CaseJob("All"));
        caseJobs.addAll(Arrays.asList(presenter.getCaseJobs()));
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        jobCombo=new ComboBox("Select One", caseJobs.toArray(new CaseJob[0]));
        jobCombo.setPreferredSize(new Dimension(300,20));
        if (selectedJob!=null){
            jobCombo.setSelectedItem(selectedJob);
        }
        jobCombo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectedJob=(CaseJob) jobCombo.getSelectedItem();
                
                try {
                    if (selectedJob == null){
                        doRefresh(new JobMessage[0]);
                        return; 
                    }
                    JobMessage[] msgs=presenter.getJobMessages(caseId, selectedJob.getId());
                    doRefresh(msgs);
                } catch (EmfException exc) {
                    messagePanel.setError("Could not retrieve all historys for job " );
                }
            }
        });
        layoutGenerator.addLabelWidgetPair("Job: ", jobCombo, panel);
layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
        150, 15, // initialX, initialY
        5, 15);// xPad, yPad
        return panel;
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
        String[] columnNames = { "Received Date"};
        return new SortCriteria(columnNames, new boolean[] {false}, new boolean[] {false});
    }

    private JPanel controlPanel() {
        JPanel container = new JPanel();
        Insets insets = new Insets(1, 2, 1, 2);
        
//        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
//        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    removeSelectedHistory();
                } catch (EmfException e1) {
                    messagePanel.setError("Can't remove messages");
                }
            }
        });
        remove.setMargin(insets);
        container.add(remove);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);
        return panel;
    }
    
    protected void removeSelectedHistory() throws EmfException {
        messagePanel.clear();
        JobMessage[] selected =selectModel.selected().toArray(new JobMessage[0]);

        if (selected.length==0) {
            messagePanel.setMessage("Please select one or more outputs to remove.");
            return;
        }
        String title = "Warning";
        String message = "Are you sure you want to remove selected history message(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(selected);
            doRefresh();
            presenter.doRemove(selected);
        }
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    public void doRefresh(JobMessage[] msgs) throws EmfException {
        try {
            kickPopulateThread(msgs);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }

    }

    public void doRefresh() {
        try {
            if (tableData != null) // it's still null if you've never displayed this tab
                doRefresh(tableData.sources());
        } catch (Exception e) {
            messagePanel.setError("Cannot refresh current tab. " + e.getMessage());
        }
    }

}
