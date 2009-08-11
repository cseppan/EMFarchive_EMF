package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.Session;

public class SQLECControlScenarioQuery {
    
    private QAStep qaStep;

    private String tableName;

    private HibernateSessionFactory sessionFactory;
    
    private DbServer dbServer;
    
    private String emissionDatasourceName;
    
    private DatasetDAO dao;

    public static final String invTag = "-inv";

    public static final String gsrefTag = "-gsref";

    public static final String gsproTag = "-gspro";

    public static final String detailedResultTag = "-detailed_result";

    public SQLECControlScenarioQuery(HibernateSessionFactory sessionFactory, DbServer dbServer, 
            String emissioDatasourceName, String tableName, 
            QAStep qaStep) {
        this.qaStep = qaStep;
        this.tableName = tableName;
        this.sessionFactory = sessionFactory;
        this.emissionDatasourceName = emissioDatasourceName;
        this.dao = new DatasetDAO();
        this.dbServer = dbServer;
    }

    private String[] parseSwitchArguments(String programSwitches, int beginIndex, int endIndex) {
        List<String> inventoryList = new ArrayList<String>();
        String value = "";
        String valuesString = "";
        
        valuesString = programSwitches.substring(beginIndex, endIndex);
        StringTokenizer tokenizer2 = new StringTokenizer(valuesString, "\n");
        tokenizer2.nextToken(); // skip the flag

        while (tokenizer2.hasMoreTokens()) {
            value = tokenizer2.nextToken().trim();
            if (!value.isEmpty())
                inventoryList.add(value);
        }
        return inventoryList.toArray(new String[0]);
    }
    
//    private String parseSummaryType(String programSwitches, int beginIndex, int endIndex) {
//        String value = "";
//        String valuesString = "";
//        
//        valuesString = programSwitches.substring(beginIndex, endIndex);
//        StringTokenizer tokenizer2 = new StringTokenizer(valuesString, "\n");
//        tokenizer2.nextToken(); // skip the switch tag
//
//        //get only the first value, that is not empty...
//        while (tokenizer2.hasMoreTokens()) {
//            value = tokenizer2.nextToken().trim();
//            if (!value.isEmpty()) 
//                break;
//        }
//        return value;
//    }

    public String createCompareQuery() throws EmfException {
        String sql = "";
        String programArguments = qaStep.getProgramArguments();
        
        int gsproIndex = programArguments.indexOf(gsproTag);
        int invIndex = programArguments.indexOf(invTag);
        int gsrefIndex = programArguments.indexOf(gsrefTag);
        int detailedResultIndex = programArguments.indexOf(detailedResultTag);

        String[] gsproNames = null;
        String[] gsrefNames = null; 
        String inventoryName = null;
        String detailedResultName = null;

        String[] arguments;
        String version;
        String table;


        if (invIndex != -1) {
            arguments = parseSwitchArguments(programArguments, invIndex, programArguments.indexOf("\n-", invIndex) != -1 ? programArguments.indexOf("\n-", invIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) inventoryName = arguments[0];
        }
        if (gsproIndex != -1) {
            arguments = parseSwitchArguments(programArguments, gsproIndex, programArguments.indexOf("\n-", gsproIndex) != -1 ? programArguments.indexOf("\n-", gsproIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) gsproNames = arguments;
        }
        if (gsrefIndex != -1) {
            arguments = parseSwitchArguments(programArguments, gsrefIndex, programArguments.indexOf("\n-", gsrefIndex) != -1 ? programArguments.indexOf("\n-", gsrefIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) gsrefNames = arguments;
        }
        if (detailedResultIndex != -1) {
            arguments = parseSwitchArguments(programArguments, detailedResultIndex, programArguments.indexOf("\n-", detailedResultIndex) != -1 ? programArguments.indexOf("\n-", detailedResultIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) detailedResultName = arguments[0];
        }
        
        //validate everything has been specified...
        String errors = "";
        //make sure all dataset were specified, look at the names
        if (gsproNames == null) {
            errors = "Missing " + DatasetType.chemicalSpeciationProfilesGSPRO + " datasets. ";
        }
        if (gsrefNames == null) {
            errors += "Missing " + DatasetType.chemicalSpeciationCrossReferenceGSREF + " dataset(s). ";
        }
        if (inventoryName == null || inventoryName.length() == 0) {
            errors += "Missing inventory dataset. ";
        }
        if (detailedResultName == null || detailedResultName.length() == 0) {
            errors += "Missing " + DatasetType.strategyDetailedResult + " dataset. ";
        }

        //make sure the all the datasets actually exist
        EmfDataset[] gspros  = new EmfDataset[] {};
        if (gsproNames != null) {
            gspros = new EmfDataset[gsproNames.length];
            for (int i = 0; i < gsproNames.length; i++) {
                gspros[i] = getDataset(gsproNames[i]);
                if (gspros[i] == null) {
                    errors += "Uknown " + DatasetType.chemicalSpeciationProfilesGSPRO + " dataset, " + gsproNames[i] + ". ";
                }
            }
        }
        EmfDataset[] gsrefs = new EmfDataset[] {};
        if (gsrefNames != null) {
            gsrefs = new EmfDataset[gsrefNames.length];
            for (int i = 0; i < gsrefNames.length; i++) {
                gsrefs[i] = getDataset(gsrefNames[i]);
                if (gsrefs[i] == null) {
                    errors += "Uknown " + DatasetType.chemicalSpeciationCrossReferenceGSREF + " dataset, " + gsrefNames[i] + ". ";
                }
            }
        }
        
        EmfDataset inventory = null;
        inventory = getDataset(inventoryName);
        String inventoryTableName = qualifiedEmissionTableName(inventory);
        String inventoryVersion = new VersionedQuery(version(inventory.getId(), inventory.getDefaultVersion()), "inv").query();
        if (inventory == null) {
            errors += "Uknown inventory dataset, " + inventoryName + ". ";
        }
        
        //make sure tolerance dataset exists
        EmfDataset detailedResult = null;
        detailedResult = getDataset(detailedResultName);
        String detailedResultTableName = qualifiedEmissionTableName(detailedResult);
        String detailedResultVersion = new VersionedQuery(version(detailedResult.getId(), detailedResult.getDefaultVersion()), "dr").query();
        if (detailedResult == null) {
            errors += "Uknown " + DatasetType.strategyDetailedResult + " dataset, " + detailedResultName + ". ";
        }

        //go ahead and throw error from here, no need to validate anymore if the above is not there...
        if (errors.length() > 0) {
            throw new EmfException(errors);
        }
        
        //go ahead and throw errors from here, if there are some...
        if (errors.length() > 0) {
            throw new EmfException(errors);
        }
        
//        capIsPoint = checkTableForColumns(emissionTableName(dataset), "plantid,pointid,stackid,segment");
        
        //Outer SELECT clause
        sql = "select inv.fips,\n"
                + "inv.plantid,\n"
                + "inv.pointid,\n"
                + "inv.stackid,\n"
                + "inv.segment,\n"
                + "inv.scc,\n"
                + "case when gspro.species = 'PEC' then 'EC' when gspro.species = 'POC' then 'OC' end as poll,\n"
                + "scc.scc_description,\n"
                + "inv.plant,\n"
                + "inv.nei_unique_id,\n"
                + "inv.ann_emis * gspro.massfrac as inv_ann_emis,\n"
                + "coalesce(dr.final_emissions, inv.ann_emis) * gspro.massfrac as strat_ann_emis,\n"
                + "inv.ann_emis * gspro.massfrac - coalesce(dr.final_emissions, inv.ann_emis) * gspro.massfrac as inv_minus_strat_emis,\n"
                + "case when inv.ann_emis != null and inv.ann_emis != 0.0 then (inv.ann_emis - coalesce(dr.final_emissions, inv.ann_emis)) / inv.ann_emis * 100.0 end as pct_diff_inv_minus_strat,\n"
                + "dr.cm_abbrev,\n"
                + "ct.name as control_technology,\n"
                + "sg.name as source_group,\n"
                + "dr.annual_cost,\n"
                + "inv.design_capacity,\n"
                + "inv.design_capacity_unit_numerator,\n"
                + "inv.design_capacity_unit_denominator,\n"
                + "inv.ANNUAL_AVG_DAYS_PER_WEEK,\n"
                + "inv.ANNUAL_AVG_WEEKS_PER_YEAR,\n"
                + "inv.ANNUAL_AVG_HOURS_PER_DAY,\n"
                + "inv.ANNUAL_AVG_HOURS_PER_YEAR\n";

        sql += "from " + inventoryTableName + " inv\n"
            + "left outer join " + detailedResultTableName + " dr\n"
            + "on dr.source_id = inv.record_id\n"
            + "and " + detailedResultVersion + " \n"
            + "left outer join emf.control_measures cm\n"
            + "on cm.abbreviation = dr.cm_abbrev\n"
            + "left outer join emf.control_technologies ct\n"
            + "on ct.id = cm.control_technology\n"
            + "left outer join emf.source_groups sg\n"
            + "on sg.id = cm.source_group\n"
            + "left outer join reference.scc\n"
            + "on scc.scc = inv.scc\n"
            ;

        //union together all ...
        sql += "inner join ( \n";

        int i = 0;
        for (EmfDataset dataset : gsrefs) {
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion()), "smk").query();
            table = qualifiedEmissionTableName(dataset);
            if (i > 0) sql += " union \n";
            sql += "select scc, \n"
                + "code, \n"
                + "pollutant\n"
                + "from " + table + " smk \n"
                + "where " + version + "\n";
            ++i;
        }

        sql += ") gsref\n" 
            + "on gsref.scc = inv.scc\n"
            + "and gsref.pollutant = inv.poll\n";

        //union together all ...
        //make sure an only get one of the GSPRO records, the onw with the greatest massfrac
        sql += "inner join ( \n"
            + "select distinct on (code, pollutant, species) code, pollutant, species, massfrac\n"
            + "from (\n";

        i = 0;
        for (EmfDataset dataset : gspros) {
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion()), "smk").query();
            table = qualifiedEmissionTableName(dataset);
            if (i > 0) sql += " union \n";
            sql += "select code,\n"
                + "pollutant,\n"
                + "species,\n"
                + "massfrac\n"
                + "from " + table + " smk \n"
                + "where " + version + "\n"
                + "and species in ('PEC','POC')\n";
            ++i;
        }

        sql += ") gspro\n" 
            + "order by code, pollutant, species, massfrac desc\n"
            + ") gspro\n" 
            + "on gsref.code = gspro.code\n";
        
        sql += "where inv.poll = 'PM2_5'\n"
            + "and " + inventoryVersion + "\n";

        //add PM2_5 records
        sql += "union all\n"
            + "select inv.fips,\n"
            + "inv.plantid,\n"
            + "inv.pointid,\n"
            + "inv.stackid,\n"
            + "inv.segment,\n"
            + "inv.scc,\n"
            + "inv.poll,\n"
            + "scc.scc_description,\n"
            + "inv.plant,\n"
            + "inv.nei_unique_id,\n"
            + "inv.ann_emis as inv_ann_emis,\n"
            + "coalesce(dr.final_emissions, inv.ann_emis) as strat_ann_emis,\n"
            + "inv.ann_emis - coalesce(dr.final_emissions, inv.ann_emis) as inv_minus_strat_emis,\n"
            + "case when inv.ann_emis != null and inv.ann_emis != 0.0 then (inv.ann_emis - coalesce(dr.final_emissions, inv.ann_emis)) / inv.ann_emis * 100.0 end as pct_diff_inv_minus_strat,\n"
            + "dr.cm_abbrev,\n"
            + "ct.name as control_technology,\n"
            + "sg.name as source_group,\n"
            + "dr.annual_cost,\n"
            + "inv.design_capacity,\n"
            + "inv.design_capacity_unit_numerator,\n"
            + "inv.design_capacity_unit_denominator,\n"
            + "inv.ANNUAL_AVG_DAYS_PER_WEEK,\n"
            + "inv.ANNUAL_AVG_WEEKS_PER_YEAR,\n"
            + "inv.ANNUAL_AVG_HOURS_PER_DAY,\n"
            + "inv.ANNUAL_AVG_HOURS_PER_YEAR\n";

        sql += "from " + inventoryTableName + " inv\n"
            + "left outer join " + detailedResultTableName + " dr\n"
            + "on dr.source_id = inv.record_id\n"
            + "and " + detailedResultVersion + " \n"
            + "left outer join emf.control_measures cm\n"
            + "on cm.abbreviation = dr.cm_abbrev\n"
            + "left outer join emf.control_technologies ct\n"
            + "on ct.id = cm.control_technology\n"
            + "left outer join emf.source_groups sg\n"
            + "on sg.id = cm.source_group\n"
            + "left outer join reference.scc\n"
            + "on scc.scc = inv.scc\n"
            ;
    
        sql += "where inv.poll = 'PM2_5'\n"
            + "and " + inventoryVersion + "\n";

        sql += "order by fips, plantid, pointid, stackid, segment, scc, poll\n";
        
//        sql = query(sql, true);
        sql = "CREATE TABLE " + emissionDatasourceName + "." + tableName + " AS " + sql;
        System.out.println(sql);
        
        return sql;
    }

    protected String query(String partialQuery, boolean createClause) throws EmfException {

        SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissionDatasourceName, tableName );
        return parser.parse(partialQuery, createClause);
    }

    private EmfDataset getDataset(String dsName) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getDataset(session, dsName);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("The dataset name " + dsName + " is not valid");
        } finally {
            session.close();
        }
    }

    private Version version(int datasetId, int version) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, session);
        } finally {
            session.close();
        }
    }
    private String qualifiedEmissionTableName(Dataset dataset) {
        return qualifiedName(emissionTableName(dataset));
    }

    private String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable();
    }

    private String qualifiedName(String table) {
        return emissionDatasourceName + "." + table;
    }

    protected boolean checkTableForColumns(String table, String colList) throws EmfException {
        String query = "select public.check_table_for_columns('" + table + "', '" + colList + "', ',');";
        ResultSet rs = null;
        boolean tableHasColumns = false;
        //System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = dbServer.getEmissionsDatasource().query().executeQuery(query);
            while (rs.next()) {
                tableHasColumns = rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }
        return tableHasColumns;
    }
}
