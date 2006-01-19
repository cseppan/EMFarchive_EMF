package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabView;
import gov.epa.emissions.framework.client.meta.keywords.KeywordsTabPresenterStub;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenter;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabView;
import gov.epa.emissions.framework.client.meta.summary.SummaryTabPresenterStub;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.stub.ThrowStub;

public class PropertiesEditorPresenterTest extends MockObjectTestCase {

    private Mock view;

    private PropertiesEditorPresenterImpl presenter;

    private EmfDataset dataset;

    private Mock dataService;

    private Mock dataCommonsService;

    private Mock locator;

    protected void setUp() {
        dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        view = mock(DatasetPropertiesEditorView.class);

        dataService = mock(DataService.class);
        dataCommonsService = mock(DataCommonsService.class);
        dataCommonsService.stubs().method("getKeywords").withNoArguments().will(returnValue(new Keyword[0]));

        locator = mock(ServiceLocator.class);
        locator.stubs().method("dataService").withNoArguments().will(returnValue(dataService.proxy()));
        locator.stubs().method("dataCommonsService").withNoArguments().will(returnValue(dataCommonsService.proxy()));
        locator.stubs().method("dataEditorService").withNoArguments().will(returnValue(null));

        presenter = new PropertiesEditorPresenterImpl(dataset, (ServiceLocator) locator.proxy(), null);
    }

    public void testShouldCloseViewAndReleaseLockOnNotifyClose() throws Exception {
        displayPresenter();
        view.expects(once()).method("close");
        dataService.expects(once()).method("releaseLockedDataset").with(same(dataset)).will(returnValue(dataset));

        presenter.doClose();
    }

    private void displayPresenter() throws Exception {
        User owner = new User();
        owner.setUsername("name");
        dataset.setLockOwner(owner.getUsername());
        dataset.setLockDate(new Date());

        dataService.expects(once()).method("obtainLockedDataset").with(same(owner), same(dataset)).will(
                returnValue(dataset));

        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(owner));

        presenter = new PropertiesEditorPresenterImpl(dataset, (ServiceLocator) locator.proxy(), (EmfSession) session
                .proxy());

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(dataset));

        presenter.doDisplay((DatasetPropertiesEditorView) view.proxy());
    }

    public void testShouldDisplayViewOnDisplayAfterObtainingLock() throws Exception {
        User owner = new User();
        owner.setUsername("name");
        dataset.setLockOwner(owner.getUsername());
        dataset.setLockDate(new Date());

        dataService.expects(once()).method("obtainLockedDataset").with(same(owner), same(dataset)).will(
                returnValue(dataset));

        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(owner));

        presenter = new PropertiesEditorPresenterImpl(dataset, (ServiceLocator) locator.proxy(), (EmfSession) session
                .proxy());

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(dataset));

        presenter.doDisplay((DatasetPropertiesEditorView) view.proxy());
    }

    public void testShouldRaiseErrorOnDisplayIfFailedToObtainLock() throws Exception {
        User owner = new User();
        owner.setUsername("owner");
        owner.setFullName("owner");
        dataset.setLockOwner(owner.getUsername());
        dataset.setLockDate(new Date());

        User user = new User();
        user.setUsername("user");

        dataService.expects(once()).method("obtainLockedDataset").with(same(user), same(dataset)).will(
                returnValue(dataset));

        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(user));

        presenter = new PropertiesEditorPresenterImpl(dataset, (ServiceLocator) locator.proxy(), (EmfSession) session
                .proxy());
        view.expects(once()).method("notifyLockFailure").with(same(dataset));
        view.expects(once()).method("observe").with(same(presenter));

        presenter.doDisplay((DatasetPropertiesEditorView) view.proxy());
    }

    public void testShouldContinueToCloseIfUserOkaysLosingUnsavedChanges() throws Exception {
        displayPresenter();
        view.expects(once()).method("close");

        presenter.onChange();
        view.expects(once()).method("shouldContinueLosingUnsavedChanges").withNoArguments().will(
                returnValue(Boolean.TRUE));
        dataService.expects(once()).method("releaseLockedDataset").with(same(dataset)).will(returnValue(dataset));

        presenter.doClose();
    }

    public void testShouldNotCloseIfUserSelectsToNotCloseOnUnsavedChanges() throws Exception {
        displayPresenter();
        presenter.onChange();
        view.expects(once()).method("shouldContinueLosingUnsavedChanges").withNoArguments().will(
                returnValue(Boolean.FALSE));

        presenter.doClose();
    }

    public void testShouldAddAsOnChangeListenerToSummaryTabOnAddingSummaryTab() {
        Mock summaryView = mock(EditableSummaryTabView.class);
        summaryView.expects(once()).method("observeChanges").with(eq(presenter));

        presenter.set((EditableSummaryTabView) summaryView.proxy());
    }

    public void testShouldUpdateDatasetRefreshDatasetsBrowserAndCloseWindowOnSave() throws Exception {
        displayPresenter();
        dataService.expects(once()).method("updateDataset").with(eq(dataset));

        Mock summaryView = mock(EditableSummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        summaryView.expects(once()).method("observeChanges").with(eq(presenter));

        Mock keywordsView = mock(EditableKeywordsTabView.class);
        keywordsView.expects(once()).method("display");
        keywordsView.expects(once()).method("updates").withNoArguments().will(returnValue(new KeyVal[] {}));

        presenter.set((EditableSummaryTabView) summaryView.proxy());
        presenter.set((EditableKeywordsTabView) keywordsView.proxy());

        EmfDataset[] datasets = new EmfDataset[0];
        dataService.stubs().method("getDatasets").will(returnValue(datasets));
        view.expects(once()).method("close");

        presenter.doSave();
    }

    public void testShouldSaveWithoutPromptingOnSaveIfChangesHaveOccuredInSummaryTab() throws Exception {
        displayPresenter();
        dataService.expects(once()).method("updateDataset").with(eq(dataset));

        Mock summaryView = mock(EditableSummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        summaryView.expects(atLeastOnce()).method("observeChanges").with(eq(presenter));

        Mock keywordsView = mock(EditableKeywordsTabView.class);
        keywordsView.expects(once()).method("updates").withNoArguments().will(returnValue(new KeyVal[] {}));
        keywordsView.expects(atLeastOnce()).method("display");

        presenter.set((EditableSummaryTabView) summaryView.proxy());
        presenter.set((EditableKeywordsTabView) keywordsView.proxy());

        EmfDataset[] datasets = new EmfDataset[0];
        dataService.stubs().method("getDatasets").will(returnValue(datasets));
        view.expects(once()).method("close");

        presenter.set((EditableSummaryTabView) summaryView.proxy());
        presenter.onChange();

        presenter.doSave();
    }

    public void testShouldUpdateDatasetWithChangesFromTabsAndSaveDatasetOnUpdate() throws Exception {
        dataService.expects(once()).method("updateDataset").with(eq(dataset));

        Mock summaryTab = mock(SummaryTabPresenterStub.class);
        summaryTab.expects(once()).method("doSave");

        Mock keywordsTab = mock(KeywordsTabPresenterStub.class);
        keywordsTab.expects(once()).method("doSave");

        presenter.updateDataset((DataService) dataService.proxy(), (EditableSummaryTabPresenter) summaryTab.proxy(),
                (EditableKeywordsTabPresenter) keywordsTab.proxy());
    }

    public void testShouldDisplayErrorMessageOnDatasetsBrowserIfGettingUpdatedDatasetsFailOnSave() throws Exception {
        displayPresenter();

        dataService.expects(once()).method("updateDataset").with(eq(dataset));

        Mock summaryView = mock(EditableSummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        summaryView.expects(once()).method("observeChanges").with(eq(presenter));

        Mock keywordsView = mock(EditableKeywordsTabView.class);
        keywordsView.expects(once()).method("display");
        keywordsView.expects(once()).method("updates").withNoArguments().will(returnValue(new KeyVal[] {}));

        presenter.set((EditableSummaryTabView) summaryView.proxy());
        presenter.set((EditableKeywordsTabView) keywordsView.proxy());

        view.expects(once()).method("close");
        presenter.doSave();
    }

    public void testShouldDisplayErrorMessageOnErrorDuringSave() throws Exception {
        displayPresenter();
        Mock summaryView = mock(EditableSummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        summaryView.expects(once()).method("observeChanges").with(eq(presenter));

        dataService.expects(once()).method("updateDataset").with(eq(dataset)).will(
                new ThrowStub(new EmfException("update failure")));
        view.expects(once()).method("showError").with(
                eq("Could not update dataset - " + dataset.getName() + ". Reason: update failure"));

        Mock keywordsView = mock(EditableKeywordsTabView.class);
        keywordsView.expects(once()).method("display");
        keywordsView.expects(once()).method("updates").withNoArguments().will(returnValue(new KeyVal[] {}));

        presenter.set((EditableSummaryTabView) summaryView.proxy());
        presenter.set((EditableKeywordsTabView) keywordsView.proxy());

        presenter.doSave();
    }

}
