package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.client.meta.QA.QAStepTemplatePanelPresenter;

public interface QAStepTemplatesPanelView {
    void setTableData(QAStepTemplate template, int row);

    void observe(QAStepTemplatePanelPresenter presenter);
}
