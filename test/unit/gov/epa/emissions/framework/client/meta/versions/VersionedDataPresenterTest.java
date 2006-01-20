package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class VersionedDataPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        Mock view = mock(VersionedDataView.class);
        EmfDataset dataset = new EmfDataset();

        view.expects(once()).method("display").with(same(dataset), new IsInstanceOf(EditVersionsPresenter.class));

        VersionedDataPresenter p = new VersionedDataPresenter(null, dataset, null, null);
        view.expects(once()).method("observe").with(same(p));

        p.display((VersionedDataView) view.proxy());
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(VersionedDataView.class);
        VersionedDataPresenter p = displayPresenter(view);

        view.expects(once()).method("close").withNoArguments();
        p.doClose();
    }

    private VersionedDataPresenter displayPresenter(Mock view) {
        EmfDataset dataset = new EmfDataset();

        view.expects(once()).method("display").with(same(dataset), new IsInstanceOf(EditVersionsPresenter.class));

        VersionedDataPresenter p = new VersionedDataPresenter(null, dataset, null, null);
        view.expects(once()).method("observe").with(same(p));

        p.display((VersionedDataView) view.proxy());

        return p;
    }
}
