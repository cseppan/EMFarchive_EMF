package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.data.datasettype.EditQAStepTemplatesView;

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

    public void doNew(NewQAStepTemplateView qaStepTemplateview, DatasetType type, int row) {
        qaStepTemplateview.display(type);
        if (qaStepTemplateview.shouldCreate())
            view.setTableData(qaStepTemplateview.template(), row);
    }

    public void doAdd(NewQAStepTemplateView dialog) {
        dialog.display(type);
        if (dialog.shouldCreate())
            view.add(dialog.template());
    }

}
