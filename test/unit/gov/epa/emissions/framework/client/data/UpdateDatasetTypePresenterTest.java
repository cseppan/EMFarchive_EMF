package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.services.DataCommonsService;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class UpdateDatasetTypePresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        DatasetType type = new DatasetType();

        Mock interdata = mock(DataCommonsService.class);
        Keyword[] keywords = new Keyword[0];
        interdata.stubs().method("getKeywords").withNoArguments().will(returnValue(keywords));

        Mock view = mock(UpdateDatasetTypeView.class);
        UpdateDatasetTypePresenter presenter = new UpdateDatasetTypePresenter((UpdateDatasetTypeView) view.proxy(),
                type, null, (DataCommonsService) interdata.proxy());
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
        String name = "name";
        String desc = "desc";
        Keyword[] keywords = {};

        Mock type = mock(DatasetType.class);
        type.expects(once()).method("setName").with(same(name));
        type.expects(once()).method("setDescription").with(same(desc));
        type.expects(once()).method("setKeywords").with(same(keywords));
        DatasetType typeProxy = (DatasetType) type.proxy();

        Mock services = mock(DatasetTypeService.class);
        services.expects(once()).method("updateDatasetType").with(same(typeProxy));

        Mock view = mock(UpdateDatasetTypeView.class);
        view.expects(once()).method("close");

        UpdateDatasetTypePresenter presenter = new UpdateDatasetTypePresenter((UpdateDatasetTypeView) view.proxy(),
                typeProxy, (DatasetTypeService) services.proxy(), null);

        Mock managerView = mock(DatasetTypesManagerView.class);
        managerView.expects(once()).method("refresh").withNoArguments();

        presenter.doSave(name, desc, keywords, (DatasetTypesManagerView) managerView.proxy());
    }

    public void testShouldFailWithErrorIfDuplicateKeywordsInKeyValsOnSave() {
        String name = "name";
        String desc = "desc";

        Mock type = mock(DatasetType.class);
        type.expects(once()).method("setName").with(same(name));
        type.expects(once()).method("setDescription").with(same(desc));

        UpdateDatasetTypePresenter presenter = new UpdateDatasetTypePresenter(null, ((DatasetType) type.proxy()), null,
                null);
        Mock managerView = mock(DatasetTypesManagerView.class);

        Keyword key1 = new Keyword("1");
        try {
            presenter.doSave(name, desc, new Keyword[] { key1, key1 }, (DatasetTypesManagerView) managerView.proxy());
        } catch (EmfException e) {
            assertEquals("duplicate keyword: '1'", e.getMessage());
            return;
        }

        fail("should have raised an error on duplicate keyword entries");
    }
}
