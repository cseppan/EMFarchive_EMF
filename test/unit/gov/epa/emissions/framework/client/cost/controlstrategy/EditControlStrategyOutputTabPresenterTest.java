package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyOutputTabPresenter;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyOutputTabView;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.LoggingService;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class EditControlStrategyOutputTabPresenterTest extends MockObjectTestCase {

    private Mock session;

    private String folder;

    private Mock prefs;

    private String fileNameOnLocalDrive;

    protected void setUp() {
        session = mock(EmfSession.class);

        session.stubs().method("user").withNoArguments().will(returnValue(null));
        session.stubs().method("eximService").withNoArguments().will(returnValue(null));

        folder = "foo/blah";
        fileNameOnLocalDrive = "foo";
        setPreferences(session, folder);
    }

    private void setPreferences(Mock session, String folder) {
        prefs = mock(UserPreference.class);
        session.stubs().method("preferences").will(returnValue(prefs.proxy()));
        prefs.stubs().method("mapLocalOutputPathToRemote").will(returnValue(folder));
        prefs.stubs().method("outputFolder").will(returnValue(folder));
        prefs.stubs().method("mapRemoteOutputPathToLocal").will(returnValue(fileNameOnLocalDrive));
    }

    public void testDoExportSendsRequestToEximServiceOnExport() throws Exception {
        User user = new User();
        user.setUsername("user");
        user.setName("full name");

        Mock dataset = mock(EmfDataset.class);
        ControlStrategy controlStrategy = setupControlStrategy(dataset);

        Mock model = mock(ExImService.class);
        model.expects(once()).method("exportDatasetsWithOverwrite");

        session.stubs().method("user").withNoArguments().will(returnValue(user));
        session.stubs().method("eximService").withNoArguments().will(returnValue(model.proxy()));
        session.stubs().method("setMostRecentExportFolder").with(eq(folder));

        EditControlStrategyOutputTabPresenter presenter = new EditControlStrategyOutputTabPresenter(
                (EmfSession) session.proxy(), null);

        presenter.doExport(controlStrategy, folder);
    }

    private ControlStrategy setupControlStrategy(Mock dataset) {
        StrategyResult result = new StrategyResult();
        result.setDetailedResultDataset((Dataset) dataset.proxy());
        StrategyResult[] results = { result };
        ControlStrategy controlStrategy = new ControlStrategy();
        controlStrategy.setStrategyResults(results);
        return controlStrategy;
    }

    public void testDoAnalyzeShouldOpenAnalyzeEngineTableApp() throws EmfException {
        Mock dataset = mock(EmfDataset.class);
        int id = 201;
        dataset.expects(once()).method("getId").withNoArguments().will(returnValue(id));
        
        ControlStrategy controlStrategy = setupControlStrategy(dataset);
        
        Mock loggingService = mock(LoggingService.class);
        
        loggingService.expects(once()).method("getLastExportedFileName").with(eq(id)).will(returnValue(fileNameOnLocalDrive));
        session.stubs().method("loggingService").withNoArguments().will(returnValue(loggingService.proxy()));
        
        
        Mock view = mock(EditControlStrategyOutputTabView.class);
        view.expects(once()).method("displayAnalyzeTable").with(eq(new String[]{fileNameOnLocalDrive}));
        
        EditControlStrategyOutputTabPresenter presenter = new EditControlStrategyOutputTabPresenter(
                (EmfSession) session.proxy(), (EditControlStrategyOutputTabView) view.proxy());

        presenter.doAnalyze(controlStrategy);
    }

}
