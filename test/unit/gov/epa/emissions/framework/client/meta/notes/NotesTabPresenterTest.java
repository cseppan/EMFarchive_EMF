package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Note;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class NotesTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Note[] notes = new Note[0];
        Mock view = mock(NotesTabView.class);
        view.expects(once()).method("display").with(eq(notes));

        Mock service = mock(DataCommonsService.class);
        EmfDataset dataset = new EmfDataset();
        dataset.setId(2);
        service.stubs().method("getNotes").with(eq(dataset.getId())).will(returnValue(notes));

        NotesTabPresenter presenter = new NotesTabPresenter(dataset, (DataCommonsService) service.proxy());

        presenter.display((NotesTabView) view.proxy());
    }

}
