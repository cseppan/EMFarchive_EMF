package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.version.Version;
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

import java.util.Date;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.IsInstanceOf;

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
        session.stubs().method("getMostRecentExportFolder").withNoArguments().will(returnValue(folder));
        setPreferences(session, folder);
    }

    private void setPreferences(Mock session, String folder) {
        prefs = mock(UserPreference.class);
        session.stubs().method("preferences").will(returnValue(prefs.proxy()));
        prefs.stubs().method("mapLocalOutputPathToRemote").will(returnValue(folder));
        prefs.stubs().method("outputFolder").will(returnValue(folder));
        prefs.stubs().method("mapRemoteOutputPathToLocal").will(returnValue(fileNameOnLocalDrive));
    }
    
    public void testSendsExportRequestToEximServiceOnExport() throws Exception {
        User user = new User();
        user.setUsername("user");
        user.setName("full name");
        
        EmfDataset dataset = new EmfDataset();
        ControlStrategy controlStrategy = setupControlStrategy(dataset);
        
        Version version = new Version();
        
        Mock model = mock(ExImService.class);
        model.expects(once()).method("exportDatasetsWithOverwrite").with(
                new Constraint[] { eq(user), new IsInstanceOf(EmfDataset[].class), new IsInstanceOf(Version[].class), 
                        same(folder), new IsInstanceOf(String.class) });
        model.stubs().method("getVersion").with(eq(dataset), eq(0)).will(returnValue(version));
        
        session.stubs().method("user").withNoArguments().will(returnValue(user));
        session.stubs().method("eximService").withNoArguments().will(returnValue(model.proxy()));
        session.expects(once()).method("setMostRecentExportFolder").with(eq(folder));

        EditControlStrategyOutputTabPresenter presenter = new EditControlStrategyOutputTabPresenter(
                (EmfSession) session.proxy(), null);

        presenter.doExport(controlStrategy, folder);
    }

    private ControlStrategy setupControlStrategy(Dataset dataset) {
        StrategyResult result = new StrategyResult();
        result.setDetailedResultDataset(dataset);
        StrategyResult[] results = { result };
        ControlStrategy controlStrategy = new ControlStrategy();
        controlStrategy.setStrategyResults(results);
        return controlStrategy;
    }
    
    public void testDoAnalyzeShouldOpenAnalyzeEngineTableApp() throws EmfException {
        EmfDataset dataset = new EmfDataset();
        int id = 201;
        dataset.setName("dataset test");
        dataset.setAccessedDateTime(new Date());
        dataset.setId(id);
        
        ControlStrategy controlStrategy = setupControlStrategy(dataset);
        
        Mock loggingService = mock(LoggingService.class);
        
        loggingService.expects(once()).method("getLastExportedFileName").with(eq(id)).will(returnValue(fileNameOnLocalDrive));
        session.stubs().method("loggingService").withNoArguments().will(returnValue(loggingService.proxy()));
        
        
        Mock view = mock(EditControlStrategyOutputTabView.class);
        view.expects(once()).method("displayAnalyzeTable").with(eq(controlStrategy.getName()),eq(new String[]{fileNameOnLocalDrive}));
        
        EditControlStrategyOutputTabPresenter presenter = new EditControlStrategyOutputTabPresenter(
                (EmfSession) session.proxy(), (EditControlStrategyOutputTabView) view.proxy());

        presenter.doAnalyze(controlStrategy.getName(),controlStrategy.getStrategyResults());
    }

}
