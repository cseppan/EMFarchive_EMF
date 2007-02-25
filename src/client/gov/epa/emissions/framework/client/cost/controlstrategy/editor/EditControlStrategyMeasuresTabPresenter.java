package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategiesManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class EditControlStrategyMeasuresTabPresenter  implements EditControlStrategyTabPresenter {
    private ControlStrategyMeasuresTabView view;
    
    private EmfSession session;
    
    private ControlStrategy strategy;

    private ControlStrategiesManagerPresenter controlStrategiesManagerPresenter;

    public EditControlStrategyMeasuresTabPresenter(ControlStrategyMeasuresTabView view, 
            ControlStrategy strategy, EmfSession session, 
            ControlStrategiesManagerPresenter controlStrategiesManagerPresenter) {
        this.strategy = strategy;
        this.session = session;
        this.view = view;
        this.controlStrategiesManagerPresenter = controlStrategiesManagerPresenter;
    }
    
    public void doDisplay() throws EmfException  {
        view.observe(this);
        view.display(this.strategy);
    }

    public ControlMeasureClass[] getAllClasses() throws EmfException {
        return session.controlMeasureService().getMeasureClasses();
    }

    public ControlMeasureClass[] getControlMeasureClasses() {
        return strategy.getControlMeasureClasses();
    }

    public LightControlMeasure[] getControlMeasures() {
        return strategy.getControlMeasures();
    }

    public LightControlMeasure[] getAllControlMeasures() {
        return controlStrategiesManagerPresenter.getControlMeasures();
    }

    public void doRefresh(ControlStrategyResult result) {
        // NOTE Auto-generated method stub
        
    }

    public void doSave() throws EmfException {
        view.save(strategy);
    }
}
