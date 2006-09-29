package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.client.EmfSession;
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
        view.display(step, result, programs, dataset, session.user(), versionName);
        view.setMostRecentUsedFolder(getFolder());
    }

    public void doClose() {
        view.disposeView();
    }

    public void doSave() throws EmfException {
        QAStep step = view.save();
        session.qaService().updateWitoutCheckingConstraints(new QAStep[] { step });
        tabView.refresh();
        doClose();
    }

    public void doRun() throws EmfException {
        QAStep step = view.save();
        step.setStatus("In Progress");
        step.setDate(new Date());
        step.setWho(session.user().getUsername());
        tabView.refresh();
        // step.setTableCreationStatus("In Progress");
        session.qaService().runQAStep(step, session.user());
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

}
