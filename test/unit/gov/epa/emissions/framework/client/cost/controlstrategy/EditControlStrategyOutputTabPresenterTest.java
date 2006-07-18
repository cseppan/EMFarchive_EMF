package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyOutputTabPresenter;
import gov.epa.emissions.framework.client.preference.UserPreference;
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

    protected void setUp() {
        session = mock(EmfSession.class);

        session.stubs().method("user").withNoArguments().will(returnValue(null));
        session.stubs().method("eximService").withNoArguments().will(returnValue(null));

        folder = "foo/blah";
        setPreferences(session, folder);
    }

    private void setPreferences(Mock session, String folder) {
        prefs = mock(UserPreference.class);
        session.stubs().method("preferences").will(returnValue(prefs.proxy()));
        prefs.stubs().method("mapLocalOutputPathToRemote").will(returnValue(folder));
        prefs.stubs().method("outputFolder").will(returnValue(folder));
    }

    public void testDoExportSendsRequestToEximServiceOnExport() throws Exception {
        User user = new User();
        user.setUsername("user");
        user.setName("full name");
        
        Mock dataset = mock(EmfDataset.class);
        
        StrategyResult result = new StrategyResult();
        result.setDetailedResultDataset((Dataset) dataset.proxy());
        StrategyResult[] results = {result};
        ControlStrategy controlStrategy = new ControlStrategy();
        controlStrategy.setStrategyResults(results);
        
        Mock model = mock(ExImService.class);
        model.expects(once()).method("exportDatasetsWithOverwrite");

        session.stubs().method("user").withNoArguments().will(returnValue(user));
        session.stubs().method("eximService").withNoArguments().will(returnValue(model.proxy()));

        EditControlStrategyOutputTabPresenter presenter = new EditControlStrategyOutputTabPresenter((EmfSession) session.proxy());

        presenter.doExport(controlStrategy, folder);
    }

}
