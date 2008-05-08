package gov.epa.emissions.framework.client.casemanagement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.MessagePanel;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class SetCaseFoldersPanel extends JPanel{

    private TextField inputDir;

    private TextField outputDir;
    
    private Case caseObj;
    
    private MessagePanel messagePanel;
    
    private EmfConsole parentConsole;
    
    private EmfSession session; 

    //private Dimension preferredSize = new Dimension(380, 20);
    
    public SetCaseFoldersPanel(Case caseObj, MessagePanel messagePanel, ManageChangeables changeablesList,
            EmfConsole parentConsole) {
        this.caseObj =caseObj;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
        this.inputDir = new TextField("inputdir", 50);
        inputDir.setText(caseObj.getInputFileDir());
        //addChangeable(inputDir);
        this.outputDir = new TextField("outputdir", 50);
        outputDir.setText(caseObj.getOutputFileDir());
        //addChangeable(outputDir);
        
    }

    public void display(CaseInput input, JComponent container, EmfSession session){
        this.session=session; 
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS) );
        panel.add(messagePanel);
        panel.add(createInputFolder());
        panel.add(createOutputFolder());

        container.add(panel);
    }
    
    private JPanel createInputFolder(){
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Input Folder"));
        panel.add(getFolderChooserPanel(inputDir,
        "Select the base Input Folder for the Case"));
        return panel; 
        
    }
    
    private JPanel createOutputFolder(){
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Output Job Scripts Folder"));
        panel.add(getFolderChooserPanel(outputDir,
                "Select the base Output Job Scripts Folder for the Case"));
        return panel; 
    }
    
    
    private JPanel getFolderChooserPanel(final JTextField dir, final String title) {
        Button browseButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                messagePanel.clear();
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
            if (title.toLowerCase().contains("output"))
                caseObj.setOutputFileDir(file.getAbsolutePath());
            else 
                caseObj.setOutputFileDir(file.getAbsolutePath());
            dir.setText(file.getAbsolutePath());
        }
    }    

    public void setFields() {
        //updateDataset();
        caseObj.setInputFileDir(inputDir.getText().trim());
        caseObj.setOutputFileDir(outputDir.getText().trim());
    }
    
}
