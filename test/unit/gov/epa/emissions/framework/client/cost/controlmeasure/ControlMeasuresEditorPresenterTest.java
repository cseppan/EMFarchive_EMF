package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.CostService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jmock.Mock;

public class ControlMeasuresEditorPresenterTest extends EmfMockObjectTestCase {

    private Mock view;

    private ControlMeasuresEditorPresenterImpl presenter;

    private ControlMeasure measure;

    private Mock costService;

    private Mock session;

    protected void setUp() {
        measure = new ControlMeasure("new measure");
        view = mock(ControlMeasuresEditorView.class);
        costService = mock(CostService.class);

        session = mock(EmfSession.class);
        session.stubs().method("costService").withNoArguments().will(returnValue(costService.proxy()));

        ControlMeasuresEditorView viewProxy = (ControlMeasuresEditorView) view.proxy();
        EmfSession sessionProxy = (EmfSession) session.proxy();
        presenter = new ControlMeasuresEditorPresenterImpl(measure, viewProxy, sessionProxy);
    }

    public void testShouldDisplayViewOnDisplayAfterObtainingLock() throws Exception {
        User owner = new User();
        owner.setUsername("name");
        measure.setLockOwner(owner.getUsername());
        measure.setLockDate(new Date());

        session.stubs().method("user").withNoArguments().will(returnValue(owner));

        ControlMeasuresEditorView viewProxy = (ControlMeasuresEditorView) view.proxy();
        EmfSession sessionProxy = (EmfSession) session.proxy();
        presenter = new ControlMeasuresEditorPresenterImpl(measure, viewProxy, sessionProxy);

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(measure));

        presenter.doDisplay();
    }

    public void testShouldUpdateDatasetRefreshDatasetsBrowserAndCloseWindowOnSave() throws Exception {
        costService.expects(once()).method("addMeasure").with(eq(measure));
        expects(view, "disposeView");

        presenter.save(measure, (CostService) costService.proxy(), presenters(), (ControlMeasuresEditorView) view
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
