package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

import org.jmock.Mock;

public class EditCasePresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveAndDisplayViewOnDisplay() {
        Mock view = mock(EditCaseView.class);
        Case caseObj = new Case();
        expects(view, 1, "display", same(caseObj));

        EditCasePresenter p = new EditCasePresenter(caseObj, null, (EditCaseView) view.proxy(), null);
        expects(view, 1, "observe", same(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(EditCaseView.class);
        expects(view, 1, "close");

        EditCasePresenter p = new EditCasePresenter(null, null, (EditCaseView) view.proxy(), null);

        p.doClose();
    }

    public void testShouldSaveCaseAndCloseViewOnSave() throws EmfException {
        Mock view = mock(EditCaseView.class);
        expects(view, 1, "close");

        Mock service = mock(CaseService.class);
        Case caseObj = new Case();
        expects(service, 1, "updateCase", same(caseObj));
        stub(service, "getCases", new Case[0]);

        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());

        Mock managerPresenter = mock(CaseManagerPresenter.class);
        expects(managerPresenter, 1, "doRefresh");

        EditCasePresenter p = new EditCasePresenter(caseObj, (EmfSession) session.proxy(), (EditCaseView) view.proxy(),
                (CaseManagerPresenter) managerPresenter.proxy());

        p.doSave();
    }

    public void testShouldRaiseErrorIfDuplicateCaseNameOnSave() {
        Mock service = mock(CaseService.class);
        Case caseObj = new Case("test-case");
        Case[] cases = new Case[] { new Case("test-case") };
        stub(service, "getCases", cases);

        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());

        EditCasePresenter p = new EditCasePresenter(caseObj, (EmfSession) session.proxy(), null, null);

        try {
            p.doSave();
        } catch (EmfException e) {
            assertEquals("Duplicate name - 'test-case'.", e.getMessage());
            return;
        }

        fail("Should have raised an error if name is duplicate");
    }
}
