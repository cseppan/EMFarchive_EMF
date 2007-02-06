package gov.epa.emissions.framework.services.cost;

//import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.Pollutant;
//import gov.epa.emissions.commons.db.Datasource;
//import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.analysis.maxreduction.MaxEmsRedStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
//import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.CMImportTask;
import gov.epa.emissions.framework.services.data.EmfDataset;
//import gov.epa.emissions.framework.services.persistence.EmfDatabaseSetup;
//import gov.epa.emissions.framework.services.data.QAStep;
//import gov.epa.emissions.framework.services.data.QAStepResult;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

//import org.hibernate.Session;

public class MaxEmsRedStrategyDetailedTest extends MaxEmsRedStrategyTestDetailedCase {

    public void testShouldRunMaxEmsRedStrategyWithOneControlMeasureAndNonpointData() throws Exception {
        ControlStrategy strategy = null;
        EmfDataset inputDataset = setInputDataset("ORL nonpoint");
        
        //import control measures to use...
        importControlMeasures();
        ResultSet rs = null;
        Connection cn = null;
        try {
            ControlMeasureClass[] cmcs = {};
//            ControlMeasureClass[] cmcs = {(ControlMeasureClass)load(ControlMeasureClass.class, "Known"),
//                    (ControlMeasureClass)load(ControlMeasureClass.class, "Emerging")};
            strategy = controlStrategy(inputDataset, "CS_test_case" + Math.round(Math.random() * 1000), pm10Pollutant(), cmcs);
            User user = emfUser();
            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServer(),
                    new Integer(500), sessionFactory());
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory().getSession());
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();
//            Datasource datasource = dbServer().getEmissionsDatasource();
            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            //make sure nothing shows up, assigned different pollutant for same SCC...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName
                    + " where scc = '2104008000' and fips = '37029' and poll ='VOC'");
            assertTrue("assigned different pollutant for same SCC", !rs.first());

            //make sure nothing shows up, assigned different pollutant for same SCC...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37019' and poll ='PM2.5'");
            assertTrue("assigned different pollutant for same SCC", !rs.first());

//            rs = stmt.executeQuery("SELECT * FROM " + tableName
//                    + "where scc = '2104008000' and fips = '37019' and poll ='PM2.5'");
//
//            while (rs.next()) {
//                // retrieve and print the values for the current row
//                int i = rs.getInt("a");
//                String s = rs.getString("b");
//                float f = rs.getFloat("c");
//                System.out.println("ROW = " + i + " " + s + " " + f);
//            }
            
            // rs will be scrollable, will not show changes made by others,
            // and will be updatable

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy, inputDataset);
            removeData();
        }
    }

    private void dropTables(ControlStrategy strategy, EmfDataset inputDataset) throws Exception {
        if (strategy != null)
            dropTable(detailResultDatasetTableName(strategy), dbServer().getEmissionsDatasource());
        dropQASummaryTables(inputDataset);
        ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, session);
        dropQASummaryTables((EmfDataset) result.getDetailedResultDataset());

    }

    private void dropQASummaryTables(EmfDataset dataset) throws Exception {
        dropTable("qasummarize_by_county_and_pollutant_dsid" + dataset.getId() + "_v0", dbServer()
                .getEmissionsDatasource());
        dropTable("qasummarize_by_scc_and_pollutant_dsid" + dataset.getId() + "_v0", dbServer()
                .getEmissionsDatasource());
        dropTable("qasummarize_by_pollutant_dsid" + dataset.getId() + "_v0", dbServer().getEmissionsDatasource());
    }

    private void removeData() {
//        dropAll(Scc.class);
//        dropAll(ControlMeasure.class);
//        dropAll(ControlStrategyResult.class);
//        dropAll(ControlStrategy.class);
//        dropAll(QAStepResult.class);
//        dropAll(QAStep.class);
//        dropAll(Dataset.class);
    }

    private Pollutant pm10Pollutant() {
        return (Pollutant) load(Pollutant.class, "PM10");
    }

//    public void testImportControlMeasures() throws EmfException, Exception {
//        importControlMeasures();
//    }
//    
    private void importControlMeasures() throws EmfException, Exception {
        File folder = new File("test/data/cost/controlMeasure");
        String[] fileNames = { "CMSummary.csv", "CMSCCs.csv", "CMEfficiencies.csv", "CMReferences.csv" };
        CMImportTask task = new CMImportTask(folder, fileNames, emfUser(), sessionFactory());
        task.run();
    }
}
