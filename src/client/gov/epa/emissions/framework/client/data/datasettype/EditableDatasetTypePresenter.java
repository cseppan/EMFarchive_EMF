package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;

public interface EditableDatasetTypePresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave(String name, String description, Keyword[] keywords)
            throws EmfException;

}