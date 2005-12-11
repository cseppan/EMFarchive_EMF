package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DataTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayInternalSourcesAndVersionsOnDisplay() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetid(1);
        dataset.addInternalSource(new InternalSource());

        DatasetType type = new DatasetType();
        dataset.setDatasetType(type);

        Mock view = mock(DataTabView.class);
        view.expects(once()).method("displayInternalSources").with(eq(dataset.getInternalSources()));

        Mock editorService = mock(DataEditorService.class);
        Version[] versions = new Version[0];
        editorService.stubs().method("getVersions").with(eq(new Long(1))).will(returnValue(versions));
        view.expects(once()).method("displayVersions").with(same(versions), eq(dataset.getInternalSources()));

        DataTabPresenter presenter = new DataTabPresenter((DataTabView) view.proxy(), dataset,
                (DataEditorService) editorService.proxy());

        presenter.doDisplay();
    }

    public void testShouldDisplayExternalSourcesAndVersionsIfDatasetTypeIsExternalOnDisplay() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetid(1);
        dataset.addExternalSource(new ExternalSource());

        DatasetType type = new DatasetType();
        type.setExternal(true);
        dataset.setDatasetType(type);

        Mock view = mock(DataTabView.class);
        view.expects(once()).method("displayExternalSources").with(eq(dataset.getExternalSources()));

        Mock editorService = mock(DataEditorService.class);
        Version[] versions = new Version[0];
        editorService.stubs().method("getVersions").with(eq(new Long(1))).will(returnValue(versions));
        view.expects(once()).method("displayVersions").with(same(versions), eq(dataset.getInternalSources()));

        DataTabPresenter presenter = new DataTabPresenter((DataTabView) view.proxy(), dataset,
                (DataEditorService) editorService.proxy());

        presenter.doDisplay();
    }

    public void testShouldDoNothingOnSave() {
        DataTabPresenter presenter = new DataTabPresenter(null, null, null);
        presenter.doSave();
    }

}
