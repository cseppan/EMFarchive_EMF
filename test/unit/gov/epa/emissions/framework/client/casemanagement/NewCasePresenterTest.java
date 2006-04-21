package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

import org.jmock.Mock;

public class NewCasePresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveAndDisplayViewOnDisplay() {
        Mock view = mock(NewCaseView.class);
        expects(view, 1, "display");

        NewCasePresenter p = new NewCasePresenter(null, (NewCaseView) view.proxy());
        expects(view, 1, "observe", same(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(NewCaseView.class);
        expects(view, 1, "close");

        NewCasePresenter p = new NewCasePresenter(null, (NewCaseView) view.proxy());

        p.doClose();
    }

    public void testShouldSaveCaseAndCloseViewOnSave() throws EmfException {
        Mock view = mock(NewCaseView.class);
        expects(view, 1, "close");

        Mock service = mock(CaseService.class);
        Case newCase = new Case();
        expects(service, 1, "addCase", same(newCase));

        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());

        NewCasePresenter p = new NewCasePresenter((EmfSession) session.proxy(), (NewCaseView) view.proxy());

        p.doSave(newCase);
    }
}
