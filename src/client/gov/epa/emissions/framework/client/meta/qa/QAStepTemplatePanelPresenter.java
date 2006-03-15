package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.data.datasettype.QAStepTemplatesPanelView;

public class QAStepTemplatePanelPresenter {

    private QAStepTemplatesPanelView view;
    
    public QAStepTemplatePanelPresenter(QAStepTemplatesPanelView view) {
        this.view = view;
        view.observe(this);
    }
    
    public void doNewQAStepTemplate(NewQAStepTemplateView qaStepTemplateview, DatasetType type, int row) {
        qaStepTemplateview.display(type);
        if(qaStepTemplateview.shouldCreate())
            view.setTableData(qaStepTemplateview.template(), row);
    }

}
