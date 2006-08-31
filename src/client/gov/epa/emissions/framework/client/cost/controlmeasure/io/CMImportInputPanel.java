package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.FileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;

public class CMImportInputPanel extends JPanel {

    private MessagePanel messagePanel;

    private CMImportPresenter presenter;

    private TextField pattern;

    private TextField folder;

    private TextArea filenames;

    private TextArea importStatusTextArea;

    private static File lastFolder = null;

    public CMImportInputPanel(MessagePanel messagePanel) {
        this.messagePanel = messagePanel;

        initialize();
    }

    private void initialize() {
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new SpringLayout());
        
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        JPanel chooser = new JPanel(new BorderLayout(6, 0));
        folder = new TextField("folder", 35);
        chooser.add(folder, BorderLayout.LINE_START);
        chooser.add(browseFileButton(), BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Folder", chooser, mainPanel);

        JPanel apply = new JPanel(new BorderLayout(6, 0));
        pattern = new TextField("pattern", 35);
        apply.add(pattern, BorderLayout.LINE_START);
        apply.add(applyPatternButton(), BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Pattern", apply, mainPanel);

        filenames = new TextArea("filenames", "", 35, 6);
        JScrollPane fileTextAreaPane = new JScrollPane(filenames, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        layoutGenerator.addLabelWidgetPair("Filenames", fileTextAreaPane, mainPanel);

        importStatusTextArea = new TextArea("Import Status", "", 35);
        JScrollPane statusTextAreaPane = new JScrollPane(importStatusTextArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        layoutGenerator.addLabelWidgetPair("Status", statusTextAreaPane, mainPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(mainPanel, 4, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad
        
        this.setLayout(new BorderLayout());
        this.add(mainPanel);

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
        FileChooser chooser = new FileChooser("Select File", new File(folder.getText()), CMImportInputPanel.this);
        chooser.setTitle("Select files for control measures import");
        File[] files = chooser.choose();
        if (files.length == 0)
            return;

        if (files.length > 1) {
            setFolder(files);
            String[] fileNames = new String[files.length];
            for (int i = 0; i < fileNames.length; i++) {
                fileNames[i] = files[i].getName();
            }
            populateFilenamesFiled(fileNames);
        } else {
            singleFile(files[0]);
        }
    }

    private void singleFile(File file) {
        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
            filenames.setText("");
            lastFolder = file;
            return;
        }
        folder.setText(file.getParent());
        filenames.setText(file.getName());
        lastFolder = file.getParentFile();
    }

    private void setFolder(File[] files) {
        if (files.length > 0) {
            folder.setText(files[0].getParentFile().toString());
            lastFolder = files[0].getParentFile();
        }
    }

    public void setDefaultBaseFolder(String folder) {
        if (lastFolder == null)
            this.folder.setText(folder);
        else
            this.folder.setText(lastFolder.getAbsolutePath());
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
