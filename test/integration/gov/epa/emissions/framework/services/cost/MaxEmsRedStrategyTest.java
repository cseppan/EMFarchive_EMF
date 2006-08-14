package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.cost.analysis.maxreduction.MaxEmsRedStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MaxEmsRedStrategyTest extends ServiceTestCase {

    private DbServer dbServer;

    private SqlDataTypes sqlDataTypes;

    private EmfDataset inputDataset;

    private ControlStrategy strategy;

    protected void doSetUp() throws Exception {
        dbServer = dbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
        inputDataset = new EmfDataset();
        inputDataset.setName("test");
        inputDataset.setCreator(user().getUsername());
        inputDataset.setDatasetType(orlNonpointDatasetType());
    }

    private DatasetType orlNonpointDatasetType() {
        return (DatasetType) load(DatasetType.class, "ORL Nonpoint Inventory (ARINV)");
    }

    protected void doTearDown() throws Exception {
        dropTable("test", dbServer.getEmissionsDatasource());
        dropTable(detailResultDatasetTableName(strategy), dbServer.getEmissionsDatasource());
    }
   
    public void testShouldRunMaxEmsRedStrategyWithOneControlMeasure() throws Exception {
        try {
            addControlMeasure("Control Measure 1", "CM1", 90, 900);
            inputDataset = addORLNonpointDataset();
            strategy = controlStrategy(inputDataset);
            User user = user();
            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, dbServer, new Integer(500),
                    sessionFactory());
            maxEmfEmsRedStrategy.run(user);
            assertEquals("No of rows in the detail result table is 16",16,countRecords(detailResultDatasetTableName(strategy)));
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            dropAll(ControlMeasure.class);
            dropAll(ControlStrategy.class);
            dropAll(EmfDataset.class);
        }

    }

    private ControlStrategy controlStrategy(EmfDataset inputDataset) {
        ControlStrategy strategy = new ControlStrategy();
        strategy.setName("CS1");
        strategy.setInputDatasets(new EmfDataset[] { inputDataset });
        strategy.setDatasetType(inputDataset.getDatasetType());
        strategy.setDatasetVersion(0);// initial version
        strategy.setAnalysisYear(2000);
        strategy.setTargetPollutant("PM10");
        strategy.setStrategyType(strategyType());
        add(strategy);
        return strategy;
    }

    private User user() {
        return new UserDAO().get("emf", session);
    }

    private StrategyType strategyType() {
        return (StrategyType) load(StrategyType.class, "Max Emissions Reduction");
    }

    private EmfDataset addORLNonpointDataset() throws ImporterException {
        Version version = new Version();
        version.setVersion(0);
        File folder = new File("test/data/cost");
        String[] fileNames = { "orl-nonpoint-with-larger_values.txt" };
        ORLNonPointImporter importer = new ORLNonPointImporter(folder, fileNames, inputDataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version));
        importer.run();
        add(inputDataset);
        session.flush();
        return (EmfDataset) load(EmfDataset.class, "test");
    }

    private void addControlMeasure(String name, String abbr, float efficiency, float cost) {
        ControlMeasure measure = new ControlMeasure();
        measure.setName(name);
        measure.setAbbreviation(abbr);
        EfficiencyRecord record = record(efficiency, cost);
        EfficiencyRecord[] efficiencyRecords = new EfficiencyRecord[] { record };
        measure.setEfficiencyRecords(efficiencyRecords);
        measure.setSccs(sccs());
        add(measure);
    }

    private Scc[] sccs() {
        String[] codes = { "2294000000", "2296000000", "2296000000", "2311010000", "2302002100", "2805001000",
                "2104008001", "2302002100", "2801500000", "2311010000", "2801500000", "2850000030", "2311010000",
                "2801000003", "2104008001", "2801500000" };
        List list = new ArrayList();
        for (int i = 0; i < codes.length; i++) {
            Scc scc = new Scc();
            scc.setCode(codes[i]);
            list.add(scc);
        }
        return (Scc[]) list.toArray(new Scc[0]);
    }

    private EfficiencyRecord record(float efficiency, float cost) {
        EfficiencyRecord record = new EfficiencyRecord();
        record.setPollutant(pm10Pollutant());
        record.setEfficiency(efficiency);
        record.setRuleEffectiveness(100);
        record.setRulePenetration(100);
        record.setCostPerTon(cost);
        return record;
    }

    private Pollutant pm10Pollutant() {
        return (Pollutant) load(Pollutant.class, "PM10");
    }
    
    private String detailResultDatasetTableName(ControlStrategy strategy) {
        StrategyResult[] strategyResults = strategy.getStrategyResults();
        Dataset detailedResultDataset = strategyResults[0].getDetailedResultDataset();
        return detailedResultDataset.getInternalSources()[0].getTable();
    }


}
