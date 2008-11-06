package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class LoadCaseDialog extends JDialog {

    private SingleLineMessagePanel messagePanel;

    private JTextField path;
    
    private JComboBox jobs;

    private LoadCasePresenter presenter;

    private JButton loadButton;

    private EmfConsole parentConsole;

    private DataCommonsService service;

    public LoadCaseDialog(String title, Component container, EmfConsole parentConsole, EmfSession session) {
        super(parentConsole);
        super.setTitle(title);
        super.setLocation(ScreenUtils.getCascadedLocation(container, container.getLocation(), 300, 300));
        super.setModal(true);

        this.parentConsole = parentConsole;
        this.service = session.dataCommonsService();
    }
    
    public void display() {
        this.getContentPane().add(createLayout());
        this.pack();
        this.setVisible(true);
    }

    public void observe(LoadCasePresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(createFolderPanel());
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createFolderPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // folder
        path = new JTextField(30);
        path.setName("path");
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMessagePanel();
                selectFile();
            }
        });
        Icon icon = new ImageResources().open("Open a file");
        button.setIcon(icon);
        
        try {
            jobs = new JComboBox(presenter.getJobs());
            jobs.setPreferredSize(new Dimension(334, 30));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(path, BorderLayout.LINE_START);
        folderPanel.add(button, BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("File:", folderPanel, panel);
        layoutGenerator.addLabelWidgetPair("Case Job:", jobs, panel);


        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        loadButton = new Button("Load", loadCase());
        container.add(loadButton);
        getRootPane().setDefaultButton(loadButton);

        JButton cancelButton = new CancelButton(cancelAction());
        container.add(cancelButton);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private Action loadCase() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e){
                clearMessagePanel();
                
                try {
                    checkFolderField();
                    presenter.loadCase(path.getText(), (CaseJob)jobs.getSelectedItem());
                    dispose();
                } catch (Exception e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        };
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e){ 
                dispose();
            }
        };
    }
    

    private void clearMessagePanel() {
        messagePanel.clear();
    }

    public void setMostRecentUsedFolder(String mostRecentUsedFolder) {
        if (mostRecentUsedFolder != null && path != null)
            path.setText(mostRecentUsedFolder);
    }

    private void selectFile() {
        EmfFileInfo initDir = new EmfFileInfo(path.getText(), true, false);

        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(service));
        chooser.setTitle("Select a case log file for loading data into case");
        chooser.setDirectoryAndFileMode();
        int option = chooser.showDialog(parentConsole, "Select a file");

        EmfFileInfo[] files = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedFiles() : null;
        
        if (files == null)
            return;

        if (files.length > 1) {
            path.setText("");
            messagePanel.setError("Please select a single log file.");
            return;
        }
        
        if (files[0].isFile()) {
            path.setText(files[0].getAbsolutePath());
        }
    }
    
    private void checkFolderField() throws EmfException {
        String specified = path.getText();
        
        if (specified == null || specified.trim().isEmpty() || specified.trim().length() == 1)
            throw new EmfException("Please specify a valid file.");
        
        if (specified.charAt(0) != '/' && specified.charAt(1) != ':')
            throw new EmfException("Specified folder is not in a right format (ex. C:\\, /home, etc.).");
        
        if (specified.charAt(0) != '/' && !Character.isLetter(specified.charAt(0)))
            throw new EmfException("Specified folder is not in a right format (ex. C:\\).");
        
        if (!new File(specified).isFile())
            throw new EmfException("Specified file is invalid.");
    }

}
