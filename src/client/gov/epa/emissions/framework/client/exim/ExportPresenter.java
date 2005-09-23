package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;

public interface ExportPresenter {

    public abstract void notifyDone();

    public abstract void display(ExportView view);

    // FIXME: have two separate, explicit methods for overwrite/no overwrite
    public abstract void notifyExport(EmfDataset[] datasets, String folder, String purpose) throws EmfException;

    public abstract void notifyExportWithoutOverwrite(EmfDataset[] datasets, String folder, String purpose)
            throws EmfException;

}