package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.client.ManagedView;

public interface EditableDatasetTypeView extends ManagedView {

    void observe(EditableDatasetTypePresenter presenter);

    void display(DatasetType type, Keyword[] masterKeywords);

    void close();

}
