package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.Session;

public class SQLAnnualReportQuery {

    private HibernateSessionFactory sessionFactory;
    
    public SQLAnnualReportQuery(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    public String getSectorListTableQuery(int caseId, String gridName) throws EmfException {
        StringBuilder sql = new StringBuilder();
//        for (int i = 0; i < caseIds.length; ++i) {
//            if ( i > 0 ) sql.append( " union all " );
        sql.append(" select ");
        sql.append(" internal_sources.table_name, " );
        sql.append(" cases_caseinputs.dataset_id, ");
        sql.append(" versions.version");
        sql.append(" FROM ");
        sql.append(" emf.internal_sources, " );
        sql.append(" cases.input_envt_vars, ");
        sql.append(" cases.cases, ");
        sql.append(" cases.cases_caseinputs, ");
        sql.append(" emf.versions ");
        
        sql.append(" WHERE ");
        sql.append(" input_envt_vars.id = cases_caseinputs.envt_vars_id " );
        sql.append(" AND cases.id = cases_caseinputs.case_id ");
        sql.append(" AND input_envt_vars.name = 'SECTORLIST' ");
        sql.append(" AND internal_sources.dataset_id = cases_caseinputs.dataset_id ");
        sql.append(" AND cases_caseinputs.version_id = versions.id ");
        sql.append(" AND cases.id = cases_caseinputs.case_id ");
        sql.append(" AND cases.id = " + caseId );
        sql.append(" AND cases_caseinputs.region_id = " +
        		" (select id from emf.georegions where name = '" + gridName + "')" );
        
        return sql.toString();
    }
    
    public String getSectorsCasesQuery(String tableName, Integer dsId, Integer versionId) throws EmfException {
        StringBuilder sql = new StringBuilder();
        Session session = sessionFactory.getSession();
        
        Version version = version(session, dsId, versionId );
        String datasetVersionedQuery = new VersionedQuery(version).query();
//        for (int i = 0; i < caseIds.length; ++i) {
//            if ( i > 0 ) sql.append( " union all " );
        sql.append(" select " );
        sql.append(" sector, " );
        sql.append(" sectorcase ");
        sql.append(" from " + qualifiedName(tableName) );
        sql.append(" where " +datasetVersionedQuery );
              
        return sql.toString();
    }
    
    public String getReportTableQuery(String sector, String mapSectorCase, String gridName) throws EmfException {
        StringBuilder sql = new StringBuilder();

        sql.append(" select ");
        sql.append(" internal_sources.table_name, " );
        sql.append(" outputs.dataset_id, ");
        sql.append(" datasets.default_version ");
        //            sql.append( caseAbbrev + " as caseAbbrev" );
        sql.append(" FROM ");
        sql.append(" emf.internal_sources, " );
        sql.append(" cases.outputs, ");
        sql.append(" cases.cases_casejobs, ");
        sql.append(" cases.cases, ");
        sql.append(" emf.datasets ");
        sql.append(" WHERE ");
        sql.append(" cases.id = outputs.case_id " );
        sql.append(" AND cases_casejobs.id = outputs.job_id ");
        sql.append(" AND outputs.dataset_id = datasets.id ");
        sql.append(" AND internal_sources.dataset_id = datasets.id ");
        sql.append(" AND cases.abbreviation_id = ( select id from cases.case_abbreviations where name = '" + mapSectorCase + "')");
        sql.append(" AND cases_casejobs.sector_id = (select id from emf.sectors where name = '" + sector + "')");
        sql.append(" AND cases_casejobs.region_id = " +
                " (select id from emf.georegions where name = '" + gridName + "') " );
        sql.append(" AND datasets.dataset_type = " +
        		"(select id from emf.dataset_types where name = 'Smkmerge report state annual summary (CSV)')");

        return sql.toString();
    }
    
    public String getSectorsReportsQuery(String[] tableNames, Integer[] dsIds, Integer[] dsVers, String caseAbbrev) throws EmfException {
        StringBuilder sql = new StringBuilder();
        Session session = sessionFactory.getSession();
        for (int i = 0; i < tableNames.length; i++) {
            // skip the loop if a sector is not in the current case
            if ( dsIds[i] != null && dsIds[i] !=null) {
                Version version = version(session, dsIds[i], dsVers[i] );            
                String datasetVersionedQuery = new VersionedQuery(version).query();
                if ( sql.toString().contains("select")) sql.append( " union all " );
                sql.append(" select " );
                sql.append(" state,");
                sql.append(" sector,");
                sql.append(" species,");
                sql.append(" ann_emis as " + caseAbbrev );
                sql.append(" from " + qualifiedName(tableNames[i]) );
                sql.append(" where " + datasetVersionedQuery );
            }   
        }
        return sql.toString();
    }
    
//    private String getCaseAbbreviation(Case caseObj) {
//        return (caseObj != null ? caseObj.getAbbreviation().getName() : "");
//    }
    
    public static void main(String[] args) {
        
//        try {
//            //System.out.println(new SQLAnnualReportQuery(null).createCompareQuery(new int[] {7, 8}));
//        } catch (EmfException e) {
//            // NOTE Auto-generated catch block
//            e.printStackTrace();
//        }
    }

//    private Case getCase(int caseId) throws EmfException {
//        Session session = sessionFactory.getSession();
//        try {
//            return new CaseDAO().getCase(caseId, session);
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not get Case, id = " + caseId);
//        } finally {
//            if (session != null && session.isConnected())
//                session.close();
//        }
//    }
    
    private Version version(int datasetId, int version) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, session);
        } finally {
            session.close();
        }
    }
    
    private Version version(Session session, int datasetId, int version) {
        Versions versions = new Versions();
        return versions.get(datasetId, version, session);
    }
    
    private String qualifiedName(String table) throws EmfException {
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("versions".equalsIgnoreCase(table.toLowerCase())) {
            throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        return "emissions." + table;
    }

}
