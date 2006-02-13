package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.Note;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.HasPropertyWithValue;
import org.jmock.core.constraint.IsInstanceOf;

public class DataViewPresenterTest extends MockObjectTestCase {

    public void testShouldLoadTablesOfDatasetOnDisplay() throws Exception {
        Version version = new Version();
        String table = "table";
        version.markFinal();

        Mock service = mock(DataViewService.class);
        Constraint constraint = tokenConstraint(version, table);
        service.expects(once()).method("openSession").with(constraint);

        DataViewService serviceProxy = (DataViewService) service.proxy();

        Mock view = mock(DataView.class);
        view.expects(once()).method("display").with(eq(version), eq(table), same(serviceProxy));

        EmfSession session = session(serviceProxy, null);
        DataViewPresenter p = new DataViewPresenter(version, table, (DataView) view.proxy(), session);
        view.expects(once()).method("observe").with(same(p));

        p.display();
    }

    private EmfSession session(DataViewService viewService, DataCommonsService commons) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("dataViewService").will(returnValue(viewService));
        session.stubs().method("dataCommonsService").will(returnValue(commons));

        return (EmfSession) session.proxy();
    }

    public void testShouldCloseViewAndCloseDataEditSessionOnClose() throws Exception {
        Mock view = mock(DataView.class);
        view.expects(once()).method("close").withNoArguments();

        Version version = new Version();
        String table = "table";

        Mock service = mock(DataViewService.class);
        Constraint constraint = tokenConstraint(version, table);
        service.expects(once()).method("closeSession").with(constraint);

        EmfSession session = session((DataViewService) service.proxy(), null);
        DataViewPresenter p = new DataViewPresenter(version, table, (DataView) view.proxy(), session);

        p.doClose();
    }

    private Constraint tokenConstraint(Version version, String table) {
        Constraint propertyConstraint = and(new HasPropertyWithValue("version", same(version)),
                new HasPropertyWithValue("table", eq(table)));
        Constraint constraint = and(new IsInstanceOf(DataAccessToken.class), propertyConstraint);
        return constraint;
    }

    public void testShouldAddNoteOnAdd() throws Exception {
        Mock service = mock(DataCommonsService.class);
        Note note = new Note();
        service.expects(once()).method("addNote").with(same(note));

        EmfSession session = session(null, (DataCommonsService) service.proxy());
        DataViewPresenter presenter = new DataViewPresenter(null, null, null, session);

        presenter.doAdd(note);
    }

}
