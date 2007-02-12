package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public class ControlMeasureSelectionPresenter {

    private ControlMeasureTableData tableData;
    private EmfSession session;
    private EditControlStrategyMeasuresTab parentView;

    public ControlMeasureSelectionPresenter(EditControlStrategyMeasuresTab parentView, ControlMeasureSelectionView view, 
            EmfSession session) {
        this.parentView = parentView;
        this.session = session;
    }

    public void display(ControlMeasureSelectionView view) throws Exception {
        ControlMeasure[] cms = {};
        view.observe(this);
        this.tableData = new ControlMeasureTableData(cms);
        view.display(tableData);

    }

    public ControlMeasure[] getControlMeasures() throws EmfException {
        return session.controlMeasureService().getMeasures();
    }

    public void doAdd(ControlMeasure[] cms) {
        parentView.add(cms);
    }

}
