package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class ControlMeasuresManagerPresenter implements RefreshObserver {

    private ControlMeasuresManagerView view;

    private EmfSession session;

    public ControlMeasuresManagerPresenter(EmfSession session) {
        this.session = session;
    }

    public void doDisplay(ControlMeasuresManagerView view) {
        this.view = view;
        view.observe(this);

        view.display();
    }

    public void doClose() {
        view.close();
    }

    public void doRefresh() throws EmfException {
        view.refresh(session.costService().getMeasures());
        view.clearMessage();
    }

}
