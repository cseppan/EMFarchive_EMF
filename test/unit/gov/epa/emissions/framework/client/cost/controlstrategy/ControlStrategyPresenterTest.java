package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;

import org.jmock.Mock;

public class ControlStrategyPresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveAndDisplayViewOnDisplay() {
        Mock view = mock(ControlStrategyView.class);
        expects(view, 1, "display");

        ControlStrategyPresenter p = new ControlStrategyPresenterImpl(null, (ControlStrategyView) view.proxy(), null);
        expects(view, 1, "observe", same(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws EmfException {
        Mock view = mock(ControlStrategyView.class);
        expects(view, 1, "close");

        ControlStrategyPresenter p = new ControlStrategyPresenterImpl(null, (ControlStrategyView) view.proxy(), null);

        p.doClose();
    }

    public void testShouldSaveControlStrategyAndCloseViewOnSave() throws EmfException {
        Mock view = mock(ControlStrategyView.class);
        expects(view, 1, "close");

        Mock service = mock(ControlStrategyService.class);
        ControlStrategy newControlStrategy = new ControlStrategy();
        expects(service, 1, "addControlStrategy", same(newControlStrategy));
        stub(service, "getControlStrategies", new ControlStrategy[0]);

        Mock session = mock(EmfSession.class);
        stub(session, "controlStrategyService", service.proxy());
        User user = new User();
        stub(session, "user", user);

        Mock managerPresenter = mock(ControlStrategiesManagerPresenter.class);
        expects(managerPresenter, 1, "doRefresh");

        ControlStrategyPresenter p = new ControlStrategyPresenterImpl((EmfSession) session.proxy(), (ControlStrategyView) view.proxy(),
                (ControlStrategiesManagerPresenter)managerPresenter.proxy());

        p.doSave(newControlStrategy);
        
        assertSame(user , newControlStrategy.getCreator());
        assertNotNull(newControlStrategy.getLastModifiedDate());
    }

    public void testShouldRaiseErrorIfDuplicateControlStrategyNameOnSave() {
        Mock view = mock(ControlStrategyView.class);

        Mock service = mock(ControlStrategyService.class);
        ControlStrategy newControlStrategy = new ControlStrategy("test-controlstrategy");
        ControlStrategy[] controlStrategies = new ControlStrategy[] { new ControlStrategy("test-controlstrategy") };
        stub(service, "getControlStrategies", controlStrategies);

        Mock session = mock(EmfSession.class);
        stub(session, "controlStrategyService", service.proxy());

        ControlStrategyPresenter p = new ControlStrategyPresenterImpl((EmfSession) session.proxy(), (ControlStrategyView) view.proxy(), null);

        try {
            p.doSave(newControlStrategy);
        } catch (EmfException e) {
            assertEquals("Duplicate name - 'test-controlstrategy'.", e.getMessage());
            return;
        }

        fail("Should have raised an error if name is duplicate");
    }
}
