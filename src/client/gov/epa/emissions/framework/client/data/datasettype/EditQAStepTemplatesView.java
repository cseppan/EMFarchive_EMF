package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.client.meta.qa.EditQAStepTemplatesPresenter;

public interface EditQAStepTemplatesView {
    void setTableData(QAStepTemplate template, int row);

    void observe(EditQAStepTemplatesPresenter presenter);
    
    void add(QAStepTemplate template);
}
