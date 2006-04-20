package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

import org.jmock.Mock;

public class CaseBrowserPresenterTest extends EmfMockObjectTestCase {

    public void testShouldDisplayBrowserOnDisplay() throws EmfException {
        Mock browser = mock(CaseBrowserView.class);
        Case[] cases = new Case[0];
        expects(browser, 1, "display", eq(cases));

        Mock service = mock(CaseService.class);
        stub(service, "getCases", cases);

        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());

        CaseBrowserPresenter presenter = new CaseBrowserPresenter((EmfSession) session.proxy(),
                (CaseBrowserView) browser.proxy());
        expects(browser, 1, "observe", same(presenter));

        presenter.display();
    }

    public void testShouldRefreshBrowserOnRefresh() throws EmfException {
        Mock browser = mock(CaseBrowserView.class);
        Case[] cases = new Case[0];
        expects(browser, 1, "refresh", eq(cases));
        
        Mock service = mock(CaseService.class);
        stub(service, "getCases", cases);
        
        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());
        
        CaseBrowserPresenter presenter = new CaseBrowserPresenter((EmfSession) session.proxy(),
                (CaseBrowserView) browser.proxy());
        
        presenter.doRefresh();
    }

    public void testShouldRemoveCaseOnRemove() throws EmfException {
        Mock browser = mock(CaseBrowserView.class);

        Mock service = mock(CaseService.class);
        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());

        Case caseObj = new Case();
        expects(service, 1, "removeCase", same(caseObj));

        CaseBrowserPresenter presenter = new CaseBrowserPresenter((EmfSession) session.proxy(),
                (CaseBrowserView) browser.proxy());

        presenter.doRemove(caseObj);
    }

    public void testShouldAddCasesOnRemove() throws EmfException {
        Mock browser = mock(CaseBrowserView.class);

        Mock service = mock(CaseService.class);
        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());

        Case caseObj = new Case();
        expects(service, 1, "addCase", same(caseObj));

        CaseBrowserPresenter presenter = new CaseBrowserPresenter((EmfSession) session.proxy(),
                (CaseBrowserView) browser.proxy());

        presenter.doAdd(caseObj);
    }
}
