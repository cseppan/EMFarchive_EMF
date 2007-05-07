package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.qa.QAService;

import java.io.File;
import java.util.Date;

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

        session.qaService().exportQAStep(qaStep, session.user(), mapToRemote(dirName));

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

    private String mapToRemote(String dir) throws EmfException {
        if (dir == null || dir.trim().length() == 0)
            throw new EmfException("Please select a directory before export");
        return session.preferences().mapLocalOutputPathToRemote(dir);
    }

    public String userName() {
        return session.user().getUsername();
    }

    public void viewResults(QAStep qaStep, QAStepResult qaStepResult, String exportDir) throws EmfException {
        File exported = new File(exportDir, exportedQAStepFileName(qaStep));
        
        if (!exported.exists())
            throw new EmfException("Please export run results before view them.");
        
        view.displayResultsTable(qaStep.getName(), exported.getAbsolutePath());
    }
    
    private String exportedQAStepFileName(QAStep qaStep) {
        String formattedDate = EmfDateFormat.format_ddMMMyyyy(new Date());
        String result = "QA" + qaStep.getName() + "_DSID" + qaStep.getDatasetId() + "_V" + qaStep.getVersion() + "_"
                + formattedDate;

        for (int i = 0; i < result.length(); i++) {
            if (!Character.isJavaLetterOrDigit(result.charAt(i))) {
                result = result.replace(result.charAt(i), '_');
            }
        }

        return result.trim().replaceAll(" ", "_") + ".csv";
    }

}
