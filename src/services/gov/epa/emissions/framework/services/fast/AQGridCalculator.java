package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AQGridCalculator {

    // private DbServerFactory dbServerFactory;
    //
    // private HibernateSessionFactory sessionFactory;
    //
    // private User user;
    //
    // private static Log LOG = LogFactory.getLog(AQGridCalculator.class);

    private EmfDataset invTable;

    private Version invTableVersion;

//    private Map<String, FastCMAQResult> fastCMAQResultMap = new HashMap<String, FastCMAQResult>();

    public AQGridCalculator() {
        //
    }

    public AQGridCalculator(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory, User user) {
        // this.dbServerFactory = dbServerFactory;
        // this.sessionFactory = sessionFactory;
    }

    private Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/EMF?autoReconnect=true", "emf", "emf");
        } catch (SQLException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return con;
    }

    public void go() throws IOException {
        // DataSource ds = null;
        Connection con = getConnection();
        Map<String, AQTransferCoefficient> transferCoefficientMap = new HashMap<String, AQTransferCoefficient>();// map
                                                                                                                 // key
                                                                                                                 // is
                                                                                                                 // sector
                                                                                                                 // + _
                                                                                                                 // +
                                                                                                                 // pollutant
        // Map<String, FastCMAQResult> fastCMAQResultMap
        // = new HashMap<String, FastCMAQResult>();//map key is sector + _ + cmaq pollutant

        // load invtable and version
        this.invTable = loadDataset("invtable_cap_hg");
        this.invTableVersion = version(invTable, 6);

        //
        // double[][] emiss = new double[37][46];
        // double[][] emiss2 = new double[37][46];
        double[][] emiss = new double[37][46];
        double[][] emiss2 = new double[37][46];

        System.out.println("default conc values for grid " + System.currentTimeMillis());
        for (int x = 1; x <= 36/* 45 */; x++) {
            for (int y = 1; y <= 45/* 36 */; y++) {
                emiss[x][y] = 0.0;
                emiss2[x][y] = 0.0;
            }
        }

        ResultSet rs = null;
        Statement statement = null;

        String sqlTemp = "";
        String sql2 = "";
        System.out.println("load up dataset pollutant column names into a HashMap " + System.currentTimeMillis());
        // getDatasetPollutantColumns
        EmfDataset dataset = loadDataset("2020ac_det_link3_nonpt_det_scc_cell_4DET1");
        sqlTemp = buildSQLSelectForSMOKEGriddedSCCRpt(dataset);
        if (sqlTemp.length() > 0)
            sql2 = sqlTemp;
        // emissionSourcePollutantMap.put(dataset.getName(), getDatasetPollutantColumns(dataset));
        dataset = loadDataset("2020ac_det_link_onroad_jan_scc_cell_4DET1");
        // emissionSourcePollutantMap.put(dataset.getName(), getDatasetPollutantColumns(dataset));
        sqlTemp = buildSQLSelectForSMOKEGriddedSCCRpt(dataset);
        if (sqlTemp.length() > 0)
            sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;
        dataset = loadDataset("2020ac_det_link3_lmb_nonroad_jan_scc_cell_4DET1");
        // emissionSourcePollutantMap.put(dataset.getName(), getDatasetPollutantColumns(dataset));
        sqlTemp = buildSQLSelectForSMOKEGriddedSCCRpt(dataset);
        if (sqlTemp.length() > 0)
            sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;
        dataset = loadDataset("2020ac_det_link3_lmb_alm_det_scc_cell_4DET1");
        // emissionSourcePollutantMap.put(dataset.getName(), getDatasetPollutantColumns(dataset));
        sqlTemp = buildSQLSelectForSMOKEGriddedSCCRpt(dataset);
        if (sqlTemp.length() > 0)
            sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;

        // add ORL Point inventory emissions...
        dataset = loadDataset("ptnonipm_hap2005v2_revised_24feb2009_v0");
        sqlTemp = buildSQLSelectForORLPointDataset(dataset, 0);
        if (sqlTemp.length() > 0)
            sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;
        dataset = loadDataset("ptnonipm_xportfrac_cap2005v2_20nov2008_revised_20jan2009_v0");
        sqlTemp = buildSQLSelectForORLPointDataset(dataset, 0);
        if (sqlTemp.length() > 0)
            sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;
        dataset = loadDataset("ptnonipm_offshore_oil_cap2005v2_20nov2008_20nov2008_v0");
        sqlTemp = buildSQLSelectForORLPointDataset(dataset, 0);
        if (sqlTemp.length() > 0)
            sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;
        dataset = loadDataset("canada_point_uog_2006_orl_02mar2009_v0");
        sqlTemp = buildSQLSelectForORLPointDataset(dataset, 0);
        if (sqlTemp.length() > 0)
            sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;
        dataset = loadDataset("canada_point_cb5_2006_orl_10mar2009_v0");
        sqlTemp = buildSQLSelectForORLPointDataset(dataset, 0);
        if (sqlTemp.length() > 0)
            sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;
        dataset = loadDataset("canada_point_2006_orl_09mar2009_v2");
        sqlTemp = buildSQLSelectForORLPointDataset(dataset, 0);
        if (sqlTemp.length() > 0)
            sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;
        dataset = loadDataset("ptipm_cap2005v2_revised12mar2009_14may2009_v3");
        sqlTemp = buildSQLSelectForORLPointDataset(dataset, 0);
        if (sqlTemp.length() > 0)
            sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;
        dataset = loadDataset("ptipm_hap2005v2_allHAPs_revised12mar2009_12mar2009_v0");
        sqlTemp = buildSQLSelectForORLPointDataset(dataset, 0);
        if (sqlTemp.length() > 0)
            sql2 += (sql2.length() > 0 ? " union all " : "") + sqlTemp;

        sql2 = "select sector, poll, sum(emis) as emis, x, y \n" + "from ( \n" + sql2;
        sql2 += ") summary \n" + "group by sector, poll, x, y \n" + "order by sector, poll, x, y;";

        System.out.println("sql for all datasets sql = " + sql2);

        // System.out.println("finished loading up dataset pollutant column names into a HashMap " +
        // System.currentTimeMillis());
        File outputFile2 = new File("D:\\My Documents\\karen\\sql.csv");
        FileWriter writer2 = null;
        outputFile2.createNewFile();
        writer2 = new FileWriter(outputFile2);
        writer2.write(sql2);
        writer2.flush();
        writer2.close();

        // if (1 == 1) {
        // throw new EmfException("stop the function");
        // }
        // Name
        // 2020ac_det_link3_nonpt_det_scc_cell_4DET1
        // 2020ac_det_link_onroad_jan_scc_cell_4DET1
        // 2020ac_det_link_onroad_jul_scc_cell_4DET1
        // 2020ac_det_link3_lmb_nonroad_jul_scc_cell_4DET1
        // 2020ac_det_link3_lmb_nonroad_jan_scc_cell_4DET1
        // 2020ac_det_link3_lmb_alm_det_scc_cell_4DET1

        System.out.println("load up transfer coefficients into a HashMap " + System.currentTimeMillis());
        String query = "select sector, pollutant, b1, b2 from emissions.DS_transfer_coefficients_1252137419;";
        try {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement
                    .executeQuery("select sector, pollutant, b1, b2 from emissions.DS_transfer_coefficients_1252137419;");
            while (rs.next()) {
                String sector = rs.getString(1).toLowerCase();
                String pollutant = rs.getString(2).toLowerCase();
                transferCoefficientMap.put(sector + "_" + pollutant, new AQTransferCoefficient(sector, pollutant, rs
                        .getDouble(3), rs.getDouble(4)));
            }
            rs.close();
            rs = null;
            statement.close();
            statement = null;
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /**/
                }
                rs = null;
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) { /**/
                }
                statement = null;
            }
        }
        System.out.println("finished loading transfer coefficients into a HashMap " + System.currentTimeMillis());

        List<FastCMAQResult> results = new ArrayList<FastCMAQResult>();

        System.out.println("load up conc values for grid " + System.currentTimeMillis());
        query = "select sector, poll, sum(emis) as emis, x, y \n"
                + "from ( \n"
                + "--nonpt \n"
                + "/*union all */select 'nonpt' as sector, 'NOX' as poll, sum(nox) as emis, x_cell as x, y_cell as y from emissions.DS_2020ac_det_link3_nonpt_det_scc_cell_4DET1_small_1755731222 where dataset_id = 5306 and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(nox,0.0) <> 0.0 group by x_cell, y_cell \n"
                + "union all select 'nonpt' as sector, 'SO2' as poll, sum(so2) as emis, x_cell as x, y_cell as y from emissions.DS_2020ac_det_link3_nonpt_det_scc_cell_4DET1_small_1755731222 where dataset_id = 5306 and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(so2,0.0) <> 0.0 group by x_cell, y_cell \n"
                + " \n"
                + "--alm\n"
                + "union all select 'alm' as sector, 'NOX' as poll, sum(nox) as emis, x_cell as x, y_cell as y from emissions.DS_2020ac_det_link3_lmb_alm_det_scc_cell_4DET1_97387529 where dataset_id = 5298 and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(nox,0.0) <> 0.0 group by x_cell, y_cell \n"
                + "union all select 'alm' as sector, 'SO2' as poll, sum(so2) as emis, x_cell as x, y_cell as y from emissions.DS_2020ac_det_link3_lmb_alm_det_scc_cell_4DET1_97387529 where dataset_id = 5298 and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(so2,0.0) <> 0.0 group by x_cell, y_cell \n"
                + "--nonroad jan \n"
                + "union all select 'nonroad' as sector, 'NOX' as poll, sum(31 * nox) as emis, x_cell as x, y_cell as y from emissions.DS_2020ac_det_link3_lmb_nonroad_jan_scc_cell_4DET1_1110835211 where dataset_id = 5301 and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(nox,0.0) <> 0.0 group by x_cell, y_cell \n"
                + "union all select 'nonroad' as sector, 'SO2' as poll, sum(31 * so2) as emis, x_cell as x, y_cell as y from emissions.DS_2020ac_det_link3_lmb_nonroad_jan_scc_cell_4DET1_1110835211 where dataset_id = 5301 and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(so2,0.0) <> 0.0 group by x_cell, y_cell \n"
                + "--nonroad jul \n"
                + "union all select 'nonroad' as sector, 'NOX' as poll, sum(31 * nox) as emis, x_cell as x, y_cell as y from emissions.DS_2020ac_det_link3_lmb_nonroad_jan_scc_cell_4DET1_1110835211 where dataset_id = 5302 and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(nox,0.0) <> 0.0 group by x_cell, y_cell \n"
                + "union all select 'nonroad' as sector, 'SO2' as poll, sum(31 * so2) as emis, x_cell as x, y_cell as y from emissions.DS_2020ac_det_link3_lmb_nonroad_jan_scc_cell_4DET1_1110835211 where dataset_id = 5302 and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(so2,0.0) <> 0.0 group by x_cell, y_cell \n"
                + "--onroad jan \n"
                + "union all select 'onroad' as sector, 'NOX' as poll, sum(31 * nox) as emis, x_cell as x, y_cell as y from emissions.DS_2020ac_det_link_onroad_jan_scc_cell_4DET1_233281623 where dataset_id = 5304 and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(nox,0.0) <> 0.0 group by x_cell, y_cell \n"
                + "union all select 'onroad' as sector, 'SO2' as poll, sum(31 * so2) as emis, x_cell as x, y_cell as y from emissions.DS_2020ac_det_link_onroad_jan_scc_cell_4DET1_233281623 where dataset_id = 5304 and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(so2,0.0) <> 0.0 group by x_cell, y_cell \n"
                + "--onroad jul \n"
                + "union all select 'onroad' as sector, 'NOX' as poll, sum(31 * nox) as emis, x_cell as x, y_cell as y from emissions.DS_2020ac_det_link_onroad_jan_scc_cell_4DET1_233281623 where dataset_id = 5305 and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(nox,0.0) <> 0.0 group by x_cell, y_cell \n"
                + "union all select 'onroad' as sector, 'SO2' as poll, sum(31 * so2) as emis, x_cell as x, y_cell as y from emissions.DS_2020ac_det_link_onroad_jan_scc_cell_4DET1_233281623 where dataset_id = 5305 and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(so2,0.0) <> 0.0 group by x_cell, y_cell \n"
                + ") summary \n" + "group by sector, poll, x, y \n" + "order by sector, poll, x, y;";

        query = "select distinct fo.sector, fo.cmaq_pollutant, fo.inventory_pollutant, fo.x, fo.y, fo.factor, fo.emis, fsm.transfer_coeff from test.fast_output2 fo inner join emissions.DS_fast_species_mapping_1604915993 fsm on fsm.sector = fo.sector and fsm.cmaq_pollutant = fo.cmaq_pollutant and fsm.inventory_pollutant = fo.inventory_pollutant order by fo.sector, fo.cmaq_pollutant, fo.inventory_pollutant;";
        try {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(query);
            int y, x;
            double emissionValue;
            float factor;
            String sector = "";
            String cmaqPollutant = "";
            String inventoryPollutant = "";
            String prevSector = "";
            String prevCmaqPollutant = "";
            String prevInventoryPollutant = "";
            String transferCoeff = "";
            FastCMAQResult fastCMAQResult = null;
            // FastCMAQInventoryPollutantResult fastCMAQInventoryPollutantResult;

            double[][] emission = new double[37][46];
            FastInventoryPollutantResult result = null;
            while (rs.next()) {
                sector = rs.getString(1);
                cmaqPollutant = rs.getString(2);
                inventoryPollutant = rs.getString(3);
                x = rs.getInt(4);
                y = rs.getInt(5);
                factor = rs.getFloat(6);
                emissionValue = rs.getDouble(7);
                transferCoeff = rs.getString(8);
                // if (fastCMAQResultMap.containsKey(sector + "_" + cmaqPollutant)) {
                // fastCMAQResult = fastCMAQResultMap.get(sector + "_" + cmaqPollutant);
                // } else {
                // fastCMAQResult = new FastCMAQResult(sector, cmaqPollutant);
                // // fastCMAQInventoryPollutantResult = new FastCMAQInventoryPollutantResult();
                // }

                if (!sector.equals(prevSector) || !cmaqPollutant.equals(prevCmaqPollutant)) {
                    if (result != null) {
                        result.setEmission(emission);
                        fastCMAQResult.addCmaqInventoryPollutantResults(result);
                        // fastCMAQResultMap.put(sector + "_" + cmaqPollutant, fastCMAQResult);
                        results.add(fastCMAQResult);
                    }
                    emission = new double[36][45];
                    fastCMAQResult = new FastCMAQResult(sector, cmaqPollutant);
                    result = new FastInventoryPollutantResult(inventoryPollutant, factor, transferCoeff, 36, 45);
                } else if (!inventoryPollutant.equals(prevInventoryPollutant)) {
                    if (result != null) {
                        result.setEmission(emission);
                        fastCMAQResult.addCmaqInventoryPollutantResults(result);
                    }
                    emission = new double[36][45];
                    result = new FastInventoryPollutantResult(inventoryPollutant, factor, transferCoeff, 36, 45);
                }

                prevSector = sector;
                prevCmaqPollutant = cmaqPollutant;
                prevInventoryPollutant = inventoryPollutant;
                emission[x - 1][y - 1] = emissionValue;

                // fastCMAQResultMap.put(sector + "_" + cmaqPollutant, fastCMAQResult);
            }
            // get last item in there too.
            result.setEmission(emission);
            fastCMAQResult.addCmaqInventoryPollutantResults(result);
            // fastCMAQResultMap.put(sector + "_" + cmaqPollutant, fastCMAQResult);
            results.add(fastCMAQResult);
            rs.close();
            rs = null;
            statement.close();
            statement = null;
            con.close();
            con = null;
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /**/
                }
                rs = null;
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) { /**/
                }
                statement = null;
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /**/
                }
                con = null;
            }
        }
        System.out.println("finished loading up conc values for grid " + System.currentTimeMillis());

        for (FastCMAQResult cMAQResult : results) {
            String sector = cMAQResult.getSector();
            System.out.println("result sector = " + sector + ", pollutant = " + cMAQResult.getCmaqPollutant());
            for (FastInventoryPollutantResult result : cMAQResult.getCmaqInventoryPollutantResults()) {
                double[][] emission = result.getEmission();
                double[][] airQuality = new double[36][45];

                if (sector.equals("ptnonipm") || sector.equals("ptipm") || sector.equals("point") || sector.equals("othpt"))
                    sector = "point";
                else
                    sector = "other";

                System.out.println("start to calc affect for each cell on every other cell "
                        + System.currentTimeMillis());

                AQTransferCoefficient transferCoefficient = transferCoefficientMap.get(sector.toLowerCase() + "_"
                        + result.getTranferCoefficient().toLowerCase());
                double beta1 = transferCoefficient.getBeta1();
                double beta2 = transferCoefficient.getBeta2();

                for (int x = 1; x <= 36/* 45 */; x++) {
                    for (int y = 1; y <= 45/* 36 */; y++) {
                        for (int xx = 1; xx <= 36/* 45 */; xx++) {
                            for (int yy = 1; yy <= 45/* 36 */; yy++) {
                                airQuality[x - 1][y - 1] = airQuality[x - 1][y - 1]
                                        + beta1 /* transferCoefficient.getBeta1() 0.000108 0.000008470 */
                                        * 365
                                        * emission[xx - 1][yy - 1]
                                        / (1 + Math.exp(Math.pow(Math.pow(Math.pow(Math
                                                .abs((yy * 4000.0 + 1044.0 + 0.5 * 4000.0) / 1000
                                                        - (y * 4000.0 + 1044.0 + 0.5 * 4000.0) / 1000), 2.0)
                                                + Math.pow(Math.abs((xx * 4000.0 + 252.0 + 0.5 * 4000.0) / 1000
                                                        - (x * 4000.0 + 252.0 + 0.5 * 4000.0) / 1000), 2.0), 0.5),
                                                beta2 /* transferCoefficient.getBeta2() (0.3084*2) 0.2170 */)));
                            }
                        }
                    }
                }
                result.setAirQuality(airQuality);
            }
            System.out.println("finished calc affect for each cell on every other cell " + System.currentTimeMillis());
        }

        File outputFile = new File("D:\\My Documents\\karen\\detailed_aq.csv");
        FileWriter writer = null;
        outputFile.createNewFile();
        writer = new FileWriter(outputFile);

        writer.write("sector,cmaq_pollutant,inventory_pollutant,x,y,emission,airquality");

        for (FastCMAQResult cmaqResult : results) {
            String sector = cmaqResult.getSector();
            String pollutant = cmaqResult.getCmaqPollutant();
            System.out.println("result sector = " + cmaqResult.getSector() + ", pollutant = " + pollutant);
            for (FastInventoryPollutantResult result : cmaqResult.getCmaqInventoryPollutantResults()) {
                double[][] emission = result.getEmission();
                double[][] airQuality = result.getAirQuality();
                String invetoryPollutant = result.getPollutant();
                for (int x = 1; x <= 36; x++) {
                    for (int y = 1; y <= 45; y++) {
                        writer.write("\n" + sector + "," + pollutant + "," + invetoryPollutant + "," + x + "," + y + "," + emission[x - 1][y - 1] + ","
                                + airQuality[x - 1][y - 1]);
                    }
                    writer.flush();
                }
            }
        }
        writer.close();

        File outputFile21 = new File("D:\\My Documents\\karen\\aq.csv");
        FileWriter writer21 = null;
        outputFile21.createNewFile();
        writer21 = new FileWriter(outputFile21);

        writer21.write("sector,pollutant,x,y,emission,airquality");

        for (FastCMAQResult cmaqResult : results) {
            String sector = cmaqResult.getSector();
            String pollutant = cmaqResult.getCmaqPollutant();
            System.out.println("result sector = " + cmaqResult.getSector() + ", pollutant = " + pollutant);
            double[][] emission = cmaqResult.getEmission();
            double[][] airQuality = cmaqResult.getAirQuality();
            for (int x = 1; x <= 36; x++) {
                for (int y = 1; y <= 45; y++) {
                    writer21.write("\n" + sector + "," + pollutant + "," + x + "," + y + "," + emission[x - 1][y - 1] + ","
                            + airQuality[x - 1][y - 1]);
                }
                writer21.flush();
            }
        }
        writer21.close();

        // System.out.println("start to calc affect for each cell on every other cell " + System.currentTimeMillis());
        //        
        // AQTransferCoefficient transferCoefficient = transferCoefficientMap.get("point_nox");
        // double beta1 = transferCoefficient.getBeta1();
        // double beta2 = transferCoefficient.getBeta2();
        //        
        // for (int x = 1; x <= 36/*45*/; x++) {
        // for (int y = 1; y <= 45/*36*/; y++) {
        // // FOR i IN 1..5 LOOP
        // // FOR j IN 1..5 LOOP
        // // raise notice '%', 'now calculating cell i = ' || i || ' j = ' || j;
        // for (int xx = 1; xx <= 36/*45*/; xx++) {
        // for (int yy = 1; yy <= 45/*36*/; yy++) {
        // // FOR ii IN 1..36 LOOP
        // // FOR jj IN 1..45 LOOP
        // emiss[x][y] = emiss[x][y]
        // + beta1 /*transferCoefficient.getBeta1() 0.000108 0.000008470*/
        // * 365
        // * emiss2[xx][yy]
        // / (1 + Math.exp(Math.pow(Math.pow(Math.pow(Math
        // .abs((yy * 4000.0 + 1044.0 + 0.5 * 4000.0) / 1000
        // - (y * 4000.0 + 1044.0 + 0.5 * 4000.0) / 1000), 2.0)
        // + Math.pow(Math.abs((xx * 4000.0 + 252.0 + 0.5 * 4000.0) / 1000
        // - (x * 4000.0 + 252.0 + 0.5 * 4000.0) / 1000), 2.0), 0.5), beta2 /*transferCoefficient.getBeta2() (0.3084*2)
        // 0.2170*/)));
        // // emiss[i][j] := emiss[i][j]
        // // + 0.000008470 * 365 * emiss2[ii][jj]
        // // -- emiss[ii][jj] := emiss[ii][jj]
        // // -- + 0.000008470 * 365 * emiss2[i][j]
        // // / (1
        // // +
        // // case when sqrt((abs((ii*4000.0 + 1044.0 + 0.5*4000.0)/1000 - (i*4000.0 + 1044.0 +
        // // 0.5*4000.0)/1000))^2 + (abs((jj*4000.0 + 252.0 + 0.5*4000.0)/1000 - (j*4000.0 + 252.0 +
        // // 0.5*4000.0)/1000))^2) = 0.0 then 1
        // // else exp(sqrt((abs((ii*4000.0 + 1044.0 + 0.5*4000.0)/1000 - (i*4000.0 + 1044.0 +
        // // 0.5*4000.0)/1000))^2 + (abs((jj*4000.0 + 252.0 + 0.5*4000.0)/1000 - (j*4000.0 + 252.0 +
        // // 0.5*4000.0)/1000))^2)^0.2170)
        // // end
        // // );
        // }
        // // END LOOP;
        // }
        // // END LOOP;
        // //
        // }
        // // END LOOP;
        // }
        // System.out.println("finished calc affect for each cell on every other cell " + System.currentTimeMillis());
        // // END LOOP;
        // //
        // // -- stuff valus into table
        // // FOR i IN 1..36 LOOP
        // for (int x = 1; x <= 36/*45*/; x++) {
        // for (int y = 1; y <= 45/*36*/; y++) {
        // // FOR j IN 1..45 LOOP
        // System.out.println("cell emis[" + x + "," + y + "] = " + emiss2[x][y] + ", aq[" + x + "," + y + "] = " +
        // emiss[x][y]);
        // // insert into public.test
        // // select i, j, emiss[i][j];
        // // -- raise notice '%', 'calculated cell i = ' || i || ' j = ' || j || ' ' || emiss[i][j] || ' ' ||
        // // clock_timestamp();
        // // END LOOP;
        // }
        // // END LOOP;
        // }
    }

    private String buildSQLSelectForSMOKEGriddedSCCRpt(EmfDataset griddedSCCDataset) throws EmfException {
        String sql = "";
        Sector sector = griddedSCCDataset.getSectors()[0];
        String tableName = griddedSCCDataset.getInternalSources()[0].getTable();
        int datasetId = griddedSCCDataset.getId();
        if (sector == null)
            throw new EmfException("Dataset " + griddedSCCDataset.getName() + " is missing the sector.");
        boolean isMonthly = sector.getName().equals("nonroad") || sector.getName().equals("onroad");
        List<String> pollutantColumns = getDatasetPollutantColumns(griddedSCCDataset);
        for (int i = 0; i < pollutantColumns.size(); i++) {
            String pollutant = pollutantColumns.get(i);
            sql += (i > 0 ? " union all " : "") + "select '" + sector.getName().replace("'", "''")
                    + "' as sector, scc, '" + pollutant.toUpperCase() + "' as poll, sum("
                    + (!isMonthly ? "" : "365 * ") + pollutant + ") as emis, x_cell as x, y_cell as y from emissions."
                    + tableName + " where dataset_id = " + datasetId
                    + " and x_cell between 1 and 36 and y_cell between 1 and 45 and coalesce(" + pollutant
                    + ",0.0) <> 0.0 group by x_cell, y_cell, scc \n";
        }
        return sql;
    }

    protected String buildSQLSelectForORLPointDataset(EmfDataset orlPointDataset, int versionNumber)
            throws EmfException {
        String sql = "";
        Sector sector = orlPointDataset.getSectors()[0];
        String tableName = orlPointDataset.getInternalSources()[0].getTable();
        String invTableTableName = invTable.getInternalSources()[0].getTable();
        if (sector == null)
            throw new EmfException("Dataset " + orlPointDataset.getName() + " is missing the sector.");
        VersionedQuery versionedQuery = new VersionedQuery(version(orlPointDataset, versionNumber), "inv");
        VersionedQuery invTableVersionedQuery = new VersionedQuery(invTableVersion, "invtable");
        sql = "select '"
                + sector.getName().replace("'", "''")
                + "' as sector, inv.scc, invtable.name as poll, sum(invtable.factor * inv.ann_emis) as emis, ceiling((public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - 1044000.0) / 4000.0 + 1) as x, ceiling((public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - 252000.0) / 4000.0 + 1) as y from emissions."
                + tableName + " inv inner join emissions." + invTableTableName
                + " invtable on invtable.cas = inv.poll where coalesce(inv.ann_emis,0.0) <> 0.0 and "
                + versionedQuery.query() + " and " + invTableVersionedQuery.query();
        sql += " and ceiling((public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - 1044000.0) / 4000.0 + 1) between 1 and 36 ";
        sql += " and ceiling((public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - 252000.0) / 4000.0 + 1) between 1 and 45 ";
        sql += " group by ceiling((public.ST_X(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - 1044000.0) / 4000.0 + 1), ceiling((public.ST_Y(public.ST_Transform(public.GeomFromEWKT('SRID=104308;POINT(' || inv.xloc || ' ' || inv.yloc || ')'),104307)) - 252000.0) / 4000.0 + 1), inv.scc, invtable.name \n";

        return sql;
    }

    private EmfDataset loadDataset(String datasetName) throws EmfException {
        EmfDataset dataset = new EmfDataset();
        Connection con = getConnection();
        ResultSet rs = null;
        Statement statement = null;
        try {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement
                    .executeQuery("select datasets.id, datasets.name, internal_sources.table_name, sectors.name from emf.datasets inner join emf.internal_sources on internal_sources.dataset_id = datasets.id left outer join emf.datasets_sectors on datasets_sectors.dataset_id = datasets.id left outer join emf.sectors on sectors.id = datasets_sectors.sector_id where datasets.name = '"
                            + datasetName + "' limit 1");
            while (rs.next()) {
                dataset.setId(rs.getInt(1));
                dataset.setName(rs.getString(2));
                InternalSource internalSource = new InternalSource();
                internalSource.setTable(rs.getString(3));
                dataset.setInternalSources(new InternalSource[] { internalSource });
                Sector sector = new Sector();
                sector.setName(rs.getString(4));
                dataset.setSectors(new Sector[] { sector });
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query\n" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /**/
                }
                rs = null;
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) { /**/
                }
                statement = null;
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /**/
                }
                con = null;
            }
        }

        return dataset;
    }

    private List<String> getDatasetPollutantColumns(EmfDataset dataset) throws EmfException {
        List<String> pollutantColumnList = new ArrayList<String>();
        Connection con = getConnection();
        ResultSet rs;
        ResultSetMetaData md;
        Statement statement = null;
        try {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery("select * from emissions." + dataset.getInternalSources()[0].getTable()
                    + " where 1 = 0;");
            md = rs.getMetaData();
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }

        try {
            for (int i = 1; i < md.getColumnCount(); i++) {
                String columnName = md.getColumnName(i);
                int columnType = md.getColumnType(i);
                // ignore these columns, we really just want the pollutant/specie columns
                if (!columnName.equalsIgnoreCase("x_cell") && !columnName.equalsIgnoreCase("y_cell")
                        && !columnName.equalsIgnoreCase("source_id") && !columnName.equalsIgnoreCase("region")
                        && !columnName.equalsIgnoreCase("scc") && !columnName.equalsIgnoreCase("scc2")
                        && !columnName.equalsIgnoreCase("record_id") && !columnName.equalsIgnoreCase("dataset_id")
                        && !columnName.equalsIgnoreCase("version") && !columnName.equalsIgnoreCase("delete_versions")
                        && !columnName.equalsIgnoreCase("road") && !columnName.equalsIgnoreCase("link")
                        && !columnName.equalsIgnoreCase("veh_type") && columnType == Types.DOUBLE)
                    pollutantColumnList.add(columnName);
            }
        } catch (SQLException e) {
            //
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /**/
                }
                rs = null;
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) { /**/
                }
                statement = null;
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /**/
                }
                con = null;
            }
        }

        return pollutantColumnList;

    }

    protected Version version(EmfDataset inputDataset, int datasetVersion) throws EmfException {
        Version version = new Version();
        Connection con = getConnection();
        ResultSet rs;
        Statement statement = null;
        try {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement
                    .executeQuery("select \"version\", \"name\", path, final_version from emissions.versions where dataset_id = "
                            + inputDataset.getId() + " and \"version\" = " + datasetVersion + ";");
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }

        try {
            while (rs.next()) {
                version.setDatasetId(inputDataset.getId());
                version.setVersion(datasetVersion);
                version.setName(rs.getString(2));
                version.setPath(rs.getString(3));
            }
        } catch (SQLException e) {
            //
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /**/
                }
                rs = null;
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) { /**/
                }
                statement = null;
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /**/
                }
                con = null;
            }
        }

        return version;

        // Session session = sessionFactory.getSession();
        // try {
        // Versions versions = new Versions();
        // return versions.get(inputDataset.getId(), datasetVersion, session);
        // } finally {
        // session.close();
        // }
    }

    public static void main(String args[]) {
        AQGridCalculator aq = new AQGridCalculator();
        try {
            aq.go();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    public class FastCMAQResult {

        private String sector;

        private String cmaqPollutant;

        private FastInventoryPollutantResult[] inventoryPollutantResults = new FastInventoryPollutantResult[] {};

        public FastCMAQResult(String sector, String cmaqPollutant) {
            super();
            this.sector = sector;
            this.cmaqPollutant = cmaqPollutant;
        }

        public FastCMAQResult() {
            // NOTE Auto-generated constructor stub
        }

        // private Map<String, FastCMAQInventoryPollutantResult> cmaqInventoryPollutantResults;
        public void setSector(String sector) {
            this.sector = sector;
        }

        public String getSector() {
            return sector;
        }

        public void setCmaqPollutant(String cmaqPollutant) {
            this.cmaqPollutant = cmaqPollutant;
        }

        public String getCmaqPollutant() {
            return cmaqPollutant;
        }

        public void addCmaqInventoryPollutantResults(FastInventoryPollutantResult inventoryPollutantResult) {
            List<FastInventoryPollutantResult> inventoryPollutantResultList = new ArrayList<FastInventoryPollutantResult>();
            inventoryPollutantResultList.addAll(Arrays.asList(inventoryPollutantResults));
            inventoryPollutantResultList.add(inventoryPollutantResult);
            this.inventoryPollutantResults = inventoryPollutantResultList.toArray(new FastInventoryPollutantResult[0]);
        }

        public void setCmaqInventoryPollutantResults(FastInventoryPollutantResult[] cmaqInventoryPollutantResults) {
            this.inventoryPollutantResults = cmaqInventoryPollutantResults;
        }

        public FastInventoryPollutantResult[] getCmaqInventoryPollutantResults() {
            return inventoryPollutantResults;
        }

        public double[][] getEmission() {
            double[][] emission = new double[36][45];
            for (FastInventoryPollutantResult result : inventoryPollutantResults) {
                double[][] resultEmission = result.getEmission();
                for (int x = 1; x <= 36; x++) {
                    for (int y = 1; y <= 45; y++) {
                        emission[x - 1][y - 1] += resultEmission[x - 1][y - 1];
                    }
                }
              
            }
            return emission;
        }

        public double[][] getAirQuality() {
            double[][] airQuality = new double[36][45];
            for (FastInventoryPollutantResult result : inventoryPollutantResults) {
                double[][] resultAirQuality = result.getAirQuality();
                for (int x = 1; x <= 36; x++) {
                    for (int y = 1; y <= 45; y++) {
                        airQuality[x - 1][y - 1] += resultAirQuality[x - 1][y - 1];
                    }
                }
              
            }
            return airQuality;
        }

    }

    public class FastCMAQInventoryPollutantResult {

        private FastInventoryPollutantResult inventoryPollutantResult;

        private float adjustmentFactor;

        private String tranferCoefficient;

        public void setAdjustmentFactor(float adjustmentFactor) {
            this.adjustmentFactor = adjustmentFactor;
        }

        public float getAdjustmentFactor() {
            return adjustmentFactor;
        }

        public void setInventoryPollutantResult(FastInventoryPollutantResult inventoryPollutantResult) {
            this.inventoryPollutantResult = inventoryPollutantResult;
        }

        public FastInventoryPollutantResult getInventoryPollutantResult() {
            return inventoryPollutantResult;
        }

        public void setTranferCoefficient(String tranferCoefficient) {
            this.tranferCoefficient = tranferCoefficient;
        }

        public String getTranferCoefficient() {
            return tranferCoefficient;
        }

    }

    public class FastInventoryPollutantResult {

        private String inventoryPollutant;

        private double[][] emission;

        private double[][] airQuality;

        private float adjustmentFactor;

        private String tranferCoefficient;

        public FastInventoryPollutantResult(String inventoryPollutant, float adjustmentFactor,
                String tranferCoefficient, int numerOfXGridCells, int numerOfYGridCells) {
            this.inventoryPollutant = inventoryPollutant;
            this.adjustmentFactor = adjustmentFactor;
            this.tranferCoefficient = tranferCoefficient;
            this.emission = new double[numerOfXGridCells][numerOfYGridCells];
            this.airQuality = new double[numerOfXGridCells][numerOfYGridCells];
        }

        public void setPollutant(String inventoryPollutant) {
            this.inventoryPollutant = inventoryPollutant;
        }

        public String getPollutant() {
            return inventoryPollutant;
        }

        public void setEmission(double[][] emission) {
            this.emission = emission;
        }

        public double[][] getEmission() {
            return emission;
        }

        public void setAirQuality(double[][] airQuality) {
            this.airQuality = airQuality;
        }

        public double[][] getAirQuality() {
            return airQuality;
        }

        public void setAdjustmentFactor(float adjustmentFactor) {
            this.adjustmentFactor = adjustmentFactor;
        }

        public float getAdjustmentFactor() {
            return adjustmentFactor;
        }

        public void setTranferCoefficient(String tranferCoefficient) {
            this.tranferCoefficient = tranferCoefficient;
        }

        public String getTranferCoefficient() {
            return tranferCoefficient;
        }
    }

    public class AQTransferCoefficient {

        private String sector;

        private String pollutant;

        private double beta1;

        private double beta2;

        public AQTransferCoefficient() {
            //
        }

        public AQTransferCoefficient(String sector, String pollutant, double beta1, double beta2) {
            this();
            this.sector = sector;
            this.pollutant = pollutant;
            this.beta1 = beta1;
            this.beta2 = beta2;
        }

        public void setPollutant(String pollutant) {
            this.pollutant = pollutant;
        }

        public String getPollutant() {
            return pollutant;
        }

        public void setSector(String sector) {
            this.sector = sector;
        }

        public String getSector() {
            return sector;
        }

        public void setBeta1(double beta1) {
            this.beta1 = beta1;
        }

        public double getBeta1() {
            return beta1;
        }

        public void setBeta2(double beta2) {
            this.beta2 = beta2;
        }

        public double getBeta2() {
            return beta2;
        }
    }

}
