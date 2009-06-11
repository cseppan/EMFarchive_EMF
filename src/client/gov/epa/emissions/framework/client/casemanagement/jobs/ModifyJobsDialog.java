package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class ModifyJobsDialog extends Dialog implements ManageChangeables {

    private EditJobsTabPresenterImpl presenter;
    private MessagePanel messagePanel;
    private JCheckBox hostChkBx = new JCheckBox();
    private JCheckBox sectorChkBx = new JCheckBox();
    private JCheckBox execChkBx = new JCheckBox();
    private JCheckBox argChkBx = new JCheckBox();
    private JCheckBox qChkBx = new JCheckBox();
    private ComboBox hosts;
    private ComboBox sectors;
    private JTextField exec;
    private JTextField args;
    private JTextField qOpt;
    private EmfSession session;
    private EmfConsole parent;
    private CaseJob[] jobs;
    
    public ModifyJobsDialog(EmfConsole parent, CaseJob[] jobs, EmfSession session) {
        super("Modify " + jobs.length + " jobs", parent);
        super.setSize(new Dimension(620, 300));
        super.center();
        
        this.jobs = jobs;
        this.session = session;
        this.parent = parent;
    }

    public void display() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        
        try {
            panel.add(inputPanel());
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        
        panel.add(buttonsPanel());

        super.getContentPane().add(panel);
        super.display();
    }

    private JPanel inputPanel() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel left = new JPanel(new SpringLayout());
        SpringLayoutGenerator layout = new SpringLayoutGenerator();
        layout.addLabelWidgetPair("Modify hostname?", hostChkBx, left);
        layout.addLabelWidgetPair("Modify sector?", sectorChkBx, left);
        layout.addLabelWidgetPair("Modify executable?", execChkBx, left);
        layout.addLabelWidgetPair("Modify arguments?", argChkBx, left);
        layout.addLabelWidgetPair("Modify queue options?", qChkBx, left);
        layout.makeCompactGrid(left, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad
        //left.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        
        JPanel right = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        hosts = new ComboBox("Select a host", presenter.getJobHosts());
        layoutGenerator.addLabelWidgetPair("New Hostname:", hosts, right);
        
        sectors = new ComboBox("Select a sector", presenter.getJobSectors());
        layoutGenerator.addLabelWidgetPair("New Sector:", sectors, right);
        
        exec = new JTextField(20);
        layoutGenerator.addLabelWidgetPair("New Executable:", getFolderChooserPanel(exec,
        "Select the executable file for jobs"), right);
        
        args = new JTextField(20);
        layoutGenerator.addLabelWidgetPair("New Arguments:", args, right);
        
        qOpt = new JTextField(20);
        layoutGenerator.addLabelWidgetPair("New Queue Options:", qOpt, right);
        
        layoutGenerator.makeCompactGrid(right, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad
        //right.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        
        hostChkBx.addActionListener(resetValidation());
        sectorChkBx.addActionListener(resetValidation());
        execChkBx.addActionListener(resetValidation());
        argChkBx.addActionListener(resetValidation());
        qChkBx.addActionListener(resetValidation());
        hosts.addActionListener(resetValidation());
        sectors.addActionListener(resetValidation());
        exec.addActionListener(resetValidation());
        args.addActionListener(resetValidation());
        qOpt.addActionListener(resetValidation());
        
        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        
        panel.setBorder(BorderFactory.createTitledBorder("Properties"));
        
        return panel;
    }
    
    private JPanel getFolderChooserPanel(final JTextField dir, final String title) {
        Button browseButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                cleareMsg();
                selectFolder(dir, title);
                validateInputs();
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
        chooser.setDirectoryAndFileMode();
        int option = chooser.showDialog(parent, "Select an executable file");

        EmfFileInfo[] files = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedFiles() : null;
        
        if (files == null || files.length == 0)
            return;

        if (files[0].isFile()) 
            dir.setText(files[0].getAbsolutePath());
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new OKButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    cleareMsg();
                    modifyJobs();
                    close();
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        panel.add(cancel);
        
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        return panel;
    }

    protected void modifyJobs() throws EmfException {
        if (!hostChkBx.isSelected() 
                && !sectorChkBx.isSelected() 
                && !execChkBx.isSelected() 
                && !argChkBx.isSelected() 
                && !qChkBx.isSelected())
            throw new EmfException("Please check a checkbox corresponding to one or more fields you wish to modify.");
        
        if (!validateInputs())
            throw new EmfException(messagePanel.getMessage());
        
        for (CaseJob job : jobs) {
            if (hostChkBx.isSelected())
                job.setHost((Host)hosts.getSelectedItem());
            
            if (sectorChkBx.isSelected())
                job.setSector((Sector)sectors.getSelectedItem());
            
            if (execChkBx.isSelected()) {
                String filePath = exec.getText().trim();
                File file = new File(filePath);
                
                job.setPath(file.getParent());
                job.setExecutable(getExecutable(new Executable(file.getName())));
            }
            
            if (argChkBx.isSelected())
                job.setArgs(args.getText());
            
            if (qChkBx.isSelected())
                job.setQueOptions(qOpt.getText());
        }
        
        presenter.doSave(jobs);
    }

    private boolean validateInputs() {
        boolean passed = true;
        
        if (hostChkBx.isSelected() && hosts.getSelectedItem() == null) {
            setMsg("Please select a valid host.");
            passed = false;
        }
        
        if (sectorChkBx.isSelected() && sectors.getSelectedItem() == null) {
            setMsg("Please select a valid sector.");
            passed = false;
        }
        
        if (execChkBx.isSelected() && (exec.getText() == null || exec.getText().trim().isEmpty())) {
            setMsg("Please select a valid executable file.");
            passed = false;
        }
        
        if (execChkBx.isSelected()) {
            String filePath = exec.getText().trim();
            File file = new File(filePath);
            
            if (file.getParent() == null || file.getParent().isEmpty()) {
                setMsg("Please specify a full path for the executable file.");
                passed = false;
            }
        }
        
        return passed;
    }
    
    private Action resetValidation() {
        return new AbstractAction() {

            public void actionPerformed(ActionEvent arg0) {
                cleareMsg();
                validateInputs();
            }
            
        };
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

    public void register(Object presenter) {
        this.presenter = (EditJobsTabPresenterImpl) presenter;
    }
    
    public void addChangeable(Changeable changeable) {
        // NOTE Auto-generated method stub
    }
    
    private void setMsg(String msg) {
        this.messagePanel.setMessage(msg);
    }

    private void setError(String msg) {
        this.messagePanel.setError(msg);
    }

    private void cleareMsg() {
        this.messagePanel.clear();
    }

    public void resetChanges() {
        // NOTE Auto-generated method stub
        
    }

}
