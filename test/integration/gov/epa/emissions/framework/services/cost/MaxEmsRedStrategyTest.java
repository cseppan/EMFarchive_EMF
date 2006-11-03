package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.analysis.maxreduction.MaxEmsRedStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

import java.util.ArrayList;
import java.util.List;

public class MaxEmsRedStrategyTest extends MaxEmsRedStrategyTestCase {

    public void testShouldRunMaxEmsRedStrategyWithOneControlMeasure() throws Exception {
        ControlStrategy strategy = null;
        try {
            EfficiencyRecord[] records = { record(pm10Pollutant(), "", 90, 900, 1989) };
            addControlMeasure("Control Measure 1", "CM1", sccs(), records);
            strategy = controlStrategy(inputDataset, "CS1", pm10Pollutant());
            User user = emfUser();
            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServer(),
                    new Integer(500), sessionFactory());
            maxEmfEmsRedStrategy.run();
            assertEquals("No of rows in the detail result table is 15", 15,
                    countRecords(detailResultDatasetTableName(strategy)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (strategy != null)
                dropTable(detailResultDatasetTableName(strategy), dbServer().getEmissionsDatasource());
            removeData();

        }

    }

    private void removeData() {
        dropAll(Scc.class);
        dropAll(ControlMeasure.class);
        dropAll(ControlStrategyResult.class);
        dropAll(ControlStrategy.class);
        dropAll(QAStepResult.class);
        dropAll(QAStep.class);
        dropAll(Dataset.class);
    }

    private Scc[] sccs() {
        String[] codes = { "2294000000", "2296000000", "2296002000", "2311010000", "2302002100", "2805001000",
                "2104008001", "2302302100", "2801500500", "2311010340", "2801500000", "2850000037", "2311010002",
                "2801000003", "2104008021", "2801500002" };
        List list = new ArrayList();
        for (int i = 0; i < codes.length; i++) {
            Scc scc = new Scc();
            scc.setCode(codes[i]);
            list.add(scc);
        }
        return (Scc[]) list.toArray(new Scc[0]);
    }

    private Pollutant pm10Pollutant() {
        return (Pollutant) load(Pollutant.class, "PM10");
    }

}
