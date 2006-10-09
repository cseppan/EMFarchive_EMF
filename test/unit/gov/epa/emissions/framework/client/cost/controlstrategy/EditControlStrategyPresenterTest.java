package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyPresenter;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyPresenterImpl;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategySummaryTabView;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;

import org.jmock.Mock;

public class EditControlStrategyPresenterTest extends EmfMockObjectTestCase {

    public void FIXME_testShouldObserveLockControlStrategyAndDisplayViewOnDisplay() throws EmfException {
        Mock view = mock(EditControlStrategyView.class);
        Mock controlStrategyObj = mock(ControlStrategy.class);
        expects(view, 1, "display", same(controlStrategyObj.proxy()));
        stub(controlStrategyObj, "isLocked", Boolean.TRUE);

        Mock service = mock(ControlStrategyService.class);
        Mock session = mock(EmfSession.class);
        stub(session, "controlStrategyService", service.proxy());
        stub(session, "user", new User());
        expects(service, 1, "obtainLocked", controlStrategyObj.proxy());

        EditControlStrategyPresenter p = new EditControlStrategyPresenterImpl((ControlStrategy) controlStrategyObj.proxy(), (EmfSession) session.proxy(),
                (EditControlStrategyView) view.proxy(), null);
        expects(view, 1, "observe", same(p));

        p.doDisplay();
    }

    public void testShouldCloseControlStrategyViewOnClose() throws EmfException {
        Mock view = mock(EditControlStrategyView.class);
        expects(view, 1, "disposeView");

        Mock summaryTabView = mock(EditControlStrategySummaryTabView.class);
        expects(summaryTabView, 1, "stopRun");
        expects(summaryTabView, 1, "doRefresh");

        Mock service = mock(ControlStrategyService.class);
        Mock session = mock(EmfSession.class);
        stub(session, "controlStrategyService", service.proxy());
        expects(service, 1, "releaseLocked");
        expects(service, 1, "stopRunStrategy");

        EditControlStrategyPresenter p = new EditControlStrategyPresenterImpl(null, (EmfSession) session.proxy(), (EditControlStrategyView) view
                .proxy(), null);
        p.set((EditControlStrategySummaryTabView)summaryTabView.proxy());

        p.doClose();
    }

    public void testShouldSaveControlStrategyAndCloseViewOnSave() throws EmfException {
        Mock view = mock(EditControlStrategyView.class);
        //expects(view, 1, "disposeView");

        Mock service = mock(ControlStrategyService.class);
        ControlStrategy comtrolStrategy = new ControlStrategy("name");
        expects(service, 1, "updateControlStrategyWithLock", same(comtrolStrategy));
        stub(service, "getControlStrategies", new ControlStrategy[0]);

        Mock session = mock(EmfSession.class);
        stub(session, "controlStrategyService", service.proxy());
        stub(session, "user", new User());

        Mock managerPresenter = mock(ControlStrategiesManagerPresenter.class);
        expects(managerPresenter, 1, "doRefresh");

        EditControlStrategyPresenter p = new EditControlStrategyPresenterImpl(comtrolStrategy, (EmfSession) session.proxy(),
                (EditControlStrategyView) view.proxy(), (ControlStrategiesManagerPresenter) managerPresenter.proxy());

        p.doSave();
    }

    public void testShouldRaiseErrorIfDuplicateControlStrategyNameOnSave() {
        
        Mock view = mock(EditControlStrategyView.class);

        Mock service = mock(ControlStrategyService.class);
        
        ControlStrategy duplicateControlStrategy = new ControlStrategy("controlStrategy2");
        duplicateControlStrategy.setId(1243);
        ControlStrategy controlStrategyObj = new ControlStrategy("controlStrategy2");
        controlStrategyObj.setId(9324);
        
        ControlStrategy[] controlStrategies = new ControlStrategy[] { new ControlStrategy("controlStrategy1"), duplicateControlStrategy, controlStrategyObj };
        stub(service, "getControlStrategies", controlStrategies);

        Mock session = mock(EmfSession.class);
        stub(session, "controlStrategyService", service.proxy());

        EditControlStrategyPresenter p = new EditControlStrategyPresenterImpl(controlStrategyObj, (EmfSession) session.proxy(), (EditControlStrategyView) view.proxy(), null);

        try {
            p.doSave();
        } catch (EmfException e) {
            assertEquals("A Control Strategy named 'controlStrategy2' already exists.", e.getMessage());
            return;
        }

        fail("Should have raised an error if name is duplicate");
    }
}
