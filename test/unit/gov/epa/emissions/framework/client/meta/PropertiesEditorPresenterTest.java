package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabPresenter;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabView;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenter;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class PropertiesEditorPresenterTest extends MockObjectTestCase {

    private Mock view;

    private PropertiesEditorPresenterImpl presenter;

    private EmfDataset dataset;

    private Mock dataService;

    private Mock dataCommonsService;

    private Mock session;

    protected void setUp() {
        dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        view = mock(DatasetPropertiesEditorView.class);

        dataService = mock(DataService.class);
        dataCommonsService = mock(DataCommonsService.class);
        dataCommonsService.stubs().method("getKeywords").withNoArguments().will(returnValue(new Keyword[0]));

        session = mock(EmfSession.class);
        session.stubs().method("dataService").withNoArguments().will(returnValue(dataService.proxy()));
        session.stubs().method("dataCommonsService").withNoArguments().will(returnValue(dataCommonsService.proxy()));
        session.stubs().method("dataEditorService").withNoArguments().will(returnValue(null));

        DatasetPropertiesEditorView viewProxy = (DatasetPropertiesEditorView) view.proxy();
        EmfSession sessionProxy = (EmfSession) session.proxy();
        presenter = new PropertiesEditorPresenterImpl(dataset, viewProxy, sessionProxy);
    }

    public void testShouldCloseViewAndReleaseLockOnNotifyClose() throws Exception {
        view.expects(once()).method("close");
        dataService.expects(once()).method("releaseLockedDataset").with(same(dataset)).will(returnValue(dataset));

        presenter.doClose();
    }

    public void testShouldDisplayViewOnDisplayAfterObtainingLock() throws Exception {
        User owner = new User();
        owner.setUsername("name");
        dataset.setLockOwner(owner.getUsername());
        dataset.setLockDate(new Date());

        dataService.expects(once()).method("obtainLockedDataset").with(same(owner), same(dataset)).will(
                returnValue(dataset));

        session.stubs().method("user").withNoArguments().will(returnValue(owner));

        DatasetPropertiesEditorView viewProxy = (DatasetPropertiesEditorView) view.proxy();
        presenter = new PropertiesEditorPresenterImpl(dataset, viewProxy, (EmfSession) session.proxy());

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(dataset));

        presenter.doDisplay();
    }

    public void testShouldRaiseErrorOnDisplayIfFailedToObtainLock() throws Exception {
        User owner = new User();
        owner.setUsername("owner");
        owner.setName("owner");
        dataset.setLockOwner(owner.getUsername());
        dataset.setLockDate(new Date());

        User user = new User();
        user.setUsername("user");

        dataService.expects(once()).method("obtainLockedDataset").with(same(user), same(dataset)).will(
                returnValue(dataset));

        session.stubs().method("user").withNoArguments().will(returnValue(user));

        DatasetPropertiesEditorView viewProxy = (DatasetPropertiesEditorView) view.proxy();
        presenter = new PropertiesEditorPresenterImpl(dataset, viewProxy, (EmfSession) session.proxy());

        view.expects(once()).method("notifyLockFailure").with(same(dataset));
        view.expects(once()).method("observe").with(same(presenter));
        presenter.doDisplay();
    }

    public void testShouldUpdateDatasetRefreshDatasetsBrowserAndCloseWindowOnSave() throws Exception {
        dataService.expects(once()).method("updateDataset").with(eq(dataset));
        view.expects(once()).method("close");

        EditableSummaryTabPresenter summaryTabProxy = summaryMockForSave();
        EditableKeywordsTabPresenter keywordsTabProxy = keywordsMockForSave();
        EditNotesTabPresenter notesTabProxy = notesMockForSave();

        presenter.save((DataService) dataService.proxy(), summaryTabProxy, keywordsTabProxy, notesTabProxy);
    }

    public void testShouldUpdateDatasetWithChangesFromTabsAndSaveDatasetOnUpdate() throws Exception {
        dataService.expects(once()).method("updateDataset").with(eq(dataset));

        EditableSummaryTabPresenter summaryTabProxy = summaryMockForSave();
        EditableKeywordsTabPresenter keywordsTabProxy = keywordsMockForSave();
        EditNotesTabPresenter notesTabProxy = notesMockForSave();

        presenter.updateDataset((DataService) dataService.proxy(), summaryTabProxy, keywordsTabProxy, notesTabProxy);
    }

    private EditNotesTabPresenter notesMockForSave() {
        Mock notesTab = mock(EditNotesTabPresenter.class);
        notesTab.expects(once()).method("doSave");
        return (EditNotesTabPresenter) notesTab.proxy();
    }

    private EditableKeywordsTabPresenter keywordsMockForSave() {
        Mock keywordsTab = mock(EditableKeywordsTabPresenter.class);
        keywordsTab.expects(once()).method("doSave");
        return (EditableKeywordsTabPresenter) keywordsTab.proxy();
    }

    private EditableSummaryTabPresenter summaryMockForSave() {
        Mock summaryTab = mock(EditableSummaryTabPresenter.class);
        summaryTab.expects(once()).method("doSave");
        return (EditableSummaryTabPresenter) summaryTab.proxy();
    }

    public void testShouldDisplayErrorMessageOnDatasetsBrowserIfGettingUpdatedDatasetsFailOnSave() throws Exception {
        dataService.expects(once()).method("updateDataset").with(eq(dataset));
        view.expects(once()).method("close");

        EditableSummaryTabPresenter summaryTabProxy = summaryMockForSave();
        EditableKeywordsTabPresenter keywordsTabProxy = keywordsMockForSave();
        EditNotesTabPresenter notesTabProxy = notesMockForSave();

        presenter.save((DataService) dataService.proxy(), summaryTabProxy, keywordsTabProxy, notesTabProxy);
    }

    public void testShouldDisplayNotesTabOnSetNotesTab() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(EditNotesTabView.class);
        view.expects(once()).method("display");

        Mock dataCommons = mock(DataCommonsService.class);
        dataCommons.stubs().method(ANYTHING);
        session.stubs().method("dataCommonsService").will(returnValue(dataCommons.proxy()));

        Mock dataEditor = mock(DataEditorService.class);
        dataEditor.stubs().method(ANYTHING);
        session.stubs().method("dataEditorService").will(returnValue(dataEditor.proxy()));
        session.stubs().method("user");

        PropertiesEditorPresenter presenter = new PropertiesEditorPresenterImpl(dataset, null, (EmfSession) session
                .proxy());

        presenter.set((EditNotesTabView) view.proxy());
    }
}
