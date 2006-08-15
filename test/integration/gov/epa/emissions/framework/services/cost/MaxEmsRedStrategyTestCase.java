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
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.File;

public class MaxEmsRedStrategyTestCase extends ServiceTestCase {

    private DbServer dbServer;

    private SqlDataTypes sqlDataTypes;

    protected EmfDataset inputDataset;

    protected void doSetUp() throws Exception {
        dbServer = dbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
        inputDataset = new EmfDataset();
        inputDataset.setName("test");
        inputDataset.setCreator(emfUser().getUsername());
        inputDataset.setDatasetType(orlNonpointDatasetType());
        inputDataset = addORLNonpointDataset();
    }

    private DatasetType orlNonpointDatasetType() {
        return (DatasetType) load(DatasetType.class, "ORL Nonpoint Inventory (ARINV)");
    }

    protected void doTearDown() throws Exception {
        dropTable("test", dbServer.getEmissionsDatasource());
        dropAll(EmfDataset.class);
    }



    protected ControlStrategy controlStrategy(EmfDataset inputDataset,String name, Pollutant pollutant) {
        ControlStrategy strategy = new ControlStrategy();
        strategy.setName(name);
        strategy.setInputDatasets(new EmfDataset[] { inputDataset });
        strategy.setDatasetType(inputDataset.getDatasetType());
        strategy.setDatasetVersion(0);// initial version
        strategy.setAnalysisYear(2000);
        strategy.setTargetPollutant(pollutant.getName());
        strategy.setStrategyType(maxEmisRedStrategyType());
        add(strategy);
        return strategy;
    }

    protected User emfUser() {
        return new UserDAO().get("emf", session);
    }

    private StrategyType maxEmisRedStrategyType() {
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

    protected ControlMeasure addControlMeasure(String name, String abbr,Scc[]sccs, EfficiencyRecord[] records) {
        ControlMeasure measure = new ControlMeasure();
        measure.setName(name);
        measure.setAbbreviation(abbr);
        measure.setEfficiencyRecords(records);
        measure.setSccs(sccs);
        add(measure);
        return (ControlMeasure) load(ControlMeasure.class,measure.getName());
    }

    protected EfficiencyRecord record(Pollutant pollutant, float efficiency, float cost) {
        EfficiencyRecord record = new EfficiencyRecord();
        record.setPollutant(pollutant);
        record.setEfficiency(efficiency);
        record.setRuleEffectiveness(100);
        record.setRulePenetration(100);
        record.setCostPerTon(cost);
        return record;
    }

    protected String detailResultDatasetTableName(ControlStrategy strategy) {
        StrategyResult[] strategyResults = strategy.getStrategyResults();
        Dataset detailedResultDataset = strategyResults[0].getDetailedResultDataset();
        return detailedResultDataset.getInternalSources()[0].getTable();
    }

}
