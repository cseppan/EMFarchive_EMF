package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;

public interface DatasetsBrowserView {

    void observe(DatasetsBrowserPresenter presenter);

    void showExport(EmfDataset[] datasets, ExportPresenter exportPresenter) throws EmfException;

    void refresh(EmfDataset[] datasets);

    void close();
}
