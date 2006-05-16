package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.CostService;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jmock.Mock;

public class ControlMeasuresEditorPresenterTest extends EmfMockObjectTestCase {

    private Mock view;

    private EditorControlMeasurePresenterImpl presenter;

    private ControlMeasure measure;

    private Mock costService;

    private Mock session;
    
    private Mock managerPresenter;

    protected void setUp() {
        measure = new ControlMeasure();
        measure.setName("");
        view = mock(ControlMeasureView.class);
        costService = mock(CostService.class);
        
        managerPresenter = mock(RefreshObserver.class);
        managerPresenter.stubs().method("doRefresh");

        session = mock(EmfSession.class);
        session.stubs().method("costService").withNoArguments().will(returnValue(costService.proxy()));

        ControlMeasureView viewProxy = (ControlMeasureView) view.proxy();
        EmfSession sessionProxy = (EmfSession) session.proxy();
        presenter = new EditorControlMeasurePresenterImpl(measure, viewProxy, sessionProxy, (RefreshObserver)managerPresenter.proxy());
    }

    public void testShouldDisplayViewOnDisplayAfterObtainingLock() throws Exception {
        User owner = new User();
        owner.setUsername("name");
        measure.setLockOwner(owner.getUsername());
        measure.setLockDate(new Date());
        
        costService.stubs().method("obtainLockedMeasure").with(eq(owner), eq(measure)).will(returnValue(measure));

        session.stubs().method("user").withNoArguments().will(returnValue(owner));

        ControlMeasureView viewProxy = (ControlMeasureView) view.proxy();
        EmfSession sessionProxy = (EmfSession) session.proxy();
        presenter = new EditorControlMeasurePresenterImpl(measure, viewProxy, sessionProxy, (RefreshObserver)managerPresenter.proxy());

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(measure));

        presenter.doDisplay();
    }

    public void testShouldUpdateDatasetRefreshDatasetsBrowserAndCloseWindowOnSave() throws Exception {
        //costService.expects(once()).method("addMeasure").with(eq(measure));
        expects(view, "disposeView");
        presenter.save(measure, (CostService) costService.proxy(), presenters(), (ControlMeasureView) view
                .proxy());
    }

    private List presenters() {
        List presenters = new ArrayList();
        presenters.add(summaryMockForSave());
        return presenters;
    }

    private EditableCMSummaryTabPresenter summaryMockForSave() {
        Mock summaryTab = mock(EditableCMSummaryTabPresenter.class);
        summaryTab.expects(once()).method("doSave");
        return (EditableCMSummaryTabPresenter) summaryTab.proxy();
    }

}
