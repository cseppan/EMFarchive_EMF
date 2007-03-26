package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class CMImportInputPanel extends JPanel {

    private MessagePanel messagePanel;

    private CMImportPresenter presenter;

    private TextField pattern;

    private TextField folder;

    private TextArea filenames;

    private TextArea importStatusTextArea;
    
    private EmfSession session;
    
    private EmfConsole parentConsole;

    private static String lastFolder = null;

    public CMImportInputPanel(EmfConsole parentConsole, MessagePanel messagePanel, EmfSession session) {
        this.messagePanel = messagePanel;
        this.session = session;
        this.parentConsole = parentConsole;

        initialize();
    }

    private void initialize() {
        
        int width = 40;
        
        JPanel mainPanel = new JPanel();
        //mainPanel.setLayout(new SpringLayout());
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
        
        //SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        JPanel chooser = new JPanel(new BorderLayout(10,10));
        folder = new TextField("folder", width);
        chooser.add(new JLabel("Folder     "),BorderLayout.WEST);
        chooser.add(folder);
        chooser.add(browseFileButton(), BorderLayout.EAST);
        
        //layoutGenerator.addLabelWidgetPair("Folder   ", chooser, mainPanel);

        JPanel apply = new JPanel(new BorderLayout(10,10));
        pattern = new TextField("pattern", width);
        apply.add(new JLabel("Pattern   "),BorderLayout.WEST);
        apply.add(pattern);
        apply.add(applyPatternButton(), BorderLayout.EAST);
        //layoutGenerator.addLabelWidgetPair("Pattern", apply, mainPanel);

        JPanel fileNamesPanel = new JPanel(new BorderLayout(4,10));
        filenames = new TextArea("filenames", "", width, 6);
        JScrollPane fileTextAreaPane = new JScrollPane(filenames, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fileNamesPanel.add(new JLabel("Filenames"),BorderLayout.WEST);
        fileNamesPanel.add(fileTextAreaPane);
        //layoutGenerator.addLabelWidgetPair("Filenames", fileTextAreaPane, mainPanel);

        JPanel statusPanel = new JPanel(new BorderLayout(10,10));
        importStatusTextArea = new TextArea("Import Status", "", width);
        JScrollPane statusTextAreaPane = new JScrollPane(importStatusTextArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        statusPanel.add(new JLabel("Status    "),BorderLayout.WEST);
        statusPanel.add(statusTextAreaPane);
        
        //layoutGenerator.addLabelWidgetPair("Status", statusTextAreaPane, mainPanel);

        // Lay out the panel.
        //layoutGenerator.makeCompactGrid(mainPanel, 4, 2, // rows, cols
        //        10, 10, // initialX, initialY
        //        10, 10);// xPad, yPad
        
        mainPanel.add(chooser);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(apply);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(fileNamesPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        this.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
        this.setLayout(new BorderLayout(10,10));
        this.add(mainPanel,BorderLayout.NORTH);
        this.add(statusPanel);
        

    }

    private JButton browseFileButton() {
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clear();
                selectFile();
            }
        });

        Icon icon = new ImageResources().open("Import a File");
        button.setIcon(icon);

        return button;
    }

    private JButton applyPatternButton() {
        Button button = new Button("Apply Pattern", new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clear();
                selectFilesFromPattern();
            }
        });

        return button;
    }

    public void register(CMImportPresenter presenter) {
        this.presenter = presenter;
    }

    private void selectFilesFromPattern() {
        try {
            populateFilenamesFiled(presenter.getFilesFromPatten(folder.getText(), pattern.getText()));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void populateFilenamesFiled(String[] files) {
        String text = "";
        for (int i = 0; i < files.length; i++) {
            text += files[i] + System.getProperty("line.separator");
        }
        filenames.setText(text);
    }

    private void selectFile() {
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);
        
        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle("Select files containing control measures to import");
        chooser.setDirectoryAndFileMode();
        
        int option = chooser.showDialog(parentConsole, "Select a file");

        EmfFileInfo[] files = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedFiles() : null;
        EmfFileInfo dir = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        
        if (dir != null && !dir.getAbsolutePath().equals(lastFolder)) {
            folder.setText(dir.getAbsolutePath());
            lastFolder = dir.getAbsolutePath();
            clearfilenames();
        }
        
        if (files == null || files.length == 0)
            return;

        if (files.length > 1) {
            String[] fileNames = new String[files.length];
            for (int i = 0; i < fileNames.length; i++) {
                fileNames[i] = files[i].getName();
            }
            populateFilenamesFiled(fileNames);
        } else {
            singleFile(files[0]);
        }
    }

    private void clearfilenames() {
        filenames.setText("");
    }
    
    private void singleFile(EmfFileInfo file) {
        filenames.setText(file.getName());
    }

    public void setDefaultBaseFolder(String folder) {
        if (lastFolder == null)
            this.folder.setText(folder);
        else
            this.folder.setText(lastFolder);
    }

    public String folder() {
        return folder.getText();
    }

    public String[] files() {
        List names = new ArrayList();
        int lines = filenames.getLineCount();
        try {
            for (int i = 0; i < lines; i++) {
                int start = filenames.getLineStartOffset(i);
                int end = filenames.getLineEndOffset(i);
                names.add(filenames.getText(start, end - start));
            }
            names = removeEmptyFileNams(names);

        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }
        return (String[]) names.toArray(new String[0]);
    }

    private List removeEmptyFileNams(List files) {
        List nonEmptyList = new ArrayList();
        for (int i = 0; i < files.size(); i++) {
            String file = (String) files.get(i);
            if (file.trim().length() != 0) {
                nonEmptyList.add(file.trim());
            }
        }
        return nonEmptyList;
    }

    public void setStartImportMessage(String message) {
        importStatusTextArea.clear();
        messagePanel.setMessage(message);
    }

    private void clear() {
        messagePanel.clear();
    }

    public void addStatusMessage(String messages) {
        importStatusTextArea.append(messages);
    }

}
