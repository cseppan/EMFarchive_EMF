package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public interface ControlMeasuresManagerView extends ManagedView {

    void observe(ControlMeasuresManagerPresenter presenter);

    void refresh(ControlMeasure[] measures);

    void showMessage(String message);

    void showError(String message);

    void clearMessage();

}
