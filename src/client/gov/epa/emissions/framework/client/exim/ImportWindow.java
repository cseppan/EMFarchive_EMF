package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.ui.FileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ImportWindow extends ReusableInteralFrame implements ImportView {

    private ImportPresenter presenter;

    private MessagePanel messagePanel;

    private JTextField name;

    private JTextField filename;

    private DefaultComboBoxModel datasetTypesModel;

    private DataCommonsService service;

    private JTextField folder;

    private JCheckBox isMultipleDatasets;

    private static File lastFolder = null;

    private File[] fileSelected;

    public ImportWindow(DataCommonsService service, DesktopManager desktopManager) throws EmfException {
        super("Import Dataset", new Dimension(650, 300), desktopManager);
        super.setName("importDatasets");

        this.service = service;

        this.getContentPane().add(createLayout());
    }

    private JPanel createLayout() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(createInputPanel());
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createInputPanel() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        DatasetType[] allDatasetTypes = service.getDatasetTypes();
        DatasetType[] allTypesWithMessage = new DatasetType[allDatasetTypes.length + 1];
        copyDatasetTypes(allDatasetTypes, allTypesWithMessage);
        datasetTypesModel = new DefaultComboBoxModel(allTypesWithMessage);
        JComboBox datasetTypesComboBox = new JComboBox(datasetTypesModel);
        datasetTypesComboBox.setName("datasetTypes");
        layoutGenerator.addLabelWidgetPair("Dataset Type", datasetTypesComboBox, panel);
        datasetTypesComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearTextField();
                multipleDatasetsSelected();
            }
        });

        JPanel chooser = new JPanel(new BorderLayout());
        folder = new TextField("folder", 35);
        chooser.add(folder, BorderLayout.LINE_START);
        chooser.add(importFileButton(), BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("Folder", chooser, panel);

        filename = new TextField("filename", 35);
        layoutGenerator.addLabelWidgetPair("Filename", filename, panel);

        name = new TextField("name", 35);
        layoutGenerator.addLabelWidgetPair("Dataset Name", name, panel);

        isMultipleDatasets = new JCheckBox("Create Multiple Datasets");
        isMultipleDatasets.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                multipleDatasetsSelected();
            }
        });
        layoutGenerator.addLabelWidgetPair("", isMultipleDatasets, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                20, 10, // initialX, initialY
                10, 10);// xPad, yPad

        registerForEditEvents(name, folder, filename);// edit-awareness

        return panel;
    }

    private void clearTextField() {
        filename.setText("");
        name.setText("");
        messagePanel.clear();
    }

    private void multipleDatasetsSelected() {
        enableAll();
        if (isMultipleDatasets.isSelected()) {
            name.setEnabled(false);
            name.setVisible(false);
            checkMinFiles();
        }
    }

    private boolean meetMultipleFileCondition() {
        DatasetType datasetType = (DatasetType) datasetTypesModel.getSelectedItem();
        if (fileSelected.length > 1 && !isMultipleDatasets.isSelected() && datasetType.getMaxFiles() == 1) {
            messagePanel.setError("Please check Create Multiple Datasets box.");
            return false;
        }

        return true;
    }

    private void checkMinFiles() {
        DatasetType dt = (DatasetType) datasetTypesModel.getSelectedItem();
        if (dt.getMinFiles() > 1)
            messagePanel.setError("Sorry. You cannot create multiple datasets for this dataset type.");
    }

    private void enableAll() {
        messagePanel.clear();
        name.setEnabled(true);
        name.setVisible(true);
        folder.setEnabled(true);
        filename.setEnabled(true);
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
                selectFile();
            }
        });

        Icon icon = new ImageResources().open("Import a File");
        button.setIcon(icon);

        return button;
    }

    private void selectFile() {
        FileChooser chooser = new FileChooser("Select File", new File(folder.getText()), ImportWindow.this);
        chooser.setTitle("Select a " + datasetTypesModel.getSelectedItem().toString() + " File");
        fileSelected = chooser.choose();
        if (fileSelected.length == 1)
            singleFile(fileSelected[0]);
        else
            multipleFiles(fileSelected);
    }

    private void singleFile(File file) {
        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
            filename.setText("");
            lastFolder = file;
            return;
        }

        folder.setText(file.getParent());
        filename.setText(file.getName());
        name.setText(file.getName());
        lastFolder = file.getParentFile();
    }

    private void multipleFiles(File[] file) {
        for (int i = 0; i < file.length; i++) {
            if (!file[i].isDirectory()) {
                if (i == 0)
                    filename.setText(file[i].getName());
                else
                    filename.setText(filename.getText() + ", " + file[i].getName());
            }
        }

        folder.setText(file[0].getParent());
        name.setText("");
        lastFolder = file[0].getParentFile();
    }

    private void registerForEditEvents(JTextField name, JTextField directory, JTextField filename) {
        name.getDocument().addDocumentListener(notifyBeginInput());
        directory.getDocument().addDocumentListener(notifyBeginInput());
        filename.getDocument().addDocumentListener(notifyBeginInput());
    }

    private DocumentListener notifyBeginInput() {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                presenter.notifyBeginInput();
            }

            public void removeUpdate(DocumentEvent event) {
                presenter.notifyBeginInput();
            }

            public void changedUpdate(DocumentEvent event) {// ignore
            }
        };
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        JButton importButton = new Button("Import", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doImport();
            }
        });
        container.add(importButton);
        getRootPane().setDefaultButton(importButton);

        JButton done = new Button("Done", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doDone();
            }
        });
        container.add(done);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    public void register(ImportPresenter presenter) {
        this.presenter = presenter;
    }

    /**
     * If the checkbox is selected then start importing multiple datasets of the datasetType specified The fileName is a
     * regular expression for multiple datasets
     * 
     */
    private void doImport() {
        String message = "Started import. Please monitor the Status window to track your Import request.";
        DatasetType datasetType = (DatasetType) datasetTypesModel.getSelectedItem();

        clearMessagePanel();
        if (filename.getText().equals("")) {
            messagePanel.setError("Nothing to import.");
            return;
        }
        if (!meetMultipleFileCondition())
            return;
        if (!nameBeginWithLetter(name.getText()))
            return;

        try {
            if (isMultipleDatasets.isSelected() && fileSelected.length == 1) {
                presenter.doImportDatasetForEveryFileInPattern(folder.getText(), filename.getText(), datasetType);
            }

            if (fileSelected.length > 1 && datasetType.getMaxFiles() == 1) {
                presenter.doImportDatasetForEachFile(folder.getText(), datasetNames(), datasetType);
            }

            if (fileSelected.length > 1 && datasetType.getMinFiles() > 1) {
                presenter.doImportDatasetUsingMultipleFiles(folder.getText(), datasetNames(), name.getText(),
                        datasetType);
            }

            if (fileSelected.length == 1 && datasetType.getMaxFiles() == 1) {
                presenter.doImportDatasetUsingSingleFile(folder.getText(), filename.getText(), name.getText(),
                        datasetType);
            }

            messagePanel.setMessage(message);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private boolean nameBeginWithLetter(String name) {
        if (!name.equals("") && Character.isDigit(name.charAt(0))) {
            messagePanel.setError("Dataset name has to begin with a letter");
            return false;
        }

        return true;
    }

    private String[] datasetNames() {
        String[] names = new String[fileSelected.length];
        for (int i = 0; i < names.length; i++)
            names[i] = fileSelected[i].getName();

        return names;
    }

    public void clearMessagePanel() {
        messagePanel.clear();
        super.refreshLayout();
    }

    public void setDefaultBaseFolder(String folder) {
        if (lastFolder == null)
            this.folder.setText(folder);
        else
            this.folder.setText(lastFolder.getAbsolutePath());
    }
}
