package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class EditableDatasetTypePresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewAfterObtainingLockForDatasetTypeOnDisplay() throws Exception {
        DatasetType type = new DatasetType();

        User user = new User();
        user.setFullName("name");
        type.setUsername(user.getFullName());
        type.setLockDate(new Date());

        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("getDatasetTypeLock").with(same(user), same(type)).will(returnValue(type));

        Keyword[] keywords = new Keyword[0];
        service.stubs().method("getKeywords").withNoArguments().will(returnValue(keywords));

        Mock session = session(user, service.proxy());

        Mock view = mock(EditableDatasetTypeView.class);

        EditableDatasetTypePresenter presenter = new EditableDatasetTypePresenter((EmfSession) session.proxy(),
                (EditableDatasetTypeView) view.proxy(), null, type);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(same(type), same(keywords));

        presenter.doDisplay();
    }

    private Mock session(User user, Object dataCommonsServiceProxy) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(user));
        session.stubs().method("dataCommonsService").withNoArguments().will(returnValue(dataCommonsServiceProxy));

        return session;
    }

    public void testShouldShowNonEditViewAfterFailingToObtainLockForSectorOnDisplay() throws Exception {
        DatasetType type = new DatasetType();// no lock
        User user = new User();
        user.setFullName("name");

        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("getDatasetTypeLock").with(same(user), same(type)).will(returnValue(type));

        Keyword[] keywords = new Keyword[0];
        service.stubs().method("getKeywords").withNoArguments().will(returnValue(keywords));

        Mock session = session(user, service.proxy());

        Mock view = mock(ViewableDatasetTypeView.class);
        view.expects(once()).method("observe").with(new IsInstanceOf(ViewableDatasetTypePresenter.class));
        view.expects(once()).method("display").with(eq(type), same(keywords));

        EditableDatasetTypePresenter presenter = new EditableDatasetTypePresenter((EmfSession) session.proxy(), null,
                (ViewableDatasetTypeView) view.proxy(), type);

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        DatasetType type = new DatasetType();
        Mock view = mock(EditableDatasetTypeView.class);
        view.expects(once()).method("close");

        User user = new User();
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("releaseDatasetTypeLock").with(same(user), same(type)).will(returnValue(type));

        Mock session = session(user, service.proxy());

        EditableDatasetTypePresenter presenter = new EditableDatasetTypePresenter((EmfSession) session.proxy(),
                (EditableDatasetTypeView) view.proxy(), null, type);

        presenter.doClose();
    }

    public void testShouldUpdateSectorAndCloseOnSave() throws Exception {
        String name = "name";
        String desc = "desc";
        Keyword[] keywords = {};

        Mock type = mock(DatasetType.class);
        type.expects(once()).method("setName").with(same(name));
        type.expects(once()).method("setDescription").with(same(desc));
        type.expects(once()).method("setKeywords").with(same(keywords));
        DatasetType typeProxy = (DatasetType) type.proxy();

        Mock view = mock(EditableDatasetTypeView.class);
        view.expects(once()).method("close");

        User user = new User();
        user.setUsername("test");
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("updateDatasetType").with(same(user), same(typeProxy)).will(
                returnValue(typeProxy));

        Mock session = session(user, service.proxy());

        EditableDatasetTypePresenter presenter = new EditableDatasetTypePresenter((EmfSession) session.proxy(),
                (EditableDatasetTypeView) view.proxy(), null, typeProxy);

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

        EditableDatasetTypePresenter presenter = new EditableDatasetTypePresenter(null, null, null, ((DatasetType) type
                .proxy()));
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
