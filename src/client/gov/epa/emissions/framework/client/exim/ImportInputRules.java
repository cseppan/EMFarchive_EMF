package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;

public class ImportInputRules {

    private MessagePanel messagePanel;
    
    private DefaultComboBoxModel datasetTypesModel;
    
    private TextField name, folder;

    private TextArea filenames;

    private JCheckBox isMultipleDatasets;
    
    private EmfInternalFrame parent;

    public ImportInputRules(MessagePanel messagePanel, DefaultComboBoxModel datasetTypesModel, TextField name,
            TextField folder, TextArea filenames, JCheckBox isMultipleDatasets, EmfInternalFrame parent) {
        this.messagePanel = messagePanel;
        this.datasetTypesModel = datasetTypesModel;
        this.name = name;
        this.folder = folder;
        this.filenames = filenames;
        this.isMultipleDatasets = isMultipleDatasets;
        this.parent = parent;
        initialize();
    }
    
    private void initialize() {
        isMultipleDatasets.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                multipleDatasetsSelected();
            }
        });
    }
    
    private String[] filesSelected() {
        StringTokenizer st = new StringTokenizer(filenames.getText(), System.getProperty("line.separator"));
        List files = new ArrayList();
        while(st.hasMoreTokens()) {
            files.add(st.nextToken());
        }
        
        return (String[])files.toArray(new String[0]);
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
        DatasetType datasetType = (DatasetType)datasetTypesModel.getSelectedItem();
        if (filesSelected().length > 1 && !isMultipleDatasets.isSelected() && datasetType.getMaxFiles() == 1) {
            messagePanel.setError("Please check Create Multiple Datasets box.");
            return false;
        }

        return true;
    }

    private void checkMinFiles() {
        if (((DatasetType)datasetTypesModel.getSelectedItem()).getMinFiles() > 1)
            messagePanel.setError("Sorry. You cannot create multiple datasets for this dataset type.");
    }

    private void enableAll() {
        messagePanel.clear();
        name.setEnabled(true);
        name.setVisible(true);
        folder.setEnabled(true);
        filenames.setEnabled(true);
    }

    public void doImport(ImportPresenter presenter) {
        String message = "Started import. Please monitor the Status window to track your Import request.";
        DatasetType datasetType = (DatasetType)datasetTypesModel.getSelectedItem();
        
        if(!validate())
            return;
        
        try {
            if (filesSelected().length > 1 && datasetType.getMaxFiles() == 1) {
                presenter.doImport(folder.getText(), filesSelected(), datasetType);
            }
            
            if (filesSelected().length > 1 && datasetType.getMinFiles() > 1) {
                presenter.doImport(folder.getText(), filesSelected(), datasetType, name.getText());
            }
            
            if (filesSelected().length == 1 && datasetType.getMaxFiles() == 1) {
                presenter.doImport(folder.getText(), filesSelected(), datasetType, name.getText());
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
    
    private boolean validate() {
        boolean validation = true;
        DatasetType type = (DatasetType)datasetTypesModel.getSelectedItem();
        
        clearMessagePanel();
        
        if (name.getText().length() == 0 && name.isEnabled()) {
            messagePanel.setError("Dataset Name should be specified");
            validation = false;
        }
        
        if (folder.getText().length() == 0) {
            messagePanel.setError("Folder should be specified");
            validation = false;
        }
        
        if (filesSelected().length == 0) {
            messagePanel.setError("Filename should be specified");
            validation = false;
        }
        
        if (type.getName().equals("Choose a type ...")) {
            messagePanel.setError("Dataset Type should be selected");
            validation = false;
        }
        
        if (!meetMultipleFileCondition())
            validation = false;
        
        if (!nameBeginWithLetter(name.getText()))
            validation =false;
        
        return validation;
    }

    public void clearMessagePanel() {
        messagePanel.clear();
        parent.refreshLayout();
    }
}
