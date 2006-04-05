package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;

public class QAStepTemplatesPanelPresenter {

    private QAStepTemplatesPanelView view;

    private DatasetType type;

    public QAStepTemplatesPanelPresenter(DatasetType type, QAStepTemplatesPanelView view) {
        this.type = type;
        this.view = view;
    }

    public void display() {
        view.observe(this);
    }

    public void doEdit(EditQAStepTemplateView view, QAStepTemplate template) {
        EditQAStepTemplatesPresenter presenter = new EditQAStepTemplatesPresenterImpl(view, this.view);
        presenter.display(type, template);
    }

}
