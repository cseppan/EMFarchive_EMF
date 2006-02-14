package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.services.NoteType;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;

public class EditNotesTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Note[] notes = new Note[0];
        NoteType[] noteTypes = new NoteType[0];
        Version[] versions = new Version[0];
        EmfDataset dataset = new EmfDataset();

        Mock view = mock(EditNotesTabView.class);
        User user = new User();
        Constraint[] constraints = { same(user), same(dataset), same(notes), same(noteTypes), same(versions) };
        view.expects(once()).method("display").with(constraints);

        Mock dataCommons = mock(DataCommonsService.class);
        dataset.setId(2);
        dataCommons.stubs().method("getNotes").with(eq(dataset.getId())).will(returnValue(notes));
        dataCommons.stubs().method("getNoteTypes").will(returnValue(noteTypes));

        Mock dataEditor = mock(DataEditorService.class);
        dataEditor.stubs().method("getVersions").with(eq(dataset.getId())).will(returnValue(versions));

        EmfSession session = session(user, (DataCommonsService) dataCommons.proxy(), (DataEditorService) dataEditor
                .proxy());
        EditNotesTabPresenter presenter = new EditNotesTabPresenterImpl(dataset, session, (EditNotesTabView) view
                .proxy());

        presenter.display();
    }

    private EmfSession session(User user, DataCommonsService dataCommons, DataEditorService dataEditor) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("dataCommonsService").will(returnValue(dataCommons));
        session.stubs().method("dataEditorService").will(returnValue(dataEditor));
        session.stubs().method("user").will(returnValue(user));

        return (EmfSession) session.proxy();
    }

    public void testShouldAddNoteOnSave() throws Exception {
        Mock service = mock(DataCommonsService.class);
        Note note = new Note();
        service.expects(once()).method("addNote").with(same(note));

        Mock view = mock(EditNotesTabView.class);
        Note[] notes = new Note[] { note };
        view.expects(once()).method("additions").will(returnValue(notes));

        EditNotesTabView viewProxy = (EditNotesTabView) view.proxy();
        EmfDataset dataset = new EmfDataset();
        dataset.setId(2);

        User user = new User();
        EmfSession session = session(user, (DataCommonsService) service.proxy(), null);

        EditNotesTabPresenter presenter = new EditNotesTabPresenterImpl(dataset, session, viewProxy);
        presenter.doSave();
    }
}
