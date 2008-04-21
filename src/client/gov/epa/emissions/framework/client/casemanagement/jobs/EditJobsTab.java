package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.RunButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class EditJobsTab extends JPanel implements EditJobsTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private EditJobsTabPresenter presenter;

    private Case caseObj;

    private CaseJobsTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel mainPanel;

    private MessagePanel messagePanel;

    private ManageChangeables changeables;

    private TextField outputDir;

    private EmfSession session;

    private DesktopManager desktopManager;

    public EditJobsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager, EmfSession session) {
        super.setName("editJobsTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        this.session = session;
        this.changeables = changeables;

        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, Case caseObj, EditJobsTabPresenter presenter) {
        super.removeAll();
        this.outputDir = new TextField("outputdir", 50);
        outputDir.setText(caseObj.getOutputFileDir());
        this.changeables.addChangeable(outputDir);
        this.caseObj = caseObj;
        this.presenter = presenter;

        try {
            super.add(createLayout(new CaseJob[0], parentConsole), BorderLayout.CENTER);
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case jobs.");
        }

        kickPopulateThread();
    }

    private void kickPopulateThread() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                retrieveJobs();
            }
        });
        populateThread.start();
    }

    public synchronized void retrieveJobs() {
        try {
            messagePanel.setMessage("Please wait while retrieving all case jobs...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            doRefresh(presenter.getCaseJobs());
            messagePanel.clear();
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case jobs.");
        } finally {
            setCursor(Cursor.getDefaultCursor());

            try {
                presenter.checkIfLockedByCurrentUser();
            } catch (Exception e) {
                messagePanel.setMessage(e.getMessage());
            }
        }
    }

    private void doRefresh(CaseJob[] jobs) throws Exception {
        // super.removeAll();
        String outputFileDir = caseObj.getOutputFileDir();

        if (!outputDir.getText().equalsIgnoreCase(outputFileDir))
            outputDir.setText(outputFileDir);

        setupTableModel(jobs);
        table.refresh(tableData);
        panelRefresh();
    }

    private JPanel createLayout(CaseJob[] jobs, EmfConsole parentConsole) throws Exception {
        final JPanel layout = new JPanel(new BorderLayout());

        layout.add(createFolderPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(jobs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel createFolderPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Output Folder:", getFolderChooserPanel(outputDir,
                "Select the base Output Folder for the Case"), panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private JPanel getFolderChooserPanel(final JTextField dir, final String title) {
        Button browseButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder(dir, title);
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(dir, BorderLayout.LINE_START);
        folderPanel.add(browseButton, BorderLayout.LINE_END);

        return folderPanel;
    }

    private void selectFolder(JTextField dir, String title) {
        EmfFileInfo initDir = new EmfFileInfo(dir.getText(), true, true);

        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle(title);
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            caseObj.setOutputFileDir(file.getAbsolutePath());
            dir.setText(file.getAbsolutePath());
        }
    }

    private JPanel tablePanel(CaseJob[] jobs, EmfConsole parentConsole) {
        setupTableModel(jobs);

        mainPanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        mainPanel.add(table);
        return mainPanel;
    }

    private void setupTableModel(CaseJob[] jobs) {
        tableData = new CaseJobsTableData(jobs);
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Order", "Sector", "Name", "Executable" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true, true }, new boolean[] { false, false,
                false, false });
    }

    private JPanel controlPanel() {
        Insets insets = new Insets(1, 2, 1, 2);

        JPanel container = new JPanel();

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                addNewJob();
            }
        });
        add.setMargin(insets);
        container.add(add);

        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    removeJobs();
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }
        });
        remove.setMargin(insets);
        container.add(remove);

        Button edit = new EditButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    editJobs();
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        edit.setMargin(insets);
        container.add(edit);

        String message = "You have asked to copy too many jobs. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);
        SelectAwareButton copy = new SelectAwareButton("Copy", copyAction(), table, confirmDialog);
        copy.setMargin(insets);
        container.add(copy);

        Button run = new RunButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    runJobs();
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        run.setMargin(insets);
        container.add(run);

        Button validate = new Button("Validate", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    validateJobDatasets();
                } catch (Exception ex) {
                    // ex.printStackTrace();
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        validate.setMargin(insets);
        container.add(validate);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    private Action copyAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    clearMessage();
                    copyJobs();
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
    }

    private void addNewJob() {
        NewJobDialog view = new NewJobDialog(parentConsole, caseObj, session);
        try {
            presenter.addNewJobDialog(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void removeJobs() throws EmfException {
        CaseJob[] jobs = getSelectedJobs().toArray(new CaseJob[0]);

        if (jobs.length == 0) {
            messagePanel.setMessage("Please select job(s) to remove.");
            return;
        }

        String title = "Warning";

        if (presenter.jobsUsed(jobs)) {
            int selection1 = showDialog("Selected job(s) are used by case inputs or parameters.\n "
                    + "Are you sure you want to remove the selected job(s)?", title);

            if (selection1 != JOptionPane.YES_OPTION)
                return;

            removeSelectedJobs(jobs);
            return;
        }

        int selection2 = showDialog("Are you sure you want to remove the selected job(s)?", title);

        if (selection2 == JOptionPane.YES_OPTION) {
            removeSelectedJobs(jobs);
        }
    }

    private void removeSelectedJobs(CaseJob[] jobs) throws EmfException {
        try {
            presenter.removeJobs(jobs);
        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        }
        tableData.remove(jobs);
        refresh();
    }

    private void editJobs() throws EmfException {
        List<CaseJob> jobs = getSelectedJobs();

        if (jobs.size() == 0) {
            messagePanel.setMessage("Please select job(s) to edit.");
            return;
        }

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            String title = job.getName() + "(" + job.getId() + ")(" + caseObj.getName() + ")";
            EditCaseJobView jobEditor = new EditCaseJobWindow(title, desktopManager, parentConsole, session);
            presenter.editJob(job, jobEditor);
        }
    }

    private void copyJobs() throws Exception {
        List<CaseJob> jobs = getSelectedJobs();

        if (jobs.size() == 0) {
            messagePanel.setMessage("Please select job(s) to copy.");
            return;
        }

        Object[] selected = presenter.getAllCaseNameIDs();
        String selectedCase = (String)JOptionPane.showInputDialog(parentConsole, "Copy " + jobs.size() + " job(s) to case: ",
                "Copy Case Jobs", JOptionPane.PLAIN_MESSAGE, getCopyIcon(), selected, selected[getDefultIndex(selected)]);

        if ((selectedCase != null) && (selectedCase.length() > 0)) {
            presenter.copyJobs(getCaseId(selectedCase), jobs);
        }
    }

    private int getDefultIndex(Object[] selected) {
        int currentCaseId = this.caseObj.getId();
        int length = selected.length;
        
        for (int i = 0; i < length; i++)
            if (selected[i].toString().contains("(" + currentCaseId + ")"))
                return i;
        
        return 0;
    }

    private int getCaseId(String selectedCase) {
        int index1 = selectedCase.indexOf("(") + 1;
        int index2 = selectedCase.indexOf(")");
         
        return Integer.parseInt(selectedCase.substring(index1, index2));
    }

    private Icon getCopyIcon() {
        URL imgURL = getClass().getResource("/toolbarButtonGraphics/general/Copy24.gif");
        
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }
        
        return null;
    }
    
    private void validateJobDatasets() throws EmfException {
        CaseJob[] jobs = getSelectedJobs().toArray(new CaseJob[0]);

        if (jobs.length == 0) {
            messagePanel.setMessage("Please select one or more jobs to run.");
            return;
        }

        String validationMsg = presenter.validateJobs(jobs);
        int width = 50;
        int height = validationMsg.length() / 50;
        String title = "Possible Issues with Datasets Selected for Job Inputs";

        showMessageDialog(createMsgScrollPane(validationMsg, width, height), title);
    }

    private void runJobs() throws Exception {
        CaseJob[] jobs = getSelectedJobs().toArray(new CaseJob[0]);

        if (jobs.length == 0) {
            messagePanel.setMessage("Please select one or more jobs to run.");
            return;
        }

        try {
            String msg = presenter.getJobsStatus(jobs);
            int option = JOptionPane.NO_OPTION;
            String lineSeparator = System.getProperty("line.separator");

            if (msg.equalsIgnoreCase("OK")) {
                option = validateJobs(jobs, lineSeparator);

                if (option == JOptionPane.YES_OPTION)
                    proceedRunningJobs(jobs);

                return;
            }

            if (msg.equalsIgnoreCase("CANCEL")) {
                setMessage("One or more of the selected jobs is already running.");
                return;
            }

            if (msg.equalsIgnoreCase("WARNING"))
                option = showDialog("Are you sure you want to rerun the selected job" + (jobs.length > 1 ? "s" : "")
                        + "?", "Warning");

            if (option == JOptionPane.YES_OPTION) {
                option = validateJobs(jobs, lineSeparator);
            }

            if (option == JOptionPane.YES_OPTION)
                proceedRunningJobs(jobs);
        } catch (Exception e) {
            throw e;
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private int validateJobs(CaseJob[] jobs, String ls) throws EmfException {
        String validationMsg = presenter.validateJobs(jobs);

        if (validationMsg.isEmpty())
            // there are no nonfinal dataset versions used, so return yes
            return JOptionPane.YES_OPTION;

        String finalMsg = validationMsg + ls + "ARE YOU SURE YOU WANT TO RUN THE SELECTED JOB"
                + (jobs.length > 1 ? "S" : "") + "?";
        int width = 50;
        int height = validationMsg.length() / 50;

        if (height > 30)
            height = 30;

        ScrollableComponent msgArea = createMsgScrollPane(finalMsg, width, height);

        return showDialog(msgArea, "Confirm Running Jobs");
    }

    private void proceedRunningJobs(CaseJob[] jobs) throws Exception {
        setMessage("Please wait while submitting all case jobs...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        presenter.runJobs(jobs);
        doRefresh(presenter.getCaseJobs());
        setMessage("Finished submitting jobs to run.");
    }

    private List<CaseJob> getSelectedJobs() {
        return (List<CaseJob>) table.selected();
    }

    private int showDialog(Object msg, String title) {
        return JOptionPane.showConfirmDialog(parentConsole, msg, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
    }

    private void showMessageDialog(Object msg, String title) {
        JOptionPane.showMessageDialog(parentConsole, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    private ScrollableComponent createMsgScrollPane(String msg, int width, int height) {
        TextArea message = new TextArea("msgArea", msg, width, height);
        message.setEditable(false);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(message);
        // descScrollableTextArea.setMinimumSize(new Dimension(width * 3, height * 2));
        return descScrollableTextArea;
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

    private void setMessage(String msg) {
        messagePanel.setMessage(msg);
    }

    public void addJob(CaseJob job) {
        tableData.add(job);
        table.refresh(tableData);
        panelRefresh();
    }

    private void panelRefresh() {
        mainPanel.removeAll();
        mainPanel.add(table);
        super.validate();
    }

    public CaseJob[] caseJobs() {
        return tableData.sources();
    }

    public String getCaseOutputFileDir() {
        if (outputDir == null)
            return null;
        return outputDir.getText();
    }

    public void doRefresh() throws EmfException {
        try {
            kickPopulateThread();
        } catch (RuntimeException e) {
            throw new EmfException(e.getMessage());
        }
    }

}
