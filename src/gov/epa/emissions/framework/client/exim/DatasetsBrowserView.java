package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;

public interface DatasetsBrowserView {

    void setObserver(DatasetsBrowserPresenter presenter);

    void showExport(EmfDataset dataset, ExportPresenter exportPresenter) throws EmfException;

    void close();
}
