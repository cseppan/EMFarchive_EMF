package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.qa.QAService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class ViewQAStepPresenter {

    private QAStepView view;

    private EmfDataset dataset;

    private EmfSession session;

    public ViewQAStepPresenter(QAStepView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.session = session;
    }

    public void display(QAStep step, String versionName) throws EmfException {
        QAService qaService = session.qaService();
        QAProgram[] programs = qaService.getQAPrograms();
        QAStepResult result = qaService.getQAStepResult(step);
        view.display(step, result, programs, dataset, session.user(), versionName);
    }

    public void doClose() {
        view.disposeView();
    }

    public void doExport(QAStep qaStep, QAStepResult stepResult, String dirName) throws EmfException {
        if (stepResult == null || stepResult.getTable() == null)
            throw new EmfException("You have to run the QA step successfully before exporting ");

        qaStep.setOutputFolder(dirName);
        session.qaService().updateWitoutCheckingConstraints(new QAStep[] { qaStep });
        session.qaService().exportQAStep(qaStep, session.user(), dirName);

    }

    public String userName() {
        return session.user().getUsername();
    }

    public void viewResults(QAStep qaStep, String exportDir) throws EmfException {
        QAStepResult qaResult = getStepResult(qaStep);
        
        if (qaResult == null || qaResult.getTable() == null || qaResult.getTable().isEmpty())
            throw new EmfException("No QA Step result available to view.");
        
        File localFile = new File(tempQAStepFilePath(exportDir, qaResult));
        try {
            if (!localFile.exists() || localFile.lastModified() != qaResult.getTableCreationDate().getTime()) {
                Writer output = new BufferedWriter(new FileWriter(localFile));
                try {
                    output.write( getTableAsString(qaResult) );
                }
                finally {
                    output.close();
                    localFile.setLastModified(qaResult.getTableCreationDate().getTime());
                }
            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
        
        view.displayResultsTable(qaStep.getName(), localFile.getAbsolutePath());
    }
    
    private String tempQAStepFilePath(String exportDir, QAStepResult qaStepResult) throws EmfException {
        String separator = exportDir.length() > 0 ? (exportDir.charAt(0) == '/') ? "/" : "\\" : "\\";
        String tempDir = System.getProperty("IMPORT_EXPORT_TEMP_DIR");

        if (tempDir == null || tempDir.isEmpty())
            tempDir = System.getProperty("java.io.tmpdir");

        File tempDirFile = new File(tempDir);

        if (!(tempDirFile.exists() && tempDirFile.isDirectory() && tempDirFile.canWrite() && tempDirFile.canRead()))
            throw new EmfException("Import-export temporary folder does not exist or lacks write permissions: "
                    + tempDir);


        return tempDir + separator + qaStepResult.getTable() + ".csv"; // this is how exported file name was
    }
    
//    private String exportedQAStepFilePath(String exportDir, QAStepResult qaStepResult) {
//        String separator = (exportDir.charAt(0) == '/') ? "/" : "\\";
//        //System.
//        return exportDir + separator + qaStepResult.getTable() + ".csv"; // this is how exported file name was created
//    }

    public QAStepResult getStepResult(QAStep step) throws EmfException {
        return session.qaService().getQAStepResult(step);
    }

    public String getTableAsString(QAStepResult stepResult) throws EmfException {
        return session.dataService().getTableAsString("emissions." + stepResult.getTable());
    }

    public long getTableRecordCount(QAStepResult stepResult) throws EmfException {
        return session.dataService().getTableRecordCount("emissions." + stepResult.getTable());
    }

}
