package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.cost.analysis.maxreduction.MaxEmsRedStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

//import org.hibernate.Session;

public class MaxEmsRedStrategyDetailedTest extends MaxEmsRedStrategyTestDetailedCase {

    private double tolerance = 1e-6;
    
    public void testShouldRunMaxEmsRedStrategyWithNonpointDataAndFilterOnAllMeasureClasses() throws Exception {
        ControlStrategy strategy = null;
        EmfDataset inputDataset = setInputDataset("ORL nonpoint");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            ControlMeasureClass[] cmcs = {};
//            ControlMeasureClass[] cmcs = {(ControlMeasureClass)load(ControlMeasureClass.class, "Known"),
//                    (ControlMeasureClass)load(ControlMeasureClass.class, "Emerging")};
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant(), cmcs);
            User user = emfUser();
            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServer(),
                    new Integer(500), sessionFactory());
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory().getSession());
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 15 records in the summary results.", rs.getInt(1) == 15);

            //make sure nothing shows up, this would not be the best control measure for this scc/fips...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2302002100' and cm_abbrev='PCHRBESP'");
            assertTrue("SCC = 2302002100 and CM = PCHRBESP, don't use this cm, not the max reduction cm...", !rs.first());

            //make sure nothing shows up, assigned different pollutant (PM2.5) for same SCC...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37019' and poll ='PM2.5'");
            assertTrue("assigned different pollutant for same SCC", !rs.first());

            //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37029'");
            rs.next();
            assertTrue("SCC = 2104008000 FIPS = 37029 reduction = 88.2", Math.abs(rs.getDouble("percent_reduction") - 88.2)/88.2 < tolerance);
            assertTrue("SCC = 2104008000 FIPS = 37029 annual cost = 70034226.09", Math.abs(rs.getDouble("annual_cost") - 70034226.09)/70034226.09 < tolerance);
            assertTrue("SCC = 2104008000 FIPS = 37029 emis reduction = 35280", Math.abs(rs.getDouble("emis_reduction") - 35280)/35280 < tolerance);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500000' and fips = '37015'");
            rs.next();
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 61.8", Math.abs(rs.getDouble("percent_reduction") - 61.8)/61.8 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 38445061.95", Math.abs(rs.getDouble("annual_cost") - 38445061.95)/38445061.95 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8652", Math.abs(rs.getDouble("emis_reduction") - 8652)/8652 < tolerance);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy, inputDataset);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithNonpointDataAndFilterOnKnownMeasureClass() throws Exception {
        ControlStrategy strategy = null;
        EmfDataset inputDataset = setInputDataset("ORL nonpoint");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            ControlMeasureClass[] cmcs = {(ControlMeasureClass)load(ControlMeasureClass.class, "Known")};
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant(), cmcs);
            User user = emfUser();
            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServer(),
                    new Integer(500), sessionFactory());
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory().getSession());
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 14 records in the summary results.", rs.getInt(1) == 14);

            //make sure nothing shows up, this would not be the best control measure for this scc/fips...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2302002100' and cm_abbrev='PCHRBESP'");
            assertTrue("SCC = 2302002100 and CM = PCHRBESP, don't use this cm, not the max reduction cm...", !rs.first());

            //make sure nothing shows up, assigned different pollutant (PM2.5) for same SCC...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37019' and poll ='PM2.5'");
            assertTrue("assigned different pollutant for same SCC", !rs.first());

            //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37029'");
            rs.next();
            assertTrue("SCC = 2104008000 FIPS = 37029 reduction = 88.2", Math.abs(rs.getDouble("percent_reduction") - 88.2)/88.2 < tolerance);
            assertTrue("SCC = 2104008000 FIPS = 37029 annual cost = 70034226.09", Math.abs(rs.getDouble("annual_cost") - 70034226.09)/70034226.09 < tolerance);
            assertTrue("SCC = 2104008000 FIPS = 37029 emis reduction = 35280", Math.abs(rs.getDouble("emis_reduction") - 35280)/35280 < tolerance);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500000' and fips = '37015'");
            rs.next();
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 61.8", Math.abs(rs.getDouble("percent_reduction") - 61.8)/61.8 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 38445061.95", Math.abs(rs.getDouble("annual_cost") - 38445061.95)/38445061.95 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8652", Math.abs(rs.getDouble("emis_reduction") - 8652)/8652 < tolerance);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy, inputDataset);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithNonpointDataAndFilterOnKnownAndEmergingMeasureClasses() throws Exception {
        ControlStrategy strategy = null;
        EmfDataset inputDataset = setInputDataset("ORL nonpoint");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            ControlMeasureClass[] cmcs = {(ControlMeasureClass)load(ControlMeasureClass.class, "Known"),
                    (ControlMeasureClass)load(ControlMeasureClass.class, "Emerging")};
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant(), cmcs);
            User user = emfUser();
            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServer(),
                    new Integer(500), sessionFactory());
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory().getSession());
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 15 records in the summary results.", rs.getInt(1) == 15);

            //make sure nothing shows up, this would not be the best control measure for this scc/fips...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2302002100' and cm_abbrev='PCHRBESP'");
            assertTrue("SCC = 2302002100 and CM = PCHRBESP, don't use this cm, not the max reduction cm...", !rs.first());

            //make sure nothing shows up, assigned different pollutant (PM2.5) for same SCC...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37019' and poll ='PM2.5'");
            assertTrue("assigned different pollutant for same SCC", !rs.first());

            //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37029'");
            rs.next();
            assertTrue("SCC = 2104008000 FIPS = 37029 reduction = 88.2", Math.abs(rs.getDouble("percent_reduction") - 88.2)/88.2 < tolerance);
            assertTrue("SCC = 2104008000 FIPS = 37029 annual cost = 70034226.09", Math.abs(rs.getDouble("annual_cost") - 70034226.09)/70034226.09 < tolerance);
            assertTrue("SCC = 2104008000 FIPS = 37029 emis reduction = 35280", Math.abs(rs.getDouble("emis_reduction") - 35280)/35280 < tolerance);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500000' and fips = '37015'");
            rs.next();
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 61.8", Math.abs(rs.getDouble("percent_reduction") - 61.8)/61.8 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 38445061.95", Math.abs(rs.getDouble("annual_cost") - 38445061.95)/38445061.95 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8652", Math.abs(rs.getDouble("emis_reduction") - 8652)/8652 < tolerance);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy, inputDataset);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithNonpointDataAndFilterOnEmergingMeasureClass() throws Exception {
        ControlStrategy strategy = null;
        EmfDataset inputDataset = setInputDataset("ORL nonpoint");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            ControlMeasureClass[] cmcs = {(ControlMeasureClass)load(ControlMeasureClass.class, "Emerging")};
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant(), cmcs);
            User user = emfUser();
            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServer(),
                    new Integer(500), sessionFactory());
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory().getSession());
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 3 records in the summary results.", rs.getInt(1) == 3);

            //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
            //check SCC = 2104008000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008000' and fips = '37029'");
            rs.next();
            assertTrue("SCC = 2104008000 FIPS = 37029 reduction = 50", Math.abs(rs.getDouble("percent_reduction") - 50)/50 < tolerance);
            assertTrue("SCC = 2104008000 FIPS = 37029 annual cost = 36438495.61", Math.abs(rs.getDouble("annual_cost") - 36438495.61)/36438495.61 < tolerance);
            assertTrue("SCC = 2104008000 FIPS = 37029 emis reduction = 20000", Math.abs(rs.getDouble("emis_reduction") - 20000)/20000 < tolerance);

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
        dropAll(Scc.class);
        dropAll(ControlMeasure.class);
        dropAll(ControlStrategyResult.class);
        dropAll(ControlStrategy.class);
        dropAll(QAStepResult.class);
        dropAll(QAStep.class);
        dropAll(Dataset.class);
    }

    private Pollutant pm10Pollutant() {
        return (Pollutant) load(Pollutant.class, "PM10");
    }

//    public void testImportControlMeasures() throws EmfException, Exception {
//        importControlMeasures();
//    }
//    
}
