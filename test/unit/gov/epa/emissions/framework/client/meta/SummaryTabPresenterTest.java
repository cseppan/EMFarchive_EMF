package gov.epa.emissions.framework.client.meta;

import java.util.Date;

import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class SummaryTabPresenterTest extends MockObjectTestCase {

    public void testUpdateDatasetOnSave() {
        Mock dataset = mock(EmfDataset.class);
        dataset.expects(once()).method("setModifiedDateTime").with(new IsInstanceOf(Date.class));

        Mock view = mock(SummaryTabView.class);
        Object datasetProxy = dataset.proxy();
        view.expects(once()).method("updateDataset").with(eq(datasetProxy));

        SummaryTabPresenter presenter = new SummaryTabPresenter((EmfDataset) datasetProxy, (SummaryTabView) view
                .proxy());

        presenter.doSave();
    }
}
