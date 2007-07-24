package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.remote.RemoteCopy;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.qa.QAService;

import java.io.File;

public class ViewQAStepPresenter {

    private QAStepView view;

    private EmfDataset dataset;

    private EmfSession session;

    private static String lastFolder = null;

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
        view.setMostRecentUsedFolder(getFolder());
    }

    public void doClose() {
        view.disposeView();
    }

    public void doExport(QAStep qaStep, QAStepResult stepResult, String dirName) throws EmfException {
        File dir = new File(dirName);
        if (dir.isDirectory())
            lastFolder = dirName;

        if (stepResult == null || stepResult.getTable() == null)
            throw new EmfException("You have to run the QA step successfully before exporting ");

        session.qaService().exportQAStep(qaStep, session.user(), dirName);

    }

    private String getFolder() {
        return (lastFolder != null) ? lastFolder : getDefaultFolder();
    }

    private String getDefaultFolder() {
        String folder = session.preferences().outputFolder();
        if (!new File(folder).isDirectory())
            folder = "";// default, if unspecified

        return folder;
    }

    public String userName() {
        return session.user().getUsername();
    }

    public void viewResults(QAStep qaStep, String exportDir) throws EmfException {
        QAStepResult qaResult = getStepResult(qaStep);
        
        if (qaResult == null || qaResult.getTable() == null || qaResult.getTable().isEmpty())
            throw new EmfException("No QA Step result available to view.");
        
        File exported;
        try {
            RemoteCopy remoteCopy = new RemoteCopy(new DefaultUserPreferences(), session.user());
            String copied = remoteCopy.copyToLocal(exportedQAStepFilePath(exportDir, qaResult), "");
            exported = new File(copied);
            
            if (!exported.exists())
                throw new EmfException("Copy remote files failed.");
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
        
        view.displayResultsTable(qaStep.getName(), exported.getAbsolutePath());
    }
    
    private String exportedQAStepFilePath(String exportDir, QAStepResult qaStepResult) {
        String separator = (exportDir.charAt(0) == '/') ? "/" : "\\";
        return exportDir + separator + qaStepResult.getTable() + ".csv"; // this is how exported file name was created
    }

    public QAStepResult getStepResult(QAStep step) throws EmfException {
        return session.qaService().getQAStepResult(step);
    }

}
