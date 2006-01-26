package gov.epa.emissions.framework.client.meta.summary;

import java.util.Date;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenter;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabView;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class EditableSummaryTabPresenterTest extends MockObjectTestCase {

    public void testUpdateDatasetOnSave() throws EmfException {
        Mock dataset = mock(EmfDataset.class);
        dataset.expects(once()).method("setModifiedDateTime").with(new IsInstanceOf(Date.class));

        Mock view = mock(EditableSummaryTabView.class);
        Object datasetProxy = dataset.proxy();
        view.expects(once()).method("updateDataset").with(eq(datasetProxy));

        EditableSummaryTabPresenter presenter = new EditableSummaryTabPresenter((EmfDataset) datasetProxy, (EditableSummaryTabView) view
                .proxy());

        presenter.doSave();
    }
}
