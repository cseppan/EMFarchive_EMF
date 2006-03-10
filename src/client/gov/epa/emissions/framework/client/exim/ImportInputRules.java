package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.EmfException;

public class ImportInputRules {

    public void validate(String directory, String[] files, DatasetType type, String datasetName) throws EmfException {
        validateDatasetType(type);
        validateDirectory(directory);
        validateFiles(files);
        validateDatasetName(datasetName);
        checkForMinAndMaxFiles(type, files);
    }

    public void validate(String directory, String[] files, DatasetType type) throws EmfException {
        validateDatasetType(type);
        validateDirectory(directory);
        validateFiles(files);
        checkMultipleFilesRequired(type);
    }

    private void checkMultipleFilesRequired(DatasetType type) throws EmfException {
        if(type.getMinFiles()>1){
            throw new EmfException("Sorry. You cannot create multiple datasets for this dataset type.");
        }
        
    }

    private void checkForMinAndMaxFiles(DatasetType type, String[] files) throws EmfException {
        int size = files.length;
        if (type.getMinFiles() > size) {
            throw new EmfException("The " + type.getName() + " requires atleast " + type.getMinFiles() + " files");
        }

        int maxFiles = type.getMaxFiles();
        if (maxFiles != -1 && maxFiles < size) {
            throw new EmfException("The " + type.getName() + " only takes atmost " + maxFiles + " files");
        }

    }

    private void validateFiles(String[] files) throws EmfException {
        if (files == null || files.length == 0) {
            throw new EmfException("Filename should be specified");
        }

    }

    private void validateDatasetName(String datasetName) throws EmfException {
        if (datasetName == null || datasetName.trim().length() == 0) {
            throw new EmfException("Dataset Name should be specified");
        }

    }

    private void validateDirectory(String directory) throws EmfException {
        if (directory == null || directory.trim().length() == 0) {
            throw new EmfException("Folder should be specified");
        }

    }

    private void validateDatasetType(DatasetType type) throws EmfException {
        if (type == null || type.getName().startsWith("Choose a type ...")) {
            throw new EmfException("Dataset Type should be selected");
        }

    }

}
