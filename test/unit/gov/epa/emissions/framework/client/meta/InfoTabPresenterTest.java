package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.framework.client.meta.info.InfoTabPresenter;
import gov.epa.emissions.framework.client.meta.info.InfoTabView;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class InfoTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayInternalSourcesAndVersionsOnDisplay() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(1);
        dataset.addInternalSource(new InternalSource());

        DatasetType type = new DatasetType();
        dataset.setDatasetType(type);

        Mock view = mock(InfoTabView.class);
        view.expects(once()).method("displayInternalSources").with(eq(dataset.getInternalSources()));

        InfoTabPresenter presenter = new InfoTabPresenter((InfoTabView) view.proxy(), dataset);

        presenter.doDisplay();
    }

    public void testShouldDisplayExternalSourcesAndVersionsIfDatasetTypeIsExternalOnDisplay() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(1);
        dataset.addExternalSource(new ExternalSource());

        DatasetType type = new DatasetType();
        type.setExternal(true);
        dataset.setDatasetType(type);

        Mock view = mock(InfoTabView.class);
        view.expects(once()).method("displayExternalSources").with(eq(dataset.getExternalSources()));

        InfoTabPresenter presenter = new InfoTabPresenter((InfoTabView) view.proxy(), dataset);

        presenter.doDisplay();
    }

}
