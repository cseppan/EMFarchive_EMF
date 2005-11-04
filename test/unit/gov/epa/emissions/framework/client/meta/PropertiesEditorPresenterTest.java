package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.DatasetsBrowserView;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.InterDataServices;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.stub.ThrowStub;

public class PropertiesEditorPresenterTest extends MockObjectTestCase {

    private Mock view;

    private PropertiesEditorPresenter presenter;

    private EmfDataset dataset;

    private Mock dataServices;

    private Mock interdataServices;

    protected void setUp() {
        dataset = new EmfDataset();
        dataset.setName("test");

        view = mock(PropertiesEditorView.class);

        dataServices = mock(DataServices.class);
        interdataServices = mock(InterDataServices.class);
        interdataServices.stubs().method("getKeywords").withNoArguments().will(returnValue(new Keyword[0]));

        Mock locator = mock(ServiceLocator.class);
        locator.stubs().method("getDataServices").withNoArguments().will(returnValue(dataServices.proxy()));
        locator.stubs().method("getInterDataServices").withNoArguments().will(returnValue(interdataServices.proxy()));

        presenter = new PropertiesEditorPresenter(dataset, (ServiceLocator) locator.proxy());

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(dataset));

        presenter.display((PropertiesEditorView) view.proxy());
    }

    public void testShouldCloseViewOnNotifyClose() {
        view.expects(once()).method("close");

        presenter.doClose();
    }

    public void testShouldContinueToCloseIfUserOkaysLosingUnsavedChanges() {
        view.expects(once()).method("close");

        presenter.onChange();
        view.expects(once()).method("shouldContinueLosingUnsavedChanges").withNoArguments().will(
                returnValue(Boolean.TRUE));

        presenter.doClose();
    }

    public void testShouldNotCloseIfUserSelectsToNotCloseOnUnsavedChanges() {
        presenter.onChange();
        view.expects(once()).method("shouldContinueLosingUnsavedChanges").withNoArguments().will(
                returnValue(Boolean.FALSE));

        presenter.doClose();
    }

    public void testShouldAddAsOnChangeListenerToSummaryTabOnAddingSummaryTab() {
        Mock summaryView = mock(SummaryTabView.class);
        summaryView.expects(once()).method("observeChanges").with(eq(presenter));

        presenter.set((SummaryTabView) summaryView.proxy());
    }

    public void testShouldUpdateDatasetRefreshDatasetsBrowserAndCloseWindowOnSave() throws Exception {
        dataServices.expects(once()).method("updateDataset").with(eq(dataset));

        Mock summaryView = mock(SummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        summaryView.expects(once()).method("observeChanges").with(eq(presenter));

        Mock keywordsView = mock(KeywordsTabView.class);
        keywordsView.expects(once()).method("display");
        keywordsView.expects(once()).method("updates").withNoArguments().will(returnValue(new KeyVal[]{}));

        presenter.set((SummaryTabView) summaryView.proxy());
        presenter.set((KeywordsTabView) keywordsView.proxy());

        Mock datasetsBrowser = mock(DatasetsBrowserView.class);
        EmfDataset[] datasets = new EmfDataset[0];
        dataServices.stubs().method("getDatasets").will(returnValue(datasets));
        datasetsBrowser.expects(once()).method("refresh").with(eq(datasets));
        view.expects(once()).method("close");

        presenter.doSave((DatasetsBrowserView) datasetsBrowser.proxy());
    }

    public void testShouldSaveWithoutPromptingOnSaveIfChangesHaveOccuredInSummaryTab() throws EmfException {
        dataServices.expects(once()).method("updateDataset").with(eq(dataset));

        Mock summaryView = mock(SummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        summaryView.expects(atLeastOnce()).method("observeChanges").with(eq(presenter));

        Mock keywordsView = mock(KeywordsTabView.class);
        keywordsView.expects(once()).method("updates").withNoArguments().will(returnValue(new KeyVal[]{}));
        keywordsView.expects(atLeastOnce()).method("display");

        presenter.set((SummaryTabView) summaryView.proxy());
        presenter.set((KeywordsTabView) keywordsView.proxy());

        Mock datasetsBrowser = mock(DatasetsBrowserView.class);
        EmfDataset[] datasets = new EmfDataset[0];
        dataServices.stubs().method("getDatasets").will(returnValue(datasets));
        datasetsBrowser.expects(once()).method("refresh").with(eq(datasets));
        view.expects(once()).method("close");

        presenter.set((SummaryTabView) summaryView.proxy());
        presenter.onChange();

        presenter.doSave((DatasetsBrowserView) datasetsBrowser.proxy());
    }

    public void testShouldUpdateDatasetWithChangesFromTabsAndSaveDatasetOnUpdate() throws Exception {
        dataServices.expects(once()).method("updateDataset").with(eq(dataset));

        Mock summaryTab = mock(SummaryTabPresenterStub.class);
        summaryTab.expects(once()).method("doSave");

        Mock keywordsTab = mock(KeywordsTabPresenterStub.class);
        keywordsTab.expects(once()).method("doSave");

        presenter.updateDataset((DataServices) dataServices.proxy(), (SummaryTabPresenter) summaryTab.proxy(),
                (KeywordsTabPresenter) keywordsTab.proxy());
    }

    public void testShouldDisplayErrorMessageOnDatasetsBrowserIfGettingUpdatedDatasetsFailOnSave() throws EmfException {
        dataServices.expects(once()).method("updateDataset").with(eq(dataset));

        Mock summaryView = mock(SummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        summaryView.expects(once()).method("observeChanges").with(eq(presenter));

        Mock keywordsView = mock(KeywordsTabView.class);
        keywordsView.expects(once()).method("display");
        keywordsView.expects(once()).method("updates").withNoArguments().will(returnValue(new KeyVal[]{}));

        presenter.set((SummaryTabView) summaryView.proxy());
        presenter.set((KeywordsTabView) keywordsView.proxy());

        Mock datasetsBrowser = mock(DatasetsBrowserView.class);
        dataServices.stubs().method("getDatasets").will(throwException(new EmfException("failure")));
        datasetsBrowser.expects(once()).method("showError").with(
                eq("Could not refresh Datasets, after updating " + dataset.getName()));

        presenter.doSave((DatasetsBrowserView) datasetsBrowser.proxy());
    }

    public void testShouldDisplayErrorMessageOnErrorDuringSave() throws EmfException {
        Mock summaryView = mock(SummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        summaryView.expects(once()).method("observeChanges").with(eq(presenter));

        dataServices.expects(once()).method("updateDataset").with(eq(dataset)).will(
                new ThrowStub(new EmfException("Reason")));
        view.expects(once()).method("showError").with(eq("Could not update dataset - " + dataset.getName() + ".Reason"));

        Mock keywordsView = mock(KeywordsTabView.class);
        keywordsView.expects(once()).method("display");
        keywordsView.expects(once()).method("updates").withNoArguments().will(returnValue(new KeyVal[]{}));

        presenter.set((SummaryTabView) summaryView.proxy());
        presenter.set((KeywordsTabView) keywordsView.proxy());

        presenter.doSave(null);
    }

}
