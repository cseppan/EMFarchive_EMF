package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.services.EmfDataset;

public interface PropertiesEditorView extends EmfView {

    void observe(PropertiesEditorPresenter presenter);

    void display(EmfDataset dataset);

    void showError(String message);

}
