package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.EmptyStrings;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class JobFieldsPanel extends JPanel implements JobFieldsPanelView {

    private JobFieldsPanelPresenter presenter;

    private ManageChangeables changeablesList;

    private CaseJob job;

    private boolean edit;

    private TextField name;

    private TextArea purpose;

    private TextField jobNo;

    private TextField version;

    private TextField args;

    private TextField path;

    private EmfConsole parent;

    private EmfSession session;

    private EditableComboBox host;

    private TextField qoption;

    private MessagePanel messagePanel;

    private ComboBox status;

    private ComboBox sector;

    private String comboWidth = EmptyStrings.create(35);

    private JLabel queID;

    private JLabel start;

    private JLabel complete;

    private TextArea runNote;

    private TextArea runLog;

    private JLabel userLabel;

    private Case caseObj;

    private TextField jobOrder;

    private static String lastPath = "";

    public JobFieldsPanel(boolean edit, MessagePanel messagePanel, ManageChangeables changeablesList,
            EmfConsole parent, EmfSession session) {
        this.edit = edit;
        this.changeablesList = changeablesList;
        this.parent = parent;
        this.session = session;
        this.messagePanel = messagePanel;
    }

    public void display(Case caseObj, CaseJob job, JComponent container) {
        this.job = job;
        this.caseObj = caseObj;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        try {
            container.add(nameNPurposPanel());
            container.add(setupPanel());
        } catch (EmfException e) {
            setError("Could not retrieve all job related fields.");
        }

        if (edit) {
            container.add(resultPanel());
            populateFields();
        }
    }

    private JPanel nameNPurposPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // name
        name = new TextField("name", 40);
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        // description
        purpose = new TextArea("purposes", job.getPurpose());
        changeablesList.addChangeable(purpose);
        ScrollableComponent scrolpane = new ScrollableComponent(purpose);
        scrolpane.setPreferredSize(new Dimension(444, 80));
        layoutGenerator.addLabelWidgetPair("Purpose:", scrolpane, panel);

        String execPath = job.getPath();
        String caseInputPath = this.caseObj.getInputFileDir();
        if (execPath == null || execPath.trim().isEmpty())
            execPath = caseInputPath + getFileSeparator(caseInputPath);

        path = new TextField("path", execPath, 32);
        path.setPreferredSize(new Dimension(300, 15));
        changeablesList.addChangeable(path);
        layoutGenerator.addLabelWidgetPair("Executable:", getFolderChooserPanel(path, "Select the Executable File"),
                panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel setupPanel() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(leftSetupPanel(), BorderLayout.LINE_START);
        panel.add(rightSetupPanel(), BorderLayout.LINE_END);

        if (edit)
            // panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "Job Setup",
            // 1, 1, Font.getFont(Font.SANS_SERIF), Color.blue));
            panel.setBorder(BorderFactory.createTitledBorder("Setup"));

        return panel;
    }

    private JPanel leftSetupPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        args = new TextField("args", job.getArgs(), 12);
        changeablesList.addChangeable(args);
        layoutGenerator.addLabelWidgetPair("Arguments:", args, panel);

        jobNo = new TextField("jobNo", job.getJobNo() + "", 12);
        jobNo.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(jobNo);
        layoutGenerator.addLabelWidgetPair("Job Number:", jobNo, panel);
        jobNo.setToolTipText("A number that makes this job unique for the " +
                "given case (used to specify dependencies between jobs)");

        jobOrder = new TextField("jobOrder", job.getOrder() + "", 12);
        jobOrder.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(jobOrder);
        layoutGenerator.addLabelWidgetPair("Job Order:", jobOrder, panel);
        
        Host[] hosts = presenter.getHostsObject().getAll();
        host = new EditableComboBox(hosts);
        host.setPrototypeDisplayValue(comboWidth);
        changeablesList.addChangeable(host);
        layoutGenerator.addLabelWidgetPair("Host:", host, panel);

        qoption = new TextField("qoption", job.getQueOptions(), 12);
        qoption.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(qoption);
        layoutGenerator.addLabelWidgetPair("Queue Options:", qoption, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private Component rightSetupPanel() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        version = new TextField("version", job.getVersion() + "", 12);
        version.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(version);
        layoutGenerator.addLabelWidgetPair("Version:", version, panel);

        Sector[] sectors = presenter.getSectors();
        sector = new ComboBox(sectors);
        sector.setPrototypeDisplayValue(comboWidth);
        changeablesList.addChangeable(sector);
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);

        JobRunStatus[] statuses = presenter.getRunStatuses();
        status = new ComboBox(statuses);
        status.setPrototypeDisplayValue(comboWidth);
        changeablesList.addChangeable(status);
        layoutGenerator.addLabelWidgetPair("Run Status:", status, panel);

        String user = job.getUser() == null ? session.user().getName() : job.getUser().getName();
        userLabel = new JLabel(user);
        layoutGenerator.addLabelWidgetPair("User:", userLabel, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel getFolderChooserPanel(final JTextField dir, final String title) {
        Button browseButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMessage();
                selectFolder(dir, title);
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(dir, BorderLayout.LINE_START);
        folderPanel.add(browseButton, BorderLayout.LINE_END);

        return folderPanel;
    }

    protected void selectFolder(JTextField dir, String title) {
        EmfFileInfo initDir = new EmfFileInfo(dir.getText(), true, false);
        if ((initDir.getAbsolutePath() == null) || (initDir.getAbsolutePath().length() == 0)) {
            initDir = new EmfFileInfo(lastPath, true, false);
        }

        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle(title);
        chooser.setDirectoryAndFileMode();
        int option = chooser.showDialog(parent, "Select a file that contains the executable");

        EmfFileInfo[] files = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedFiles() : null;
        if (files == null || files.length == 0)
            return;

        if (files.length > 1) {
            setError("Please select a single file for the executable.");
        }

        dir.setText(files[0].getAbsolutePath());
    }

    private JPanel resultPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel leftpanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator leftlayout = new SpringLayoutGenerator();

        queID = new JLabel();
        leftlayout.addLabelWidgetPair("Queue ID:", queID, leftpanel);

        start = new JLabel();
        leftlayout.addLabelWidgetPair("Start Date:", start, leftpanel);

        complete = new JLabel();
        leftlayout.addLabelWidgetPair("Complete Date:", complete, leftpanel);

        // Lay out the panel.
        leftlayout.makeCompactGrid(leftpanel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        JPanel rightpanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator rightlayout = new SpringLayoutGenerator();

        runNote = new TextArea("runnote", job.getRunNotes());
        changeablesList.addChangeable(runNote);
        ScrollableComponent scrolpane1 = new ScrollableComponent(runNote);
        scrolpane1.setPreferredSize(new Dimension(224, 80));
        rightlayout.addLabelWidgetPair("Run Notes:", scrolpane1, rightpanel);

        runLog = new TextArea("runLog", job.getRunNotes());
        changeablesList.addChangeable(runLog);
        ScrollableComponent scrolpane2 = new ScrollableComponent(runLog);
        scrolpane2.setPreferredSize(new Dimension(224, 80));
        rightlayout.addLabelWidgetPair("Run Log:", scrolpane2, rightpanel);

        // Lay out the panel.
        rightlayout.makeCompactGrid(rightpanel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        panel.add(leftpanel, BorderLayout.LINE_START);
        panel.add(rightpanel, BorderLayout.LINE_END);
        // panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "Job Run
        // Results",
        // 1, 1, Font.getFont(Font.SANS_SERIF), Color.blue));
        panel.setBorder(BorderFactory.createTitledBorder("Run Results"));

        return panel;
    }

    private void populateFields() {
        name.setText(job.getName());
        purpose.setText(job.getPurpose());
        String jobPath = job.getPath();
        Executable exec = job.getExecutable();

        if (jobPath != null && !path.isEmpty())
            path.setText(jobPath + getFileSeparator(jobPath) + ((exec == null) ? "" : exec.getName()));
        
        args.setText(job.getArgs());
        jobNo.setText(job.getJobNo() + "");
        jobOrder.setText(job.getOrder() + "");
        host.setSelectedItem(job.getHost());
        this.qoption.setText(job.getQueOptions());
        this.version.setText(job.getVersion() + "");
        this.sector.setSelectedItem(job.getSector() == null ? new Sector("All sectors", "All sectors") : job
                .getSector());
        this.status.setSelectedItem(job.getRunstatus());

        User user = job.getUser();
        Date startDate = job.getRunStartDate();
        Date completeDate = job.getRunCompletionDate();

        this.userLabel.setText(user == null ? "" : user.getName());
        this.queID.setText(job.getIdInQueue() + "");
        this.start.setText(startDate == null ? "" : EmfDateFormat.format_MM_DD_YYYY_HH_mm(startDate));
        this.complete.setText(completeDate == null ? "" : EmfDateFormat.format_MM_DD_YYYY_HH_mm(completeDate));
        this.runNote.setText(job.getRunNotes());
        this.runLog.setText(job.getRunLog());
    }

    public CaseJob setFields() throws EmfException {
        job.setName(name.getText().trim());
        job.setPurpose(purpose.getText().trim());
        job.setJobNo(Float.parseFloat(jobNo.getText().trim()));
        job.setOrder(Integer.parseInt(jobOrder.getText().trim()));
        job.setArgs(args.getText().trim());
        setPathNExecutable();
        setHost();
        updateSector();
        job.setRunstatus((JobRunStatus) status.getSelectedItem());
        job.setVersion(Integer.parseInt(version.getText().trim()));
        job.setQueOptions(qoption.getText().trim());
        
        if (edit) {
            job.setRunLog(runLog.getText());
            job.setRunNotes(runNote.getText());
            job.setUser(session.user());
        }

        if (presenter.checkDuplication(job))
            showRemind();

        return job;
    }

    private void setPathNExecutable() {
        String absolute = (path.getText() == null) ? null : path.getText().trim();

        if (absolute == null || absolute.isEmpty())
            return;

        char separator = getFileSeparator(absolute);
        int index = absolute.lastIndexOf(separator);
        if (index >= 0)
            job.setPath(absolute.substring(0, index));

        lastPath = job.getPath();

        if (++index < absolute.length()) {
            Executable exe = new Executable(absolute.substring(index));
            job.setExecutable(getExecutable(exe));
        }
    }

    private char getFileSeparator(String path) {
        if (path == null)
        {
            // this assumes that the server and client are running on the same platform
            return File.pathSeparatorChar;
        }
        if (path.charAt(0) == '/')
            return '/';

        return '\\';
    }

    private Executable getExecutable(Executable exe) {
        CaseService service = session.caseService();
        try {
            return service.addExecutable(exe);
        } catch (EmfException e) {
            setError("Could not add the new executable " + exe.getName());
            return null;
        }
    }

    private void updateSector() {
        Sector selected = (Sector) sector.getSelectedItem();

        if (selected.getName().equalsIgnoreCase("All sectors")) {
            job.setSector(null);
            return;
        }

        job.setSector(selected);
    }

    private void setHost() throws EmfException {
        job.setHost(presenter.getHost(host.getSelectedItem()));
    }

    public void observe(JobFieldsPanelPresenter presenter) {
        this.presenter = presenter;
    }

    public void validateFields() throws EmfException {
        String temp = name.getText().trim();

        if (temp.trim().length() == 0)
            throw new EmfException("Please enter a name for the job.");

        String absolute = path.getText();
        // File execFile = new File(absolute);

        if (absolute == null || absolute.trim().equals(""))
            throw new EmfException("Please select an executable file.");

        if (!absolute.contains("\\") && !absolute.contains("/"))
            throw new EmfException("Please specify an absolute path for executable file.");

        try {
            Float.parseFloat(jobNo.getText().trim());
        } catch (NumberFormatException e) {
            throw new EmfException("Please enter a floating point number into the Job Number field.");
        }

        Object selected = host.getSelectedItem();

        if (selected == null || selected.toString().trim().equals(""))
            throw new EmfException("Please enter a valid host name.");

        // if (absolute == null || absolute.trim().equals("") || !execFile.isFile())
        // throw new EmfException("Please select an executable file.");

        try {
            Integer.parseInt(version.getText().trim());
        } catch (NumberFormatException e) {
            throw new EmfException("Please enter an integer that is the version of the executable.");
        }
    }

    private void showRemind() throws EmfException {
        String title = "Warning";
        String message = "A similar job already existed for this case. Are you sure want to add another one?";
        int selection = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection != JOptionPane.YES_OPTION) {
            throw new EmfException("Please cancel or modify your job settings.");
        }
    }

    public CaseJob getJob() throws EmfException {
        presenter.doValidateFields();
        return this.job;
    }

    private void clearMessage() {
        messagePanel.clear();
    }

    private void setError(String error) {
        messagePanel.setError(error);
    }

}
