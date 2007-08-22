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
import java.util.Date;

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
        close();
    }

    public void run() throws EmfException {
        QAStep step = view.save();
        step.setStatus("In Progress");
        step.setDate(new Date());
        step.setWho(session.user().getUsername());
        tabView.refresh();
        session.qaService().runQAStep(step, session.user());
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
        return exportDir + separator + qaStepResult.getTable() + ".csv"; // this is how exported file name was
                                                                            // created
    }

    public QAStepResult getStepResult(QAStep step) throws EmfException {
        return session.qaService().getQAStepResult(step);
    }

    public EmfDataset getDataset(String datasetName ) throws EmfException {
        return session.dataService().getDataset(datasetName);
    }
}
