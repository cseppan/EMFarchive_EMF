package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.info.InfoTabView;
import gov.epa.emissions.framework.client.meta.logs.LogsTabView;
import gov.epa.emissions.framework.client.meta.notes.NotesTabView;
import gov.epa.emissions.framework.client.meta.revisions.RevisionsTabView;
import gov.epa.emissions.framework.services.basic.LoggingService;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.editor.Revision;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class PropertiesViewPresenterTest extends MockObjectTestCase {

    public void testShouldObserveAndDisplayViewOnDisplay() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(PropertiesView.class);
        view.expects(once()).method("display").with(eq(dataset));

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, null);
        view.expects(once()).method("observe").with(eq(presenter));

        presenter.doDisplay((PropertiesView) view.proxy());
    }

    public void testShouldCloseViewOnClose() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(PropertiesView.class);
        view.expects(once()).method("display").with(eq(dataset));

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, null);
        view.expects(once()).method("observe").with(eq(presenter));
        presenter.doDisplay((PropertiesView) view.proxy());

        view.expects(once()).method("close");

        presenter.doClose();
    }

    public void testShouldDisplayInfoTabOnSetInfoTab() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(InfoTabView.class);
        view.expects(once()).method("displayInternalSources");

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, null);

        presenter.set((InfoTabView) view.proxy());
    }

    public void testShouldDisplayDataTabOnSetDataTab() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(DataTabView.class);
        view.expects(once()).method("display");
        view.expects(once()).method("observe");

        Mock session = mock(EmfSession.class);
        session.stubs().method("dataViewService");

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, (EmfSession) session.proxy());

        presenter.set((DataTabView) view.proxy());
    }

    public void testShouldDisplayLogTabOnSetLogTab() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(LogsTabView.class);
        view.expects(once()).method("display");

        Mock session = mock(EmfSession.class);
        Mock loggingService = mock(LoggingService.class);
        loggingService.stubs().method("getAccessLogs");
        session.stubs().method("loggingService").will(returnValue(loggingService.proxy()));

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, (EmfSession) session.proxy());

        presenter.set((LogsTabView) view.proxy());
    }

    public void testShouldDisplayNotesTabOnSetNotesTab() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(NotesTabView.class);
        view.expects(once()).method("display");

        Mock session = mock(EmfSession.class);
        Mock service = mock(DataCommonsService.class);
        Note[] notes = new Note[0];
        service.stubs().method("getNotes").will(returnValue(notes));
        session.stubs().method("dataCommonsService").will(returnValue(service.proxy()));

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, (EmfSession) session.proxy());

        presenter.set((NotesTabView) view.proxy());
    }

    public void testShouldDisplayRevisionsTabOnSetRevisionsTab() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(RevisionsTabView.class);
        view.expects(once()).method("display");

        Mock session = mock(EmfSession.class);
        Mock service = mock(DataCommonsService.class);
        Revision[] revisions = new Revision[0];
        service.stubs().method("getRevisions").will(returnValue(revisions));
        session.stubs().method("dataCommonsService").will(returnValue(service.proxy()));

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, (EmfSession) session.proxy());

        presenter.set((RevisionsTabView) view.proxy());
    }
}
