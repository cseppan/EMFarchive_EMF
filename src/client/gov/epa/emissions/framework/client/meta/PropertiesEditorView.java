package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfDataset;

public interface PropertiesEditorView extends ManagedView {

    void observe(PropertiesEditorPresenter presenter);

    void display(EmfDataset dataset);

    void showError(String message);

    boolean shouldContinueLosingUnsavedChanges();

    void notifyLockFailure(EmfDataset dataset);

}
