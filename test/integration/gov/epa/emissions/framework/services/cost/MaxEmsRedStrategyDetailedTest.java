package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.cost.analysis.maxreduction.MaxEmsRedStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyInventoryOutput;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfDatabaseSetup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.hibernate.Session;

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

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServerFactory(),
                    new Integer(500), sessionFactory);
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory.getSession());
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 15 records in the summary results." + rs.getInt(1), rs.getInt(1) == 15);

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
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 37553698.05", Math.abs(rs.getDouble("annual_cost") - 37553698.05)/37553698.05 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8820", Math.abs(rs.getDouble("emis_reduction") - 8820)/8820 < tolerance);

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

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServerFactory(),
                    new Integer(500), sessionFactory);
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory.getSession());
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
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 37553698.05", Math.abs(rs.getDouble("annual_cost") - 37553698.05)/37553698.05 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8820", Math.abs(rs.getDouble("emis_reduction") - 8820)/8820 < tolerance);

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

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServerFactory(),
                    new Integer(500), sessionFactory);
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory.getSession());
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
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 37553698.05", Math.abs(rs.getDouble("annual_cost") - 37553698.05)/37553698.05 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8820", Math.abs(rs.getDouble("emis_reduction") - 8820)/8820 < tolerance);

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

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServerFactory(),
                    new Integer(500), sessionFactory);
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory.getSession());
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

    public void testShouldRunMaxEmsRedStrategyWithNonpointDataAndFilterOnSpecificMeasures() throws Exception {
        ControlStrategy strategy = null;
        EmfDataset inputDataset = setInputDataset("ORL nonpoint");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            LightControlMeasure[] cms = {(LightControlMeasure)load(LightControlMeasure.class, "Bale Stack/Propane Burning; Agricultural Burning"), 
                    (LightControlMeasure)load(LightControlMeasure.class, "ESP for Commercial Cooking; Conveyorized Charbroilers")};
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant(), cms);
            User user = emfUser();
            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServerFactory(),
                    new Integer(500), sessionFactory);
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory.getSession());
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 4 records in the summary results.", rs.getInt(1) == 4);

            //make sure inv entry has the right numbers...
            //check SCC = 2302002100 FIPS = 37013 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2302002100' and fips = '37005'");
            rs.next();
            assertTrue("SCC = 2302002100 FIPS = 37005 reduction = 18.5", Math.abs(rs.getDouble("percent_reduction") - 18.5)/18.5 < tolerance);
            assertTrue("SCC = 2302002100 FIPS = 37005 annual cost = 10282803.04", Math.abs(rs.getDouble("annual_cost") - 10282803.04)/10282803.04 < tolerance);
            assertTrue("SCC = 2302002100 FIPS = 37005 emis reduction = 1480", Math.abs(rs.getDouble("emis_reduction") - 1480)/1480 < tolerance);

            //make sure inv entry has the right numbers...
            //check SCC = 2801500000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500000' and fips = '37015'");
            rs.next();
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 37553698.05", Math.abs(rs.getDouble("annual_cost") - 37553698.05)/37553698.05 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8820", Math.abs(rs.getDouble("emis_reduction") - 8820)/8820 < tolerance);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy, inputDataset);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithNonRoadData() throws Exception {
        ControlStrategy strategy = null;
        EmfDataset inputDataset = setInputDataset("ORL nonroad");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant());
            User user = emfUser();
            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServerFactory(),
                    new Integer(500), sessionFactory);
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory.getSession());
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 3 records in the summary results." + rs.getInt(1), rs.getInt(1) == 3);

            //make sure inv entry has the right numbers...
            //check SCC = 2302002100 FIPS = 37013 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2294000270' and fips = '37001'");
            rs.next();
            assertTrue("SCC = 2294000270 FIPS = 37001 reduction = 71.1" + rs.getDouble("percent_reduction"), Math.abs(rs.getDouble("percent_reduction") - 71.1)/71.1 < tolerance);
            assertTrue("SCC = 2294000270 FIPS = 37001 annual cost = 7.02655E-07 " + rs.getDouble("annual_cost"), Math.abs(rs.getDouble("annual_cost") - 7.02655E-07)/7.02655E-07 < tolerance);
            assertTrue("SCC = 2294000270 FIPS = 37001 emis reduction = 2.37705E-09", Math.abs(rs.getDouble("emis_reduction") - 2.37705E-09)/2.37705E-09 < tolerance);

            //make sure inv entry has the right numbers...
            //check SCC = 2801500000 FIPS = 37029 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500130' and fips = '37001'");
            rs.next();
            assertTrue("SCC = 2801500130 FIPS = 37001 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < tolerance);
            assertTrue("SCC = 2801500130 FIPS = 37001 annual cost = 4.927410E-04", Math.abs(rs.getDouble("annual_cost") - 4.927410E-04)/4.927410E-04 < tolerance);
            assertTrue("SCC = 2801500130 FIPS = 37001 emis reduction = 1.15727E-07", Math.abs(rs.getDouble("emis_reduction") - 1.15727E-07)/1.15727E-07 < tolerance);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy, inputDataset);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithOnRoadData() throws Exception {
        ControlStrategy strategy = null;
        EmfDataset inputDataset = setInputDataset("ORL onroad");
        
        ResultSet rs = null;
        Connection cn = null;
        try {
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant());
            User user = emfUser();
            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServerFactory(),
                    new Integer(500), sessionFactory);
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory.getSession());
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 4 records in the summary results." + rs.getInt(1), rs.getInt(1) == 4);

            //make sure inv entry has the right numbers...
            //check SCC = 2302002100 FIPS = 37013 POLL = PM10 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2311010000' and fips = '37013' and poll='PM10'");
            rs.next();
            assertTrue("SCC = 2311010000 FIPS = 37013 reduction = 63 poll = PM10" + rs.getDouble("percent_reduction"), Math.abs(rs.getDouble("percent_reduction") - 63)/63 < tolerance);
            assertTrue("SCC = 2311010000 FIPS = 37013 annual cost = 8.608595E-01 poll = PM10" + rs.getDouble("annual_cost"), Math.abs(rs.getDouble("annual_cost") - 8.608595E-01)/8.608595E-01 < tolerance);
            assertTrue("SCC = 2311010000 FIPS = 37013 emis reduction = 1.732500E-04 poll = PM10", Math.abs(rs.getDouble("emis_reduction") - 1.732500E-04)/1.732500E-04 < tolerance);

            //check SCC = 2302002100 FIPS = 37013 POLL = NOX inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2311010000' and fips = '37013' and poll='NOX'");
            rs.next();
            assertTrue("SCC = 2311010000 FIPS = 37013 reduction = 70 poll = NOX" + rs.getDouble("percent_reduction"), Math.abs(rs.getDouble("percent_reduction") - 70)/70 < tolerance);
            assertTrue("SCC = 2311010000 FIPS = 37013 annual cost = 2.536202E+03 poll = NOX" + rs.getDouble("annual_cost"), Math.abs(rs.getDouble("annual_cost") - 2.536202E+03)/2.536202E+03 < tolerance);
            assertTrue("SCC = 2311010000 FIPS = 37013 emis reduction = 1.750000E+00 poll = NOX", Math.abs(rs.getDouble("emis_reduction") - 1.750000E+00)/1.750000E+00 < tolerance);

            //check SCC = 2801500000 FIPS = 37029 POLL = PM10 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2296000000' and fips = '37006' and poll='PM10'");
            rs.next();
            assertTrue("SCC = 2296000000 FIPS = 37006 reduction = 68 poll = PM10", Math.abs(rs.getDouble("percent_reduction") - 68)/68 < tolerance);
            assertTrue("SCC = 2296000000 FIPS = 37006 annual cost = 4.959112E+02 poll = PM10", Math.abs(rs.getDouble("annual_cost") - 4.959112E+02)/4.959112E+02 < tolerance);
            assertTrue("SCC = 2296000000 FIPS = 37006 emis reduction = 6.690724E-01 poll = PM10", Math.abs(rs.getDouble("emis_reduction") - 6.690724E-01)/6.690724E-01 < tolerance);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            dropTables(strategy, inputDataset);
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithNonpointDataAndFilterOnAllMeasureClassesAndCreateControlledInv() throws Exception {
        ControlStrategy strategy = null;
        EmfDataset inputDataset = setInputDataset("ORL nonpoint");
        
        ResultSet rs = null;
        Connection cn = null;
        Connection cn2 = null;
        String tableName2 = "";
        ControlStrategyResult result = null;
        try {
            ControlMeasureClass[] cmcs = {};
//            ControlMeasureClass[] cmcs = {(ControlMeasureClass)load(ControlMeasureClass.class, "Known"),
//                    (ControlMeasureClass)load(ControlMeasureClass.class, "Emerging")};
            String strategyName = "CS_test_case__" + Math.round(Math.random() * 10000);
            strategy = controlStrategy(inputDataset, strategyName, pm10Pollutant(), cmcs);
            User user = emfUser();
            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());
//            HibernateSessionFactory sessionFactory = sessionFactory;
            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServerFactory(),
                    new Integer(500), sessionFactory);
            maxEmfEmsRedStrategy.run();

            Session session = sessionFactory.getSession();

//            session.flush();
//            session.clear();

            //get detailed result dataset
            result = new ControlStrategyDAO().controlStrategyResult(strategy, session);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

//            session.flush();
//            session.clear();

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
            assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 37553698.05", Math.abs(rs.getDouble("annual_cost") - 37553698.05)/37553698.05 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8820", Math.abs(rs.getDouble("emis_reduction") - 8820)/8820 < tolerance);

            //create the controlled inventory for this strategy run....
            ControlStrategyInventoryOutput output = new ControlStrategyInventoryOutput(user, strategy,
                    sessionFactory, dbServerFactory());
            output.create();
            //reload
            result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory.getSession());

            tableName2 = result.getControlledInventoryDataset().getName().replaceAll("ControlledInventory", "CSINVEN");
            
            cn2 = new EmfDatabaseSetup(config()).getDbServer().getEmissionsDatasource().getConnection();
            stmt = cn2.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2);
            rs.next();
            assertTrue("make sure there are 31 records in the controlled inventory results. ", rs.getInt(1) == 31);

/*
 * FIXME
 
                //make sure nothing shows up, this would not be the best control measure for this scc/fips...
//                rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2 
//                        + " where scc = '2302002100' and cm_abbrev='PCHRBESP'");
//                assertTrue("SCC = 2302002100 and CM = PCHRBESP, don't use this cm, not the max reduction cm...", !rs.first());
            
                //make sure nothing shows up, assigned different pollutant (PM2.5) for same SCC...
                rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2 
                        + " where scc = '2104008000' and fips = '37019' and poll ='PM2.5'");
                assertTrue("assigned different pollutant for same SCC", !rs.first());
            
                //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
                //check SCC = 2104008000 FIPS = 37029 inv entry
                rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2 
                        + " where scc = '2104008000' and fips = '37029'");
                rs.next();
                assertTrue("SCC = 2104008000 FIPS = 37029 reduction = 88.2", Math.abs(rs.getDouble("percent_reduction") - 88.2)/88.2 < tolerance);
                assertTrue("SCC = 2104008000 FIPS = 37029 annual cost = 70034226.09", Math.abs(rs.getDouble("annual_cost") - 70034226.09)/70034226.09 < tolerance);
                assertTrue("SCC = 2104008000 FIPS = 37029 emis reduction = 35280", Math.abs(rs.getDouble("emis_reduction") - 35280)/35280 < tolerance);
            
                //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
                //check SCC = 2104008000 FIPS = 37029 inv entry
                rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2 
                        + " where scc = '2801500000' and fips = '37015'");
                rs.next();
                assertTrue("SCC = 2801500000 FIPS = 37015 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < tolerance);
                assertTrue("SCC = 2801500000 FIPS = 37015 annual cost = 37553698.05", Math.abs(rs.getDouble("annual_cost") - 37553698.05)/37553698.05 < tolerance);
                assertTrue("SCC = 2801500000 FIPS = 37015 emis reduction = 8820", Math.abs(rs.getDouble("emis_reduction") - 8820)/8820 < tolerance);
*/            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            if (cn2 != null) cn2.close();
            dropTables(strategy, inputDataset);
            dropTable(tableName2, dbServer().getEmissionsDatasource());
            int dsid = result.getControlledInventoryDataset().getId();
            dropTable("qasummarize_by_county_and_pollutant_dsid" + dsid + "_v0", dbServer().getEmissionsDatasource());
            dropTable("qasummarize_by_scc_and_pollutant_dsid" + dsid + "_v0", dbServer().getEmissionsDatasource());
            dropTable("qasummarize_by_pollutant_dsid" + dsid + "_v0", dbServer().getEmissionsDatasource());
            dropTable("qasummarize_by_us_state_and_pollutant_dsid" + dsid + "_v0", dbServer().getEmissionsDatasource());
            removeData();
        }
    }

    public void testShouldRunMaxEmsRedStrategyWithPointDataAndFilterOnAllMeasureClasses() throws Exception {
        ControlStrategy strategy = null;
        EmfDataset inputDataset = setInputDataset("ORL point");
        
        ResultSet rs = null;
        Connection cn = null;
        Connection cn2 = null;
        ControlStrategyResult result = null;
        String tableName2 = "";
        try {
            strategy = controlStrategy(inputDataset, "CS_test_case__" + Math.round(Math.random() * 1000), pm10Pollutant());
            User user = emfUser();
            strategy = (ControlStrategy) load(ControlStrategy.class, strategy.getName());

            MaxEmsRedStrategy maxEmfEmsRedStrategy = new MaxEmsRedStrategy(strategy, user, dbServerFactory(),
                    new Integer(500), sessionFactory);
            maxEmfEmsRedStrategy.run();

            //get detailed result dataset
            result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory.getSession());
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            String tableName = detailedResultDataset.getInternalSources()[0].getTable();

            cn = dbServer().getEmissionsDatasource().getConnection();
            Statement stmt = cn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure 15 records come back...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName);
            rs.next();
            assertTrue("make sure there are 9 records in the summary results. ", rs.getInt(1) == 9);

            /*
            //make sure inv entry has the right numbers, there are NO locale specific measures for this entry...
            //check SCC = 2801500000 FIPS = 37119 inv entry
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2801500000' and fips = '37119' and plantid = '0001' and pointid='0001' and stackid='1' and segment='1'");
            rs.next();
            assertTrue("SCC = 2801500000 FIPS = 37119 reduction = 63", Math.abs(rs.getDouble("percent_reduction") - 63)/63 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37119 annual cost = 2.603046E+04", Math.abs(rs.getDouble("annual_cost") - 2.603046E+04)/2.603046E+04 < tolerance);
            assertTrue("SCC = 2801500000 FIPS = 37119 emis reduction = 6.113609E+00", Math.abs(rs.getDouble("emis_reduction") - 6.113609E+00)/6.113609E+00 < tolerance);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008010 FIPS = 37067 inv entry
            //also make sure the right control eff was used, this point already had a cm eff...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008010' and fips = '37067' and plantid = '00466' and pointid='001' and stackid='1' and segment='1'");
            rs.next();
            assertTrue("SCC = 2104008010 FIPS = 37067 reduction = 88.2", Math.abs(rs.getDouble("percent_reduction") - 88.2)/88.2 < tolerance);
            assertTrue("SCC = 2104008010 FIPS = 37067 annual cost = 3.699558E+04 ", Math.abs(rs.getDouble("annual_cost") - 3.699558E+04)/3.699558E+04 < tolerance);
            assertTrue("SCC = 2104008010 FIPS = 37067 emis reduction = 1.863666E+01", Math.abs(rs.getDouble("emis_reduction") - 1.863666E+01)/1.863666E+01 < tolerance);

            //make sure inv entry has the right numbers, this a locale (37015) specific measure for this entry...
            //check SCC = 2104008010 FIPS = 37111 inv entry
            //also make sure the right control eff was used, this point already had a more eff cm ...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName 
                    + " where scc = '2104008010' and fips = '37111' and plantid = '00778' and pointid='001' and stackid='2' and segment='1'");//00778 001 1 1 
            rs.next();
            assertTrue("SCC = 2104008010 FIPS = 37111 reduction = 99 " + rs.getDouble("percent_reduction"), Math.abs(rs.getDouble("percent_reduction") - 99)/99 < tolerance);
            assertTrue("SCC = 2104008010 FIPS = 37111 annual cost = 8.093505E+01", Math.abs(rs.getDouble("annual_cost") - 8.093505E+01)/8.093505E+01 < tolerance);
            assertTrue("SCC = 2104008010 FIPS = 37111 emis reduction = 4.576374E-02", Math.abs(rs.getDouble("emis_reduction") - 4.576374E-02)/4.576374E-02 < tolerance);
*/
            rs.close();

            //create the controlled inventory for this strategy run....
            ControlStrategyInventoryOutput output = new ControlStrategyInventoryOutput(user, strategy,
                    sessionFactory, dbServerFactory());
            output.create();
            //reload
            result = new ControlStrategyDAO().controlStrategyResult(strategy, sessionFactory.getSession());

            tableName2 = result.getControlledInventoryDataset().getName().replaceAll("ControlledInventory", "CSINVEN");
            
            cn2 = new EmfDatabaseSetup(config()).getDbServer().getEmissionsDatasource().getConnection();
            stmt = cn2.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            //make sure there are the corect amount of controlled inv records...
            rs = stmt.executeQuery("SELECT count(*) FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2);
            rs.next();
            assertTrue("make sure there are 206 records in the summary results. " + rs.getInt(1), rs.getInt(1) == 206);

            //make sure no inv info has been updated...
            rs = stmt.executeQuery("SELECT * FROM "+ EmfDbServer.EMF_EMISSIONS_SCHEMA + "." + tableName2 
                    + " where scc = '2294000270' and fips = '37067' and poll ='PM2.5'");
//            assertTrue("assigned different pollutant for same SCC", rs.getDouble("reff") == 0 && rs.getDouble("reff") == 0);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (cn != null) cn.close();
            if (cn2 != null) cn2.close();
            dropTables(strategy, inputDataset);
            dropTable(tableName2, dbServer().getEmissionsDatasource());
            int dsid = result.getControlledInventoryDataset().getId();
            dropTable("qasummarize_by_county_and_pollutant_dsid" + dsid + "_v0", dbServer().getEmissionsDatasource());
            dropTable("qasummarize_by_scc_and_pollutant_dsid" + dsid + "_v0", dbServer().getEmissionsDatasource());
            dropTable("qasummarize_by_pollutant_dsid" + dsid + "_v0", dbServer().getEmissionsDatasource());
            dropTable("qasummarize_by_us_state_and_pollutant_dsid" + dsid + "_v0", dbServer().getEmissionsDatasource());
            removeData();
        }
    }

//    public void testCountyImport() throws Exception {
//
//        try {
//            
//            File file = new File("test/data/cost/controlStrategy/070 Run Counties_OTC and West States Statewide2.csv");
//
//            CSCountyImporter task = new CSCountyImporter(file, new CSCountyFileFormat());//folder, fileNames, emfUser(), sessionFactory, dbServerFactory());
//            String[] fips = task.run();
//System.out.print(fips.length);
////            assertTrue("assigned different pollutant for same SCC", rs.getDouble("reff") == 0 && rs.getDouble("reff") == 0);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            //
//        }
//    }

    private void dropTables(ControlStrategy strategy, EmfDataset inputDataset) throws Exception {
        if (strategy != null)
            dropTable(detailResultDatasetTableName(strategy), dbServer().getEmissionsDatasource());
        dropQASummaryTables(inputDataset);
        ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(strategy, session);
        dropQASummaryTables((EmfDataset) result.getDetailedResultDataset());
    }

    private void dropQASummaryTables(EmfDataset dataset) throws Exception {
        dropTable("qasummarize_by_county_and_pollutant_dsid" + dataset.getId() + "_v0", dbServer().getEmissionsDatasource());
        dropTable("qasummarize_by_scc_and_pollutant_dsid" + dataset.getId() + "_v0", dbServer().getEmissionsDatasource());
        dropTable("qasummarize_by_pollutant_dsid" + dataset.getId() + "_v0", dbServer().getEmissionsDatasource());
        dropTable("qasummarize_by_us_state_and_pollutant_dsid" + dataset.getId() + "_v0", dbServer().getEmissionsDatasource());
    }

    private void removeData() throws Exception {
//        dropAll(Scc.class);
//        dropAll(QAStepResult.class);
//        dropAll(QAStep.class);
//        new PostgresDbUpdate().deleteAll("emf.input_datasets_control_strategies");
//        dropAll(ControlStrategyResult.class);
//        dropAll(EmfDataset.class);
//        dropAll(Dataset.class);
//        new PostgresDbUpdate().deleteAll("emf.control_strategy_measures");
//        dropAll(EfficiencyRecord.class);
//        dropAll(ControlMeasure.class);
//        dropAll(ControlStrategy.class);

        dropAll("Scc");
        dropAll("QAStepResult");
        dropAll("QAStep");
        new PostgresDbUpdate().deleteAll("emf.input_datasets_control_strategies");
        dropAll("ControlStrategyResult");
        dropAll(EmfDataset.class);
        dropAll(Dataset.class);
        new PostgresDbUpdate().deleteAll("emf.control_strategy_measures");
        dropAll("EfficiencyRecord");
        dropAll(ControlMeasure.class);
        dropAll(ControlStrategy.class);
    }

    private Pollutant pm10Pollutant() {
        return (Pollutant) load(Pollutant.class, "PM10");
    }

//    public void testImportControlMeasures() throws EmfException, Exception {
//        importControlMeasures();
//    }
//    
}
