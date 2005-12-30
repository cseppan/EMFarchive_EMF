package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.services.DataCommonsService;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class ViewableDatasetTypePresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        DatasetType type = new DatasetType();

        Mock interdata = mock(DataCommonsService.class);
        Keyword[] keywords = new Keyword[0];
        interdata.stubs().method("getKeywords").withNoArguments().will(returnValue(keywords));

        Mock view = mock(ViewableDatasetTypeView.class);
        ViewableDatasetTypePresenter presenter = new ViewableDatasetTypePresenter((ViewableDatasetTypeView) view
                .proxy(), type, (DataCommonsService) interdata.proxy());
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(same(type), same(keywords));

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(ViewableDatasetTypeView.class);
        view.expects(once()).method("close");

        ViewableDatasetTypePresenter presenter = new ViewableDatasetTypePresenter((ViewableDatasetTypeView) view
                .proxy(), null, null);

        presenter.doClose();
    }

}
