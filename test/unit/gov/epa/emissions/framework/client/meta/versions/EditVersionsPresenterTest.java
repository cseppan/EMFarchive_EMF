package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.editor.DataEditorPresenter;
import gov.epa.emissions.framework.client.editor.DataEditorView;
import gov.epa.emissions.framework.client.editor.DataView;
import gov.epa.emissions.framework.client.editor.DataViewPresenter;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.EmfDataset;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class EditVersionsPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayTableViewOnView() throws Exception {
        Version version = new Version();
        String table = "table";
        version.markFinal();

        Mock service = mock(DataViewService.class);
        service.expects(once()).method("openSession").withAnyArguments();
        DataViewService serviceProxy = (DataViewService) service.proxy();

        Mock dataView = mock(DataView.class);
        dataView.expects(once()).method("display").with(same(version), eq(table), same(serviceProxy));
        dataView.expects(once()).method("observe").with(new IsInstanceOf(DataViewPresenter.class));

        EmfSession session = session(null, null, serviceProxy);
        EditVersionsPresenter presenter = new EditVersionsPresenter(null, session);
        presenter.doView(version, table, (DataView) dataView.proxy());
    }

    private EmfSession session(User user, DataEditorService editor, DataViewService view) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("dataEditorService").will(returnValue(editor));
        session.stubs().method("dataViewService").will(returnValue(view));
        session.stubs().method("user").will(returnValue(user));

        return (EmfSession) session.proxy();
    }

    public void testShouldRaiseErrorWhenAttemptedToViewNonFinalVersionOnDisplay() throws Exception {
        Version version = new Version();

        EditVersionsPresenter presenter = new EditVersionsPresenter(null, null);

        try {
            presenter.doView(version, null, null);
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an error if user attempts to view a non-final version");
    }

    public void testShouldDisplayEditableTableViewOnEdit() throws Exception {
        Version version = new Version();
        String table = "table";
        User user = new User();

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("openSession").withAnyArguments().will(returnValue(token()));
        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock dataView = mock(DataEditorView.class);
        dataView.expects(once()).method("display").with(same(version), eq(table), same(user), same(serviceProxy));
        dataView.expects(once()).method("observe").with(new IsInstanceOf(DataEditorPresenter.class));
        dataView.expects(once()).method("updateLockPeriod").with(new IsInstanceOf(Date.class),
                new IsInstanceOf(Date.class));

        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(user));

        EditVersionsPresenter presenter = new EditVersionsPresenter(null, session(user, serviceProxy, null));
        presenter.doEdit(version, table, (DataEditorView) dataView.proxy());
    }

    private DataAccessToken token() {
        Mock mock = mock(DataAccessToken.class);
        mock.stubs().method("isLocked").will(returnValue(Boolean.TRUE));
        mock.stubs().method("lockStart").will(returnValue(new Date()));
        mock.stubs().method("lockEnd").will(returnValue(new Date()));

        return (DataAccessToken) mock.proxy();
    }

    public void testShouldRaiseErrorOnEditWhenVersionIsFinal() throws Exception {
        Version version = new Version();
        version.markFinal();

        EditVersionsPresenter presenter = new EditVersionsPresenter(null, null);

        try {
            presenter.doEdit(version, null, null);
        } catch (EmfException e) {
            assertEquals("Cannot edit a Version(" + version.getVersion() + ") that is Final. Please choose 'View'.", e
                    .getMessage());
            return;
        }

        fail("Should have failed to edit a Version that is already Final.");
    }

    public void testShouldDeriveNewVersionOnNew() throws Exception {
        Version version = new Version();
        Version derived = new Version();
        String derivedName = "name";

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("derive").with(same(version), eq(derivedName)).will(returnValue(derived));

        Mock view = mock(EditVersionsView.class);
        view.expects(once()).method("add").with(same(derived));

        EditVersionsPresenter presenter = displayPresenter(service, view);

        presenter.doNew(version, derivedName);
    }

    private EditVersionsPresenter displayPresenter(Mock service, Mock view) throws EmfException {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(1);
        Version[] versions = new Version[0];
        InternalSource[] internalSources = new InternalSource[0];

        service.stubs().method("getVersions").with(eq(new Long(dataset.getId()))).will(returnValue(versions));
        EmfSession session = session(null, (DataEditorService) service.proxy(), null);
        EditVersionsPresenter presenter = new EditVersionsPresenter(dataset, session);
        view.expects(once()).method("observe").with(same(presenter));
        view.expects(once()).method("display").with(eq(versions), eq(internalSources));

        presenter.display((EditVersionsView) view.proxy());

        return presenter;
    }

    public void testShouldObserveAndDisplayViewOnDisplay() throws Exception {
        Mock service = mock(DataEditorService.class);
        Mock view = mock(EditVersionsView.class);

        displayPresenter(service, view);
    }

    public void testShouldMarkVersionAsFinalOnMarkFinal() throws Exception {
        Version version = new Version();
        version.setVersion(8);

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("markFinal").with(new IsInstanceOf(DataAccessToken.class)).will(
                returnValue(new Version()));

        Version[] versions = {};

        Mock view = mock(EditVersionsView.class);
        view.expects(once()).method("reload").with(eq(versions));

        EditVersionsPresenter p = displayPresenter(service, view);

        p.doMarkFinal(new Version[] { version });
    }

    public void testShouldRaiseErrorOnMarkFinalWhenVersionIsAlreadyFinal() throws Exception {
        Version version = new Version();
        version.setVersion(2);
        version.markFinal();

        EditVersionsPresenter p = new EditVersionsPresenter(null, null);

        try {
            p.doMarkFinal(new Version[] { version });
        } catch (EmfException e) {
            assertEquals("Version: " + version.getVersion() + " is already Final. It should be non-final.", e
                    .getMessage());
            return;
        }

        fail("Should have failed to mark Final when Version is already Final");
    }

}
