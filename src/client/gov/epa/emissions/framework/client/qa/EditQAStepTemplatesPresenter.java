package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.services.EmfException;

public class EditQAStepTemplatesPresenter {

    private EditQAStepTemplatesView view;

    private DatasetType type;

    public EditQAStepTemplatesPresenter(DatasetType type, EditQAStepTemplatesView view) {
        this.type = type;
        this.view = view;
    }

    public void display() {
        view.observe(this);
    }

    public void doAdd(NewQAStepTemplateView dialog) {
        dialog.display(type);
        if (dialog.shouldCreate())
            view.add(dialog.template());
    }

    public void doEdit(EditQAStepTemplateView view, QAStepTemplate template) {
        view.observe(this);
        view.display(type);
        view.display(template);
    }

    public void doEdit_WRONG_PRESENTER(EditQAStepTemplateView view) throws EmfException {
        view.loadTemplate();
        this.view.refresh();
    }

}
