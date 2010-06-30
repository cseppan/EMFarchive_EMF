package gov.epa.emissions.framework.client.fast.run.tabs.inputs;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.fast.FastRunInput;

public interface FastRunInputView extends ManagedView {

    void observe(FastRunInputPresenter presenter);

    void display(FastRunInput input);

    void refresh(FastRunInput input);

    void notifyLockFailure(FastRunInput input);

    void signalChanges();

    void showError(String message);

    void showMessage(String message);

    void clearMessage();
}
