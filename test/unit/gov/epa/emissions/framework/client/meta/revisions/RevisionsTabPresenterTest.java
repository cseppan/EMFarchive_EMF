package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Revision;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class RevisionsTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Revision[] revisions = new Revision[0];
        Mock view = mock(RevisionsTabView.class);
        view.expects(once()).method("display").with(eq(revisions));

        Mock service = mock(DataCommonsService.class);
        EmfDataset dataset = new EmfDataset();
        dataset.setId(2);
        service.stubs().method("getRevisions").with(eq(dataset.getId())).will(returnValue(revisions));

        RevisionsTabPresenter presenter = new RevisionsTabPresenter(dataset, (DataCommonsService) service.proxy());

        presenter.display((RevisionsTabView) view.proxy());
    }

}
