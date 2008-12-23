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
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class PrintCaseDialog extends JDialog {

    private SingleLineMessagePanel messagePanel;

    private JTextField folder;

    private PrintCasePresenter presenter;

    private JButton okButton;

    private EmfConsole parentConsole;

    private DataCommonsService service;

    public PrintCaseDialog(String title, Component container, EmfConsole parentConsole, EmfSession session) {
        super(parentConsole);
        super.setTitle(title);
        super.setLocation(ScreenUtils.getCascadedLocation(container, container.getLocation(), 300, 300));
        super.setModal(true);

        this.parentConsole = parentConsole;
        this.service = session.dataCommonsService();
        this.getContentPane().add(createLayout());
    }
    
    public void display() {
        this.pack();
        this.setVisible(true);
    }

    public void observe(PrintCasePresenter presenter) {
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
        folder = new JTextField(40);
        folder.setName("folder");
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMessagePanel();
                selectFolder();
            }
        });
        Icon icon = new ImageResources().open("Open a folder");
        button.setIcon(icon);

        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(folder, BorderLayout.LINE_START);
        folderPanel.add(button, BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Folder", folderPanel, panel);


        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
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

        okButton = new Button("OK", printCase());
        container.add(okButton);
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new CancelButton(cancelAction());
        container.add(cancelButton);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private Action printCase() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e){
                clearMessagePanel();
                
                try {
                    checkFolderField();
                    presenter.printCase(folder.getText());
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
        if (mostRecentUsedFolder != null && folder != null)
            folder.setText(mostRecentUsedFolder);
    }

    private void selectFolder() {
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);

        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(service));
        chooser.setTitle("Select a folder for print case files");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
        }
    }
    
    private void checkFolderField() throws EmfException {
        String specified = folder.getText();
        
        if (specified == null || specified.trim().isEmpty() || specified.trim().length() == 1)
            throw new EmfException("Please specify a valid folder.");
        
        if (specified.contains("/home/") || specified.endsWith("/home"))
            throw new EmfException("Export data into user's home directory is not allowed.");
        
        if (specified.charAt(0) != '/' && specified.charAt(1) != ':')
            throw new EmfException("Specified folder is not in a right format (ex. C:\\, /home, etc.).");
        
        if (specified.charAt(0) != '/' && !Character.isLetter(specified.charAt(0)))
            throw new EmfException("Specified folder is not in a right format (ex. C:\\).");
    }

}
