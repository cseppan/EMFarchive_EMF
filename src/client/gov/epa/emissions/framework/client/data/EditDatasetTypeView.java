package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.client.ManagedView;

public interface EditDatasetTypeView extends ManagedView {

    void observe(EditDatasetTypePresenter presenter);

    void display(DatasetType type, Keyword[] masterKeywords);

    void close();

}
