package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.importer.FilePatternMatcher;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.ui.FileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ImportInputPanel extends JPanel {

    private MessagePanel messagePanel;

    private DataCommonsService service;

    private DefaultComboBoxModel datasetTypesModel;

    private TextField name, pattern;

    private TextField folder;

    private TextArea filenames;

    private JCheckBox isMultipleDatasets;

    private static File lastFolder = null;

    public ImportInputPanel(DataCommonsService service, MessagePanel messagePanel) throws EmfException {
        this.messagePanel = messagePanel;
        this.service = service;
        initialize();
    }

    private void initialize() throws EmfException {
        setLayout(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        JComboBox datasetTypesComboBox = typesComboBox();
        layoutGenerator.addLabelWidgetPair("Dataset Type", datasetTypesComboBox, this);

        JPanel chooser = new JPanel(new BorderLayout(6,0));
        folder = new TextField("folder", 35);
        chooser.add(folder, BorderLayout.LINE_START);
        chooser.add(importFileButton(), BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Folder", chooser, this);

        JPanel apply = new JPanel(new BorderLayout(6,0));
        pattern = new TextField("pattern", 35);
        apply.add(pattern, BorderLayout.LINE_START);
        apply.add(applyPatternButton(), BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Pattern", apply, this);

        filenames = new TextArea("filenames", "", 35, 6);
        ScrollableTextArea fileTextArea = new ScrollableTextArea(filenames);
        fileTextArea.setMinimumSize(new Dimension(80, 20));
        layoutGenerator.addLabelWidgetPair("Filenames", fileTextArea, this);

        name = new TextField("name", 35);
        layoutGenerator.addLabelWidgetPair("Dataset Name", name, this);

        isMultipleDatasets = new JCheckBox("Create Multiple Datasets");
        isMultipleDatasets.addActionListener(multipleDatasetsActionListener());

        layoutGenerator.addLabelWidgetPair("", isMultipleDatasets, this);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(this, 6, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

    }

    private JComboBox typesComboBox() throws EmfException {
        DatasetType[] allDatasetTypes = service.getDatasetTypes();
        DatasetType[] allTypesWithMessage = new DatasetType[allDatasetTypes.length + 1];
        copyDatasetTypes(allDatasetTypes, allTypesWithMessage);
        datasetTypesModel = new DefaultComboBoxModel(allTypesWithMessage);
        JComboBox datasetTypesComboBox = new JComboBox(datasetTypesModel);
        datasetTypesComboBox.setName("datasetTypes");
        
        datasetTypesComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        return datasetTypesComboBox;
    }

    private ActionListener multipleDatasetsActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
                makeVisibleDatasetNameField(!isMultipleDatasets.isSelected());
            }
        };
    }

    protected void makeVisibleDatasetNameField(boolean singleDataset) {
        messagePanel.clear();
        name.setEnabled(singleDataset);
        name.setVisible(singleDataset);
    }

    private void copyDatasetTypes(DatasetType[] allDatasetTypes, DatasetType[] allTypesWithMessage) {
        allTypesWithMessage[0] = new DatasetType("Choose a type ...");
        for (int i = 0; i < allDatasetTypes.length; i++) {
            allTypesWithMessage[i + 1] = allDatasetTypes[i];
        }
    }

    private JButton importFileButton() {
        Button button = new Button("Choose File", new AbstractAction() {
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

    private void selectFilesFromPattern() {
        try {
            File inputFilesFolder = new File(folder.getText());
            FilePatternMatcher fpm = new FilePatternMatcher(inputFilesFolder, pattern.getText());
            String[] allFilesInFolder = inputFilesFolder.list();
            String[] fileNamesForImport = fpm.matchingNames(allFilesInFolder);
            if (fileNamesForImport.length > 0) {
                populateFilenamesFiled(fileNamesForImport);
            } else {
                messagePanel.setError("No files found for pattern '" + pattern.getText() + "'");
            }
        } catch (ImporterException e) {
            messagePanel.setError("Cannot apply pattern.");
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
        FileChooser chooser = new FileChooser("Select File", new File(folder.getText()), ImportInputPanel.this);
        chooser.setTitle("Select a " + datasetTypesModel.getSelectedItem().toString() + " File");
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
            name.setText("");
            lastFolder = file;
            return;
        }
        folder.setText(file.getParent());
        filenames.setText(file.getName());
        name.setText(file.getName());
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

    public boolean isCreateMutlipleDatasets() {
        return isMultipleDatasets.isSelected();
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

    public DatasetType datasetType() {
        return (DatasetType) datasetTypesModel.getSelectedItem();

    }

    public String datasetName() {
        return name.getText();
    }

    public void setMessage(String message) {
        messagePanel.setMessage(message);
    }

    private void clear() {
        messagePanel.clear();
    }

}
