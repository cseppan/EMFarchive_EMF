package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
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
        view.disposeView();
    }

    public void doRefresh() throws EmfException {
        view.refresh(session.costService().getMeasures());
        view.clearMessage();
    }

    public void doDisplayCMEditor(ControlMeasure measure, String newOrEdit, DesktopManager desktopManager) throws EmfException {
        ControlMeasureView editor = new ControlMeasureEditor(session,desktopManager);
        ControlMeasurePresenter presenter = new EditorControlMeasurePresenterImpl(
                measure, editor, session, this);
        presenter.doDisplay(newOrEdit);
    }
    
    public void doEdit(ControlMeasure measure, DesktopManager desktopManager) throws EmfException {
        ControlMeasureView editor = new ControlMeasureEditor(session, desktopManager);
        ControlMeasurePresenter presenter = new EditorControlMeasurePresenterImpl(
                measure, editor, session, this);
        presenter.doDisplay();
    }

}
