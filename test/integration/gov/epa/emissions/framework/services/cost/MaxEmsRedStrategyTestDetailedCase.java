package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableModifier;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.commons.io.orl.ORLNonRoadImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.io.orl.ORLPointImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.CMImportTask;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.data.EmfDataset;
import java.io.File;

import org.hibernate.Session;

public class MaxEmsRedStrategyTestDetailedCase extends ServiceTestCase {

    private DbServer dbServer;

    private SqlDataTypes sqlDataTypes;
//    protected ControlStrategyService service;

    protected String tableName = "test" + Math.round(Math.random() * 1000) % 1000;

    protected void doSetUp() throws Exception {
        dbServer = dbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
        //import control measures to use...
        importControlMeasures();
//        service = new ControlStrategyServiceImpl(sessionFactory());
    }

    protected EmfDataset setInputDataset(String type) throws Exception {
        EmfDataset inputDataset = new EmfDataset();
        inputDataset.setName(tableName);
        inputDataset.setCreator(emfUser().getUsername());
        inputDataset.setDatasetType(getDatasetType(type));//orlNonpointDatasetType());

        add(inputDataset);
        session.flush();
        inputDataset = (EmfDataset) load(EmfDataset.class, tableName);

        if (type.equalsIgnoreCase("ORL nonpoint"))
            inputDataset = addORLNonpointDataset(inputDataset);
        else if (type.equalsIgnoreCase("ORL point"))
            inputDataset = addORLPointDataset(inputDataset);
        else if (type.equalsIgnoreCase("ORL onroad"))
            inputDataset = addORLOnroadDataset(inputDataset);
        else if (type.equalsIgnoreCase("ORL Nonroad"))
            inputDataset = addORLNonroadDataset(inputDataset);
        
        addVersionZeroEntryToVersionsTable(inputDataset, dbServer.getEmissionsDatasource());
        return (EmfDataset) load(EmfDataset.class, tableName);
    }

    private DatasetType getDatasetType(String type) {
        DatasetType ds = null;
        if (type.equalsIgnoreCase("ORL nonpoint")) 
            ds = (DatasetType) load(DatasetType.class, "ORL Nonpoint Inventory (ARINV)");
        else if (type.equalsIgnoreCase("ORL point"))
            ds = (DatasetType) load(DatasetType.class, "ORL Point Inventory (PTINV)");
        else if (type.equalsIgnoreCase("ORL onroad"))
            ds = (DatasetType) load(DatasetType.class, "ORL Onroad Inventory (MBINV)");
        else if (type.equalsIgnoreCase("ORL Nonroad"))
            ds = (DatasetType) load(DatasetType.class, "ORL Nonroad Inventory (ARINV)");
        return ds;
    }

//    private DatasetType orlNonpointDatasetType() {
//        return (DatasetType) load(DatasetType.class, "ORL Nonpoint Inventory (ARINV)");
//    }

    private void addVersionZeroEntryToVersionsTable(Dataset dataset, Datasource datasource) throws Exception {
        TableModifier modifier = new TableModifier(datasource, "versions");
        String[] data = { null, dataset.getId() + "", "0", "Initial Version", "", "true", null };
        modifier.insertOneRow(data);
    }

    protected void doTearDown() throws Exception {
        dropTable(tableName, dbServer.getEmissionsDatasource());

//        dropAll(Version.class);
//        dropAll(InternalSource.class);
//        dropAll(QAStepResult.class);
//        dropAll(QAStep.class);
//        dropAll(EmfDataset.class);

    }

    protected ControlStrategy controlStrategy(EmfDataset inputDataset, String name, Pollutant pollutant, ControlMeasureClass[] classes) {
        ControlStrategy strategy = new ControlStrategy();
        strategy.setName(name);
        strategy.setInputDatasets(new EmfDataset[] { inputDataset });
        strategy.setDatasetType(inputDataset.getDatasetType());
        strategy.setDatasetVersion(0);// initial version
        strategy.setInventoryYear(2000);
        strategy.setCostYear(2000);
        strategy.setTargetPollutant(pollutant);
        strategy.setStrategyType(maxEmisRedStrategyType());
        strategy.setControlMeasureClasses(classes);
        add(strategy);
        return strategy;
    }

    protected ControlStrategy controlStrategy(EmfDataset inputDataset, String name, Pollutant pollutant, LightControlMeasure[] measures) {
        ControlStrategy strategy = new ControlStrategy();
        strategy.setName(name);
        strategy.setInputDatasets(new EmfDataset[] { inputDataset });
        strategy.setDatasetType(inputDataset.getDatasetType());
        strategy.setDatasetVersion(0);// initial version
        strategy.setInventoryYear(2000);
        strategy.setCostYear(2000);
        strategy.setTargetPollutant(pollutant);
        strategy.setStrategyType(maxEmisRedStrategyType());
        strategy.setControlMeasures(measures);
        add(strategy);
        return strategy;
    }

    protected ControlStrategy controlStrategy(EmfDataset inputDataset, String name, Pollutant pollutant) {
        ControlStrategy strategy = new ControlStrategy();
        strategy.setName(name);
        strategy.setInputDatasets(new EmfDataset[] { inputDataset });
        strategy.setDatasetType(inputDataset.getDatasetType());
        strategy.setDatasetVersion(0);// initial version
        strategy.setInventoryYear(2000);
        strategy.setCostYear(2000);
        strategy.setTargetPollutant(pollutant);
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

    private EmfDataset addORLPointDataset(EmfDataset inputDataset) throws ImporterException {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(inputDataset.getId());

        File folder = new File("test/data/orl/nc");
        String[] fileNames = { "ptinv.nti99_NC-with-matching_values.txt" };
        ORLPointImporter importer = new ORLPointImporter(folder, fileNames, inputDataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, inputDataset));
        importer.run();
        add(inputDataset);
        session.flush();
        return (EmfDataset) load(EmfDataset.class, tableName);
    }

    private EmfDataset addORLNonpointDataset(EmfDataset inputDataset) throws ImporterException {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(inputDataset.getId());

        File folder = new File("test/data/cost");
        String[] fileNames = { "orl-nonpoint-for_cs_testing.txt" };
        ORLNonPointImporter importer = new ORLNonPointImporter(folder, fileNames, inputDataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, inputDataset));
        importer.run();
        add(inputDataset);
        session.flush();
        return (EmfDataset) load(EmfDataset.class, tableName);
    }

    private EmfDataset addORLOnroadDataset(EmfDataset inputDataset) throws ImporterException {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(inputDataset.getId());

        File folder = new File("test/data/orl/nc");
        String[] fileNames = { "orl_onroad_with_poll_name_txt_17aug2006.txt" };
        ORLOnRoadImporter importer = new ORLOnRoadImporter(folder, fileNames, inputDataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, inputDataset));
        importer.run();
        add(inputDataset);
        session.flush();
        return (EmfDataset) load(EmfDataset.class, tableName);
    }

    private EmfDataset addORLNonroadDataset(EmfDataset inputDataset) throws ImporterException {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(inputDataset.getId());

        File folder = new File("test/data/orl/nc");
        String[] fileNames = { "arinv.nonroad.nti99d_NC-with-matching_values.txt" };
        ORLNonRoadImporter importer = new ORLNonRoadImporter(folder, fileNames, inputDataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, inputDataset));
        importer.run();
        add(inputDataset);
        session.flush();
        return (EmfDataset) load(EmfDataset.class, tableName);
    }

    protected ControlMeasure addControlMeasure(String name, String abbr, Scc[] sccs, EfficiencyRecord[] records) {
        ControlMeasure measure = new ControlMeasure();
        measure.setName(name);
        measure.setAbbreviation(abbr);
        measure.setEfficiencyRecords(records);
        measure.setSccs(sccs);
        add(measure);
        ControlMeasure load = (ControlMeasure) load(ControlMeasure.class, measure.getName());
        for (int i = 0; i < sccs.length; i++) {
            sccs[i].setControlMeasureId(load.getId());
            add(sccs[i]);
        }
        return load;
    }

    protected EfficiencyRecord record(Pollutant pollutant, String locale, float efficiency, float cost, int costYear) {
        EfficiencyRecord record = new EfficiencyRecord();
        record.setPollutant(pollutant);
        record.setLocale(locale);
        record.setEfficiency(efficiency);
        record.setRuleEffectiveness(100);
        record.setRulePenetration(100);
        record.setCostPerTon(cost);
        record.setCostYear(costYear);
        return record;
    }

    protected String detailResultDatasetTableName(ControlStrategy strategy) throws Exception {
        Session session = sessionFactory().getSession();
        try {
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, session);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            return detailedResultDataset.getInternalSources()[0].getTable();
        } finally {
            session.close();
        }
    }

    private void importControlMeasures() throws EmfException, Exception {
        File folder = new File("test/data/cost/controlMeasure");
        String[] fileNames = { "CMSummary.csv", "CMSCCs.csv", "CMEfficiencies.csv", "CMReferences.csv" };
        CMImportTask task = new CMImportTask(folder, fileNames, emfUser(), sessionFactory(), dbServer);
        task.run();
    }
}
