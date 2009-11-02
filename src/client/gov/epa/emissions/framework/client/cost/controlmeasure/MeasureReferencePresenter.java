package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public class MeasureReferencePresenter {

    protected ControlMeasureReferencesTab parentView;

    protected MeasureReferenceWindow view;

    public MeasureReferencePresenter(ControlMeasureReferencesTab parentView, MeasureReferenceWindow view) {

        this.parentView = parentView;
        this.view = view;
    }

    // use this method when adding a new reference
    public void display(ControlMeasure measure) {

        view.observe(this);
        view.display(measure);
    }

    // use this method when editing a existing reference
    public void display(ControlMeasure measure, Reference reference) {

        view.observe(this);
        view.display(measure, reference);
    }

    public void refresh() {
        parentView.refresh();
    }

    public void add(Reference reference) {
        parentView.add(reference);
    }

    public void doSave(ControlMeasure measure) {
        parentView.save(measure);
    }
}