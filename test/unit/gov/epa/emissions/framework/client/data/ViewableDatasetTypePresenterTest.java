package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class ViewableDatasetTypePresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        DatasetType type = new DatasetType();


        Mock view = mock(ViewableDatasetTypeView.class);
        ViewableDatasetTypePresenter presenter = new ViewableDatasetTypePresenterImpl((ViewableDatasetTypeView) view
                .proxy(), type);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(same(type));

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(ViewableDatasetTypeView.class);
        view.expects(once()).method("close");

        ViewableDatasetTypePresenter presenter = new ViewableDatasetTypePresenterImpl((ViewableDatasetTypeView) view
                .proxy(), null);

        presenter.doClose();
    }

}
