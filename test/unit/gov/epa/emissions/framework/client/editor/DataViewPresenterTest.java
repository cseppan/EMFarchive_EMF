package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessService;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DataAccessToken;

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

        Mock service = mock(DataEditorService.class);
        Constraint constraint = tokenConstraint(version, table);
        service.expects(once()).method("openSession").with(constraint);

        DataAccessService serviceProxy = (DataAccessService) service.proxy();

        Mock view = mock(DataView.class);
        view.expects(once()).method("display").with(eq(version), eq(table), same(serviceProxy));

        DataViewPresenter p = new DataViewPresenter(version, table, (DataView) view.proxy(), serviceProxy);
        view.expects(once()).method("observe").with(same(p));

        p.display();
    }

    public void testShouldRaiseErrorWhenAttemptedToViewNonFinalVersionOnDisplay() throws Exception {
        Version version = new Version();
        DataViewPresenter p = new DataViewPresenter(version, null, null, null);

        try {
            p.display();
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an error if user attempts to view a non-final version");
    }

    public void testShouldCloseViewAndCloseDataEditSessionOnClose() throws Exception {
        Mock view = mock(DataView.class);
        view.expects(once()).method("close").withNoArguments();

        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        Constraint constraint = tokenConstraint(version, table);
        service.expects(once()).method("closeSession").with(constraint);

        DataViewPresenter p = new DataViewPresenter(version, table, (DataView) view.proxy(),
                (DataAccessService) service.proxy());

        p.doClose();
    }

    private Constraint tokenConstraint(Version version, String table) {
        Constraint propertyConstraint = and(new HasPropertyWithValue("version", same(version)),
                new HasPropertyWithValue("table", eq(table)));
        Constraint constraint = and(new IsInstanceOf(DataAccessToken.class), propertyConstraint);
        return constraint;
    }

}
