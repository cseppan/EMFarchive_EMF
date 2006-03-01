package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.importer.FilePatternMatcher;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInternalFrame;
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
    
    private ImportInputRules importrule;

    private static File lastFolder = null;

    private File[] fileSelected;

    private EmfInternalFrame parent;
    
    public ImportInputPanel(DataCommonsService service, MessagePanel messagePanel, EmfInternalFrame parent) 
        throws EmfException {
        this.messagePanel = messagePanel;
        this.service = service;
        this.parent = parent;
        initialize();
    }

    private void initialize() throws EmfException {
        setLayout(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        DatasetType[] allDatasetTypes = service.getDatasetTypes();
        DatasetType[] allTypesWithMessage = new DatasetType[allDatasetTypes.length + 1];
        copyDatasetTypes(allDatasetTypes, allTypesWithMessage);
        datasetTypesModel = new DefaultComboBoxModel(allTypesWithMessage);
        JComboBox datasetTypesComboBox = new JComboBox(datasetTypesModel);
        datasetTypesComboBox.setName("datasetTypes");
        layoutGenerator.addLabelWidgetPair("Dataset Type", datasetTypesComboBox, this);
        datasetTypesComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearTextField();
            }
        });
        
        JPanel chooser = new JPanel(new BorderLayout());
        folder = new TextField("folder", 35);
        chooser.add(folder, BorderLayout.LINE_START);
        chooser.add(importFileButton(), BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Folder", chooser, this);
        
        JPanel apply = new JPanel(new BorderLayout());
        pattern = new TextField("pattern", 35);
        apply.add(pattern, BorderLayout.LINE_START);
        apply.add(applyPatternButton(), BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Pattern", apply, this);
        
        filenames = new TextArea("filenames", "", 35);
        ScrollableTextArea fileTextArea = new ScrollableTextArea(filenames);
        fileTextArea.setMinimumSize(new Dimension(80, 10));
        layoutGenerator.addLabelWidgetPair("Filename", fileTextArea, this);

        name = new TextField("name", 35);
        layoutGenerator.addLabelWidgetPair("Dataset Name", name, this);

        isMultipleDatasets = new JCheckBox("Create Multiple Datasets");

        layoutGenerator.addLabelWidgetPair("", isMultipleDatasets, this);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(this, 6, 2, // rows, cols
                20, 10, // initialX, initialY
                10, 10);// xPad, yPad
        
        importrule = new ImportInputRules( messagePanel, datasetTypesModel, name,
            folder, filenames, isMultipleDatasets, parent);
    }
    
    private void copyDatasetTypes(DatasetType[] allDatasetTypes, DatasetType[] allTypesWithMessage) {
        allTypesWithMessage[0] = new DatasetType("Choose a type ...");
        for (int i = 0; i < allDatasetTypes.length; i++) {
            allTypesWithMessage[i + 1] = allDatasetTypes[i];
        }
    }
    
    private void clearTextField() {
        filenames.setText("");
        name.setText("");
        messagePanel.clear();
        parent.refreshLayout();
    }
    
    private JButton importFileButton() {
        Button button = new Button("Choose File", new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
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
                selectFilesFromPattern();
            }
        });

        return button;
    }
    
    private void selectFilesFromPattern() {
        clearTextField();
        try {
            File inputFilesFolder = new File(folder.getText());
            FilePatternMatcher fpm = new FilePatternMatcher(inputFilesFolder, pattern.getText());
            String[] allFilesInFolder = inputFilesFolder.list();
            String[] fileNamesForImport = fpm.matchingNames(allFilesInFolder);
            fileSelected = new File[fileNamesForImport.length];
            for(int i = 0; i < fileNamesForImport.length; i++)
                fileSelected[i] = new File(inputFilesFolder, fileNamesForImport[i]);
            populateFilenamesFiled();    
        } catch (ImporterException e) {
            messagePanel.setError("Cannot apply pattern.");
        }

    }
    
    private void selectFile() {
        clearTextField();
        FileChooser chooser = new FileChooser("Select File", new File(folder.getText()), ImportInputPanel.this);
        chooser.setTitle("Select a " + datasetTypesModel.getSelectedItem().toString() + " File");
        fileSelected = chooser.choose();
        populateFilenamesFiled();
    }

    private void populateFilenamesFiled() {
        if (fileSelected.length == 1)
            singleFile(fileSelected[0]);

        if (fileSelected.length > 1)
            multipleFiles(fileSelected);
    }
    
    private void singleFile(File file) {
        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
            filenames.setText("");
            lastFolder = file;
            return;
        }

        folder.setText(file.getParent());
        filenames.append(file.getName());
        name.setText(file.getName());
        lastFolder = file.getParentFile();
    }

    private void multipleFiles(File[] file) {
        for (int i = 0; i < file.length; i++) {
            if (!file[i].isDirectory())
                filenames.append(file[i].getName()+ System.getProperty("line.separator"));
        }

        folder.setText(file[0].getParent());
        name.setText("");
        lastFolder = file[0].getParentFile();
    }
    
    public void setDefaultBaseFolder(String folder) {
        if (lastFolder == null)
            this.folder.setText(folder);
        else
            this.folder.setText(lastFolder.getAbsolutePath());
    }
    
    public void doImport(ImportPresenter presenter) {
        importrule.doImport(presenter);
    }

}
