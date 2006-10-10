package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
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
        view.clearMessage();
        view.refresh(session.controlMeasureService().getMeasures());
    }

    public void doEdit(EmfConsole parent, ControlMeasure measure, DesktopManager desktopManager) throws EmfException {
        
        ControlMeasureView editor = new EditControlMeasureWindow(parent, session, desktopManager);
        ControlMeasurePresenter presenter = new EditorControlMeasurePresenterImpl(
                measure, editor, session, this);
        presenter.doDisplay();
    }

    public void doCreateNew(EmfConsole parent, ControlMeasure measure, DesktopManager desktopManager) throws EmfException {
        ControlMeasureView window = new NewControlMeasureWindow(parent, session, desktopManager);
        ControlMeasurePresenter presenter = new NewControlMeasurePresenterImpl(
                measure, window, session, this);
        presenter.doDisplay();
    }
    
    public ControlMeasure[] doFilterEfficiencyAndCost(ControlMeasure[] measures) {
        //List filteredMeasures = new ArrayList();
        
        for(int i = 0; i < measures.length; i++) {
           // me
        }
        
        return null;
    }

}
