package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.LoggingServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class LogsTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws EmfException {
        Mock view = mock(LogsTabView.class);
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetid(1);

        Mock loggingServices = mock(LoggingServices.class);
        AccessLog[] accessLogs = new AccessLog[0];
        loggingServices.expects(once()).method("getAccessLogs").with(eq(dataset.getDatasetid())).will(
                returnValue(accessLogs));

        LogsTabPresenter presenter = new LogsTabPresenter((LogsTabView) view.proxy(), dataset,
                (LoggingServices) loggingServices.proxy());

        view.expects(once()).method("display").with(eq(accessLogs));

        presenter.display();
    }

    public void testShouldDoNothingOnSave() {
        LogsTabPresenter presenter = new LogsTabPresenter(null, null, null);

        presenter.save();
    }

}
