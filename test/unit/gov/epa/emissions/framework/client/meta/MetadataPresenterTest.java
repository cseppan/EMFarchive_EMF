package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.stub.ThrowStub;

public class MetadataPresenterTest extends MockObjectTestCase {

    private Mock view;

    private MetadataPresenter presenter;

    private EmfDataset dataset;

    private Mock dataServices;

    protected void setUp() {
        dataset = new EmfDataset();
        dataset.setName("test");

        view = mock(MetadataView.class);

        dataServices = mock(DataServices.class);
        presenter = new MetadataPresenter(dataset, (DataServices) dataServices.proxy());

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(dataset));

        presenter.display((MetadataView) view.proxy());
    }

    public void testShouldCloseViewOnNotifyClose() {
        view.expects(once()).method("close");

        presenter.doClose();
    }

    public void testShouldUpdateDatasetUsingDataServicesOnSave() {
        dataServices.expects(once()).method("updateDataset").with(eq(dataset));

        Mock summaryView = mock(SummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        presenter.add((SummaryTabView) summaryView.proxy());

        presenter.doSave();
    }

    public void testShouldDisplayErrorMessageOnErrorDuringSave() {
        Mock summaryView = mock(SummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));

        dataServices.expects(once()).method("updateDataset").with(eq(dataset)).will(
                new ThrowStub(new EmfException("Could not save")));
        view.expects(once()).method("showError").with(eq("Could not update dataset - " + dataset.getName()));

        presenter.add((SummaryTabView) summaryView.proxy());
        presenter.doSave();
    }
}
