package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;

public interface ExportPresenter {

    public abstract void notifyDone();

    public abstract void display(ExportView view);

    public abstract void doExport(EmfDataset[] datasets, String folder, String purpose) throws EmfException;

    public abstract void doExportWithoutOverwrite(EmfDataset[] datasets, String folder, String purpose)
            throws EmfException;

}