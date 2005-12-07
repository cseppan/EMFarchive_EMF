package gov.epa.emissions.framework.client.editor;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class VersionsPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayTableViewOnView() {
        VersionsPresenter presenter = new VersionsPresenter();
        
        Mock tableView = mock(TableView.class);
//        tableView.expects(once()).method("display").with(new IsInstanceOf(Page.class));
        
        presenter.doView((TableView)tableView.proxy());
    }

}
