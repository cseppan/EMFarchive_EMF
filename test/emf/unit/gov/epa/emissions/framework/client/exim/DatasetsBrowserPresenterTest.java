package gov.epa.emissions.framework.client.exim;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DatasetsBrowserPresenterTest extends MockObjectTestCase {

    public void testShouldCloseViewOnClickOfCloseButton() {
        Mock view = mock(DatasetsBrowserView.class);

        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(null, (DatasetsBrowserView) view.proxy());

        view.expects(once()).method("setObserver").with(eq(presenter));
        view.expects(once()).method("close").withNoArguments();

        presenter.observe();
        presenter.notifyCloseView();
    }

}
