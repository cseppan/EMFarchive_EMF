package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyTargetPollutant;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.HashMap;

public class TargetPollutantRowSource implements RowSource<ControlStrategyTargetPollutant> {

    private ControlStrategyTargetPollutant source;

    private HashMap<String, Pollutant> pollutants;

    public TargetPollutantRowSource(ControlStrategyTargetPollutant source, 
            Pollutant[] allPollutants) {
        this.source = source;
        this.pollutants = new HashMap<String, Pollutant>();
        
        for (Pollutant pol : allPollutants)
            pollutants.put(pol.getName(), pol);
    }

    public Object[] values() {
        Object[] values = { 
                source.getPollutant().getName(), 
                source.getMaxEmisReduction(),
                source.getMaxControlEfficiency(),
                source.getMinCostPerTon(),
                source.getMinAnnCost(),
                source.getReplacementControlMinEfficiencyDiff() == null ? new Double(10.0) : source.getReplacementControlMinEfficiencyDiff(),
                source.getInvFilter(),
                source.getCountyDataset(),
                source.getCountyDatasetVersion()
                };
        
        return values;
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            source.setPollutant(pollutants.get(val));
            break;
        case 1:
            source.setMaxEmisReduction((Double) val);
            break;
        case 2:
            source.setMaxControlEfficiency((Double) val);
            break;
        case 3:
            source.setMinCostPerTon((Double) val);
            break;
        case 4:
            source.setMinAnnCost((Double) val);
            break;
        case 5:
            source.setReplacementControlMinEfficiencyDiff((Double) val);
            break;
        case 6:
            source.setInvFilter((String) val);
            break;
        case 7:
            source.setCountyDataset((EmfDataset) val);
            break;
        case 8:
            source.setCountyDatasetVersion((Integer) val);
            break;
        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    public ControlStrategyTargetPollutant source() {
        return source;
    }

    public void validate(int rowNumber) throws EmfException {
        Pollutant pol = source.getPollutant();
        
        if (pol == null || pol.getName().trim().length() == 0) {
            throw new EmfException("On constraints tab, empty pollutant at row " + rowNumber);
        }
    }
}