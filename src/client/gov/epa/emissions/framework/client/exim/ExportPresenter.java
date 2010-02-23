package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface ExportPresenter {

    void notifyDone();

    void display(ExportView view);

    void doExportWithOverwrite(EmfDataset[] datasets, String folder, String purpose) throws EmfException;

    void doExport(EmfDataset[] datasets, String folder, String purpose) throws EmfException;

    void setLastFolder( String lastfolder);
}