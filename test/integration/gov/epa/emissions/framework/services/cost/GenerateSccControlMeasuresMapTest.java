package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.cost.controlStrategy.GenerateSccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.controlStrategy.SccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.List;

public class GenerateSccControlMeasuresMapTest extends MaxEmsRedStrategyTestCase {
    
    public void testDummy(){
        assertTrue(true);
    }

    public void FIXME_testShouldCreateASccControlMeasureSccMap() throws Exception {
        try {
            ControlMeasure cm1 = addControlMeasure("Control Measure 1", "CM1", sccs1(), new EfficiencyRecord[0]);
            ControlMeasure cm2 = addControlMeasure("Control Measure 2", "CM2", sccs2(), new EfficiencyRecord[0]);
            ControlStrategy strategy = controlStrategy(inputDataset, "CS1", pm10Pollutant());
            GenerateSccControlMeasuresMap createMap = new GenerateSccControlMeasuresMap(dbServer(),
                    qualfiedTableName(emissionTableName()), strategy, sessionFactory());
            SccControlMeasuresMap map = createMap.create();
            assertEquals("map size 9", 9, map.size());
            ControlMeasure[] measures = map.getControlMeasures("2294000000");
            assertEquals(1, measures.length);
            assertEquals(cm1.getId(), measures[0].getId());

            measures = map.getControlMeasures("2801500000");
            assertEquals(2, measures.length);
            assertEquals(cm1.getId(), measures[0].getId());
            assertEquals(cm2.getId(), measures[1].getId());
        } finally {
            dropAll(Scc.class);
            dropAll(ControlMeasure.class);
            dropAll(ControlStrategy.class);
        }

    }

    private String qualfiedTableName(String table) {
        return dbServer().getEmissionsDatasource().getName() + "." + table;
    }

    private String emissionTableName() {
        return inputDataset.getInternalSources()[0].getTable();
    }

    private Scc[] sccs1() {
        String[] codes = { "2294000000", "2296000000", "2311010000", "2302002100", "2805001000", "2104008001",
                "2801500000", "2850000030", "2801000003" };
        List list = new ArrayList();
        for (int i = 0; i < codes.length; i++) {
            Scc scc = new Scc();
            scc.setCode(codes[i]);
            list.add(scc);
        }
        return (Scc[]) list.toArray(new Scc[0]);
    }

    private Scc[] sccs2() {
        String[] codes = { "2801500000", "2850000030", "2801000003" };
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
