package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.DatasetsBrowserView;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.stub.ThrowStub;

public class PropertiesEditorPresenterTest extends MockObjectTestCase {

    private Mock view;

    private PropertiesEditorPresenter presenter;

    private EmfDataset dataset;

    private Mock dataServices;

    protected void setUp() {
        dataset = new EmfDataset();
        dataset.setName("test");

        view = mock(PropertiesEditorView.class);

        dataServices = mock(DataServices.class);
        presenter = new PropertiesEditorPresenter(dataset, (DataServices) dataServices.proxy());

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
        
        presenter.add((SummaryTabView) summaryView.proxy());
    }

    public void testShouldUpdateDatasetRefreshDatasetsBrowserAndCloseWindowOnSave() {
        dataServices.expects(once()).method("updateDataset").with(eq(dataset));

        Mock summaryView = mock(SummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        summaryView.expects(once()).method("observeChanges").with(eq(presenter));
        presenter.add((SummaryTabView) summaryView.proxy());

        Mock datasetsBrowser = mock(DatasetsBrowserView.class);
        EmfDataset[] datasets = new EmfDataset[0];
        dataServices.stubs().method("getDatasets").will(returnValue(datasets));
        datasetsBrowser.expects(once()).method("refresh").with(eq(datasets));
        view.expects(once()).method("close");

        presenter.doSave((DatasetsBrowserView) datasetsBrowser.proxy());
    }
    
    public void testShouldSaveWithoutPromptingOnSaveIfChangesHaveOccuredInSummaryTab() {
        dataServices.expects(once()).method("updateDataset").with(eq(dataset));
        
        Mock summaryView = mock(SummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        summaryView.expects(atLeastOnce()).method("observeChanges").with(eq(presenter));
        presenter.add((SummaryTabView) summaryView.proxy());
        
        Mock datasetsBrowser = mock(DatasetsBrowserView.class);
        EmfDataset[] datasets = new EmfDataset[0];
        dataServices.stubs().method("getDatasets").will(returnValue(datasets));
        datasetsBrowser.expects(once()).method("refresh").with(eq(datasets));
        view.expects(once()).method("close");
        
        presenter.add((SummaryTabView) summaryView.proxy());
        presenter.onChange();
        
        presenter.doSave((DatasetsBrowserView) datasetsBrowser.proxy());
    }

    public void testShouldDisplayErrorMessageOnDatasetsBrowserIfGettingUpdatedDatasetsFailOnSave() {
        dataServices.expects(once()).method("updateDataset").with(eq(dataset));

        Mock summaryView = mock(SummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        summaryView.expects(once()).method("observeChanges").with(eq(presenter));
        presenter.add((SummaryTabView) summaryView.proxy());

        Mock datasetsBrowser = mock(DatasetsBrowserView.class);
        dataServices.stubs().method("getDatasets").will(throwException(new EmfException("failure")));
        datasetsBrowser.expects(once()).method("showError").with(
                eq("Could not refresh Datasets, after updating " + dataset.getName()));

        presenter.doSave((DatasetsBrowserView) datasetsBrowser.proxy());
    }

    public void testShouldDisplayErrorMessageOnErrorDuringSave() {
        Mock summaryView = mock(SummaryTabView.class);
        summaryView.expects(once()).method("updateDataset").with(eq(dataset));
        summaryView.expects(once()).method("observeChanges").with(eq(presenter));

        dataServices.expects(once()).method("updateDataset").with(eq(dataset)).will(
                new ThrowStub(new EmfException("Could not save")));
        view.expects(once()).method("showError").with(eq("Could not update dataset - " + dataset.getName()));

        presenter.add((SummaryTabView) summaryView.proxy());
        presenter.doSave(null);
    }
}
