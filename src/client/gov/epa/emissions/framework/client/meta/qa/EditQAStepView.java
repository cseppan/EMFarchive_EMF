package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

public interface EditQAStepView extends ManagedView {

    void display(QAStep step, QAStepResult qaStepResult, QAProgram[] programs, EmfDataset dataset, String versionName, EmfSession session);

    void observe(EditQAStepPresenter presenter);
    
    QAStep save() throws EmfException;
    
    void setMostRecentUsedFolder(String mostRecentUsedFolder);

    void displayResultsTable(String qaStepName, String exportedFileName) throws EmfException;
    
    void updateArgumentsTextArea (String text);
    
    void updateInventories(Object [] inventories, Object [] invTables, String summaryType);
    
    void updateInventories(Object [] inventories, Object [] invTables, String summaryType, String emissionType);
    
    void updateInventories(Object [] invBase, Object [] invControl, Object [] invTables, String summaryType);
    
    void updateInventories(Object [] inventories);

    void updateDatasets(Object capInventory, 
            Object hapInventory, 
            Object speciationToolSpecieInfoDataset, 
            Object pollToPollConversionDataset, 
            Object[] speciationProfileWeightDatasets, 
            Object[] speciationCrossReferenceDatasets, 
            String filter,
            String summaryType);
}
