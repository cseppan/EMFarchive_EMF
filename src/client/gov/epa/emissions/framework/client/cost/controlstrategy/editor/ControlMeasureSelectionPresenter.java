package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataEditorService;

public class ControlMeasureSelectionPresenter {

    private ControlMeasureTableData tableData;
    private EditControlStrategyMeasuresTab parentView;
    private LightControlMeasure[] controlMeasures;
    private EmfSession session;

    public ControlMeasureSelectionPresenter(EditControlStrategyMeasuresTab parentView, ControlMeasureSelectionView view, 
            EmfSession session, LightControlMeasure[] controlMeasures) {
        this.parentView = parentView;
        this.controlMeasures = controlMeasures;
        this.session = session;
    }

    public void display(ControlMeasureSelectionView view) throws Exception {
        view.observe(this);
        this.tableData = new ControlMeasureTableData(controlMeasures);
        view.display(tableData);

    }


    public void doAdd(LightControlMeasure[] cms, double rule, double rulePenetration, double ruleEffective) {
        parentView.add(cms, rule, rulePenetration, ruleEffective);
        
    }

    public EmfDataset[] getDatasets(DatasetType type) throws EmfException
    {
            if (type == null)
                return new EmfDataset[0];

            return dataService().getDatasets(type);
    }
    
    public DatasetType getDatasetType(String datasetType) throws EmfException
    {
            return dataCommonsService().getDatasetType(datasetType);
    }
    
    public Version[] getVersions(EmfDataset dataset) throws EmfException 
    {
        if (dataset == null) {
            return new Version[0];
        }
        return dataEditorService().getVersions(dataset.getId());
    }

    private DataService dataService() {
        return session.dataService();
    }
    
    private DataCommonsService dataCommonsService() {
        return session.dataCommonsService();
    }

    private DataEditorService dataEditorService() {
        return session.dataEditorService();
    }

}
