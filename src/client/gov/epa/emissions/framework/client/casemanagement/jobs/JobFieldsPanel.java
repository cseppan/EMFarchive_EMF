package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
    
    public JobFieldsPanel(boolean edit, MessagePanel messagePanel, ManageChangeables changeablesList, EmfConsole parent, EmfSession session) {
        this.edit = edit;
        this.changeablesList = changeablesList;
        this.parent = parent;
        this.session = session;
        this.messagePanel = messagePanel;
    }

    public void display(CaseJob job, JComponent container) {
        this.job = job;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        try {
            container.add(nameNPurposPanel());
            container.add(pathNbrowserPanel());
            container.add(setupPanel());
        } catch (EmfException e) {
            setError("Could not retrieve all job related info.");
        }
        
        if (edit)
            container.add(resultPanel());
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
        layoutGenerator.addLabelWidgetPair("Purpose:", new ScrollableComponent(purpose), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    private JPanel setupPanel() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(leftSetupPanel());
        panel.add(rightSetupPanel());
        
        return panel;
    }
    
    private JPanel pathNbrowserPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        path = new TextField("path", job.getPath(), 31);
        path.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(path);
        layoutGenerator.addLabelWidgetPair("Executable:", getFolderChooserPanel(path, "Select the Executable File"), panel);
        
//      Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        
        return panel;
    }

    private JPanel leftSetupPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        args = new TextField("args", job.getArgs(), 12);
        changeablesList.addChangeable(args);
        layoutGenerator.addLabelWidgetPair("Arguments:", args, panel);
        
        jobNo = new TextField("jobNo", job.getJobNo()+"", 12);
        jobNo.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(jobNo);
        layoutGenerator.addLabelWidgetPair("Job Number:", jobNo, panel);
        
        Host[] hosts = presenter.getHostsObject().getAll();
        host = new EditableComboBox(hosts);
        host.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(host);
        layoutGenerator.addLabelWidgetPair("Host:", host, panel);
        
        qoption = new TextField("qoption", job.getQueOptions(), 12);
        qoption.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(qoption);
        layoutGenerator.addLabelWidgetPair("Queue Options:", qoption, panel);
        
//      Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        
        return panel;
    }

    private Component rightSetupPanel() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        version = new TextField("version", job.getVersion()+"", 12);
        version.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(version);
        layoutGenerator.addLabelWidgetPair("Version:", version, panel);
        
        Sector[] sectors = presenter.getSectors();
        sector = new ComboBox(sectors);
        sector.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(sector);
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);

        JobRunStatus[] statuses = presenter.getRunStatuses();
        status = new ComboBox(statuses);
        status.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(status);
        layoutGenerator.addLabelWidgetPair("Run Status:", status, panel);
        
        String user = job.getUser() == null ? session.user().getName() : job.getUser().getName();
        layoutGenerator.addLabelWidgetPair("User:", new JLabel(user), panel);
        
        
//      Lay out the panel.
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
        JPanel folderPanel = new JPanel(new BorderLayout(2,0));
        folderPanel.add(dir, BorderLayout.LINE_START);
        folderPanel.add(browseButton, BorderLayout.LINE_END);

        return folderPanel;
    }

    protected void selectFolder(JTextField dir, String title) {
        EmfFileInfo initDir = new EmfFileInfo(dir.getText(), true, false);

        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle(title);
        chooser.setDirectoryAndFileMode();
        int option = chooser.showDialog(parent, "Select a file");

        EmfFileInfo[] files = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedFiles() : null;
        if (files == null || files.length == 0)
            return;

        if (files.length > 1) {
            setError("Only one file is selected as executable.");
        }
            
        dir.setText(files[0].getAbsolutePath());
    }

    private JPanel resultPanel() {
        // NOTE Auto-generated method stub
        return new JPanel();
    }


    public CaseJob setFields() throws EmfException {
        job.setName(name.getText().trim());
        job.setPurpose(purpose.getText().trim());
        job.setJobNo(Float.parseFloat(jobNo.getText().trim()));
        job.setArgs(args.getText().trim());
        setPathNExecutable();
        setHost();
        updateSector();
        job.setRunstatus((JobRunStatus)status.getSelectedItem());
        job.setVersion(Integer.parseInt(version.getText().trim()));
        job.setQueOptions(qoption.getText().trim());
        
        return job;
    }

    private void setPathNExecutable() {
        String absolute = path.getText().trim();
        int index = 0;
        
        if (absolute.charAt(index) == '/')
            index = absolute.lastIndexOf('/');
        else
            index = absolute.lastIndexOf('\\');
        
        job.setPath(absolute.substring(0, index));
        job.setExecutable(new Executable[]{new Executable(absolute.substring(++index))});
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
            throw new EmfException("Please give a name to case job.");
        
        String absolute = path.getText();
        
        if (absolute == null || absolute.trim().equals(""))
            throw new EmfException("Please select an executable file.");
        
        try {
            Float.parseFloat(jobNo.getText().trim());
        } catch (NumberFormatException e) {
            throw new EmfException("Please input a float number to Job # field.");
        }
        
        Object selected =  host.getSelectedItem();
        
        if (selected == null || selected.toString().trim().equals(""))
            throw new EmfException("Please give a valid host name.");
        
        if (absolute == null || absolute.trim().equals(""))
            throw new EmfException("Please select an executable file.");
        
        try {
            Integer.parseInt(version.getText().trim());
        } catch (NumberFormatException e) {
            throw new EmfException("Please input an integer to Version field.");
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
