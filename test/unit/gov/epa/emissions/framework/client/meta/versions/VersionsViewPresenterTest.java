package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.editor.DataView;
import gov.epa.emissions.framework.client.editor.DataViewPresenter;
import gov.epa.emissions.framework.services.DataAccessService;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class VersionsViewPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayTableViewOnView() throws Exception {
        Version version = new Version();
        String table = "table";
        version.markFinal();

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("openSession").withAnyArguments();
        DataAccessService serviceProxy = (DataAccessService) service.proxy();

        Mock dataView = mock(DataView.class);
        dataView.expects(once()).method("display").with(same(version), eq(table), same(serviceProxy));
        dataView.expects(once()).method("observe").with(new IsInstanceOf(DataViewPresenter.class));

        VersionsViewPresenter presenter = new VersionsViewPresenter(null, serviceProxy);
        presenter.doView(version, table, (DataView) dataView.proxy());
    }

    public void testShouldRaiseErrorWhenAttemptedToViewNonFinalVersionOnDisplay() throws Exception {
        Version version = new Version();
        VersionsViewPresenter presenter = new VersionsViewPresenter(null, null);

        try {
            presenter.doView(version, null, null);
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an error if user attempts to view a non-final version");
    }

    private VersionsViewPresenter displayPresenter(Mock service, Mock view) throws EmfException {
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetid(1);
        Version[] versions = new Version[0];
        InternalSource[] internalSources = new InternalSource[0];

        service.stubs().method("getVersions").with(eq(new Long(dataset.getDatasetid()))).will(returnValue(versions));

        VersionsViewPresenter presenter = new VersionsViewPresenter(dataset, (DataAccessService) service.proxy());
        view.expects(once()).method("observe").with(same(presenter));
        view.expects(once()).method("display").with(eq(versions), eq(internalSources));

        presenter.display((VersionsView) view.proxy());

        return presenter;
    }

    public void testShouldObserveAndDisplayViewOnDisplay() throws Exception {
        Mock service = mock(DataEditorService.class);
        Mock view = mock(VersionsView.class);

        displayPresenter(service, view);
    }

}
