package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.RunButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditJobsTab extends JPanel implements EditJobsTabView, Runnable {

    private EmfConsole parentConsole;

    private EditJobsTabPresenter presenter;

    private Case caseObj;

    private CaseJobsTableData tableData;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;

    private MessagePanel messagePanel;
    
    private EmfSession session;

    private DesktopManager desktopManager;

    private volatile Thread populateThread;

    public EditJobsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager, EmfSession session) {
        super.setName("editJobsTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        this.session = session;
        this.populateThread = new Thread(this);

        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, Case caseObj, EditJobsTabPresenter presenter) {
        super.removeAll();

        this.caseObj = caseObj;
        this.presenter = presenter;

        try {
            super.add(createLayout(new CaseJob[0], presenter, parentConsole), BorderLayout.CENTER);
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case jobs.");
        }

        populateThread.start();
    }
    
    public void run() {
        try {
            messagePanel.setMessage("Please wait while retrieving all case jobs...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            doRefresh(presenter.getCaseJobs());
            messagePanel.clear();
            setCursor(Cursor.getDefaultCursor());
        } catch (Exception e) {
            e.printStackTrace();
            messagePanel.setError("Cannot retrieve all case jobs.");
        }
    }

    private void doRefresh(CaseJob[] jobs) throws Exception {
        super.removeAll();
        super.add(createLayout(jobs, presenter, parentConsole), BorderLayout.CENTER);
    }

    private JPanel createLayout(CaseJob[] jobs, EditJobsTabPresenter presenter, EmfConsole parentConsole)
            throws Exception {
        final JPanel layout = new JPanel(new BorderLayout());

        layout.add(tablePanel(jobs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(CaseJob[] jobs, EmfConsole parentConsole) {
        tableData = new CaseJobsTableData(jobs);
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
        String[] columnNames = { "Name", "Sector", "Executable" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true }, new boolean[] { false, false, false });
    }

    private JPanel controlPanel(final EditJobsTabPresenter presenter) {
        Insets insets = new Insets(1, 2, 1, 2);

        JPanel container = new JPanel();

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                addNewJob(presenter);
            }
        });
        add.setMargin(insets);
        container.add(add);

        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    removeJobs(presenter);
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }
        });
        remove.setMargin(insets);
        container.add(remove);

        Button edit = new EditButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    editJobs(presenter);
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        edit.setMargin(insets);
        edit.setEnabled(false);
        container.add(edit);

        Button run = new RunButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    runJobs(presenter);
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        run.setEnabled(false);
        run.setMargin(insets);
        container.add(run);

        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    private void addNewJob(EditJobsTabPresenter presenter) {
        NewJobDialog view = new NewJobDialog(parentConsole, caseObj, session);
        try {
            presenter.addNewJobDialog(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void removeJobs(EditJobsTabPresenter presenter) throws EmfException {
        CaseJob[] jobs = (CaseJob[]) getSelectedJobs().toArray(new CaseJob[0]);

        if (jobs.length == 0) {
            messagePanel.setMessage("Please select job(s) to remove.");
            return;
        }

        String title = "Warning";
        String message = "Are you sure you want to remove the selected job(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(jobs);
            refresh();
            presenter.removeJobs(jobs);
        }
    }

    private void editJobs(EditJobsTabPresenter presenter) throws EmfException {
        List jobs = getSelectedJobs();

        if (jobs.size() == 0) {
            messagePanel.setMessage("Please select job(s) to edit.");
            return;
        }

        for (Iterator iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = (CaseJob) iter.next();
            String title = job.getName() + "(" + job.getId() + ")(" + caseObj.getName() + ")";
            EditCaseJobView jobEditor = new EditCaseJobWindow(title, desktopManager);
            presenter.doEditJob(job, jobEditor);
        }
    }

    private void runJobs(EditJobsTabPresenter presenter) throws EmfException {
        CaseJob[] jobs = (CaseJob[]) getSelectedJobs().toArray(new CaseJob[0]);

        if (jobs.length == 0) {
            messagePanel.setMessage("Please select job(s) to run.");
            return;
        }
        
        throw new EmfException("Under construction...");
    }
    
    private List getSelectedJobs() {
        return selectModel.selected();
    }

    public void refresh() {
        // note that this will get called when the case is save
        try {
            if (tableData != null) // it's still null if you've never displayed this tab
                doRefresh(tableData.sources());
        } catch (Exception e) {
            messagePanel.setError("Cannot refresh current tab. " + e.getMessage());
        }
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }
    

    public void addJob(CaseJob job) {
        tableData.add(job);
        selectModel.refresh();

        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }
    

    public CaseJob[] caseJobs() {
        return tableData.sources();
    }


}
