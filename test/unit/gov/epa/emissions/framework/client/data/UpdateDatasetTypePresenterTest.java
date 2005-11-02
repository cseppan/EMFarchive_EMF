package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.services.InterDataServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UpdateDatasetTypePresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        DatasetType type = new DatasetType();

        Mock interdata = mock(InterDataServices.class);
        Keyword[] keywords = new Keyword[0];
        interdata.stubs().method("getKeywords").withNoArguments().will(returnValue(keywords));

        Mock view = mock(UpdateDatasetTypeView.class);
        UpdateDatasetTypePresenter presenter = new UpdateDatasetTypePresenter((UpdateDatasetTypeView) view.proxy(),
                type, null, (InterDataServices) interdata.proxy());
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(same(type), same(keywords));

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() {
        DatasetType type = new DatasetType();
        Mock view = mock(UpdateDatasetTypeView.class);
        view.expects(once()).method("close");

        UpdateDatasetTypePresenter presenter = new UpdateDatasetTypePresenter((UpdateDatasetTypeView) view.proxy(),
                type, null, null);

        presenter.doClose();
    }

    public void testShouldUpdateSectorAndCloseOnSave() throws EmfException {
        DatasetType type = new DatasetType();
        Mock services = mock(DatasetTypesServices.class);
        services.expects(once()).method("updateDatasetType").with(same(type));

        Mock view = mock(UpdateDatasetTypeView.class);
        view.expects(once()).method("close");
        UpdateDatasetTypePresenter presenter = new UpdateDatasetTypePresenter((UpdateDatasetTypeView) view.proxy(),
                type, (DatasetTypesServices) services.proxy(), null);

        Mock managerView = mock(DatasetTypesManagerView.class);
        managerView.expects(once()).method("refresh").withNoArguments();

        presenter.doSave((DatasetTypesManagerView) managerView.proxy());
    }

}
