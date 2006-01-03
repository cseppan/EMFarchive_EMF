package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;

public interface ViewableDatasetTypePresenter {

    public abstract void doDisplay() throws EmfException;

    public abstract void doClose();

}