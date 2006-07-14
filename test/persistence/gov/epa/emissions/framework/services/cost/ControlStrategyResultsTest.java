package gov.epa.emissions.framework.services.cost;

import java.util.List;

import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class ControlStrategyResultsTest extends ServiceTestCase {

    private ControlStrategyDAO controlStrategydao;
    
    private DatasetDAO datasetDAO;

    protected void doSetUp() throws Exception {
        controlStrategydao = new ControlStrategyDAO();
        datasetDAO = new DatasetDAO();

    }

    protected void doTearDown() throws Exception {
        // NOTE Auto-generated method stub

    }
    
    //FIXME: save dataset indirrectly when saving control strategy
    public void testShouldSaveControlStrategyWithResultsSummary(){
        ControlStrategy element = new ControlStrategy("test" + Math.random());

        StrategyResult result = new StrategyResult();
        EmfDataset dataset = dataset();
        result.setDetailedResultDataset(dataset);
        
        element.setStrategyResults(new StrategyResult[]{result});
        
        try{
            controlStrategydao.add(element, session);
            
        List list = controlStrategydao.all(session);
        ControlStrategy retreivedCS = (ControlStrategy) list.get(0);
        assertEquals(element,retreivedCS);
        
        }finally{
            controlStrategydao.remove(element,session);
            datasetDAO.remove(dataset,session);
        }
    }

    private EmfDataset dataset() {
        EmfDataset dataset=  new EmfDataset();
        dataset.setName("test_dataset"+ Math.random());
        dataset.setCreator("emf");
        datasetDAO.add(dataset,session);
        return dataset;
    }

}
