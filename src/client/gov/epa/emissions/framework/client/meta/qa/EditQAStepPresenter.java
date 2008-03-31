package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.qa.QAService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Date;
import java.util.StringTokenizer;

public class EditQAStepPresenter {

    private EditQAStepView view;

    private EmfDataset dataset;

    private EditableQATabView tabView;

    private QAStep qastep;

    private EmfSession session;

    private static String lastFolder = null;

    public EditQAStepPresenter(EditQAStepView view, EmfDataset dataset, EditableQATabView tabView, EmfSession session) {
        this.view = view;
        this.tabView = tabView;
        this.dataset = dataset;
        this.session = session;
    }

    public void display(QAStep step, String versionName) throws EmfException {
        view.observe(this);
        QAService qaService = session.qaService();
        QAProgram[] programs = qaService.getQAPrograms();
        QAStepResult result = qaService.getQAStepResult(step);
        view.display(step, result, programs, dataset, versionName, session);

        // Reversed the following line from behind the one after it to make sure that most recent folder
        // is displayed properly.
        this.qastep = step;
        view.setMostRecentUsedFolder(getFolder());
    }

    public void close() {
        view.disposeView();
    }

    public void save() throws EmfException {
        QAStep step = view.save();
        session.qaService().updateWitoutCheckingConstraints(new QAStep[] { step });
        tabView.refresh();
        //close();
    }

    public void run() throws EmfException {
        QAStep step = view.save();
        step.setStatus("In Progress");
        step.setDate(new Date());
        step.setWho(session.user().getUsername());
        tabView.refresh();
        session.qaService().runQAStep(step, session.user());
        //view.resetChanges();
    }

    public void export(QAStep qaStep, QAStepResult stepResult, String dirName) throws EmfException {
        lastFolder = dirName;

        if (stepResult == null || stepResult.getTable() == null)
            throw new EmfException("You must have run the QA step successfully before exporting ");

        qaStep.setOutputFolder(dirName);
        session.qaService().updateWitoutCheckingConstraints(new QAStep[] { qaStep });
        session.qaService().exportQAStep(qaStep, session.user(), dirName);
    }

    private String getFolder() {
        return (lastFolder != null) ? lastFolder : getDefaultFolder();
    }

    private String getDefaultFolder() {
        // Added code here to fix the Null Pointer exception that occurs at first launch of Edit Window
        // because qastep is null.
        String folder;
        if (qastep != null) {
            folder = qastep.getOutputFolder();
        } else {
            folder = session.preferences().outputFolder();
        }

        return folder;
    }

    public String userName() {
        return session.user().getUsername();
    }

    public void viewResults(QAStep qaStep, String exportDir) throws EmfException {
        QAStepResult qaResult = getStepResult(qaStep);

        if (qaResult == null)
            throw new EmfException("Please run the QA Step before viewing the result.");

        File localFile = new File(tempQAStepFilePath(exportDir, qaResult));
        try {
            if (!localFile.exists() || localFile.lastModified() != qaResult.getTableCreationDate().getTime()) {
                Writer output = new BufferedWriter(new FileWriter(localFile));
                try {
                    output.write( writerHeader(qaStep, qaResult, dataset.getName())+ getTableAsString(qaResult) );
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
        String separator = File.separator;
        UserPreference preferences = new DefaultUserPreferences();
        String tempDir = preferences.localTempDir();
        
        if (tempDir == null || tempDir.isEmpty())
            tempDir = System.getProperty("java.io.tmpdir");

        File tempDirFile = new File(tempDir);

        if (!(tempDirFile.exists() && tempDirFile.isDirectory() && tempDirFile.canWrite() && tempDirFile.canRead()))
            throw new EmfException("Import-export temporary folder does not exist or lacks write permissions: "
                    + tempDir);


        return tempDir + separator + qaStepResult.getTable() + ".csv"; // this is how exported file name was
    }
    
    private String writerHeader(QAStep qaStep, QAStepResult stepResult, String dsName){
        String lineFeeder = System.getProperty("line.separator");
        String header="#DATASET_NAME=" + dsName + lineFeeder;
        header +="#DATASET_VERSION_NUM= " + qaStep.getVersion() + lineFeeder;
        header +="#CREATION_DATE=" + CustomDateFormat.format_YYYY_MM_DD_HH_MM(stepResult.getTableCreationDate())+ lineFeeder;
        header +="#QA_STEP_NAME=" + qaStep.getName() + lineFeeder; 
        header +="#QA_PROGRAM=" + qaStep.getProgram()+ lineFeeder;
        String arguments= qaStep.getProgramArguments();
        StringTokenizer argumentTokenizer = new StringTokenizer(arguments);
        header += "#ARGUMENTS=" +argumentTokenizer.nextToken(); // get first token

        while (argumentTokenizer.hasMoreTokens()){
            String next = argumentTokenizer.nextToken().trim(); 
            if (next.contains("-"))
                header += lineFeeder+ "#" +next;
            else 
                header += " " +next;
        }
        header +=lineFeeder;
        //arguments.replaceAll(lineFeeder, "#");
        System.out.println("after replace  \n" + header);
        return header;
    }
    
//    private String exportedQAStepFilePath(String exportDir, QAStepResult qaStepResult) {
//        String separator = (exportDir.charAt(0) == '/') ? "/" : "\\";
//        return exportDir + separator + qaStepResult.getTable() + ".csv"; // this is how exported file name was
//    }

    public QAStepResult getStepResult(QAStep step) throws EmfException {
        return session.qaService().getQAStepResult(step);
    }

    public EmfDataset getDataset(String datasetName ) throws EmfException {
        return session.dataService().getDataset(datasetName);
    }

    public String getTableAsString(QAStepResult stepResult) throws EmfException {
        return session.dataService().getTableAsString("emissions." + stepResult.getTable());
    }

    public long getTableRecordCount(QAStepResult stepResult) throws EmfException {
        return session.dataService().getTableRecordCount("emissions." + stepResult.getTable());
    }
}
