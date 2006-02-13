package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.Note;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class AddNotesPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Mock view = mock(AddNotesView.class);
        view.expects(once()).method("display");

        Mock service = mock(DataCommonsService.class);

        AddNotesPresenter presenter = new AddNotesPresenter((DataCommonsService) service.proxy());

        presenter.display((AddNotesView) view.proxy());
    }

    public void testShouldCloseDisplayViewOnCancel() throws Exception {
        Mock view = mock(AddNotesView.class);
        view.expects(once()).method("display");
        view.expects(once()).method("close");

        Mock service = mock(DataCommonsService.class);

        AddNotesPresenter presenter = new AddNotesPresenter((DataCommonsService) service.proxy());

        presenter.display((AddNotesView) view.proxy());

        presenter.doCancel();
    }

    public void testShouldAddNoteOnAdd() throws Exception {
        Mock service = mock(DataCommonsService.class);
        Note note = new Note();
        service.expects(once()).method("addNote").with(same(note));

        AddNotesPresenter presenter = new AddNotesPresenter((DataCommonsService) service.proxy());

        presenter.doAdd(note);
    }

}
